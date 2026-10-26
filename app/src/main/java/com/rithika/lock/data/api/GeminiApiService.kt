package com.rithika.lock.data.api

import com.rithika.lock.data.evaluation.AnswerEvaluator
import com.rithika.lock.data.model.FeedbackResult
import com.rithika.lock.data.model.InterviewDifficulty
import com.rithika.lock.data.model.InterviewMessage
import com.rithika.lock.data.model.MessageSender
import com.rithika.lock.data.model.QuestionEvaluation
import com.rithika.lock.data.model.TopicData
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateFirstQuestion(
        apiKey: String,
        topicId: String,
        difficulty: InterviewDifficulty
    ): String = withContext(Dispatchers.IO) {
        val topic = TopicData.getTopicById(topicId)
        if (apiKey.isBlank()) {
            return@withContext getFallbackFirstQuestion(topic.title, difficulty)
        }

        val prompt = """
            You are a professional campus recruitment interviewer for a top tech company.
            Generate the FIRST interview question for a candidate for the topic: "${topic.title}" (${topic.category.displayName}).
            Difficulty level: ${difficulty.displayName}.
            Instructions:
            1. Ask ONE clear, engaging question to begin the interview.
            2. Keep it under 3 sentences. Do not include greetings like "Hello" or introductory meta-text. Just state the question directly.
        """.trimIndent()

        try {
            val responseText = callAiApi(apiKey, prompt)
            if (responseText.isNotBlank()) responseText.trim() else getFallbackFirstQuestion(topic.title, difficulty)
        } catch (e: Exception) {
            getFallbackFirstQuestion(topic.title, difficulty)
        }
    }

    suspend fun generateFollowUpQuestion(
        apiKey: String,
        topicId: String,
        messages: List<InterviewMessage>
    ): String = withContext(Dispatchers.IO) {
        val topic = TopicData.getTopicById(topicId)
        val historyText = messages.joinToString("\n") {
            "${if (it.sender == MessageSender.INTERVIEWER) "Interviewer" else "Candidate"}: ${it.text}"
        }

        if (apiKey.isBlank()) {
            return@withContext getFallbackFollowUpQuestion(topic.title, messages)
        }

        val prompt = """
            You are an expert interviewer conducting a live placement interview on topic "${topic.title}".
            Below is the transcript of the interview so far:
            $historyText

            Instructions:
            1. Analyze the candidate's latest response.
            2. Ask a natural follow-up question or a new relevant question based on their answer to assess depth of knowledge or real-world application.
            3. Keep the question concise (2-3 sentences max).
            4. Do not output meta explanations.
        """.trimIndent()

        try {
            val responseText = callAiApi(apiKey, prompt)
            if (responseText.isNotBlank()) responseText.trim() else getFallbackFollowUpQuestion(topic.title, messages)
        } catch (e: Exception) {
            getFallbackFollowUpQuestion(topic.title, messages)
        }
    }

    suspend fun generateFeedbackReport(
        apiKey: String,
        topicId: String,
        messages: List<InterviewMessage>
    ): FeedbackResult = withContext(Dispatchers.IO) {
        val topic = TopicData.getTopicById(topicId)
        if (apiKey.isBlank()) {
            return@withContext getFallbackFeedbackResult(topic.title, messages)
        }

        val historyText = messages.joinToString("\n") {
            "${if (it.sender == MessageSender.INTERVIEWER) "Interviewer" else "Candidate"}: ${it.text}"
        }

        val prompt = """
            You are a strict, professional AI Campus Placement Interview Evaluator.
            Analyze and evaluate this interview transcript for topic "${topic.title}":
            $historyText

            STRICT SCORING RULES (0-100 RANGE):
            1. 0-10: Incorrect or Irrelevant
               - Pure greetings (e.g. Hi, Hello, Welcome).
               - Empty or missing answers.
               - Completely unrelated responses.
               - Technically incorrect answers or misconceptions.
               - Buzzwords with no meaningful explanation.
               - Award 0 for an empty answer or a greeting whenever appropriate. NEVER mark greetings or unrelated text as correct!
            2. 11-50: Partially Correct
               - Demonstrates some genuine understanding.
               - Mentions relevant concepts but misses important details or contains notable inaccuracies.
            3. 51-100: Correct & Sufficiently Relevant
               - Accurately addresses the question.
               - Demonstrates meaningful technical understanding.
               - Reserve scores near 100 for answers that are accurate, comprehensive, and appropriate to the question.
            4. Do not award a high score merely because the answer is long or contains technical keywords.

            EXPLANATION REQUIREMENT:
            For every question, explain what is correct or incorrect, state missing concepts or factual mistakes, and suggest improvements.

            OUTPUT FORMAT (VALID JSON ONLY, NO MARKDOWN, NO CODE FENCES):
            {
              "overallScore": 82,
              "technicalScore": 80,
              "communicationScore": 85,
              "confidenceScore": 80,
              "answerQualityScore": 83,
              "strengths": ["Clear explanation of core concepts", "Good communication structure"],
              "weakAreas": ["Needs more real-world examples", "Deep technical details could be improved"],
              "suggestions": ["Practice explaining edge cases", "Use STAR method for HR responses"],
              "questionEvaluations": [
                {
                  "questionNumber": 1,
                  "questionText": "Question text here",
                  "candidateAnswer": "Answer text here",
                  "score": 80,
                  "status": "Correct",
                  "feedback": "Status: Correct. Accurate explanation. Missing concepts: None. Suggested improvements: Add trade-off analysis.",
                  "idealAnswerSample": "Sample comprehensive answer",
                  "scoreOutOfTen": 8
                }
              ]
            }
        """.trimIndent()

        try {
            val rawResponse = callAiApi(apiKey, prompt)
            val jsonString = cleanJsonString(rawResponse)
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject

            val qEvals = mutableListOf<QuestionEvaluation>()
            if (jsonObject.has("questionEvaluations")) {
                val array = jsonObject.getAsJsonArray("questionEvaluations")
                array.forEachIndexed { index, element ->
                    val obj = element.asJsonObject
                    val rawScore = when {
                        obj.has("score") && !obj.get("score").isJsonNull -> obj.get("score").asInt
                        obj.has("scoreOutOfTen") && !obj.get("scoreOutOfTen").isJsonNull -> obj.get("scoreOutOfTen").asInt * 10
                        else -> 0
                    }.coerceIn(0, 100)

                    val statusStr = when {
                        obj.has("status") && !obj.get("status").isJsonNull -> obj.get("status").asString
                        rawScore >= 51 -> "Correct"
                        rawScore >= 11 -> "Partially Correct"
                        else -> "Incorrect"
                    }

                    qEvals.add(
                        QuestionEvaluation(
                            questionNumber = obj.get("questionNumber")?.asInt ?: (index + 1),
                            questionText = obj.get("questionText")?.asString ?: "Question ${index + 1}",
                            candidateAnswer = obj.get("candidateAnswer")?.asString ?: "",
                            feedback = obj.get("feedback")?.asString ?: "Evaluation completed.",
                            idealAnswerSample = obj.get("idealAnswerSample")?.asString ?: "Ensure complete coverage of fundamental principles.",
                            scoreOutOfTen = (rawScore / 10).coerceIn(0, 10),
                            score = rawScore,
                            status = statusStr
                        )
                    )
                }
            }

            val parsedResult = FeedbackResult(
                overallScore = jsonObject.get("overallScore")?.asInt?.coerceIn(0, 100) ?: 80,
                technicalScore = jsonObject.get("technicalScore")?.asInt?.coerceIn(0, 100) ?: 78,
                communicationScore = jsonObject.get("communicationScore")?.asInt?.coerceIn(0, 100) ?: 82,
                confidenceScore = jsonObject.get("confidenceScore")?.asInt?.coerceIn(0, 100) ?: 80,
                answerQualityScore = jsonObject.get("answerQualityScore")?.asInt?.coerceIn(0, 100) ?: 80,
                strengths = parseStringList(jsonObject, "strengths"),
                weakAreas = parseStringList(jsonObject, "weakAreas"),
                suggestions = parseStringList(jsonObject, "suggestions"),
                questionEvaluations = if (qEvals.isNotEmpty()) qEvals else getFallbackEvaluations(messages)
            )

            // Sanitize and validate external AI output in Kotlin
            AnswerEvaluator.sanitizeFeedbackResult(parsedResult, topic.title, messages)
        } catch (e: Exception) {
            getFallbackFeedbackResult(topic.title, messages)
        }
    }

    private fun callAiApi(apiKey: String, prompt: String): String {
        return if (apiKey.startsWith("sk-or-", ignoreCase = true) || apiKey.startsWith("sk-", ignoreCase = true)) {
            callOpenRouterApi(apiKey, prompt)
        } else {
            callGeminiApi(apiKey, prompt)
        }
    }

    private fun callOpenRouterApi(apiKey: String, prompt: String): String {
        val url = "https://openrouter.ai/api/v1/chat/completions"

        val jsonBody = JsonObject().apply {
            addProperty("model", "openai/gpt-4o-mini")
            val messages = com.google.gson.JsonArray().apply {
                val userMsg = JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", prompt)
                }
                add(userMsg)
            }
            add("messages", messages)
            addProperty("max_tokens", 1000)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://github.com/TheOrionGD/LOCK-SECURE")
            .addHeader("X-Title", "LOCK-SECURE")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("OpenRouter API request failed: ${response.code} ${response.message}")
        }

        val responseBody = response.body?.string() ?: ""
        val json = JsonParser.parseString(responseBody).asJsonObject
        val choices = json.getAsJsonArray("choices")
        if (choices != null && choices.size() > 0) {
            val messageObj = choices[0].asJsonObject.getAsJsonObject("message")
            if (messageObj != null && messageObj.has("content")) {
                return messageObj.get("content").asString
            }
        }
        return ""
    }

    private fun callGeminiApi(apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val jsonBody = JsonObject().apply {
            val contents = com.google.gson.JsonArray().apply {
                val contentObj = JsonObject().apply {
                    val parts = com.google.gson.JsonArray().apply {
                        val partObj = JsonObject().apply {
                            addProperty("text", prompt)
                        }
                        add(partObj)
                    }
                    add("parts", parts)
                }
                add(contentObj)
            }
            add("contents", contents)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("API Request failed: ${response.code} ${response.message}")
        }

        val responseBody = response.body?.string() ?: ""
        val json = JsonParser.parseString(responseBody).asJsonObject
        val candidates = json.getAsJsonArray("candidates")
        if (candidates != null && candidates.size() > 0) {
            val content = candidates[0].asJsonObject.getAsJsonObject("content")
            val parts = content.getAsJsonArray("parts")
            if (parts != null && parts.size() > 0) {
                return parts[0].asJsonObject.get("text").asString
            }
        }
        return ""
    }

    private fun cleanJsonString(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        return cleaned.trim()
    }

    private fun parseStringList(obj: JsonObject, key: String): List<String> {
        val list = mutableListOf<String>()
        if (obj.has(key)) {
            val arr = obj.getAsJsonArray(key)
            arr?.forEach { list.add(it.asString) }
        }
        return if (list.isNotEmpty()) list else listOf("Good performance", "Active participation")
    }

    // --- Intelligent Fallback Engine for offline / missing key testing ---
    private fun getFallbackFirstQuestion(topicTitle: String, difficulty: InterviewDifficulty): String {
        return when (topicTitle) {
            "Java Programming" -> "Welcome! Let's start with Java fundamentals. Can you explain the core concept of Object-Oriented Programming (OOP) and how polymorphism is implemented in Java?"
            "Database Management System (DBMS)" -> "Hello! To begin our DBMS discussion, could you explain the difference between a Primary Key and a Unique Key, and when you would use each?"
            "Operating Systems (OS)" -> "Welcome! Let's talk about OS concepts. What is the fundamental difference between a Process and a Thread, and how does context switching work?"
            "Computer Networks (CN)" -> "Hello! Let's start with networking. Can you explain what happens step-by-step when a user types 'www.google.com' in their web browser?"
            "Python Programming" -> "Welcome! To start with Python, can you explain the difference between mutable and immutable data types with examples?"
            "Self Introduction" -> "Hello! Welcome to your interview. Please give a brief self-introduction detailing your background, major projects, and key technical skills."
            "HR Questions" -> "Welcome! Let's start with your career motivations. Why are you interested in joining our organization and what makes you a good fit?"
            "Leadership & Initiative" -> "Hello! Can you share a specific situation where you took the lead in a college project or extracurricular activity?"
            "Teamwork & Collaboration" -> "Welcome! How do you handle a scenario where team members have conflicting opinions on how to complete a project?"
            "Communication Skills" -> "Hello! How do you ensure your technical ideas are clearly understood when presenting to non-technical stakeholders?"
            else -> "Welcome to the $topicTitle interview (${difficulty.displayName}). Could you briefly explain your understanding of this area and your practical experience with it?"
        }
    }

    private fun getFallbackFollowUpQuestion(topicTitle: String, messages: List<InterviewMessage>): String {
        val qCount = messages.count { it.sender == MessageSender.INTERVIEWER }
        val lastAnswer = messages.lastOrNull { it.sender == MessageSender.CANDIDATE }?.text ?: ""

        val followUps = when (topicTitle) {
            "Java Programming" -> listOf(
                "That was a good explanation. Building on that, how does Exception Handling work in Java, and what is the difference between Checked and Unchecked exceptions?",
                "Great! Can you explain the difference between String, StringBuilder, and StringBuffer in terms of performance and thread safety?",
                "Excellent. Lastly, what are Java Collections and how does HashMap handle collisions internally?"
            )
            "Database Management System (DBMS)" -> listOf(
                "Good point. Now explain ACID properties in transactions and why isolation levels are critical.",
                "How does Database Normalization help reduce redundancy? Briefly explain 1NF to 3NF.",
                "Can you write/describe a SQL query using JOIN and GROUP BY to calculate aggregate statistics?"
            )
            "Operating Systems (OS)" -> listOf(
                "Understood. What is a Deadlock, and what are the four necessary conditions for a deadlock to occur?",
                "Explain the concept of Virtual Memory and Paging. How does a page fault get handled?",
                "What is CPU scheduling? Compare First-Come First-Served (FCFS) with Round Robin scheduling."
            )
            "Computer Networks (CN)" -> listOf(
                "Good explanation. What is the difference between TCP and UDP protocols, and when should you choose UDP over TCP?",
                "Explain the 7 layers of the OSI model and highlight the responsibilities of the Transport Layer.",
                "What is HTTPS and how does SSL/TLS encryption secure web traffic?"
            )
            "Python Programming" -> listOf(
                "Good. How do decorators work in Python? Could you describe how you would write a custom logging decorator?",
                "What is the Global Interpreter Lock (GIL) in Python and how does it affect multi-threaded programs?",
                "How do generators and the 'yield' keyword differ from standard functions returning lists?"
            )
            "Self Introduction" -> listOf(
                "Thanks for sharing! What was the most challenging technical project you built, and what was your specific contribution?",
                "What are your top technical strengths and what area are you currently working to improve?",
                "How do your campus projects prepare you for real-world software development in an enterprise?"
            )
            "HR Questions" -> listOf(
                "That sounds reasonable. Where do you see yourself professionally in the next 3 to 5 years?",
                "Describe a situation where you failed or made a mistake. What did you learn from it?",
                "Why should our hiring team choose you over other candidates with similar qualifications?"
            )
            "Leadership & Initiative" -> listOf(
                "Great story! How did you motivate team members who were lagging behind on deliverables?",
                "Tell me about a difficult decision you had to make under tight deadlines during that project.",
                "How do you prioritize multiple urgent tasks when leading an event or project?"
            )
            "Teamwork & Collaboration" -> listOf(
                "Very good. How do you give constructive feedback to a peer without damaging your working relationship?",
                "Describe a project where you had to adapt to unexpected team changes or requirements.",
                "What role do you usually play in a team setting: driver, planner, executor, or mediator?"
            )
            "Communication Skills" -> listOf(
                "How would you explain an API or database query to a business client who has zero programming knowledge?",
                "Tell me about a miscommunication that happened during a group assignment and how you fixed it.",
                "How do you stay active and engaged during group discussions or placement interview rounds?"
            )
            else -> listOf(
                "Thanks for that response. Can you elaborate further with a practical real-world example?",
                "How would you apply this concept under constrained or high-load conditions?",
                "That's insightful. What are the common pitfalls or mistakes developers make in this area?"
            )
        }

        val index = (qCount - 1).coerceIn(0, followUps.size - 1)
        val baseQuestion = followUps[index]

        if (lastAnswer.length < 20) {
            return "Your previous response was quite concise. Could you elaborate a bit more on that before we move to: $baseQuestion"
        }
        return baseQuestion
    }

    private fun getFallbackFeedbackResult(topicTitle: String, messages: List<InterviewMessage>): FeedbackResult {
        return AnswerEvaluator.evaluateInterview(topicTitle, messages)
    }

    private fun getFallbackEvaluations(messages: List<InterviewMessage>): List<QuestionEvaluation> {
        return AnswerEvaluator.evaluateInterview("", messages).questionEvaluations
    }
}
