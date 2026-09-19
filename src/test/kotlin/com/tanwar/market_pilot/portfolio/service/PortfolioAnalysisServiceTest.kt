package com.tanwar.market_pilot.portfolio.service

import com.tanwar.market_pilot.portfolio.market.model.MarketData
import com.tanwar.market_pilot.portfolio.persistence.HoldingEntity
import com.tanwar.market_pilot.portfolio.persistence.PortfolioEntity
import com.tanwar.market_pilot.portfolio.persistence.repository.PortfolioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PortfolioAnalysisServiceTest {

    @Mock
    lateinit var portfolioRepository: PortfolioRepository

    @Mock
    lateinit var marketDataService: MarketDataService

    private lateinit var service: PortfolioAnalysisService

    @Test
    fun `should calculate portfolio analysis for single holding`() {

        service = PortfolioAnalysisService(
            portfolioRepository = portfolioRepository,
            marketDataService = marketDataService
        )

        val portfolioId = UUID.randomUUID()

        val portfolio = PortfolioEntity(
            id = portfolioId,
            name = "Technology Portfolio",
            createdAt = Instant.now()
        )

        val holding = HoldingEntity(
            id = UUID.randomUUID(),
            symbol = "NVDA",
            quantity = BigDecimal("10"),
            averagePrice = BigDecimal("175.50"),
            portfolio = portfolio
        )

        portfolio.holdings.add(holding)

        whenever(
            portfolioRepository.findById(portfolioId)
        ).thenReturn(Optional.of(portfolio))

        whenever(
            marketDataService.getMarketData("NVDA")
        ).thenReturn(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("170.00")
            )
        )

        val result = service.analyze(portfolioId)

        assertEquals(
            BigDecimal("1755.00"),
            result.totalInvested
        )

        assertEquals(
            BigDecimal("1700.00"),
            result.totalCurrentValue
        )

        assertEquals(
            BigDecimal("-55.00"),
            result.totalProfit
        )

        assertEquals(
            BigDecimal("-3.13"),
            result.totalProfitPercentage
        )

        assertEquals(1, result.holdings.size)

        val holdingAnalysis = result.holdings.first()

        assertEquals("NVDA", holdingAnalysis.symbol)
        assertEquals(BigDecimal("10"), holdingAnalysis.quantity)
        assertEquals(BigDecimal("175.50"), holdingAnalysis.averagePrice)
        assertEquals(BigDecimal("170.00"), holdingAnalysis.currentPrice)
        assertEquals(BigDecimal("1755.00"), holdingAnalysis.investedAmount)
        assertEquals(BigDecimal("1700.00"), holdingAnalysis.currentValue)
        assertEquals(BigDecimal("-55.00"), holdingAnalysis.profit)
        assertEquals(BigDecimal("-3.13"), holdingAnalysis.profitPercentage)
        assertEquals(BigDecimal("100.00"), holdingAnalysis.allocationPercentage)
    }

    @Test
    fun `should return zero analysis for empty portfolio`() {

        service = PortfolioAnalysisService(
            portfolioRepository = portfolioRepository,
            marketDataService = marketDataService
        )

        val portfolioId = UUID.randomUUID()

        val portfolio = PortfolioEntity(
            id = portfolioId,
            name = "Empty Portfolio",
            createdAt = Instant.now()
        )

        whenever(
            portfolioRepository.findById(portfolioId)
        ).thenReturn(Optional.of(portfolio))

        val result = service.analyze(portfolioId)

        assertEquals(BigDecimal.ZERO, result.totalInvested)
        assertEquals(BigDecimal.ZERO, result.totalCurrentValue)
        assertEquals(BigDecimal.ZERO, result.totalProfit)
        assertEquals(BigDecimal.ZERO, result.totalProfitPercentage)
        assertTrue(result.holdings.isEmpty())
    }


    @Test
    fun `should calculate analysis for multiple holdings`() {

        service = PortfolioAnalysisService(
            portfolioRepository = portfolioRepository,
            marketDataService = marketDataService
        )

        val portfolioId = UUID.randomUUID()

        val portfolio = PortfolioEntity(
            id = portfolioId,
            name = "Diversified Portfolio",
            createdAt = Instant.now()
        )

        val nvda = HoldingEntity(
            id = UUID.randomUUID(),
            symbol = "NVDA",
            quantity = BigDecimal("10"),
            averagePrice = BigDecimal("100"),
            portfolio = portfolio
        )

        val apple = HoldingEntity(
            id = UUID.randomUUID(),
            symbol = "AAPL",
            quantity = BigDecimal("5"),
            averagePrice = BigDecimal("100"),
            portfolio = portfolio
        )

        portfolio.holdings.add(nvda)
        portfolio.holdings.add(apple)

        whenever(
            portfolioRepository.findById(portfolioId)
        ).thenReturn(Optional.of(portfolio))

        whenever(
            marketDataService.getMarketData("NVDA")
        ).thenReturn(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("120")
            )
        )

        whenever(
            marketDataService.getMarketData("AAPL")
        ).thenReturn(
            MarketData(
                symbol = "AAPL",
                currentPrice = BigDecimal("80")
            )
        )

        val result = service.analyze(portfolioId)

        assertEquals(
            BigDecimal("1500"),
            result.totalInvested
        )

        assertEquals(
            BigDecimal("1600"),
            result.totalCurrentValue
        )

        assertEquals(
            BigDecimal("100"),
            result.totalProfit
        )

        assertEquals(
            BigDecimal("6.67"),
            result.totalProfitPercentage
        )

        assertEquals(2, result.holdings.size)
    }

    @Test
    fun `should throw exception when portfolio does not exist`() {

        service = PortfolioAnalysisService(
            portfolioRepository = portfolioRepository,
            marketDataService = marketDataService
        )

        val portfolioId = UUID.randomUUID()

        whenever(
            portfolioRepository.findById(portfolioId)
        ).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            service.analyze(portfolioId)
        }

        assertEquals(
            "Portfolio not found: $portfolioId",
            exception.message
        )
    }



}