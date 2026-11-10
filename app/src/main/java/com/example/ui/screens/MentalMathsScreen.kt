package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.MathMethodDetail
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
            title = "100 Easy Additional Steps",
            subtitle = "Speed Addition Tricks & Vedic Methods",
            category = "கூட்டல்",
            totalTricks = "500 வழிகள்",
            assetFileName = "addition_500.pdf",
            accentColor = SubikshaSkyBlue,
            imageDrawableRes = R.drawable.card_mental_maths,
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
            imageDrawableRes = R.drawable.card_mental_maths,
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
            imageDrawableRes = R.drawable.card_mental_maths,
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
            imageDrawableRes = R.drawable.card_mental_maths,
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Layer 1: Mint green and floating lilac petals wallpaper
        Image(
            painter = painterResource(id = R.drawable.bg_maths),
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

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Sleek Gradient Header with perfectly aligned Avatar & Heading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SubikshaNavy, SubikshaRoyalPurple)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Elevated White Circular Back Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(40.dp)
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

                // Center Teacher Avatar & Title Info (No line breaks, crisp single line)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                // Right Audio Voice Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Voice Guide",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "குரல் 🔊",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Main No-Scroll Body Area: All 4 Categories visible together in one screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Tabs Row: Fits All 5 Options (அனைத்தும், கூட்டல், கழித்தல், பெருக்கல், வகுத்தல்) without scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SubikshaRoyalPurple else Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, if (isSelected) SubikshaRoyalPurple else Color.White.copy(alpha = 0.30f)),
                        shadowElevation = if (isSelected) 2.dp else 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCategorySelect(category) }
                            .testTag("category_tab_$category")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else SubikshaNavy,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 4 Equal-sized Course Cards (One by one rows) filling the remaining screen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredBooks.forEach { book ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onBookSelect(book) }
                            .testTag("math_book_${book.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rounded Category Icon Box
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(book.accentColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (book.category) {
                                        "கூட்டல்" -> Icons.Default.Add
                                        "கழித்தல்" -> Icons.Default.Remove
                                        "பெருக்கல்" -> Icons.Default.Close
                                        "வகுத்தல்" -> Icons.Default.HorizontalRule
                                        else -> Icons.Default.Calculate
                                    },
                                    contentDescription = null,
                                    tint = book.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = book.title,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SubikshaNavy,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        color = book.accentColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, book.accentColor.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            text = book.totalTricks,
                                            color = book.accentColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = book.description,
                                    fontSize = 11.5.sp,
                                    color = SubikshaTextMuted,
                                    lineHeight = 15.sp,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Open Button
                            Button(
                                onClick = { onBookSelect(book) },
                                colors = ButtonDefaults.buttonColors(containerColor = book.accentColor),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("திறக்க", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
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
    var showExplanationDialog by remember { mutableStateOf(false) }

    // Pinch-to-zoom & Pan gesture states
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Intercept Back Press
    BackHandler {
        tts?.stop()
        onBack()
    }

    // Load PDF Page Bitmap
    LaunchedEffect(book.assetFileName, currentPageIndex) {
        isLoadingPage = true
        zoomScale = 1f
        panOffsetX = 0f
        panOffsetY = 0f
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
        val currentDetail = remember(book.title, currentPageIndex, selectedTrickIndex) {
            MentalMathsTricksData.getDetailedExplanation(book.title, currentPageIndex, selectedTrickIndex)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE2E8F0))
                .padding(innerPadding)
        ) {
            // 1. PDF Page with Pinch-to-Zoom & Pan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(Color(0xFFCBD5E1)),
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
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(1f, 4.5f)
                                    if (zoomScale > 1f) {
                                        panOffsetX += pan.x
                                        panOffsetY += pan.y
                                    } else {
                                        panOffsetX = 0f
                                        panOffsetY = 0f
                                    }
                                }
                            }
                    ) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page ${currentPageIndex + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = zoomScale,
                                    scaleY = zoomScale,
                                    translationX = panOffsetX,
                                    translationY = panOffsetY
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Zoom Helper indicator / Reset button
                    if (zoomScale > 1.05f) {
                        Surface(
                            color = SubikshaNavy.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(14.dp)
                                .clickable {
                                    zoomScale = 1f
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOutMap,
                                    contentDescription = "Reset Zoom",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${(zoomScale * 100).toInt()}% (Reset)",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🔍 விரல்களால் பெரிதாக்கலாம் (Pinch to Zoom)",
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "PDF பக்கத்தை ஏற்ற முடியவில்லை.",
                        color = SubikshaNavy,
                        fontSize = 14.sp
                    )
                }
            }

            // 2. Comprehensive 3-Method Step-by-Step Scrollable Explanation Card
            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.85f)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Header with Question Example and Speaker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentDetail.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = book.accentColor
                            )
                            Text(
                                text = currentDetail.example,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SubikshaNavy
                            )
                        }

                        IconButton(
                            onClick = { speakTrick(selectedTrickIndex) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(book.accentColor.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Listen",
                                tint = book.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Method 1
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = currentDetail.method1Title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            currentDetail.method1Steps.forEach { step ->
                                Text(
                                    text = "• $step",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Method 2
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = currentDetail.method2Title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            currentDetail.method2Steps.forEach { step ->
                                Text(
                                    text = "• $step",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Method 3
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = currentDetail.method3Title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            currentDetail.method3Steps.forEach { step ->
                                Text(
                                    text = "• $step",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tip banner
                    Text(
                        text = "💡 குறிப்பு: ${currentDetail.tip}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
