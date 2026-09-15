package com.tanwar.market_pilot.portfolio.market.model

import java.math.BigDecimal

data class MarketData(
    val symbol: String,
    val currentPrice: BigDecimal
)