package com.tanwar.market_pilot.recommendation.controller

import com.tanwar.market_pilot.recommendation.model.RecommendationRequest
import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import com.tanwar.market_pilot.recommendation.service.RecommendationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/recommendations")
class RecommendationController(
    private val recommendationService: RecommendationService
) {

    @PostMapping
    fun recommend(
        @Valid @RequestBody request: RecommendationRequest
    ): Mono<ResponseEntity<RecommendationResponse>> {

        return recommendationService
            .recommend(request.stock)
            .map { recommendation ->
                ResponseEntity.ok(recommendation)
            }
    }
}