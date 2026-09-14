package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.exception.LlmException
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component("fake")
class FakeLlmClient : LlmClient {

    private var attempts = 0

    override fun generate(
        request: LlmRequest
    ): Mono<LlmResponse> {

        attempts++

        log.info(
            "Fake LLM generating response attempt={} messageCount={}",
            attempts,
            request.messages.size
        )

        if (attempts <= 2) {
            log.warn(
                "Fake LLM simulating rate limit attempt={} statusCode=429",
                attempts
            )

            return Mono.error(
                LlmException(
                    statusCode = 429,
                    message = "Simulated Gemini rate limit"
                )
            )
        }

        return Mono.just(
            LlmResponse(
                content = "This is a response from the fake LLM.",
                usage = TokenUsage(
                    inputTokens = 10,
                    outputTokens = 5,
                    totalTokens = 15
                ),
                finishReason = "stop"
            )
        )
    }

    companion object {

        private val log =
            LoggerFactory.getLogger(FakeLlmClient::class.java)
    }
}