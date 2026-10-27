package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class ApiKeyManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("subiksha_teacher_prefs", Context.MODE_PRIVATE)

    // Gemini API Key
    fun getApiKey(): String {
        val userKey = prefs.getString("gemini_api_key", "")?.trim().orEmpty()
        if (userKey.isNotEmpty()) {
            return userKey
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove("gemini_api_key").apply()
    }

    // OpenAI API Key (ChatGPT)
    fun getOpenAiKey(): String {
        return prefs.getString("openai_api_key", "")?.trim().orEmpty()
    }

    fun saveOpenAiKey(key: String) {
        prefs.edit().putString("openai_api_key", key.trim()).apply()
    }

    fun clearOpenAiKey() {
        prefs.edit().remove("openai_api_key").apply()
    }

    // Voice Chat Preferences
    fun getDefaultProvider(): String {
        return prefs.getString("default_ai_provider", "chatgpt") ?: "chatgpt"
    }

    fun setDefaultProvider(provider: String) {
        prefs.edit().putString("default_ai_provider", provider.lowercase()).apply()
    }

    fun isVoiceReplyEnabled(): Boolean {
        return prefs.getBoolean("voice_reply_enabled", true)
    }

    fun setVoiceReplyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("voice_reply_enabled", enabled).apply()
    }

    fun getPreferredLanguage(): String {
        return prefs.getString("preferred_language", "ta") ?: "ta" // "ta", "en", "mixed"
    }

    fun setPreferredLanguage(lang: String) {
        prefs.edit().putString("preferred_language", lang).apply()
    }

    fun getVoiceSpeed(): String {
        return prefs.getString("voice_speed", "normal") ?: "normal" // "slow", "normal", "fast"
    }

    fun setVoiceSpeed(speed: String) {
        prefs.edit().putString("voice_speed", speed).apply()
    }

    fun isAutoPlayVoice(): Boolean {
        return prefs.getBoolean("auto_play_voice", true)
    }

    fun setAutoPlayVoice(autoPlay: Boolean) {
        prefs.edit().putBoolean("auto_play_voice", autoPlay).apply()
    }

    fun hasValidKey(): Boolean {
        return getApiKey().isNotEmpty()
    }

    fun isCustomKey(): Boolean {
        return !prefs.getString("gemini_api_key", "").isNullOrBlank()
    }
}
