package com.tanwar.market_pilot.controller

import com.tanwar.market_pilot.model.ChatRequest
import com.tanwar.market_pilot.model.ChatResponse
import com.tanwar.market_pilot.service.ChatService
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
    fun chat(@RequestBody request: ChatRequest): ChatResponse {
        return chatService.chat(request)
    }
}