package com.tanwar.market_pilot.llm.model

import com.fasterxml.jackson.annotation.JsonProperty

data class LlmRequest(
    val messages: List<LlmMessage>,
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val tools: List<ToolDefinition> = emptyList()
)

data class LlmResponse(
    val content: String?=null,
    val usage: TokenUsage,
    val finishReason: String?,
    val toolCalls: List<ToolCall> = emptyList()
)

data class LlmMessage(
    val role: LlmRole,
    val content: String? = null,
    val toolCall: ToolCall? = null,
    val toolResult: ToolResult? = null
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
    val content: String? = null,
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
    val tools: List<GeminiTool>? = null,
    val generationConfig: GeminiGenerationConfig?
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null,
    val functionCall: GeminiFunctionCall? = null,
    val functionResponse: GeminiFunctionResponse? = null,
    val thoughtSignature: String? = null
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
    val text: String?,
    val functionCall: GeminiFunctionCall? = null,
    val thoughtSignature: String? = null
)

data class GeminiUsageMetadata(
    val promptTokenCount: Long?,
    val candidatesTokenCount: Long?,
    val totalTokenCount: Long?
)

data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Map<String, Any?>
)

data class GeminiTool(
    val functionDeclarations: List<GeminiFunctionDeclaration>
)

data class GeminiFunctionCall(
    val id: String? = null,
    val name: String,
    val args: Map<String, Any?>? = null
)

data class GeminiFunctionResponse(
    val id: String? = null,
    val name: String,
    val response: Map<String, Any?>
)