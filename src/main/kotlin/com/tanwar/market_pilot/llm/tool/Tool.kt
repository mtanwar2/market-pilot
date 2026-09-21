package com.tanwar.market_pilot.llm.tool

interface Tool {

    val name: String

    fun execute(arguments: Map<String, Any?>): Any
}