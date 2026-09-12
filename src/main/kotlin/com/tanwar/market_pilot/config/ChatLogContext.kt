package com.tanwar.market_pilot.config

import org.slf4j.MDC

object ChatLogContext {
    const val REQUEST_ID = "requestId"
    const val CONVERSATION_ID = "conversationId"
    const val TURN_ID = "turnId"

    fun clearRequestScoped() {
        MDC.remove(REQUEST_ID)
        MDC.remove(CONVERSATION_ID)
        MDC.remove(TURN_ID)
    }
}
