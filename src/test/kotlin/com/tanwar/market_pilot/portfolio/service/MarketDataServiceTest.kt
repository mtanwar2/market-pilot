package com.tanwar.market_pilot.portfolio.service

import com.tanwar.market_pilot.portfolio.market.MarketDataClient
import com.tanwar.market_pilot.portfolio.market.model.MarketData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class MarketDataServiceTest {

    private val marketDataClient = mock<MarketDataClient>()

    private val service = MarketDataService(
        marketDataClient
    )

    @Test
    fun `should return market data from client`() {

        whenever(
            marketDataClient.getMarketData("NVDA")
        ).thenReturn(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("170.00")
            )
        )

        val result = service.getMarketData("NVDA")

        assertEquals("NVDA", result.symbol)
        assertEquals(
            BigDecimal("170.00"),
            result.currentPrice
        )

        verify(
            marketDataClient
        ).getMarketData("NVDA")
    }
}