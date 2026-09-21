package com.tanwar.market_pilot.tool

import com.tanwar.market_pilot.llm.model.ToolCall
import com.tanwar.market_pilot.llm.tool.ToolExecutor
import com.tanwar.market_pilot.llm.tool.ToolRegistry
import com.tanwar.market_pilot.llm.tool.impl.GetStockPriceTool
import com.tanwar.market_pilot.portfolio.market.model.MarketData
import com.tanwar.market_pilot.portfolio.service.MarketDataService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class ToolExecutorTest {

    private val marketDataService = mock<MarketDataService>()

    @Test
    fun `should execute stock price tool`() {

        whenever(
            marketDataService.getMarketData("NVDA")
        ).thenReturn(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("170.00")
            )
        )

        val tool = GetStockPriceTool(marketDataService)

        val result = tool.execute(
            mapOf(
                "symbol" to "NVDA"
            )
        )

        assertEquals(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("170.00")
            ),
            result
        )
    }

    @Test
    fun `should execute tool through executor`() {

        whenever(
            marketDataService.getMarketData("NVDA")
        ).thenReturn(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("170.00")
            )
        )

        val tool = GetStockPriceTool(marketDataService)

        val registry = ToolRegistry(
            listOf(tool)
        )

        val toolExecutor = ToolExecutor(registry)

        val result = toolExecutor.execute(
            ToolCall(
                name = "getStockPrice",
                arguments = mapOf(
                    "symbol" to "NVDA"
                )
            )
        )

        assertEquals(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("170.00")
            ),
            result
        )
    }

    @Test
    fun `should reject unknown tool names`() {

        val tool = GetStockPriceTool(marketDataService)

        val registry = ToolRegistry(
            listOf(tool)
        )

        val toolExecutor = ToolExecutor(registry)

        val error = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            toolExecutor.execute(
                ToolCall(
                    name = "unknownTool",
                    arguments = emptyMap()
                )
            )
        }

        assertEquals(
            "Unknown tool: unknownTool",
            error.message
        )
    }
}