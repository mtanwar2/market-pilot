package com.tanwar.market_pilot.llm.client.resilience

import com.tanwar.market_pilot.llm.properties.RetryProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RetryBackoffCalculatorTest {

    @Test
    fun `uses initial delay on first attempt`() {
        val calculator = RetryBackoffCalculator(
            RetryProperties(
                initialBackoffMs = 100,
                maxBackoffMs = 1000,
                jitterFactor = 0.0
            )
        )

        assertEquals(100, calculator.calculateDelay(1))
    }

    @Test
    fun `doubles delay until the configured cap`() {
        val calculator = RetryBackoffCalculator(
            RetryProperties(
                initialBackoffMs = 100,
                maxBackoffMs = 250,
                jitterFactor = 0.0
            )
        )

        assertEquals(100, calculator.calculateDelay(1))
        assertEquals(200, calculator.calculateDelay(2))
        assertEquals(250, calculator.calculateDelay(3))
        assertEquals(250, calculator.calculateDelay(4))
    }

    @Test
    fun `adds jitter within the remaining cap`() {
        val calculator = RetryBackoffCalculator(
            RetryProperties(
                initialBackoffMs = 100,
                maxBackoffMs = 1000,
                jitterFactor = 0.5
            )
        )

        val delay = calculator.calculateDelay(1)
        assertTrue(delay in 100..150)
    }

    @Test
    fun `rejects invalid attempt numbers`() {
        val calculator = RetryBackoffCalculator(RetryProperties())

        assertThrows(IllegalArgumentException::class.java) {
            calculator.calculateDelay(0)
        }
    }
}
