package com.tanwar.market_pilot.portfolio.controller

import com.tanwar.market_pilot.portfolio.market.model.MarketData
import com.tanwar.market_pilot.portfolio.service.MarketDataService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stocks")
class MarketDataController(
    private val marketDataService: MarketDataService
) {

    @GetMapping("/{symbol}")
    fun getMarketData(
        @PathVariable symbol: String
    ): MarketData {
        return marketDataService.getMarketData(symbol)
    }
}