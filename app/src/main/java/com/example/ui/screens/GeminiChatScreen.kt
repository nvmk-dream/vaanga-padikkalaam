package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.SubikshaNavy
import com.example.ui.theme.SubikshaRoyalPurple

@OptIn(ExperimentalMaterial3Api::class)
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
    // If WebView can go back to previous chat/page, navigate back inside webView.
    // Otherwise, return to the app's HomeScreen.
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Google Gemini AI",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubikshaNavy
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = SubikshaRoyalPurple.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Official Web",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SubikshaRoyalPurple,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "gemini.google.com/app",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("gemini_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SubikshaNavy
                            )
                        }
                    },
                    actions = {
                        // Refresh button
                        IconButton(
                            onClick = {
                                hasError = false
                                webViewInstance?.reload()
                            },
                            modifier = Modifier.testTag("gemini_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = SubikshaNavy
                            )
                        }

                        // Open in Chrome / External Browser
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geminiUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("gemini_open_browser_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open in Browser",
                                tint = SubikshaNavy
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )

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
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

                        // Enable modern browser capabilities required by Gemini PWA
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }

                        // Enable cookies
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
