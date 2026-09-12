package com.tanwar.market_pilot.llm.client.resilience.config

import com.tanwar.market_pilot.llm.properties.CircuitBreakerProperties
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class CircuitBreakerConfiguration(
    private val properties: CircuitBreakerProperties
) {

    @Bean
    fun circuitBreakerRegistry(): CircuitBreakerRegistry {
        val config = CircuitBreakerConfig.custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(properties.slidingWindowSize)
            .minimumNumberOfCalls(properties.minimumNumberOfCalls)
            .failureRateThreshold(properties.failureRateThreshold)
            .waitDurationInOpenState(
                Duration.ofMillis(properties.waitDurationInOpenStateMs)
            )
            .permittedNumberOfCallsInHalfOpenState(
                properties.permittedCallsInHalfOpenState
            )
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build()

        return CircuitBreakerRegistry.of(config)
    }
}