package com.example.data

object MentalMathsTricksData {

    private val tamilOrdinals = listOf(
        "முதலாவது", "இரண்டாவது", "மூன்றாவது", "நான்காவது", "ஐந்தாவது",
        "ஆறாவது", "ஏழாவது", "எட்டாவது", "ஒன்பதாவது", "பத்தாவது",
        "பதினொன்றாவது", "பன்னிரண்டாவது", "பதின்மூன்றாவது", "பதினான்காவது", "பதினைந்தாவது",
        "பதினாறாவது", "பதினேழாவது", "பதினெட்டாவது", "பத்தொன்பதாவது", "இருபதாவது"
    )

    private val additionTricks = listOf(
        "ஒரு எண்ணை 10 ஆக மாற்றி, மீதியைச் சேர்க்கவும்.",
        "எண்களை பத்துகள் மற்றும் ஒன்றுகளாகப் பிரித்துக் கூட்டவும்.",
        "முதலில் பெரிய எண்ணை மனதில் வைத்துக் கொண்டு அடுத்த எண்ணைச் சேர்க்கவும்.",
        "இரட்டிப்பு தெரிந்தால், அதற்கு அருகிலுள்ள எண்ணைச் சேர்த்துக் கணக்கிடவும்.",
        "100-ஐ அடைய எவ்வளவு தேவை என்பதைப் பயன்படுத்தவும்."
    )

    private val subtractionTricks = listOf(
        "இரண்டு எண்களையும் அருகிலுள்ள பத்தாக மாற்றி வேறுபாட்டைச் சரிசெய்யவும்.",
        "பத்துகள், ஒன்றுகள் எனப் பிரித்துக் கழிக்கவும்.",
        "கழித்தலை கூட்டலால் சரிபார்க்கவும்.",
        "100 அல்லது 1000-ஐ அடிப்படையாகக் கொண்டு கணக்கிடவும்.",
        "எண்களுக்கிடையிலான இடைவெளியை எண்ணிக் கண்டறியவும்."
    )

    private val multiplicationTricks = listOf(
        "10-ஆல் பெருக்கி, தேவையான அளவைச் சரிசெய்யவும்.",
        "எண்ணை பத்துகள் மற்றும் ஒன்றுகளாகப் பிரிக்கவும்.",
        "இரட்டிப்பு முறையைப் பயன்படுத்தவும்.",
        "5-ஆல் பெருக்கும்போது 10-ஆல் பெருக்கி பாதியாக்கவும்.",
        "11-ஆல் பெருக்கும்போது இலக்கங்களின் அமைப்பைக் கவனிக்கவும்."
    )

    private val divisionTricks = listOf(
        "வகுத்தலைச் சரிபார்க்க, விடையை வகுத்தியால் பெருக்கவும்.",
        "இரண்டால் வகுப்பது பாதியைக் கண்டறிவதற்கு சமம்.",
        "10-ஆல் வகுக்கும்போது தசம இடத்தை கவனிக்கவும்.",
        "முதலில் பொதுவான பெருக்கல் அட்டவணையை நினைவுகூரவும்.",
        "மீதியில்லாத வகுத்தலைத் தேர்ந்தெடுத்து பயிற்சி செய்யவும்."
    )

    fun getSpeechForTrick(pdfTitle: String, pageIndex: Int, itemIndex: Int): String {
        val ordinal = tamilOrdinals.getOrElse(itemIndex) { "${itemIndex + 1}-வது" }
        val overallQuestionNumber = pageIndex * 20 + itemIndex + 1

        val (operationWord, tricksList) = when {
            pdfTitle.contains("கூட்டல்") -> Pair("கூட்டல் பயிற்சி", additionTricks)
            pdfTitle.contains("கழித்தல்") -> Pair("கழித்தல் பயிற்சி", subtractionTricks)
            pdfTitle.contains("பெருக்கல்") -> Pair("பெருக்கல் பயிற்சி", multiplicationTricks)
            pdfTitle.contains("வகுத்தல்") -> Pair("வகுத்தல் பயிற்சி", divisionTricks)
            else -> Pair("கணித பயிற்சி", emptyList())
        }

        val trickExplanation = if (tricksList.isNotEmpty()) {
            val trick = tricksList[itemIndex % tricksList.size]
            "வழிமுறை: $trick"
        } else {
            "பயிற்சியைக் கவனித்துக் கணக்கிடவும்."
        }

        return "$ordinal கணக்கு (எண் $overallQuestionNumber), $operationWord. $trickExplanation"
    }
}
