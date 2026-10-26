package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.*

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GeminiChatScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val geminiUrl = "https://gemini.google.com/app"
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }

    // Intercept Android hardware/gesture back press:
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBackClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SubikshaBackground)
    ) {
        // 1. ULTRA DELUXE TOP HEADER (Matching Home Page aesthetic)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SubikshaNavy,
                            SubikshaRoyalPurple
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button (Elevated White Circle matching Home profile buttons)
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("gemini_back_button")
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
                                    text = "சுபிக்ஷா AI ஆசிரியர்",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Glowing Green Online Indicator Dot
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SubikshaMintGreen)
                                )
                            }
                            Text(
                                text = "Google Gemini 2.5 Flash • நேரலை அரட்டை",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Right Actions: Listen/Full Tab Button & Refresh Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "🔊 Listen Tab" Button (Launches Ultra Deluxe Chrome Custom Tab with full TTS and Google Account)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .height(38.dp)
                                .clickable { launchGeminiCustomTab(context, geminiUrl) }
                                .testTag("gemini_listen_tab_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen Tab",
                                    tint = SubikshaRoyalPurple,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Listen Tab",
                                    color = SubikshaRoyalPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Refresh Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("gemini_refresh_button")
                        ) {
                            IconButton(onClick = {
                                hasError = false
                                webViewInstance?.reload()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = SubikshaNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Tip Pill below header explaining the Listen feature
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 உதவிக்குறிப்பு: ",
                            color = SubikshaPastelPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "பதில்களின் கீழே உள்ள '...' அழுத்தி Listen மூலம் பெண் குரலில் கேட்கலாம்!",
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Progress Loading Indicator
        if (isLoading) {
            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = SubikshaRoyalPurple,
                trackColor = Color(0xFFE2E8F0)
            )
        }

        // 2. ULTRA DELUXE CHAT CONTAINER (Matching Home Page's Welcome Card style)
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Modern mobile Chrome User-Agent:
                            // Strips 'Version/4.0' and '; wv' so Google serves full web features including Listen & Sign-In!
                            val modernChromeUa = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                mediaPlaybackRequiresUserGesture = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                cacheMode = WebSettings.LOAD_DEFAULT
                                userAgentString = modernChromeUa
                                allowFileAccess = true
                                allowContentAccess = true
                            }

                            // Enable modern cookies
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress / 100f
                                    if (newProgress >= 100) {
                                        isLoading = false
                                    }
                                }

                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    request?.grant(request.resources)
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    hasError = false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        isLoading = false
                                        hasError = true
                                    }
                                }
                            }

                            loadUrl(geminiUrl)
                            webViewInstance = this
                        }
                    },
                    update = { webView ->
                        webViewInstance = webView
                    }
                )

                // Offline / Error fallback
                if (hasError) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "இணைய இணைப்பு தேவை",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Google Gemini-ஐ அணுக இணைய இணைப்பைச் சரிபார்க்கவும்.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                hasError = false
                                isLoading = true
                                webViewInstance?.loadUrl(geminiUrl)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple)
                        ) {
                            Text("மீண்டும் முயற்சிக்கவும் (Retry)")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Launches Google Gemini inside an Ultra Deluxe Chrome Custom Tab with Subiksha brand colors.
 * This shares the device's Google Account session, enabling the native "🔊 Listen" button,
 * "Branch in new chat", "Export to Docs", and "Draft in Gmail" with 100% fidelity.
 */
fun launchGeminiCustomTab(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(0xFF7C3AED.toInt()) // SubikshaRoyalPurple
                    .build()
            )
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
