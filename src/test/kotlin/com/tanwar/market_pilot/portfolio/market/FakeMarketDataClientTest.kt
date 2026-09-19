package com.tanwar.market_pilot.portfolio.market

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class FakeMarketDataClientTest {

    private val client = FakeMarketDataClient()

    @Test
    fun `should return market data for supported symbol`() {

        val result = client.getMarketData("NVDA")

        assertEquals("NVDA", result.symbol)
        assertEquals(
            BigDecimal("170.00"),
            result.currentPrice
        )
    }

    @Test
    fun `should normalize symbol to uppercase`() {

        val result = client.getMarketData("nvda")

        assertEquals("NVDA", result.symbol)
        assertEquals(
            BigDecimal("170.00"),
            result.currentPrice
        )
    }

    @Test
    fun `should throw exception for unsupported symbol`() {

        val exception = assertThrows<IllegalArgumentException> {
            client.getMarketData("UNKNOWN")
        }

        assertEquals(
            "Market data not available for symbol: UNKNOWN",
            exception.message
        )
    }
}