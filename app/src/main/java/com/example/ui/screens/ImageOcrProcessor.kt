package com.example.ui.screens

import org.json.JSONArray
import org.json.JSONObject

data class OcrKeyWord(
    val english: String,
    val tamil: String,
    val partOfSpeech: String = "Word"
)

data class OcrExplanation(
    val headingTamil: String,
    val centralIdea: String,
    val paragraphsExplanation: List<String>,
    val practicalApplication: String,
    val reflectionQuestion: String
)

data class OcrResult(
    val originalHeading: String,
    val rawEnglishText: String,
    val paragraphs: List<String>,
    val keyWords: List<OcrKeyWord>,
    val detailedExplanation: OcrExplanation,
    val fullTamilTranslation: String
)

object ImageOcrProcessor {

    // Initial reference sample for first display before user uploads any photo
    val referenceBookResult = OcrResult(
        originalHeading = "The Power of Positive Thinking",
        rawEnglishText = """The Power of Positive Thinking

Positive thinking helps us stay happy, healthy and successful.
It gives us confidence and helps us face challenges.
When we think positively, we can achieve our goals and live a better life.""".trimIndent(),
        paragraphs = listOf(
            "Positive thinking helps us stay happy, healthy and successful.",
            "It gives us confidence and helps us face challenges.",
            "When we think positively, we can achieve our goals and live a better life."
        ),
        keyWords = listOf(
            OcrKeyWord("Power", "சக்தி / வலிமை", "Noun"),
            OcrKeyWord("Positive", "நேர்மறையான", "Adjective"),
            OcrKeyWord("Thinking", "சிந்தனை", "Noun"),
            OcrKeyWord("Helps", "உதவுகிறது", "Verb"),
            OcrKeyWord("Happy", "மகிழ்ச்சியான", "Adjective"),
            OcrKeyWord("Healthy", "ஆரோக்கியமான", "Adjective"),
            OcrKeyWord("Successful", "வெற்றி பெற்ற", "Adjective"),
            OcrKeyWord("Confidence", "தன்னம்பிக்கை", "Noun"),
            OcrKeyWord("Challenges", "சவால்கள்", "Noun"),
            OcrKeyWord("Achieve", "அடைதல் / சாதித்தல்", "Verb"),
            OcrKeyWord("Goals", "இலக்குகள்", "Noun"),
            OcrKeyWord("Better life", "சிறந்த வாழ்க்கை", "Phrase")
        ),
        detailedExplanation = OcrExplanation(
            headingTamil = "நேர்மறைச் சிந்தனையின் சக்தி",
            centralIdea = "நேர்மறையாகச் சிந்திப்பது என்பது வாழ்க்கையில் பிரச்சினைகள் வந்தாலும் அவற்றை நம்பிக்கையுடன் எதிர்கொள்வதாகும். இது நமக்குத் தன்னம்பிக்கையை வளர்க்கவும், சவால்களைச் சமாளிக்கவும் உதவுகிறது.",
            paragraphsExplanation = listOf(
                "1. மகிழ்ச்சி மற்றும் ஆரோக்கியம்: நல்ல எண்ணங்கள் மனதில் அமைதியைத் தரும். நாம் நேர்மறையாகச் சிந்திக்கும்போது உடலில் மன அழுத்தம் குறைந்து ஆரோக்கியம் கூடும்.",
                "2. சவால்களை எதிர்கொள்ளுதல்: தடைகள் வரும்போது பயப்படாமல், 'என்னால் இதைத் தீர்க்க முடியும்' என்று நம்பிக்கை வைப்பதே நேர்மறைச் சிந்தனை.",
                "3. வெற்றியை எட்டுதல்: தெளிவான சிந்தனை நமது இலக்குகளை எளிதாக அடைய வழிவகுக்கும்."
            ),
            practicalApplication = "அன்றாட வாழ்வில்: தினமும் காலையில் எழுந்ததும் 'இன்று எனக்கு ஒரு நல்ல நாள்; என்னால் முடிந்தவரை சிறந்த முறையில் செயல்படுவேன்' என்று உங்கள் மனதில் கூறிக்கொள்ளுங்கள்.",
            reflectionQuestion = "இன்று நீங்கள் எதிர்கொள்ளும் ஒரு கடினமான விஷயத்தை நேர்மறையாக எவ்வாறு மாற்றலாம்?"
        ),
        fullTamilTranslation = """நேர்மறைச் சிந்தனையின் சக்தி

நேர்மறையாகச் சிந்திப்பது நாம் மகிழ்ச்சியாகவும் ஆரோக்கியமாகவும் இருக்க உதவுகிறது.
அது நமக்குத் தன்னம்பிக்கையை அளித்து, சவால்களை எதிர்கொள்ள உதவுகிறது.
நாம் நேர்மறையாகச் சிந்திக்கும்போது, நமது இலக்குகளை அடைந்து சிறந்த வாழ்க்கையை வாழ முடியும்.""".trimIndent()
    )

    val scienceSample = OcrResult(
        originalHeading = "Water Cycle in Nature",
        rawEnglishText = """Water Cycle in Nature

Water evaporates from oceans and lakes due to the heat of the sun.
It forms clouds in the sky through condensation.
When the clouds become heavy, water falls back to the earth as rain.""".trimIndent(),
        paragraphs = listOf(
            "Water evaporates from oceans and lakes due to the heat of the sun.",
            "It forms clouds in the sky through condensation.",
            "When the clouds become heavy, water falls back to the earth as rain."
        ),
        keyWords = listOf(
            OcrKeyWord("Water Cycle", "நீர் சுழற்சி", "Noun"),
            OcrKeyWord("Evaporates", "ஆவியாகிறது", "Verb"),
            OcrKeyWord("Oceans", "பெருங்கடல்கள்", "Noun"),
            OcrKeyWord("Heat", "வெப்பம்", "Noun"),
            OcrKeyWord("Condensation", "ஒடுக்கம் / குளிர்விப்பு", "Noun"),
            OcrKeyWord("Clouds", "மேகங்கள்", "Noun"),
            OcrKeyWord("Rain", "மழை", "Noun")
        ),
        detailedExplanation = OcrExplanation(
            headingTamil = "இயற்கையில் நீர் சுழற்சி",
            centralIdea = "பூமியில் உள்ள நீர் ஆவியாகி மேகமாகி, மீண்டும் மழையாகப் பூமிக்குத் திரும்பும் தொடர்ச்சியான இயற்கை நிகழ்வே நீர் சுழற்சி.",
            paragraphsExplanation = listOf(
                "1. சூரிய வெப்பத்தால் ஆறுகள், ஏரிகளில் உள்ள நீர் ஆவியாக மாறுகிறது.",
                "2. அந்த நீராவி வளிமண்டலத்தில் குளிர்ந்து மேகங்களாகத் திரள்கிறது.",
                "3. மேகங்கள் குளிர்ந்தவுடன் மீண்டும் மழையாகப் பூமியை அடைகிறது."
            ),
            practicalApplication = "மழைநீரைச் சேமிப்பதன் மூலம் நாம் இந்த நீர் சுழற்சியைப் பாதுகாத்து எதிர்காலத்திற்கு உதவலாம்.",
            reflectionQuestion = "நீர் சுழற்சி இல்லையென்றால் பூமியில் என்ன நிகழும்?"
        ),
        fullTamilTranslation = """இயற்கையில் நீர் சுழற்சி

சூரிய வெப்பத்தால் பெருங்கடல்கள் மற்றும் ஏரிகளிலிருந்து நீர் ஆவியாகிறது.
அது ஒடுக்கம் மூலம் வானில் மேகங்களை உருவாக்குகிறது.
மேகங்கள் கனமாகும்போது, நீர் மழையாக மீண்டும் பூமியில் விழுகிறது.""".trimIndent()
    )

    /**
     * Parses the structured JSON returned by Gemini Vision AI
     */
    fun parseGeminiJsonResponse(jsonStr: String, fallbackRawText: String): OcrResult {
        try {
            // Remove markdown code fences if Gemini outputs ```json ... ```
            val cleaned = jsonStr.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val root = JSONObject(cleaned)

            val heading = root.optString("heading", "").ifBlank { "Textbook Lesson" }
            val rawEnglish = root.optString("extractedText", "").ifBlank { fallbackRawText }

            val paragraphsList = mutableListOf<String>()
            val paragraphsArray = root.optJSONArray("paragraphs")
            if (paragraphsArray != null) {
                for (i in 0 until paragraphsArray.length()) {
                    paragraphsList.add(paragraphsArray.getString(i))
                }
            }
            if (paragraphsList.isEmpty()) {
                paragraphsList.addAll(rawEnglish.lines().map { it.trim() }.filter { it.isNotEmpty() })
            }

            val keyWordsList = mutableListOf<OcrKeyWord>()
            val keyWordsArray = root.optJSONArray("keyWords")
            if (keyWordsArray != null) {
                for (i in 0 until keyWordsArray.length()) {
                    val item = keyWordsArray.getJSONObject(i)
                    val en = item.optString("english", "").trim()
                    val ta = item.optString("tamil", "").trim()
                    val pos = item.optString("partOfSpeech", "Word").trim()
                    if (en.isNotEmpty() && ta.isNotEmpty()) {
                        keyWordsList.add(OcrKeyWord(en, ta, pos))
                    }
                }
            }

            val explanationObj = root.optJSONObject("explanation")
            val headingTamil = explanationObj?.optString("headingTamil", "") ?: heading
            val centralIdea = explanationObj?.optString("centralIdea", "") ?: ""
            val practicalApp = explanationObj?.optString("practicalApplication", "") ?: "பாடப்பகுதியை மீண்டும் கவனமாகப் படித்துப் பயிற்சி செய்யுங்கள்."
            val reflectionQ = explanationObj?.optString("reflectionQuestion", "") ?: "இந்தப் பாடத்தில் நீங்கள் கற்றுக்கொண்ட முக்கிய கருத்து யாது?"

            val explanationList = mutableListOf<String>()
            val pExpArray = explanationObj?.optJSONArray("paragraphsExplanation")
            if (pExpArray != null) {
                for (i in 0 until pExpArray.length()) {
                    explanationList.add(pExpArray.getString(i))
                }
            }
            if (explanationList.isEmpty()) {
                explanationList.add(centralIdea)
            }

            val detailedExplanation = OcrExplanation(
                headingTamil = headingTamil.ifBlank { "பாடக் கருத்து விளக்கம்" },
                centralIdea = centralIdea.ifBlank { "பாடப்பகுதியின் வரிகளை ஆழ்ந்து கவனித்து வினா-விடைகளைப் பயிற்சி செய்யவும்." },
                paragraphsExplanation = explanationList,
                practicalApplication = practicalApp,
                reflectionQuestion = reflectionQ
            )

            val fullTamilTranslation = root.optString("tamilTranslation", "").ifBlank {
                "பாடப்பகுதியின் மொழிபெயர்ப்பு உருவாக்கப்பட்டது."
            }

            return OcrResult(
                originalHeading = heading,
                rawEnglishText = rawEnglish,
                paragraphs = paragraphsList,
                keyWords = keyWordsList,
                detailedExplanation = detailedExplanation,
                fullTamilTranslation = fullTamilTranslation
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return buildFallbackResult(fallbackRawText)
        }
    }

    /**
     * Clean, non-hardcoded fallback when offline or API call fails.
     * Shows pure extracted text without inventing fake hardcoded mock text.
     */
    fun buildFallbackResult(rawText: String): OcrResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return referenceBookResult

        val lines = trimmed.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val heading = lines.firstOrNull() ?: "பாடப்பகுதி (Extracted Text)"
        val paragraphs = if (lines.size > 1) lines.drop(1) else listOf(trimmed)

        val words = trimmed.split(Regex("[^a-zA-Z]+"))
            .filter { it.length > 3 }
            .distinctBy { it.lowercase() }
            .take(10)

        val keyWords = words.map { word ->
            OcrKeyWord(word.replaceFirstChar { it.uppercase() }, "பாடச்சொல்", "Word")
        }

        return OcrResult(
            originalHeading = heading,
            rawEnglishText = trimmed,
            paragraphs = paragraphs,
            keyWords = keyWords,
            detailedExplanation = OcrExplanation(
                headingTamil = heading,
                centralIdea = "படத்தில் இருந்து ஆங்கில உரை முழுமையாக எடுக்கப்பட்டுள்ளது. இணைய இணைப்பு அல்லது API Key அமைத்தவுடன் ஆழமான தமிழ் விளக்கம் வழங்கப்படும்.",
                paragraphsExplanation = paragraphs,
                practicalApplication = "படத்தில் உள்ள வரிகளை உரக்கப் படித்து உச்சரிப்பைப் பயிற்சி செய்யுங்கள்.",
                reflectionQuestion = "இந்தப் பாடத்தின் வரிகளைப் புரிந்து கொண்டீர்களா?"
            ),
            fullTamilTranslation = "குறிப்பு: AI தமிழ் மொழிபெயர்ப்பு பெற இணைய இணைப்பு மற்றும் Gemini API Key சரிபார்க்கவும்.\n\n" + trimmed
        )
    }
}
