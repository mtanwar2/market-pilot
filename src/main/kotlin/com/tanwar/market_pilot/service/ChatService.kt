package com.tanwar.market_pilot.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.llm.model.ToolDefinition
import com.tanwar.market_pilot.llm.model.ToolResult
import com.tanwar.market_pilot.llm.tool.ToolExecutor
import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatResponse
import com.tanwar.market_pilot.model.ChatRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class ChatService(
    private val llmClientFactory: LlmClientFactory,
    private val toolExecutor: ToolExecutor
) {

    fun chat(request: ChatRequest): Mono<ChatResponse> {

        val turnId = request.turnId?.trim().orEmpty()

        require(turnId.isNotBlank()) {
            "Turn ID cannot be empty"
        }

        require(request.messages.isNotEmpty()) {
            "Messages cannot be empty"
        }

        require(request.messages.all { it.content.isNotBlank() }) {
            "Message content cannot be empty"
        }

        require(request.messages.last().role == ChatRole.USER) {
            "The last message in the transcript must be from the user"
        }

        val isNewConversation = request.conversationId == null

        val conversationId =
            request.conversationId
                ?: UUID.randomUUID().toString()

        val startedAt = System.nanoTime()

        log.info(
            "Started chat turn conversationId={} turnId={} newConversation={} messageCount={} lastMessageLength={}",
            conversationId,
            turnId,
            isNewConversation,
            request.messages.size,
            request.messages.last().content.length
        )

        val conversationMessages = buildList {
            add(
                LlmMessage(
                    role = LlmRole.SYSTEM,
                    content = ANSWER_LAST_USER_MESSAGE_INSTRUCTION
                )
            )

            request.messages.forEach { message ->
                add(
                    LlmMessage(
                        role = message.role.toLlmRole(),
                        content = message.content
                    )
                )
            }
        }.toMutableList()

        val client = llmClientFactory.getClient()

        return generateWithTools(
            client = client,
            conversationMessages = conversationMessages,
            round = 0
        )
            .map { response ->

                log.info(
                    "Completed chat turn conversationId={} turnId={} durationMs={} responseLength={} finishReason={} inputTokens={} outputTokens={} totalTokens={}",
                    conversationId,
                    turnId,
                    elapsedMs(startedAt),
                    response.content?.length,
                    response.finishReason,
                    response.usage.inputTokens,
                    response.usage.outputTokens,
                    response.usage.totalTokens
                )

                ChatResponse(
                    conversationId = conversationId,
                    turnId = turnId,
                    message = response.content
                )
            }
            .doOnError { ex ->

                log.error(
                    "Chat turn failed conversationId={} turnId={} durationMs={} exceptionType={} reason={}",
                    conversationId,
                    turnId,
                    elapsedMs(startedAt),
                    ex.javaClass.simpleName,
                    ex.message
                )
            }
    }

    private fun generateWithTools(
        client: LlmClient,
        conversationMessages: MutableList<LlmMessage>,
        round: Int = 0
    ): Mono<LlmResponse> {

        if (round >= MAX_TOOL_ROUNDS) {
            return Mono.error(
                IllegalStateException(
                    "Exceeded maximum tool-call rounds ($MAX_TOOL_ROUNDS)"
                )
            )
        }

        val llmRequest = LlmRequest(
            messages = conversationMessages.toList(),
            tools = toolDefinitions
        )

        return client.generate(llmRequest)
            .flatMap { response ->

                // No tool call means the LLM has produced
                // the final answer.
                if (response.toolCalls.isEmpty()) {
                    return@flatMap Mono.just(response)
                }

                log.info(
                    "LLM requested {} tool(s): {}",
                    response.toolCalls.size,
                    response.toolCalls.map { it.name }
                )

                response.toolCalls.forEach { toolCall ->

                    // Remember the assistant's tool call
                    // as part of the conversation.
                    conversationMessages.add(
                        LlmMessage(
                            role = LlmRole.ASSISTANT,
                            toolCall = toolCall
                        )
                    )

                    // Execute the requested tool.
                    val result = toolExecutor.execute(toolCall)

                    log.info(
                        "Tool executed name={} result={}",
                        toolCall.name,
                        result
                    )

                    // Add the tool result to the conversation.
                    conversationMessages.add(
                        LlmMessage(
                            role = LlmRole.TOOL,
                            toolResult = ToolResult(
                                toolCallId = toolCall.id,
                                toolName = toolCall.name,
                                content = result
                            )
                        )
                    )
                }

                // Ask the LLM again, now that it has
                // received the tool result.
                generateWithTools(
                    client = client,
                    conversationMessages = conversationMessages,
                    round = round + 1
                )
            }
    }

    private fun elapsedMs(startedAt: Long): Long =
        (System.nanoTime() - startedAt) / 1_000_000

    private fun ChatRole.toLlmRole(): LlmRole =
        when (this) {
            ChatRole.USER -> LlmRole.USER
            ChatRole.ASSISTANT -> LlmRole.ASSISTANT
        }

    companion object {

        private const val MAX_TOOL_ROUNDS = 5

        private const val ANSWER_LAST_USER_MESSAGE_INSTRUCTION =
            "Reply only to the final user message. " +
                    "Earlier messages are conversation history for context; " +
                    "do not answer them again."

        private val toolDefinitions = listOf(
            ToolDefinition(
                name = "getStockPrice",
                description = "Get the current stock price for a stock symbol",
                parameters = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "symbol" to mapOf(
                            "type" to "string",
                            "description" to "Stock ticker symbol, for example NVDA"
                        )
                    ),
                    "required" to listOf("symbol")
                )
            )
        )

        private val log =
            LoggerFactory.getLogger(ChatService::class.java)
    }
}