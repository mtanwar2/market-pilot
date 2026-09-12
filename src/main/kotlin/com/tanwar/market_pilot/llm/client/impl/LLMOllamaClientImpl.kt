package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.OllamaChatRequest
import com.tanwar.market_pilot.llm.model.OllamaChatResponse
import com.tanwar.market_pilot.llm.model.OllamaMessage
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.properties.LlmProperties
import com.tanwar.market_pilot.llm.properties.TimeoutProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Duration
import java.util.concurrent.TimeoutException

@Component("ollama")
class OllamaLlmClient(
    llmProperties: LlmProperties,
    private val timeoutProperties: TimeoutProperties,
    webClientBuilder: WebClient.Builder
) : LlmClient {

    private val config =
        llmProperties.providers["ollama"]
            ?: throw IllegalStateException(
                "Ollama configuration is missing"
            )

    private val webClient =
        webClientBuilder
            .baseUrl(
                config.baseUrl
                    ?: "http://localhost:11434"
            )
            .build()

    init {
        log.info(
            "Ollama client initialized model={} baseUrl={}",
            config.model,
            config.baseUrl ?: "http://localhost:11434"
        )
    }

    override fun generate(request: LlmRequest): LlmResponse {

        val ollamaRequest = OllamaChatRequest(
            model = config.model,

            messages = request.messages.map { message ->
                OllamaMessage(
                    role = message.role.name.lowercase(),
                    content = message.content
                )
            },

            stream = false
        )

        val startedAt = System.nanoTime()

        log.info(
            "Calling Ollama model={} messageCount={}",
            config.model,
            request.messages.size
        )

        try {

            val response = webClient
                .post()
                .uri("/api/chat")
                .bodyValue(ollamaRequest)
                .retrieve()
                .bodyToMono<OllamaChatResponse>()
                .timeout(Duration.ofMillis(timeoutProperties.durationMs))
                .onErrorMap(TimeoutException::class.java) { ex ->
                    LlmException(
                        statusCode = 504,
                        message = "Ollama request timed out",
                        cause = ex
                    )
                }
                .block()
                ?: throw LlmException(
                    502,
                    "Ollama returned an empty response"
                )

            log.info(
                "Ollama responded durationMs={} finishReason={} responseLength={} inputTokens={} outputTokens={}",
                elapsedMs(startedAt),
                response.done_reason,
                response.message.content.length,
                response.prompt_eval_count,
                response.eval_count
            )

            return LlmResponse(
                content = response.message.content,

                usage = TokenUsage(
                    inputTokens = response.prompt_eval_count,
                    outputTokens = response.eval_count,

                    totalTokens =
                        if (
                            response.prompt_eval_count != null &&
                            response.eval_count != null
                        ) {
                            response.prompt_eval_count +
                                    response.eval_count
                        } else {
                            null
                        }
                ),

                finishReason = response.done_reason
            )

        } catch (ex: WebClientResponseException) {
            val statusCode = ex.statusCode.value()

            log.warn(
                "Ollama HTTP request failed durationMs={} statusCode={}",
                elapsedMs(startedAt),
                statusCode
            )

            throw LlmException(
                message = "Ollama request failed with HTTP $statusCode",
                cause = ex,
                statusCode = statusCode
            )

        } catch (ex: LlmException) {
            log.warn(
                "Ollama request rejected durationMs={} statusCode={} reason={}",
                elapsedMs(startedAt),
                ex.statusCode,
                ex.message
            )
            throw ex

        } catch (ex: Exception) {
            log.error(
                "Unexpected Ollama client failure durationMs={} exceptionType={}",
                elapsedMs(startedAt),
                ex.javaClass.simpleName,
                ex
            )

            throw LlmException(
                message = "Failed to generate response from Ollama",
                cause = ex
            )
        }
    }

    companion object {

        private val log =
            LoggerFactory.getLogger(OllamaLlmClient::class.java)

        private fun elapsedMs(startedAt: Long): Long =
            (System.nanoTime() - startedAt) / 1_000_000
    }
}