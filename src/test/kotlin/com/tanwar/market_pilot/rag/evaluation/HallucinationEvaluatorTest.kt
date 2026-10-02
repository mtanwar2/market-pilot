package com.tanwar.market_pilot.rag.evaluation

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier

@SpringBootTest
class HallucinationEvaluatorTest {

    @Autowired
    lateinit var evaluator: HallucinationEvaluator

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

                assert(result.score >= 0.5)
                assert(result.reason.isNotBlank())
            }
            .verifyComplete()
    }

    @Test
    fun `should give low score when answer contains hallucinated information`() {

        val retrievedContext = listOf(
            "Data Center revenue for the second quarter was a record $89.0 billion, up 117% from a year ago and up 18% sequentially, driven by the ramp of our Blackwell Ultra infrastructure."
        )

        val generatedAnswer =
            "NVIDIA's Data Center revenue increased 117% due to the ramp of Blackwell Ultra infrastructure and NVIDIA's acquisition of OpenAI."

        StepVerifier.create(
            evaluator.evaluate(
                retrievedContext = retrievedContext,
                generatedAnswer = generatedAnswer
            )
        )
            .assertNext { result ->

                println("Score: ${result.score}")
                println("Reason: ${result.reason}")

                assert(result.score < 0.5)
                assert(result.reason.isNotBlank())
            }
            .verifyComplete()
    }
}