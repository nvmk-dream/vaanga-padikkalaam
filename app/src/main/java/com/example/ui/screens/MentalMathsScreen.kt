package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MentalMathsTricksData
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class MathBook(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val totalTricks: String,
    val assetFileName: String,
    val accentColor: Color,
    val imageDrawableRes: Int,
    val description: String
)

@Composable
fun MentalMathsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedBook by remember { mutableStateOf<MathBook?>(null) }
    var selectedCategory by remember { mutableStateOf("அனைத்தும்") }

    // TTS Teacher Speaker
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale("ta", "IN"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale.ENGLISH)
                    }
                    engine.setPitch(1.15f) // Friendly female teacher pitch
                    engine.setSpeechRate(0.95f) // Clear learning speed
                }
                isTtsReady = true
            }
        }
        ttsInstance = tts

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val books = listOf(
        MathBook(
            id = "addition",
            title = "கூட்டல் 500 எளிய வழிகள்",
            subtitle = "Speed Addition Tricks & Vedic Methods",
            category = "கூட்டல்",
            totalTricks = "500 வழிகள்",
            assetFileName = "addition_500.pdf",
            accentColor = SubikshaSkyBlue,
            imageDrawableRes = R.drawable.card_gemini_chat,
            description = "விரல் விட்டு எண்ணாமல் மனதிலேயே நொடியில் 2 மற்றும் 3 இலக்க எண்களைக் கூட்டும் அற்புத உத்திகள்."
        ),
        MathBook(
            id = "subtraction",
            title = "கழித்தல் 500 எளிய வழிகள்",
            subtitle = "Speed Subtraction Shortcuts",
            category = "கழித்தல்",
            totalTricks = "500 வழிகள்",
            assetFileName = "addition_500.pdf", // Graceful fallback
            accentColor = SubikshaMintGreen,
            imageDrawableRes = R.drawable.card_image_to_text,
            description = "கடன் வாங்காமல் மனக்கணக்காகவே பெரிய எண்களை எளிதில் கழிக்கும் எளிய கணித வித்தைகள்."
        ),
        MathBook(
            id = "multiplication",
            title = "பெருக்கல் 500 எளிய வழிகள்",
            subtitle = "Vedic Multiplication Magic",
            category = "பெருக்கல்",
            totalTricks = "500 வழிகள்",
            assetFileName = "addition_500.pdf", // Graceful fallback
            accentColor = SubikshaWarmOrange,
            imageDrawableRes = R.drawable.card_spoken_english,
            description = "வாய்ப்பாடுகளை மனப்பாடம் செய்யாமல் குறுக்கு பெருக்கல் முறையில் நொடியில் விடை காணும் வழிகள்."
        ),
        MathBook(
            id = "division",
            title = "வகுத்தல் 500 எளிய வழிகள்",
            subtitle = "Mental Division & Fraction Shortcuts",
            category = "வகுத்தல்",
            totalTricks = "500 வழிகள்",
            assetFileName = "division_500.pdf",
            accentColor = SubikshaPink,
            imageDrawableRes = R.drawable.card_voice_chat,
            description = "மீதியில்லா வகுத்தல் மற்றும் பெரிய எண்களை நொடிகளில் சுருக்கும் மனக்கணித யுக்திகள்."
        )
    )

    if (selectedBook != null) {
        MentalMathsPdfViewerScreen(
            book = selectedBook!!,
            tts = ttsInstance,
            isTtsReady = isTtsReady,
            onBack = {
                ttsInstance?.stop()
                selectedBook = null
            }
        )
    } else {
        MentalMathsLibraryScreen(
            books = books,
            selectedCategory = selectedCategory,
            onCategorySelect = { selectedCategory = it },
            onBookSelect = { book -> selectedBook = book },
            onBackClick = onBackClick
        )
    }
}

/**
 * 1. ULTRA DELUXE LIBRARY SCREEN (Matching Home Page aesthetic)
 */
@Composable
fun MentalMathsLibraryScreen(
    books: List<MathBook>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onBookSelect: (MathBook) -> Unit,
    onBackClick: () -> Unit
) {
    val categories = listOf("அனைத்தும்", "கூட்டல்", "கழித்தல்", "பெருக்கல்", "வகுத்தல்")
    val filteredBooks = if (selectedCategory == "அனைத்தும்") {
        books
    } else {
        books.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SubikshaBackground)
    ) {
        // Ultra Deluxe Gradient Header (Matching Home Page)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SubikshaNavy, SubikshaRoyalPurple)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Elevated White Circular Back Button (Matching Home)
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("maths_back_button")
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SubikshaNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Center Teacher Avatar & Title Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, SubikshaPastelPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.teacher_avatar),
                                contentDescription = "Subiksha Teacher Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "மனக்கணக்கு நூலகம்",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SubikshaMintGreen)
                                )
                            }
                            Text(
                                text = "Mental Maths • 500 எளிய வழிகள்",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Right Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Audio Voice Guide",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "பெண் குரல் 🔊",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Natural Realistic Hero Card (Matching Home Welcome Banner)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.welcome_card),
                            contentDescription = "Mental Maths Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            SubikshaNavy.copy(alpha = 0.85f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    color = SubikshaPink,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "ULTRA DELUXE MATHS",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "மூளைக்கு சுறுசுறுப்புப் பயிற்சி",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "500 எளிய வழிகளுடன் கூடிய விரிவான PDF கையேடு",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Natural Realistic Tabs / Category Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) SubikshaRoyalPurple else Color.White,
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onCategorySelect(category) }
                                .testTag("category_tab_$category")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = when (category) {
                                        "கூட்டல்" -> Icons.Default.Add
                                        "கழித்தல்" -> Icons.Default.Remove
                                        "பெருக்கல்" -> Icons.Default.Close
                                        "வகுத்தல்" -> Icons.Default.HorizontalRule
                                        else -> Icons.Default.Calculate
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else SubikshaNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category,
                                    color = if (isSelected) Color.White else SubikshaNavy,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Book Cards with Realistic Image & Details
            items(filteredBooks) { book ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookSelect(book) }
                        .testTag("math_book_${book.id}")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Realistic Image Header for Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Image(
                                painter = painterResource(id = book.imageDrawableRes),
                                contentDescription = book.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Badge Overlay
                            Surface(
                                color = book.accentColor,
                                shape = RoundedCornerShape(bottomStart = 12.dp),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = book.totalTricks,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Content Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(book.accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = book.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubikshaNavy
                                )
                                Text(
                                    text = book.subtitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SubikshaRoyalPurple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = book.description,
                                    fontSize = 12.sp,
                                    color = SubikshaTextMuted,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onBookSelect(book) },
                                colors = ButtonDefaults.buttonColors(containerColor = book.accentColor),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("திறக்க", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. ULTRA DELUXE PDF VIEWER SCREEN
 * Renders Android native PDF pages with zoom controls and female teacher voice guidance for each trick!
 */
@Composable
fun MentalMathsPdfViewerScreen(
    book: MathBook,
    tts: TextToSpeech?,
    isTtsReady: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(1) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(true) }
    var selectedTrickIndex by remember { mutableIntStateOf(0) }
    var speakingTrickText by remember { mutableStateOf<String?>(null) }

    // Intercept Back Press
    BackHandler {
        tts?.stop()
        onBack()
    }

    // Load PDF Page Bitmap
    LaunchedEffect(book.assetFileName, currentPageIndex) {
        isLoadingPage = true
        withContext(Dispatchers.IO) {
            try {
                val tempFile = File(context.cacheDir, "temp_${book.assetFileName}")
                if (!tempFile.exists()) {
                    context.assets.open("pdf/${book.assetFileName}").use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                totalPages = renderer.pageCount
                val safePageIndex = currentPageIndex.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
                val page = renderer.openPage(safePageIndex)

                // High resolution render
                val width = (page.width * 2f).toInt()
                val height = (page.height * 2f).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                pfd.close()

                withContext(Dispatchers.Main) {
                    currentBitmap = bitmap
                    isLoadingPage = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingPage = false
                }
            }
        }
    }

    // Function to speak trick explanation
    fun speakTrick(trickIndex: Int) {
        selectedTrickIndex = trickIndex
        val speech = MentalMathsTricksData.getSpeechForTrick(book.title, currentPageIndex, trickIndex)
        speakingTrickText = speech
        if (isTtsReady && tts != null) {
            tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null, "trick_$trickIndex")
        }
    }

    Scaffold(
        topBar = {
            // Ultra Deluxe Top Bar (Subiksha Navy to Royal Purple Gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(SubikshaNavy, SubikshaRoyalPurple)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SubikshaNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Title & Page indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = book.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "பக்கம் ${currentPageIndex + 1} / $totalPages",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Speaker Voice Button
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = { speakTrick(selectedTrickIndex) }) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak Trick",
                                tint = SubikshaRoyalPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Ultra Deluxe Bottom Bar for selecting sums 1-20
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // Speaking Banner if active
                    if (!speakingTrickText.isNullOrBlank()) {
                        Surface(
                            color = SubikshaPastelPurple.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = SubikshaRoyalPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = speakingTrickText ?: "",
                                    fontSize = 11.sp,
                                    color = SubikshaNavy,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    // Sums Selector Label & Page Navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "கணக்குகள் (ஒலியில் கேட்க தொடவும்):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (currentPageIndex > 0) {
                                        currentPageIndex--
                                        selectedTrickIndex = 0
                                    }
                                },
                                enabled = currentPageIndex > 0,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page")
                            }

                            Text(
                                text = "${currentPageIndex + 1}/$totalPages",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaRoyalPurple
                            )

                            IconButton(
                                onClick = {
                                    if (currentPageIndex < totalPages - 1) {
                                        currentPageIndex++
                                        selectedTrickIndex = 0
                                    }
                                },
                                enabled = currentPageIndex < totalPages - 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                            }
                        }
                    }

                    // Row of 20 sums chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(top = 6.dp)
                    ) {
                        items(20) { index ->
                            val isSelected = index == selectedTrickIndex
                            val questionNumber = currentPageIndex * 20 + index + 1
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) book.accentColor else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { speakTrick(index) }
                                    .testTag("trick_chip_$index")
                            ) {
                                Text(
                                    text = "#$questionNumber",
                                    color = if (isSelected) Color.White else SubikshaNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE2E8F0))
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingPage) {
                CircularProgressIndicator(color = book.accentColor)
            } else if (currentBitmap != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page ${currentPageIndex + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                Text(
                    text = "PDF பக்கத்தை ஏற்ற முடியவில்லை.",
                    color = SubikshaNavy,
                    fontSize = 14.sp
                )
            }
        }
    }
}
