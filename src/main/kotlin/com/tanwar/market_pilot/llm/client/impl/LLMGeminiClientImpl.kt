package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.GeminiContent
import com.tanwar.market_pilot.llm.model.GeminiFunctionDeclaration
import com.tanwar.market_pilot.llm.model.GeminiGenerationConfig
import com.tanwar.market_pilot.llm.model.GeminiPart
import com.tanwar.market_pilot.llm.model.GeminiRequest
import com.tanwar.market_pilot.llm.model.GeminiResponse
import com.tanwar.market_pilot.llm.model.GeminiTool
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.model.ToolCall
import com.tanwar.market_pilot.llm.model.ToolDefinition
import com.tanwar.market_pilot.llm.properties.LlmProperties
import com.tanwar.market_pilot.llm.properties.TimeoutProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeoutException

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

    override fun generate(request: LlmRequest): Mono<LlmResponse> {

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
                        LlmRole.ASSISTANT -> "model"

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
            tools = toGeminiTools(request.tools),
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

        val apiKey = config.apiKey
            ?.takeIf { it.isNotBlank() }
            ?: return Mono.error(
                LlmException(
                    message = "Gemini API key is not configured"
                )
            )

        return webClient
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

            /*
             * Convert GeminiResponse → LlmResponse.
             *
             * This executes when the Mono is subscribed
             * and the Gemini response arrives.
             */
            .map { response ->

                val candidate =
                    response.candidates?.firstOrNull()
                        ?: throw LlmException(
                            502,
                            "Gemini returned no candidates"
                        )

                val content =
                    candidate.content
                        ?.parts
                        ?.mapNotNull { it.text }
                        ?.joinToString("")
                        ?: throw LlmException(
                            502,
                            "Gemini returned no text content"
                        )
                val toolCalls =
                    candidate.content
                        ?.parts
                        .orEmpty()
                        .mapNotNull { part ->
                            part.functionCall?.let { functionCall ->
                                ToolCall(
                                    name = functionCall.name,
                                    arguments = functionCall.args ?: emptyMap()
                                )
                            }
                        }

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

                LlmResponse(
                    content = content,

                    usage = TokenUsage(
                        inputTokens = usage?.promptTokenCount,
                        outputTokens = usage?.candidatesTokenCount,
                        totalTokens = usage?.totalTokenCount
                    ),

                    finishReason = candidate.finishReason,
                    toolCalls = toolCalls
                )
            }

            /*
             * Timeout happens asynchronously.
             * Convert it into our domain exception so
             * the retry layer can recognize HTTP 504.
             */
            .timeout(
                Duration.ofMillis(
                    timeoutProperties.durationMs
                )
            )
            .onErrorMap(TimeoutException::class.java) { ex ->
                log.warn(
                    "Gemini request timed out durationMs={} timeoutMs={}",
                    elapsedMs(startedAt),
                    timeoutProperties.durationMs
                )

                LlmException(
                    statusCode = 504,
                    message = "Gemini request timed out",
                    cause = ex
                )
            }

            /*
             * Convert HTTP errors into LlmException.
             *
             * 429 / 500 / 503 / etc. are preserved
             * as status codes for the retry policy.
             */
            .onErrorMap(
                WebClientResponseException::class.java
            ) { ex ->

                val statusCode = ex.statusCode.value()

                log.warn(
                    "Gemini HTTP request failed durationMs={} statusCode={}",
                    elapsedMs(startedAt),
                    statusCode
                )

                LlmException(
                    message = "Gemini request failed with HTTP $statusCode",
                    cause = ex,
                    statusCode = statusCode
                )
            }

            /*
             * Preserve our own domain exceptions.
             */
            .onErrorMap { ex ->

                if (ex is LlmException) {
                    ex
                } else {
                    log.error(
                        "Unexpected Gemini client failure durationMs={} exceptionType={}",
                        elapsedMs(startedAt),
                        ex.javaClass.simpleName,
                        ex
                    )

                    LlmException(
                        message = "Failed to generate response from Gemini",
                        cause = ex
                    )
                }
            }
    }

    companion object {

        private val log =
            LoggerFactory.getLogger(GeminiLlmClient::class.java)

        private fun elapsedMs(startedAt: Long): Long =
            (System.nanoTime() - startedAt) / 1_000_000
    }

    private fun toGeminiTools(
        tools: List<ToolDefinition>
    ): List<GeminiTool>? {

        if (tools.isEmpty()) {
            return null
        }

        return listOf(
            GeminiTool(
                functionDeclarations = tools.map { tool ->
                    GeminiFunctionDeclaration(
                        name = tool.name,
                        description = tool.description,
                        parameters = tool.parameters
                    )
                }
            )
        )
    }
}