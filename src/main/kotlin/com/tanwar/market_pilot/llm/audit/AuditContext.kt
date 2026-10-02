package com.tanwar.market_pilot.llm.audit

import java.util.UUID

data class AuditContext(
    val userId: UUID?,
    val conversationId: String?,
    val turnId: String?
)
