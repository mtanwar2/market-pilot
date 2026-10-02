/*
package com.tanwar.market_pilot.config

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks
import reactor.core.publisher.Operators

@Configuration
class ReactorMdcConfiguration {

    companion object {
        private const val MDC_HOOK_KEY = "market-pilot-mdc"

        private val MDC_LIFTER =
            Operators.lift<Any, Any> { _, subscriber ->
                MdcContextLifter(subscriber)
            }
    }

    @PostConstruct
    fun registerMdcHook() {
        Hooks.onEachOperator(
            MDC_HOOK_KEY,
            MDC_LIFTER
        )
    }

    @PreDestroy
    fun removeMdcHook() {
        Hooks.resetOnEachOperator(
            MDC_HOOK_KEY
        )
    }
}
*/
