package com.tanwar.market_pilot.portfolio.market

import com.tanwar.market_pilot.portfolio.market.model.MarketData
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class FakeMarketDataClient : MarketDataClient {

    private val prices = mapOf(
        "AAPL" to BigDecimal("190.00"),
        "NVDA" to BigDecimal("170.00"),
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
