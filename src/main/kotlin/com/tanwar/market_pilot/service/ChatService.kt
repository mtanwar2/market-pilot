package com.tanwar.market_pilot.service

import com.tanwar.market_pilot.agent.impl.MarketPilotAgent
import com.tanwar.market_pilot.config.ChatLogContext
import com.tanwar.market_pilot.config.MdcContextBridge
import com.tanwar.market_pilot.llm.audit.AuditContext
import com.tanwar.market_pilot.llm.model.LlmMessage
import com.tanwar.market_pilot.llm.model.LlmRole
import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatResponse
import com.tanwar.market_pilot.model.ChatRole
import com.tanwar.market_pilot.security.PromptInjectionGuard
import com.tanwar.market_pilot.security.UserContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class ChatService(
    private val agent: MarketPilotAgent,
    private val promptInjectionGuard: PromptInjectionGuard,
    private val userContext: UserContext
) {

    fun chat(request: ChatRequest): Mono<ChatResponse> {

        // Validate user messages for prompt injection.
        promptInjectionGuard.validate(
            request.messages
        )

        // Validate the turn ID.
        val turnId = request.turnId?.trim().orEmpty()

        require(turnId.isNotBlank()) {
            "Turn ID cannot be empty"
        }

        // Validate that at least one message exists.
        require(request.messages.isNotEmpty()) {
            "Messages cannot be empty"
        }

        // Validate that no message is blank.
        require(request.messages.all { it.content.isNotBlank() }) {
            "Message content cannot be empty"
        }

        // The final message must always come from the user.
        require(request.messages.last().role == ChatRole.USER) {
            "The last message in the transcript must be from the user"
        }

        // Determine whether this is a new conversation.
        val isNewConversation = request.conversationId == null

        // Use the existing conversation ID or create a new one.
        val conversationId =
            request.conversationId
                ?: UUID.randomUUID().toString()

        // Capture the request ID from MDC while we are still
        // on the servlet request thread.
        val requestId =
            MDC.get(ChatLogContext.REQUEST_ID)

        // Capture the current user and request context
        // for audit logging.
        val auditContext = AuditContext(
            userId = userContext.currentUser().userId,
            conversationId = conversationId,
            turnId = turnId
        )

        // Start measuring the complete chat request.
        val startedAt = System.nanoTime()

        log.info(
            "CHAT_STARTED conversationId={} turnId={} newConversation={} messageCount={} lastMessageLength={}",
            conversationId,
            turnId,
            isNewConversation,
            request.messages.size,
            request.messages.last().content.length
        )

        val conversationMessages = buildList {

            request.messages.forEach { message ->
                add(
                    LlmMessage(
                        role = message.role.toLlmRole(),
                        content = message.content
                    )
                )
            }

        }.toMutableList()

        return agent.run(
            conversationMessages,
            auditContext
        )
            .flatMap { response ->

                Mono.deferContextual { context ->

                    // Reactor Context is available here even if
                    // execution has moved to another thread.
                    //
                    // Copy the Reactor Context into MDC for the
                    // duration of this synchronous logging block.
                    MdcContextBridge.withMdc(context) {

                        log.info(
                            "MDC_BRIDGE_TEST requestId={} conversationId={} turnId={}",
                            MDC.get(ChatLogContext.REQUEST_ID),
                            MDC.get(ChatLogContext.CONVERSATION_ID),
                            MDC.get(ChatLogContext.TURN_ID)
                        )

                        val durationMs =
                            (System.nanoTime() - startedAt) / 1_000_000

                        log.info(
                            "CHAT_COMPLETED conversationId={} turnId={} durationMs={}",
                            conversationId,
                            turnId,
                            durationMs
                        )
                    }

                    Mono.just(
                        ChatResponse(
                            conversationId = conversationId,
                            turnId = turnId,
                            message = response.content
                        )
                    )
                }
            }

            // Reactor Context is propagated independently of threads.
            // The MDC bridge reads these values later and temporarily
            // places them into MDC.
            .contextWrite { context ->
                val withRequestId =
                    requestId?.let {
                        context.put(
                            ChatLogContext.REQUEST_ID,
                            it
                        )
                    } ?: context

                withRequestId
                    .put(
                        ChatLogContext.CONVERSATION_ID,
                        conversationId
                    )
                    .put(
                        ChatLogContext.TURN_ID,
                        turnId
                    )
            }
    }

    private fun ChatRole.toLlmRole(): LlmRole =
        when (this) {
            ChatRole.USER -> LlmRole.USER
            ChatRole.ASSISTANT -> LlmRole.ASSISTANT
        }

    companion object {

        private val log =
            LoggerFactory.getLogger(ChatService::class.java)
    }
}