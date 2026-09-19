package com.tanwar.market_pilot.recommendation.model

data class StockRecommendation(
    val symbol: String,
    val recommendation: Recommendation,
    val confidence: Double,
    val reasons: List<String>,
    val risks: List<String>
)

data class RecommendationResponse(
    val recommendations: List<StockRecommendation>
)