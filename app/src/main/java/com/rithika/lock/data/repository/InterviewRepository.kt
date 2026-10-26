package com.rithika.lock.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.rithika.lock.data.model.InterviewCategory
import com.rithika.lock.data.model.InterviewSession
import com.rithika.lock.data.model.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class InterviewRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ai_placement_coach_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USER_PROFILE = "key_user_profile"
        private const val KEY_INTERVIEW_SESSIONS = "key_interview_sessions"
        private const val KEY_API_KEY = "key_api_key"
        private const val KEY_REGISTERED_USERS = "key_registered_users"
    }

    data class UserAccount(
        val name: String,
        val email: String,
        val college: String,
        val department: String,
        val password: String,
        val apiKey: String
    )

    fun registerUser(account: UserAccount): Boolean {
        val users = getAllRegisteredUsers().toMutableMap()
        if (users.containsKey(account.email)) {
            return false // Account already exists
        }
        users[account.email] = account
        saveRegisteredUsers(users)
        saveUserProfile(UserProfile(name = account.name, email = account.email, college = account.college, department = account.department, apiKey = account.apiKey))
        return true
    }

    fun loginUser(email: String, password: String): Boolean {
        val users = getAllRegisteredUsers()
        val account = users[email] ?: return false
        if (account.password == password) {
            saveUserProfile(UserProfile(name = account.name, email = account.email, college = account.college, department = account.department, apiKey = account.apiKey))
            return true
        }
        return false
    }

    private fun getAllRegisteredUsers(): Map<String, UserAccount> {
        val json = prefs.getString(KEY_REGISTERED_USERS, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, UserAccount>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveRegisteredUsers(users: Map<String, UserAccount>) {
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_REGISTERED_USERS, json).apply()
    }

    fun getUserProfile(): UserProfile {
        val json = prefs.getString(KEY_USER_PROFILE, null) ?: return UserProfile()
        return try {
            gson.fromJson(json, UserProfile::class.java) ?: UserProfile()
        } catch (e: Exception) {
            UserProfile()
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString(KEY_USER_PROFILE, json).apply()
        saveApiKey(profile.apiKey)
    }

    fun getApiKey(): String {
        val key = prefs.getString(KEY_API_KEY, null)
        if (key.isNullOrBlank()) {
            val defaultKey = com.rithika.lock.data.api.Secrets.DEFAULT_API_KEY
            if (defaultKey.isNotBlank() && defaultKey != "YOUR_API_KEY_HERE") {
                saveApiKey(defaultKey)
                return defaultKey
            }
            return defaultKey
        }
        return key
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
        val currentProfile = getUserProfile()
        if (currentProfile.apiKey != apiKey) {
            val updated = currentProfile.copy(apiKey = apiKey)
            prefs.edit().putString(KEY_USER_PROFILE, gson.toJson(updated)).apply()
        }
    }

    fun getAllSessions(): List<InterviewSession> {
        val profile = getUserProfile()
        val userKey = if (profile.email.isNotBlank()) profile.email else (profile.name.ifBlank { "default_user" })
        val prefKey = "${KEY_INTERVIEW_SESSIONS}_$userKey"
        val json = prefs.getString(prefKey, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<InterviewSession>>() {}.type
            val list: List<InterviewSession>? = gson.fromJson(json, type)
            list ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSession(session: InterviewSession) {
        val profile = getUserProfile()
        val userKey = if (profile.email.isNotBlank()) profile.email else (profile.name.ifBlank { "default_user" })
        val prefKey = "${KEY_INTERVIEW_SESSIONS}_$userKey"

        val sessions = getAllSessions().toMutableList()
        val index = sessions.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            sessions[index] = session
        } else {
            sessions.add(0, session)
        }
        val json = gson.toJson(sessions)
        prefs.edit().putString(prefKey, json).apply()
    }

    fun deleteSession(sessionId: String) {
        val profile = getUserProfile()
        val userKey = if (profile.email.isNotBlank()) profile.email else (profile.name.ifBlank { "default_user" })
        val prefKey = "${KEY_INTERVIEW_SESSIONS}_$userKey"

        val sessions = getAllSessions().filter { it.id != sessionId }
        val json = gson.toJson(sessions)
        prefs.edit().putString(prefKey, json).apply()
    }

    fun clearAllHistory() {
        val profile = getUserProfile()
        val userKey = if (profile.email.isNotBlank()) profile.email else (profile.name.ifBlank { "default_user" })
        val prefKey = "${KEY_INTERVIEW_SESSIONS}_$userKey"
        prefs.edit().remove(prefKey).apply()
    }

    fun getAverageOverallScore(): Int {
        val sessions = getAllSessions().filter { it.isCompleted && it.feedback != null }
        if (sessions.isEmpty()) return 0
        return sessions.mapNotNull { it.feedback?.overallScore }.average().toInt()
    }

    fun getCategoryAverageScore(category: InterviewCategory): Int {
        val sessions = getAllSessions().filter { it.category == category && it.isCompleted && it.feedback != null }
        if (sessions.isEmpty()) return 0
        return sessions.mapNotNull { it.feedback?.overallScore }.average().toInt()
    }

    fun getTotalCompletedInterviews(): Int {
        return getAllSessions().count { it.isCompleted }
    }
}
