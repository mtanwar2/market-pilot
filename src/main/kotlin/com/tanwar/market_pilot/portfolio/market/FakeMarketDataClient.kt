package com.tanwar.market_pilot.portfolio.market

import com.tanwar.market_pilot.portfolio.market.model.MarketData
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnProperty(
    prefix = "market-data",
    name = ["provider"],
    havingValue = "fake",
    matchIfMissing = true
)
class FakeMarketDataClient : MarketDataClient {

    private val prices = mapOf(
        "AAPL" to BigDecimal("190.00"),
        "NVDA" to BigDecimal("185.00"),
        "MSFT" to BigDecimal("420.00"),
        "GOOGL" to BigDecimal("250.00")
    )

    override fun getMarketData(symbol: String): MarketData {
        val normalizedSymbol = symbol.uppercase()

        val price = prices[normalizedSymbol]
            ?: throw IllegalArgumentException(
                "Market data not available for symbol: $symbol"
            )

        return MarketData(
            symbol = normalizedSymbol,
            currentPrice = price
        )
    }
}
