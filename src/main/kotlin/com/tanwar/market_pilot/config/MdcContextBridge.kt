package com.tanwar.market_pilot.config

import org.slf4j.MDC
import reactor.util.context.ContextView

object MdcContextBridge {

    fun withMdc(
        context: ContextView,
        block: () -> Unit
    ) {

        try {

            if (context.hasKey(ChatLogContext.REQUEST_ID)) {
                MDC.put(
                    ChatLogContext.REQUEST_ID,
                    context.get<String>(
                        ChatLogContext.REQUEST_ID
                    )
                )
            }

            if (context.hasKey(ChatLogContext.CONVERSATION_ID)) {
                MDC.put(
                    ChatLogContext.CONVERSATION_ID,
                    context.get<String>(
                        ChatLogContext.CONVERSATION_ID
                    )
                )
            }

            if (context.hasKey(ChatLogContext.TURN_ID)) {
                MDC.put(
                    ChatLogContext.TURN_ID,
                    context.get<String>(
                        ChatLogContext.TURN_ID
                    )
                )
            }

            block()

        } finally {

            // Reactor threads are shared between requests.
            // Always clear request-specific MDC values.
            ChatLogContext.clearRequestScoped()
        }
    }
}