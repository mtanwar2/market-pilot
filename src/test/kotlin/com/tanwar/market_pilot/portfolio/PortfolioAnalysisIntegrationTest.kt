package com.tanwar.market_pilot.portfolio

import com.tanwar.market_pilot.portfolio.market.MarketDataClient
import com.tanwar.market_pilot.portfolio.market.model.MarketData
import com.tanwar.market_pilot.portfolio.persistence.HoldingEntity
import com.tanwar.market_pilot.portfolio.persistence.PortfolioEntity
import com.tanwar.market_pilot.portfolio.persistence.repository.PortfolioRepository
import com.tanwar.market_pilot.portfolio.service.PortfolioAnalysisService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.math.BigDecimal
import java.time.Instant

@SpringBootTest
class PortfolioAnalysisIntegrationTest {

    @Autowired
    lateinit var portfolioRepository: PortfolioRepository

    @Autowired
    lateinit var portfolioAnalysisService: PortfolioAnalysisService

    @MockitoBean
    lateinit var marketDataClient: MarketDataClient

    @Test
    fun `should analyze portfolio using real application wiring`() {

        // --------------------------------------------------
        // 1. Create portfolio
        // --------------------------------------------------

        val portfolio = PortfolioEntity(
            name = "Technology Portfolio",
            createdAt = Instant.now()
        )

        // --------------------------------------------------
        // 2. Add holding
        // --------------------------------------------------

        val holding = HoldingEntity(
            symbol = "NVDA",
            quantity = BigDecimal("10"),
            averagePrice = BigDecimal("175.50"),
            portfolio = portfolio
        )

        portfolio.holdings.add(holding)

        // --------------------------------------------------
        // 3. Save using real JPA repository
        // --------------------------------------------------

        val savedPortfolio = portfolioRepository.save(portfolio)

        // --------------------------------------------------
        // 4. Mock only the external market-data boundary
        // --------------------------------------------------

        whenever(
            marketDataClient.getMarketData("NVDA")
        ).thenReturn(
            MarketData(
                symbol = "NVDA",
                currentPrice = BigDecimal("170.00")
            )
        )

        // --------------------------------------------------
        // 5. Execute real application service
        // --------------------------------------------------

        val result = portfolioAnalysisService.analyze(
            savedPortfolio.id!!
        )

        // --------------------------------------------------
        // 6. Verify portfolio-level result
        // --------------------------------------------------

        assertEquals(
            savedPortfolio.id,
            result.portfolioId
        )

        assertBigDecimalEquals(
            BigDecimal("1755.00"),
            result.totalInvested
        )

        assertBigDecimalEquals(
            BigDecimal("1700.00"),
            result.totalCurrentValue
        )

        assertBigDecimalEquals(
            BigDecimal("-55.00"),
            result.totalProfit
        )

        assertBigDecimalEquals(
            BigDecimal("-3.13"),
            result.totalProfitPercentage
        )

        // --------------------------------------------------
        // 7. Verify holding-level result
        // --------------------------------------------------

        assertEquals(
            1,
            result.holdings.size
        )

        val holdingResult = result.holdings[0]

        assertEquals(
            "NVDA",
            holdingResult.symbol
        )

        assertBigDecimalEquals(
            BigDecimal("10"),
            holdingResult.quantity
        )

        assertBigDecimalEquals(
            BigDecimal("175.50"),
            holdingResult.averagePrice
        )

        assertBigDecimalEquals(
            BigDecimal("170.00"),
            holdingResult.currentPrice
        )

        assertBigDecimalEquals(
            BigDecimal("1755.00"),
            holdingResult.investedAmount
        )

        assertBigDecimalEquals(
            BigDecimal("1700.00"),
            holdingResult.currentValue
        )

        assertBigDecimalEquals(
            BigDecimal("-55.00"),
            holdingResult.profit
        )

        assertBigDecimalEquals(
            BigDecimal("-3.13"),
            holdingResult.profitPercentage
        )

        assertBigDecimalEquals(
            BigDecimal("100.00"),
            holdingResult.allocationPercentage
        )
    }

    private fun assertBigDecimalEquals(
        expected: BigDecimal,
        actual: BigDecimal
    ) {
        assertEquals(
            0,
            expected.compareTo(actual),
            "Expected $expected but was $actual"
        )
    }
}