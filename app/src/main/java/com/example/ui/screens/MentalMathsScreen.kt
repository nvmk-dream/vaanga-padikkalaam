package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MathPdfItem
import com.example.data.MentalMathsPdfManager
import com.example.data.MentalMathsTricksData
import com.example.ui.theme.*
import com.example.util.TamilMathSpeaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentalMathsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val pdfManager = remember { MentalMathsPdfManager(context) }
    var pdfList by remember { mutableStateOf(pdfManager.getAllPdfs()) }
    var selectedPdf by remember { mutableStateOf<MathPdfItem?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<MathPdfItem?>(null) }

    // Text to Speech with Female Voice
    val speaker = remember { TamilMathSpeaker(context) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            speaker.shutdown()
        }
    }

    // PDF Document Picker for adding new PDFs
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val addedItem = pdfManager.addCustomPdf(uri)
            if (addedItem != null) {
                pdfList = pdfManager.getAllPdfs()
            }
        }
    }

    // Intercept back button when reading a PDF to return to library
    BackHandler {
        if (selectedPdf != null) {
            speaker.stop()
            isSpeaking = false
            selectedPdf = null
        } else {
            onBackClick()
        }
    }

    // Delete Confirmation Dialog
    if (deleteCandidate != null) {
        val candidate = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "PDF-ஐ நிரந்தரமாக நீக்கவா?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SubikshaNavy
                )
            },
            text = {
                Text(
                    text = "\"${candidate.title}\" கோப்பு நிரந்தரமாக நீக்கப்படும். ஒருமுறை நீக்கினால் மீண்டும் வராது.",
                    fontSize = 14.sp,
                    color = SubikshaTextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pdfManager.deletePdf(candidate.id)
                        pdfList = pdfManager.getAllPdfs()
                        if (selectedPdf?.id == candidate.id) {
                            speaker.stop()
                            isSpeaking = false
                            selectedPdf = null
                        }
                        deleteCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("நிரந்தரமாக நீக்கு (Delete)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteCandidate = null }) {
                    Text("ரத்து செய் (Cancel)")
                }
            }
        )
    }

    // Settings / PDF Management Dialog (Only one settings access point)
    if (showSettingsDialog) {
        MentalMathsSettingsDialog(
            pdfList = pdfList,
            onDismiss = { showSettingsDialog = false },
            onAddPdfClick = {
                pdfPickerLauncher.launch(arrayOf("application/pdf"))
            },
            onDeletePdf = { item ->
                deleteCandidate = item
            }
        )
    }

    // Display either PDF Viewer Screen or the Books Library Screen
    if (selectedPdf != null) {
        PdfViewerScreen(
            pdfItem = selectedPdf!!,
            speaker = speaker,
            isSpeaking = isSpeaking,
            onBack = {
                speaker.stop()
                isSpeaking = false
                selectedPdf = null
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "மனக்கணக்கு / Mental Maths",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                            Text(
                                text = "500 மனக்கணக்கு பயிற்சி வழிகள்",
                                fontSize = 12.sp,
                                color = SubikshaTextMuted
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("maths_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SubikshaNavy
                            )
                        }
                    },
                    actions = {
                        // Single Settings Gear Button on top-right (Requirement 1)
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("maths_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Mental Maths Settings",
                                tint = SubikshaRoyalPurple
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(SubikshaBackground),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Banner Card explaining Tap-to-Speak feature
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    .background(SubikshaRoyalPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🔊", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ஆசிரியை பெண் குரல் வழிகாட்டல்",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SubikshaNavy
                                )
                                Text(
                                    text = "PDF-ல் 2-வது கணக்கைத் தொட்டால் 2-வது கணக்கை பெண் குரல் பேசும்! 3-வது கணக்கைத் தொட்டால் 3-வது கணக்கை பேசும்!",
                                    fontSize = 12.sp,
                                    color = SubikshaTextMuted
                                )
                            }
                        }
                    }
                }

                // Section Title (Clean - duplicate settings button removed as requested in Requirement 1)
                item {
                    Text(
                        text = "பாடப் புத்தகங்கள் (${pdfList.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                // List of PDF Books
                items(pdfList, key = { it.id }) { pdf ->
                    MathBookCard(
                        pdfItem = pdf,
                        onClick = {
                            selectedPdf = pdf
                        }
                    )
                }

                // Empty state if all PDFs are deleted
                if (pdfList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📚", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "புத்தகங்கள் எதுவும் இல்லை",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SubikshaNavy
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "மேல் வலது மூலையில் உள்ள Settings (⚙️) வழியாக புதிய PDF கோப்பைச் சேர்க்கலாம்.",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showSettingsDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("புதிய PDF சேர்க்க (Add PDF)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MathBookCard(
    pdfItem: MathPdfItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(pdfItem.colorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val iconSymbol = when {
                    pdfItem.title.contains("கூட்டல்") -> "➕"
                    pdfItem.title.contains("கழித்தல்") -> "➖"
                    pdfItem.title.contains("பெருக்கல்") -> "✖️"
                    pdfItem.title.contains("வகுத்தல்") -> "➗"
                    else -> "📄"
                }
                Text(text = iconSymbol, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pdfItem.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pdfItem.subtitle,
                    fontSize = 12.sp,
                    color = SubikshaTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SubikshaSurfaceVariant
                    ) {
                        Text(
                            text = "${pdfItem.pageCount} பக்கங்கள்",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubikshaNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (pdfItem.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SubikshaPink.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "பயனர் சேர்த்தது",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaPink,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(pdfItem.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Settings Dialog: Allows adding new PDFs and permanently deleting existing ones
 */
@Composable
fun MentalMathsSettingsDialog(
    pdfList: List<MathPdfItem>,
    onDismiss: () -> Unit,
    onAddPdfClick: () -> Unit,
    onDeletePdf: (MathPdfItem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = SubikshaRoyalPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mental Maths Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "PDF மேலாண்மை (Add & Delete)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubikshaRoyalPurple
                )
                Text(
                    text = "புதிய PDF கோப்புகளை சேர்க்கலாம். ஒருமுறை Delete செய்தால் அது நிரந்தரமாக நீக்கப்பட்டு மீண்டும் வராது.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Button(
                    onClick = onAddPdfClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple)
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("புதிய PDF சேர்க்க (Add New PDF)")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SubikshaCardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "தற்போதைய புத்தகங்கள்:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (pdfList.isEmpty()) {
                    Text(
                        text = "புத்தகங்கள் எதுவும் சேர்க்கப்படவில்லை.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    pdfList.forEach { pdf ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SubikshaSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pdf.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SubikshaNavy
                                    )
                                    Text(
                                        text = "${pdf.pageCount} பக்கங்கள்",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = { onDeletePdf(pdf) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete PDF",
                                        tint = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("சரி (Done)")
            }
        }
    )
}

/**
 * Mobile-Fit PDF Viewer Screen with:
 * 1. Mobile screen width fitting
 * 2. Smooth Pinch-to-Zoom (1x to 4x) & Pan
 * 3. Interactive Tap-to-Speak on the 20 individual tricks with Female Voice TTS
 * 4. Highlighting of selected trick
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfItem: MathPdfItem,
    speaker: TamilMathSpeaker,
    isSpeaking: Boolean,
    onBack: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(pdfItem.pageCount) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(true) }

    // Pinch to Zoom & Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Interactive Tap-to-Speak State
    var activeSpokenItemIndex by remember { mutableStateOf<Int?>(null) }
    var activeSpokenText by remember { mutableStateOf("") }
    var imageContainerSize by remember { mutableStateOf(IntSize.Zero) }

    // Reset zoom and highlights when changing page
    LaunchedEffect(currentPage) {
        scale = 1f
        offset = Offset.Zero
        activeSpokenItemIndex = null
        activeSpokenText = ""
    }

    // Function to speak a specific trick by index (0 to 19)
    fun speakTrick(itemIndex: Int) {
        activeSpokenItemIndex = itemIndex
        val speech = MentalMathsTricksData.getSpeechForTrick(pdfItem.title, currentPage, itemIndex)
        activeSpokenText = speech
        speaker.speak(speech)
    }

    // Render PDF page to high-definition Bitmap asynchronously
    LaunchedEffect(currentPage, pdfItem.localFilePath) {
        isLoadingPage = true
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            var renderer: PdfRenderer? = null
            var page: PdfRenderer.Page? = null
            try {
                val file = File(pdfItem.localFilePath)
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(pfd)
                totalPages = renderer.pageCount
                val pageIndex = currentPage.coerceIn(0, totalPages - 1)

                page = renderer.openPage(pageIndex)
                // Render at high density for crystal-clear readability on mobile
                val bmp = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                withContext(Dispatchers.Main) {
                    pageBitmap = bmp
                    isLoadingPage = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isLoadingPage = false
                }
            } finally {
                page?.close()
                renderer?.close()
                pfd?.close()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = pdfItem.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "பக்கம் ${currentPage + 1} / $totalPages",
                                fontSize = 12.sp,
                                color = SubikshaRoyalPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (scale > 1.05f) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🔍 ${String.format("%.1f", scale)}x Zoom",
                                    fontSize = 11.sp,
                                    color = SubikshaTextMuted
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SubikshaNavy
                        )
                    }
                },
                actions = {
                    // Reset Zoom Button (visible when zoomed in)
                    if (scale > 1.05f) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOutMap,
                                contentDescription = "Reset Zoom",
                                tint = SubikshaRoyalPurple
                            )
                        }
                    }

                    // Speaker Button (Reads current selected trick or entire page prompt)
                    IconButton(
                        onClick = {
                            if (speaker.isSpeaking()) {
                                speaker.stop()
                                activeSpokenItemIndex = null
                            } else {
                                val targetIndex = activeSpokenItemIndex ?: 0
                                speakTrick(targetIndex)
                            }
                        },
                        modifier = Modifier.testTag("pdf_speaker_button")
                    ) {
                        Icon(
                            imageVector = if (speaker.isSpeaking()) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Female Voice Read",
                            tint = if (speaker.isSpeaking()) SubikshaRoyalPurple else Color.Gray,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // Interactive 20-Trick Quick Selector Strip (1 to 20)
                // Allows user to directly tap Trick 2, Trick 3, etc. with instant voice speech
                Text(
                    text = "🔊 கணக்கைத் தட்டிப் பெண் குரலில் கேட்கவும் (1-20):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy,
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 4.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(20) { index ->
                        val isSelected = activeSpokenItemIndex == index
                        val qNum = currentPage * 20 + index + 1
                        FilterChip(
                            selected = isSelected,
                            onClick = { speakTrick(index) },
                            label = {
                                Text(
                                    text = "$qNum",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected && speaker.isSpeaking()) {
                                { Text("🔊", fontSize = 10.sp) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SubikshaRoyalPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Active Spoken Trick Announcement Banner
                AnimatedVisibility(
                    visible = activeSpokenItemIndex != null && activeSpokenText.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        color = SubikshaSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🗣️",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = activeSpokenText,
                                fontSize = 12.sp,
                                color = SubikshaNavy,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            IconButton(
                                onClick = {
                                    speaker.stop()
                                    activeSpokenItemIndex = null
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Stop",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = SubikshaCardBorder)

                // Page Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentPage > 0) {
                                speaker.stop()
                                currentPage--
                            }
                        },
                        enabled = currentPage > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("முந்தைய")
                    }

                    // Direct Page Indicator
                    Text(
                        text = "பக்கம் ${currentPage + 1} / $totalPages",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )

                    Button(
                        onClick = {
                            if (currentPage < totalPages - 1) {
                                speaker.stop()
                                currentPage++
                            }
                        },
                        enabled = currentPage < totalPages - 1,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SubikshaNavy)
                    ) {
                        Text("அடுத்தது")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        // PDF Display Box with Pinch-to-Zoom, Mobile Width Fitting, and Direct Tap-to-Speak
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F5F9))
                .onSizeChanged { imageContainerSize = it }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            val maxX = (imageContainerSize.width * (scale - 1)) / 2f
                            val maxY = (imageContainerSize.height * (scale - 1)) / 2f
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingPage) {
                CircularProgressIndicator(color = SubikshaRoyalPurple)
            } else if (pageBitmap != null) {
                // The Rendered PDF Image with Transform & Direct Tap Gestures
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        // Direct Tap Detection on the PDF page:
                        // Maps vertical touch position to one of the 20 questions
                        .pointerInput(currentPage) {
                            detectTapGestures { tapOffset ->
                                val height = size.height
                                if (height > 0) {
                                    val normY = tapOffset.y / height
                                    // The 20 questions span from ~16% to ~94% of page height
                                    val topBound = 0.16f
                                    val bottomBound = 0.94f
                                    if (normY in topBound..bottomBound) {
                                        val fraction = (normY - topBound) / (bottomBound - topBound)
                                        val clickedIndex = (fraction * 20).toInt().coerceIn(0, 19)
                                        speakTrick(clickedIndex)
                                    } else if (normY < topBound) {
                                        speakTrick(0)
                                    } else {
                                        speakTrick(19)
                                    }
                                }
                            }
                        }
                ) {
                    // PDF Page rendered to fit mobile screen width
                    Image(
                        bitmap = pageBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page ${currentPage + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.FillWidth
                    )

                    // Visual Highlight Overlay over the currently tapped trick
                    if (activeSpokenItemIndex != null) {
                        val index = activeSpokenItemIndex!!
                        // 20 items distributed vertically between 16% and 94%
                        val topPercent = 0.16f + (index * ((0.94f - 0.16f) / 20f))
                        val itemHeightPercent = (0.94f - 0.16f) / 20f

                        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                            val boxHeight = maxHeight * itemHeightPercent
                            val boxTop = maxHeight * topPercent

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(boxHeight)
                                    .padding(top = boxTop, start = 8.dp, end = 8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SubikshaRoyalPurple.copy(alpha = 0.22f))
                                    .border(2.dp, SubikshaRoyalPurple, RoundedCornerShape(6.dp))
                            ) {
                                // Small speaker badge in corner of active item
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(SubikshaRoyalPurple)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "பக்கத்தை ஏற்றுவதில் பிழை ஏற்பட்டது.",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}
