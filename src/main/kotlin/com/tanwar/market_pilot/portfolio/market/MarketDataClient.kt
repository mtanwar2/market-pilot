package com.tanwar.market_pilot.portfolio.market

import com.tanwar.market_pilot.portfolio.market.model.MarketData

interface MarketDataClient {

    fun getMarketData(symbol: String): MarketData
}