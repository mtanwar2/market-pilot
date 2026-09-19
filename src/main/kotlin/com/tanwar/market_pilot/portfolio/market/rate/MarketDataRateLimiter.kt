package com.tanwar.market_pilot.portfolio.market.rate

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class MarketDataRateLimiter {

    private val lastRequestTime = AtomicLong(0)

    private val log =
        LoggerFactory.getLogger(MarketDataRateLimiter::class.java)

    fun waitIfNeeded() {

        while (true) {

            val now = System.currentTimeMillis()
            val lastRequest = lastRequestTime.get()

            val elapsed = now - lastRequest

            if (elapsed < 1000) {

                val waitTime = 1000 - elapsed

                log.info(
                    "Market data rate limiter waiting {} ms",
                    waitTime
                )

                Thread.sleep(waitTime)
            }

            val requestTime = System.currentTimeMillis()

            if (lastRequestTime.compareAndSet(
                    lastRequest,
                    requestTime
                )
            ) {

                log.info(
                    "Market data request slot acquired at {}",
                    requestTime
                )

                return
            }
        }
    }
}