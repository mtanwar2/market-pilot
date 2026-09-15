package com.tanwar.market_pilot.portfolio.analysis.model

import java.math.BigDecimal
import java.util.UUID

data class PortfolioAnalysis(
    val portfolioId: UUID,
    val totalInvested: BigDecimal,
    val totalCurrentValue: BigDecimal,
    val totalProfit: BigDecimal,
    val totalProfitPercentage: BigDecimal,
    val holdings: List<HoldingAnalysis>
)