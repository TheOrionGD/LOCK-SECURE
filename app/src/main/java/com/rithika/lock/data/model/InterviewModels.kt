package com.rithika.lock.data.model

import java.util.UUID

enum class InterviewCategory(val displayName: String, val description: String) {
    TECHNICAL("Technical Interview", "Java, DBMS, OS, Computer Networks & Python"),
    NON_TECHNICAL("Non-Technical / HR", "Self Introduction, HR, Leadership & Communication")
}

enum class InterviewDifficulty(val displayName: String) {
    FRESHER("Fresher / Entry Level"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced / Senior Level")
}

data class Topic(
    val id: String,
    val title: String,
    val description: String,
    val category: InterviewCategory,
    val iconName: String,
    val sampleQuestions: List<String>
)

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val college: String = "",
    val department: String = "",
    val apiKey: String = ""
)

enum class MessageSender {
    INTERVIEWER,
    CANDIDATE
}

data class InterviewMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuestionEvaluation(
    val questionNumber: Int,
    val questionText: String,
    val candidateAnswer: String,
    val feedback: String,
    val idealAnswerSample: String,
    val scoreOutOfTen: Int
)

data class FeedbackResult(
    val overallScore: Int, // 0 - 100
    val technicalScore: Int, // 0 - 100
    val communicationScore: Int, // 0 - 100
    val confidenceScore: Int, // 0 - 100
    val answerQualityScore: Int, // 0 - 100
    val strengths: List<String>,
    val weakAreas: List<String>,
    val suggestions: List<String>,
    val questionEvaluations: List<QuestionEvaluation>
)

data class InterviewSession(
    val id: String = UUID.randomUUID().toString(),
    val topicId: String,
    val topicTitle: String,
    val category: InterviewCategory,
    val difficulty: InterviewDifficulty,
    val startTimestamp: Long = System.currentTimeMillis(),
    val messages: List<InterviewMessage> = emptyList(),
    val isCompleted: Boolean = false,
    val feedback: FeedbackResult? = null
)
