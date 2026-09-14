package com.tanwar.market_pilot.portfolio.service

import com.tanwar.market_pilot.portfolio.controller.CreateHoldingRequest
import com.tanwar.market_pilot.portfolio.model.Holding
import com.tanwar.market_pilot.portfolio.persistence.HoldingEntity
import com.tanwar.market_pilot.portfolio.persistence.mapper.HoldingMapper
import com.tanwar.market_pilot.portfolio.persistence.repository.HoldingRepository
import com.tanwar.market_pilot.portfolio.persistence.repository.PortfolioRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HoldingService(
    private val portfolioRepository: PortfolioRepository,
    private val holdingRepository: HoldingRepository
) {

    fun addHolding(
        portfolioId: UUID,
        request: CreateHoldingRequest
    ): Holding {

        val portfolioEntity = portfolioRepository
            .findById(portfolioId)
            .orElseThrow {
                IllegalArgumentException(
                    "Portfolio not found: $portfolioId"
                )
            }

        val holdingEntity = HoldingEntity(
            symbol = request.symbol,
            quantity = request.quantity,
            averagePrice = request.averagePrice,
            portfolio = portfolioEntity
        )

        val savedEntity = holdingRepository.save(holdingEntity)

        return HoldingMapper.toDomain(savedEntity)
    }
}