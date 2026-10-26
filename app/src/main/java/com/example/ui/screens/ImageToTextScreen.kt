package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class OcrTab(val title: String) {
    EXTRACTED_TEXT("உரை"),
    KEY_WORDS("முக்கிய சொற்கள்"),
    EXPLANATION("விளக்கம்"),
    TAMIL_TRANSLATION("தமிழில்")
}

@Composable
fun ImageToTextScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State management
    var selectedTab by remember { mutableStateOf(OcrTab.TAMIL_TRANSLATION) } // Match reference screenshot
    var ocrResult by remember { mutableStateOf(ImageOcrProcessor.referenceBookResult) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(ocrResult.rawEnglishText) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Text to Speech
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var speakingWord by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale.US
                ttsEngine?.setPitch(1.15f)
                ttsEngine?.setSpeechRate(0.88f)
                isTtsReady = true
            }
        }
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speak(text: String, wordTag: String? = null) {
        if (!isTtsReady || ttsEngine == null) return
        ttsEngine?.stop()
        speakingWord = wordTag
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, wordTag ?: "ocr_speech")
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label நகலெடுக்கப்பட்டது (Copied)!", Toast.LENGTH_SHORT).show()
        snackbarMessage = "$label நகலெடுக்கப்பட்டது!"
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
            isProcessing = true
            coroutineScope.launch {
                delay(1200) // Simulate fast intelligent OCR
                ocrResult = ImageOcrProcessor.processExtractedText(ocrResult.rawEnglishText)
                editedText = ocrResult.rawEnglishText
                isProcessing = false
                Toast.makeText(context, "படத்தில் உள்ள உரை வெற்றிகரமாக கண்டறியப்பட்டது!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "கேமரா அனுமதி தேவை (Camera permission needed)", Toast.LENGTH_LONG).show()
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
            isProcessing = true
            coroutineScope.launch {
                delay(1200) // Simulate fast OCR
                ocrResult = ImageOcrProcessor.processExtractedText(ocrResult.rawEnglishText)
                editedText = ocrResult.rawEnglishText
                isProcessing = false
                Toast.makeText(context, "கேலரி படத்திலிருந்து உரை பெறப்பட்டது!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Palette matching the Reference Screenshot (Dark Navy + Cyan-Blue neon)
    val backgroundDark = Color(0xFF030712) // Midnight Black/Navy
    val cardBackground = Color(0xFF0B1528) // Deep Blue Navy Card
    val cyanAccent = Color(0xFF00B4D8)     // Neon Cyan Blue
    val cyanDark = Color(0xFF0077B6)       // Darker Ocean Blue
    val cyanBorder = Color(0xFF0284C7)     // Border cyan

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // 1. Top App Bar matching screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("ocr_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Image to Text",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Quick Action: Retake / Demo Switcher
                IconButton(
                    onClick = {
                        ocrResult = if (ocrResult.originalHeading == ImageOcrProcessor.referenceBookResult.originalHeading) {
                            ImageOcrProcessor.scienceSample
                        } else {
                            ImageOcrProcessor.referenceBookResult
                        }
                        editedText = ocrResult.rawEnglishText
                        Toast.makeText(context, "மாதிரிப் புத்தகம் மாற்றப்பட்டது!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(cyanAccent.copy(alpha = 0.15f))
                        .testTag("ocr_sample_switch_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CollectionsBookmark,
                        contentDescription = "Switch Sample",
                        tint = cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onAskGemini("இந்தப் புத்தக உரையை மேலும் ஆழமாக எனக்குத் தமிழில் விளக்குங்கள்:\n${ocrResult.rawEnglishText}") },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(cyanAccent.copy(alpha = 0.15f))
                        .testTag("ocr_ask_ai_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Ask AI",
                        tint = cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Scrollable Body
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 2. Original Book Image / Captured Photo Preview
                item {
                    BookImagePreviewCard(
                        bitmap = capturedBitmap,
                        ocrResult = ocrResult,
                        isProcessing = isProcessing,
                        cyanBorder = cyanBorder,
                        onCameraClick = {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        onGalleryClick = {
                            galleryLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                }

                // 3. Four Tabs Bar matching Screenshot exactly
                item {
                    OcrTabsSegment(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        cyanAccent = cyanAccent,
                        cyanDark = cyanDark
                    )
                }

                // 4. Result Screen Card based on active Tab
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.2.dp, cyanBorder.copy(alpha = 0.7f)),
                                RoundedCornerShape(18.dp)
                            )
                            .shadow(8.dp, RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            when (selectedTab) {
                                OcrTab.EXTRACTED_TEXT -> {
                                    ExtractedTextTabContent(
                                        ocrResult = ocrResult,
                                        onEdit = { showEditDialog = true },
                                        onSpeak = { speak(ocrResult.rawEnglishText) },
                                        onCopy = { copyToClipboard("அசல் ஆங்கில உரை", ocrResult.rawEnglishText) },
                                        cyanAccent = cyanAccent
                                    )
                                }

                                OcrTab.KEY_WORDS -> {
                                    KeyWordsTabContent(
                                        keyWords = ocrResult.keyWords,
                                        onSpeakWord = { word -> speak(word, word) },
                                        onSpeakAll = {
                                            val allWords = ocrResult.keyWords.joinToString(". ") { it.english }
                                            speak(allWords)
                                        },
                                        speakingWord = speakingWord,
                                        cyanAccent = cyanAccent
                                    )
                                }

                                OcrTab.EXPLANATION -> {
                                    ExplanationTabContent(
                                        explanation = ocrResult.detailedExplanation,
                                        onSpeak = {
                                            val text = "${ocrResult.detailedExplanation.headingTamil}. ${ocrResult.detailedExplanation.centralIdea}"
                                            speak(text)
                                        },
                                        cyanAccent = cyanAccent
                                    )
                                }

                                OcrTab.TAMIL_TRANSLATION -> {
                                    TamilTranslationTabContent(
                                        translation = ocrResult.fullTamilTranslation,
                                        onSpeak = { speak(ocrResult.fullTamilTranslation) },
                                        cyanAccent = cyanAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Bottom Cyan Gradient Action Button matching Reference Screenshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Button(
                    onClick = {
                        when (selectedTab) {
                            OcrTab.TAMIL_TRANSLATION -> copyToClipboard("தமிழ் மொழிபெயர்ப்பு", ocrResult.fullTamilTranslation)
                            OcrTab.EXTRACTED_TEXT -> copyToClipboard("ஆங்கில உரை", ocrResult.rawEnglishText)
                            OcrTab.KEY_WORDS -> {
                                val text = ocrResult.keyWords.joinToString("\n") { "${it.english} — ${it.tamil}" }
                                copyToClipboard("முக்கியச் சொற்கள்", text)
                            }
                            OcrTab.EXPLANATION -> {
                                val text = "${ocrResult.detailedExplanation.headingTamil}\n\n${ocrResult.detailedExplanation.centralIdea}\n\n${ocrResult.detailedExplanation.practicalApplication}"
                                copyToClipboard("விளக்கம்", text)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(cyanAccent, cyanDark)
                            )
                        )
                        .testTag("ocr_bottom_action_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (selectedTab) {
                                OcrTab.TAMIL_TRANSLATION -> "தமிழ் உரையை நகலெடு"
                                OcrTab.EXTRACTED_TEXT -> "ஆங்கில உரையை நகலெடு"
                                OcrTab.KEY_WORDS -> "சொற்களை நகலெடு"
                                OcrTab.EXPLANATION -> "விளக்கத்தை நகலெடு"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Edit Text Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "ஆங்கில உரையைத் திருத்துக",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = cyanAccent,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        ocrResult = ImageOcrProcessor.processExtractedText(editedText)
                        showEditDialog = false
                        Toast.makeText(context, "உரை புதுப்பிக்கப்பட்டது!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cyanDark)
                ) {
                    Text("புதுப்பி (Update)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("ரத்து (Cancel)", color = Color.LightGray)
                }
            },
            containerColor = cardBackground
        )
    }
}

// ==============================================================
// 1. Book Image Preview Card (Real Photo or Reference Book Style)
// ==============================================================
@Composable
private fun BookImagePreviewCard(
    bitmap: Bitmap?,
    ocrResult: OcrResult,
    isProcessing: Boolean,
    cyanBorder: Color,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.5.dp, cyanBorder), RoundedCornerShape(18.dp))
            .shadow(10.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1220)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Realistic Book Page Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 210.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9F7F1)) // Classic cream book page color
                    .border(BorderStroke(1.dp, Color(0xFFD4CEB8)), RoundedCornerShape(14.dp))
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF0077B6))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "படத்தில் உள்ள உரையை AI வாசிக்கிறது...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                } else if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured Book Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Printed Book Page simulation matching Reference Screenshot
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = ocrResult.originalHeading,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF0F172A),
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ocrResult.paragraphs.forEach { paragraph ->
                            Text(
                                text = paragraph,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF334155),
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two Main Buttons: Take Photo & From Gallery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCameraClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "படம் எடு (Camera)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onGalleryClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF00B4D8))
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color(0xFF00B4D8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gallery-லிருந்து",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00B4D8)
                    )
                }
            }
        }
    }
}

// ==============================================================
// 2. Four Tabs Segment (Matching Reference Screenshot)
// ==============================================================
@Composable
private fun OcrTabsSegment(
    selectedTab: OcrTab,
    onTabSelected: (OcrTab) -> Unit,
    cyanAccent: Color,
    cyanDark: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF081224),
        border = BorderStroke(1.2.dp, cyanAccent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OcrTab.values().forEach { tab ->
                val isSelected = tab == selectedTab

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(
                                    colors = listOf(cyanAccent, cyanDark)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent)
                                )
                            }
                        )
                        .clickable { onTabSelected(tab) }
                        .testTag("ocr_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }

                if (tab != OcrTab.values().last() && !isSelected) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                }
            }
        }
    }
}

// ==============================================================
// 3. Tab 1 Content: உரை (Extracted Text)
// ==============================================================
@Composable
private fun ExtractedTextTabContent(
    ocrResult: OcrResult,
    onEdit: () -> Unit,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    cyanAccent: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = cyanAccent.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Article,
                            contentDescription = null,
                            tint = cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "அசல் ஆங்கில உரை",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Text",
                        tint = cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onSpeak, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen English",
                        tint = cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy English",
                        tint = cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Divider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Text(
            text = ocrResult.rawEnglishText,
            fontSize = 15.sp,
            color = Color(0xFFF1F5F9),
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// ==============================================================
// 4. Tab 2 Content: முக்கிய சொற்கள் (Key Words)
// ==============================================================
@Composable
private fun KeyWordsTabContent(
    keyWords: List<OcrKeyWord>,
    onSpeakWord: (String) -> Unit,
    onSpeakAll: () -> Unit,
    speakingWord: String?,
    cyanAccent: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = cyanAccent.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = null,
                            tint = cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "முக்கியச் சொற்கள் & அர்த்தங்கள்",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onSpeakAll,
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, cyanAccent)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = cyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Listen All", fontSize = 11.sp, color = cyanAccent, fontWeight = FontWeight.Bold)
            }
        }

        Divider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        keyWords.forEach { word ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0D1B32))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = word.english,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = cyanAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = word.partOfSpeech,
                                fontSize = 10.sp,
                                color = cyanAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = word.tamil,
                        fontSize = 13.sp,
                        color = Color(0xFF67E8F9),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { onSpeakWord(word.english) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (speakingWord == word.english) cyanAccent else cyanAccent.copy(alpha = 0.15f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak",
                        tint = if (speakingWord == word.english) Color.White else cyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==============================================================
// 5. Tab 3 Content: விளக்கம் (Detailed Explanation)
// ==============================================================
@Composable
private fun ExplanationTabContent(
    explanation: OcrExplanation,
    onSpeak: () -> Unit,
    cyanAccent: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = cyanAccent.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = explanation.headingTamil,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            IconButton(onClick = onSpeak, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen",
                    tint = cyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Divider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Central Idea
        Surface(
            color = cyanAccent.copy(alpha = 0.08f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, cyanAccent.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🎯 மையக் கருத்து (Core Concept):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = cyanAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = explanation.centralIdea,
                    fontSize = 13.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Paragraph Explanation
        Text(
            text = "விரிவான கருத்து விளக்கம்:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))

        explanation.paragraphsExplanation.forEach { item ->
            Text(
                text = item,
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Practical Application
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🌱 அன்றாட வாழ்வில் பயன்பாடு:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = explanation.practicalApplication,
                    fontSize = 12.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ==============================================================
// 6. Tab 4 Content: தமிழில் (Full Tamil Translation)
// ==============================================================
@Composable
private fun TamilTranslationTabContent(
    translation: String,
    onSpeak: () -> Unit,
    cyanAccent: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = cyanAccent.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = cyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "தமிழ் மொழிபெயர்ப்பு",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            IconButton(onClick = onSpeak, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen",
                    tint = cyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Divider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Translation text matching Screenshot typography exactly
        Text(
            text = translation,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            lineHeight = 24.sp
        )
    }
}
