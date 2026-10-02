package com.tanwar.market_pilot.rag.service

import org.springframework.stereotype.Component

@Component
class DocumentLoader {

    fun load(path: String): String {
        return this::class.java
            .getResource(path)
            ?.readText()
            ?: throw IllegalArgumentException(
                "Document not found: $path"
            )
    }
}