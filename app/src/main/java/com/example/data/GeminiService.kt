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

        // Standard official Gemini models
        val primaryModel = "gemini-1.5-flash"
        val fallbackModel = "gemini-2.0-flash"

        val firstAttempt = callGeminiApi(primaryModel, apiKey, conversation, customPrompt)
        if (firstAttempt.isSuccess) {
            return@withContext firstAttempt
        }

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

    /**
     * Complete Vision + OCR analysis function for ImageToText:
     * - Uses official gemini-1.5-flash and gemini-2.0-flash models
     * - Accurately passes image and OCR text
     * - Preserves question numbering, MCQ choices, headings, and paragraphs
     * - Returns explicit Result.failure if API fails so UI can inform the user
     */
    suspend fun analyzeBookImage(
        bitmap: Bitmap,
        ocrExtractedText: String
    ): Result<OcrResult> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyManager.getApiKey().trim()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API Key அமைக்கப்படவில்லை. திரையின் மேலே உள்ள 'API Key' பட்டனைத் தொட்டு உங்கள் Google AI Studio Key-ஐ உள்ளிடவும்.")
            )
        }

        // Try primary stable model gemini-1.5-flash, fallback to gemini-2.0-flash
        val primaryModel = "gemini-1.5-flash"
        val fallbackModel = "gemini-2.0-flash"

        val firstAttempt = callGeminiVisionApi(primaryModel, apiKey, bitmap, ocrExtractedText)
        if (firstAttempt.isSuccess) {
            return@withContext firstAttempt
        }

        val secondAttempt = callGeminiVisionApi(fallbackModel, apiKey, bitmap, ocrExtractedText)
        if (secondAttempt.isSuccess) {
            return@withContext secondAttempt
        }

        // Return the actual failure reason (e.g. Invalid API Key, Quota Exceeded)
        return@withContext firstAttempt
    }

    private fun callGeminiVisionApi(
        modelName: String,
        apiKey: String,
        bitmap: Bitmap,
        ocrExtractedText: String
    ): Result<OcrResult> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            // Resize large bitmaps to save bandwidth & stay well within API limits
            val maxDimension = 1280
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(bitmap, targetW.coerceAtLeast(100), targetH.coerceAtLeast(100), true)
            } else {
                bitmap
            }

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val imageBytes = baos.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val rootJson = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val systemParts = JSONArray().apply {
                put(JSONObject().put("text", """
                    You are 'Subiksha Teacher' (சுபிக்ஷா ஆசிரியர்), an expert school teacher assisting Tamil-medium students.
                    You analyze school textbook pages, exercises, lessons, questions, and comprehension passages.
                    
                    TASK RULES:
                    1. Read both the uploaded textbook image and the OCR text.
                    2. Correct any OCR mistakes by checking the image carefully. Preserve question numbers (1, 2, 3..), MCQ choices (a, b, c, d..), headings, and paragraph sequence.
                    3. Do not invent missing facts.
                    4. Output MUST be ONLY valid JSON matching this exact schema:
                    {
                      "heading": "Lesson / Exercise Title in English",
                      "extractedText": "Full, accurate, corrected English text from the textbook page",
                      "paragraphs": ["Paragraph or Question 1", "Paragraph or Question 2"],
                      "keyWords": [
                        {
                          "english": "Vocabulary Word",
                          "tamil": "சரியான தமிழாக்கம்",
                          "partOfSpeech": "Noun / Verb / Adjective"
                        }
                      ],
                      "explanation": {
                        "headingTamil": "பாடத் தலைப்பு (தமிழில்)",
                        "centralIdea": "பாடத்தின் மையக்கருத்து (எளிய தமிழில்)",
                        "paragraphsExplanation": [
                          "1. முதல் பத்தி அல்லது கேள்விக்கான தனித்தனி விளக்கம்",
                          "2. அடுத்த பத்தி அல்லது கேள்விக்கான தனித்தனி விளக்கம்"
                        ],
                        "practicalApplication": "மாணவர்கள் நடைமுறை வாழ்வில் அல்லது தேர்வில் பயன்படுத்த வேண்டிய குறிப்பு",
                        "reflectionQuestion": "மாணவர்கள் சிந்திக்க ஒரு வினா"
                      },
                      "tamilTranslation": "முழு ஆங்கிலப் பாடப்பகுதியின் இயல்பான, இலக்கணப் பிழையற்ற தூய தமிழாக்கம். தலைப்பு, கேள்விகள், விருப்பங்கள் அனைத்தும் தமிழில் மொழிபெயர்க்கப்பட வேண்டும்."
                    }
                """.trimIndent()))
            }
            systemInstruction.put("parts", systemParts)
            rootJson.put("system_instruction", systemInstruction)

            val contentsArray = JSONArray()
            val userContent = JSONObject()
            userContent.put("role", "user")

            val partsArray = JSONArray()
            // 1. Text prompt part (Safely isolated)
            val promptText = if (ocrExtractedText.isNotBlank()) {
                "Please analyze this textbook page. The OCR preliminary text is:\n$ocrExtractedText\nProvide the complete structured JSON response."
            } else {
                "Please analyze this textbook page image and provide the complete structured JSON response."
            }
            partsArray.put(JSONObject().put("text", promptText))

            // 2. Image part
            val inlineData = JSONObject()
            inlineData.put("mime_type", "image/jpeg")
            inlineData.put("data", base64Image)
            partsArray.put(JSONObject().put("inline_data", inlineData))

            userContent.put("parts", partsArray)
            contentsArray.put(userContent)
            rootJson.put("contents", contentsArray)

            // Generation config with JSON response type
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.2)
            genConfig.put("maxOutputTokens", 3000)
            genConfig.put("responseMimeType", "application/json")
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
                    "HTTP Error ${response.code}: $responseBody"
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

            val result = ImageOcrProcessor.parseGeminiJsonResponse(text, ocrExtractedText)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
