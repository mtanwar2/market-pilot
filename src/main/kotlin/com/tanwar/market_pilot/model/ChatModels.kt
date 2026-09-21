package com.tanwar.market_pilot.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ChatRequest(
    @field:NotEmpty(message = "Messages cannot be empty")
    @field:Size(
        max = 50,
        message = "Transcript must not exceed 50 messages"
    )
    @field:Valid
    val messages: List<ChatMessage> = emptyList(),

    @field:NotBlank(message = "Turn ID cannot be empty")
    @field:Size(
        max = 100,
        message = "Turn ID must not exceed 100 characters"
    )
    @field:Pattern(
        regexp = "[A-Za-z0-9._-]{1,100}",
        message = "Turn ID must contain only letters, digits, '.', '_' or '-'"
    )
    val turnId: String? = null,

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

data class ChatMessage(
    val role: ChatRole,

    @field:NotBlank(message = "Message content cannot be empty")
    @field:Size(
        max = 10_000,
        message = "Message must not exceed 10000 characters"
    )
    val content: String
)

enum class ChatRole {
    USER,
    ASSISTANT;

    @JsonValue
    fun toJson(): String = name.lowercase()

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromJson(value: String): ChatRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unsupported chat role: $value")
    }
}

data class ChatResponse(
    val message: String? = null,
    val conversationId: String,
    val turnId: String
)
