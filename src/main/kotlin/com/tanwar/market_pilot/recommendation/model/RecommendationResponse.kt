package com.tanwar.market_pilot.recommendation.model

data class RecommendationResponse(
    val recommendation: Recommendation,
    val confidence: Double,
    val reasons: List<String>,
    val risks: List<String>
)