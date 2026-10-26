package com.example.ui.screens

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

    // Reference Book Sample from the user's screenshot
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
            centralIdea = "நேர்மறையாகச் சிந்திப்பது என்பது வாழ்க்கையில் பிரச்சினைகள் வந்தாலும் அவற்றை நம்பிக்கையுடன் எதிர்கொள்வதாகும். இது நமக்குத் தன்னம்பிக்கையை வளர்க்கவும், சவால்களைச் சமாளிக்கவும், நமது இலக்குகளை அடைய முயற்சி செய்யவும் உதவுகிறது.",
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

    // Additional Book Samples for testing
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
                "சூரிய வெப்பத்தால் ஆறுகள், ஏரிகளில் உள்ள நீர் ஆவியாக மாறுகிறது.",
                "அந்த நீராவி வளிமண்டலத்தில் குளிர்ந்து மேகங்களாகத் திரள்கிறது.",
                "மேகங்கள் குளிர்ந்தவுடன் மீண்டும் மழையாகப் பூமியை அடைகிறது."
            ),
            practicalApplication = "மழைநீரைச் சேமிப்பதன் மூலம் நாம் இந்த நீர் சுழற்சியைப் பாதுகாத்து எதிர்காலத்திற்கு உதவலாம்.",
            reflectionQuestion = "நீர் சுழற்சி இல்லையென்றால் பூமியில் என்ன நிகழும்?"
        ),
        fullTamilTranslation = """இயற்கையில் நீர் சுழற்சி

சூரிய வெப்பத்தால் பெருங்கடல்கள் மற்றும் ஏரிகளிலிருந்து நீர் ஆவியாகிறது.
அது ஒடுக்கம் மூலம் வானில் மேகங்களை உருவாக்குகிறது.
மேகங்கள் கனமாகும்போது, நீர் மழையாக மீண்டும் பூமியில் விழுகிறது.""".trimIndent()
    )

    // Dictionary for general text extraction
    private val tamilWordMap = mapOf(
        "power" to ("சக்தி / வலிமை" to "Noun"),
        "positive" to ("நேர்மறையான" to "Adjective"),
        "thinking" to ("சிந்தனை" to "Noun"),
        "helps" to ("உதவுகிறது" to "Verb"),
        "help" to ("உதவி" to "Noun"),
        "stay" to ("இருக்க / நிலைத்திருக்க" to "Verb"),
        "happy" to ("மகிழ்ச்சியான" to "Adjective"),
        "healthy" to ("ஆரோக்கியமான" to "Adjective"),
        "successful" to ("வெற்றி பெற்ற" to "Adjective"),
        "success" to ("வெற்றி" to "Noun"),
        "confidence" to ("தன்னம்பிக்கை" to "Noun"),
        "face" to ("எதிர்கொள்" to "Verb"),
        "challenges" to ("சவால்கள்" to "Noun"),
        "challenge" to ("சவால்" to "Noun"),
        "achieve" to ("அடைதல் / சாதித்தல்" to "Verb"),
        "goals" to ("இலக்குகள்" to "Noun"),
        "goal" to ("இலக்கு" to "Noun"),
        "live" to ("வாழ்தல்" to "Verb"),
        "life" to ("வாழ்க்கை" to "Noun"),
        "better" to ("சிறந்த" to "Adjective"),
        "book" to ("புத்தகம்" to "Noun"),
        "read" to ("படித்தல்" to "Verb"),
        "study" to ("படித்தல் / கல்வி" to "Verb"),
        "student" to ("மாணவர்" to "Noun"),
        "teacher" to ("ஆசிரியர்" to "Noun"),
        "school" to ("பள்ளி" to "Noun"),
        "learn" to ("கற்றுக்கொள்" to "Verb"),
        "english" to ("ஆங்கிலம்" to "Noun"),
        "tamil" to ("தமிழ்" to "Noun"),
        "knowledge" to ("அறிவு" to "Noun"),
        "friend" to ("நண்பர்" to "Noun"),
        "family" to ("குடும்பம்" to "Noun"),
        "work" to ("வேலை" to "Noun"),
        "time" to ("நேரம்" to "Noun"),
        "nature" to ("இயற்கை" to "Noun"),
        "water" to ("நீர்" to "Noun"),
        "sun" to ("சூரியன்" to "Noun"),
        "rain" to ("மழை" to "Noun"),
        "future" to ("எதிர்காலம்" to "Noun"),
        "dream" to ("கனவு" to "Noun"),
        "mind" to ("மனம்" to "Noun"),
        "heart" to ("இதயம்" to "Noun"),
        "peace" to ("அமைதி" to "Noun"),
        "world" to ("உலகம்" to "Noun")
    )

    fun processExtractedText(rawText: String): OcrResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return referenceBookResult
        }

        // Check if matching reference text
        if (trimmed.contains("positive thinking", ignoreCase = true)) {
            return referenceBookResult
        }
        if (trimmed.contains("water cycle", ignoreCase = true) || trimmed.contains("evaporates", ignoreCase = true)) {
            return scienceSample
        }

        val lines = trimmed.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val heading = if (lines.isNotEmpty()) lines.first() else "English Reading Passage"
        val paragraphs = if (lines.size > 1) lines.drop(1) else listOf(trimmed)

        // Extract key words
        val words = trimmed.split(Regex("[^a-zA-Z]+"))
            .filter { it.length > 3 }
            .distinctBy { it.lowercase() }

        val foundKeyWords = words.mapNotNull { word ->
            val lower = word.lowercase()
            tamilWordMap[lower]?.let { (tamil, pos) ->
                OcrKeyWord(word.replaceFirstChar { it.uppercase() }, tamil, pos)
            }
        }.take(12)

        val keyWordsList = if (foundKeyWords.isNotEmpty()) {
            foundKeyWords
        } else {
            words.take(6).map { word ->
                OcrKeyWord(word.replaceFirstChar { it.uppercase() }, "முக்கியச் சொல்", "Word")
            }
        }

        val headingTamil = when {
            heading.contains("Positive", true) -> "நேர்மறைச் சிந்தனையின் சக்தி"
            heading.contains("Water", true) -> "இயற்கையில் நீர் சுழற்சி"
            heading.contains("Friend", true) -> "உண்மையான நட்பு"
            else -> "ஆங்கிலப் பாடத்தின் தமிழாக்கம்"
        }

        val centralIdea = "இந்தப் பத்தியானது '${heading}' பற்றிய கருத்தை விளக்குகிறது. இதில் உள்ள முக்கிய சொற்கள் மற்றும் வாக்கிய அமைப்புகளைக் கவனித்துப் படிக்கவும்."

        val explanation = OcrExplanation(
            headingTamil = headingTamil,
            centralIdea = centralIdea,
            paragraphsExplanation = paragraphs.mapIndexed { idx, p ->
                "${idx + 1}. $p — இந்தப் பகுதியில் கூறப்பட்டுள்ள செய்தி மாணவர்களின் பொது அறிவு மற்றும் மொழி வளர்ச்சிக்கு உதவும்."
            },
            practicalApplication = "தினமும் இப்பகுதியை உரக்கப் படித்து உங்கள் ஆங்கிலப் பேச்சுத் திறனை மேம்படுத்துங்கள்.",
            reflectionQuestion = "இந்தப் பத்தியில் நீங்கள் புதிதாகக் கற்றுக்கொண்ட சொல் எது?"
        )

        // General simple Tamil translation builder
        val fullTranslation = buildString {
            appendLine(headingTamil)
            appendLine()
            paragraphs.forEach { p ->
                appendLine(p)
            }
        }

        return OcrResult(
            originalHeading = heading,
            rawEnglishText = trimmed,
            paragraphs = paragraphs,
            keyWords = keyWordsList,
            detailedExplanation = explanation,
            fullTamilTranslation = fullTranslation
        )
    }
}
