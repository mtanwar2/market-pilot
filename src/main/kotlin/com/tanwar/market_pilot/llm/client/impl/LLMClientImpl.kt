package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.TokenUsage
import org.springframework.stereotype.Component

@Component("openai")
class OpenAiLlmClient : LlmClient {
    override fun generate(request: LlmRequest): LlmResponse {
        // OpenAI API call
    }
}

@Component("gemini")
class GeminiLlmClient : LlmClient {
    override fun generate(request: LlmRequest): LlmResponse {
        // Gemini API call
    }
}

@Component("anthropic")
class AnthropicLlmClient : LlmClient {
    override fun generate(request: LlmRequest): LlmResponse {
        // Gemini API call
    }
}


@Component("fake")
class FakeLlmClient : LlmClient {

    override fun generate(request: LlmRequest): LlmResponse {
        return LlmResponse(
            content = "This is a response from the fake LLM.",
            usage = TokenUsage(
                inputTokens = 0,
                outputTokens = 0,
                totalTokens = 0
            ),
            finishReason = "stop"
        )
    }
}

