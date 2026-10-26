package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SpokenEnglishScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
    val context = LocalContext.current

    // Navigation states
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var selectedChapterId by remember { mutableStateOf<String?>(null) }

    // TextToSpeech engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var currentlySpeakingSentenceId by remember { mutableStateOf<Int?>(null) }
    var slowSpeed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale.US
                isTtsReady = true
            }
        }
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakText(text: String, id: Int? = null) {
        if (!isTtsReady || ttsEngine == null) return
        ttsEngine?.stop()
        ttsEngine?.setSpeechRate(if (slowSpeed) 0.72f else 0.95f)
        currentlySpeakingSentenceId = id
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id?.toString() ?: "id")
    }

    fun stopSpeaking() {
        ttsEngine?.stop()
        currentlySpeakingSentenceId = null
    }

    // BackHandler to satisfy smooth backstack without adding bottom tabs
    BackHandler {
        when {
            selectedChapterId != null -> {
                stopSpeaking()
                selectedChapterId = null
            }
            selectedBookId != null -> {
                stopSpeaking()
                selectedBookId = null
            }
            else -> {
                stopSpeaking()
                onBackClick()
            }
        }
    }

    val selectedBook = SpokenEnglishRepository.books.find { it.id == selectedBookId }
    val selectedChapter = selectedBook?.chapters?.find { it.id == selectedChapterId }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SubikshaBackground
    ) {
        when {
            // Level 3: Chapter Detail / Lesson Reader
            selectedBook != null && selectedChapter != null -> {
                ChapterDetailView(
                    book = selectedBook,
                    chapter = selectedChapter,
                    onBack = {
                        stopSpeaking()
                        selectedChapterId = null
                    },
                    onSpeak = { text, id -> speakText(text, id) },
                    onStopSpeak = { stopSpeaking() },
                    currentlySpeakingId = currentlySpeakingSentenceId,
                    slowSpeed = slowSpeed,
                    onToggleSlowSpeed = { slowSpeed = !slowSpeed },
                    onNavigateChapter = { targetId ->
                        stopSpeaking()
                        selectedChapterId = targetId
                    },
                    onAskGemini = onAskGemini
                )
            }

            // Level 2: Book Chapters Index
            selectedBook != null -> {
                BookIndexView(
                    book = selectedBook,
                    onBack = { selectedBookId = null },
                    onSelectChapter = { chapterId -> selectedChapterId = chapterId }
                )
            }

            // Level 1: Main Page - Learn English Through Tamil (Book 1 & Book 2 Overview)
            else -> {
                BooksOverviewView(
                    onBack = onBackClick,
                    onSelectBook = { bookId -> selectedBookId = bookId },
                    onAskGemini = onAskGemini
                )
            }
        }
    }
}

// ==============================================================
// 1. Books Overview View (Main Page)
// ==============================================================
@Composable
private fun BooksOverviewView(
    onBack: () -> Unit,
    onSelectBook: (String) -> Unit,
    onAskGemini: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Ultra Deluxe App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .testTag("spoken_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SubikshaNavy
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Learn English Through Tamil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SubikshaNavy,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "அன்றாடப் பேச்சு ஆங்கிலம் மற்றும் இலக்கணப் பயிற்சி",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubikshaRoyalPurple
                    )
                }
            }
        }

        // Hero Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F172A), // Midnight Slate
                                Color(0xFF1E1B4B), // Deep Indigo
                                Color(0xFF312E81)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "✨ ULTRA DELUXE LEARNING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE68A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "2 COMPLETE BOOKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "சுபிக்ஷா ஆசிரியை கற்பிக்கும்\nமுழுமையான Spoken English",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "புக் 1-ல் அன்றாடப் பேச்சு மற்றும் 100 வாக்கியங்கள்; புக் 2-ல் 12 காலங்கள் (12 Tenses) மற்றும் நேரடி ஒப்பீடுகள்.",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Two Main Book Cards
        item {
            Text(
                text = "பாடப் புத்தகங்கள் (Select a Book)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SubikshaNavy,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        items(SpokenEnglishRepository.books) { book ->
            BookSelectionCard(
                book = book,
                onClick = { onSelectBook(book.id) }
            )
        }

        // Daily Motivation / AI Question Assistant
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onAskGemini("ஆங்கிலத்தில் சரளமாகப் பேச சில எளிய வழிகளைத் தமிழில் சொல்லுங்கள்.") }
                    .testTag("spoken_ai_assistant_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SubikshaPastelPurple.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Help",
                            tint = SubikshaRoyalPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "சுபிக்ஷா AI-யிடம் சந்தேகம் கேட்கலாம்",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )
                        Text(
                            text = "வாக்கியங்கள், உச்சரிப்பு விளக்கம் பெற இங்கே தட்டவும்.",
                            fontSize = 12.sp,
                            color = SubikshaTextMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = SubikshaRoyalPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==============================================================
// Book Selection Card
// ==============================================================
@Composable
private fun BookSelectionCard(
    book: SpokenBook,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(6.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag("book_card_${book.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            book.accentColor.copy(alpha = 0.04f),
                            Color.White
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Book Badge & ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = book.accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (book.id == "book_1") "📘 BOOK 1" else "📙 BOOK 2",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = book.accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = SubikshaSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = book.badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Book Titles
            Text(
                text = book.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SubikshaNavy,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = book.tamilTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = book.accentColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = book.description,
                fontSize = 13.sp,
                color = SubikshaTextMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = book.accentColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "படிக்கத் தொடங்கு (Start Learning)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==============================================================
// 2. Book Chapters Index View (Heading by Heading)
// ==============================================================
@Composable
private fun BookIndexView(
    book: SpokenBook,
    onBack: () -> Unit,
    onSelectChapter: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .testTag("book_index_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SubikshaNavy
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.tamilTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Text(
                        text = "பாடப் பட்டியல் (14 Chapters)",
                        fontSize = 12.sp,
                        color = book.accentColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Chapters List
        items(book.chapters) { chapter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectChapter(chapter.id) }
                    .testTag("chapter_card_${chapter.id}"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Number Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(book.accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", chapter.number),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = book.accentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = chapter.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Surface(
                                color = SubikshaBackground,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = chapter.badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SubikshaTextMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = chapter.tamilTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = book.accentColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = chapter.description,
                            fontSize = 11.sp,
                            color = SubikshaTextMuted,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Chapter",
                        tint = book.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==============================================================
// 3. Chapter Detail / Lesson Reader
// ==============================================================
@Composable
private fun ChapterDetailView(
    book: SpokenBook,
    chapter: SpokenChapter,
    onBack: () -> Unit,
    onSpeak: (String, Int?) -> Unit,
    onStopSpeak: () -> Unit,
    currentlySpeakingId: Int?,
    slowSpeed: Boolean,
    onToggleSlowSpeed: () -> Unit,
    onNavigateChapter: (String) -> Unit,
    onAskGemini: (String) -> Unit
) {
    val currentIdx = book.chapters.indexOfFirst { it.id == chapter.id }
    val prevChapter = if (currentIdx > 0) book.chapters[currentIdx - 1] else null
    val nextChapter = if (currentIdx < book.chapters.size - 1) book.chapters[currentIdx + 1] else null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Detail Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .testTag("chapter_detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SubikshaNavy
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "பாடம் ${chapter.number}: ${chapter.title}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Text(
                        text = chapter.tamilTitle,
                        fontSize = 12.sp,
                        color = book.accentColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Slow Audio Toggle Pill
                Surface(
                    color = if (slowSpeed) SubikshaWarmOrange.copy(alpha = 0.2f) else SubikshaSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { onToggleSlowSpeed() }
                ) {
                    Text(
                        text = if (slowSpeed) "🐢 Slow" else "🐇 Normal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (slowSpeed) SubikshaWarmOrange else SubikshaNavy,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chapter Summary Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = book.accentColor.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = chapter.description,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubikshaNavy,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = book.accentColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = chapter.badge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Play All Button
                        OutlinedButton(
                            onClick = {
                                val allText = chapter.sentences.take(10).joinToString(". ") { it.english }
                                onSpeak(allText, 999)
                            },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "ஒலி கேட்க", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Rules section (if any)
        if (chapter.rules.isNotEmpty()) {
            item {
                Text(
                    text = "அடிப்படை விதிகள் (Core Rules)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy,
                    modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp)
                )
            }

            items(chapter.rules) { (title, rule) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = book.accentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rule,
                            fontSize = 12.sp,
                            color = SubikshaNavy,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Tense Comparison (if any)
        if (chapter.comparisons.isNotEmpty()) {
            item {
                Text(
                    text = "3 காலங்களின் நேரடி ஒப்பீட்டு அட்டவணை",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy,
                    modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp)
                )
            }

            items(chapter.comparisons) { item ->
                TenseComparisonCard(item = item, onSpeak = onSpeak)
            }
        }

        // 1-Minute Speech Challenges (if any)
        if (chapter.challenges.isNotEmpty()) {
            items(chapter.challenges) { challenge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🎤 1-Min Topic: ${challenge.topic}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )

                            IconButton(
                                onClick = { onSpeak(challenge.englishSpeech, challenge.topicNumber) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SubikshaRoyalPurple.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak All",
                                    tint = SubikshaRoyalPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = challenge.tamilTopic,
                            fontSize = 12.sp,
                            color = SubikshaRoyalPurple,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = SubikshaBackground,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = challenge.englishSpeech,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = SubikshaNavy,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = challenge.tamilSpeech,
                            fontSize = 12.sp,
                            color = Color(0xFF047857),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Sentences List
        if (chapter.sentences.isNotEmpty()) {
            item {
                Text(
                    text = "வாக்கியங்கள் & பயிற்சிகள் (${chapter.sentences.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy,
                    modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp)
                )
            }

            items(chapter.sentences) { sentence ->
                SentenceItemCard(
                    sentence = sentence,
                    accentColor = book.accentColor,
                    isPlaying = currentlySpeakingId == sentence.id,
                    onSpeak = { onSpeak(sentence.english, sentence.id) },
                    onStop = onStopSpeak
                )
            }
        }

        // Bottom Chapter Navigation (Previous / Next)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (prevChapter != null) {
                    OutlinedButton(
                        onClick = { onNavigateChapter(prevChapter.id) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "முந்தைய பாடம்", fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                if (nextChapter != null) {
                    Button(
                        onClick = { onNavigateChapter(nextChapter.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = book.accentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "அடுத்த பாடம்", fontSize = 12.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==============================================================
// Sentence Item Card with TTS button
// ==============================================================
@Composable
private fun SentenceItemCard(
    sentence: SpokenSentence,
    accentColor: Color,
    isPlaying: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isPlaying) 1.5.dp else 0.dp,
                color = if (isPlaying) accentColor else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) accentColor.copy(alpha = 0.05f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sentence Index
            Surface(
                color = SubikshaBackground,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = sentence.id.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // English & Tamil
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sentence.english,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = sentence.tamil,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F766E), // Emerald Green
                    lineHeight = 17.sp
                )

                if (sentence.tip.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 ${sentence.tip}",
                        fontSize = 10.sp,
                        color = SubikshaTextMuted,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Audio Speaker Button
            IconButton(
                onClick = { if (isPlaying) onStop() else onSpeak() },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) accentColor else accentColor.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen",
                    tint = if (isPlaying) Color.White else accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==============================================================
// 3-Tense Comparison Card (Color-Coded)
// ==============================================================
@Composable
private fun TenseComparisonCard(
    item: TenseComparisonItem,
    onSpeak: (String, Int?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SubikshaNavy
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Past Tense (Blue/Indigo)
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSpeak(item.pastSentence, null) }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PAST (இறந்தகாலம்)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = item.pastSentence,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Text(
                        text = item.pastTamil,
                        fontSize = 11.sp,
                        color = Color(0xFF1E40AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Present Tense (Emerald Green)
            Surface(
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSpeak(item.presentSentence, null) }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRESENT (நிகழ்காலம்)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = item.presentSentence,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Text(
                        text = item.presentTamil,
                        fontSize = 11.sp,
                        color = Color(0xFF065F46)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Future Tense (Royal Purple)
            Surface(
                color = Color(0xFFF5F3FF),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSpeak(item.futureSentence, null) }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FUTURE (எதிர்காலம்)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaRoyalPurple
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = SubikshaRoyalPurple,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = item.futureSentence,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Text(
                        text = item.futureTamil,
                        fontSize = 11.sp,
                        color = Color(0xFF5B21B6)
                    )
                }
            }
        }
    }
}
