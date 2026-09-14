package com.tanwar.market_pilot.config

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestCorrelationFilterTest {

    private val filter = RequestCorrelationFilter()

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    @Test
    fun `generates a requestId when the client omits the header`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, FilterChain { _, _ -> })

        val requestId = response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER)
        assertNotNull(requestId)
        assertEquals(true, requestId!!.isNotBlank())
        assertNull(MDC.get(ChatLogContext.REQUEST_ID))
    }

    @Test
    fun `reuses a valid client requestId`() {
        val request = MockHttpServletRequest()
        request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, "client-req-1")
        val response = MockHttpServletResponse()
        var seenDuringChain: String? = null

        filter.doFilter(
            request,
            response,
            FilterChain { _, _ ->
                seenDuringChain = MDC.get(ChatLogContext.REQUEST_ID)
            }
        )

        assertEquals("client-req-1", seenDuringChain)
        assertEquals(
            "client-req-1",
            response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER)
        )
        assertNull(MDC.get(ChatLogContext.REQUEST_ID))
    }

    @Test
    fun `ignores an invalid client requestId`() {
        val request = MockHttpServletRequest()
        request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, "not a valid id")
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, FilterChain { _, _ -> })

        val requestId = response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER)
        assertNotNull(requestId)
        assertNotEquals("not a valid id", requestId)
    }

    @Test
    fun `clears conversation and turn ids after the request`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        filter.doFilter(
            request,
            response,
            FilterChain { _, _ ->
                MDC.put(ChatLogContext.CONVERSATION_ID, "conversation-1")
                MDC.put(ChatLogContext.TURN_ID, "turn-1")
            }
        )

        assertNull(MDC.get(ChatLogContext.REQUEST_ID))
        assertNull(MDC.get(ChatLogContext.CONVERSATION_ID))
        assertNull(MDC.get(ChatLogContext.TURN_ID))
    }
}
