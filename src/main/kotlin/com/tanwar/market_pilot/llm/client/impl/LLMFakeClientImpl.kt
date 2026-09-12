package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("fake")
class FakeLlmClient : LlmClient {

    private val log =
        LoggerFactory.getLogger(FakeLlmClient::class.java)

    override fun generate(request: LlmRequest): LlmResponse {
        log.info(
            "Fake LLM generating deterministic response messageCount={}",
            request.messages.size
        )

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