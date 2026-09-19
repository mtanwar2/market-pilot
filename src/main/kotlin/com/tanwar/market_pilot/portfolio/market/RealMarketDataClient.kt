package com.tanwar.market_pilot.portfolio.market

import com.tanwar.market_pilot.portfolio.market.model.MarketData
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

class RealMarketDataClient(
    private val webClientBuilder: WebClient.Builder
) : MarketDataClient {

    private val webClient = webClientBuilder
        .baseUrl("YOUR_MARKET_DATA_API_BASE_URL")
        .build()

    override fun getMarketData(symbol: String): MarketData {
        // external API call
        TODO("Implement")
    }
}