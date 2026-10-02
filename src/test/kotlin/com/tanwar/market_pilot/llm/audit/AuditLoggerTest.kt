package com.tanwar.market_pilot.llm.audit

import org.junit.jupiter.api.Test
import java.util.UUID

class AuditLoggerTest {

    private val auditLogger = AuditLogger()

    @Test
    fun `should log successful tool execution`() {

        val event = AuditEvent(
            eventType = AuditEventType.TOOL_EXECUTION,
            userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
            ),
            conversationId = "conversation-1",
            turnId = "turn-1",
            toolName = "getStockPrice",
            toolCallId = "call-1",
            success = true,
            errorCode = null,
            durationMs = 42
        )

        auditLogger.log(event)
    }

    @Test
    fun `should log failed tool execution`() {

        val event = AuditEvent(
            eventType = AuditEventType.TOOL_EXECUTION,
            userId = UUID.fromString(
                "11111111-1111-1111-1111-111111111111"
            ),
            conversationId = "conversation-1",
            turnId = "turn-2",
            toolName = "getPortfolio",
            toolCallId = "call-2",
            success = false,
            errorCode = "INVALID_ARGUMENT",
            durationMs = 5
        )

        auditLogger.log(event)
    }

}
