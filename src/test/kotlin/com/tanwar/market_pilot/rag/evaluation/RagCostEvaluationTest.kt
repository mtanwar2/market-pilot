package com.tanwar.market_pilot.rag.evaluation

import com.tanwar.market_pilot.llm.model.TokenUsage
import org.junit.jupiter.api.Test

class RagCostEvaluationTest {

    @Test
    fun `should calculate cost across RAG requests`() {

        val usages = listOf(
            TokenUsage(
                inputTokens = 132,
                outputTokens = 404,
                totalTokens = 536
            ),
            TokenUsage(
                inputTokens = 132,
                outputTokens = 465,
                totalTokens = 597
            ),
            TokenUsage(
                inputTokens = 132,
                outputTokens = 483,
                totalTokens = 615
            ),
            TokenUsage(
                inputTokens = 132,
                outputTokens = 389,
                totalTokens = 521
            ),
            TokenUsage(
                inputTokens = 132,
                outputTokens = 504,
                totalTokens = 636
            )
        )

        val results = usages.map { usage ->

            CostCalculator.calculate(
                usage = usage,
                inputPricePerMillionTokens = 1.0,
                outputPricePerMillionTokens = 5.0
            )
        }

        results.forEachIndexed { index, result ->

            println(
                "Request ${index + 1}: " +
                        "inputCost=${result.inputCost}, " +
                        "outputCost=${result.outputCost}, " +
                        "totalCost=${result.totalCost}"
            )
        }

        val averageCost =
            results.map { it.totalCost }.average()

        val minimumCost =
            results.minOf { it.totalCost }

        val maximumCost =
            results.maxOf { it.totalCost }

        println()
        println("Average cost: $averageCost")
        println("Minimum cost: $minimumCost")
        println("Maximum cost: $maximumCost")
    }
}