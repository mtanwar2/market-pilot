package com.tanwar.market_pilot.rag.evaluation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RetrievalMetricsTest {

    @Test
    fun `should calculate retrieval precision`() {

        val retrieved = listOf(
            "NVIDIA Data Center revenue grew strongly.",
            "Apple launched a new iPhone.",
            "NVIDIA's Edge Computing revenue increased."
        )

        val relevant = listOf(
            "NVIDIA Data Center revenue grew strongly.",
            "NVIDIA's Edge Computing revenue increased."
        )

        val precision = RetrievalMetrics.precision(
            retrievedTexts = retrieved,
            relevantTexts = relevant
        )

        assertEquals(
            2.0 / 3.0,
            precision,
            0.0001
        )
    }

    @Test
    fun `should calculate retrieval recall`() {

        val retrieved = listOf(
            "NVIDIA Data Center revenue grew strongly.",
            "Apple launched a new iPhone.",
            "NVIDIA's Edge Computing revenue increased."
        )

        val relevant = listOf(
            "NVIDIA Data Center revenue grew strongly.",
            "NVIDIA's Edge Computing revenue increased.",
            "NVIDIA revenue was driven by AI demand."
        )

        val recall = RetrievalMetrics.recall(
            retrievedTexts = retrieved,
            relevantTexts = relevant
        )

        assertEquals(
            2.0 / 3.0,
            recall,
            0.0001
        )
    }

    @Test
    fun `should calculate reciprocal rank`() {

        val retrieved = listOf(
            "Apple information.",
            "Microsoft information.",
            "NVIDIA Data Center revenue grew strongly."
        )

        val relevant = listOf(
            "NVIDIA Data Center revenue grew strongly."
        )

        val reciprocalRank = RetrievalMetrics.reciprocalRank(
            retrievedTexts = retrieved,
            relevantTexts = relevant
        )

        assertEquals(
            1.0 / 3.0,
            reciprocalRank,
            0.0001
        )
    }
}