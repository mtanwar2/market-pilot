package com.tanwar.market_pilot.config

import org.reactivestreams.Subscription
import reactor.core.CoreSubscriber
import reactor.util.context.Context

class MdcContextSubscriber<T : Any>(
    private val actual: CoreSubscriber<in T>
) : CoreSubscriber<T> {

    override fun currentContext(): Context =
        actual.currentContext()

    override fun onSubscribe(subscription: Subscription) {
        actual.onSubscribe(subscription)
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

    private fun withMdc(block: () -> Unit) {
        val context = currentContext()

        MdcContextBridge.withMdc(
            context = context,
            block = block
        )
    }
}