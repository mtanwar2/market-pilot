package com.tanwar.market_pilot.recommendation.parser

import tools.jackson.databind.ObjectMapper
import com.tanwar.market_pilot.recommendation.model.RecommendationResponse
import org.springframework.stereotype.Component

@Component
class RecommendationParser(
    private val objectMapper: ObjectMapper
) {

    fun parse(content: String): RecommendationResponse {
        return objectMapper.readValue(
            content,
            RecommendationResponse::class.java
        )
    }
}