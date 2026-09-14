package com.tanwar.market_pilot.portfolio.persistence

import com.tanwar.market_pilot.portfolio.Portfolio
import com.tanwar.market_pilot.portfolio.persistence.mapper.HoldingMapper

object PortfolioMapper {

    fun toEntity(domain: Portfolio): PortfolioEntity {
        val entity = PortfolioEntity(
            name = domain.name,
            createdAt = domain.createdAt
        )

        entity.holdings = domain.holdings
            .map {
                HoldingMapper.toEntity(
                    domain = it,
                    portfolioEntity = entity
                )
            }
            .toMutableList()

        return entity
    }

    fun toDomain(entity: PortfolioEntity): Portfolio {
        return Portfolio(
            id = requireNotNull(entity.id),
            name = entity.name,
            createdAt = entity.createdAt,
            holdings = entity.holdings.map(HoldingMapper::toDomain)
        )
    }
}