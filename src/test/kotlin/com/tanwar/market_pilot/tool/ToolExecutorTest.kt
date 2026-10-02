package com.tanwar.market_pilot.tool

import com.tanwar.market_pilot.llm.audit.AuditContext
import com.tanwar.market_pilot.llm.audit.AuditLogger
import com.tanwar.market_pilot.llm.model.ToolCall
import com.tanwar.market_pilot.llm.tool.ToolAuthorizationException
import com.tanwar.market_pilot.llm.tool.ToolAuthorizationService
import com.tanwar.market_pilot.llm.tool.ToolExecutionResult
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
import java.util.UUID

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

        val toolExecutor = toolExecutor(registry)

        val result = toolExecutor.execute(
            ToolCall(
                name = "getStockPrice",
                arguments = mapOf(
                    "symbol" to "NVDA"
                )
            ),
            auditContext
        )

        assertEquals(
            ToolExecutionResult(
                success = true,
                content = MarketData(
                    symbol = "NVDA",
                    currentPrice = BigDecimal("170.00")
                )
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

        val toolExecutor = toolExecutor(registry)

        val error = org.junit.jupiter.api.assertThrows<ToolAuthorizationException> {
            toolExecutor.execute(
                ToolCall(
                    name = "unknownTool",
                    arguments = emptyMap()
                ),
                auditContext
            )
        }

        assertEquals(
            "Tool execution is not authorized: unknownTool",
            error.message
        )
    }

    private val auditContext = AuditContext(
        userId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
        conversationId = "conversation-1",
        turnId = "turn-1"
    )

    private fun toolExecutor(registry: ToolRegistry) = ToolExecutor(
        toolRegistry = registry,
        toolAuthorizationService = ToolAuthorizationService(),
        auditLogger = AuditLogger()
    )
}