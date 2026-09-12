package com.tanwar.market_pilot.service

import com.tanwar.market_pilot.config.ChatLogContext
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ChatService(
    private val llmClientFactory: LlmClientFactory
) {

    fun chat(request: ChatRequest): ChatResponse {
        if (request.message.isBlank()) {
            log.warn(
                "Rejected empty chat message conversationId={}",
                request.conversationId ?: "new"
            )
        }
        require(request.message.isNotBlank()) {
            "Message cannot be empty"
        }

        val isNewConversation = request.conversationId == null
        val conversationId =
            request.conversationId
                ?: UUID.randomUUID().toString()
        val turnId = UUID.randomUUID().toString()

        MDC.put(ChatLogContext.CONVERSATION_ID, conversationId)
        MDC.put(ChatLogContext.TURN_ID, turnId)
        val startedAt = System.nanoTime()

        try {
            log.info(
                "Started chat turn conversationId={} turnId={} newConversation={} messageLength={}",
                conversationId,
                turnId,
                isNewConversation,
                request.message.length
            )

            val llmRequest = LlmRequest(
                messages = listOf(
                    LlmMessage(
                        role = LlmRole.USER,
                        content = request.message
                    )
                )
            )

            val client = llmClientFactory.getClient()
            val response = client.generate(llmRequest)

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

            return ChatResponse(
                conversationId = conversationId,
                turnId = turnId,
                message = response.content
            )
        } catch (ex: Exception) {
            log.error(
                "Chat turn failed conversationId={} turnId={} durationMs={} exceptionType={} reason={}",
                conversationId,
                turnId,
                elapsedMs(startedAt),
                ex.javaClass.simpleName,
                ex.message
            )
            throw ex
        }
    }

    private fun elapsedMs(startedAt: Long): Long =
        (System.nanoTime() - startedAt) / 1_000_000

    companion object {
        private val log = LoggerFactory.getLogger(ChatService::class.java)
    }
}