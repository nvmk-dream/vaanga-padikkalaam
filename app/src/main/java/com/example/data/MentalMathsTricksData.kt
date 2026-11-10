package com.example.data

data class MathMethodDetail(
    val title: String,
    val example: String,
    val method1Title: String,
    val method1Steps: List<String>,
    val method2Title: String,
    val method2Steps: List<String>,
    val method3Title: String,
    val method3Steps: List<String>,
    val tip: String
)

object MentalMathsTricksData {

    private val tamilOrdinals = listOf(
        "முதலாவது", "இரண்டாவது", "மூன்றாவது", "நான்காவது", "ஐந்தாவது",
        "ஆறாவது", "ஏழாவது", "எட்டாவது", "ஒன்பதாவது", "பத்தாவது",
        "பதினொன்றாவது", "பன்னிரண்டாவது", "பதின்மூன்றாவது", "பதினான்காவது", "பதினைந்தாவது",
        "பதினாறாவது", "பதினேழாவது", "பதினெட்டாவது", "பத்தொன்பதாவது", "இருபதாவது"
    )

    fun getDetailedExplanation(pdfTitle: String, pageIndex: Int, itemIndex: Int): MathMethodDetail {
        val questionNumber = pageIndex * 20 + itemIndex + 1

        return when {
            pdfTitle.contains("கூட்டல்") -> {
                MathMethodDetail(
                    title = "கூட்டல் மனக்கணக்கு எளிய வழிமுறைகள் (Addition Tricks)",
                    example = "எடுத்துக்காட்டு $questionNumber: 86 + 134 = 220",
                    method1Title = "முறை 1: முழு எண்களாக மாற்றி கூட்டுதல் (Rounding / Making 10s - மிக எளிதான முறை)",
                    method1Steps = listOf(
                        "86-ஐ அருகிலுள்ள முழு எண்ணான 90-ஆக மாற்ற 4 தேவை.",
                        "அந்த 4-ஐ 134-லிருந்து கழித்து எடுத்துக்கொள்ளுங்கள் (134 - 4 = 130).",
                        "இப்போது இரண்டு எண்களையும் எளிதாகக் கூட்டலாம்: 90 + 130 = 220."
                    ),
                    method2Title = "முறை 2: இடமதிப்புப் படி பிரித்துக் கூட்டுதல் (Place Value Splitting)",
                    method2Steps = listOf(
                        "முதலில் நூறு மற்றும் பத்துகளை மட்டும் தனியாகக் கூட்டுங்கள்: 80 + 130 = 210.",
                        "அடுத்து ஒன்றுகளை (ஒற்றை இலக்க எண்களை) தனியாகக் கூட்டுங்கள்: 6 + 4 = 10.",
                        "இப்போது இரண்டையும் சேர்த்துக் கூட்டுங்கள்: 210 + 10 = 220."
                    ),
                    method3Title = "முறை 3: பெரிய எண்ணுடன் சிறிய எண்ணைப் பிரித்துக் கூட்டுதல் (Left-to-Right Addition)",
                    method3Steps = listOf(
                        "பெரிய எண்ணான 134-ஐ அப்படியே மனதில் வைத்துக்கொள்ளுங்கள்.",
                        "அதனுடன் 80-ஐ முதலில் கூட்டுங்கள்: 134 + 80 = 214.",
                        "மீதமுள்ள 6-ஐ அதனுடன் கூட்டுங்கள்: 214 + 6 = 220."
                    ),
                    tip = "பத்துகளாகவும் நூறுகளாகவும் முழுமைப்படுத்துவது (Rounding) மனக்கணக்கில் கூட்டலை மிக வேகமாக்கும்!"
                )
            }
            pdfTitle.contains("கழித்தல்") -> {
                MathMethodDetail(
                    title = "கழித்தல் மனக்கணக்கு எளிய வழிமுறைகள் (Subtraction Tricks)",
                    example = "எடுத்துக்காட்டு $questionNumber: 220 - 86 = 134",
                    method1Title = "முறை 1: முழு எண்ணாக மாற்றி கழித்தல் (Rounding / Base Method)",
                    method1Steps = listOf(
                        "86-ஐ 90 ஆக மாற்ற 4 தேவை.",
                        "220-லிருந்து 90-ஐ நேரடியாகக் கழியுங்கள்: 220 - 90 = 130.",
                        "முன்பு நாம் அதிகமாகக் கழித்த 4-ஐ மீண்டும் கூட்டிக்கொள்ளுங்கள்: 130 + 4 = 134."
                    ),
                    method2Title = "முறை 2: இடமதிப்புப் படி பிரித்துக் கழித்தல் (Place Value Decomposition)",
                    method2Steps = listOf(
                        "முதலில் பத்துகளை மட்டும் கழியுங்கள்: 220 - 80 = 140.",
                        "அடுத்து ஒன்றுகளை (6) கழியுங்கள்: 140 - 6 = 134.",
                        "விடை எளிதாகக் கிடைத்துவிடும்: 134."
                    ),
                    method3Title = "முறை 3: முன்னோக்கி கூட்டி வேறுபாடு காணுதல் (Counting Up Method)",
                    method3Steps = listOf(
                        "சிறிய எண் 86-லிருந்து 100-ஐ அடைய +14 தேவை.",
                        "100-லிருந்து 220-ஐ அடைய +120 தேவை.",
                        "இப்போது இரண்டையும் கூட்டுங்கள்: 120 + 14 = 134."
                    ),
                    tip = "முன்னோக்கி கூட்டும் முறை (Counting Up) கடன்பெற்று கழிப்பதை விட மிக எளிமையானது!"
                )
            }
            pdfTitle.contains("பெருக்கல்") -> {
                MathMethodDetail(
                    title = "பெருக்கல் மனக்கணக்கு எளிய வழிமுறைகள் (Multiplication Tricks)",
                    example = "எடுத்துக்காட்டு $questionNumber: 15 × 12 = 180",
                    method1Title = "முறை 1: பத்துகளாகப் பிரித்துப் பெருக்குதல் (Splitting Method)",
                    method1Steps = listOf(
                        "12-ஐ (10 + 2) என்று பிரியுங்கள்.",
                        "15 × 10 = 150 (மிக எளிது).",
                        "15 × 2 = 30; இப்போது இரண்டையும் கூட்டுங்கள்: 150 + 30 = 180."
                    ),
                    method2Title = "முறை 2: இரட்டிப்பாக்கி பாதியாக்கும் முறை (Double and Halve)",
                    method2Steps = listOf(
                        "15-ஐ இரட்டிப்பாக்குங்கள்: 15 × 2 = 30.",
                        "12-ஐ பாதியாக்குங்கள்: 12 ÷ 2 = 6.",
                        "இப்போது மனக்கணக்கில் பெருக்குங்கள்: 30 × 6 = 180."
                    ),
                    method3Title = "முறை 3: காரணி முறை (Factoring Trick)",
                    method3Steps = listOf(
                        "12-ஐ (3 × 4) என்று பிரியுங்கள்.",
                        "15 × 4 = 60.",
                        "60 × 3 = 180."
                    ),
                    tip = "இரட்டிப்பாக்கி பாதியாக்கும் முறை (Double & Halve) 5 மற்றும் இரட்டைப்படை எண்களுக்கு மிகச் சிறந்தது!"
                )
            }
            else -> {
                MathMethodDetail(
                    title = "வகுத்தல் மனக்கணக்கு எளிய வழிமுறைகள் (Division Tricks)",
                    example = "எடுத்துக்காட்டு $questionNumber: 220 ÷ 4 = 55",
                    method1Title = "முறை 1: தொடர்ந்து பாதியாக்குதல் (Half and Half Again)",
                    method1Steps = listOf(
                        "4-ஆல் வகுக்க, எண்ணை இரண்டு முறை பாதியாக்கினால் போதும்.",
                        "220-ன் பாதி: 220 ÷ 2 = 110.",
                        "110-ன் பாதி: 110 ÷ 2 = 55."
                    ),
                    method2Title = "முறை 2: எண்களைப் பிரித்து வகுத்தல் (Chunking / Splitting)",
                    method2Steps = listOf(
                        "220-ஐ (200 + 20) என்று பிரியுங்கள்.",
                        "200 ÷ 4 = 50.",
                        "20 ÷ 4 = 5; இப்போது கூட்டுங்கள்: 50 + 5 = 55."
                    ),
                    method3Title = "முறை 3: பெருக்கலை எதிர்ப்பதமாகப் பயன்படுத்துதல் (Inverse Multiplication)",
                    method3Steps = listOf(
                        "4 × 50 = 200 என்று நமக்குத் தெரியும்.",
                        "மீதமுள்ள 20-ல் 4 ஐந்து முறை அடங்கும் (4 × 5 = 20).",
                        "மொத்த விடை: 50 + 5 = 55."
                    ),
                    tip = "4-ஆல் வகுக்க இரண்டு முறை பாதியாக்குங்கள்; 8-ஆல் வகுக்க மூன்று முறை பாதியாக்குங்கள்!"
                )
            }
        }
    }

    fun getSpeechForTrick(pdfTitle: String, pageIndex: Int, itemIndex: Int): String {
        val ordinal = tamilOrdinals.getOrElse(itemIndex) { "${itemIndex + 1}-வது" }
        val overallQuestionNumber = pageIndex * 20 + itemIndex + 1
        val detail = getDetailedExplanation(pdfTitle, pageIndex, itemIndex)

        return "$ordinal கணக்கு எண் $overallQuestionNumber. ${detail.example}. ${detail.method1Title}. ${detail.method1Steps.joinToString(" ")}. ${detail.method2Title}. ${detail.method2Steps.joinToString(" ")}. ${detail.tip}"
    }
}
