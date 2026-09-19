package com.tanwar.market_pilot.portfolio.market.tool

import com.tanwar.market_pilot.portfolio.market.model.MarketData

interface MarketDataTool {

    fun getMarketData(symbol: String): MarketData
}