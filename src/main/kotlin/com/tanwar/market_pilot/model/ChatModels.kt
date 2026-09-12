package com.tanwar.market_pilot.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ChatRequest(
    @field:NotBlank(message = "Message cannot be empty")
    @field:Size(
        max = 10_000,
        message = "Message must not exceed 10000 characters"
    )
    val message: String,

    @field:Size(
        max = 100,
        message = "Conversation ID must not exceed 100 characters"
    )
    @field:Pattern(
        regexp = ".*\\S.*",
        message = "Conversation ID cannot be blank"
    )
    val conversationId: String? = null
)

data class ChatResponse(
    val message: String,
    val conversationId: String,
    val turnId: String
)