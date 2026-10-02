package com.tanwar.market_pilot.llm.audit

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuditLogger {

    fun log(event: AuditEvent) {

        log.info(
            "AUDIT eventType={} userId={} conversationId={} turnId={} " +
                    "toolName={} toolCallId={} success={} errorCode={} " +
                    "durationMs={} timestamp={}",
            event.eventType,
            event.userId,
            event.conversationId,
            event.turnId,
            event.toolName,
            event.toolCallId,
            event.success,
            event.errorCode,
            event.durationMs,
            event.timestamp
        )
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(AuditLogger::class.java)
    }

}
