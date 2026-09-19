package com.tanwar.market_pilot.recommendation.validation

import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import org.springframework.stereotype.Component

@Component
class RecommendationValidator {

    fun validate(response: RecommendationResponse) {

        require(response.recommendations.isNotEmpty()) {
            "At least one stock recommendation is required"
        }

        response.recommendations.forEach { recommendation ->

            require(recommendation.symbol.isNotBlank()) {
                "Stock symbol must not be blank"
            }

            require(recommendation.confidence in 0.0..1.0) {
                "Confidence must be between 0.0 and 1.0 for ${recommendation.symbol}"
            }

            require(recommendation.reasons.isNotEmpty()) {
                "At least one reason is required for ${recommendation.symbol}"
            }

            require(recommendation.risks.isNotEmpty()) {
                "At least one risk is required for ${recommendation.symbol}"
            }
        }
    }
}
