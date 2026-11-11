package com.example.ui.screens

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyLibraryScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current

    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var viewingDocument by remember { mutableStateOf<LibraryDocument?>(null) }
    var activeHtmlBook by remember { mutableStateOf<LibraryDocument?>(null) }
    var showAddFileDialog by remember { mutableStateOf(false) }

    // State refresh trigger when new file is added/deleted
    var refreshKey by remember { mutableIntStateOf(0) }

    val selectedFolder = MyLibraryRepository.folders.find { it.id == selectedFolderId }

    // File Picker for PDF and Images
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && selectedFolderId != null) {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isPdf = mimeType.contains("pdf", ignoreCase = true) || uri.toString().contains(".pdf", ignoreCase = true)
            val fileType = if (isPdf) DocumentType.PDF else DocumentType.IMAGE

            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: if (isPdf) "புதிய PDF புத்தகம்" else "புதிய படம்"
            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            val newDoc = LibraryDocument(
                id = "doc_${System.currentTimeMillis()}",
                title = fileName.replace(".pdf", "").replace(".jpg", "").replace(".png", ""),
                subtitle = if (isPdf) "மொபைலிலிருந்து சேர்க்கப்பட்ட PDF ஆவணம்" else "சேமிக்கப்பட்ட புகைப்படக் குறிப்பு",
                fileType = fileType,
                uriString = uri.toString(),
                dateAdded = currentDate,
                sizeText = "2.1 MB",
                textSnippet = "இந்த ஆவணம் உங்கள் சாதனத்திலிருந்து வெற்றிகரமாக நூலகத்தில் சேர்க்கப்பட்டுள்ளது."
            )

            MyLibraryRepository.addDocumentToFolder(selectedFolderId!!, newDoc)
            refreshKey++
            Toast.makeText(context, "கோப்பு வெற்றிகரமாக சேர்க்கப்பட்டது!", Toast.LENGTH_SHORT).show()
        }
    }

    if (activeHtmlBook != null) {
        AathichudiHtmlReaderScreen(
            title = activeHtmlBook!!.title,
            htmlAssetPath = activeHtmlBook!!.htmlAssetPath ?: "aathichudi.html",
            pdfAssetPath = activeHtmlBook!!.pdfAssetPath ?: "pdf/aathichudi_full_book.pdf",
            onBack = { activeHtmlBook = null }
        )
        return
    }

    BackHandler {
        when {
            activeHtmlBook != null -> activeHtmlBook = null
            viewingDocument != null -> viewingDocument = null
            selectedFolderId != null -> selectedFolderId = null
            else -> onBackClick()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Layer 1: Green clover floral wallpaper
        Image(
            painter = painterResource(id = R.drawable.bg_library),
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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (selectedFolderId != null) {
                            selectedFolderId = null
                        } else {
                            onBackClick()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .testTag("library_back_button")
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
                        text = if (selectedFolder != null) selectedFolder.nameTamil else "என் நூலகம் (My Library)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SubikshaNavy
                    )
                    Text(
                        text = if (selectedFolder != null) selectedFolder.nameEnglish else "உங்கள் புத்தகம் • PDF & படங்கள்",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubikshaRoyalPurple
                    )
                }

                if (selectedFolder != null) {
                    // Add File Button inside folder
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedFolder.primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add File",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ சேர்",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Main Content Area
            if (selectedFolder == null) {
                // No-Scroll Category View: 6 Subject Categories (தமிழ், English, Maths, Science, Social, Others)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Compact Header Strip (No oversized explanatory banner)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📚", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "பாட நூலகப் பிரிவுகள்",
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
                                    text = "6 பிரிவுகள்",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubikshaRoyalPurple,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 6 Categories rendered in single-column equal-height cards (one by one rows)
                    // Perfectly sized to fit initial screen without scrolling across 360px - 432px devices
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MyLibraryRepository.folders.forEach { folder ->
                            CompactFolderRowCard(
                                folder = folder,
                                docCount = folder.documents.size,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                onClick = { selectedFolderId = folder.id }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                // 2. Documents inside selected folder
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Folder Banner
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = selectedFolder.primaryColor.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(16.dp)
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
                                        .background(selectedFolder.primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = selectedFolder.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedFolder.nameTamil,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SubikshaNavy
                                    )
                                    Text(
                                        text = "${selectedFolder.documents.size} புத்தகங்கள் / ஆவணங்கள் உள்ளன",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = selectedFolder.primaryColor
                                    )
                                }
                            }
                        }
                    }

                    if (selectedFolder.documents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = SubikshaTextMuted,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "இந்த ஃபோல்டரில் இன்னும் கோப்புகள் இல்லை",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SubikshaNavy
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "மேலே உள்ள '+ சேர்' பொத்தானை அழுத்தி PDF அல்லது படங்களைச் சேர்க்கவும்.",
                                        fontSize = 12.sp,
                                        color = SubikshaTextMuted
                                    )
                                }
                            }
                        }
                    } else {
                        items(selectedFolder.documents, key = { it.id }) { doc ->
                            DocumentCardItem(
                                document = doc,
                                accentColor = selectedFolder.primaryColor,
                                onClick = {
                                    if (doc.fileType == DocumentType.HTML_BOOK) {
                                        activeHtmlBook = doc
                                    } else {
                                        viewingDocument = doc
                                    }
                                },
                                onReadHtml = {
                                    activeHtmlBook = doc
                                },
                                onDownloadPdf = {
                                    downloadPdfToDevice(
                                        context,
                                        doc.pdfAssetPath ?: "pdf/aathichudi_full_book.pdf",
                                        "Avvaiyar_Aathichudi_Full_Book.pdf"
                                    )
                                },
                                onDelete = {
                                    MyLibraryRepository.removeDocument(selectedFolder.id, doc.id)
                                    refreshKey++
                                    Toast.makeText(context, "கோப்பு நீக்கப்பட்டது", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    // Document Viewer Dialog
    if (viewingDocument != null) {
        val doc = viewingDocument!!
        Dialog(onDismissRequest = { viewingDocument = null }) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .shadow(12.dp, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (doc.fileType == DocumentType.PDF) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF3B82F6).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (doc.fileType == DocumentType.PDF) "📕 PDF புத்தகம்" else "🖼️ படக் குறிப்பு",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (doc.fileType == DocumentType.PDF) Color(0xFFDC2626) else Color(0xFF2563EB),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewingDocument = null },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SubikshaTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = doc.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = doc.subtitle,
                        fontSize = 12.sp,
                        color = SubikshaTextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reading Page Simulation
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "உள்ளடக்கச் சுருக்கம் (Content Preview):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaRoyalPurple
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = doc.textSnippet,
                                fontSize = 13.sp,
                                color = SubikshaNavy,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "தேதி: ${doc.dateAdded}",
                            fontSize = 11.sp,
                            color = SubikshaTextMuted
                        )
                        Text(
                            text = "அளவு: ${doc.sizeText}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewingDocument = null },
                        colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("மூடு (Close)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==============================================================
// Compact Folder Row Card (Single Row, One by One, No-Scroll)
// ==============================================================
@Composable
private fun CompactFolderRowCard(
    folder: LibraryFolder,
    docCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("folder_card_${folder.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(folder.primaryColor, folder.secondaryColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = folder.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.nameTamil,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = folder.nameEnglish,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = folder.primaryColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = folder.primaryColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, folder.primaryColor.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "$docCount",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = folder.primaryColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = folder.primaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==============================================================
// Realistic Folder Card Item
// ==============================================================
@Composable
private fun FolderItemCard(
    folder: LibraryFolder,
    docCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("folder_card_${folder.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.12f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Realistic 3D Folder / Book Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(folder.primaryColor, folder.secondaryColor)
                        )
                    )
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = folder.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
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
                        text = folder.nameTamil,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = folder.primaryColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, folder.primaryColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "$docCount புத்தகங்கள்",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = folder.primaryColor,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = folder.description,
                    fontSize = 12.sp,
                    color = SubikshaTextMuted,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open Folder",
                tint = folder.primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ==============================================================
// Document Card Item (Inside Folder)
// ==============================================================
@Composable
private fun DocumentCardItem(
    document: LibraryDocument,
    accentColor: Color,
    onClick: () -> Unit,
    onReadHtml: () -> Unit = {},
    onDownloadPdf: () -> Unit = {},
    onDelete: () -> Unit
) {
    if (document.fileType == DocumentType.HTML_BOOK) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .testTag("doc_item_${document.id}"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = document.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "109 பாடல்கள்",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = document.subtitle,
                            fontSize = 11.5.sp,
                            color = SubikshaTextMuted,
                            lineHeight = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Document",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReadHtml,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "படிக்க (Read)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onDownloadPdf,
                        border = BorderStroke(1.2.dp, Color(0xFFDC2626)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PDF சேமி",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .testTag("doc_item_${document.id}"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (document.fileType == DocumentType.PDF) Color(0xFFFEE2E2) else Color(0xFFDBEAFE),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (document.fileType == DocumentType.PDF) Icons.Default.PictureAsPdf else Icons.Default.Image,
                            contentDescription = null,
                            tint = if (document.fileType == DocumentType.PDF) Color(0xFFDC2626) else Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${document.dateAdded} • ${document.sizeText}",
                        fontSize = 11.sp,
                        color = SubikshaTextMuted
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Document",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==============================================================
// ஔவையார் ஆத்திசூடி Mobile In-App HTML Reader Screen
// ==============================================================
@Composable
fun AathichudiHtmlReaderScreen(
    title: String = "ஔவையார் ஆத்திசூடி",
    htmlAssetPath: String = "aathichudi.html",
    pdfAssetPath: String = "pdf/aathichudi_full_book.pdf",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SubikshaNavy
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    Button(
                        onClick = {
                            downloadPdfToDevice(
                                context,
                                pdfAssetPath,
                                "Avvaiyar_Aathichudi_Full_Book.pdf"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PDF சேமி",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        defaultTextEncodingName = "utf-8"
                    }
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    loadUrl("file:///android_asset/$htmlAssetPath")
                }
            }
        )
    }
}

// ==============================================================
// Safe PDF Download Helper to Downloads Folder
// ==============================================================
fun downloadPdfToDevice(context: android.content.Context, assetPath: String, fileName: String) {
    try {
        val inputStream = context.assets.open(assetPath)
        val success: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                success = true
            } else {
                success = false
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val destFile = File(downloadsDir, fileName)
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            success = true
        }

        if (success) {
            Toast.makeText(context, "ஔவையார் ஆத்திசூடி PDF Downloads பகுதியில் சேமிக்கப்பட்டது!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "பதிவிறக்க முடியவில்லை, மீண்டும் முயலவும்", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "பிழை: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
