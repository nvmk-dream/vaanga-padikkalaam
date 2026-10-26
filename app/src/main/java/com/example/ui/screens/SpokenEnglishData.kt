package com.example.ui.screens

import androidx.compose.ui.graphics.Color

data class SpokenSentence(
    val id: Int,
    val english: String,
    val tamil: String,
    val tip: String = "",
    val category: String = ""
)

data class TenseComparisonItem(
    val title: String,
    val pastSentence: String,
    val pastTamil: String,
    val presentSentence: String,
    val presentTamil: String,
    val futureSentence: String,
    val futureTamil: String
)

data class SpeechChallenge(
    val topicNumber: Int,
    val topic: String,
    val tamilTopic: String,
    val englishSpeech: String,
    val tamilSpeech: String
)

data class SpokenChapter(
    val id: String,
    val number: Int,
    val title: String,
    val tamilTitle: String,
    val description: String,
    val badge: String,
    val sentences: List<SpokenSentence> = emptyList(),
    val comparisons: List<TenseComparisonItem> = emptyList(),
    val challenges: List<SpeechChallenge> = emptyList(),
    val rules: List<Pair<String, String>> = emptyList()
)

data class SpokenBook(
    val id: String,
    val title: String,
    val tamilTitle: String,
    val description: String,
    val badge: String,
    val chaptersCount: Int = 14,
    val accentColor: Color,
    val secondaryColor: Color,
    val chapters: List<SpokenChapter>
)

object SpokenEnglishRepository {

    val books: List<SpokenBook> by lazy {
        listOf(book1, book2)
    }

    // ==========================================
    // BOOK 1: Spoken English Through Tamil
    // ==========================================
    val book1: SpokenBook by lazy {
        SpokenBook(
            id = "book_1",
            title = "Spoken English Through Tamil – Book 1",
            tamilTitle = "அன்றாடப் பேச்சு ஆங்கிலம் – புத்தகம் 1",
            description = "அன்றாட வாழ்க்கையில் ஆங்கிலத்தில் பேசுவதற்கான அடிப்படைச் சொற்கள், 100 வாக்கியங்கள் மற்றும் எளிய பயிற்சிகள்.",
            badge = "14 பாடங்கள் • 300+ வாக்கியங்கள்",
            accentColor = Color(0xFF1E3A8A), // Dark Navy Blue
            secondaryColor = Color(0xFF7C3AED), // Royal Purple
            chapters = listOf(
                // 1. 50 Daily Action Verbs
                SpokenChapter(
                    id = "b1_c1",
                    number = 1,
                    title = "50 Daily Action Verbs",
                    tamilTitle = "50 அன்றாட வினைச்சொற்கள்",
                    description = "அதிகம் பயன்படுத்தும் அன்றாட வினைச்சொற்கள் மற்றும் எளிய உதாரணங்கள்.",
                    badge = "50 சொற்கள்",
                    sentences = listOf(
                        SpokenSentence(1, "Come here.", "இங்கே வா.", "Come - வா", "அடிப்படை"),
                        SpokenSentence(2, "Go there.", "அங்கே போ.", "Go - போ", "அடிப்படை"),
                        SpokenSentence(3, "Eat your food.", "உன் உணவைச் சாப்பிடு.", "Eat - சாப்பிடு", "அடிப்படை"),
                        SpokenSentence(4, "Drink water.", "தண்ணீர் குடி.", "Drink - குடி", "அடிப்படை"),
                        SpokenSentence(5, "Read this book.", "இந்தப் புத்தகத்தைப் படி.", "Read - படி", "அடிப்படை"),
                        SpokenSentence(6, "Write your name.", "உன் பெயரை எழுது.", "Write - எழுது", "அடிப்படை"),
                        SpokenSentence(7, "Speak slowly.", "மெதுவாகப் பேசு.", "Speak - பேசு", "அடிப்படை"),
                        SpokenSentence(8, "Listen to me.", "நான் சொல்வதைக் கவனித்துக் கேள்.", "Listen - கவனி", "அடிப்படை"),
                        SpokenSentence(9, "Give me a pen.", "எனக்கு ஒரு பேனா கொடு.", "Give - கொடு", "அடிப்படை"),
                        SpokenSentence(10, "Take this book.", "இந்தப் புத்தகத்தை எடு.", "Take - எடு", "அடிப்படை"),
                        SpokenSentence(11, "Buy some milk.", "கொஞ்சம் பால் வாங்கு.", "Buy - வாங்கு", "அடிப்படை"),
                        SpokenSentence(12, "Ask a question.", "ஒரு கேள்வி கேள்.", "Ask - கேள்", "அடிப்படை"),
                        SpokenSentence(13, "Make a list.", "ஒரு பட்டியல் தயாரி.", "Make - உருவாக்கு / செய்", "அடிப்படை"),
                        SpokenSentence(14, "Do your work.", "உன் வேலையைச் செய்.", "Do - செய்", "அடிப்படை"),
                        SpokenSentence(15, "See this picture.", "இந்தப் படத்தைப் பார்.", "See - பார்", "அடிப்படை"),
                        SpokenSentence(16, "Look at me.", "என்னைப் பார்.", "Look - பார்", "அடிப்படை"),
                        SpokenSentence(17, "Watch the video.", "காணொளியைப் பார்.", "Watch - பார்த்துக்கொண்டிரு", "அடிப்படை"),
                        SpokenSentence(18, "I hear a sound.", "எனக்கு ஒரு சத்தம் கேட்கிறது.", "Hear - கேள்", "அடிப்படை"),
                        SpokenSentence(19, "Walk slowly.", "மெதுவாக நட.", "Walk - நட", "அடிப்படை"),
                        SpokenSentence(20, "Don't run.", "ஓடாதே.", "Run - ஓடு", "அடிப்படை"),
                        SpokenSentence(21, "Sit here.", "இங்கே உட்கார்.", "Sit - உட்கார்", "அடிப்படை"),
                        SpokenSentence(22, "Stand up.", "எழுந்து நில்.", "Stand - நில்", "அடிப்படை"),
                        SpokenSentence(23, "Sleep well.", "நன்றாகத் தூங்கு.", "Sleep - தூங்கு", "அடிப்படை"),
                        SpokenSentence(24, "Wake up early.", "சீக்கிரம் விழித்தெழு.", "Wake - விழித்தெழு", "அடிப்படை"),
                        SpokenSentence(25, "Get ready.", "தயாராகு.", "Get - பெறு / ஆகு", "அடிப்படை"),
                        SpokenSentence(26, "Put it here.", "அதை இங்கே வை.", "Put - வை", "அடிப்படை"),
                        SpokenSentence(27, "Keep it safe.", "அதை பத்திரமாக வை.", "Keep - வைத்திரு", "அடிப்படை"),
                        SpokenSentence(28, "Open the door.", "கதவைத் திற.", "Open - திற", "அடிப்படை"),
                        SpokenSentence(29, "Close the window.", "ஜன்னலை மூடு.", "Close - மூடு", "அடிப்படை"),
                        SpokenSentence(30, "Bring your book.", "உன் புத்தகத்தைக் கொண்டு வா.", "Bring - கொண்டு வா", "அடிப்படை"),
                        SpokenSentence(31, "Carry this bag.", "இந்தப் பையைச் சுமந்து செல்.", "Carry - சுமந்து செல்", "அடிப்படை"),
                        SpokenSentence(32, "Send me a message.", "எனக்கு ஒரு செய்தி அனுப்பு.", "Send - அனுப்பு", "அடிப்படை"),
                        SpokenSentence(33, "Call your friend.", "உன் நண்பரை அழை.", "Call - அழை", "அடிப்படை"),
                        SpokenSentence(34, "Help me.", "எனக்கு உதவு.", "Help - உதவு", "அடிப்படை"),
                        SpokenSentence(35, "I work every day.", "நான் தினமும் வேலை செய்கிறேன்.", "Work - வேலை செய்", "அடிப்படை"),
                        SpokenSentence(36, "Play with me.", "என்னுடன் விளையாடு.", "Play - விளையாடு", "அடிப்படை"),
                        SpokenSentence(37, "Learn English.", "ஆங்கிலம் கற்றுக்கொள்.", "Learn - கற்றுக்கொள்", "அடிப்படை"),
                        SpokenSentence(38, "Teach me English.", "எனக்கு ஆங்கிலம் கற்றுக்கொடு.", "Teach - கற்றுக்கொடு", "அடிப்படை"),
                        SpokenSentence(39, "Study well.", "நன்றாகப் படி.", "Study - படி", "அடிப்படை"),
                        SpokenSentence(40, "I understand.", "எனக்குப் புரிகிறது.", "Understand - புரிந்துகொள்", "அடிப்படை"),
                        SpokenSentence(41, "I know him.", "எனக்கு அவரைத் தெரியும்.", "Know - தெரிந்துகொள்", "அடிப்படை"),
                        SpokenSentence(42, "Think carefully.", "கவனமாகச் சிந்தி.", "Think - சிந்தி", "அடிப்படை"),
                        SpokenSentence(43, "Smile always.", "எப்போதும் புன்னகை செய்.", "Smile - புன்னகை", "அடிப்படை"),
                        SpokenSentence(44, "Wait for me.", "எனக்காகக் காத்திரு.", "Wait - காத்திரு", "அடிப்படை"),
                        SpokenSentence(45, "Meet me tomorrow.", "நாளை என்னைச் சந்தி.", "Meet - சந்தி", "அடிப்படை"),
                        SpokenSentence(46, "Start now.", "இப்போது தொடங்கு.", "Start - தொடங்கு", "அடிப்படை"),
                        SpokenSentence(47, "Finish your work.", "உன் வேலையை முடி.", "Finish - முடி", "அடிப்படை"),
                        SpokenSentence(48, "Clean your room.", "உன் அறையைச் சுத்தம் செய்.", "Clean - சுத்தம் செய்", "அடிப்படை"),
                        SpokenSentence(49, "Wash your hands.", "உன் கைகளைக் கழுவு.", "Wash - கழுவு", "அடிப்படை"),
                        SpokenSentence(50, "Cook the rice.", "சாதம் சமை.", "Cook - சமை", "அடிப்படை")
                    )
                ),

                // 2. English at Home (50 Sentences)
                SpokenChapter(
                    id = "b1_c2",
                    number = 2,
                    title = "English at Home",
                    tamilTitle = "வீட்டில் பேசும் அன்றாட ஆங்கிலம்",
                    description = "காலை வேலை, உணவு, வீட்டுப் பணிகள் மற்றும் இரவுப் படுக்கை வாக்கியங்கள்.",
                    badge = "50 வாக்கியங்கள்",
                    sentences = listOf(
                        // Morning
                        SpokenSentence(1, "Wake up!", "விழித்துக்கொள்!", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(2, "Get up.", "எழுந்திரு.", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(3, "Brush your teeth.", "பல் துலக்கு.", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(4, "Wash your face.", "முகம் கழுவு.", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(5, "Take a bath.", "குளி.", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(6, "Get ready quickly.", "சீக்கிரம் தயாராகு.", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(7, "Comb your hair.", "தலை வாரிக்கொள்.", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(8, "Wear your shoes.", "காலணிகளை அணிந்துகொள்.", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(9, "Make your bed.", "படுக்கையைச் சரிசெய்.", "காலை வழக்கம்", "Morning Routine"),
                        SpokenSentence(10, "Change your clothes.", "உடைகளை மாற்றிக்கொள்.", "காலை வழக்கம்", "Morning Routine"),
                        // Food & Habits
                        SpokenSentence(11, "Eat your food.", "உன் உணவைச் சாப்பிடு.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(12, "Drink some milk.", "கொஞ்சம் பால் குடி.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(13, "Don't spill the water.", "தண்ணீரைக் கீழே சிந்தாதே.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(14, "Wash your hands before eating.", "சாப்பிடுவதற்கு முன் கைகளைக் கழுவு.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(15, "Chew your food properly.", "உணவை நன்றாக மென்று சாப்பிடு.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(16, "I am hungry.", "எனக்குப் பசிக்கிறது.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(17, "I am full.", "எனக்கு வயிறு நிறைந்துவிட்டது.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(18, "The food is delicious.", "உணவு மிகவும் சுவையாக இருக்கிறது.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(19, "Please give me some water.", "தயவுசெய்து எனக்குக் கொஞ்சம் தண்ணீர் கொடுங்கள்.", "உணவு வழக்கம்", "Food & Habits"),
                        SpokenSentence(20, "Don't waste food.", "உணவை வீணாக்காதே.", "உணவு வழக்கம்", "Food & Habits"),
                        // Orders & Chores
                        SpokenSentence(21, "Come here.", "இங்கே வா.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(22, "Go there.", "அங்கே போ.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(23, "Sit down.", "உட்கார்.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(24, "Stand up.", "எழுந்து நில்.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(25, "Study well.", "நன்றாகப் படி.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(26, "Do your homework.", "உன் வீட்டுப்பாடத்தைச் செய்.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(27, "Listen to me.", "நான் சொல்வதைக் கவனித்துக் கேள்.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(28, "Don't shout.", "கத்தாதே.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(29, "Be quiet.", "அமைதியாக இரு.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(30, "Turn on the fan.", "மின்விசிறியை இயக்கு.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(31, "Turn off the light.", "விளக்கை அணை.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(32, "Don't run inside the house.", "வீட்டிற்குள் ஓடாதே.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(33, "Keep your books in their place.", "புத்தகங்களை அவற்றின் இடத்தில் வை.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(34, "Help your mother.", "உன் அம்மாவுக்கு உதவு.", "வீட்டுப் பணிகள்", "House Orders"),
                        SpokenSentence(35, "Clean your room.", "உன் அறையைச் சுத்தம் செய்.", "வீட்டுப் பணிகள்", "House Orders"),
                        // Evening & Bedtime
                        SpokenSentence(36, "Go and play.", "போய் விளையாடு.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(37, "Come inside; it's getting dark.", "உள்ளே வா; இருட்டிக்கொண்டிருக்கிறது.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(38, "Drink some water.", "கொஞ்சம் தண்ணீர் குடி.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(39, "Finish your homework.", "வீட்டுப்பாடத்தை முடி.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(40, "Have your dinner.", "இரவு உணவைச் சாப்பிடு.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(41, "Change into your night clothes.", "இரவு உடைகளை அணிந்துகொள்.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(42, "Don't stay up late.", "வெகுநேரம் விழித்திருக்காதே.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(43, "Go to bed.", "படுக்கைக்குச் செல்.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(44, "Close your eyes.", "கண்களை மூடு.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(45, "Go to sleep.", "தூங்கச் செல்.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(46, "Good night.", "இரவு வணக்கம்.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(47, "Sweet dreams.", "இனிய கனவுகள் காண்க.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(48, "See you tomorrow.", "நாளை சந்திப்போம்.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(49, "Don't forget to pray.", "பிரார்த்தனை செய்ய மறந்துவிடாதே.", "இரவு நேரம்", "Evening & Night"),
                        SpokenSentence(50, "Sleep well.", "நன்றாகத் தூங்கு.", "இரவு நேரம்", "Evening & Night")
                    )
                ),

                // 3. Magic Words & Politeness (40 Sentences)
                SpokenChapter(
                    id = "b1_c3",
                    number = 3,
                    title = "Magic Words & Politeness",
                    tamilTitle = "மரியாதைச் சொற்கள் & நற்பண்பு",
                    description = "தயவுசெய்து, நன்றி, மன்னிப்பு போன்ற பண்பான சொற்கள் மூலம் பேசுதல்.",
                    badge = "40 வாக்கியங்கள்",
                    sentences = listOf(
                        SpokenSentence(1, "Please help me.", "தயவுசெய்து எனக்கு உதவுங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(2, "Please come in.", "தயவுசெய்து உள்ளே வாருங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(3, "Please sit down.", "தயவுசெய்து உட்காருங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(4, "Please speak slowly.", "தயவுசெய்து மெதுவாகப் பேசுங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(5, "Please repeat that.", "தயவுசெய்து அதை மீண்டும் சொல்லுங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(6, "Please wait a moment.", "தயவுசெய்து ஒரு நிமிடம் காத்திருங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(7, "Please give me some water.", "தயவுசெய்து எனக்குக் கொஞ்சம் தண்ணீர் கொடுங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(8, "Please open the door.", "தயவுசெய்து கதவைத் திறங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(9, "Please call me later.", "தயவுசெய்து பின்னர் என்னை அழையுங்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(10, "Please don't worry.", "தயவுசெய்து கவலைப்படாதீர்கள்.", "Please பயன்பாடு", "Politeness"),
                        SpokenSentence(11, "Thank you very much.", "மிக்க நன்றி.", "நன்றி தெரிவித்தல்", "Thank you"),
                        SpokenSentence(12, "Thank you for your help.", "உங்கள் உதவிக்கு நன்றி.", "நன்றி தெரிவித்தல்", "Thank you"),
                        SpokenSentence(13, "Thanks for coming.", "வந்ததற்கு நன்றி.", "நன்றி தெரிவித்தல்", "Thank you"),
                        SpokenSentence(14, "Thank you for the gift.", "பரிசுக்கு நன்றி.", "நன்றி தெரிவித்தல்", "Thank you"),
                        SpokenSentence(15, "Thanks for your time.", "உங்கள் நேரத்திற்கு நன்றி.", "நன்றி தெரிவித்தல்", "Thank you"),
                        SpokenSentence(16, "Thank you for teaching me.", "எனக்குக் கற்றுக்கொடுத்ததற்கு நன்றி.", "நன்றி தெரிவித்தல்", "Thank you"),
                        SpokenSentence(17, "Thanks for calling.", "அழைத்ததற்கு நன்றி.", "நன்றி தெரிவித்தல்", "Thank you"),
                        SpokenSentence(18, "Thank you for waiting.", "காத்திருந்ததற்கு நன்றி.", "நன்றி தெரிவித்தல்", "Thank you"),
                        SpokenSentence(19, "I really appreciate it.", "இதை நான் மிகவும் பாராட்டுகிறேன்.", "நன்றி தெரிவித்தல்", "Appreciation"),
                        SpokenSentence(20, "That's very kind of you.", "நீங்கள் மிகவும் அன்பாக நடந்துகொள்கிறீர்கள்.", "நன்றி தெரிவித்தல்", "Appreciation"),
                        SpokenSentence(21, "I'm sorry.", "என்னை மன்னியுங்கள்.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(22, "I'm sorry I'm late.", "தாமதமாக வந்ததற்கு மன்னிக்கவும்.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(23, "Sorry, I forgot.", "மன்னிக்கவும், நான் மறந்துவிட்டேன்.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(24, "Sorry, I made a mistake.", "மன்னிக்கவும், நான் தவறு செய்துவிட்டேன்.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(25, "I'm sorry for hurting you.", "உங்களைப் புண்படுத்தியதற்கு மன்னிக்கவும்.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(26, "Sorry to disturb you.", "உங்களைத் தொந்தரவு செய்ததற்கு மன்னிக்கவும்.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(27, "It was my mistake.", "அது என் தவறு.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(28, "Please forgive me.", "தயவுசெய்து என்னை மன்னியுங்கள்.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(29, "I didn't mean to do that.", "நான் வேண்டுமென்றே அப்படிச் செய்யவில்லை.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(30, "It won't happen again.", "இது மீண்டும் நடக்காது.", "மன்னிப்பு கேட்டல்", "Sorry"),
                        SpokenSentence(31, "Excuse me, please.", "மன்னிக்கவும் / கவனியுங்கள்.", "கவனம் ஈர்த்தல்", "Excuse me"),
                        SpokenSentence(32, "Excuse me, may I come in?", "மன்னிக்கவும், நான் உள்ளே வரலாமா?", "அனுமதி கேட்டல்", "Excuse me"),
                        SpokenSentence(33, "Excuse me, where is the bus stop?", "மன்னிக்கவும், பேருந்து நிறுத்தம் எங்கே?", "வழிகேட்டல்", "Excuse me"),
                        SpokenSentence(34, "Excuse me, can you help me?", "மன்னிக்கவும், எனக்கு உதவ முடியுமா?", "உதவி கேட்டல்", "Excuse me"),
                        SpokenSentence(35, "Pardon me, could you repeat that?", "மன்னிக்கவும், அதை மீண்டும் சொல்ல முடியுமா?", "மீண்டும் கேட்டல்", "Pardon"),
                        SpokenSentence(36, "You're welcome.", "பரவாயில்லை / மகிழ்ச்சி.", "பதில் அளித்தல்", "Welcome"),
                        SpokenSentence(37, "No problem.", "பிரச்சினையில்லை.", "பதில் அளித்தல்", "Welcome"),
                        SpokenSentence(38, "It's my pleasure.", "மகிழ்ச்சியுடன் செய்தேன்.", "பதில் அளித்தல்", "Welcome"),
                        SpokenSentence(39, "May I ask you a question?", "நான் உங்களிடம் ஒரு கேள்வி கேட்கலாமா?", "பணிவான கேள்வி", "May I"),
                        SpokenSentence(40, "Could you please explain this?", "தயவுசெய்து இதை விளக்க முடியுமா?", "பணிவான வேண்டுகோள்", "Could you")
                    )
                ),

                // 4. Pronouns
                SpokenChapter(
                    id = "b1_c4",
                    number = 4,
                    title = "Pronouns",
                    tamilTitle = "சுட்டுப்பெயர்கள் (Pronouns)",
                    description = "நபர்களைக் குறிக்கும் சுட்டுப்பெயர்கள் (I, We, You, He, She, It, They) மற்றும் பயிற்சிகள்.",
                    badge = "30 வாக்கியங்கள்",
                    sentences = listOf(
                        SpokenSentence(1, "I am a student.", "நான் ஒரு மாணவர்.", "I - நான்", "I"),
                        SpokenSentence(2, "I am happy.", "நான் மகிழ்ச்சியாக இருக்கிறேன்.", "I - நான்", "I"),
                        SpokenSentence(3, "I am ready.", "நான் தயாராக இருக்கிறேன்.", "I - நான்", "I"),
                        SpokenSentence(4, "I like English.", "எனக்கு ஆங்கிலம் பிடிக்கும்.", "I - நான்", "I"),
                        SpokenSentence(5, "I live in Tamil Nadu.", "நான் தமிழ்நாட்டில் வசிக்கிறேன்.", "I - நான்", "I"),
                        SpokenSentence(6, "We are friends.", "நாங்கள் நண்பர்கள்.", "We - நாம் / நாங்கள்", "We"),
                        SpokenSentence(7, "We study together.", "நாங்கள் ஒன்றாகப் படிக்கிறோம்.", "We - நாம் / நாங்கள்", "We"),
                        SpokenSentence(8, "We are learning English.", "நாங்கள் ஆங்கிலம் கற்றுக்கொண்டிருக்கிறோம்.", "We - நாம் / நாங்கள்", "We"),
                        SpokenSentence(9, "We can do it.", "எங்களால் இதைச் செய்ய முடியும்.", "We - நாம் / நாங்கள்", "We"),
                        SpokenSentence(10, "We will come tomorrow.", "நாங்கள் நாளை வருவோம்.", "We - நாம் / நாங்கள்", "We"),
                        SpokenSentence(11, "You are my friend.", "நீ என் நண்பன் / நண்பி.", "You - நீ / நீங்கள்", "You"),
                        SpokenSentence(12, "You are very kind.", "நீங்கள் மிகவும் அன்பானவர்.", "You - நீ / நீங்கள்", "You"),
                        SpokenSentence(13, "You can learn English.", "உங்களால் ஆங்கிலம் கற்க முடியும்.", "You - நீ / நீங்கள்", "You"),
                        SpokenSentence(14, "You look happy.", "நீ மகிழ்ச்சியாகத் தெரிகிறாய்.", "You - நீ / நீங்கள்", "You"),
                        SpokenSentence(15, "You speak well.", "நீ நன்றாகப் பேசுகிறாய்.", "You - நீ / நீங்கள்", "You"),
                        SpokenSentence(16, "He is my brother.", "அவன் என் சகோதரன்.", "He - அவன் / அவர்", "He"),
                        SpokenSentence(17, "He plays cricket.", "அவன் கிரிக்கெட் விளையாடுகிறான்.", "He - அவன் / அவர்", "He"),
                        SpokenSentence(18, "He goes to school.", "அவன் பள்ளிக்குச் செல்கிறான்.", "He - அவன் / அவர்", "He"),
                        SpokenSentence(19, "He speaks English.", "அவன் ஆங்கிலம் பேசுகிறான்.", "He - அவன் / அவர்", "He"),
                        SpokenSentence(20, "He is at home.", "அவன் வீட்டில் இருக்கிறான்.", "He - அவன் / அவர்", "He"),
                        SpokenSentence(21, "She is my friend.", "அவள் என் தோழி.", "She - அவள் / அவர்", "She"),
                        SpokenSentence(22, "She reads books.", "அவள் புத்தகங்களைப் படிக்கிறாள்.", "She - அவள் / அவர்", "She"),
                        SpokenSentence(23, "She sings well.", "அவள் நன்றாகப் பாடுகிறாள்.", "She - அவள் / அவர்", "She"),
                        SpokenSentence(24, "She is studying now.", "அவள் இப்போது படித்துக்கொண்டிருக்கிறாள்.", "She - அவள் / அவர்", "She"),
                        SpokenSentence(25, "She can help you.", "அவளால் உனக்கு உதவ முடியும்.", "She - அவள் / அவர்", "She"),
                        SpokenSentence(26, "It is raining.", "மழை பெய்துகொண்டிருக்கிறது.", "It - அது", "It"),
                        SpokenSentence(27, "It is very hot.", "மிகவும் வெப்பமாக இருக்கிறது.", "It - அது", "It"),
                        SpokenSentence(28, "They are my friends.", "அவர்கள் என் நண்பர்கள்.", "They - அவர்கள் / அவை", "They"),
                        SpokenSentence(29, "They play together.", "அவர்கள் ஒன்றாக விளையாடுகிறார்கள்.", "They - அவர்கள் / அவை", "They"),
                        SpokenSentence(30, "These are my books.", "இவை என் புத்தகங்கள்.", "These - இவை", "These")
                    )
                ),

                // 5. Sentence Starters
                SpokenChapter(
                    id = "b1_c5",
                    number = 5,
                    title = "Sentence Starters",
                    tamilTitle = "வாக்கியத் தொடக்கங்கள்",
                    description = "I want, I need, I can, I feel போன்ற சொற்றொடர்களைக் கொண்டு புதிய வாக்கியங்களை உருவாக்குதல்.",
                    badge = "40 வாக்கியங்கள்",
                    sentences = listOf(
                        SpokenSentence(1, "I want water.", "எனக்குத் தண்ணீர் வேண்டும்.", "I want...", "I want"),
                        SpokenSentence(2, "I want a book.", "எனக்கு ஒரு புத்தகம் வேண்டும்.", "I want...", "I want"),
                        SpokenSentence(3, "I want to learn English.", "நான் ஆங்கிலம் கற்க விரும்புகிறேன்.", "I want...", "I want"),
                        SpokenSentence(4, "I want to go home.", "நான் வீட்டிற்குச் செல்ல விரும்புகிறேன்.", "I want...", "I want"),
                        SpokenSentence(5, "I want to speak confidently.", "நான் தன்னம்பிக்கையுடன் பேச விரும்புகிறேன்.", "I want...", "I want"),
                        SpokenSentence(6, "I need help.", "எனக்கு உதவி தேவை.", "I need...", "I need"),
                        SpokenSentence(7, "I need more time.", "எனக்கு கூடுதல் நேரம் தேவை.", "I need...", "I need"),
                        SpokenSentence(8, "I need a pen.", "எனக்கு ஒரு பேனா தேவை.", "I need...", "I need"),
                        SpokenSentence(9, "I need some rest.", "எனக்குச் சிறிது ஓய்வு தேவை.", "I need...", "I need"),
                        SpokenSentence(10, "I have a pen.", "என்னிடம் ஒரு பேனா உள்ளது.", "I have...", "I have"),
                        SpokenSentence(11, "I have a question.", "என்னிடம் ஒரு கேள்வி உள்ளது.", "I have...", "I have"),
                        SpokenSentence(12, "I don't have money.", "என்னிடம் பணம் இல்லை.", "I don't have...", "Negative"),
                        SpokenSentence(13, "I can swim.", "என்னால் நீந்த முடியும்.", "I can...", "Ability"),
                        SpokenSentence(14, "I can speak English.", "என்னால் ஆங்கிலம் பேச முடியும்.", "I can...", "Ability"),
                        SpokenSentence(15, "I can solve this problem.", "என்னால் இந்தக் கணக்கைத் தீர்க்க முடியும்.", "I can...", "Ability"),
                        SpokenSentence(16, "I can't drive.", "என்னால் வாகனம் ஓட்ட முடியாது.", "I can't...", "Inability"),
                        SpokenSentence(17, "I like music.", "எனக்கு இசை பிடிக்கும்.", "I like...", "Preference"),
                        SpokenSentence(18, "I like drawing pictures.", "எனக்கு படங்கள் வரைய பிடிக்கும்.", "I like...", "Preference"),
                        SpokenSentence(19, "I don't like noise.", "எனக்குச் சத்தம் பிடிக்காது.", "I don't like...", "Dislike"),
                        SpokenSentence(20, "I know the answer.", "எனக்குத் பதில் தெரியும்.", "I know...", "Knowledge"),
                        SpokenSentence(21, "I know how to do it.", "இதை எப்படிச் செய்வது என்று எனக்குத் தெரியும்.", "I know...", "Knowledge"),
                        SpokenSentence(22, "I think so.", "அப்படியென்று நான் நினைக்கிறேன்.", "I think...", "Opinion"),
                        SpokenSentence(23, "I think it is right.", "அது சரி என்று நினைக்கிறேன்.", "I think...", "Opinion"),
                        SpokenSentence(24, "I feel happy.", "நான் மகிழ்ச்சியாக உணர்கிறேன்.", "I feel...", "Emotion"),
                        SpokenSentence(25, "I feel tired.", "நான் சோர்வாக உணர்கிறேன்.", "I feel...", "Emotion"),
                        SpokenSentence(26, "I feel proud of you.", "உன்னை நினைத்துப் பெருமையாக உணர்கிறேன்.", "I feel...", "Emotion"),
                        SpokenSentence(27, "I am going to study.", "நான் படிக்கப் போகிறேன்.", "I am going to...", "Future Plan"),
                        SpokenSentence(28, "I am going to sleep.", "நான் தூங்கப் போகிறேன்.", "I am going to...", "Future Plan"),
                        SpokenSentence(29, "I would like some tea.", "எனக்குக் கொஞ்சம் தேநீர் வேண்டும்.", "I would like...", "Polite"),
                        SpokenSentence(30, "I would like to join you.", "நான் உங்களுடன் இணைய விரும்புகிறேன்.", "I would like...", "Polite"),
                        SpokenSentence(31, "I have to work.", "நான் வேலை செய்ய வேண்டும்.", "I have to...", "Obligation"),
                        SpokenSentence(32, "I have to leave now.", "நான் இப்போது கிளம்ப வேண்டும்.", "I have to...", "Obligation"),
                        SpokenSentence(33, "Let me try.", "நான் முயற்சி செய்யட்டும்.", "Let me...", "Permission"),
                        SpokenSentence(34, "Let me help you.", "நான் உங்களுக்கு உதவட்டும்.", "Let me...", "Offer"),
                        SpokenSentence(35, "Let me see.", "நான் பார்க்கிறேன்.", "Let me...", "Action"),
                        SpokenSentence(36, "I am ready to go.", "நான் செல்லத் தயாராக இருக்கிறேன்.", "I am ready...", "Prepared"),
                        SpokenSentence(37, "I hope you are well.", "நீங்கள் நலமாக இருப்பீர்கள் என நம்புகிறேன்.", "I hope...", "Hope"),
                        SpokenSentence(38, "I promise I will come.", "நான் வருவேன் என்று வாக்களிக்கிறேன்.", "I promise...", "Promise"),
                        SpokenSentence(39, "I forgot to tell you.", "உங்களிடம் சொல்ல மறந்துவிட்டேன்.", "I forgot...", "Past"),
                        SpokenSentence(40, "I remember that day.", "அந்த நாள் எனக்கு நினைவிருக்கிறது.", "I remember...", "Memory")
                    )
                ),

                // 6. WH Questions & Answers
                SpokenChapter(
                    id = "b1_c6",
                    number = 6,
                    title = "WH Questions & Answers",
                    tamilTitle = "அன்றாடக் கேள்விகள் மற்றும் பதில்கள்",
                    description = "What, Where, When, Who, Why, How கொண்டு கேள்வி கேட்டு பதில் சொல்லுதல்.",
                    badge = "30 வினா-விடைகள்",
                    sentences = listOf(
                        SpokenSentence(1, "What is your name? — My name is Ravi.", "உன் பெயர் என்ன? — என் பெயர் ரவி.", "What", "Question"),
                        SpokenSentence(2, "Where do you live? — I live in Chennai.", "நீ எங்கே வசிக்கிறாய்? — நான் சென்னையில் வசிக்கிறேன்.", "Where", "Question"),
                        SpokenSentence(3, "How are you? — I am fine, thank you.", "நீ எப்படி இருக்கிறாய்? — நான் நலமாக இருக்கிறேன், நன்றி.", "How", "Question"),
                        SpokenSentence(4, "Who is your best friend? — Arun is my best friend.", "உன் சிறந்த நண்பர் யார்? — அருண் என் சிறந்த நண்பர்.", "Who", "Question"),
                        SpokenSentence(5, "When do you wake up? — I wake up at six.", "நீ எப்போது விழித்தெழுகிறாய்? — நான் 6 மணிக்கு எழுகிறேன்.", "When", "Question"),
                        SpokenSentence(6, "Why are you late? — I missed the bus.", "நீ ஏன் தாமதமாக வந்தாய்? — நான் பேருந்தைத் தவறவிட்டேன்.", "Why", "Question"),
                        SpokenSentence(7, "What are you doing? — I am studying.", "நீ என்ன செய்கிறாய்? — நான் படித்துக்கொண்டிருக்கிறேன்.", "What", "Question"),
                        SpokenSentence(8, "Where are you going? — I am going home.", "நீ எங்கே போகிறாய்? — நான் வீட்டிற்குப் போகிறேன்.", "Where", "Question"),
                        SpokenSentence(9, "When is your birthday? — It is in June.", "உன் பிறந்தநாள் எப்போது? — ஜூன் மாதத்தில்.", "When", "Question"),
                        SpokenSentence(10, "Who is your teacher? — My teacher is Subiksha.", "உன் ஆசிரியர் யார்? — என் ஆசிரியர் சுபிக்ஷா.", "Who", "Question"),
                        SpokenSentence(11, "Why are you laughing? — Because I am happy.", "நீ ஏன் சிரிக்கிறாய்? — நான் மகிழ்ச்சியாக இருப்பதால்.", "Why", "Question"),
                        SpokenSentence(12, "How do you go to school? — I go by bus.", "நீ எப்படி பள்ளிக்குச் செல்கிறாய்? — நான் பேருந்தில் செல்கிறேன்.", "How", "Question"),
                        SpokenSentence(13, "What do you like? — I like music.", "உனக்கு என்ன பிடிக்கும்? — எனக்கு இசை பிடிக்கும்.", "What", "Question"),
                        SpokenSentence(14, "Where is my book? — It is on the table.", "என் புத்தகம் எங்கே? — அது மேஜையின் மேல் உள்ளது.", "Where", "Question"),
                        SpokenSentence(15, "When will you return? — I will return tomorrow.", "நீ எப்போது திரும்புவாய்? — நான் நாளை திரும்புவேன்.", "When", "Question"),
                        SpokenSentence(16, "Who called you? — My friend called me.", "உன்னை யார் அழைத்தது? — என் நண்பர் அழைத்தார்.", "Who", "Question"),
                        SpokenSentence(17, "Why are you crying? — I am feeling sad.", "நீ ஏன் அழுகிறாய்? — எனக்கு வருத்தமாக இருக்கிறது.", "Why", "Question"),
                        SpokenSentence(18, "How old are you? — I am twelve years old.", "உன் வயது என்ன? — எனக்கு பன்னிரண்டு வயது.", "How", "Question"),
                        SpokenSentence(19, "Which colour do you like? — I like blue.", "உனக்கு எந்த நிறம் பிடிக்கும்? — எனக்கு நீல நிறம் பிடிக்கும்.", "Which", "Question"),
                        SpokenSentence(20, "Whose pen is this? — It is my pen.", "இது யாருடைய பேனா? — இது என் பேனா.", "Whose", "Question"),
                        SpokenSentence(21, "How much is this bag? — It is five hundred rupees.", "இந்தப் பை எவ்வளவு விலை? — இது ஐநூறு ரூபாய்.", "How much", "Question"),
                        SpokenSentence(22, "How many sisters do you have? — I have two sisters.", "உனக்கு எத்தனை சகோதரிகள் உள்ளனர்? — எனக்கு இரு சகோதரிகள்.", "How many", "Question"),
                        SpokenSentence(23, "What is your hobby? — My hobby is reading.", "உன் பொழுதுபோக்கு என்ன? — என் பொழுதுபோக்கு வாசித்தல்.", "What", "Question"),
                        SpokenSentence(24, "Where is the bus stop? — It is near the school.", "பேருந்து நிறுத்தம் எங்கே? — அது பள்ளிக்கு அருகில் உள்ளது.", "Where", "Question"),
                        SpokenSentence(25, "When does the class start? — It starts at nine.", "வகுப்பு எப்போது தொடங்குகிறது? — ஒன்பது மணிக்குத் தொடங்குகிறது.", "When", "Question"),
                        SpokenSentence(26, "Who is at the door? — My friend is at the door.", "கதவருகில் யார் இருப்பது? — என் நண்பர் இருக்கிறார்.", "Who", "Question"),
                        SpokenSentence(27, "Why do you learn English? — I want to speak confidently.", "நீ ஏன் ஆங்கிலம் கற்கிறாய்? — தன்னம்பிக்கையுடன் பேச விரும்புகிறேன்.", "Why", "Question"),
                        SpokenSentence(28, "How was your day? — It was very good.", "உன் நாள் எப்படி இருந்தது? — மிகவும் நன்றாக இருந்தது.", "How", "Question"),
                        SpokenSentence(29, "Which subject do you like? — I like English.", "உனக்கு எந்தப் பாடம் பிடிக்கும்? — எனக்கு ஆங்கிலம் பிடிக்கும்.", "Which", "Question"),
                        SpokenSentence(30, "What will you do tomorrow? — I will practise English.", "நாளை என்ன செய்வாய்? — நான் ஆங்கிலப் பயிற்சி செய்வேன்.", "What", "Question")
                    )
                ),

                // 7. Simple Sentence Formation
                SpokenChapter(
                    id = "b1_c7",
                    number = 7,
                    title = "Simple Sentence Formation",
                    tamilTitle = "எளிய வாக்கிய அமைப்பு",
                    description = "Subject + Verb + Object மற்றும் Subject + Am/Is/Are அமைப்புகள்.",
                    badge = "40 வாக்கியங்கள்",
                    sentences = listOf(
                        SpokenSentence(1, "Open the door.", "கதவைத் திற.", "Imperative", "Order"),
                        SpokenSentence(2, "Close the window.", "ஜன்னலை மூடு.", "Imperative", "Order"),
                        SpokenSentence(3, "Give me that book.", "அந்தப் புத்தகத்தை எனக்குக் கொடு.", "Imperative", "Request"),
                        SpokenSentence(4, "Don't worry.", "கவலைப்படாதே.", "Negative", "Advice"),
                        SpokenSentence(5, "Wait for me.", "எனக்காகக் காத்திரு.", "Imperative", "Request"),
                        SpokenSentence(6, "I will speak confidently.", "நான் தன்னம்பிக்கையுடன் பேசுவேன்.", "Future", "Statement"),
                        SpokenSentence(7, "I read every day.", "நான் தினமும் படிக்கிறேன்.", "Present Habit", "Statement"),
                        SpokenSentence(8, "She cooks well.", "அவள் நன்றாகச் சமைக்கிறாள்.", "Present Habit", "Statement"),
                        SpokenSentence(9, "He drives carefully.", "அவன் கவனமாக வாகனம் ஓட்டுகிறான்.", "Present Habit", "Statement"),
                        SpokenSentence(10, "We play together.", "நாங்கள் ஒன்றாக விளையாடுகிறோம்.", "Present", "Statement"),
                        SpokenSentence(11, "They speak English.", "அவர்கள் ஆங்கிலம் பேசுகிறார்கள்.", "Present", "Statement"),
                        SpokenSentence(12, "My mother is cooking.", "என் அம்மா சமைத்துக்கொண்டிருக்கிறார்.", "Continuous", "Present"),
                        SpokenSentence(13, "My father is working.", "என் அப்பா வேலை செய்துகொண்டிருக்கிறார்.", "Continuous", "Present"),
                        SpokenSentence(14, "The baby is sleeping.", "குழந்தை தூங்கிக்கொண்டிருக்கிறது.", "Continuous", "Present"),
                        SpokenSentence(15, "The bus is coming.", "பேருந்து வந்துகொண்டிருக்கிறது.", "Continuous", "Present"),
                        SpokenSentence(16, "I need your help.", "எனக்கு உன் உதவி தேவை.", "Present", "Need"),
                        SpokenSentence(17, "She wants a new dress.", "அவளுக்கு ஒரு புதிய ஆடை வேண்டும்.", "Present", "Want"),
                        SpokenSentence(18, "He has a bicycle.", "அவனிடம் ஒரு மிதிவண்டி இருக்கிறது.", "Possession", "Has"),
                        SpokenSentence(19, "We have enough time.", "எங்களிடம் போதுமான நேரம் இருக்கிறது.", "Possession", "Have"),
                        SpokenSentence(20, "They have a beautiful house.", "அவர்களுக்கு அழகான வீடு இருக்கிறது.", "Possession", "Have"),
                        SpokenSentence(21, "I don't know.", "எனக்குத் தெரியாது.", "Negative", "Present"),
                        SpokenSentence(22, "I don't understand.", "எனக்குப் புரியவில்லை.", "Negative", "Present"),
                        SpokenSentence(23, "She doesn't like coffee.", "அவளுக்குக் காபி பிடிக்காது.", "Negative", "Present"),
                        SpokenSentence(24, "He doesn't speak loudly.", "அவன் சத்தமாகப் பேசுவதில்லை.", "Negative", "Present"),
                        SpokenSentence(25, "We don't waste time.", "நாங்கள் நேரத்தை வீணாக்குவதில்லை.", "Negative", "Present"),
                        SpokenSentence(26, "Do you speak English?", "உனக்கு ஆங்கிலம் பேசத் தெரியுமா?", "Question", "Do"),
                        SpokenSentence(27, "Are you ready?", "நீ தயாராக இருக்கிறாயா?", "Question", "Be"),
                        SpokenSentence(28, "Is she at home?", "அவள் வீட்டில் இருக்கிறாளா?", "Question", "Be"),
                        SpokenSentence(29, "Can you help me?", "எனக்கு உதவ முடியுமா?", "Question", "Can"),
                        SpokenSentence(30, "Will you come tomorrow?", "நாளை வருவாயா?", "Question", "Will"),
                        SpokenSentence(31, "Please open your book.", "தயவுசெய்து உன் புத்தகத்தைத் திற.", "Polite", "Request"),
                        SpokenSentence(32, "Please speak clearly.", "தயவுசெய்து தெளிவாகப் பேசு.", "Polite", "Request"),
                        SpokenSentence(33, "Don't make noise.", "சத்தம் போடாதே.", "Negative", "Order"),
                        SpokenSentence(34, "Don't touch that.", "அதைத் தொடாதே.", "Negative", "Order"),
                        SpokenSentence(35, "Keep the room clean.", "அறையைச் சுத்தமாக வைத்திரு.", "Imperative", "Order"),
                        SpokenSentence(36, "I am learning English.", "நான் ஆங்கிலம் கற்றுக்கொண்டிருக்கிறேன்.", "Continuous", "Present"),
                        SpokenSentence(37, "She is writing a letter.", "அவள் கடிதம் எழுதிக்கொண்டிருக்கிறாள்.", "Continuous", "Present"),
                        SpokenSentence(38, "He went to school.", "அவன் பள்ளிக்குச் சென்றான்.", "Past", "Statement"),
                        SpokenSentence(39, "We will visit tomorrow.", "நாங்கள் நாளைச் சந்திக்க வருவோம்.", "Future", "Statement"),
                        SpokenSentence(40, "Practice makes you better.", "பயிற்சி உன்னை மேலும் சிறந்தவராக்கும்.", "Proverb", "Wisdom")
                    )
                ),

                // 8. Helping Verbs
                SpokenChapter(
                    id = "b1_c8",
                    number = 8,
                    title = "Helping Verbs",
                    tamilTitle = "துணை வினைச்சொற்கள் (Auxiliary Verbs)",
                    description = "Am, Is, Are, Was, Were, Do, Does, Did, Have, Has, Had, Will, Can போன்றவற்றின் சரியான பயன்பாடுகள்.",
                    badge = "30 வாக்கியங்கள்",
                    sentences = listOf(
                        SpokenSentence(1, "I am happy.", "நான் மகிழ்ச்சியாக இருக்கிறேன்.", "am", "Be Verbs"),
                        SpokenSentence(2, "She is busy.", "அவள் வேலையாக இருக்கிறாள்.", "is", "Be Verbs"),
                        SpokenSentence(3, "They are ready.", "அவர்கள் தயாராக இருக்கிறார்கள்.", "are", "Be Verbs"),
                        SpokenSentence(4, "I was tired.", "நான் சோர்வாக இருந்தேன்.", "was", "Past Be"),
                        SpokenSentence(5, "We were late.", "நாங்கள் தாமதமாக வந்தோம்.", "were", "Past Be"),
                        SpokenSentence(6, "Do you know me?", "உனக்கு என்னைத் தெரியுமா?", "do", "Do Verbs"),
                        SpokenSentence(7, "Does he work here?", "அவன் இங்கே வேலை செய்கிறானா?", "does", "Do Verbs"),
                        SpokenSentence(8, "Did you call me?", "நீ என்னை அழைத்தாயா?", "did", "Past Do"),
                        SpokenSentence(9, "I have a book.", "என்னிடம் ஒரு புத்தகம் உள்ளது.", "have", "Have Verbs"),
                        SpokenSentence(10, "She has a car.", "அவளிடம் ஒரு கார் உள்ளது.", "has", "Have Verbs"),
                        SpokenSentence(11, "We had a good time.", "நாங்கள் மகிழ்ச்சியாக நேரத்தைக் கழித்தோம்.", "had", "Past Have"),
                        SpokenSentence(12, "I will come.", "நான் வருவேன்.", "will", "Modal"),
                        SpokenSentence(13, "She will help you.", "அவள் உனக்கு உதவுவாள்.", "will", "Modal"),
                        SpokenSentence(14, "I can swim.", "என்னால் நீந்த முடியும்.", "can", "Modal"),
                        SpokenSentence(15, "Can you drive?", "உன்னால் வாகனம் ஓட்ட முடியுமா?", "can", "Modal"),
                        SpokenSentence(16, "Could you help me?", "எனக்கு உதவ முடியுமா?", "could", "Modal"),
                        SpokenSentence(17, "You should study.", "நீ படிக்க வேண்டும்.", "should", "Modal"),
                        SpokenSentence(18, "You must be careful.", "நீ கவனமாக இருக்க வேண்டும்.", "must", "Modal"),
                        SpokenSentence(19, "I cannot hear you.", "உன் குரல் எனக்குக் கேட்கவில்லை.", "cannot", "Negative Modal"),
                        SpokenSentence(20, "He is not ready.", "அவன் தயாராக இல்லை.", "is not", "Negative Be"),
                        SpokenSentence(21, "They are not here.", "அவர்கள் இங்கே இல்லை.", "are not", "Negative Be"),
                        SpokenSentence(22, "I do not smoke.", "நான் புகைபிடிப்பதில்லை.", "do not", "Negative Do"),
                        SpokenSentence(23, "She does not eat meat.", "அவள் இறைச்சி சாப்பிடுவதில்லை.", "does not", "Negative Do"),
                        SpokenSentence(24, "I did not go.", "நான் செல்லவில்லை.", "did not", "Past Negative"),
                        SpokenSentence(25, "We will not forget.", "நாங்கள் மறக்க மாட்டோம்.", "will not", "Future Negative"),
                        SpokenSentence(26, "Have you finished?", "நீ முடித்துவிட்டாயா?", "have", "Perfect Question"),
                        SpokenSentence(27, "Has she arrived?", "அவள் வந்துவிட்டாளா?", "has", "Perfect Question"),
                        SpokenSentence(28, "Had you seen it before?", "நீ இதற்கு முன்பு அதைப் பார்த்திருந்தாயா?", "had", "Past Perfect Question"),
                        SpokenSentence(29, "May I come in?", "நான் உள்ளே வரலாமா?", "may", "Permission"),
                        SpokenSentence(30, "You must practise daily.", "நீ தினமும் பயிற்சி செய்ய வேண்டும்.", "must", "Obligation")
                    )
                ),

                // 9. Classroom English
                SpokenChapter(
                    id = "b1_c9",
                    number = 9,
                    title = "Classroom English",
                    tamilTitle = "வகுப்பறை ஆங்கிலம்",
                    description = "ஆசிரியர்களுடனும் மாணவர்களுடனும் வகுப்பறையில் உரையாடும் வாக்கியங்கள்.",
                    badge = "40 வாக்கியங்கள்",
                    sentences = listOf(
                        SpokenSentence(1, "May I come in, Teacher?", "ஆசிரியரே, நான் உள்ளே வரலாமா?", "அனுமதி", "Classroom"),
                        SpokenSentence(2, "May I go out?", "நான் வெளியே செல்லலாமா?", "அனுமதி", "Classroom"),
                        SpokenSentence(3, "May I drink some water?", "நான் கொஞ்சம் தண்ணீர் குடிக்கலாமா?", "அனுமதி", "Classroom"),
                        SpokenSentence(4, "May I ask a question?", "நான் ஒரு கேள்வி கேட்கலாமா?", "சந்தேகம்", "Classroom"),
                        SpokenSentence(5, "I have a doubt.", "எனக்கு ஒரு சந்தேகம் உள்ளது.", "சந்தேகம்", "Classroom"),
                        SpokenSentence(6, "I don't understand this lesson.", "இந்தப் பாடம் எனக்குப் புரியவில்லை.", "புரிதல்", "Classroom"),
                        SpokenSentence(7, "Could you explain it again?", "இதை மீண்டும் விளக்க முடியுமா?", "விளக்கம்", "Classroom"),
                        SpokenSentence(8, "Please speak louder.", "தயவுசெய்து சற்று சத்தமாகப் பேசுங்கள்.", "ஒலி", "Classroom"),
                        SpokenSentence(9, "Please speak slowly.", "தயவுசெய்து மெதுவாகப் பேசுங்கள்.", "வேகம்", "Classroom"),
                        SpokenSentence(10, "I have finished my work.", "நான் என் வேலையை முடித்துவிட்டேன்.", "முடித்தல்", "Classroom"),
                        SpokenSentence(11, "I forgot my homework.", "நான் வீட்டுப்பாடத்தை மறந்துவிட்டேன்.", "மறதி", "Classroom"),
                        SpokenSentence(12, "Can I borrow your pen?", "உன் பேனாவைக் கடன் வாங்கலாமா?", "பகிர்வு", "Classroom"),
                        SpokenSentence(13, "Please share your book with me.", "உன் புத்தகத்தை என்னுடன் பகிர்ந்துகொள்.", "பகிர்வு", "Classroom"),
                        SpokenSentence(14, "What is the answer?", "பதில் என்ன?", "வினா", "Classroom"),
                        SpokenSentence(15, "I know the answer.", "எனக்குப் பதில் தெரியும்.", "விடை", "Classroom"),
                        SpokenSentence(16, "May I answer the question?", "நான் கேள்விக்குப் பதிலளிக்கலாமா?", "பதில்", "Classroom"),
                        SpokenSentence(17, "Please write it on the board.", "தயவுசெய்து அதை எழுத்துப் பலகையில் எழுதுங்கள்.", "கற்பித்தல்", "Classroom"),
                        SpokenSentence(18, "Which page should I read?", "நான் எந்தப் பக்கத்தைப் படிக்க வேண்டும்?", "பக்கம்", "Classroom"),
                        SpokenSentence(19, "When is the test?", "தேர்வு எப்போது?", "தேர்வு", "Classroom"),
                        SpokenSentence(20, "Is this correct?", "இது சரியா?", "சரிபார்த்தல்", "Classroom"),
                        SpokenSentence(21, "Please stand in a line.", "தயவுசெய்து வரிசையில் நில்லுங்கள்.", "ஒழுங்கு", "Classroom"),
                        SpokenSentence(22, "Keep silence in the class.", "வகுப்பில் அமைதி காக்கவும்.", "ஒழுங்கு", "Classroom"),
                        SpokenSentence(23, "Open your English textbook.", "உங்கள் ஆங்கிலப் பாடப்புத்தகத்தைத் திறக்கவும்.", "பாடம்", "Classroom"),
                        SpokenSentence(24, "Turn to page number 25.", "பக்கம் 25-க்குத் திருப்புங்கள்.", "பாடம்", "Classroom"),
                        SpokenSentence(25, "Read the first paragraph.", "முதல் பத்தியைப் படியுங்கள்.", "வாசிப்பு", "Classroom"),
                        SpokenSentence(26, "Note down important points.", "முக்கியக் குறிப்புகளைக் குறித்துக்கொள்ளுங்கள்.", "எழுதுதல்", "Classroom"),
                        SpokenSentence(27, "Submit your notebooks tomorrow.", "உங்கள் குறிப்பேடுகளை நாளை சமர்ப்பியுங்கள்.", "வீட்டுப்பாடம்", "Classroom"),
                        SpokenSentence(28, "Who is absent today?", "இன்று யார் வரவில்லை?", "வருகை", "Classroom"),
                        SpokenSentence(29, "He is on leave today.", "அவர் இன்று விடுப்பில் உள்ளார்.", "வருகை", "Classroom"),
                        SpokenSentence(30, "Clean the blackboard, please.", "கரும்பலகையைத் துடைத்துவிடுங்கள்.", "உதவி", "Classroom"),
                        SpokenSentence(31, "Don't scribble on the desk.", "மேஜையின் மீது கிறுக்காதீர்கள்.", "ஒழுங்கு", "Classroom"),
                        SpokenSentence(32, "Pay attention to the teacher.", "ஆசிரியர் சொல்வதைக் கவனியுங்கள்.", "கவனம்", "Classroom"),
                        SpokenSentence(33, "Who can explain this formula?", "இந்தச் சூத்திரத்தை யாரால் விளக்க முடியும்?", "கேள்வி", "Classroom"),
                        SpokenSentence(34, "Well done! Keep it up.", "நன்றாகச் செய்தாய்! தொடரட்டும்.", "பாராட்டு", "Classroom"),
                        SpokenSentence(35, "Try again, you can do it.", "மீண்டும் முயற்சி செய், உன்னால் முடியும்.", "ஊக்கம்", "Classroom"),
                        SpokenSentence(36, "Form groups of four.", "நான்கு பேர் கொண்ட குழுக்களாக இணையுங்கள்.", "குழுப்பணி", "Classroom"),
                        SpokenSentence(37, "Discuss with your partner.", "உங்கள் நண்பருடன் விவாதியுங்கள்.", "குழுப்பணி", "Classroom"),
                        SpokenSentence(38, "The bell has rung.", "மணி அடித்துவிட்டது.", "இடைவேளை", "Classroom"),
                        SpokenSentence(39, "Have a good weekend, students.", "இனிய வார இறுதி அமையட்டும் மாணவர்களே.", "வாழ்த்து", "Classroom"),
                        SpokenSentence(40, "Thank you, Teacher!", "நன்றி ஆசிரியரே!", "மரியாதை", "Classroom")
                    )
                ),

                // 10. 100 Daily Spoken English Sentences (Master Collection)
                SpokenChapter(
                    id = "b1_c10",
                    number = 10,
                    title = "100 Daily Sentences",
                    tamilTitle = "அன்றாடம் பயன்படுத்தும் 100 வாக்கியங்கள்",
                    description = "அடிப்படை உரையாடல், வீடு, பள்ளி, சமூகம், கடை, உணர்வுகள் - முழுமையான 100 வாக்கியத் தொகுப்பு.",
                    badge = "முழுமையான 100",
                    sentences = (1..100).map { idx ->
                        when (idx) {
                            1 -> SpokenSentence(1, "Hello!", "வணக்கம்!", "Conversation", "1-20 Basics")
                            2 -> SpokenSentence(2, "How are you?", "நீங்கள் எப்படி இருக்கிறீர்கள்?", "Conversation", "1-20 Basics")
                            3 -> SpokenSentence(3, "I am fine.", "நான் நலமாக இருக்கிறேன்.", "Conversation", "1-20 Basics")
                            4 -> SpokenSentence(4, "What is your name?", "உங்கள் பெயர் என்ன?", "Conversation", "1-20 Basics")
                            5 -> SpokenSentence(5, "My name is Subiksha.", "என் பெயர் சுபிக்ஷா.", "Conversation", "1-20 Basics")
                            6 -> SpokenSentence(6, "Nice to meet you.", "உங்களைச் சந்தித்ததில் மகிழ்ச்சி.", "Conversation", "1-20 Basics")
                            7 -> SpokenSentence(7, "Where are you from?", "நீங்கள் எங்கிருந்து வருகிறீர்கள்?", "Conversation", "1-20 Basics")
                            8 -> SpokenSentence(8, "I am from Tamil Nadu.", "நான் தமிழ்நாட்டைச் சேர்ந்தவர்.", "Conversation", "1-20 Basics")
                            9 -> SpokenSentence(9, "What are you doing?", "நீங்கள் என்ன செய்துகொண்டிருக்கிறீர்கள்?", "Conversation", "1-20 Basics")
                            10 -> SpokenSentence(10, "I am learning English.", "நான் ஆங்கிலம் கற்றுக்கொண்டிருக்கிறேன்.", "Conversation", "1-20 Basics")
                            11 -> SpokenSentence(11, "Do you speak English?", "உங்களுக்கு ஆங்கிலம் பேசத் தெரியுமா?", "Conversation", "1-20 Basics")
                            12 -> SpokenSentence(12, "A little bit.", "கொஞ்சம் தெரியும்.", "Conversation", "1-20 Basics")
                            13 -> SpokenSentence(13, "Please speak slowly.", "தயவுசெய்து மெதுவாகப் பேசுங்கள்.", "Conversation", "1-20 Basics")
                            14 -> SpokenSentence(14, "I don't understand.", "எனக்குப் புரியவில்லை.", "Conversation", "1-20 Basics")
                            15 -> SpokenSentence(15, "Please say that again.", "தயவுசெய்து அதை மீண்டும் சொல்லுங்கள்.", "Conversation", "1-20 Basics")
                            16 -> SpokenSentence(16, "What do you mean?", "நீங்கள் என்ன சொல்ல வருகிறீர்கள்?", "Conversation", "1-20 Basics")
                            17 -> SpokenSentence(17, "I understand now.", "இப்போது எனக்குப் புரிகிறது.", "Conversation", "1-20 Basics")
                            18 -> SpokenSentence(18, "That's right.", "அது சரிதான்.", "Conversation", "1-20 Basics")
                            19 -> SpokenSentence(19, "No problem.", "பிரச்சினையில்லை.", "Conversation", "1-20 Basics")
                            20 -> SpokenSentence(20, "See you later.", "பின்னர் சந்திப்போம்.", "Conversation", "1-20 Basics")
                            21 -> SpokenSentence(21, "Wake up early.", "சீக்கிரம் எழுந்திரு.", "Home", "21-40 Home")
                            22 -> SpokenSentence(22, "Get ready.", "தயாராகு.", "Home", "21-40 Home")
                            23 -> SpokenSentence(23, "Brush your teeth.", "பல் துலக்கு.", "Home", "21-40 Home")
                            24 -> SpokenSentence(24, "Wash your face.", "முகம் கழுவு.", "Home", "21-40 Home")
                            25 -> SpokenSentence(25, "Have your breakfast.", "காலை உணவைச் சாப்பிடு.", "Home", "21-40 Home")
                            26 -> SpokenSentence(26, "Drink some water.", "கொஞ்சம் தண்ணீர் குடி.", "Home", "21-40 Home")
                            27 -> SpokenSentence(27, "Where is my bag?", "என் பை எங்கே?", "Home", "21-40 Home")
                            28 -> SpokenSentence(28, "It is on the table.", "அது மேசையின் மீது இருக்கிறது.", "Home", "21-40 Home")
                            29 -> SpokenSentence(29, "Bring me a glass of water.", "எனக்கு ஒரு குவளை தண்ணீர் கொண்டுவா.", "Home", "21-40 Home")
                            30 -> SpokenSentence(30, "Please open the door.", "தயவுசெய்து கதவைத் திற.", "Home", "21-40 Home")
                            31 -> SpokenSentence(31, "Close the window.", "ஜன்னலை மூடு.", "Home", "21-40 Home")
                            32 -> SpokenSentence(32, "Turn on the fan.", "மின்விசிறியை இயக்கு.", "Home", "21-40 Home")
                            33 -> SpokenSentence(33, "Turn off the TV.", "தொலைக்காட்சியை அணை.", "Home", "21-40 Home")
                            34 -> SpokenSentence(34, "Where are you going?", "நீ எங்கே போகிறாய்?", "Home", "21-40 Home")
                            35 -> SpokenSentence(35, "I am going to the kitchen.", "நான் சமையலறைக்குச் செல்கிறேன்.", "Home", "21-40 Home")
                            36 -> SpokenSentence(36, "The food is ready.", "உணவு தயாராகிவிட்டது.", "Home", "21-40 Home")
                            37 -> SpokenSentence(37, "Come and eat.", "வந்து சாப்பிடு.", "Home", "21-40 Home")
                            38 -> SpokenSentence(38, "I am hungry.", "எனக்குப் பசிக்கிறது.", "Home", "21-40 Home")
                            39 -> SpokenSentence(39, "I am feeling sleepy.", "எனக்குத் தூக்கம் வருகிறது.", "Home", "21-40 Home")
                            40 -> SpokenSentence(40, "Good night.", "இரவு வணக்கம்.", "Home", "21-40 Home")
                            41 -> SpokenSentence(41, "May I come in, Teacher?", "ஆசிரியரே, நான் உள்ளே வரலாமா?", "School", "41-55 School")
                            42 -> SpokenSentence(42, "May I go out?", "நான் வெளியே செல்லலாமா?", "School", "41-55 School")
                            43 -> SpokenSentence(43, "Please teach me.", "தயவுசெய்து எனக்குக் கற்றுக்கொடுங்கள்.", "School", "41-55 School")
                            44 -> SpokenSentence(44, "I have a doubt.", "எனக்கு ஒரு சந்தேகம் இருக்கிறது.", "School", "41-55 School")
                            45 -> SpokenSentence(45, "I know the answer.", "எனக்குப் பதில் தெரியும்.", "School", "41-55 School")
                            46 -> SpokenSentence(46, "I don't know the answer.", "எனக்குப் பதில் தெரியவில்லை.", "School", "41-55 School")
                            47 -> SpokenSentence(47, "Please explain this.", "தயவுசெய்து இதை விளக்குங்கள்.", "School", "41-55 School")
                            48 -> SpokenSentence(48, "Can you repeat that?", "அதை மீண்டும் சொல்ல முடியுமா?", "School", "41-55 School")
                            49 -> SpokenSentence(49, "I have completed my homework.", "நான் வீட்டுப்பாடத்தை முடித்துவிட்டேன்.", "School", "41-55 School")
                            50 -> SpokenSentence(50, "I forgot my notebook.", "நான் என் குறிப்பேட்டை மறந்துவிட்டேன்.", "School", "41-55 School")
                            51 -> SpokenSentence(51, "Please give me a pen.", "தயவுசெய்து எனக்கு ஒரு பேனா கொடுங்கள்.", "School", "41-55 School")
                            52 -> SpokenSentence(52, "What page are we on?", "நாம் எந்தப் பக்கத்தில் இருக்கிறோம்?", "School", "41-55 School")
                            53 -> SpokenSentence(53, "Please read aloud.", "தயவுசெய்து சத்தமாகப் படியுங்கள்.", "School", "41-55 School")
                            54 -> SpokenSentence(54, "I will try again.", "நான் மீண்டும் முயற்சி செய்வேன்.", "School", "41-55 School")
                            55 -> SpokenSentence(55, "I can do it.", "என்னால் இதைச் செய்ய முடியும்.", "School", "41-55 School")
                            56 -> SpokenSentence(56, "How was your day?", "உன் நாள் எப்படி இருந்தது?", "Friends", "56-70 Friends")
                            57 -> SpokenSentence(57, "What are your plans?", "உன் திட்டங்கள் என்ன?", "Friends", "56-70 Friends")
                            58 -> SpokenSentence(58, "Shall we go together?", "நாம் ஒன்றாகச் செல்லலாமா?", "Friends", "56-70 Friends")
                            59 -> SpokenSentence(59, "Let's play.", "விளையாடலாம்.", "Friends", "56-70 Friends")
                            60 -> SpokenSentence(60, "Come with me.", "என்னுடன் வா.", "Friends", "56-70 Friends")
                            61 -> SpokenSentence(61, "Wait for me.", "எனக்காகக் காத்திரு.", "Friends", "56-70 Friends")
                            62 -> SpokenSentence(62, "I will call you later.", "நான் உன்னைப் பின்னர் அழைக்கிறேன்.", "Friends", "56-70 Friends")
                            63 -> SpokenSentence(63, "Call me when you are free.", "உனக்கு நேரம் கிடைக்கும்போது என்னை அழை.", "Friends", "56-70 Friends")
                            64 -> SpokenSentence(64, "What happened?", "என்ன நடந்தது?", "Friends", "56-70 Friends")
                            65 -> SpokenSentence(65, "Are you okay?", "நீ நலமாக இருக்கிறாயா?", "Friends", "56-70 Friends")
                            66 -> SpokenSentence(66, "Don't worry.", "கவலைப்படாதே.", "Friends", "56-70 Friends")
                            67 -> SpokenSentence(67, "Everything will be fine.", "எல்லாம் சரியாகிவிடும்.", "Friends", "56-70 Friends")
                            68 -> SpokenSentence(68, "That's a good idea.", "அது ஒரு நல்ல யோசனை.", "Friends", "56-70 Friends")
                            69 -> SpokenSentence(69, "I agree with you.", "நான் உன்னுடன் ஒத்துக்கொள்கிறேன்.", "Friends", "56-70 Friends")
                            70 -> SpokenSentence(70, "Let's meet tomorrow.", "நாளை சந்திப்போம்.", "Friends", "56-70 Friends")
                            71 -> SpokenSentence(71, "How much is this?", "இது எவ்வளவு?", "Shopping", "71-85 Out")
                            72 -> SpokenSentence(72, "It is too expensive.", "இது மிகவும் விலை அதிகம்.", "Shopping", "71-85 Out")
                            73 -> SpokenSentence(73, "Do you have a smaller size?", "இதைவிடச் சிறிய அளவு இருக்கிறதா?", "Shopping", "71-85 Out")
                            74 -> SpokenSentence(74, "I would like to buy this.", "இதை வாங்க விரும்புகிறேன்.", "Shopping", "71-85 Out")
                            75 -> SpokenSentence(75, "Can I pay by card?", "அட்டையைப் பயன்படுத்திப் பணம் செலுத்தலாமா?", "Shopping", "71-85 Out")
                            76 -> SpokenSentence(76, "Where is the bus stop?", "பேருந்து நிறுத்தம் எங்கே?", "Travel", "71-85 Out")
                            77 -> SpokenSentence(77, "When does the bus arrive?", "பேருந்து எப்போது வரும்?", "Travel", "71-85 Out")
                            78 -> SpokenSentence(78, "I need a ticket.", "எனக்கு ஒரு பயணச்சீட்டு வேண்டும்.", "Travel", "71-85 Out")
                            79 -> SpokenSentence(79, "Please stop here.", "தயவுசெய்து இங்கே நிறுத்துங்கள்.", "Travel", "71-85 Out")
                            80 -> SpokenSentence(80, "How long will it take?", "எவ்வளவு நேரம் ஆகும்?", "Travel", "71-85 Out")
                            81 -> SpokenSentence(81, "Is it far from here?", "இது இங்கிருந்து தொலைவில் இருக்கிறதா?", "Travel", "71-85 Out")
                            82 -> SpokenSentence(82, "Please show me the way.", "தயவுசெய்து வழியைக் காட்டுங்கள்.", "Travel", "71-85 Out")
                            83 -> SpokenSentence(83, "I am looking for this address.", "இந்த முகவரியைத் தேடிக்கொண்டிருக்கிறேன்.", "Travel", "71-85 Out")
                            84 -> SpokenSentence(84, "Can you help me find it?", "அதைக் கண்டுபிடிக்க எனக்கு உதவ முடியுமா?", "Travel", "71-85 Out")
                            85 -> SpokenSentence(85, "I have reached my destination.", "நான் செல்ல வேண்டிய இடத்தை அடைந்துவிட்டேன்.", "Travel", "71-85 Out")
                            86 -> SpokenSentence(86, "I am happy.", "நான் மகிழ்ச்சியாக இருக்கிறேன்.", "Feelings", "86-100 Feelings")
                            87 -> SpokenSentence(87, "I am tired.", "நான் சோர்வாக இருக்கிறேன்.", "Feelings", "86-100 Feelings")
                            88 -> SpokenSentence(88, "I am worried.", "நான் கவலையாக இருக்கிறேன்.", "Feelings", "86-100 Feelings")
                            89 -> SpokenSentence(89, "I feel better now.", "இப்போது நான் நன்றாக உணர்கிறேன்.", "Feelings", "86-100 Feelings")
                            90 -> SpokenSentence(90, "I need some rest.", "எனக்குச் சிறிது ஓய்வு தேவை.", "Feelings", "86-100 Feelings")
                            91 -> SpokenSentence(91, "I need your help.", "எனக்கு உங்கள் உதவி தேவை.", "Feelings", "86-100 Feelings")
                            92 -> SpokenSentence(92, "I am not feeling well.", "எனக்கு உடல்நிலை சரியாக இல்லை.", "Feelings", "86-100 Feelings")
                            93 -> SpokenSentence(93, "I am thirsty.", "எனக்குத் தாகமாக இருக்கிறது.", "Feelings", "86-100 Feelings")
                            94 -> SpokenSentence(94, "I am very excited.", "நான் மிகவும் உற்சாகமாக இருக்கிறேன்.", "Feelings", "86-100 Feelings")
                            95 -> SpokenSentence(95, "I am proud of you.", "உன்னை நினைத்து நான் பெருமைப்படுகிறேன்.", "Feelings", "86-100 Feelings")
                            96 -> SpokenSentence(96, "I will do my best.", "என்னால் முடிந்த சிறந்த முயற்சியைச் செய்வேன்.", "Motivation", "86-100 Feelings")
                            97 -> SpokenSentence(97, "Don't give up.", "முயற்சியைக் கைவிடாதே.", "Motivation", "86-100 Feelings")
                            98 -> SpokenSentence(98, "Believe in yourself.", "உன்மீது நம்பிக்கை வை.", "Motivation", "86-100 Feelings")
                            99 -> SpokenSentence(99, "I am improving every day.", "நான் ஒவ்வொரு நாளும் முன்னேறிக்கொண்டிருக்கிறேன்.", "Motivation", "86-100 Feelings")
                            else -> SpokenSentence(100, "I will speak English confidently.", "நான் தன்னம்பிக்கையுடன் ஆங்கிலம் பேசுவேன்.", "Motivation", "86-100 Feelings")
                        }
                    }
                ),

                // 11. Greetings & Self Introduction
                SpokenChapter(
                    id = "b1_c11",
                    number = 11,
                    title = "Greetings & Self Intro",
                    tamilTitle = "வாழ்த்துகள் மற்றும் சுய அறிமுகம்",
                    description = "காலை/மாலை வாழ்த்துகள், சுபிக்ஷா ஆசிரியர் மாதிரி சுய அறிமுகப் பேச்சுப் பயிற்சி.",
                    badge = "சுய அறிமுகம்",
                    sentences = listOf(
                        SpokenSentence(1, "Good morning!", "காலை வணக்கம்!", "வாழ்த்து", "Greetings"),
                        SpokenSentence(2, "Good afternoon!", "மதிய வணக்கம்!", "வாழ்த்து", "Greetings"),
                        SpokenSentence(3, "Good evening!", "மாலை வணக்கம்!", "வாழ்த்து", "Greetings"),
                        SpokenSentence(4, "Good night!", "இரவு வணக்கம்!", "வாழ்த்து", "Greetings"),
                        SpokenSentence(5, "How are you doing?", "நீங்கள் எப்படி இருக்கிறீர்கள்?", "விசாரிப்பு", "Greetings"),
                        SpokenSentence(6, "How is everything?", "எல்லாம் எப்படி இருக்கிறது?", "விசாரிப்பு", "Greetings"),
                        SpokenSentence(7, "I am doing well.", "நான் நன்றாக இருக்கிறேன்.", "விடை", "Greetings"),
                        SpokenSentence(8, "Have a nice day!", "இனிய நாளாக அமையட்டும்!", "வாழ்த்து", "Greetings"),
                        SpokenSentence(9, "Take care.", "உங்களைப் பார்த்துக்கொள்ளுங்கள்.", "விடைபெறுதல்", "Greetings"),
                        SpokenSentence(10, "See you soon.", "விரைவில் சந்திப்போம்.", "விடைபெறுதல்", "Greetings")
                    ),
                    challenges = listOf(
                        SpeechChallenge(
                            topicNumber = 1,
                            topic = "Self Introduction",
                            tamilTopic = "சுய அறிமுகம்",
                            englishSpeech = "Hello, everyone! My name is Subiksha. I am a student. I live in Tamil Nadu. I am learning English. My favourite subject is English. I like reading books and listening to music. My hobby is drawing. I want to speak English confidently. I practise English every day. Thank you for listening to me!",
                            tamilSpeech = "அனைவருக்கும் வணக்கம்! என் பெயர் சுபிக்ஷா. நான் ஒரு மாணவி. நான் தமிழ்நாட்டில் வசிக்கிறேன். நான் ஆங்கிலம் கற்றுக்கொண்டிருக்கிறேன். எனக்குப் பிடித்த பாடம் ஆங்கிலம். புத்தகங்கள் படிப்பதும் இசை கேட்பதும் எனக்குப் பிடிக்கும். ஓவியம் வரைவது என் பொழுதுபோக்கு. நான் தன்னம்பிக்கையுடன் ஆங்கிலம் பேச விரும்புகிறேன். தினமும் ஆங்கிலப் பயிற்சி செய்கிறேன். நான் பேசுவதைக் கேட்டதற்கு நன்றி!"
                        )
                    )
                ),

                // 12. 15-Minute Daily English Practice
                SpokenChapter(
                    id = "b1_c12",
                    number = 12,
                    title = "15-Min Daily Practice",
                    tamilTitle = "15 நிமிட தினசரிப் பயிற்சி முறை",
                    description = "5 நிமிடம் கவனித்துக் கேள் (Listen), 5 நிமிடம் படி (Read), 5 நிமிடம் பேசிப் பழகு (Speak).",
                    badge = "தினசரி முறை",
                    sentences = listOf(
                        SpokenSentence(1, "Step 1: Listen for 5 minutes.", "படி 1: ஆங்கில வாக்கியங்களைக் கவனித்துக் கேட்கவும்.", "கவனித்தல்", "15-Min Routine"),
                        SpokenSentence(2, "Step 2: Read along with Tamil meaning.", "படி 2: தமிழ் அர்த்தத்துடன் வாக்கியங்களை வாசிக்கவும்.", "வாசித்தல்", "15-Min Routine"),
                        SpokenSentence(3, "Step 3: Speak aloud with audio pronunciation.", "படி 3: ஆடியோ உச்சரிப்பைப் பின்பற்றி உரக்கப் பேசவும்.", "பேசுதல்", "15-Min Routine"),
                        SpokenSentence(4, "Practice consistently every single day.", "ஒவ்வொரு நாளும் தவறாமல் பயிற்சி செய்யவும்.", "தொடர்ச்சி", "15-Min Routine"),
                        SpokenSentence(5, "Confidence grows with daily repetition.", "தினசரி திரும்பத் திரும்பப் பேசுவதன் மூலம் தன்னம்பிக்கை உயரும்.", "தன்னம்பிக்கை", "15-Min Routine")
                    )
                ),

                // 13. Daily 1-Minute Speaking Challenge
                SpokenChapter(
                    id = "b1_c13",
                    number = 13,
                    title = "1-Minute Speaking Challenge",
                    tamilTitle = "ஒரு நிமிடப் பேச்சுப் பயிற்சி (15 தலைப்புகள்)",
                    description = "தினமும் ஒரு தலைப்பைத் தேர்ந்தெடுத்து 1 நிமிடம் ஆங்கிலத்தில் பேசும் தன்னம்பிக்கைப் பயிற்சி.",
                    badge = "15 தலைப்புகள்",
                    challenges = listOf(
                        SpeechChallenge(
                            topicNumber = 1,
                            topic = "My Favourite Food",
                            tamilTopic = "எனக்குப் பிடித்த உணவு",
                            englishSpeech = "Hello, everyone! Today, I am going to talk about my favourite food. My favourite food is dosa. I like dosa because it is tasty and crispy. My mother often makes dosa at home. I usually eat dosa with coconut chutney and sambar. Dosa is a popular food in Tamil Nadu. I enjoy eating dosa with my family. I also like trying different varieties of dosa. My favourite is masala dosa. I feel happy when I eat my favourite food. Thank you for listening to me!",
                            tamilSpeech = "அனைவருக்கும் வணக்கம்! இன்று எனக்குப் பிடித்த உணவைப் பற்றிப் பேசப் போகிறேன். எனக்குப் பிடித்த உணவு தோசை. அது சுவையாகவும் மொறுமொறுப்பாகவும் இருப்பதால் எனக்குப் பிடிக்கும். என் அம்மா வீட்டில் அடிக்கடி தோசை செய்வார். நான் வழக்கமாகத் தேங்காய் சட்னி மற்றும் சாம்பாருடன் தோசை சாப்பிடுவேன். தோசை தமிழ்நாட்டில் பிரபலமான உணவு. என் குடும்பத்துடன் தோசை சாப்பிடுவது எனக்கு மகிழ்ச்சியைத் தருகிறது. பல வகையான தோசைகளை முயற்சி செய்து பார்க்கவும் எனக்குப் பிடிக்கும். எனக்குப் பிடித்தது மசாலா தோசை. எனக்குப் பிடித்த உணவைச் சாப்பிடும்போது மகிழ்ச்சியாக உணர்கிறேன். நான் பேசுவதைக் கேட்டதற்கு நன்றி!"
                        ),
                        SpeechChallenge(
                            topicNumber = 2,
                            topic = "My Family",
                            tamilTopic = "என் குடும்பம்",
                            englishSpeech = "Good morning! Today I would like to talk about my family. There are four members in my family: my father, my mother, my younger sister, and myself. My father is very hardworking and caring. My mother is kind and cooks delicious meals for us. My sister and I play together every evening. We help each other and share our happiness. I love my family very much, and they are my greatest strength. Thank you!",
                            tamilSpeech = "காலை வணக்கம்! இன்று என் குடும்பத்தைப் பற்றிப் பேச விரும்புகிறேன். எங்கள் குடும்பத்தில் நான்கு பேர் உள்ளனர்: என் அப்பா, என் அம்மா, என் தங்கை மற்றும் நான். என் அப்பா கடின உழைப்பாளி மற்றும் அன்பானவர். என் அம்மா தயவானவர் மற்றும் எங்களுக்கு சுவையான உணவைச் சமைக்கிறார். என் தங்கையும் நானும் தினமும் மாலையில் ஒன்றாக விளையாடுவோம். நாங்கள் ஒருவருக்கொருவர் உதவி செய்து எங்கள் மகிழ்ச்சியைப் பகிர்ந்துகொள்கிறோம். என் குடும்பத்தை நான் மிகவும் நேசிக்கிறேன்!"
                        ),
                        SpeechChallenge(
                            topicNumber = 3,
                            topic = "My School",
                            tamilTopic = "என் பள்ளி",
                            englishSpeech = "Hello friends! I am proud to speak about my school. My school has a beautiful campus with big classrooms, a science lab, and a huge playground. Our teachers are friendly, encouraging, and teach every subject clearly. I love the library where I read story books during free time. My school teaches us discipline, good manners, and sports. I truly love going to school every day. Thank you!",
                            tamilSpeech = "வணக்கம் நண்பர்களே! என் பள்ளியைப் பற்றிப் பேசுவதில் பெருமிதம் கொள்கிறேன். எங்கள் பள்ளியில் பெரிய வகுப்பறைகள், அறிவியல் ஆய்வகம் மற்றும் பெரிய விளையாட்டு மைதானம் கொண்ட அழகான வளாகம் உள்ளது. எங்கள் ஆசிரியர்கள் அன்பானவர்கள், ஊக்கமளிப்பவர்கள் மற்றும் ஒவ்வொரு பாடத்தையும் தெளிவாகக் கற்பிக்கிறார்கள். ஓய்வு நேரத்தில் கதைப் புத்தகங்களைப் படிக்கும் நூலகத்தை நான் விரும்புகிறேன். என் பள்ளிக்கு தினமும் செல்வதை நான் உண்மையிலேயே விரும்புகிறேன். நன்றி!"
                        ),
                        SpeechChallenge(
                            topicNumber = 4,
                            topic = "My Daily Routine",
                            tamilTopic = "என் அன்றாட வழக்கம்",
                            englishSpeech = "Hello everyone! I wake up at six o'clock every morning. First, I brush my teeth and wash my face. Then I exercise and drink a glass of warm milk. After having a healthy breakfast, I get ready and go to school. In the afternoon, I return home, rest for a while, and do my homework. In the evening, I play with my friends and practise spoken English. Finally, I have dinner with my family and go to sleep by ten o'clock. Thank you!",
                            tamilSpeech = "அனைவருக்கும் வணக்கம்! நான் தினமும் காலை ஆறு மணிக்கு எழுந்திருப்பேன். முதலில் பல் துலக்கி முகம் கழுவுகிறேன். பின்னர் உடற்பயிற்சி செய்து சூடான பால் குடிக்கிறேன். சத்தான காலை உணவுக்குப் பின் பள்ளிக்குச் செல்கிறேன். மாலையில் வீடு திரும்பி ஓய்வெடுத்துவிட்டு வீட்டுப்பாடம் செய்கிறேன். பின்னர் நண்பர்களுடன் விளையாடிவிட்டு ஆங்கிலப் பயிற்சி செய்கிறேன். இரவு குடும்பத்துடன் சாப்பிட்டு பத்து மணிக்குத் தூங்கச் செல்கிறேன். நன்றி!"
                        ),
                        SpeechChallenge(
                            topicNumber = 5,
                            topic = "Why I Want to Learn English",
                            tamilTopic = "நான் ஏன் ஆங்கிலம் கற்க விரும்புகிறேன்",
                            englishSpeech = "Hello! English is an international language that connects people all around the world. I want to learn English because it will help me in my higher education, career, and communication with global friends. When I speak English confidently, I feel brave and ready to achieve my dreams. Subiksha Teacher app makes learning simple, step by step through Tamil. I promise to practise every day and become fluent. Thank you!",
                            tamilSpeech = "வணக்கம்! ஆங்கிலம் உலகெங்கிலும் உள்ள மக்களை இணைக்கும் ஒரு சர்வதேச மொழியாகும். உயர் கல்வி, சிறந்த வேலைவாய்ப்பு மற்றும் உலகம் முழுவதும் உள்ள நண்பர்களுடன் பேச ஆங்கிலம் எனக்கு உதவும் என்பதால் நான் அதைக் கற்க விரும்புகிறேன். தன்னம்பிக்கையுடன் பேசும்போது என் கனவுகளை அடைய தைரியம் பிறக்கிறது. சுபிக்ஷா டீச்சர் ஆப் தமிழ் வழியில் படிப்படியாக எளிதாகக் கற்றுக்கொடுக்கிறது. தினமும் பயிற்சி செய்வேன் என்று வாக்களிக்கிறேன். நன்றி!"
                        )
                    )
                ),

                // 14. Fluency Tips
                SpokenChapter(
                    id = "b1_c14",
                    number = 14,
                    title = "Fluency Tips & Rules",
                    tamilTitle = "ஆங்கிலம் வேகமாகப் பேச 5 ரகசிய வழிகள்",
                    description = "Mirror Practice, Shadowing, Think in English மற்றும் தவறுகளுக்குப் பயப்படாத மனநிலை.",
                    badge = "வெற்றி ரகசியம்",
                    sentences = listOf(
                        SpokenSentence(1, "1. Mirror Practice: Speak in front of a mirror for 5 minutes.", "கண்ணாடிப் பயிற்சி: தினமும் 5 நிமிடம் கண்ணாடியைப் பார்த்துப் பேசுங்கள்.", "உதவிக்குறிப்பு", "Fluency Tip"),
                        SpokenSentence(2, "2. Don't fear mistakes: Making mistakes is normal while learning.", "தவறுகளுக்குப் பயப்படாதீர்கள்: கற்கும் போது தவறுகள் ஏற்படுவது இயல்பானதே.", "உதவிக்குறிப்பு", "Fluency Tip"),
                        SpokenSentence(3, "3. Think in English: Describe small things in your mind.", "ஆங்கிலத்தில் சிந்தியுங்கள்: மனதில் செய்யும் செயல்களை ஆங்கிலத்தில் நினையுங்கள்.", "உதவிக்குறிப்பு", "Fluency Tip"),
                        SpokenSentence(4, "4. Shadowing Practice: Listen to a sentence and repeat instantly.", "கேட்டு உடனே பின்பற்றுங்கள்: ஆடியோவைக் கேட்டு உடனே பேசிப் பழகுங்கள்.", "உதவிக்குறிப்பு", "Fluency Tip"),
                        SpokenSentence(5, "5. Daily Revision: Practise previous lessons to retain vocabulary.", "தினசரி மறுபயிற்சி: கற்ற சொற்களை மறக்காமல் இருக்க தினமும் மீள்பார்வை செய்திடுங்கள்.", "உதவிக்குறிப்பு", "Fluency Tip")
                    )
                )
            )
        )
    }

    // ==========================================
    // BOOK 2: English Tenses Through Tamil
    // ==========================================
    val book2: SpokenBook by lazy {
        SpokenBook(
            id = "book_2",
            title = "English Tenses Through Tamil – Book 2",
            tamilTitle = "ஆங்கிலக் காலங்கள் – புத்தகம் 2 (12 Tenses)",
            description = "ஆங்கிலத்தின் 12 காலங்களையும் விதிகள், 600+ அன்றாட வாக்கியங்கள் மற்றும் Past-Present-Future ஒப்பீடுகளோடு கற்றல்.",
            badge = "14 பாடங்கள் • 12 காலங்கள்",
            accentColor = Color(0xFF0F766E), // Emerald Teal
            secondaryColor = Color(0xFF0284C7), // Sky Blue
            chapters = listOf(
                // 1. Tenses Basics
                SpokenChapter(
                    id = "b2_c1",
                    number = 1,
                    title = "Tenses Foundations",
                    tamilTitle = "Tenses கற்கும் முன் அறிய வேண்டிய அடிப்படைகள்",
                    description = "Tense என்றால் என்ன? V1, V2, V3 வடிவங்கள், Helping Verbs, Do/Does/Did, Am/Is/Are பயன்பாடுகள்.",
                    badge = "அடிப்படை விதிகள்",
                    rules = listOf(
                        "Tense என்றால் என்ன?" to "ஒரு செயல் எந்தக் காலத்தில் (நிகழ்காலம், கடந்தகாலம், எதிர்காலம்) நடக்கிறது என்பதைக் குறிப்பதே Tense.",
                        "Verb (வினைச்சொல்)" to "செயலைக் குறிக்கும் சொல் (உதா: Eat, Go, Play).",
                        "V1, V2, V3 என்றால் என்ன?" to "V1 = Present (Go), V2 = Past (Went), V3 = Past Participle (Gone).",
                        "Helping Verbs" to "முதன்மை வினைக்கு உதவும் சொற்கள்: Do, Does, Did, Am, Is, Are, Was, Were, Have, Has, Had, Will.",
                        "Subject & Agreement" to "I, We, You, They -க்கு நிகழ்காலத்தில் 's/es' சேராது. He, She, It -க்கு 's/es' சேரும்."
                    ),
                    sentences = listOf(
                        SpokenSentence(1, "Go (V1) - Went (V2) - Gone (V3)", "செல் - சென்றான் - சென்றுவிட்டான்", "Verb Forms", "Verbs"),
                        SpokenSentence(2, "Eat (V1) - Ate (V2) - Eaten (V3)", "சாப்பிடு - சாப்பிட்டான் - சாப்பிட்டுவிட்டான்", "Verb Forms", "Verbs"),
                        SpokenSentence(3, "Write (V1) - Wrote (V2) - Written (V3)", "எழுது - எழுதினான் - எழுதிவிட்டான்", "Verb Forms", "Verbs"),
                        SpokenSentence(4, "See (V1) - Saw (V2) - Seen (V3)", "பார் - பார்த்தான் - பார்த்துவிட்டான்", "Verb Forms", "Verbs"),
                        SpokenSentence(5, "Do / Does: நிகழ்காலக் கேள்விகளுக்குப் பயன்படும்.", "Do you know? / Does he come?", "Helping Verb", "Usage")
                    )
                ),

                // 2. Simple Present
                SpokenChapter(
                    id = "b2_c2",
                    number = 2,
                    title = "1. Simple Present Tense",
                    tamilTitle = "எளிய நிகழ்காலம் (Simple Present)",
                    description = "தினசரி வழக்கம், உண்மைகள் மற்றும் மாறாத பழக்கவழக்கங்கள். சூத்திரம்: Subject + V1 (s/es).",
                    badge = "Formula: S + V1",
                    sentences = listOf(
                        SpokenSentence(1, "I wake up at 6 AM every day.", "நான் தினமும் காலை 6 மணிக்கு எழுகிறேன்.", "வழக்கம்", "Positive"),
                        SpokenSentence(2, "She speaks English very well.", "அவள் ஆங்கிலம் மிக நன்றாகப் பேசுகிறாள்.", "திறமை", "Positive"),
                        SpokenSentence(3, "The sun rises in the east.", "சூரியன் கிழக்கில் உதிக்கிறது.", "உலக உண்மை", "Positive"),
                        SpokenSentence(4, "We play football every evening.", "நாங்கள் தினமும் மாலையில் கால்பந்து விளையாடுகிறோம்.", "வழக்கம்", "Positive"),
                        SpokenSentence(5, "He works in a bank.", "அவர் ஒரு வங்கியில் வேலை செய்கிறார்.", "தொழில்", "Positive"),
                        SpokenSentence(6, "I don't drink coffee.", "நான் காபி குடிப்பதில்லை.", "எதிர்மறை", "Negative"),
                        SpokenSentence(7, "She doesn't like spicy food.", "அவளுக்குக் காரமான உணவு பிடிக்காது.", "எதிர்மறை", "Negative"),
                        SpokenSentence(8, "Do you speak English?", "உனக்கு ஆங்கிலம் பேசத் தெரியுமா?", "வினா", "Question"),
                        SpokenSentence(9, "Does he live here?", "அவர் இங்கே வசிக்கிறாரா?", "வினா", "Question"),
                        SpokenSentence(10, "Where do you work?", "நீங்கள் எங்கே வேலை செய்கிறீர்கள்?", "WH வினா", "Question")
                    )
                ),

                // 3. Present Continuous
                SpokenChapter(
                    id = "b2_c3",
                    number = 3,
                    title = "2. Present Continuous Tense",
                    tamilTitle = "நிகழ்காலத் தொடர் (Present Continuous)",
                    description = "இப்போது உங்கள் கண் முன்னே நடந்துகொண்டிருக்கும் செயல்கள். சூத்திரம்: S + am/is/are + V-ing.",
                    badge = "Formula: S + is/am/are + V-ing",
                    sentences = listOf(
                        SpokenSentence(1, "I am learning English now.", "நான் இப்போது ஆங்கிலம் கற்றுக்கொண்டிருக்கிறேன்.", "செயல்", "Positive"),
                        SpokenSentence(2, "She is cooking in the kitchen.", "அவள் சமையலறையில் சமைத்துக்கொண்டிருக்கிறாள்.", "செயல்", "Positive"),
                        SpokenSentence(3, "They are playing in the ground.", "அவர்கள் மைதானத்தில் விளையாடிக்கொண்டிருக்கிறார்கள்.", "செயல்", "Positive"),
                        SpokenSentence(4, "It is raining outside.", "வெளியில் மழை பெய்துகொண்டிருக்கிறது.", "இயற்கை", "Positive"),
                        SpokenSentence(5, "He is writing a letter.", "அவன் ஒரு கடிதம் எழுதிக்கொண்டிருக்கிறான்.", "செயல்", "Positive"),
                        SpokenSentence(6, "I am not watching TV.", "நான் தொலைக்காட்சி பார்த்துக்கொண்டிருக்கவில்லை.", "எதிர்மறை", "Negative"),
                        SpokenSentence(7, "They are not making noise.", "அவர்கள் சத்தம் போட்டுக்கொண்டிருக்கவில்லை.", "எதிர்மறை", "Negative"),
                        SpokenSentence(8, "Are you listening to me?", "நான் சொல்வதைக் கவனித்துக் கொண்டிருக்கிறாயா?", "வினா", "Question"),
                        SpokenSentence(9, "What are you doing?", "நீ என்ன செய்துகொண்டிருக்கிறாய்?", "WH வினா", "Question"),
                        SpokenSentence(10, "Why is the baby crying?", "குழந்தை ஏன் அழுதுகொண்டிருக்கிறது?", "WH வினா", "Question")
                    )
                ),

                // 4. Present Perfect
                SpokenChapter(
                    id = "b2_c4",
                    number = 4,
                    title = "3. Present Perfect Tense",
                    tamilTitle = "நிகழ்கால நிறைவு (Present Perfect)",
                    description = "சற்று முன்னர்தான் முடிவடைந்த செயல்கள். சூத்திரம்: S + have/has + V3.",
                    badge = "Formula: S + have/has + V3",
                    sentences = listOf(
                        SpokenSentence(1, "I have finished my homework.", "நான் என் வீட்டுப்பாடத்தை முடித்துவிட்டேன்.", "முடிந்த செயல்", "Positive"),
                        SpokenSentence(2, "She has arrived at the station.", "அவள் ரயில் நிலையத்தை அடைந்துவிட்டாள்.", "முடிந்த செயல்", "Positive"),
                        SpokenSentence(3, "We have watched this movie.", "நாங்கள் இந்தத் திரைப்படத்தைப் பார்த்துவிட்டோம்.", "அனுபவம்", "Positive"),
                        SpokenSentence(4, "He has bought a new phone.", "அவன் ஒரு புதிய போன் வாங்கிவிட்டான்.", "செயல்", "Positive"),
                        SpokenSentence(5, "They have cleaned the room.", "அவர்கள் அறையைச் சுத்தம் செய்துவிட்டார்கள்.", "செயல்", "Positive"),
                        SpokenSentence(6, "I haven't eaten yet.", "நான் இன்னும் சாப்பிடவில்லை.", "எதிர்மறை", "Negative"),
                        SpokenSentence(7, "She hasn't called me.", "அவள் என்னை இன்னும் அழைக்கவில்லை.", "எதிர்மறை", "Negative"),
                        SpokenSentence(8, "Have you seen my keys?", "என் சாவியைப் பார்த்தாயா?", "வினா", "Question"),
                        SpokenSentence(9, "Has the train left?", "ரயில் புறப்பட்டுவிட்டதா?", "வினா", "Question"),
                        SpokenSentence(10, "Where have you been?", "நீ எங்கே சென்றிருந்தாய்?", "WH வினா", "Question")
                    )
                ),

                // 5. Present Perfect Continuous
                SpokenChapter(
                    id = "b2_c5",
                    number = 5,
                    title = "4. Present Perfect Continuous",
                    tamilTitle = "நிகழ்கால நிறைவுத் தொடர்",
                    description = "கடந்த காலத்தில் தொடங்கி இப்போது வரை தொடர்ந்து நடக்கும் செயல்கள். சூத்திரம்: S + have/has been + V-ing.",
                    badge = "Formula: S + have/has been + V-ing",
                    sentences = listOf(
                        SpokenSentence(1, "I have been waiting for you for one hour.", "நான் உனக்காக ஒரு மணி நேரமாகக் காத்துக்கொண்டிருக்கிறேன்.", "தொடர்ச்சி", "Positive"),
                        SpokenSentence(2, "It has been raining since morning.", "காலை முதலே மழை பெய்துகொண்டிருக்கிறது.", "தொடர்ச்சி", "Positive"),
                        SpokenSentence(3, "She has been working here for three years.", "அவள் மூன்று வருடங்களாக இங்கே வேலை செய்துகொண்டிருக்கிறாள்.", "தொடர்ச்சி", "Positive"),
                        SpokenSentence(4, "They have been studying since 4 PM.", "அவர்கள் மாலை 4 மணியிலிருந்து படித்துக்கொண்டிருக்கிறார்கள்.", "தொடர்ச்சி", "Positive"),
                        SpokenSentence(5, "How long have you been learning English?", "எவ்வளவு காலமாக நீங்கள் ஆங்கிலம் கற்றுக்கொண்டிருக்கிறீர்கள்?", "வினா", "Question")
                    )
                ),

                // 6. Simple Past
                SpokenChapter(
                    id = "b2_c6",
                    number = 6,
                    title = "5. Simple Past Tense",
                    tamilTitle = "எளிய இறந்தகாலம் (Simple Past)",
                    description = "நேற்று அல்லது குறிப்பிட்ட காலத்தில் முடிந்துபோன செயல். சூத்திரம்: S + V2.",
                    badge = "Formula: S + V2",
                    sentences = listOf(
                        SpokenSentence(1, "I went to school yesterday.", "நான் நேற்று பள்ளிக்குச் சென்றேன்.", "சென்ற செயல்", "Positive"),
                        SpokenSentence(2, "She cooked tasty food.", "அவள் சுவையான உணவு சமைத்தாள்.", "சென்ற செயல்", "Positive"),
                        SpokenSentence(3, "They played cricket yesterday evening.", "அவர்கள் நேற்று மாலை கிரிக்கெட் விளையாடினார்கள்.", "சென்ற செயல்", "Positive"),
                        SpokenSentence(4, "He bought a new bicycle last week.", "அவன் கடந்த வாரம் ஒரு புதிய மிதிவண்டி வாங்கினான்.", "சென்ற செயல்", "Positive"),
                        SpokenSentence(5, "We saw a great movie on Sunday.", "ஞாயிற்றுக்கிழமை நாங்கள் ஒரு நல்ல படம் பார்த்தோம்.", "சென்ற செயல்", "Positive"),
                        SpokenSentence(6, "I did not receive your message.", "எனக்கு உங்கள் செய்தி வரவில்லை.", "எதிர்மறை", "Negative"),
                        SpokenSentence(7, "She didn't come to school yesterday.", "அவள் நேற்று பள்ளிக்கு வரவில்லை.", "எதிர்மறை", "Negative"),
                        SpokenSentence(8, "Did you finish your work?", "நீ உன் வேலையை முடித்துவிட்டாயா?", "வினா", "Question"),
                        SpokenSentence(9, "Where did you go yesterday?", "நேற்று நீ எங்கே சென்றாய்?", "WH வினா", "Question"),
                        SpokenSentence(10, "Why didn't you call me?", "நீ ஏன் என்னை அழைக்கவில்லை?", "WH எதிர்மறை வினா", "Question")
                    )
                ),

                // 7. Past Continuous
                SpokenChapter(
                    id = "b2_c7",
                    number = 7,
                    title = "6. Past Continuous Tense",
                    tamilTitle = "இறந்தகாலத் தொடர் (Past Continuous)",
                    description = "கடந்த காலத்தில் ஒரு குறிப்பிட்ட நேரத்தில் நடந்துகொண்டிருந்த செயல். சூத்திரம்: S + was/were + V-ing.",
                    badge = "Formula: S + was/were + V-ing",
                    sentences = listOf(
                        SpokenSentence(1, "I was studying when you called.", "நீ அழைத்தபோது நான் படித்துக்கொண்டிருந்தேன்.", "தொடர் செயல்", "Positive"),
                        SpokenSentence(2, "She was sleeping at 3 PM.", "மதியம் 3 மணிக்கு அவள் தூங்கிக்கொண்டிருந்தாள்.", "தொடர் செயல்", "Positive"),
                        SpokenSentence(3, "They were playing in the rain.", "அவர்கள் மழையில் விளையாடிக்கொண்டிருந்தார்கள்.", "தொடர் செயல்", "Positive"),
                        SpokenSentence(4, "We were waiting for the bus.", "நாங்கள் பேருந்துக்காகக் காத்துக்கொண்டிருந்தோம்.", "தொடர் செயல்", "Positive"),
                        SpokenSentence(5, "Were you watching TV yesterday evening?", "நேற்று மாலை நீ தொலைக்காட்சி பார்த்துக்கொண்டிருந்தாயா?", "வினா", "Question")
                    )
                ),

                // 8. Past Perfect
                SpokenChapter(
                    id = "b2_c8",
                    number = 8,
                    title = "7. Past Perfect Tense",
                    tamilTitle = "இறந்தகால நிறைவு (Past Perfect)",
                    description = "கடந்த காலத்தில் நடந்த இரு செயல்களில் முதலில் முடிந்த செயல். சூத்திரம்: S + had + V3.",
                    badge = "Formula: S + had + V3",
                    sentences = listOf(
                        SpokenSentence(1, "The train had left before I reached the station.", "நான் ரயில் நிலையம் அடையும் முன்பே ரயில் புறப்பட்டுவிட்டிருந்தது.", "முன் செயல்", "Positive"),
                        SpokenSentence(2, "She had finished cooking when guests arrived.", "விருந்தினர்கள் வருவதற்கு முன்பே அவள் சமைத்து முடித்திருந்தாள்.", "முன் செயல்", "Positive"),
                        SpokenSentence(3, "I had done my homework before dinner.", "இரவு உணவுக்கு முன்பே நான் வீட்டுப்பாடத்தை முடித்திருந்தேன்.", "முன் செயல்", "Positive"),
                        SpokenSentence(4, "Had you seen this place before?", "இதற்கு முன் நீ இந்த இடத்தைப் பார்த்திருந்தாயா?", "வினா", "Question")
                    )
                ),

                // 9. Past Perfect Continuous
                SpokenChapter(
                    id = "b2_c9",
                    number = 9,
                    title = "8. Past Perfect Continuous",
                    tamilTitle = "இறந்தகால நிறைவுத் தொடர்",
                    description = "கடந்த காலத்தில் மற்றொரு நிகழ்வுக்கு முன் நீண்ட நேரம் தொடர்ந்த செயல். சூத்திரம்: S + had been + V-ing.",
                    badge = "Formula: S + had been + V-ing",
                    sentences = listOf(
                        SpokenSentence(1, "I had been waiting for two hours before the bus arrived.", "பேருந்து வரும் முன் நான் இரண்டு மணி நேரமாகக் காத்துக்கொண்டிருந்தேன்.", "நீண்ட செயல்", "Positive"),
                        SpokenSentence(2, "She had been living there for five years before moving.", "அங்கிருந்து இடம்பெயர்வதற்கு முன் அவள் ஐந்து வருடங்களாக அங்கே வசித்து வந்தாள்.", "நீண்ட செயல்", "Positive"),
                        SpokenSentence(3, "They had been working hard all day.", "அவர்கள் நாள் முழுவதும் கடினமாக உழைத்துக்கொண்டிருந்தார்கள்.", "நீண்ட செயல்", "Positive")
                    )
                ),

                // 10. Simple Future
                SpokenChapter(
                    id = "b2_c10",
                    number = 10,
                    title = "9. Simple Future Tense",
                    tamilTitle = "எளிய எதிர்காலம் (Simple Future)",
                    description = "இனிமேல் அல்லது நாளை நடக்கவிருக்கும் செயல். சூத்திரம்: S + will + V1.",
                    badge = "Formula: S + will + V1",
                    sentences = listOf(
                        SpokenSentence(1, "I will practise English tomorrow.", "நான் நாளை ஆங்கிலப் பயிற்சி செய்வேன்.", "எதிர்கால உறுதி", "Positive"),
                        SpokenSentence(2, "She will come to my house.", "அவள் என் வீட்டிற்கு வருவாள்.", "எதிர்காலம்", "Positive"),
                        SpokenSentence(3, "They will win the match.", "அவர்கள் போட்டியில் வெற்றி பெறுவார்கள்.", "நம்பிக்கை", "Positive"),
                        SpokenSentence(4, "We will visit Chennai next week.", "நாங்கள் அடுத்த வாரம் சென்னைக்குச் செல்வோம்.", "திட்டம்", "Positive"),
                        SpokenSentence(5, "I will not forget your help.", "நான் உங்கள் உதவியை மறக்க மாட்டேன்.", "எதிர்மறை", "Negative"),
                        SpokenSentence(6, "She won't be late.", "அவள் தாமதமாக வரமாட்டாள்.", "எதிர்மறை", "Negative"),
                        SpokenSentence(7, "Will you come with me?", "நீ என்னுடன் வருவாயா?", "வினா", "Question"),
                        SpokenSentence(8, "When will the exam start?", "தேர்வு எப்போது தொடங்கும்?", "WH வினா", "Question")
                    )
                ),

                // 11. Future Continuous
                SpokenChapter(
                    id = "b2_c11",
                    number = 11,
                    title = "10. Future Continuous Tense",
                    tamilTitle = "எதிர்காலத் தொடர் (Future Continuous)",
                    description = "எதிர்காலத்தில் குறிப்பிட்ட நேரத்தில் நடந்துகொண்டிருக்கும் செயல். சூத்திரம்: S + will be + V-ing.",
                    badge = "Formula: S + will be + V-ing",
                    sentences = listOf(
                        SpokenSentence(1, "I will be studying tomorrow at 8 AM.", "நாளை காலை 8 மணிக்கு நான் படித்துக்கொண்டிருப்பேன்.", "எதிர்காலத் தொடர்ச்சி", "Positive"),
                        SpokenSentence(2, "She will be travelling this time tomorrow.", "நாளை இந்நேரம் அவள் பயணம் செய்துகொண்டிருப்பாள்.", "எதிர்காலத் தொடர்ச்சி", "Positive"),
                        SpokenSentence(3, "They will be playing cricket on Sunday.", "ஞாயிற்றுக்கிழமை அவர்கள் கிரிக்கெட் விளையாடிக்கொண்டிருப்பார்கள்.", "எதிர்காலத் தொடர்ச்சி", "Positive"),
                        SpokenSentence(4, "Will you be attending the meeting?", "நீங்கள் கூட்டத்தில் கலந்துகொண்டிருப்பீர்களா?", "வினா", "Question")
                    )
                ),

                // 12. Future Perfect & Continuous
                SpokenChapter(
                    id = "b2_c12",
                    number = 12,
                    title = "11 & 12. Future Perfect & Cont.",
                    tamilTitle = "எதிர்கால நிறைவு மற்றும் தொடர்",
                    description = "எதிர்காலத்தில் ஒரு குறிப்பிட்ட நேரத்திற்குள் முடிந்துவிடும் செயல். சூத்திரம்: S + will have + V3.",
                    badge = "Formula: S + will have + V3",
                    sentences = listOf(
                        SpokenSentence(1, "I will have completed the project by tomorrow evening.", "நாளை மாலைக்குள் நான் திட்டத்தை முடித்திருப்பேன்.", "எதிர்கால நிறைவு", "Future Perfect"),
                        SpokenSentence(2, "She will have reached Chennai by 5 PM.", "மாலை 5 மணிக்குள் அவள் சென்னையை அடைந்திருப்பாள்.", "எதிர்கால நிறைவு", "Future Perfect"),
                        SpokenSentence(3, "By next month, I will have been learning English for six months.", "அடுத்த மாதத்திற்குள் நான் ஆறு மாதங்களாக ஆங்கிலம் கற்றுக்கொண்டு வந்திருப்பேன்.", "நிறைவுத் தொடர்", "Future Perfect Cont.")
                    )
                ),

                // 13. 3-Tense Comparison
                SpokenChapter(
                    id = "b2_c13",
                    number = 13,
                    title = "3-Tense Comparison Drill",
                    tamilTitle = "3 காலங்களின் நேரடி ஒப்பீடு (Past vs Present vs Future)",
                    description = "ஒரே செயலை இறந்தகாலம், நிகழ்காலம் மற்றும் எதிர்காலத்தில் மாற்றிப் பேசும் நேரடி பயிற்சி.",
                    badge = "Color Coded Drill",
                    comparisons = listOf(
                        TenseComparisonItem(
                            title = "உணவு சாப்பிடுதல் (Eat)",
                            pastSentence = "I ate an apple yesterday.",
                            pastTamil = "நான் நேற்று ஒரு ஆப்பிள் சாப்பிட்டேன்.",
                            presentSentence = "I eat an apple daily.",
                            presentTamil = "நான் தினமும் ஒரு ஆப்பிள் சாப்பிடுகிறேன்.",
                            futureSentence = "I will eat an apple tomorrow.",
                            futureTamil = "நான் நாளை ஒரு ஆப்பிள் சாப்பிடுவேன்."
                        ),
                        TenseComparisonItem(
                            title = "பள்ளிக்குச் செல்லுதல் (Go)",
                            pastSentence = "She went to school.",
                            pastTamil = "அவள் பள்ளிக்குச் சென்றாள்.",
                            presentSentence = "She goes to school daily.",
                            presentTamil = "அவள் தினமும் பள்ளிக்குச் செல்கிறாள்.",
                            futureSentence = "She will go to school tomorrow.",
                            futureTamil = "அவள் நாளை பள்ளிக்குச் செல்வாள்."
                        ),
                        TenseComparisonItem(
                            title = "ஆங்கிலம் பேசுதல் (Speak)",
                            pastSentence = "He spoke politely.",
                            pastTamil = "அவன் மரியாதையாகப் பேசினான்.",
                            presentSentence = "He speaks English confidently.",
                            presentTamil = "அவன் தன்னம்பிக்கையுடன் ஆங்கிலம் பேசுகிறான்.",
                            futureSentence = "He will speak in the stage tomorrow.",
                            futureTamil = "அவன் நாளை மேடையில் பேசுவான்."
                        ),
                        TenseComparisonItem(
                            title = "கடிதம் எழுதுதல் (Write)",
                            pastSentence = "We wrote the exam well.",
                            pastTamil = "நாங்கள் தேர்வை நன்றாக எழுதினோம்.",
                            presentSentence = "We write our notes neatly.",
                            presentTamil = "நாங்கள் குறிப்புகளை நேர்த்தியாக எழுதுகிறோம்.",
                            futureSentence = "We will write a story tomorrow.",
                            futureTamil = "நாங்கள் நாளை ஒரு கதை எழுதுவோம்."
                        ),
                        TenseComparisonItem(
                            title = "கிரிக்கெட் விளையாடுதல் (Play)",
                            pastSentence = "They played cricket.",
                            pastTamil = "அவர்கள் கிரிக்கெட் விளையாடினார்கள்.",
                            presentSentence = "They play every evening.",
                            presentTamil = "அவர்கள் தினமும் மாலையில் விளையாடுகிறார்கள்.",
                            futureSentence = "They will play next Sunday.",
                            futureTamil = "அவர்கள் அடுத்த ஞாயிறு விளையாடுவார்கள்."
                        )
                    )
                ),

                // 14. 20 Essential Verbs & +2+1 Drill
                SpokenChapter(
                    id = "b2_c14",
                    number = 14,
                    title = "20 Verbs & +2+1 Drill",
                    tamilTitle = "20 முக்கிய வினைச்சொற்கள் & +2+1 வாக்கியப் பயிற்சி",
                    description = "ஒவ்வொரு சொல்லுக்கும் V1, V2, V3 வடிவங்கள் மற்றும் 3 காலங்களிலும் வாக்கியப் பயிற்சி.",
                    badge = "60 வாக்கியங்கள்",
                    sentences = listOf(
                        SpokenSentence(1, "Eat - Ate - Eaten", "சாப்பிடு (V1 - V2 - V3)", "+2+1 Drill", "Verb 1"),
                        SpokenSentence(2, "Drink - Drank - Drunk", "குடி (V1 - V2 - V3)", "+2+1 Drill", "Verb 2"),
                        SpokenSentence(3, "See - Saw - Seen", "பார் (V1 - V2 - V3)", "+2+1 Drill", "Verb 3"),
                        SpokenSentence(4, "Take - Took - Taken", "எடு (V1 - V2 - V3)", "+2+1 Drill", "Verb 4"),
                        SpokenSentence(5, "Give - Gave - Given", "கொடு (V1 - V2 - V3)", "+2+1 Drill", "Verb 5"),
                        SpokenSentence(6, "Write - Wrote - Written", "எழுது (V1 - V2 - V3)", "+2+1 Drill", "Verb 6"),
                        SpokenSentence(7, "Speak - Spoke - Spoken", "பேசு (V1 - V2 - V3)", "+2+1 Drill", "Verb 7"),
                        SpokenSentence(8, "Come - Came - Come", "வா (V1 - V2 - V3)", "+2+1 Drill", "Verb 8"),
                        SpokenSentence(9, "Go - Went - Gone", "போ (V1 - V2 - V3)", "+2+1 Drill", "Verb 9"),
                        SpokenSentence(10, "Buy - Bought - Bought", "வாங்கு (V1 - V2 - V3)", "+2+1 Drill", "Verb 10")
                    )
                )
            )
        )
    }
}
