package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import com.example.ui.screens.ImageOcrProcessor
import com.example.ui.screens.OcrResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class DefaultQuotaExceededException(
    message: String = "Default API quota முடிந்தது, App இல் API key store செய்யப்படவில்லை எனவே key யை ADD செய்யவும்"
) : Exception(message)

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
                IllegalStateException("Gemini API Key அமைக்கப்படவில்லை.")
            )
        }

        // Reliable 3.x models for conversation
        val models = listOf("gemini-3-flash-preview", "gemini-3.1-flash-lite")
        for (model in models) {
            val attempt = callGeminiApi(model, apiKey, conversation, customPrompt)
            if (attempt.isSuccess) {
                return@withContext attempt
            }
        }

        return@withContext Result.failure(Exception("AI ஆசிரியர் பதில் அளிக்க முடியவில்லை. தயவுசெய்து மீண்டும் முயற்சிக்கவும்."))
    }

    private fun isQuotaError(msg: String): Boolean {
        val lower = msg.lowercase()
        return lower.contains("429") ||
                lower.contains("quota") ||
                lower.contains("resource_exhausted") ||
                lower.contains("rate limit") ||
                lower.contains("too many requests")
    }

    /**
     * Direct Multimodal Vision API call for Textbook / Worksheet Analysis:
     * - Uses dual key failover: Key 1 -> Key 2 (Rollback Key) -> Key 1
     * - Specifically enabled ONLY for Image-to-Text OCR feature
     * - Uses models: gemini-3-flash-preview, gemini-3.1-flash-lite
     */
    suspend fun analyzeBookImageDirect(
        bitmap: Bitmap
    ): Result<OcrResult> = withContext(Dispatchers.IO) {
        val userKey = apiKeyManager.getUserCustomKey().trim()
        val key1 = if (userKey.isNotBlank()) userKey else apiKeyManager.getDefaultApiKey().trim()
        val key2 = apiKeyManager.getRollbackApiKey().trim()

        val models = listOf("gemini-3-flash-preview", "gemini-3.1-flash-lite")

        // 1. Try Key 1 (Primary Key)
        var key1QuotaExhausted = false
        var lastErrorMsg = ""

        if (key1.isNotBlank()) {
            for (model in models) {
                val attempt = callGeminiVisionApiDirect(model, key1, bitmap)
                if (attempt.isSuccess) {
                    return@withContext attempt
                }
                val errMsg = attempt.exceptionOrNull()?.message.orEmpty()
                lastErrorMsg = errMsg
                if (isQuotaError(errMsg)) {
                    key1QuotaExhausted = true
                    break
                }
            }
        }

        // 2. If Key 1 quota exhausted (or Key 1 empty), switch to Key 2 (Rollback Key)
        var key2QuotaExhausted = false
        if (key2.isNotBlank() && key2 != key1) {
            for (model in models) {
                val attempt = callGeminiVisionApiDirect(model, key2, bitmap)
                if (attempt.isSuccess) {
                    return@withContext attempt
                }
                val errMsg = attempt.exceptionOrNull()?.message.orEmpty()
                lastErrorMsg = errMsg
                if (isQuotaError(errMsg)) {
                    key2QuotaExhausted = true
                    break
                }
            }
        }

        // 3. If Key 2 quota also exhausted, try Key 1 again (Round-robin Retry)
        if (key2QuotaExhausted && key1.isNotBlank()) {
            for (model in models) {
                val attempt = callGeminiVisionApiDirect(model, key1, bitmap)
                if (attempt.isSuccess) {
                    return@withContext attempt
                }
                lastErrorMsg = attempt.exceptionOrNull()?.message.orEmpty()
            }
        }

        if (lastErrorMsg.isNotBlank()) {
            if (isQuotaError(lastErrorMsg)) {
                return@withContext Result.failure(
                    DefaultQuotaExceededException("API Quota வரம்பு முடிந்தது. சிறிது நேரம் கழித்து மீண்டும் முயற்சிக்கவும் அல்லது புதிய Key சேர்க்கவும்.")
                )
            }
            return@withContext Result.failure(Exception("AI பிழை: $lastErrorMsg"))
        }

        return@withContext Result.failure(Exception("AI இணைப்பு தோல்வியடைந்தது. API Key-ஐ சரிபார்க்கவும்."))
    }

    /**
     * Backward-compatible overload for analyzeBookImage
     */
    suspend fun analyzeBookImage(
        bitmap: Bitmap,
        ocrExtractedText: String = ""
    ): Result<OcrResult> {
        return analyzeBookImageDirect(bitmap)
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
            val systemInstruction = JSONObject()
            val systemParts = JSONArray().apply {
                put(JSONObject().put("text", "You are 'Subiksha Teacher' (சுபிக்ஷா ஆசிரியர்), a friendly, encouraging, expert Tamil & English teacher. You teach Tamil, English, Maths, Science, and life skills. Respond in friendly Tamil with clear English explanations where helpful."))
            }
            systemInstruction.put("parts", systemParts)
            rootJson.put("system_instruction", systemInstruction)

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

            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 1200)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("X-goog-api-key", apiKey)
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
                    "HTTP Error ${response.code}: $responseBody"
                }
                return Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return Result.failure(Exception("பதில் கிடைக்கவில்லை."))
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



    private fun callGeminiVisionApiDirect(
        modelName: String,
        apiKey: String,
        bitmap: Bitmap
    ): Result<OcrResult> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val maxDimension = 1600
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(bitmap, targetW.coerceAtLeast(100), targetH.coerceAtLeast(100), true)
            } else {
                bitmap
            }

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val imageBytes = baos.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val rootJson = JSONObject()

            val systemInstruction = JSONObject()
            val systemParts = JSONArray().apply {
                put(JSONObject().put("text", """
                    You are 'Subiksha Teacher' (சுபிக்ஷா ஆசிரியர்), an expert school teacher assisting Tamil-medium students.
                    You analyze school textbook pages, worksheets, exam questions, exercises, and comprehension passages.
                    
                    CRITICAL TASK RULES FOR WORKSHEET & TEXTBOOK IMAGE ANALYSIS:
                    1. Read the uploaded image directly with extreme precision.
                    2. TRANSCRIBE ALL TEXT ACCURATELY: Extract the complete English text exactly as printed on the worksheet/book in its natural reading order (top to bottom, sections 1, 2, 3..).
                    3. PRESERVE NUMBERING & CHOICES: Strictly keep question numbers (1, 2, 3..), sub-questions / multiple choice options ((a), (b), (c), (d)..), fill-in-the-blanks, matching columns, and headings intact and properly attached to their corresponding sentences. Never separate choice letters from their text.
                    4. SPELLING & CLARITY: Correct any printing blurriness or layout distortion. Ensure correct English spelling.
                    5. EXPLANATIONS & SOLUTIONS: In 'paragraphsExplanation', provide step-by-step solutions/answers with friendly, clear Tamil explanations for each question shown.
                    6. KEY WORDS: Extract 6-12 important vocabulary words with accurate Tamil meanings and parts of speech.
                    7. Output MUST be ONLY valid JSON matching this exact schema:
                    {
                      "heading": "Worksheet / Lesson Title in English (e.g. Formative Assessment-IV)",
                      "extractedText": "Full, accurate, properly ordered English text of all questions and sections from the page",
                      "paragraphs": [
                        "Full text of Question / Section 1 with all its sub-parts (a, b, c, d...)",
                        "Full text of Question / Section 2 with all its sub-parts (a, b, c, d...)"
                      ],
                      "keyWords": [
                        {
                          "english": "Vocabulary Word",
                          "tamil": "சரியான தமிழாக்கம்",
                          "partOfSpeech": "Noun / Verb / Adjective"
                        }
                      ],
                      "explanation": {
                        "headingTamil": "பாடத் தலைப்பு (தமிழில்)",
                        "centralIdea": "பாடப்பகுதி அல்லது வினாத்தாளின் மையக் கருத்து (எளிய தமிழில்)",
                        "paragraphsExplanation": [
                          "1. முதல் வினா/பகுதிக்கான தெளிவான தீர்வு மற்றும் தமிழ்க் கருத்து விளக்கம் விடையுடன்",
                          "2. இரண்டாம் வினா/பகுதிக்கான தெளிவான தீர்வு மற்றும் தமிழ்க் கருத்து விளக்கம் விடையுடன்"
                        ],
                        "practicalApplication": "மாணவர்கள் தேர்வில் அல்லது நடைமுறையில் நினைவில் கொள்ள வேண்டிய முக்கியக் குறிப்பு",
                        "reflectionQuestion": "மாணவர்கள் சிந்திக்க ஒரு வினா"
                      },
                      "tamilTranslation": "முழு ஆங்கிலப் பாடப்பகுதி/வினாத்தாளின் இயல்பான, இலக்கணப் பிழையற்ற தூய தமிழாக்கம். தலைப்புகள், வினாக்கள், விருப்பங்கள் அனைத்தும் தமிழில் மொழிபெயர்க்கப்பட வேண்டும்."
                    }
                """.trimIndent()))
            }
            systemInstruction.put("parts", systemParts)
            rootJson.put("system_instruction", systemInstruction)

            val contentsArray = JSONArray()
            val userContent = JSONObject()
            userContent.put("role", "user")

            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", "Please read this school worksheet / textbook page image directly. Extract the entire text accurately in correct order without omitting any questions, options, or numbers, and provide the complete structured JSON response with solutions and Tamil explanation."))

            val inlineData = JSONObject()
            inlineData.put("mime_type", "image/jpeg")
            inlineData.put("data", base64Image)
            partsArray.put(JSONObject().put("inline_data", inlineData))

            userContent.put("parts", partsArray)
            contentsArray.put(userContent)
            rootJson.put("contents", contentsArray)

            val genConfig = JSONObject()
            genConfig.put("temperature", 0.1)
            genConfig.put("maxOutputTokens", 4096)
            genConfig.put("responseMimeType", "application/json")
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("X-goog-api-key", apiKey)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val err = errJson.optJSONObject("error")
                    err?.optString("message") ?: "HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val candidates = resJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return Result.failure(Exception("AI-லிருந்து பதில் கிடைக்கவில்லை."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                return Result.failure(Exception("AI பதில் காலியாக உள்ளது."))
            }

            val result = ImageOcrProcessor.parseGeminiJsonResponse(text, "")
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
