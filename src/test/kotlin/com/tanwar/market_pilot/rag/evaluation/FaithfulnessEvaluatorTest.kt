package com.tanwar.market_pilot.rag.evaluation

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier

@SpringBootTest
class FaithfulnessEvaluatorTest {

    @Autowired
    lateinit var evaluator: FaithfulnessEvaluator

    @Test
    fun `should give high score when answer is supported by context`() {

        val retrievedContext = listOf(
            "Data Center revenue for the second quarter was a record $89.0 billion, up 117% from a year ago and up 18% sequentially, driven by the ramp of our Blackwell Ultra infrastructure."
        )

        val generatedAnswer =
            "NVIDIA's Data Center revenue increased 117% due to the ramp of Blackwell Ultra infrastructure."

        StepVerifier.create(
            evaluator.evaluate(
                retrievedContext = retrievedContext,
                generatedAnswer = generatedAnswer
            )
        )
            .assertNext { result ->

                println("Score: ${result.score}")
                println("Reason: ${result.reason}")

                assertTrue(result.score >= 0.5)
                assertTrue(result.reason.isNotBlank())
            }
            .verifyComplete()
    }

    @Test
    fun `should give low score when answer contains unsupported claims`() {

        val retrievedContext = listOf(
            "Data Center revenue for the second quarter was a record $89.0 billion, up 117% from a year ago and up 18% sequentially, driven by the ramp of our Blackwell Ultra infrastructure."
        )

        val generatedAnswer =
            "NVIDIA's Data Center revenue increased 117% due to the ramp of Blackwell Ultra infrastructure and a major acquisition of OpenAI."

        StepVerifier.create(
            evaluator.evaluate(
                retrievedContext = retrievedContext,
                generatedAnswer = generatedAnswer
            )
        )
            .assertNext { result ->

                println("Score: ${result.score}")
                println("Reason: ${result.reason}")

                assertTrue(result.score <= 0.5)
                assertTrue(result.reason.isNotBlank())
            }
            .verifyComplete()
    }
}