package com.tanwar.market_pilot.portfolio.persistence

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "portfolios")
class PortfolioEntity(

    @Id
    var id: UUID? = null,

    var name: String = "",

    var createdAt: Instant = Instant.EPOCH,

    @OneToMany(
        mappedBy = "portfolio",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var holdings: MutableList<HoldingEntity> = mutableListOf()
)