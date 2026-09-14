package com.tanwar.market_pilot.config

import jakarta.servlet.AsyncEvent
import jakarta.servlet.AsyncListener
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestCorrelationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request
            .getHeader(REQUEST_ID_HEADER)
            ?.takeIf(VALID_REQUEST_ID::matches)
            ?: UUID.randomUUID().toString()

        MDC.put(ChatLogContext.REQUEST_ID, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            if (request.isAsyncStarted) {
                request.asyncContext.addListener(MdcCleanupListener)
            } else {
                ChatLogContext.clearRequestScoped()
            }
        }
    }

    private object MdcCleanupListener : AsyncListener {
        override fun onComplete(event: AsyncEvent) {
            ChatLogContext.clearRequestScoped()
        }

        override fun onTimeout(event: AsyncEvent) {
            ChatLogContext.clearRequestScoped()
        }

        override fun onError(event: AsyncEvent) {
            ChatLogContext.clearRequestScoped()
        }

        override fun onStartAsync(event: AsyncEvent) {
            event.asyncContext.addListener(this)
        }
    }

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-ID"
        private val VALID_REQUEST_ID = Regex("[A-Za-z0-9._-]{1,100}")
    }
}
