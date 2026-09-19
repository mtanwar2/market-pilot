package com.tanwar.market_pilot.portfolio.market

import com.tanwar.market_pilot.portfolio.market.exception.MarketDataException
import com.tanwar.market_pilot.portfolio.market.model.AlphaVantageQuoteResponse
import com.tanwar.market_pilot.portfolio.market.model.MarketData
import com.tanwar.market_pilot.portfolio.market.properties.MarketDataProperties
import com.tanwar.market_pilot.portfolio.market.rate.MarketDataRateLimiter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

@Component
@ConditionalOnProperty(
    prefix = "market-data",
    name = ["provider"],
    havingValue = "alphavantage"
)
class AlphaVantageMarketDataClient(
    private val webClientBuilder: WebClient.Builder,
    private val marketDataProperties: MarketDataProperties,
    private val rateLimiter: MarketDataRateLimiter
) : MarketDataClient {

    private val webClient = webClientBuilder
        .baseUrl(marketDataProperties.baseUrl)
        .build()
    private val log = LoggerFactory.getLogger(AlphaVantageMarketDataClient::class.java)
    override fun getMarketData(symbol: String): MarketData {

        val normalizedSymbol = symbol.trim().uppercase()

        if (normalizedSymbol.isBlank()) {
            throw MarketDataException("Stock symbol must not be blank")
        }

        rateLimiter.waitIfNeeded()

        log.info("Sending market data request for symbol={}", normalizedSymbol)

        val response = try {
            webClient.get()
                .uri { builder ->
                    builder
                        .path("/query")
                        .queryParam("function", "GLOBAL_QUOTE")
                        .queryParam("symbol", normalizedSymbol)
                        .queryParam("apikey", marketDataProperties.apiKey)
                        .build()
                }
                .retrieve()
                .bodyToMono(AlphaVantageQuoteResponse::class.java)
                .block()
        } catch (ex: Exception) {
            throw MarketDataException(
                "Failed to fetch market data for symbol=$normalizedSymbol",
                ex
            )
        }

        log.info("Alpha Vantage response for symbol={}: {}", normalizedSymbol, response)


        if (response == null) {
            throw MarketDataException(
                "Empty response from Alpha Vantage for symbol=$normalizedSymbol"
            )
        }

        val quote = response.globalQuote

        if (quote == null) {
            val message = response.information
                ?: response.note
                ?: response.errorMessage
                ?: "No quote data returned"

            throw MarketDataException(
                "Alpha Vantage did not return market data for symbol=$normalizedSymbol: $message"
            )
        }

        if (quote.symbol.isBlank() || quote.price.isBlank()) {
            throw MarketDataException(
                "Invalid quote returned by Alpha Vantage for symbol=$normalizedSymbol"
            )
        }

        return try {
            MarketData(
                symbol = quote.symbol,
                currentPrice = BigDecimal(quote.price)
            )
        } catch (ex: NumberFormatException) {
            throw MarketDataException(
                "Invalid price returned by Alpha Vantage for symbol=$normalizedSymbol",
                ex
            )
        }
    }
}