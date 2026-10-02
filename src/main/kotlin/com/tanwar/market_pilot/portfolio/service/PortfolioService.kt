package com.tanwar.market_pilot.portfolio.service

import com.tanwar.market_pilot.portfolio.Portfolio
import com.tanwar.market_pilot.portfolio.persistence.PortfolioMapper
import com.tanwar.market_pilot.portfolio.persistence.repository.PortfolioRepository
import com.tanwar.market_pilot.security.UserContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PortfolioService(
    private val portfolioRepository: PortfolioRepository,
    private val userContext: UserContext
) {

    @Transactional
    fun createPortfolio(
        name: String
    ): Portfolio {

        val currentUserId = userContext
            .currentUser()
            .userId

        val portfolio = Portfolio(
            id = UUID.randomUUID(),
            userId = currentUserId,
            name = name,
            createdAt = Instant.now(),
            holdings = emptyList()
        )

        val entity =
            PortfolioMapper.toEntity(portfolio)

        val savedEntity =
            portfolioRepository.save(entity)

        return PortfolioMapper.toDomain(savedEntity)
    }

    @Transactional(readOnly = true)
    fun getPortfolio(
        id: UUID
    ): Portfolio {

        val currentUserId = userContext
            .currentUser()
            .userId

        val entity =
            portfolioRepository.findByIdAndUserId(
                id = id,
                userId = currentUserId
            )
                ?: throw IllegalArgumentException(
                    "Portfolio not found: $id"
                )

        return PortfolioMapper.toDomain(entity)
    }
}