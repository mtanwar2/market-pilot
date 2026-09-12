package com.tanwar.market_pilot.config

import com.tanwar.market_pilot.llm.properties.TimeoutProperties
import io.netty.channel.ChannelOption
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig(
    private val timeoutProperties: TimeoutProperties
) {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun webClientBuilder(): WebClient.Builder {
        val timeout = Duration.ofMillis(timeoutProperties.durationMs)
        val httpClient = HttpClient.create()
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                timeoutProperties.durationMs
                    .coerceAtMost(Int.MAX_VALUE.toLong())
                    .toInt()
            )
            .responseTimeout(timeout)

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.USER_AGENT, "market-pilot/0.0.1")
    }
}