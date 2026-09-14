package com.tanwar.market_pilot.portfolio.model

import java.math.BigDecimal
import java.util.UUID

data class Holding(
    val id: UUID,
    val symbol: String,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val portfolioId: UUID
)