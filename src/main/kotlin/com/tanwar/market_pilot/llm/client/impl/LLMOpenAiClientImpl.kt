package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.properties.LlmProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("openai")
class OpenAiLlmClient(
    private val llmProperties: LlmProperties
) : LlmClient {

    override fun generate(request: LlmRequest): LlmResponse {

        val model = llmProperties.providers["openai"]?.model

        log.warn(
            "Fake OpenAI client invoked; no external API call will be made model={} messageCount={}",
            model,
            request.messages.size
        )

        return LlmResponse(
            content = "This is a response from the fake OpenAI client.",
            usage = TokenUsage(
                inputTokens = 0,
                outputTokens = 0,
                totalTokens = 0
            ),
            finishReason = "stop"
        )
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(OpenAiLlmClient::class.java)
    }
}