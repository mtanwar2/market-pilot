package com.tanwar.market_pilot.portfolio.service

import com.tanwar.market_pilot.portfolio.market.MarketDataClient
import com.tanwar.market_pilot.portfolio.market.model.MarketData
import org.springframework.stereotype.Service

@Service
class MarketDataService(
    private val marketDataClient: MarketDataClient
) {

    fun getMarketData(symbol: String): MarketData {
        return marketDataClient.getMarketData(symbol)
    }
}