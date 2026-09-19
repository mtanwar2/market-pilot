package com.tanwar.market_pilot.portfolio.market

import com.tanwar.market_pilot.portfolio.market.exception.MarketDataException
import com.tanwar.market_pilot.portfolio.market.model.AlphaVantageQuoteResponse
import com.tanwar.market_pilot.portfolio.market.model.GlobalQuote
import com.tanwar.market_pilot.portfolio.market.properties.MarketDataProperties
import com.tanwar.market_pilot.portfolio.market.rate.MarketDataRateLimiter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.net.URI
import java.util.function.Function

class AlphaVantageMarketDataClientTest {

    @Test
    fun `should return market data for valid response`() {

        // Arrange
        val webClientBuilder = mock<WebClient.Builder>()
        val webClient = mock<WebClient>()
        val requestHeadersUriSpec =
            mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec =
            mock<WebClient.RequestHeadersSpec<*>>()
        val responseSpec =
            mock<WebClient.ResponseSpec>()

        val properties = MarketDataProperties(
            provider = "alphavantage",
            apiKey = "test-api-key",
            baseUrl = "https://www.alphavantage.co"
        )

        whenever(webClientBuilder.baseUrl(properties.baseUrl))
            .thenReturn(webClientBuilder)

        whenever(webClientBuilder.build())
            .thenReturn(webClient)

        whenever(webClient.get())
            .thenReturn(requestHeadersUriSpec)

        whenever(
            requestHeadersUriSpec.uri(
                any<Function<org.springframework.web.util.UriBuilder, URI>>()
            )
        ).thenReturn(requestHeadersSpec)

        whenever(requestHeadersSpec.retrieve())
            .thenReturn(responseSpec)

        whenever(
            responseSpec.bodyToMono(AlphaVantageQuoteResponse::class.java)
        ).thenReturn(
            Mono.just(
                AlphaVantageQuoteResponse(
                    globalQuote = GlobalQuote(
                        symbol = "AAPL",
                        price = "190.25"
                    )
                )
            )
        )

        val client = AlphaVantageMarketDataClient(
            webClientBuilder = webClientBuilder,
            marketDataProperties = properties,
            rateLimiter = MarketDataRateLimiter()
        )

        // Act
        val result = client.getMarketData("aapl")

        // Assert
        assertEquals("AAPL", result.symbol)
        assertEquals(
            BigDecimal("190.25"),
            result.currentPrice
        )
    }

    @Test
    fun `should throw MarketDataException when quote is empty`() {

        // Arrange
        val webClientBuilder = mock<WebClient.Builder>()
        val webClient = mock<WebClient>()
        val requestHeadersUriSpec =
            mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec =
            mock<WebClient.RequestHeadersSpec<*>>()
        val responseSpec =
            mock<WebClient.ResponseSpec>()

        val properties = MarketDataProperties(
            provider = "alphavantage",
            apiKey = "test-api-key",
            baseUrl = "https://www.alphavantage.co"
        )

        whenever(webClientBuilder.baseUrl(properties.baseUrl))
            .thenReturn(webClientBuilder)

        whenever(webClientBuilder.build())
            .thenReturn(webClient)

        whenever(webClient.get())
            .thenReturn(requestHeadersUriSpec)

        whenever(
            requestHeadersUriSpec.uri(
                any<Function<org.springframework.web.util.UriBuilder, URI>>()
            )
        ).thenReturn(requestHeadersSpec)

        whenever(requestHeadersSpec.retrieve())
            .thenReturn(responseSpec)

        whenever(
            responseSpec.bodyToMono(AlphaVantageQuoteResponse::class.java)
        ).thenReturn(
            Mono.just(
                AlphaVantageQuoteResponse(
                    globalQuote = null
                )
            )
        )

        val client = AlphaVantageMarketDataClient(
            webClientBuilder = webClientBuilder,
            marketDataProperties = properties,
            rateLimiter = MarketDataRateLimiter()
        )

        // Act + Assert
        val exception = org.junit.jupiter.api.assertThrows<MarketDataException> {
            client.getMarketData("AAPL")
        }

        assertEquals(
            "Alpha Vantage did not return market data for symbol=AAPL: " +
                "No quote data returned",
            exception.message
        )
    }

    @Test
    fun `should throw MarketDataException when price is invalid`() {

        // Arrange
        val webClientBuilder = mock<WebClient.Builder>()
        val webClient = mock<WebClient>()
        val requestHeadersUriSpec =
            mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec =
            mock<WebClient.RequestHeadersSpec<*>>()
        val responseSpec =
            mock<WebClient.ResponseSpec>()

        val properties = MarketDataProperties(
            provider = "alphavantage",
            apiKey = "test-api-key",
            baseUrl = "https://www.alphavantage.co"
        )

        whenever(webClientBuilder.baseUrl(properties.baseUrl))
            .thenReturn(webClientBuilder)

        whenever(webClientBuilder.build())
            .thenReturn(webClient)

        whenever(webClient.get())
            .thenReturn(requestHeadersUriSpec)

        whenever(
            requestHeadersUriSpec.uri(
                any<Function<org.springframework.web.util.UriBuilder, URI>>()
            )
        ).thenReturn(requestHeadersSpec)

        whenever(requestHeadersSpec.retrieve())
            .thenReturn(responseSpec)

        whenever(
            responseSpec.bodyToMono(AlphaVantageQuoteResponse::class.java)
        ).thenReturn(
            Mono.just(
                AlphaVantageQuoteResponse(
                    globalQuote = GlobalQuote(
                        symbol = "AAPL",
                        price = "not-a-number"
                    )
                )
            )
        )

        val client = AlphaVantageMarketDataClient(
            webClientBuilder = webClientBuilder,
            marketDataProperties = properties,
            rateLimiter = MarketDataRateLimiter()
        )

        // Act + Assert
        val exception = assertThrows<MarketDataException> {
            client.getMarketData("AAPL")
        }

        assertEquals(
            "Invalid price returned by Alpha Vantage for symbol=AAPL",
            exception.message
        )
    }

    @Test
    fun `should throw MarketDataException when Alpha Vantage returns HTTP error`() {

        // Arrange
        val webClientBuilder = mock<WebClient.Builder>()
        val webClient = mock<WebClient>()
        val requestHeadersUriSpec =
            mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec =
            mock<WebClient.RequestHeadersSpec<*>>()
        val responseSpec =
            mock<WebClient.ResponseSpec>()

        val properties = MarketDataProperties(
            provider = "alphavantage",
            apiKey = "test-api-key",
            baseUrl = "https://www.alphavantage.co"
        )

        whenever(webClientBuilder.baseUrl(properties.baseUrl))
            .thenReturn(webClientBuilder)

        whenever(webClientBuilder.build())
            .thenReturn(webClient)

        whenever(webClient.get())
            .thenReturn(requestHeadersUriSpec)

        whenever(
            requestHeadersUriSpec.uri(
                any<Function<org.springframework.web.util.UriBuilder, URI>>()
            )
        ).thenReturn(requestHeadersSpec)

        whenever(requestHeadersSpec.retrieve())
            .thenReturn(responseSpec)

        whenever(
            responseSpec.bodyToMono(AlphaVantageQuoteResponse::class.java)
        ).thenReturn(
            Mono.error(
                RuntimeException("Alpha Vantage returned HTTP 500")
            )
        )

        val client = AlphaVantageMarketDataClient(
            webClientBuilder = webClientBuilder,
            marketDataProperties = properties,
            rateLimiter = MarketDataRateLimiter()
        )

        // Act + Assert
        val exception = assertThrows<MarketDataException> {
            client.getMarketData("AAPL")
        }

        assertEquals(
            "Failed to fetch market data for symbol=AAPL",
            exception.message
        )
    }
}