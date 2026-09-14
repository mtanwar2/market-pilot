package com.tanwar.market_pilot.portfolio.persistence.mapper

import com.tanwar.market_pilot.portfolio.model.Holding
import com.tanwar.market_pilot.portfolio.persistence.HoldingEntity
import com.tanwar.market_pilot.portfolio.persistence.PortfolioEntity

object HoldingMapper {

    fun toEntity(
        domain: Holding,
        portfolioEntity: PortfolioEntity
    ): HoldingEntity {
        return HoldingEntity(
            id = domain.id,
            symbol = domain.symbol,
            quantity = domain.quantity,
            averagePrice = domain.averagePrice,
            portfolio = portfolioEntity
        )
    }

    fun toDomain(entity: HoldingEntity): Holding {
        return Holding(
            id = requireNotNull(entity.id),
            symbol = entity.symbol,
            quantity = entity.quantity,
            averagePrice = entity.averagePrice,
            portfolioId = requireNotNull(entity.portfolio?.id)
        )
    }
}