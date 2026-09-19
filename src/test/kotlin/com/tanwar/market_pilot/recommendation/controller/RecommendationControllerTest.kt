package com.tanwar.market_pilot.recommendation.controller

import com.tanwar.market_pilot.recommendation.model.Recommendation
import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import com.tanwar.market_pilot.recommendation.model.StockRecommendation
import com.tanwar.market_pilot.recommendation.service.RecommendationService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import reactor.core.publisher.Mono
import java.util.UUID

@WebMvcTest(RecommendationController::class)
class RecommendationControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var recommendationService: RecommendationService

    @Test
    fun `debug recommendation endpoint`() {

        val portfolioId = UUID.randomUUID()

        val recommendation = RecommendationResponse(
            recommendations = listOf(
                StockRecommendation(
                    symbol = "NVDA",
                    recommendation = Recommendation.BUY,
                    confidence = 0.82,
                    reasons = listOf(
                        "Strong revenue growth",
                        "Positive market momentum"
                    ),
                    risks = listOf(
                        "High valuation"
                    )
                )
            )
        )

        whenever(
            recommendationService.recommend(portfolioId)
        ).thenReturn(
            Mono.just(recommendation)
        )

        val result = mockMvc.perform(
            post("/portfolios/$portfolioId/recommendation")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andReturn()

        println("========== RECOMMENDATION TEST ==========")
        println("STATUS      = ${result.response.status}")
        println("CONTENT TYPE= ${result.response.contentType}")
        println("BODY        = ${result.response.contentAsString}")
        println("ERROR       = ${result.response.errorMessage}")
        println("==========================================")
    }

    @Test
    fun `debug invalid portfolio id`() {

        val result = mockMvc.perform(
            post("/portfolios/not-a-uuid/recommendation")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andReturn()

        println("========== INVALID UUID TEST ==========")
        println("STATUS      = ${result.response.status}")
        println("CONTENT TYPE= ${result.response.contentType}")
        println("BODY        = ${result.response.contentAsString}")
        println("ERROR       = ${result.response.errorMessage}")
        println("=======================================")
    }
}