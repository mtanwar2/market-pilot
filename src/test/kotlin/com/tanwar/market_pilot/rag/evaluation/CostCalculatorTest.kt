package com.tanwar.market_pilot.rag.evaluation

import com.tanwar.market_pilot.llm.model.TokenUsage
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CostCalculatorTest {

    @Test
    fun `should calculate input output and total cost`() {

        val usage = TokenUsage(
            inputTokens = 132,
            outputTokens = 504,
            totalTokens = 636
        )

        val result = CostCalculator.calculate(
            usage = usage,
            inputPricePerMillionTokens = 1.0,
            outputPricePerMillionTokens = 5.0
        )

        println("Input cost: ${result.inputCost}")
        println("Output cost: ${result.outputCost}")
        println("Total cost: ${result.totalCost}")

        assertEquals(
            0.000132,
            result.inputCost,
            0.000000001
        )

        assertEquals(
            0.00252,
            result.outputCost,
            0.000000001
        )

        assertEquals(
            0.002652,
            result.totalCost,
            0.000000001
        )
    }
}