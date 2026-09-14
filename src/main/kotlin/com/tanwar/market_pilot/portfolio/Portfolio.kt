package com.tanwar.market_pilot.portfolio

import com.tanwar.market_pilot.portfolio.model.Holding
import java.time.Instant
import java.util.UUID

data class Portfolio(
    val id: UUID? = null,
    val name: String,
    val createdAt: Instant,
    val holdings: List<Holding> = emptyList()
)