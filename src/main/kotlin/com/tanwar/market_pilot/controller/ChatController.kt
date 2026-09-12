package com.tanwar.market_pilot.controller

import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatResponse
import com.tanwar.market_pilot.service.ChatService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun chat(
        @Valid @RequestBody request: ChatRequest,
        httpResponse: HttpServletResponse
    ): ChatResponse {
        log.info(
            "Received chat request conversationId={} messageLength={}",
            request.conversationId ?: "new",
            request.message.length
        )
        val response = chatService.chat(request)
        httpResponse.setHeader(CONVERSATION_ID_HEADER, response.conversationId)
        httpResponse.setHeader(TURN_ID_HEADER, response.turnId)
        return response
    }

    companion object {
        const val CONVERSATION_ID_HEADER = "X-Conversation-ID"
        const val TURN_ID_HEADER = "X-Turn-ID"
        private val log = LoggerFactory.getLogger(ChatController::class.java)
    }
}