package com.tanwar.market_pilot.llm.model

data class LlmRequest(
    val model: String,
    val messages: List<LlmMessage>,
    val temperature: Double? = null,
    val maxTokens: Int? = null
)

data class LlmResponse(
    val content: String,
    val usage: TokenUsage,
    val finishReason: String?
)

data class LlmMessage(
    val role: LlmRole,
    val content: String
)

enum class LlmRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}

data class TokenUsage(
    val inputTokens: Long,
    val outputTokens: Long,
    val totalTokens: Long
)