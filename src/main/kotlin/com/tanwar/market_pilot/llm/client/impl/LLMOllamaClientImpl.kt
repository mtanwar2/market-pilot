package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.OllamaChatRequest
import com.tanwar.market_pilot.llm.model.OllamaChatResponse
import com.tanwar.market_pilot.llm.model.OllamaFunction
import com.tanwar.market_pilot.llm.model.OllamaMessage
import com.tanwar.market_pilot.llm.model.OllamaOptions
import com.tanwar.market_pilot.llm.model.OllamaTool
import com.tanwar.market_pilot.llm.model.OllamaToolCall
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.ResponseFormat
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.model.ToolCall
import com.tanwar.market_pilot.llm.properties.LlmProperties
import com.tanwar.market_pilot.llm.properties.TimeoutProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.util.concurrent.TimeoutException

@Component("ollama")
class OllamaLlmClient(
    llmProperties: LlmProperties,
    private val timeoutProperties: TimeoutProperties,
    webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper
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

    override fun generate(
        request: LlmRequest
    ): Mono<LlmResponse> {

        val ollamaRequest = toOllamaRequest(
            request = request,
            model = config.model,
            objectMapper = objectMapper
        )

        val startedAt = System.nanoTime()

        log.info(
            "Calling Ollama model={} messageCount={}",
            config.model,
            request.messages.size
        )

        return webClient
            .post()
            .uri("/api/chat")
            .bodyValue(ollamaRequest)
            .retrieve()
            .bodyToMono<OllamaChatResponse>()

            /*
             * Convert Ollama response into our common
             * LlmResponse model.
             */
            .map { response ->

                log.info(
                    "Ollama responded durationMs={} finishReason={} responseLength={} inputTokens={} outputTokens={}",
                    elapsedMs(startedAt),
                    response.done_reason,
                    response.message.content?.length,
                    response.prompt_eval_count,
                    response.eval_count
                )

                toLlmResponse(response)
            }

            /*
             * Convert timeout into our domain exception.
             */
            .timeout(
                Duration.ofMillis(
                    timeoutProperties.durationMs
                )
            )
            .onErrorMap(
                TimeoutException::class.java
            ) { ex ->

                log.warn(
                    "Ollama request timed out durationMs={} timeoutMs={}",
                    elapsedMs(startedAt),
                    timeoutProperties.durationMs
                )

                LlmException(
                    statusCode = 504,
                    message = "Ollama request timed out",
                    cause = ex
                )
            }

            /*
             * Convert HTTP errors into LlmException
             * so the retry layer can inspect statusCode.
             */
            .onErrorMap(
                WebClientResponseException::class.java
            ) { ex ->

                val statusCode = ex.statusCode.value()

                log.warn(
                    "Ollama HTTP request failed durationMs={} statusCode={}",
                    elapsedMs(startedAt),
                    statusCode
                )

                LlmException(
                    message = "Ollama request failed with HTTP $statusCode",
                    cause = ex,
                    statusCode = statusCode
                )
            }

            /*
             * Preserve existing LlmException.
             * Convert all other unexpected exceptions
             * into our domain exception.
             */
            .onErrorMap { ex ->

                if (ex is LlmException) {
                    ex
                } else {

                    log.error(
                        "Unexpected Ollama client failure durationMs={} exceptionType={}",
                        elapsedMs(startedAt),
                        ex.javaClass.simpleName,
                        ex
                    )

                    LlmException(
                        message = "Failed to generate response from Ollama",
                        cause = ex
                    )
                }
            }
    }

    companion object {

        private val log =
            LoggerFactory.getLogger(OllamaLlmClient::class.java)

        private fun elapsedMs(startedAt: Long): Long =
            (System.nanoTime() - startedAt) / 1_000_000

        internal fun toOllamaRequest(
            request: LlmRequest,
            model: String,
            objectMapper: ObjectMapper
        ): OllamaChatRequest {

            val options =
                if (
                    request.temperature != null ||
                    request.maxTokens != null
                ) {
                    OllamaOptions(
                        temperature = request.temperature,
                        numPredict = request.maxTokens
                    )
                } else {
                    null
                }

            return OllamaChatRequest(
                model = model,
                messages = request.messages.map { message ->
                    when (message.role) {
                        LlmRole.SYSTEM,
                        LlmRole.USER -> OllamaMessage(
                            role = message.role.name.lowercase(),
                            content = message.content
                        )

                        LlmRole.ASSISTANT -> {
                            val toolCall = message.toolCall

                            if (toolCall != null) {
                                OllamaMessage(
                                    role = "assistant",
                                    content = message.content,
                                    toolCalls = listOf(
                                        OllamaToolCall(
                                            id = toolCall.id,
                                            function = OllamaFunction(
                                                name = toolCall.name,
                                                arguments =
                                                    toolCall.arguments
                                            )
                                        )
                                    )
                                )
                            } else {
                                OllamaMessage(
                                    role = "assistant",
                                    content = message.content
                                )
                            }
                        }

                        LlmRole.TOOL -> {
                            val toolResult = message.toolResult
                                ?: throw IllegalArgumentException(
                                    "TOOL message must contain toolResult"
                                )

                            OllamaMessage(
                                role = "tool",
                                content = objectMapper.writeValueAsString(
                                    toolResult.content
                                ),
                                toolName = toolResult.toolName
                            )
                        }
                    }
                },
                stream = false,
                tools = request.tools
                    .takeIf { it.isNotEmpty() }
                    ?.map { tool ->
                        OllamaTool(
                            function = OllamaFunction(
                                name = tool.name,
                                description = tool.description,
                                parameters = tool.parameters
                            )
                        )
                    },
                format =
                    if (
                        request.responseFormat?.type ==
                        ResponseFormat.Type.JSON
                    ) {
                        "json"
                    } else {
                        null
                    },
                options = options
            )
        }

        internal fun toLlmResponse(
            response: OllamaChatResponse
        ): LlmResponse {
            val inputTokens = response.prompt_eval_count
            val outputTokens = response.eval_count

            return LlmResponse(
                content = response.message.content
                    ?.ifBlank { null },
                usage = TokenUsage(
                    inputTokens = inputTokens,
                    outputTokens = outputTokens,
                    totalTokens =
                        if (
                            inputTokens != null &&
                            outputTokens != null
                        ) {
                            inputTokens + outputTokens
                        } else {
                            null
                        }
                ),
                finishReason = response.done_reason,
                toolCalls = response.message.toolCalls
                    .orEmpty()
                    .map { toolCall ->
                        ToolCall(
                            id = toolCall.id,
                            name = toolCall.function.name,
                            arguments =
                                toolCall.function.arguments.orEmpty()
                        )
                    }
            )
        }
    }
}