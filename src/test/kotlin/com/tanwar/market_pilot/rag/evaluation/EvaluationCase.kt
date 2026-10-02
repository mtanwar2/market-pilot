package com.tanwar.market_pilot.rag.evaluation

data class EvaluationCase(
    val question: String,
    val relevantTexts: List<String>,
    val expectedAnswer: String? = null
)