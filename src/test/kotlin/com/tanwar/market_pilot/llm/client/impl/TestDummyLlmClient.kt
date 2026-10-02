package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Used only by the test classpath. The Gradle test task points
 * `LLM_PROVIDER` at this bean so Spring tests never call Gemini.
 */
@Component("test-dummy")
class TestDummyLlmClient : LlmClient {

    override fun generate(request: LlmRequest): Mono<LlmResponse> {
        val prompt = request.messages.joinToString("\n") { it.content.orEmpty() }

        return Mono.just(
            LlmResponse(
                content = dummyContent(prompt),
                usage = TokenUsage(
                    inputTokens = 10,
                    outputTokens = 5,
                    totalTokens = 15
                ),
                finishReason = "STOP"
            )
        )
    }

    private fun dummyContent(prompt: String): String {
        val unsupported = prompt.contains("OpenAI") || prompt.contains("Edge Computing")
        val score = if (unsupported) "0.0" else "1.0"

        return when {
            prompt.contains("evaluating the relevance") ||
                prompt.contains("evaluating the faithfulness") ||
                prompt.contains("hallucinated") ->
                """{"score":$score,"reason":"dummy test response"}"""

            else ->
                "NVIDIA Data Center revenue increased 117% due to the ramp of Blackwell Ultra infrastructure."
        }
    }
}
