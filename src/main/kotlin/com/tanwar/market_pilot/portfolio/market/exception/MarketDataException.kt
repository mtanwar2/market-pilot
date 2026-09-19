package com.tanwar.market_pilot.portfolio.market.exception

class MarketDataException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)