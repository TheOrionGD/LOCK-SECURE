package com.rithika.lock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rithika.lock.data.api.GeminiApiService
import com.rithika.lock.data.model.FeedbackResult
import com.rithika.lock.data.model.InterviewCategory
import com.rithika.lock.data.model.InterviewDifficulty
import com.rithika.lock.data.model.InterviewMessage
import com.rithika.lock.data.model.InterviewSession
import com.rithika.lock.data.model.MessageSender
import com.rithika.lock.data.model.TopicData
import com.rithika.lock.data.model.UserProfile
import com.rithika.lock.data.repository.InterviewRepository
import com.rithika.lock.data.service.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InterviewRepository(application)

    private val _userProfile = MutableStateFlow(repository.getUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _sessions = MutableStateFlow(repository.getAllSessions())
    val sessions: StateFlow<List<InterviewSession>> = _sessions.asStateFlow()

    private val _apiKey = MutableStateFlow(repository.getApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    fun updateProfile(name: String, email: String, college: String, department: String, apiKey: String) {
        val updated = UserProfile(
            name = name,
            email = email,
            college = college,
            department = department,
            apiKey = apiKey
        )
        repository.saveUserProfile(updated)
        _userProfile.value = updated
        _apiKey.value = apiKey
    }

    fun register(name: String, email: String, college: String, department: String, password: String, apiKey: String = ""): Boolean {
        if (email.isBlank() || password.isBlank() || name.isBlank()) return false
        val account = InterviewRepository.UserAccount(name, email, college, department, password, apiKey)
        val success = repository.registerUser(account)
        if (success) {
            _userProfile.value = repository.getUserProfile()
            _apiKey.value = repository.getApiKey()
        }
        return success
    }

    fun login(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) return false
        val success = repository.loginUser(email, password)
        if (success) {
            _userProfile.value = repository.getUserProfile()
            _apiKey.value = repository.getApiKey()
        }
        return success
    }

    fun refreshSessions() {
        _sessions.value = repository.getAllSessions()
    }

    fun deleteSession(sessionId: String) {
        repository.deleteSession(sessionId)
        refreshSessions()
    }

    fun clearAllHistory() {
        repository.clearAllHistory()
        refreshSessions()
    }

    fun getAverageScore(): Int = repository.getAverageOverallScore()
    fun getTotalCompleted(): Int = repository.getTotalCompletedInterviews()
    fun getCategoryAverage(category: InterviewCategory): Int = repository.getCategoryAverageScore(category)
}

class InterviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InterviewRepository(application)
    private val geminiApi = GeminiApiService()
    private val ttsManager = TextToSpeechManager(application)

    private val _currentSession = MutableStateFlow<InterviewSession?>(null)
    val currentSession: StateFlow<InterviewSession?> = _currentSession.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _isEvaluating = MutableStateFlow(false)
    val isEvaluating: StateFlow<Boolean> = _isEvaluating.asStateFlow()

    private val _feedbackResult = MutableStateFlow<FeedbackResult?>(null)
    val feedbackResult: StateFlow<FeedbackResult?> = _feedbackResult.asStateFlow()

    fun startNewSession(topicId: String, difficulty: InterviewDifficulty) {
        val topic = TopicData.getTopicById(topicId)
        val apiKey = repository.getApiKey()

        val newSession = InterviewSession(
            topicId = topic.id,
            topicTitle = topic.title,
            category = topic.category,
            difficulty = difficulty,
            startTimestamp = System.currentTimeMillis()
        )
        _currentSession.value = newSession
        _feedbackResult.value = null

        viewModelScope.launch {
            _isLoading.value = true
            val question = geminiApi.generateFirstQuestion(apiKey, topic.id, difficulty)
            _isLoading.value = false

            val initialMsg = InterviewMessage(sender = MessageSender.INTERVIEWER, text = question)
            val updatedMessages = listOf(initialMsg)
            _currentSession.value = newSession.copy(messages = updatedMessages)

            if (_isTtsEnabled.value) {
                ttsManager.speak(question)
            }
        }
    }

    fun submitAnswer(candidateAnswerText: String) {
        val session = _currentSession.value ?: return
        if (candidateAnswerText.isBlank() || _isLoading.value || _isEvaluating.value) return

        ttsManager.stop()

        val answerMsg = InterviewMessage(sender = MessageSender.CANDIDATE, text = candidateAnswerText)
        val updatedMessages = session.messages + answerMsg
        _currentSession.value = session.copy(messages = updatedMessages)

        val apiKey = repository.getApiKey()

        viewModelScope.launch {
            _isLoading.value = true
            val nextQuestion = geminiApi.generateFollowUpQuestion(apiKey, session.topicId, updatedMessages)
            _isLoading.value = false

            val questionMsg = InterviewMessage(sender = MessageSender.INTERVIEWER, text = nextQuestion)
            val finalMessages = updatedMessages + questionMsg
            val updatedSession = session.copy(messages = finalMessages)
            _currentSession.value = updatedSession

            if (_isTtsEnabled.value) {
                ttsManager.speak(nextQuestion)
            }
        }
    }

    fun toggleTts() {
        val newValue = !_isTtsEnabled.value
        _isTtsEnabled.value = newValue
        if (!newValue) {
            ttsManager.stop()
        }
    }

    fun readCurrentQuestion() {
        val session = _currentSession.value ?: return
        val lastInterviewerMsg = session.messages.lastOrNull { it.sender == MessageSender.INTERVIEWER }
        if (lastInterviewerMsg != null) {
            ttsManager.speak(lastInterviewerMsg.text)
        }
    }

    fun finishAndEvaluateInterview() {
        val session = _currentSession.value ?: return
        val apiKey = repository.getApiKey()

        viewModelScope.launch {
            _isEvaluating.value = true
            ttsManager.stop()

            val result = geminiApi.generateFeedbackReport(apiKey, session.topicId, session.messages)
            _feedbackResult.value = result

            val completedSession = session.copy(
                isCompleted = true,
                feedback = result
            )
            _currentSession.value = completedSession
            repository.saveSession(completedSession)

            _isEvaluating.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
