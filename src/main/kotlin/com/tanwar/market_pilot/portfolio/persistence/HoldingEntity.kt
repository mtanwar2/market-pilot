package com.tanwar.market_pilot.portfolio.persistence

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "holdings")
class HoldingEntity(

    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(nullable = false)
    var symbol: String = "",

    @Column(nullable = false, precision = 19, scale = 4)
    var quantity: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 19, scale = 4)
    var averagePrice: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    var portfolio: PortfolioEntity? = null
)