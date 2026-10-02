package com.tanwar.market_pilot.llm.audit

import java.time.Instant
import java.util.UUID

data class AuditEvent(
    val eventType: AuditEventType,
    val userId: UUID?,
    val conversationId: String?,
    val turnId: String?,
    val toolName: String?,
    val toolCallId: String?,
    val success: Boolean?,
    val errorCode: String?,
    val durationMs: Long?,
    val timestamp: Instant = Instant.now()
)

enum class AuditEventType {
    TOOL_EXECUTION
}
