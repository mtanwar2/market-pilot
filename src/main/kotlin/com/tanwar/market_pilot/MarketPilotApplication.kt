package com.tanwar.market_pilot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AiEngineeringAssistantApplication

fun main(args: Array<String>) {
	runApplication<AiEngineeringAssistantApplication>(*args)
}
