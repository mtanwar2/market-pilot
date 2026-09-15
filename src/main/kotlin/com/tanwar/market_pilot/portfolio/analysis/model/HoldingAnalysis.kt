package com.tanwar.market_pilot.portfolio.analysis.model

import java.math.BigDecimal

data class HoldingAnalysis(
    val symbol: String,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val currentPrice: BigDecimal,
    val investedAmount: BigDecimal,
    val currentValue: BigDecimal,
    val profit: BigDecimal,
    val profitPercentage: BigDecimal,
    val allocationPercentage: BigDecimal
)