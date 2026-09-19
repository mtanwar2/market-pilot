package com.tanwar.market_pilot.portfolio.service

import com.tanwar.market_pilot.portfolio.Portfolio
import com.tanwar.market_pilot.portfolio.persistence.PortfolioMapper
import com.tanwar.market_pilot.portfolio.persistence.repository.PortfolioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class PortfolioService(
    private val portfolioRepository: PortfolioRepository
) {
          @Transactional
    fun createPortfolio(portfolio: Portfolio): Portfolio {

        val entity = PortfolioMapper.toEntity(portfolio)

        val savedEntity = portfolioRepository.save(entity)

        return PortfolioMapper.toDomain(savedEntity)
    }
          @Transactional(readOnly = true)
    fun getPortfolio(id: UUID): Portfolio {

        val entity = portfolioRepository.findById(id)
            .orElseThrow {
                IllegalArgumentException("Portfolio not found: $id")
            }

        return PortfolioMapper.toDomain(entity)
    }
}