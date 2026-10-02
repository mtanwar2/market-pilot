package com.tanwar.market_pilot.observability

import java.util.UUID

data class RequestContext(
    val conversationId: String,
    val turnId: String,
    val userId: UUID
)