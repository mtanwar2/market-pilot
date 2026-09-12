package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.GeminiContent
import com.tanwar.market_pilot.llm.model.GeminiGenerationConfig
import com.tanwar.market_pilot.llm.model.GeminiPart
import com.tanwar.market_pilot.llm.model.GeminiRequest
import com.tanwar.market_pilot.llm.model.GeminiResponse
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.properties.LlmProperties
import com.tanwar.market_pilot.llm.properties.TimeoutProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Duration

@Component("gemini")
class GeminiLlmClient(
    llmProperties: LlmProperties,
    private val timeoutProperties: TimeoutProperties,
    webClientBuilder: WebClient.Builder
) : LlmClient {

    private val config =
        llmProperties.providers["gemini"]
            ?: throw IllegalStateException(
                "Gemini configuration is missing"
            )

    private val webClient =
        webClientBuilder
            .baseUrl(
                config.baseUrl
                    ?: "https://generativelanguage.googleapis.com"
            )
            .build()

    init {
        log.info(
            "Gemini client initialized model={} baseUrl={} apiKeyConfigured={}",
            config.model,
            config.baseUrl
                ?: "https://generativelanguage.googleapis.com",
            !config.apiKey.isNullOrBlank()
        )
    }

    override fun generate(request: LlmRequest): LlmResponse {

        val systemMessages = request.messages
            .filter { it.role == LlmRole.SYSTEM }

        val conversationMessages = request.messages
            .filter { it.role != LlmRole.SYSTEM }

        val geminiRequest = GeminiRequest(

            systemInstruction =
                if (systemMessages.isNotEmpty()) {
                    GeminiContent(
                        parts = systemMessages.map {
                            GeminiPart(
                                text = it.content
                            )
                        }
                    )
                } else {
                    null
                },

            contents = conversationMessages.map { message ->
                GeminiContent(
                    role = when (message.role) {
                        LlmRole.USER -> "user"
                        LlmRole.ASSISTANT -> "assistant"

                        LlmRole.SYSTEM ->
                            error(
                                "SYSTEM message should not be in contents"
                            )

                        LlmRole.TOOL ->
                            error(
                                "TOOL message is not supported by Gemini yet"
                            )
                    },
                    parts = listOf(
                        GeminiPart(
                            text = message.content
                        )
                    )
                )
            },

            generationConfig = GeminiGenerationConfig(
                temperature = request.temperature,
                maxOutputTokens = request.maxTokens
            )
        )

        val startedAt = System.nanoTime()

        log.info(
            "Calling Gemini model={} messageCount={} systemMessageCount={} conversationMessageCount={}",
            config.model,
            request.messages.size,
            systemMessages.size,
            conversationMessages.size
        )

        try {

            val apiKey = config.apiKey
                ?: throw LlmException(
                    401,
                    "Gemini API key is missing"
                ) as Throwable

            val response = webClient
                .post()
                .uri(
                    "/v1beta/models/${config.model}:generateContent"
                )
                .header(
                    "x-goog-api-key",
                    apiKey
                )
                .bodyValue(geminiRequest)
                .retrieve()
                .bodyToMono<GeminiResponse>()
                .timeout(Duration.ofMillis(timeoutProperties.durationMs))
                .block()
                ?: throw LlmException(
                    204,
                    "Gemini returned an empty response"
                )

            val candidate =
                response.candidates?.firstOrNull()
                    ?: throw LlmException(
                        204,
                        "Gemini returned no candidates"
                    )

            val content =
                candidate.content
                    ?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString("")
                    ?: throw LlmException(
                        204,
                        "Gemini returned no text content"
                    )

            val usage = response.usageMetadata

            log.info(
                "Gemini responded durationMs={} finishReason={} responseLength={} inputTokens={} outputTokens={} totalTokens={}",
                elapsedMs(startedAt),
                candidate.finishReason,
                content.length,
                usage?.promptTokenCount,
                usage?.candidatesTokenCount,
                usage?.totalTokenCount
            )

            return LlmResponse(
                content = content,

                usage = TokenUsage(
                    inputTokens = usage?.promptTokenCount,
                    outputTokens = usage?.candidatesTokenCount,
                    totalTokens = usage?.totalTokenCount
                ),

                finishReason = candidate.finishReason
            )

        } catch (ex: WebClientResponseException) {

            log.error(
                "Gemini request failed durationMs={} statusCode={} reason={}",
                elapsedMs(startedAt),
                ex.statusCode,
                ex.responseBodyAsString,
                ex.message,
                ex
            )

            throw ex

        } catch (ex: WebClientResponseException) {

            val statusCode = ex.statusCode.value()

            log.error(
                "Gemini HTTP request failed durationMs={} statusCode={} reason={}",
                elapsedMs(startedAt),
                statusCode,
                ex.message,
                ex
            )

            throw LlmException(
                message = "Gemini request failed with HTTP $statusCode",
                cause = ex,
                statusCode = statusCode
            )

        } catch (ex: Exception) {

            log.error(
                "Gemini request failed durationMs={} reason={}",
                elapsedMs(startedAt),
                ex.message,
                ex
            )

            throw LlmException(
                503,
                message = "Failed to generate response from Gemini",
                cause = ex
            )
        }
    }

    companion object {

        private val log =
            LoggerFactory.getLogger(GeminiLlmClient::class.java)

        private fun elapsedMs(startedAt: Long): Long =
            (System.nanoTime() - startedAt) / 1_000_000
    }
}