package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ApiKeyManager
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.data.DefaultQuotaExceededException
import com.example.data.GeminiService
import com.example.ui.components.ApiKeyDialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.data.AppVoiceManager

enum class OcrTab(val title: String) {
    EXTRACTED_TEXT("உரை"),
    KEY_WORDS("முக்கிய"),
    EXPLANATION("விளக்கம்"),
    TAMIL_TRANSLATION("தமிழில்"),
    QUESTIONS_ANSWERS("வினா-விடை (Q/A)")
}

enum class ScannerEngine(val title: String, val subtitle: String) {
    GEMINI_AI("⚡ Gemini AI", "ஆன்லைன்"),
    ML_KIT("📱 ML Kit", "ஆஃப்லைன்")
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
    var selectedTab by remember { mutableStateOf(OcrTab.EXTRACTED_TEXT) }
    var selectedEngine by remember { mutableStateOf(ScannerEngine.GEMINI_AI) }
    var ocrResult by remember { mutableStateOf(ImageOcrProcessor.referenceBookResult) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingStatus by remember { mutableStateOf("படத்தில் உள்ள உரையை AI வாசிக்கிறது...") }
    var showEditDialog by remember { mutableStateOf(false) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var showQuotaDialog by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(ocrResult.rawEnglishText) }
    var hasValidKey by remember { mutableStateOf(apiKeyManager.getApiKey().isNotBlank()) }

    // Theme palette matching User Technical Spec & CSS
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80) // CSS: #b45b80 for active tab
    val mauveDeep = Color(0xFF633249)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF85405F), Color(0xFF71334F)) // CSS: linear-gradient(135deg, #85405f, #71334f)
    )
    val glassCardBackground = Color.White.copy(alpha = 0.08f)
    val glassBorder = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f))
    val deepTextColor = Color(0xFF2E1A23)
    val mutedTextColor = Color(0xFF5A3A4A)

    // Attached image fixed screen background (Image 1: Sakura Petals theme)
    val backgroundRes = R.drawable.bg_sakura_petals

    // Text to Speech
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var speakingWord by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                AppVoiceManager.configureTts(ttsEngine, context, isTamil = false)
                isTtsReady = true
            }
        }
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun cleanSpeechText(rawText: String, isTamil: Boolean): String {
        val blankReplacement = if (isTamil) " கோடிட்ட இடம் " else " dash "
        return rawText
            // Replace runs of underscores or dashes into single dash / கோடிட்ட இடம்
            .replace(Regex("([_—–]+(?:\\s*[_—–]+)*|-{2,}(?:\\s*-{2,})*)"), blankReplacement)
            // Remove markdown formatting asterisks and headers
            .replace(Regex("[*#`~]"), " ")
            // Ensure friendly pronunciation for brackets
            .replace("(", ", ")
            .replace(")", ", ")
            // Collapse whitespace
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun speak(text: String, wordTag: String? = null, isTamil: Boolean = false) {
        if (!isTtsReady || ttsEngine == null) return
        ttsEngine?.stop()
        speakingWord = wordTag

        AppVoiceManager.configureTts(ttsEngine, context, isTamil = isTamil)

        val speechText = cleanSpeechText(text, isTamil)
        ttsEngine?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, wordTag ?: "ocr_speech")
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label நகலெடுக்கப்பட்டது (Copied)!", Toast.LENGTH_SHORT).show()
    }

    fun runMlKitOffline(bitmap: Bitmap, reasonMessage: String) {
        isProcessing = true
        processingStatus = "ஆஃப்லைன் ஸ்கேனர் உரையைப் பெறுகிறது..."
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val ocrRaw = visionText.text.trim()
                    if (ocrRaw.isNotEmpty()) {
                        ocrResult = ImageOcrProcessor.buildFallbackResult(ocrRaw)
                        editedText = ocrResult.rawEnglishText
                        Toast.makeText(context, "$reasonMessage: ஆஃப்லைன் உரை பெறப்பட்டது.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "படத்தில் உரை எதுவும் கண்டறியப்படவில்லை.", Toast.LENGTH_LONG).show()
                    }
                    isProcessing = false
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Toast.makeText(context, "ஸ்கேன் தோல்வியடைந்தது: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    isProcessing = false
                }
        } catch (e: Exception) {
            e.printStackTrace()
            isProcessing = false
        }
    }

    fun decodeBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun renderPdfPagesToBitmaps(uri: Uri, maxPages: Int = 10): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val pdfRenderer = PdfRenderer(pfd)
                val totalPages = pdfRenderer.pageCount.coerceAtMost(maxPages)
                for (i in 0 until totalPages) {
                    val page = pdfRenderer.openPage(i)
                    val w = (page.width * 1.5).toInt().coerceAtLeast(100)
                    val h = (page.height * 1.5).toInt().coerceAtLeast(100)
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmaps.add(bmp)
                }
                pdfRenderer.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmaps
    }

    fun runOcrOnBitmaps(bitmaps: List<Bitmap>, sourceLabel: String = "பாடம்") {
        if (bitmaps.isEmpty()) {
            Toast.makeText(context, "படங்கள் எதுவும் தேர்ந்தெடுக்கப்படவில்லை.", Toast.LENGTH_SHORT).show()
            return
        }

        capturedBitmap = bitmaps.firstOrNull()

        if (selectedEngine == ScannerEngine.ML_KIT) {
            runMlKitOffline(bitmaps.first(), "ஆஃப்லைன் ML Kit")
            return
        }

        isProcessing = true
        processingStatus = if (bitmaps.size > 1) {
            "சுபிக்ஷா AI ஆசிரியர் ${bitmaps.size} பக்க முழுப் பாடத்தையும் ஆய்வு செய்கிறது..."
        } else {
            "சுபிக்ஷா AI ஆசிரியர் படத்தை நேரடியாக வாசிக்கிறது (Gemini Vision)..."
        }

        coroutineScope.launch {
            val aiResult = geminiService.analyzeBookImagesDirect(bitmaps)
            aiResult.onSuccess { structuredResult ->
                ocrResult = structuredResult
                editedText = structuredResult.rawEnglishText
                selectedTab = if (structuredResult.questionAnswers.isNotEmpty()) {
                    OcrTab.QUESTIONS_ANSWERS
                } else {
                    OcrTab.EXTRACTED_TEXT
                }
                val msg = if (bitmaps.size > 1) {
                    "${bitmaps.size} பக்க முழுப் பாடமும் வினா-விடைகளும் தயாராகிவிட்டன!"
                } else {
                    "பாடப்புத்தகம் வெற்றிகரமாக ஆய்வு செய்யப்பட்டது!"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                isProcessing = false
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "AI ஆய்வு தோல்வியடைந்தது"
                if (err is DefaultQuotaExceededException || errorMsg.contains("Default API quota முடிந்தது")) {
                    isProcessing = false
                    showQuotaDialog = true
                } else {
                    Toast.makeText(context, "AI பிழை: $errorMsg (ஆஃப்லைன் ஸ்கேனர் இயக்கப்படுகிறது)", Toast.LENGTH_LONG).show()
                    runMlKitOffline(bitmaps.first(), "AI பிழை ஏற்பட்டதால்")
                }
            }
        }
    }

    fun runOcrOnBitmap(bitmap: Bitmap) {
        runOcrOnBitmaps(listOf(bitmap), "ஒற்றைப் படம்")
    }

    // 1. Camera Launcher
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

    // 2. Single Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val bitmap = decodeBitmapFromUri(uri)
            if (bitmap != null) {
                runOcrOnBitmap(bitmap)
            } else {
                Toast.makeText(context, "படத்தை ஏற்றுவதில் பிழை ஏற்பட்டது.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 3. Multi-Image Gallery Picker Launcher (Full Lesson)
    val multiImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val bitmaps = uris.mapNotNull { decodeBitmapFromUri(it) }
            if (bitmaps.isNotEmpty()) {
                selectedImageUri = uris.firstOrNull()
                runOcrOnBitmaps(bitmaps, "${bitmaps.size} பக்கங்கள்")
            } else {
                Toast.makeText(context, "படங்களை ஏற்றுவதில் பிழை ஏற்பட்டது.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. PDF Document Picker Launcher (Full Lesson PDF)
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val bitmaps = renderPdfPagesToBitmaps(uri, maxPages = 10)
            if (bitmaps.isNotEmpty()) {
                selectedImageUri = uri
                runOcrOnBitmaps(bitmaps, "PDF (${bitmaps.size} பக்கங்கள்)")
            } else {
                Toast.makeText(context, "PDF கோப்பைத் திறக்க முடியவில்லை.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 2-Layer Architecture (Z-Index: 1 Background Wallpaper, Z-Index: 2 Glassmorphism UI)
    Box(modifier = Modifier.fillMaxSize()) {
        // Z-Index: 1 - Background Wallpaper
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Z-Index: 2 - Floating UI Scaffold (No dense overlay so wallpaper is 100% sharp and clear)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top Bar with Mauve buttons (#7D405D)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(BorderStroke(1.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                            .testTag("ocr_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = mauveHeader
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Image to Text",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = mauveDeep,
                        modifier = Modifier.weight(1f)
                    )

                    // Key Setup Button
                    IconButton(
                        onClick = { showKeyDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(BorderStroke(1.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                            .testTag("ocr_api_key_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Gemini Key",
                            tint = mauveHeader,
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
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(BorderStroke(1.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                            .testTag("ocr_sample_switch_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CollectionsBookmark,
                            contentDescription = "Switch Sample",
                            tint = mauveHeader,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onAskGemini("இந்தப் புத்தக உரையை மேலும் ஆழமாக எனக்குத் தமிழில் விளக்குங்கள்:\n${ocrResult.rawEnglishText}") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(BorderStroke(1.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                            .testTag("ocr_ask_ai_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask AI",
                            tint = mauveHeader,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Scrollable Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Scanner Mode Switch (⚡ Gemini AI vs 📱 ML Kit)
                    item {
                        ScannerEngineSwitch(
                            selectedEngine = selectedEngine,
                            onEngineSelected = { newEngine ->
                                selectedEngine = newEngine
                                Toast.makeText(
                                    context,
                                    if (newEngine == ScannerEngine.GEMINI_AI) "Gemini AI ஆன்லைன் மோடு செயலில் உள்ளது" else "ML Kit ஆஃப்லைன் மோடு செயலில் உள்ளது",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            buttonGradient = buttonGradient,
                            mauveHeader = mauveHeader
                        )
                    }

                    // Book Image / Captured Photo Preview
                    item {
                        BookImagePreviewCard(
                            bitmap = capturedBitmap,
                            ocrResult = ocrResult,
                            isProcessing = isProcessing,
                            processingStatus = processingStatus,
                            buttonGradient = buttonGradient,
                            deepTextColor = deepTextColor,
                            mauveHeader = mauveHeader,
                            onCameraClick = {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            },
                            onGalleryClick = {
                                galleryLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onMultiImageClick = {
                                multiImageLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onPdfClick = {
                                pdfLauncher.launch(arrayOf("application/pdf"))
                            }
                        )
                    }

                    // 5 Interactive Navigation Tabs
                    item {
                        OcrTabsRow(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            activeColor = roseGoldAccent,
                            textColor = mauveHeader
                        )
                    }

                    // Tab Content Glassmorphism Card (.result-card)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(glassBorder, RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(containerColor = glassCardBackground),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(modifier = Modifier.padding(4.dp)) {
                                when (selectedTab) {
                                    OcrTab.EXTRACTED_TEXT -> {
                                        ExtractedTextTabContent(
                                            ocrResult = ocrResult,
                                            onEdit = { showEditDialog = true },
                                            onSpeak = { speak(ocrResult.rawEnglishText, isTamil = false) },
                                            onCopy = { copyToClipboard("அசல் ஆங்கில உரை", ocrResult.rawEnglishText) },
                                            accentColor = mauveHeader,
                                            deepTextColor = deepTextColor
                                        )
                                    }

                                    OcrTab.KEY_WORDS -> {
                                        KeyWordsTabContent(
                                            keyWords = ocrResult.keyWords,
                                            onSpeakWord = { word -> speak(word, word, isTamil = false) },
                                            onSpeakAll = {
                                                val allWords = ocrResult.keyWords.joinToString(". ") { it.english }
                                                speak(allWords, isTamil = false)
                                            },
                                            speakingWord = speakingWord,
                                            accentColor = roseGoldAccent,
                                            deepTextColor = deepTextColor
                                        )
                                    }

                                    OcrTab.EXPLANATION -> {
                                        ExplanationTabContent(
                                            explanation = ocrResult.detailedExplanation,
                                            onSpeakCoreConcept = {
                                                val text = "${ocrResult.detailedExplanation.headingTamil}. மையக் கருத்து: ${ocrResult.detailedExplanation.centralIdea}"
                                                speak(text, isTamil = true)
                                            },
                                            onSpeakDetailed = {
                                                val detailedText = "விரிவான கருத்து விளக்கம் மற்றும் விடைகள். " +
                                                    ocrResult.detailedExplanation.paragraphsExplanation.joinToString(". ")
                                                speak(detailedText, isTamil = true)
                                            },
                                            onSpeakItem = { itemText ->
                                                speak(itemText, isTamil = true)
                                            },
                                            onSpeakPractical = {
                                                val practicalText = "அன்றாட வாழ்வில் பயன்பாடு. " + ocrResult.detailedExplanation.practicalApplication
                                                speak(practicalText, isTamil = true)
                                            },
                                            accentColor = mauveHeader,
                                            deepTextColor = deepTextColor
                                        )
                                    }

                                    OcrTab.TAMIL_TRANSLATION -> {
                                        TamilTranslationTabContent(
                                            translation = ocrResult.fullTamilTranslation,
                                            onSpeak = { speak(ocrResult.fullTamilTranslation, isTamil = true) },
                                            accentColor = mauveHeader,
                                            deepTextColor = deepTextColor
                                        )
                                    }

                                    OcrTab.QUESTIONS_ANSWERS -> {
                                        QuestionsAnswersTabContent(
                                            questionAnswers = ocrResult.questionAnswers,
                                            onSpeakQuestionEn = { q -> speak(q, isTamil = false) },
                                            onSpeakQuestionTa = { q -> speak(q, isTamil = true) },
                                            onSpeakAnswerEn = { a -> speak(a, isTamil = false) },
                                            onSpeakAnswerTa = { a -> speak(a, isTamil = true) },
                                            onSpeakAllEn = {
                                                val allText = ocrResult.questionAnswers.joinToString(". ") {
                                                    "Question ${it.questionNumber}. ${it.questionEnglish}. Answer: ${it.answerEnglish}"
                                                }
                                                speak(allText, isTamil = false)
                                            },
                                            onSpeakAllTa = {
                                                val allText = ocrResult.questionAnswers.joinToString(". ") {
                                                    "வினா ${it.questionNumber}. ${it.questionTamil}. விடை: ${it.answerTamil}. ${it.conceptExplanation}"
                                                }
                                                speak(allText, isTamil = true)
                                            },
                                            accentColor = mauveHeader,
                                            deepTextColor = deepTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Button with Mauve Linear Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
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
                                OcrTab.QUESTIONS_ANSWERS -> {
                                    val text = ocrResult.questionAnswers.joinToString("\n\n") {
                                        "Q${it.questionNumber} [${it.questionType}]: ${it.questionEnglish}\nகேள்வி: ${it.questionTamil}\nAnswer: ${it.answerEnglish}\nவிடை: ${it.answerTamil}"
                                    }
                                    copyToClipboard("பாட வினா-விடைகள்", text)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(5.dp, RoundedCornerShape(50.dp), ambientColor = Color(0x1A642841))
                            .clip(RoundedCornerShape(50.dp))
                            .background(buttonGradient)
                            .testTag("ocr_bottom_action_btn"),
                        shape = RoundedCornerShape(50.dp),
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
                                    OcrTab.QUESTIONS_ANSWERS -> "வினா-விடைகளை நகலெடு"
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
    }

    // Default Quota Exceeded Alert Dialog
    if (showQuotaDialog) {
        AlertDialog(
            onDismissRequest = { showQuotaDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = roseGoldAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "API Quota முடிவு",
                        fontWeight = FontWeight.Bold,
                        color = mauveDeep
                    )
                }
            },
            text = {
                Text(
                    text = "Default API quota முடிந்தது, App இல் API key store செய்யப்படவில்லை எனவே key யை ADD செய்யவும்",
                    color = deepTextColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuotaDialog = false
                        showKeyDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = mauveHeader),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Key-ஐ ADD செய்க", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showQuotaDialog = false
                        selectedEngine = ScannerEngine.ML_KIT
                        capturedBitmap?.let { runMlKitOffline(it, "ஆஃப்லைன் முறை") }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, mauveHeader.copy(alpha = 0.5f))
                ) {
                    Text("ஆஃப்லைன் ML Kit", color = mauveHeader)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
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
                    color = mauveDeep
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
                        focusedTextColor = deepTextColor,
                        unfocusedTextColor = deepTextColor,
                        focusedBorderColor = roseGoldAccent,
                        unfocusedBorderColor = Color.LightGray
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
                    colors = ButtonDefaults.buttonColors(containerColor = mauveHeader),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("புதுப்பி (Update)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("ரத்து (Cancel)", color = mutedTextColor)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
private fun ScannerEngineSwitch(
    selectedEngine: ScannerEngine,
    onEngineSelected: (ScannerEngine) -> Unit,
    buttonGradient: Brush,
    mauveHeader: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(20.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ScannerEngine.values().forEach { engine ->
            val isSelected = engine == selectedEngine
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) buttonGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onEngineSelected(engine) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = engine.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else mauveHeader
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = engine.subtitle,
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White else mauveHeader,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookImagePreviewCard(
    bitmap: Bitmap?,
    ocrResult: OcrResult,
    isProcessing: Boolean,
    processingStatus: String,
    buttonGradient: Brush,
    deepTextColor: Color,
    mauveHeader: Color,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onMultiImageClick: () -> Unit,
    onPdfClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(16.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = mauveHeader)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = processingStatus,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = deepTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected Book Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 240.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = ocrResult.originalHeading,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = deepTextColor,
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        ocrResult.paragraphs.forEach { paragraph ->
                            Text(
                                text = paragraph,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Serif,
                                color = deepTextColor.copy(alpha = 0.90f),
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (bitmap != null) "✓ புத்தகம் தயாராக உள்ளது" else "உதாரணப் புத்தகம்",
                        fontSize = 12.sp,
                        color = mauveHeader,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Row 1: Single image inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onCameraClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(buttonGradient)
                            .testTag("ocr_camera_btn"),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onGalleryClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(buttonGradient)
                            .testTag("ocr_gallery_btn"),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Row 2: Full Lesson inputs (Multiple Images & PDF)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onMultiImageClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(buttonGradient)
                            .testTag("ocr_multi_image_btn"),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Multi Pages",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("📚 பல படங்கள்", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPdfClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(buttonGradient)
                            .testTag("ocr_pdf_btn"),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "PDF Book",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("📄 PDF பாடம்", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
    activeColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(20.dp))
            .horizontalScroll(rememberScrollState())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OcrTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color(0xFFB45B80) else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else textColor,
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
    accentColor: Color,
    deepTextColor: Color
) {
    val context = LocalContext.current
    val currentVoice = AppVoiceManager.getSelectedVoice(context)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Article,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "அசல் ஆங்கில உரை",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = deepTextColor
                    )
                    Surface(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ஆசிரியர் குரல்: ${currentVoice.titleTamil}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Text",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onSpeak, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen English",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy English",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ocrResult.paragraphs.forEachIndexed { index, paragraph ->
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            color = accentColor.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = deepTextColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = paragraph,
                            fontSize = 14.sp,
                            color = deepTextColor,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyWordsTabContent(
    keyWords: List<OcrKeyWord>,
    onSpeakWord: (String) -> Unit,
    onSpeakAll: () -> Unit,
    speakingWord: String?,
    accentColor: Color,
    deepTextColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "முக்கியச் சொற்கள் & அர்த்தங்கள்",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = deepTextColor
                    )
                    if (keyWords.isNotEmpty()) {
                        Text(
                            text = "மொத்தம்: ${keyWords.size} சொற்கள் (Scroll to read all)",
                            fontSize = 11.sp,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onSpeakAll,
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, accentColor),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Listen All", fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        keyWords.forEach { word ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = word.english,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = deepTextColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = word.partOfSpeech,
                                fontSize = 10.sp,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = word.tamil,
                        fontSize = 13.sp,
                        color = Color(0xFF8A4867),
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { onSpeakWord(word.english) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (speakingWord == word.english) accentColor else accentColor.copy(alpha = 0.15f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak",
                        tint = if (speakingWord == word.english) Color.White else accentColor,
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
    onSpeakCoreConcept: () -> Unit,
    onSpeakDetailed: () -> Unit,
    onSpeakItem: (String) -> Unit,
    onSpeakPractical: () -> Unit,
    accentColor: Color,
    deepTextColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = explanation.headingTamil,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = deepTextColor
                )
            }

            IconButton(onClick = onSpeakCoreConcept, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen Heading and Core Concept",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Central Idea
        Surface(
            color = accentColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎯 மையக் கருத்து (Core Concept):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    IconButton(onClick = onSpeakCoreConcept, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen Core Concept",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = explanation.centralIdea,
                    fontSize = 13.sp,
                    color = deepTextColor,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Paragraph Explanation & Solutions Header with Listen Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "விரிவான கருத்து விளக்கம் & விடைகள்:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = deepTextColor,
                modifier = Modifier.weight(1f)
            )

            // Listen button for all detailed explanation points & answers
            IconButton(
                onClick = onSpeakDetailed,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen Detailed Explanation and Answers",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        explanation.paragraphsExplanation.forEachIndexed { index, item ->
            Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item,
                        fontSize = 13.sp,
                        color = deepTextColor,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onSpeakItem(item) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen item",
                            tint = accentColor.copy(alpha = 0.75f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Practical Application
        Surface(
            color = Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🌱 அன்றாட வாழ்வில் பயன்பாடு:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    IconButton(
                        onClick = onSpeakPractical,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen Practical Application",
                            tint = accentColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = explanation.practicalApplication,
                    fontSize = 12.sp,
                    color = deepTextColor,
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
    accentColor: Color,
    deepTextColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "தமிழ் மொழிபெயர்ப்பு",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = deepTextColor
                )
            }

            IconButton(onClick = onSpeak, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Surface(
            color = Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = translation,
                fontSize = 15.sp,
                color = deepTextColor,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun QuestionsAnswersTabContent(
    questionAnswers: List<OcrQuestionAnswer>,
    onSpeakQuestionEn: (String) -> Unit,
    onSpeakQuestionTa: (String) -> Unit,
    onSpeakAnswerEn: (String) -> Unit,
    onSpeakAnswerTa: (String) -> Unit,
    onSpeakAllEn: () -> Unit,
    onSpeakAllTa: () -> Unit,
    accentColor: Color,
    deepTextColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header with Title and All-Listen Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "வினா - விடைகள் & தீர்வுகள்",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = deepTextColor
                    )
                    Text(
                        text = "${questionAnswers.size} வினாக்கள் / பயிற்சிகள்",
                        fontSize = 11.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (questionAnswers.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onSpeakAllEn,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.20f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak English",
                            tint = deepTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("EN", fontSize = 10.sp, color = deepTextColor, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSpeakAllTa,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak Tamil",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("தமிழ்", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        HorizontalDivider(
            color = accentColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        if (questionAnswers.isEmpty()) {
            Surface(
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📖 வினாக்கள் எதுவும் கண்டறியப்படவில்லை",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = deepTextColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "புத்தகத்தின் பயிற்சிப் பக்கங்களை கேமரா மூலமாகவோ, பல படங்களாகவோ (Multi-Pages) அல்லது PDF கோப்பாகவோ தேர்வு செய்து ஸ்கேன் செய்யவும். கேள்விகள் மற்றும் கணக்குகளுக்கான படிநிலைகள் தானாக இங்கே தோன்றும்.",
                        fontSize = 12.sp,
                        color = deepTextColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            for (qa in questionAnswers) {
                QuestionAnswerCard(
                    qa = qa,
                    onSpeakQuestionEn = onSpeakQuestionEn,
                    onSpeakQuestionTa = onSpeakQuestionTa,
                    onSpeakAnswerEn = onSpeakAnswerEn,
                    onSpeakAnswerTa = onSpeakAnswerTa,
                    accentColor = accentColor,
                    deepTextColor = deepTextColor
                )
            }
        }
    }
}

@Composable
private fun QuestionAnswerCard(
    qa: OcrQuestionAnswer,
    onSpeakQuestionEn: (String) -> Unit,
    onSpeakQuestionTa: (String) -> Unit,
    onSpeakAnswerEn: (String) -> Unit,
    onSpeakAnswerTa: (String) -> Unit,
    accentColor: Color,
    deepTextColor: Color
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Question Number Badge + Type Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = accentColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Q${qa.questionNumber.ifBlank { "•" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (qa.questionType.isNotBlank()) {
                        Surface(
                            color = Color(0xFF6B4E71).copy(alpha = 0.20f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = qa.questionType,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = deepTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Question Section (English & Tamil with individual speaker buttons)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (qa.questionEnglish.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = qa.questionEnglish,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = deepTextColor,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onSpeakQuestionEn(qa.questionEnglish) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen English Question",
                                tint = accentColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                if (qa.questionTamil.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = qa.questionTamil,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = deepTextColor.copy(alpha = 0.90f),
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onSpeakQuestionTa(qa.questionTamil) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen Tamil Question",
                                tint = accentColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // Step-by-Step Working (Maths and practical problems)
            if (qa.stepByStepWorking.isNotEmpty()) {
                Surface(
                    color = Color(0xFFF9A825).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFF9A825).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📐 தீர்க்கும் முறை (Step-by-Step Working):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = deepTextColor
                            )
                        }

                        for (step in qa.stepByStepWorking) {
                            Text(
                                text = "• $step",
                                fontSize = 12.sp,
                                color = deepTextColor,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Answer Section (English & Tamil with individual speaker buttons)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2E7D32).copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                    .border(BorderStroke(0.5.dp, Color(0xFF2E7D32).copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "✓ விடை (Answer):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )

                if (qa.answerEnglish.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = qa.answerEnglish,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = deepTextColor,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onSpeakAnswerEn(qa.answerEnglish) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen English Answer",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                if (qa.answerTamil.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = qa.answerTamil,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = deepTextColor,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onSpeakAnswerTa(qa.answerTamil) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen Tamil Answer",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // Concept / Method Explanation (எப்படி செய்ய வேண்டும் என்ற விளக்கம்)
            if (qa.conceptExplanation.isNotBlank()) {
                Surface(
                    color = Color(0xFF1565C0).copy(alpha = 0.10f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF1565C0).copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFF1565C0),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "💡 எப்படி செய்ய வேண்டும்? (விளக்கம்):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D47A1)
                                )
                            }
                            IconButton(
                                onClick = { onSpeakAnswerTa(qa.conceptExplanation) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen Explanation",
                                    tint = Color(0xFF1565C0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = qa.conceptExplanation,
                            fontSize = 12.sp,
                            color = deepTextColor,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Practical / Real-Life Application (அன்றாட வாழ்வில் இதன் பயன்பாடு)
            if (qa.practicalApplication.isNotBlank()) {
                Surface(
                    color = Color(0xFF6A1B9A).copy(alpha = 0.10f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF6A1B9A).copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = Color(0xFF6A1B9A),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🌱 நடைமுறைப் பயன்பாடு (Real-Life Application):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4A148C)
                                )
                            }
                            IconButton(
                                onClick = { onSpeakAnswerTa(qa.practicalApplication) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen Practical Application",
                                    tint = Color(0xFF6A1B9A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = qa.practicalApplication,
                            fontSize = 12.sp,
                            color = deepTextColor,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
