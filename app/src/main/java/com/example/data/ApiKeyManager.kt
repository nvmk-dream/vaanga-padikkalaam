package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class ApiKeyManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("subiksha_teacher_prefs", Context.MODE_PRIVATE)

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

    fun hasValidKey(): Boolean {
        return getApiKey().isNotEmpty()
    }

    fun isCustomKey(): Boolean {
        return !prefs.getString("gemini_api_key", "").isNullOrBlank()
    }
}
