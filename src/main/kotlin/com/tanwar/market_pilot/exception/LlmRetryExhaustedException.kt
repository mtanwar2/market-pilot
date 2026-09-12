package com.tanwar.market_pilot.exception

import com.tanwar.market_pilot.llm.exception.LlmException

class LlmRetryExhaustedException(
    val attempts: Int,
    cause: Throwable
) : LlmException(
    503,
    "LLM request failed after maximum $attempts attempts",
    cause
)