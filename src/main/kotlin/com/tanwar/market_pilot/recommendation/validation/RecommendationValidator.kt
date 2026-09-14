package com.tanwar.market_pilot.recommendation.validation

import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import org.springframework.stereotype.Component

@Component
class RecommendationValidator {

    fun validate(response: RecommendationResponse) {

        require(response.confidence in 0.0..1.0) {
            "Confidence must be between 0.0 and 1.0"
        }

        require(response.reasons.isNotEmpty()) {
            "At least one reason is required"
        }

        require(response.risks.isNotEmpty()) {
            "At least one risk is required"
        }
    }
}