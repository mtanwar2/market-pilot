package com.tanwar.market_pilot.service

import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatResponse
import com.tanwar.market_pilot.model.ChatRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class ChatService(
    private val llmClientFactory: LlmClientFactory
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

        val llmRequest = LlmRequest(
            messages = buildList {
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
            }
        )

        val client = llmClientFactory.getClient()

        return client
            .generate(llmRequest)

            /*
             * This code executes when the LLM response
             * arrives and the Mono emits LlmResponse.
             */
            .map { response ->

                log.info(
                    "Completed chat turn conversationId={} turnId={} durationMs={} responseLength={} finishReason={} inputTokens={} outputTokens={} totalTokens={}",
                    conversationId,
                    turnId,
                    elapsedMs(startedAt),
                    response.content.length,
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

            /*
             * Errors now arrive as reactive error signals.
             * This is the reactive equivalent of your catch block.
             */
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

    private fun elapsedMs(startedAt: Long): Long =
        (System.nanoTime() - startedAt) / 1_000_000

    private fun ChatRole.toLlmRole(): LlmRole =
        when (this) {
            ChatRole.USER -> LlmRole.USER
            ChatRole.ASSISTANT -> LlmRole.ASSISTANT
        }

    companion object {

        private const val ANSWER_LAST_USER_MESSAGE_INSTRUCTION =
            "Reply only to the final user message. " +
                "Earlier messages are conversation history for context; " +
                "do not answer them again."

        private val log =
            LoggerFactory.getLogger(ChatService::class.java)
    }
}