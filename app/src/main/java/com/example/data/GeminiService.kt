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
     * - Supports single image or entire chapter/lesson with multiple page images or PDF pages
     * - Uses dual key failover: Key 1 -> Key 2 (Rollback Key) -> Key 1
     * - Specifically enabled ONLY for Image-to-Text OCR feature
     * - Uses models: gemini-3-flash-preview, gemini-3.1-flash-lite
     */
    suspend fun analyzeBookImagesDirect(
        bitmaps: List<Bitmap>
    ): Result<OcrResult> = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("பாடப் படங்கள் எதுவும் தேர்ந்தெடுக்கப்படவில்லை."))
        }

        val userKey = apiKeyManager.getUserCustomKey().trim()
        val key1 = if (userKey.isNotBlank()) userKey else apiKeyManager.getDefaultApiKey().trim()
        val key2 = apiKeyManager.getRollbackApiKey().trim()

        val models = listOf("gemini-3-flash-preview", "gemini-3.1-flash-lite")

        // 1. Try Key 1 (Primary Key)
        var key1QuotaExhausted = false
        var lastErrorMsg = ""

        if (key1.isNotBlank()) {
            for (model in models) {
                val attempt = callGeminiVisionApiDirect(model, key1, bitmaps)
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
                val attempt = callGeminiVisionApiDirect(model, key2, bitmaps)
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
                val attempt = callGeminiVisionApiDirect(model, key1, bitmaps)
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

    suspend fun analyzeBookImageDirect(
        bitmap: Bitmap
    ): Result<OcrResult> {
        return analyzeBookImagesDirect(listOf(bitmap))
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
        bitmaps: List<Bitmap>
    ): Result<OcrResult> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val maxDimension = 1500
            val rootJson = JSONObject()

            val systemInstruction = JSONObject()
            val systemParts = JSONArray().apply {
                put(JSONObject().put("text", """
                    You are 'Subiksha Teacher' (சுபிக்ஷா ஆசிரியர்), an expert school teacher assisting Tamil-medium and English-medium students.
                    You analyze school textbook pages, full lesson chapters, worksheets, mathematics problems, and question papers.
                    
                    CRITICAL TASK RULES FOR WORKSHEET & TEXTBOOK IMAGE ANALYSIS:
                    1. READ ALL ATTACHED PAGES DIRECTLY: When multiple pages of a lesson are provided, read them in natural sequence (Page 1, Page 2, Page 3...).
                    2. TRANSCRIBE ALL TEXT ACCURATELY: Extract the complete English text exactly as printed in the book or worksheet.
                    3. PRESERVE ALL NUMBERS, HEADINGS & SYMBOLS: Keep question numbers (1, 2, 3..), sub-parts (a, b, c, d..), True/False, matching, decimals (e.g. 50.04, 50.045), and mathematical expressions intact.
                    4. COMPREHENSIVE VOCABULARY: Extract ALL important words, technical terms, and grammar with accurate Tamil meanings and parts of speech without any artificial limits.
                    5. DETAILED CONCEPT EXPLANATION & TRANSLATION: Provide central idea, section-wise explanations, and full Tamil translation.
                    6. EXHAUSTIVE QUESTION & ANSWER ANALYSIS (Q/A):
                       Identify EVERY single question, exercise, and math problem found on the pages. For each question, output:
                       - "questionNumber": e.g. "Ex 2.2 - Q2(a)" or "Q4(b)"
                       - "questionType": "கணக்கு (Maths Problem)", "சரியா? தவறா? (True/False)", "பொருத்துக (Match)", "வினா-விடை (Q&A)", "HOTS சிந்தனை வினா", "இலக்கணம் (Grammar)"
                       - "questionEnglish": Full question in English
                       - "questionTamil": Full question translated to clear Tamil
                       - "answerEnglish": Exact, complete answer in English
                       - "answerTamil": Exact, complete answer in Tamil
                       - "stepByStepWorking": For mathematics problems or multi-step questions, provide an array of step-by-step working:
                         ["படி 1 / Step 1: ...", "படி 2 / Step 2: ...", "படி 3 / Step 3: ...", "இறுதி விடை / Final Answer: ..."]
                       - "conceptExplanation": Explain HOW and WHY this answer or method is used.
                       - "practicalApplication": State WHERE and WHY this concept/answer is used in real life! (e.g. for circle circumference: "வட்ட வடிவத் தோட்டத்திற்கு வேலி அமைக்கவும், சக்கரம் சுழலும் தூரம் கணக்கிடவும் பயன்படுகிறது"; for decimals: "கடைகளில் ரூபாய்-பைசா கணக்கிட, பொருட்கள் எடை, துல்லிய நீளம் அளக்கப் பயன்படுகிறது").
                    
                    7. Output MUST be ONLY valid JSON matching this schema:
                    {
                      "heading": "Lesson / Exercise Title in English",
                      "extractedText": "Complete transcribed English text",
                      "paragraphs": [
                        "Section / Question 1 text",
                        "Section / Question 2 text"
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
                        "centralIdea": "பாடத்தின் அல்லது கணக்குகளின் மையக் கருத்து",
                        "paragraphsExplanation": [
                          "விளக்கம் 1",
                          "விளக்கம் 2"
                        ],
                        "practicalApplication": "அன்றாட வாழ்வில் இதன் முக்கியப் பயன்பாடு",
                        "reflectionQuestion": "சிந்தனை வினா"
                      },
                      "tamilTranslation": "முழு பாடப்பகுதியின் தெளிவான தமிழாக்கம்",
                      "questionAnswers": [
                        {
                          "questionNumber": "1",
                          "questionType": "கணக்கு (Maths Problem) / சரியா தவறா / வினா-விடை",
                          "questionEnglish": "Question in English",
                          "questionTamil": "கேள்வி தமிழில்",
                          "answerEnglish": "Answer in English",
                          "answerTamil": "விடை தமிழில்",
                          "stepByStepWorking": [
                            "படி 1: ...",
                            "படி 2: ..."
                          ],
                          "conceptExplanation": "எப்படி செய்ய வேண்டும் என்ற எளிய விளக்கம்",
                          "practicalApplication": "இதை கண்டுபிடிப்பதின் நடைமுறைப் பயன்பாடு"
                        }
                      ]
                    }
                """.trimIndent()))
            }
            systemInstruction.put("parts", systemParts)
            rootJson.put("system_instruction", systemInstruction)

            val contentsArray = JSONArray()
            val userContent = JSONObject()
            userContent.put("role", "user")

            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", "Please analyze these school textbook pages / worksheet completely. Extract the entire text, vocabulary, detailed explanations, and ALL exercises with step-by-step math working, practical real-life applications, and answers in both English and Tamil. Return valid JSON."))

            // Add all page images as inline_data parts
            for (bitmap in bitmaps) {
                val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val targetW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                    val targetH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                    Bitmap.createScaledBitmap(bitmap, targetW.coerceAtLeast(100), targetH.coerceAtLeast(100), true)
                } else {
                    bitmap
                }

                val baos = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 88, baos)
                val imageBytes = baos.toByteArray()
                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                val inlineData = JSONObject()
                inlineData.put("mime_type", "image/jpeg")
                inlineData.put("data", base64Image)
                partsArray.put(JSONObject().put("inline_data", inlineData))
            }

            userContent.put("parts", partsArray)
            contentsArray.put(userContent)
            rootJson.put("contents", contentsArray)

            val genConfig = JSONObject()
            genConfig.put("temperature", 0.1)
            genConfig.put("maxOutputTokens", 8192)
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
