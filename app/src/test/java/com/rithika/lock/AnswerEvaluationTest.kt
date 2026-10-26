package com.rithika.lock

import com.rithika.lock.data.evaluation.AnswerEvaluator
import com.rithika.lock.data.model.FeedbackResult
import com.rithika.lock.data.model.InterviewMessage
import com.rithika.lock.data.model.MessageSender
import com.rithika.lock.data.model.QuestionEvaluation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerEvaluationTest {

    private val dbQuestion = "What is a database?"

    // Case 1: Empty answer
    @Test
    fun test1_emptyAnswer() {
        val evalBlank = AnswerEvaluator.evaluateAnswer(dbQuestion, "")
        assertEquals(0, evalBlank.score)
        assertEquals("Incorrect", evalBlank.status)
        assertTrue(evalBlank.explanation.contains("No answer was provided", ignoreCase = true))

        val evalWhitespace = AnswerEvaluator.evaluateAnswer(dbQuestion, "   \n\t  ")
        assertEquals(0, evalWhitespace.score)
        assertEquals("Incorrect", evalWhitespace.status)
    }

    // Case 2: "Hi", "Hello", and "Welcome" (Greetings)
    @Test
    fun test2_greetings() {
        val greetings = listOf("Hi", "Hello", "Welcome", "hello sir", "good morning", "Hey there, how are you?")
        for (greeting in greetings) {
            val eval = AnswerEvaluator.evaluateAnswer(dbQuestion, greeting)
            assertTrue("Greeting '$greeting' score must be in 0..10, got ${eval.score}", eval.score in 0..10)
            assertEquals("Greeting '$greeting' must be marked Incorrect", "Incorrect", eval.status)
            assertTrue(
                "Feedback should mention greeting or conversational phrase",
                eval.explanation.contains("greeting", ignoreCase = true) || eval.explanation.contains("conversational", ignoreCase = true)
            )
        }
    }

    // Case 3: Completely unrelated text
    @Test
    fun test3_completelyUnrelatedText() {
        val unrelatedAnswers = listOf(
            "Cricket is a popular sport played with a bat and ball.",
            "The capital of France is Paris and it is known for the Eiffel Tower.",
            "Photosynthesis is the process by which green plants make food from sunlight."
        )
        for (ans in unrelatedAnswers) {
            val eval = AnswerEvaluator.evaluateAnswer(dbQuestion, ans)
            assertTrue("Unrelated answer score must be in 0..10, got ${eval.score}", eval.score in 0..10)
            assertEquals("Unrelated answer must be marked Incorrect", "Incorrect", eval.status)
            assertTrue(eval.explanation.contains("unrelated", ignoreCase = true))
        }
    }

    // Case 4: Technically incorrect answer
    @Test
    fun test4_technicallyIncorrectAnswer() {
        val incorrectAnswer = "A database is a hardware cable and networking switch used to connect computer devices together."
        val eval = AnswerEvaluator.evaluateAnswer(dbQuestion, incorrectAnswer)
        assertTrue("Incorrect answer score must be in 0..10, got ${eval.score}", eval.score in 0..10)
        assertEquals("Incorrect", eval.status)
    }

    // Case 5: Relevant answer with a major factual error
    @Test
    fun test5_relevantAnswerWithMajorFactualError() {
        val q = "What is the difference between TCP and UDP protocols?"
        val erroneousAnswer = "TCP is connectionless and unreliable without any handshake, whereas UDP guarantees reliable three way handshake and delivery."
        val eval = AnswerEvaluator.evaluateAnswer(q, erroneousAnswer)
        assertTrue("Major factual error score must be <= 50, got ${eval.score}", eval.score <= 50)
        assertNotEquals("Must not be marked Correct", "Correct", eval.status)
        assertTrue(eval.explanation.contains("factual error", ignoreCase = true) || eval.status == "Incorrect")
    }

    // Case 6: Partially correct answer
    @Test
    fun test6_partiallyCorrectAnswer() {
        val partialAnswer = "A database stores data."
        val eval = AnswerEvaluator.evaluateAnswer(dbQuestion, partialAnswer)
        assertTrue("Partially correct score must be in 11..50, got ${eval.score}", eval.score in 11..50)
        assertEquals("Partially Correct", eval.status)
        assertTrue(eval.missingConcepts.isNotBlank())
    }

    // Case 7: Correct answer with limited detail
    @Test
    fun test7_correctAnswerWithLimitedDetail() {
        val limitedAnswer = "A database is an organized system to store and retrieve data electronically."
        val eval = AnswerEvaluator.evaluateAnswer(dbQuestion, limitedAnswer)
        assertTrue("Correct with limited detail score must be in 51..100, got ${eval.score}", eval.score in 51..100)
        assertEquals("Correct", eval.status)
    }

    // Case 8: Complete and technically accurate answer
    @Test
    fun test8_completeAndTechnicallyAccurateAnswer() {
        val completeAnswer = "A database is an organized collection of structured data stored electronically in a computer system, typically controlled by a DBMS, enabling efficient storage, retrieval, modification, and management."
        val eval = AnswerEvaluator.evaluateAnswer(dbQuestion, completeAnswer)
        assertTrue("Comprehensive answer score should be high (80-100), got ${eval.score}", eval.score >= 80)
        assertTrue("Comprehensive answer score must be in 51..100", eval.score in 51..100)
        assertEquals("Correct", eval.status)
    }

    // Case 9: Correct answer phrased differently from the expected answer
    @Test
    fun test9_correctAnswerPhrasedDifferently() {
        val synonymAnswer = "It refers to a digital repository where structured records are systematically maintained, indexed, and fetched by applications on a server."
        val eval = AnswerEvaluator.evaluateAnswer(dbQuestion, synonymAnswer)
        assertTrue("Phrased differently answer score must be in 51..100, got ${eval.score}", eval.score in 51..100)
        assertEquals("Correct", eval.status)
    }

    // Case 10: Long answer containing both correct and incorrect claims
    @Test
    fun test10_longAnswerContainingIncorrectClaims() {
        val q = "Can you explain String, StringBuilder, and StringBuffer in Java?"
        val misleadingLongAnswer = "In Java, strings are used everywhere in enterprise software development and are fundamental. However, String is mutable and modifies internal char arrays directly in place without creating new objects, whereas StringBuilder is synchronized and thread safe for multi-threaded programming."
        val eval = AnswerEvaluator.evaluateAnswer(q, misleadingLongAnswer)
        assertTrue("Long answer with factual contradictions must not get high score (score <= 50), got ${eval.score}", eval.score <= 50)
        assertNotEquals("Must not be marked Correct", "Correct", eval.status)
    }

    // Case 11: Malformed evaluation response or evaluation-service failure
    @Test
    fun test11_malformedEvaluationResponseOrFailure() {
        // AI returned score 85 and status "Correct" for "Hello"
        val hallucinatedEval = QuestionEvaluation(
            questionNumber = 1,
            questionText = dbQuestion,
            candidateAnswer = "Hello",
            feedback = "Great greeting attempt.",
            idealAnswerSample = "A database stores organized data.",
            scoreOutOfTen = 8,
            score = 85,
            status = "Correct"
        )

        val sanitized = AnswerEvaluator.sanitizeEvaluation(hallucinatedEval, "Database Management System (DBMS)")
        assertEquals("Score for 'Hello' must be forced to 0", 0, sanitized.score)
        assertEquals("Status for 'Hello' must be forced to Incorrect", "Incorrect", sanitized.status)
        assertEquals(0, sanitized.scoreOutOfTen)

        // Whole FeedbackResult failure fallback
        val messages = listOf(
            InterviewMessage(sender = MessageSender.INTERVIEWER, text = dbQuestion),
            InterviewMessage(sender = MessageSender.CANDIDATE, text = "Hello"),
            InterviewMessage(sender = MessageSender.INTERVIEWER, text = "What is ACID?"),
            InterviewMessage(sender = MessageSender.CANDIDATE, text = "Atomicity, Consistency, Isolation, and Durability ensure reliable transactions.")
        )

        val fallbackResult = AnswerEvaluator.evaluateInterview("Database Management System (DBMS)", messages)
        assertTrue("Fallback result must generate evaluations", fallbackResult.questionEvaluations.isNotEmpty())
        assertEquals(2, fallbackResult.questionEvaluations.size)
        assertEquals("Hello must be scored 0", 0, fallbackResult.questionEvaluations[0].score)
        assertEquals("Incorrect", fallbackResult.questionEvaluations[0].status)
        assertTrue("ACID answer must be scored >= 51", fallbackResult.questionEvaluations[1].score >= 51)
        assertEquals("Correct", fallbackResult.questionEvaluations[1].status)

        // If an AI gave overallScore 90 while candidate said "Hello", sanitizeFeedbackResult corrects it
        val badAiResult = FeedbackResult(
            overallScore = 90,
            technicalScore = 90,
            communicationScore = 80,
            confidenceScore = 80,
            answerQualityScore = 90,
            strengths = listOf("Good greeting"),
            weakAreas = listOf("None"),
            suggestions = listOf("Keep greeting"),
            questionEvaluations = listOf(hallucinatedEval)
        )
        val sanitizedResult = AnswerEvaluator.sanitizeFeedbackResult(badAiResult, "DBMS", listOf(
            InterviewMessage(sender = MessageSender.INTERVIEWER, text = dbQuestion),
            InterviewMessage(sender = MessageSender.CANDIDATE, text = "Hello")
        ))
        assertEquals(0, sanitizedResult.overallScore)
        assertEquals(0, sanitizedResult.technicalScore)
        assertEquals("Incorrect", sanitizedResult.questionEvaluations[0].status)
    }

    // Case 12: Boundary scores of 0, 10, 11, 50, 51, and 100
    @Test
    fun test12_boundaryScores() {
        val eval0 = QuestionEvaluation(1, "Q", "A", "FB", "Ideal", 0, 0, "Incorrect")
        assertEquals(0, eval0.getSafeScore())
        assertEquals("Incorrect", eval0.getSafeStatus())

        val eval10 = QuestionEvaluation(1, "Q", "A", "FB", "Ideal", 1, 10, "Incorrect")
        assertEquals(10, eval10.getSafeScore())
        assertEquals("Incorrect", eval10.getSafeStatus())

        val eval11 = QuestionEvaluation(1, "Q", "A", "FB", "Ideal", 1, 11, "Partially Correct")
        assertEquals(11, eval11.getSafeScore())
        assertEquals("Partially Correct", eval11.getSafeStatus())

        val eval50 = QuestionEvaluation(1, "Q", "A", "FB", "Ideal", 5, 50, "Partially Correct")
        assertEquals(50, eval50.getSafeScore())
        assertEquals("Partially Correct", eval50.getSafeStatus())

        val eval51 = QuestionEvaluation(1, "Q", "A", "FB", "Ideal", 5, 51, "Correct")
        assertEquals(51, eval51.getSafeScore())
        assertEquals("Correct", eval51.getSafeStatus())

        val eval100 = QuestionEvaluation(1, "Q", "A", "FB", "Ideal", 10, 100, "Correct")
        assertEquals(100, eval100.getSafeScore())
        assertEquals("Correct", eval100.getSafeStatus())

        // Out-of-bounds score clamping
        val evalNegative = QuestionEvaluation(1, "Q", "A", "FB", "Ideal", -1, -15, "")
        assertEquals(0, evalNegative.getSafeScore())
        assertEquals("Incorrect", evalNegative.getSafeStatus())

        val evalOver100 = QuestionEvaluation(1, "Q", "A", "FB", "Ideal", 15, 120, "")
        assertEquals(100, evalOver100.getSafeScore())
        assertEquals("Correct", evalOver100.getSafeStatus())
    }
}
