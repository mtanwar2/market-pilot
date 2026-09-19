package com.tanwar.market_pilot.portfolio.market.tool

import com.tanwar.market_pilot.portfolio.market.model.MarketData
import com.tanwar.market_pilot.portfolio.market.tool.impl.DefaultMarketDataTool
import com.tanwar.market_pilot.portfolio.service.MarketDataService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class DefaultMarketDataToolTest {

    private val marketDataService = mock<MarketDataService>()

    private val tool = DefaultMarketDataTool(
        marketDataService = marketDataService
    )

    @Test
    fun `should return market data from market data service`() {

        whenever(
            marketDataService.getMarketData("NVDA")
        ).thenReturn(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("170.00")
            )
        )

        val result = tool.getMarketData("NVDA")

        assertEquals("NVDA", result.symbol)
        assertEquals(
            BigDecimal("170.00"),
            result.currentPrice
        )
    }
}