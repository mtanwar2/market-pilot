package com.tanwar.market_pilot.llm.model

data class LlmRequest(
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
    val inputTokens: Long?,
    val outputTokens: Long?,
    val totalTokens: Long?
)

data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean
)

data class OllamaMessage(
    val role: String,
    val content: String
)

data class OllamaChatResponse(
    val model: String,
    val message: OllamaMessage,
    val done: Boolean,
    val done_reason: String? = null,
    val prompt_eval_count: Long? = null,
    val eval_count: Long? = null
)

data class GeminiRequest(
    val systemInstruction: GeminiContent?,
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig?
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double?,
    val maxOutputTokens: Int?
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val usageMetadata: GeminiUsageMetadata?
)

data class GeminiCandidate(
    val content: GeminiResponseContent?,
    val finishReason: String?
)

data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>?
)

data class GeminiResponsePart(
    val text: String?
)

data class GeminiUsageMetadata(
    val promptTokenCount: Long?,
    val candidatesTokenCount: Long?,
    val totalTokenCount: Long?
)