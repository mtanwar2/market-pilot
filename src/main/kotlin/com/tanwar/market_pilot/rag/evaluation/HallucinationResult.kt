package com.tanwar.market_pilot.rag.evaluation

data class HallucinationResult(
    val score: Double,
    val reason: String
)