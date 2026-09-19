package com.tanwar.market_pilot.recommendation.controller

import com.tanwar.market_pilot.recommendation.model.RecommendationRequest
import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import com.tanwar.market_pilot.recommendation.service.RecommendationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/portfolios")
class RecommendationController(
    private val recommendationService: RecommendationService
) {

    @PostMapping("/{portfolioId}/recommendation")
    fun recommend(
        @PathVariable portfolioId: UUID
    ): Mono<ResponseEntity<RecommendationResponse>> {

        return recommendationService
            .recommend(portfolioId)
            .map { recommendation ->
                ResponseEntity.ok(recommendation)
            }
    }
}