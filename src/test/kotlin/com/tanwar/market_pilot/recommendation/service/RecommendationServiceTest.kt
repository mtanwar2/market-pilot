package com.tanwar.market_pilot.recommendation.service

import com.tanwar.market_pilot.llm.client.LlmClient
import com.tanwar.market_pilot.llm.client.LlmClientFactory
import com.tanwar.market_pilot.llm.model.LlmResponse
import com.tanwar.market_pilot.llm.model.TokenUsage
import com.tanwar.market_pilot.portfolio.analysis.model.HoldingAnalysis
import com.tanwar.market_pilot.portfolio.analysis.model.PortfolioAnalysis
import com.tanwar.market_pilot.portfolio.service.PortfolioAnalysisService
import com.tanwar.market_pilot.recommendation.model.Recommendation
import com.tanwar.market_pilot.recommendation.parser.RecommendationParser
import com.tanwar.market_pilot.recommendation.prompt.RecommendationPromptBuilder
import com.tanwar.market_pilot.recommendation.validation.RecommendationValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.util.UUID

class RecommendationServiceTest {

    private val llmClientFactory: LlmClientFactory = mock()
    private val llmClient: LlmClient = mock()
    private val portfolioAnalysisService: PortfolioAnalysisService = mock()

    private val promptBuilder = RecommendationPromptBuilder()

    private val parser = RecommendationParser(
        ObjectMapper()
    )

    private val validator = RecommendationValidator()

    private val service = RecommendationService(
        llmClientFactory = llmClientFactory,
        portfolioAnalysisService = portfolioAnalysisService,
        promptBuilder = promptBuilder,
        parser = parser,
        validator = validator
    )

    private val portfolioId = UUID.randomUUID()

    private val portfolioAnalysis = PortfolioAnalysis(
        portfolioId = portfolioId,
        totalInvested = BigDecimal("1755.00"),
        totalCurrentValue = BigDecimal("1700.00"),
        totalProfit = BigDecimal("-55.00"),
        totalProfitPercentage = BigDecimal("-3.13"),
        holdings = listOf(
            HoldingAnalysis(
                symbol = "NVDA",
                quantity = BigDecimal("10"),
                averagePrice = BigDecimal("175.50"),
                currentPrice = BigDecimal("170.00"),
                investedAmount = BigDecimal("1755.00"),
                currentValue = BigDecimal("1700.00"),
                profit = BigDecimal("-55.00"),
                profitPercentage = BigDecimal("-3.13"),
                allocationPercentage = BigDecimal("100.00")
            )
        )
    )

    @Test
    fun `should return recommendation when LLM returns valid JSON`() {

        whenever(
            portfolioAnalysisService.analyze(portfolioId)
        ).thenReturn(portfolioAnalysis)

        val llmResponse = LlmResponse(
            content = """
                {
                  "recommendations": [
                    {
                      "symbol": "NVDA",
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
            .thenReturn(Mono.just(llmResponse))

        StepVerifier.create(
            service.recommend(portfolioId)
        )
            .assertNext { result ->
                assertEquals(
                    1,
                    result.recommendations.size
                )

                val recommendation = result.recommendations.first()

                assertEquals(
                    "NVDA",
                    recommendation.symbol
                )

                assertEquals(
                    Recommendation.BUY,
                    recommendation.recommendation
                )

                assertEquals(
                    0.82,
                    recommendation.confidence
                )

                assertEquals(
                    2,
                    recommendation.reasons.size
                )

                assertEquals(
                    1,
                    recommendation.risks.size
                )
            }
            .verifyComplete()

        verify(portfolioAnalysisService)
            .analyze(portfolioId)
    }

    @Test
    fun `should fail when LLM returns invalid JSON`() {

        whenever(
            portfolioAnalysisService.analyze(portfolioId)
        ).thenReturn(portfolioAnalysis)

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
            .thenReturn(Mono.just(llmResponse))

        StepVerifier.create(
            service.recommend(portfolioId)
        )
            .expectError()
            .verify()
    }

    @Test
    fun `should fail when recommendation violates business validation`() {

        whenever(
            portfolioAnalysisService.analyze(portfolioId)
        ).thenReturn(portfolioAnalysis)

        val llmResponse = LlmResponse(
            content = """
                {
                  "recommendations": [
                    {
                      "symbol": "NVDA",
                      "recommendation": "BUY",
                      "confidence": 1.5,
                      "reasons": [
                        "Strong growth"
                      ],
                      "risks": [
                        "High valuation"
                      ]
                    }
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
            .thenReturn(Mono.just(llmResponse))

        StepVerifier.create(
            service.recommend(portfolioId)
        )
            .expectErrorMatches { error ->
                error is IllegalArgumentException &&
                        error.message ==
                        "Confidence must be between 0.0 and 1.0 for NVDA"
            }
            .verify()
    }

    @Test
    fun `should propagate LLM error`() {

        whenever(
            portfolioAnalysisService.analyze(portfolioId)
        ).thenReturn(portfolioAnalysis)

        val llmException =
            RuntimeException("LLM service unavailable")

        whenever(llmClientFactory.getClient())
            .thenReturn(llmClient)

        whenever(llmClient.generate(any()))
            .thenReturn(Mono.error(llmException))

        StepVerifier.create(
            service.recommend(portfolioId)
        )
            .expectErrorMatches { error ->
                error === llmException
            }
            .verify()
    }

    @Test
    fun `should build prompt using portfolio analysis`() {

        whenever(
            portfolioAnalysisService.analyze(portfolioId)
        ).thenReturn(portfolioAnalysis)

        val llmResponse = LlmResponse(
            content = """
                {
                  "recommendations": [
                    {
                      "symbol": "NVDA",
                      "recommendation": "HOLD",
                      "confidence": 0.65,
                      "reasons": [
                        "Mixed signals"
                      ],
                      "risks": [
                        "Market uncertainty"
                      ]
                    }
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
            .thenReturn(Mono.just(llmResponse))

        StepVerifier.create(
            service.recommend(portfolioId)
        )
            .assertNext { result ->
                assertEquals(
                    Recommendation.HOLD,
                    result.recommendations.first().recommendation
                )
            }
            .verifyComplete()

        verify(llmClient).generate(
            argThat { request ->
                request.messages.any { message ->
                    message.content.contains("NVDA") &&
                            message.content.contains("1755.00") &&
                            message.content.contains("1700.00") &&
                            message.content.contains("-55.00") &&
                            message.content.contains("-3.13")
                }
            }
        )
    }
}