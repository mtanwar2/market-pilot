package com.tanwar.market_pilot.rag.evaluation

import com.tanwar.market_pilot.llm.model.TokenUsage

data class CostResult(
    val inputCost: Double,
    val outputCost: Double,
    val totalCost: Double
)

object CostCalculator {

    fun calculate(
        usage: TokenUsage,
        inputPricePerMillionTokens: Double,
        outputPricePerMillionTokens: Double
    ): CostResult {

        val inputTokens = usage.inputTokens ?: 0L
        val outputTokens = usage.outputTokens ?: 0L

        val inputCost =
            (inputTokens / 1_000_000.0) *
                    inputPricePerMillionTokens

        val outputCost =
            (outputTokens / 1_000_000.0) *
                    outputPricePerMillionTokens

        val totalCost =
            inputCost + outputCost

        return CostResult(
            inputCost = inputCost,
            outputCost = outputCost,
            totalCost = totalCost
        )
    }
}