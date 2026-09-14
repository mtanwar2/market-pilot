package com.tanwar.market_pilot.llm.client.impl

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.model.LlmRequest
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.llm.properties.LlmProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component("openai")
class OpenAiLlmClient(
    llmProperties: LlmProperties
) : LlmClient {

    private val config =
        llmProperties.providers["openai"]
            ?: throw IllegalStateException(
                "OpenAI configuration is missing"
            )

    override fun generate(
        request: LlmRequest
    ): Mono<LlmResponse> {

        log.info(
            "Fake OpenAI client invoked model={} messageCount={}",
            config.model,
            request.messages.size
        )

        return Mono.just(
            LlmResponse(
                content = "This is a response from the fake OpenAI client.",
                usage = TokenUsage(
                    inputTokens = 0,
                    outputTokens = 0,
                    totalTokens = 0
                ),
                finishReason = "stop"
            )
        )
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(OpenAiLlmClient::class.java)
    }
}