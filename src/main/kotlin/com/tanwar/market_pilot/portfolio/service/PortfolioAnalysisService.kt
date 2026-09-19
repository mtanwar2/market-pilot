package com.tanwar.market_pilot.portfolio.service

import com.tanwar.market_pilot.portfolio.analysis.model.HoldingAnalysis
import com.tanwar.market_pilot.portfolio.analysis.model.PortfolioAnalysis
import com.tanwar.market_pilot.portfolio.persistence.HoldingEntity
import com.tanwar.market_pilot.portfolio.persistence.repository.PortfolioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Service
class PortfolioAnalysisService(
    private val portfolioRepository: PortfolioRepository,
    private val marketDataService: MarketDataService
) {
          @Transactional(readOnly = true)
    fun analyze(portfolioId: UUID): PortfolioAnalysis {

        val portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow {
                IllegalArgumentException(
                    "Portfolio not found: $portfolioId"
                )
            }

        val marketPrices = portfolio.holdings.associate { holding ->
            val marketData = marketDataService.getMarketData(
                holding.symbol
            )

            holding to marketData.currentPrice
        }

        val totalInvested = portfolio.holdings
            .map { calculateInvestedAmount(it) }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        val totalCurrentValue = portfolio.holdings
            .map { holding ->
                holding.quantity.multiply(marketPrices[holding]!!)
            }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        val holdingAnalyses = portfolio.holdings.map { holding ->

            val currentPrice = marketPrices[holding]!!
            val investedAmount = calculateInvestedAmount(holding)
            val currentValue = holding.quantity.multiply(currentPrice)
            val profit = currentValue.subtract(investedAmount)

            HoldingAnalysis(
                symbol = holding.symbol,
                quantity = holding.quantity,
                averagePrice = holding.averagePrice,
                currentPrice = currentPrice,
                investedAmount = investedAmount,
                currentValue = currentValue,
                profit = profit,
                profitPercentage = calculatePercentage(
                    profit,
                    investedAmount
                ),
                allocationPercentage = calculatePercentage(
                    currentValue,
                    totalCurrentValue
                )
            )
        }

        val totalProfit = totalCurrentValue.subtract(totalInvested)

        return PortfolioAnalysis(
            portfolioId = portfolio.id!!,
            totalInvested = totalInvested,
            totalCurrentValue = totalCurrentValue,
            totalProfit = totalProfit,
            totalProfitPercentage = calculatePercentage(
                totalProfit,
                totalInvested
            ),
            holdings = holdingAnalyses
        )
    }

    private fun calculateInvestedAmount(
        holding: HoldingEntity
    ): BigDecimal {
        return holding.quantity.multiply(holding.averagePrice)
    }

    private fun calculatePercentage(
        value: BigDecimal,
        total: BigDecimal
    ): BigDecimal {

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
        }

        return value
            .divide(total, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP)
    }
}