package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiService(private val apiKeyManager: ApiKeyManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        conversation: List<ChatMessage>,
        customPrompt: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API Key இன்னும் அமைக்கப்படவில்லை. மெனுவில் சென்று உங்கள் API Key-ஐ உள்ளிடவும்.")
            )
        }

        val primaryModel = "gemini-2.5-flash"
        val fallbackModel = "gemini-1.5-flash"

        // Try primary model first, fallback if not found
        val firstAttempt = callGeminiApi(primaryModel, apiKey, conversation, customPrompt)
        if (firstAttempt.isSuccess) {
            return@withContext firstAttempt
        }

        // If 404 or model not found, try fallback
        val secondAttempt = callGeminiApi(fallbackModel, apiKey, conversation, customPrompt)
        if (secondAttempt.isSuccess) {
            return@withContext secondAttempt
        }

        return@withContext firstAttempt
    }

    private fun callGeminiApi(
        modelName: String,
        apiKey: String,
        conversation: List<ChatMessage>,
        customPrompt: String?
    ): Result<String> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val rootJson = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val systemParts = JSONArray().apply {
                put(JSONObject().put("text", "You are 'Subiksha Teacher' (சுபிக்ஷா ஆசிரியர்), a friendly, encouraging, expert Tamil & English teacher. You teach Tamil, English, Maths, Science, and life skills. Respond in friendly Tamil with clear English explanations where helpful. Keep explanations clear, engaging, and motivating for students."))
            }
            systemInstruction.put("parts", systemParts)
            rootJson.put("system_instruction", systemInstruction)

            // Contents array
            val contentsArray = JSONArray()
            conversation.takeLast(10).forEach { msg ->
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.isUser) "user" else "model")
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", msg.text))
                }
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            if (customPrompt != null && conversation.none { it.text == customPrompt }) {
                val userContent = JSONObject()
                userContent.put("role", "user")
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", customPrompt))
                }
                userContent.put("parts", parts)
                contentsArray.put(userContent)
            }

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 1200)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val err = errJson.optJSONObject("error")
                    err?.optString("message") ?: "API Error HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP Error ${response.code}: ${response.message}"
                }
                return Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return Result.failure(Exception("பதில் கிடைக்கவில்லை. தயவுசெய்து மீண்டும் முயற்சிக்கவும்."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                Result.failure(Exception("பதில் காலியாக உள்ளது."))
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
