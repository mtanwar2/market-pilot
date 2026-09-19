package com.tanwar.market_pilot.portfolio.market.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AlphaVantageQuoteResponse(
    @JsonProperty("Global Quote")
    val globalQuote: GlobalQuote? = null,

    @JsonProperty("Information")
    val information: String? = null,

    @JsonProperty("Note")
    val note: String? = null,

    @JsonProperty("Error Message")
    val errorMessage: String? = null
)

data class GlobalQuote(
    @JsonProperty("01. symbol")
    val symbol: String = "",

    @JsonProperty("05. price")
    val price: String = ""
)