package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.example.data.ApiKeyManager
import com.example.data.GeminiService
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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
import com.example.ui.components.ApiKeyDialog
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
    val apiKeyManager = remember { ApiKeyManager(context) }
    val geminiService = remember { GeminiService(apiKeyManager) }

    // State management
    var selectedTab by remember { mutableStateOf(OcrTab.TAMIL_TRANSLATION) }
    var ocrResult by remember { mutableStateOf(ImageOcrProcessor.referenceBookResult) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingStatus by remember { mutableStateOf("படத்தில் உள்ள உரையை AI வாசிக்கிறது...") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(ocrResult.rawEnglishText) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var hasValidKey by remember { mutableStateOf(apiKeyManager.getApiKey().isNotBlank()) }

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

    fun runOcrOnBitmap(bitmap: Bitmap) {
        isProcessing = true
        processingStatus = "பாடப்புத்தக உரையை ஸ்கேன் செய்கிறது..."
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val ocrRaw = visionText.text.trim()
                    
                    // Show preliminary extracted text immediately
                    if (ocrRaw.isNotEmpty()) {
                        ocrResult = ImageOcrProcessor.buildFallbackResult(ocrRaw)
                        editedText = ocrResult.rawEnglishText
                    }

                    // Call Gemini Vision to accurately inspect the image & OCR text
                    processingStatus = "சுபிக்ஷா AI ஆசிரியர் பாடத்தை ஆழமாகப் பகுப்பாய்வு செய்கிறது..."
                    coroutineScope.launch {
                        val aiResult = geminiService.analyzeBookImage(bitmap, ocrRaw)
                        aiResult.onSuccess { structuredResult ->
                            ocrResult = structuredResult
                            editedText = structuredResult.rawEnglishText
                            Toast.makeText(context, "பாடப்புத்தகம் வெற்றிகரமாக ஆய்வு செய்யப்பட்டது!", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            val msg = err.localizedMessage ?: "AI ஆய்வு தோல்வியடைந்தது"
                            Toast.makeText(context, "AI ஆய்வு: $msg", Toast.LENGTH_LONG).show()
                        }
                        isProcessing = false
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    processingStatus = "AI ஆசிரியர் படத்தை நேரடியாகப் படிக்கிறது..."
                    coroutineScope.launch {
                        val aiResult = geminiService.analyzeBookImage(bitmap, "")
                        aiResult.onSuccess { structuredResult ->
                            ocrResult = structuredResult
                            editedText = structuredResult.rawEnglishText
                            Toast.makeText(context, "பாடப்புத்தகம் வெற்றிகரமாக ஆய்வு செய்யப்பட்டது!", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(context, "AI பிழை: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                        isProcessing = false
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            isProcessing = false
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
            runOcrOnBitmap(bitmap)
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
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                capturedBitmap = bitmap
                if (bitmap != null) {
                    runOcrOnBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "படத்தை ஏற்றுவதில் பிழை ஏற்பட்டது.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val backgroundDark = Color(0xFF030712)
    val cardBackground = Color(0xFF0B1528)
    val cyanAccent = Color(0xFF00B4D8)
    val cyanDark = Color(0xFF0077B6)
    val cyanBorder = Color(0xFF0284C7)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .testTag("ocr_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Image to Text",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // API Key Setup Button
                IconButton(
                    onClick = { showKeyDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (hasValidKey) Color(0xFF10B981).copy(alpha = 0.2f) else cyanAccent.copy(alpha = 0.15f))
                        .testTag("ocr_api_key_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Gemini Key",
                        tint = if (hasValidKey) Color(0xFF10B981) else cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Demo Switcher
                IconButton(
                    onClick = {
                        capturedBitmap = null
                        selectedImageUri = null
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

                Spacer(modifier = Modifier.width(6.dp))

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
                // Book Image / Captured Photo Preview
                item {
                    BookImagePreviewCard(
                        bitmap = capturedBitmap,
                        ocrResult = ocrResult,
                        isProcessing = isProcessing,
                        processingStatus = processingStatus,
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

                // 4 Interactive Navigation Tabs
                item {
                    OcrTabsRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        cyanAccent = cyanAccent
                    )
                }

                // Tab Content Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(18.dp))
                            .shadow(6.dp, RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
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

            // Bottom Action Button
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

    // Key Setup Dialog
    if (showKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showKeyDialog = false },
            onKeySaved = {
                hasValidKey = apiKeyManager.getApiKey().isNotBlank()
                capturedBitmap?.let { runOcrOnBitmap(it) }
            }
        )
    }

    // Edit Extracted Text Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "ஆங்கில உரையைத் திருத்து",
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
                        ocrResult = ImageOcrProcessor.buildFallbackResult(editedText)
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

@Composable
private fun BookImagePreviewCard(
    bitmap: Bitmap?,
    ocrResult: OcrResult,
    isProcessing: Boolean,
    processingStatus: String,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 210.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9F7F1))
                    .border(BorderStroke(1.dp, Color(0xFFD4CEB8)), RoundedCornerShape(14.dp))
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF0077B6))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = processingStatus,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected Book Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 260.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
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
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF334155),
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (bitmap != null) "✓ தேர்ந்தெடுக்கப்பட்ட படம்" else "உதாரணப் பாடப் புத்தகம்",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    fontStyle = FontStyle.Italic
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCameraClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("ocr_camera_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Camera", fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = onGalleryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("ocr_gallery_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun OcrTabsRow(
    selectedTab: OcrTab,
    onTabSelected: (OcrTab) -> Unit,
    cyanAccent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D182E))
            .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OcrTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) cyanAccent else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF030712) else Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

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

        HorizontalDivider(
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

        HorizontalDivider(
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

        HorizontalDivider(
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

        HorizontalDivider(
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Surface(
            color = Color(0xFF0F2038),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = translation,
                fontSize = 15.sp,
                color = Color(0xFFE0F2FE),
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}
