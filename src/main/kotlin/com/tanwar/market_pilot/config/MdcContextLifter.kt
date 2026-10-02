package com.tanwar.market_pilot.config

import org.reactivestreams.Subscription
import org.slf4j.MDC
import reactor.core.CoreSubscriber
import reactor.util.context.Context
import reactor.util.context.ContextView

/**
 * Bridges values from Reactor Context into SLF4J MDC.
 *
 * Reactor Context travels with the reactive pipeline.
 * MDC is associated with the current thread.
 *
 * This bridge copies the values from Reactor Context
 * into MDC whenever Reactor executes downstream work.
 */
class MdcContextLifter<T : Any>(
    private val actual: CoreSubscriber<in T>
) : CoreSubscriber<T> {

    override fun currentContext(): Context {
        return actual.currentContext()
    }

    override fun onSubscribe(s: Subscription) {
        actual.onSubscribe(s)
    }

    override fun onNext(value: T) {
        withMdc {
            actual.onNext(value)
        }
    }

    override fun onError(error: Throwable) {
        withMdc {
            actual.onError(error)
        }
    }

    override fun onComplete() {
        withMdc {
            actual.onComplete()
        }
    }

    private fun <R> withMdc(block: () -> R): R {
        val context = currentContext()

        setMdcFromContext(context)

        return try {
            block()
        } finally {
            clearMdc()
        }
    }

    private fun setMdcFromContext(context: ContextView) {

        context.getOrEmpty<String>(
            ChatLogContext.REQUEST_ID
        ).ifPresent {
            MDC.put(
                ChatLogContext.REQUEST_ID,
                it
            )
        }

        context.getOrEmpty<String>(
            ChatLogContext.CONVERSATION_ID
        ).ifPresent {
            MDC.put(
                ChatLogContext.CONVERSATION_ID,
                it
            )
        }

        context.getOrEmpty<String>(
            ChatLogContext.TURN_ID
        ).ifPresent {
            MDC.put(
                ChatLogContext.TURN_ID,
                it
            )
        }
    }

    private fun clearMdc() {
        ChatLogContext.clearRequestScoped()
    }
}
