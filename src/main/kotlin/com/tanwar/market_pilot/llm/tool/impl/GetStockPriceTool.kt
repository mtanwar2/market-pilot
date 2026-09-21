package com.tanwar.market_pilot.llm.tool.impl

import com.tanwar.market_pilot.llm.tool.Tool
import com.tanwar.market_pilot.portfolio.service.MarketDataService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GetStockPriceTool(
    private val marketDataService: MarketDataService
) : Tool {

    override val name: String = "getStockPrice"

    override fun execute(arguments: Map<String, Any?>): Any {

        val symbol = arguments["symbol"]
            ?: throw IllegalArgumentException(
                "Missing required argument: symbol"
            )

        log.info("Executing getStockPrice for symbol={}", symbol)

        // Temporary hardcoded value.
        // Later this will call a real market-data provider.
        return marketDataService.getMarketData(symbol.toString())
    }

    companion object {
        private val log = LoggerFactory.getLogger(GetStockPriceTool::class.java)
    }
}