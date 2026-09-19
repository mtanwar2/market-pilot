package com.tanwar.market_pilot.portfolio.market.tool.impl

import com.tanwar.market_pilot.portfolio.market.model.MarketData
import com.tanwar.market_pilot.portfolio.market.tool.MarketDataTool
import com.tanwar.market_pilot.portfolio.service.MarketDataService
import org.springframework.stereotype.Component

@Component
class DefaultMarketDataTool(
    private val marketDataService: MarketDataService
) : MarketDataTool {

    override fun getMarketData(symbol: String): MarketData {
        return marketDataService.getMarketData(symbol)
    }
}