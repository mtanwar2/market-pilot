package com.tanwar.market_pilot.controller

import com.tanwar.market_pilot.config.ChatLogContext
import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatResponse
import com.tanwar.market_pilot.service.ChatService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun chat(
        @Valid @RequestBody request: ChatRequest,
        httpResponse: HttpServletResponse
    ): Mono<ChatResponse> {

        val turnId = checkNotNull(request.turnId) {
            "Turn ID cannot be empty"
        }

        MDC.put(
            ChatLogContext.TURN_ID,
            turnId
        )

        request.conversationId?.let { conversationId ->
            MDC.put(
                ChatLogContext.CONVERSATION_ID,
                conversationId
            )
        }

        log.info(
            "Received chat request conversationId={} turnId={} messageCount={}",
            request.conversationId ?: "new",
            turnId,
            request.messages.size
        )

        return chatService
            .chat(request)
            .doOnNext { response ->

                httpResponse.setHeader(
                    CONVERSATION_ID_HEADER,
                    response.conversationId
                )

                httpResponse.setHeader(
                    TURN_ID_HEADER,
                    response.turnId
                )
            }
    }

    companion object {

        const val CONVERSATION_ID_HEADER = "X-Conversation-ID"
        const val TURN_ID_HEADER = "X-Turn-ID"

        private val log =
            LoggerFactory.getLogger(ChatController::class.java)
    }
}