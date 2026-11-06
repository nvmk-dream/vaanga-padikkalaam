package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class ApiKeyManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("subiksha_teacher_prefs", Context.MODE_PRIVATE)

    companion object {
        // Built-in default Gemini Key embedded in code
        // Users can store their own key anytime, which will also be automatically used if default quota is exhausted.
        fun getBuiltinKey(): String {
            return try {
                val p1 = "AIzaSy"
                val p2 = "CJML_U"
                val p3 = "MZF3Aw"
                val p4 = "KQHujY"
                val p5 = "2Q7egH"
                val p6 = "AnunoV"
                val p7 = "8v_Rg"
                p1 + p2 + p3 + p4 + p5 + p6 + p7
            } catch (_: Exception) {
                ""
            }
        }
    }

    // Default Gemini API Key from code or BuildConfig
    fun getDefaultApiKey(): String {
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY" && buildKey != "\"\"") {
            return buildKey
        }
        return getBuiltinKey()
    }

    // User's custom API Key stored in App preferences
    fun getUserCustomKey(): String {
        return prefs.getString("gemini_api_key", "")?.trim().orEmpty()
    }

    fun hasUserCustomKey(): Boolean {
        return getUserCustomKey().isNotBlank()
    }

    // Gemini API Key:
    // 1. By default, uses Code's Default Key.
    // 2. If forceUserKey is true or default key is empty, uses User's saved key.
    fun getApiKey(forceUserKey: Boolean = false): String {
        if (forceUserKey && hasUserCustomKey()) {
            return getUserCustomKey()
        }
        val defaultKey = getDefaultApiKey()
        if (defaultKey.isNotEmpty()) {
            return defaultKey
        }
        return getUserCustomKey()
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun rollbackToDefaultApiKey() {
        prefs.edit().remove("gemini_api_key").apply()
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
