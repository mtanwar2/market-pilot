package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component("fake")
class FakeFailingLlmClient : LlmClient {

    private val log =
        LoggerFactory.getLogger(FakeFailingLlmClient::class.java)

    private val callCount = AtomicInteger(0)

    override fun generate(request: LlmRequest): LlmResponse {

        val currentCall = callCount.incrementAndGet()

        log.info(
            "Fake LLM called: callNumber={}, messageCount={}",
            currentCall,
            request.messages.size
        )

        // First call fails
        if (currentCall < 4) {

            log.warn(
                "Fake LLM intentionally failing first call"
            )

            throw LlmException(
                500,
                message = "Simulated LLM failure on first call"
            )
        }

        // Second call succeeds
        log.info(
            "Fake LLM succeeding: callNumber={}",
            currentCall
        )

        return LlmResponse(
            content = "Fake LLM response after  ${currentCall } retry",

            usage = TokenUsage(
                inputTokens = 10,
                outputTokens = 20,
                totalTokens = 30
            ),

            finishReason = "stop"
        )
    }
}