package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
                // Try Indian English first for sweet native accent, fallback to US
                val inLocale = Locale("en", "IN")
                val langResult = ttsEngine?.setLanguage(inLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsEngine?.language = Locale.US
                }

                // Search and set Female voice suitable for teaching
                try {
                    val femaleVoice = ttsEngine?.voices?.find { voice ->
                        val nameLower = voice.name.lowercase(Locale.ROOT)
                        val isEn = voice.locale.language == "en"
                        isEn && (nameLower.contains("female") || 
                                 nameLower.contains("fem") || 
                                 nameLower.contains("en-in") || 
                                 nameLower.contains("en_in"))
                    }
                    if (femaleVoice != null) {
                        ttsEngine?.voice = femaleVoice
                    }
                } catch (_: Exception) {}

                // Sweet, clear female teacher pitch and pleasant cadence
                ttsEngine?.setPitch(1.18f)
                isTtsReady = true
            }
        }
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakText(rawText: String, id: Int? = null) {
        if (!isTtsReady || ttsEngine == null) return
        ttsEngine?.stop()

        // 1. Clean out all occurrences of V1, V2, V3 and dashed blanks from voice listening
        val cleanedText = rawText
            .replace(Regex("(?i)\\(\\s*v[1-3]\\s*\\)"), "")
            .replace(Regex("(?i)\\bv[1-3]\\b"), "")
            .replace(Regex("([_—–]+(?:\\s*[_—–]+)*|-{2,}(?:\\s*-{2,})*)"), " dash ")
            .replace("(", "")
            .replace(")", "")
            .trim()

        // 2. Check if this is a Verb Drill (e.g. "Eat - Ate - Eaten" or words separated by '-')
        val isVerbDrill = cleanedText.contains("-")
        if (isVerbDrill) {
            val words = cleanedText.split("-", ",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.equals("v1", true) && !it.equals("v2", true) && !it.equals("v3", true) }

            if (words.size > 1) {
                ttsEngine?.setPitch(1.18f)
                ttsEngine?.setSpeechRate(if (slowSpeed) 0.65f else 0.82f)
                currentlySpeakingSentenceId = id

                // Speak word 1 -> 1-sec silence pause -> word 2 -> 1-sec silence pause -> word 3
                words.forEachIndexed { index, word ->
                    if (index == 0) {
                        ttsEngine?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "${id}_verb_$index")
                    } else {
                        // Exactly 1 second (1000ms) silence pause between verb forms
                        ttsEngine?.playSilentUtterance(1000L, TextToSpeech.QUEUE_ADD, "${id}_pause_$index")
                        ttsEngine?.speak(word, TextToSpeech.QUEUE_ADD, null, "${id}_verb_$index")
                    }
                }
                return
            }
        }

        // 3. Standard sentence speech with female cadence
        ttsEngine?.setPitch(1.18f)
        ttsEngine?.setSpeechRate(if (slowSpeed) 0.72f else 0.88f)
        currentlySpeakingSentenceId = id
        ttsEngine?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, id?.toString() ?: "sentence_id")
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Layer 1: Blue floating water flowers wallpaper
        Image(
            painter = painterResource(id = R.drawable.bg_spoken),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay to ensure contrast and readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.05f))
        )
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
// ==============================================================
// 1. Books Overview View (Main Page - Re-designed to match My Library Spec)
// ==============================================================
@Composable
private fun BooksOverviewView(
    onBack: () -> Unit,
    onSelectBook: (String) -> Unit,
    onAskGemini: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("spoken_custom_books_prefs", android.content.Context.MODE_PRIVATE)
    }

    // Dynamic titles for Books 3, 4, 5, 6 saved in SharedPreferences
    var book3Title by remember { mutableStateOf(sharedPrefs.getString("book_3_title", "Add Book 3") ?: "Add Book 3") }
    var book4Title by remember { mutableStateOf(sharedPrefs.getString("book_4_title", "Add Book 4") ?: "Add Book 4") }
    var book5Title by remember { mutableStateOf(sharedPrefs.getString("book_5_title", "Add Book 5") ?: "Add Book 5") }
    var book6Title by remember { mutableStateOf(sharedPrefs.getString("book_6_title", "Add Book 6") ?: "Add Book 6") }

    // Custom file URIs for Books 1 to 6 (saved in SharedPreferences)
    var book1Uri by remember { mutableStateOf(sharedPrefs.getString("book_1_uri", null)) }
    var book2Uri by remember { mutableStateOf(sharedPrefs.getString("book_2_uri", null)) }
    var book3Uri by remember { mutableStateOf(sharedPrefs.getString("book_3_uri", null)) }
    var book4Uri by remember { mutableStateOf(sharedPrefs.getString("book_4_uri", null)) }
    var book5Uri by remember { mutableStateOf(sharedPrefs.getString("book_5_uri", null)) }
    var book6Uri by remember { mutableStateOf(sharedPrefs.getString("book_6_uri", null)) }

    // Dialog state for setting custom book titles & picking files
    var editingBookIndex by remember { mutableStateOf<Int?>(null) }
    var inputDialogTitle by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<String?>(null) }

    // File Picker Launcher for PDF or Images
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            selectedFileUri = uri.toString()
            android.widget.Toast.makeText(context, "கோப்பு இணைக்கப்பட்டது!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (editingBookIndex != null) {
        val bIdx = editingBookIndex!!
        AlertDialog(
            onDismissRequest = { editingBookIndex = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditDocument,
                        contentDescription = null,
                        tint = SubikshaRoyalPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "புத்தகம் $bIdx மேலாண்மை (Book Management)",
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "புத்தகத் தலைப்பு (Book Title):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = inputDialogTitle,
                        onValueChange = { inputDialogTitle = it },
                        placeholder = { Text("புத்தகத்தின் பெயர்...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "புத்தகக் கோப்பு (PDF or Image):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SubikshaRoyalPurple
                        )
                    ) {
                        Icon(
                            imageVector = if (selectedFileUri != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = if (selectedFileUri != null) Color(0xFF10B981) else SubikshaRoyalPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedFileUri != null) "கோப்பு தேர்வு செய்யப்பட்டது ✓" else "+ PDF / படம் தேர்வு செய்",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedFileUri != null) {
                        Text(
                            text = "இணைப்பு: ${selectedFileUri?.substringAfterLast('/')?.take(30)}...",
                            fontSize = 10.sp,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Reset option for Books 1 and 2
                    if (bIdx in 1..2 && (selectedFileUri != null || (bIdx == 1 && book1Uri != null) || (bIdx == 2 && book2Uri != null))) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = {
                                selectedFileUri = null
                                if (bIdx == 1) {
                                    book1Uri = null
                                    sharedPrefs.edit().remove("book_1_uri").apply()
                                } else {
                                    book2Uri = null
                                    sharedPrefs.edit().remove("book_2_uri").apply()
                                }
                                android.widget.Toast.makeText(context, "இயல்புநிலை பாடப் புத்தகத்திற்கு மாற்றப்பட்டது!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("இயல்புநிலைக்கு மாற்று (Restore Default)", fontSize = 11.sp, color = Color(0xFFDC2626))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val savedText = inputDialogTitle.trim()
                        when (bIdx) {
                            1 -> {
                                if (savedText.isNotEmpty()) sharedPrefs.edit().putString("book_1_title", savedText).apply()
                                if (selectedFileUri != null) {
                                    book1Uri = selectedFileUri
                                    sharedPrefs.edit().putString("book_1_uri", selectedFileUri).apply()
                                }
                            }
                            2 -> {
                                if (savedText.isNotEmpty()) sharedPrefs.edit().putString("book_2_title", savedText).apply()
                                if (selectedFileUri != null) {
                                    book2Uri = selectedFileUri
                                    sharedPrefs.edit().putString("book_2_uri", selectedFileUri).apply()
                                }
                            }
                            3 -> {
                                val t = savedText.ifEmpty { "Add Book 3" }
                                book3Title = t
                                sharedPrefs.edit().putString("book_3_title", t).apply()
                                if (selectedFileUri != null) {
                                    book3Uri = selectedFileUri
                                    sharedPrefs.edit().putString("book_3_uri", selectedFileUri).apply()
                                }
                            }
                            4 -> {
                                val t = savedText.ifEmpty { "Add Book 4" }
                                book4Title = t
                                sharedPrefs.edit().putString("book_4_title", t).apply()
                                if (selectedFileUri != null) {
                                    book4Uri = selectedFileUri
                                    sharedPrefs.edit().putString("book_4_uri", selectedFileUri).apply()
                                }
                            }
                            5 -> {
                                val t = savedText.ifEmpty { "Add Book 5" }
                                book5Title = t
                                sharedPrefs.edit().putString("book_5_title", t).apply()
                                if (selectedFileUri != null) {
                                    book5Uri = selectedFileUri
                                    sharedPrefs.edit().putString("book_5_uri", selectedFileUri).apply()
                                }
                            }
                            6 -> {
                                val t = savedText.ifEmpty { "Add Book 6" }
                                book6Title = t
                                sharedPrefs.edit().putString("book_6_title", t).apply()
                                if (selectedFileUri != null) {
                                    book6Uri = selectedFileUri
                                    sharedPrefs.edit().putString("book_6_uri", selectedFileUri).apply()
                                }
                            }
                        }
                        android.widget.Toast.makeText(context, "புத்தகம் வெற்றிகரமாகச் சேமிக்கப்பட்டது!", android.widget.Toast.LENGTH_SHORT).show()
                        editingBookIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("சேமி (Save)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBookIndex = null }) {
                    Text("ரத்து (Cancel)", color = SubikshaNavy)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Header (Matching My Library Screen Header Structure)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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

        // Main Content Area: Zero-scroll compact layout with 6 cards and Bottom AI Banner
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 2. Title Banner Strip (Matching My Library "பாட நூலகப் பிரிவுகள்" Banner)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.20f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📚", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "பாடப் புத்தகங்கள்",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )
                    }

                    Surface(
                        color = SubikshaPastelPurple.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "6 புத்தகங்கள்",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaRoyalPurple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. 6 Book Cards (Image 2 My Library Style - Uniform Height, Glassmorphism, Zero-scroll)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Book 1 (With replace option)
                SpokenBookRowCard(
                    title = sharedPrefs.getString("book_1_title", "Spoken English Through Tamil – Book 1") ?: "Spoken English Through Tamil – Book 1",
                    subtitle = if (book1Uri != null) "மாற்றப்பட்ட தனிப்பயன் புத்தகம் (PDF/Image இணைக்கப்பட்டது)" else "அன்றாடப் பேச்சு ஆங்கிலம் - புத்தகம் 1",
                    number = 1,
                    primaryColor = Color(0xFF2563EB),     // Vibrant Royal Blue
                    secondaryColor = Color(0xFF3B82F6),
                    icon = Icons.Default.MenuBook,
                    hasCustomFile = book1Uri != null,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = {
                        if (book1Uri != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(book1Uri), if (book1Uri!!.contains(".pdf", true)) "application/pdf" else "image/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                onSelectBook("book_1")
                            }
                        } else {
                            onSelectBook("book_1")
                        }
                    },
                    onReplace = {
                        inputDialogTitle = sharedPrefs.getString("book_1_title", "Spoken English Through Tamil – Book 1") ?: ""
                        selectedFileUri = book1Uri
                        editingBookIndex = 1
                    }
                )

                // Card 2: Book 2 (With replace option)
                SpokenBookRowCard(
                    title = sharedPrefs.getString("book_2_title", "English Tenses Through Tamil – Book 2") ?: "English Tenses Through Tamil – Book 2",
                    subtitle = if (book2Uri != null) "மாற்றப்பட்ட தனிப்பயன் புத்தகம் (PDF/Image இணைக்கப்பட்டது)" else "ஆங்கிலக் காலங்கள் - புத்தகம் 2 (12 Tenses)",
                    number = 2,
                    primaryColor = Color(0xFF0284C7),     // Deep Sky Cyan
                    secondaryColor = Color(0xFF38BDF8),
                    icon = Icons.Default.MenuBook,
                    hasCustomFile = book2Uri != null,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = {
                        if (book2Uri != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(book2Uri), if (book2Uri!!.contains(".pdf", true)) "application/pdf" else "image/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                onSelectBook("book_2")
                            }
                        } else {
                            onSelectBook("book_2")
                        }
                    },
                    onReplace = {
                        inputDialogTitle = sharedPrefs.getString("book_2_title", "English Tenses Through Tamil – Book 2") ?: ""
                        selectedFileUri = book2Uri
                        editingBookIndex = 2
                    }
                )

                // Card 3: Add Book 3
                SpokenBookRowCard(
                    title = book3Title,
                    subtitle = if (book3Uri != null) "சேமிக்கப்பட்ட ஆவணம் (தட்டினால் திறக்கும்)" else if (book3Title == "Add Book 3") "புதிய PDF/படம் சேர்க்க தட்டவும்" else "சேமிக்கப்பட்ட புத்தகம் 3",
                    number = 3,
                    primaryColor = Color(0xFF059669),     // Emerald Green
                    secondaryColor = Color(0xFF34D399),
                    icon = if (book3Uri != null) Icons.Default.MenuBook else Icons.Default.PostAdd,
                    hasCustomFile = book3Uri != null,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = {
                        if (book3Uri != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(book3Uri), if (book3Uri!!.contains(".pdf", true)) "application/pdf" else "image/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                inputDialogTitle = if (book3Title != "Add Book 3") book3Title else ""
                                selectedFileUri = book3Uri
                                editingBookIndex = 3
                            }
                        } else {
                            inputDialogTitle = if (book3Title != "Add Book 3") book3Title else ""
                            selectedFileUri = book3Uri
                            editingBookIndex = 3
                        }
                    },
                    onReplace = {
                        inputDialogTitle = if (book3Title != "Add Book 3") book3Title else ""
                        selectedFileUri = book3Uri
                        editingBookIndex = 3
                    }
                )

                // Card 4: Add Book 4
                SpokenBookRowCard(
                    title = book4Title,
                    subtitle = if (book4Uri != null) "சேமிக்கப்பட்ட ஆவணம் (தட்டினால் திறக்கும்)" else if (book4Title == "Add Book 4") "புதிய PDF/படம் சேர்க்க தட்டவும்" else "சேமிக்கப்பட்ட புத்தகம் 4",
                    number = 4,
                    primaryColor = Color(0xFFEA580C),     // Warm Orange
                    secondaryColor = Color(0xFFFB923C),
                    icon = if (book4Uri != null) Icons.Default.MenuBook else Icons.Default.PostAdd,
                    hasCustomFile = book4Uri != null,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = {
                        if (book4Uri != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(book4Uri), if (book4Uri!!.contains(".pdf", true)) "application/pdf" else "image/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                inputDialogTitle = if (book4Title != "Add Book 4") book4Title else ""
                                selectedFileUri = book4Uri
                                editingBookIndex = 4
                            }
                        } else {
                            inputDialogTitle = if (book4Title != "Add Book 4") book4Title else ""
                            selectedFileUri = book4Uri
                            editingBookIndex = 4
                        }
                    },
                    onReplace = {
                        inputDialogTitle = if (book4Title != "Add Book 4") book4Title else ""
                        selectedFileUri = book4Uri
                        editingBookIndex = 4
                    }
                )

                // Card 5: Add Book 5
                SpokenBookRowCard(
                    title = book5Title,
                    subtitle = if (book5Uri != null) "சேமிக்கப்பட்ட ஆவணம் (தட்டினால் திறக்கும்)" else if (book5Title == "Add Book 5") "புதிய PDF/படம் சேர்க்க தட்டவும்" else "சேமிக்கப்பட்ட புத்தகம் 5",
                    number = 5,
                    primaryColor = Color(0xFF9333EA),     // Royal Purple
                    secondaryColor = Color(0xFFA855F7),
                    icon = if (book5Uri != null) Icons.Default.MenuBook else Icons.Default.PostAdd,
                    hasCustomFile = book5Uri != null,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = {
                        if (book5Uri != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(book5Uri), if (book5Uri!!.contains(".pdf", true)) "application/pdf" else "image/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                inputDialogTitle = if (book5Title != "Add Book 5") book5Title else ""
                                selectedFileUri = book5Uri
                                editingBookIndex = 5
                            }
                        } else {
                            inputDialogTitle = if (book5Title != "Add Book 5") book5Title else ""
                            selectedFileUri = book5Uri
                            editingBookIndex = 5
                        }
                    },
                    onReplace = {
                        inputDialogTitle = if (book5Title != "Add Book 5") book5Title else ""
                        selectedFileUri = book5Uri
                        editingBookIndex = 5
                    }
                )

                // Card 6: Add Book 6
                SpokenBookRowCard(
                    title = book6Title,
                    subtitle = if (book6Uri != null) "சேமிக்கப்பட்ட ஆவணம் (தட்டினால் திறக்கும்)" else if (book6Title == "Add Book 6") "புதிய PDF/படம் சேர்க்க தட்டவும்" else "சேமிக்கப்பட்ட புத்தகம் 6",
                    number = 6,
                    primaryColor = Color(0xFF0D9488),     // Teal Green
                    secondaryColor = Color(0xFF2DD4BF),
                    icon = if (book6Uri != null) Icons.Default.MenuBook else Icons.Default.PostAdd,
                    hasCustomFile = book6Uri != null,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onClick = {
                        if (book6Uri != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(book6Uri), if (book6Uri!!.contains(".pdf", true)) "application/pdf" else "image/*")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                inputDialogTitle = if (book6Title != "Add Book 6") book6Title else ""
                                selectedFileUri = book6Uri
                                editingBookIndex = 6
                            }
                        } else {
                            inputDialogTitle = if (book6Title != "Add Book 6") book6Title else ""
                            selectedFileUri = book6Uri
                            editingBookIndex = 6
                        }
                    },
                    onReplace = {
                        inputDialogTitle = if (book6Title != "Add Book 6") book6Title else ""
                        selectedFileUri = book6Uri
                        editingBookIndex = 6
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Bottom AI Banner (Matching Image 2 Bottom Navigation Card with '<' and '>>')
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.16f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAskGemini("ஆங்கிலத்தில் சரளமாகப் பேச சில எளிய வழிகளைத் தமிழில் சொல்லுங்கள்.") }
                    .testTag("spoken_ai_assistant_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Arrow (<)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = SubikshaRoyalPurple,
                        modifier = Modifier.size(18.dp)
                    )

                    // Center Content with Sparkle Icon and Tamil Text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI-யிடம் சந்தேகம் கேட்கலாம்",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    // Right Double-Arrow (>>)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ask AI",
                        tint = SubikshaRoyalPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ==============================================================
// Spoken Book Row Card (Image 2 My Library Compact Card Exact Replica)
// ==============================================================
@Composable
private fun SpokenBookRowCard(
    title: String,
    subtitle: String,
    number: Int,
    primaryColor: Color,
    secondaryColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hasCustomFile: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onReplace: () -> Unit
) {
    // Elegant Translucent Glassmorphism Card (Exact replica of Image 2 My Library Category Card)
    Card(
        modifier = modifier
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("spoken_book_card_$number"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.14f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge with Gradient and subtle shadow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(primaryColor, secondaryColor)
                        )
                    )
                    .shadow(3.dp, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Titles
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (hasCustomFile) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Custom Attached",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Replace / Edit Button
            IconButton(
                onClick = onReplace,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.10f))
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Replace Book",
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Elegant Glass Count Badge
            Surface(
                color = primaryColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, primaryColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "$number",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = primaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

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
