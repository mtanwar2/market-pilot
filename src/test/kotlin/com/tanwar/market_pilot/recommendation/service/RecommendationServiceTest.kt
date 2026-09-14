package com.tanwar.market_pilot.recommendation.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.recommendation.model.Recommendation
import com.tanwar.market_pilot.recommendation.parser.RecommendationParser
import com.tanwar.market_pilot.recommendation.prompt.RecommendationPromptBuilder
import com.tanwar.market_pilot.recommendation.validation.RecommendationValidator
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper

import kotlin.test.assertEquals

class RecommendationServiceTest {

    private val llmClientFactory: LlmClientFactory = mock()
    private val llmClient: LlmClient = mock()
    private val promptBuilder = RecommendationPromptBuilder()
    private val parser = RecommendationParser(
        ObjectMapper()
    )
    private val validator = RecommendationValidator()

    private val service = RecommendationService(
        llmClientFactory = llmClientFactory,
        promptBuilder = promptBuilder,
        parser = parser,
        validator = validator
    )

    @Test
    fun `should return recommendation when LLM returns valid JSON`() {

        val llmResponse = LlmResponse(
            content = """
                {
                  "recommendation": "BUY",
                  "confidence": 0.82,
                  "reasons": [
                    "Strong revenue growth",
                    "Positive market momentum"
                  ],
                  "risks": [
                    "High valuation"
                  ]
                }
            """.trimIndent(),
            usage = TokenUsage(
                inputTokens = 100,
                outputTokens = 80,
                totalTokens = 180
            ),
            finishReason = "STOP"
        )

        whenever(llmClientFactory.getClient())
            .thenReturn(llmClient)

        whenever(llmClient.generate(any()))
            .thenReturn(reactor.core.publisher.Mono.just(llmResponse))

        StepVerifier.create(
            service.recommend("NVIDIA")
        )
            .assertNext { result ->
                assertEquals(Recommendation.BUY, result.recommendation)
                assertEquals(0.82, result.confidence)
                assertEquals(2, result.reasons.size)
                assertEquals(1, result.risks.size)
            }
            .verifyComplete()
    }

    @Test
    fun `should fail when LLM returns invalid JSON`() {

        val llmResponse = LlmResponse(
            content = "This is not valid JSON",
            usage = TokenUsage(
                inputTokens = 100,
                outputTokens = 20,
                totalTokens = 120
            ),
            finishReason = "STOP"
        )

        whenever(llmClientFactory.getClient())
            .thenReturn(llmClient)

        whenever(llmClient.generate(any()))
            .thenReturn(reactor.core.publisher.Mono.just(llmResponse))

        StepVerifier.create(
            service.recommend("NVIDIA")
        )
            .expectError()
            .verify()
    }

    @Test
    fun `should fail when recommendation violates business validation`() {

        val llmResponse = LlmResponse(
            content = """
                {
                  "recommendation": "BUY",
                  "confidence": 1.5,
                  "reasons": [
                    "Strong growth"
                  ],
                  "risks": [
                    "High valuation"
                  ]
                }
            """.trimIndent(),
            usage = TokenUsage(
                inputTokens = 100,
                outputTokens = 50,
                totalTokens = 150
            ),
            finishReason = "STOP"
        )

        whenever(llmClientFactory.getClient())
            .thenReturn(llmClient)

        whenever(llmClient.generate(any()))
            .thenReturn(reactor.core.publisher.Mono.just(llmResponse))

        StepVerifier.create(
            service.recommend("NVIDIA")
        )
            .expectErrorMatches { error ->
                error is IllegalArgumentException &&
                        error.message == "Confidence must be between 0.0 and 1.0"
            }
            .verify()
    }

    @Test
    fun `should propagate LLM error`() {

        val llmException = RuntimeException("LLM service unavailable")

        whenever(llmClientFactory.getClient())
            .thenReturn(llmClient)

        whenever(llmClient.generate(any()))
            .thenReturn(reactor.core.publisher.Mono.error(llmException))

        StepVerifier.create(
            service.recommend("NVIDIA")
        )
            .expectErrorMatches { error ->
                error === llmException
            }
            .verify()
    }

    @Test
    fun `should build request using requested stock`() {

        val llmResponse = LlmResponse(
            content = """
                {
                  "recommendation": "HOLD",
                  "confidence": 0.65,
                  "reasons": [
                    "Mixed signals"
                  ],
                  "risks": [
                    "Market uncertainty"
                  ]
                }
            """.trimIndent(),
            usage = TokenUsage(
                inputTokens = 100,
                outputTokens = 50,
                totalTokens = 150
            ),
            finishReason = "STOP"
        )

        whenever(llmClientFactory.getClient())
            .thenReturn(llmClient)

        whenever(llmClient.generate(any()))
            .thenReturn(reactor.core.publisher.Mono.just(llmResponse))

        StepVerifier.create(
            service.recommend("Tesla")
        )
            .assertNext { result ->
                assertEquals(Recommendation.HOLD, result.recommendation)
            }
            .verifyComplete()

        org.mockito.kotlin.verify(llmClient).generate(
            org.mockito.kotlin.argThat { request ->
                request.messages.any { message ->
                    message.content.contains("Tesla")
                }
            }
        )
    }
}
