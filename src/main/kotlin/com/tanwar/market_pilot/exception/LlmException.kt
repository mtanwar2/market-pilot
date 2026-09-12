package com.tanwar.market_pilot.llm.exception

open class LlmException(
    val statusCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)