package com.tanwar.market_pilot.model

data class ChatRequest(
    val message: String,
    val conversationId: String?
)

data class ChatResponse(
    val message: String,
    val conversationId: String
)