package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.theme.*

class SpeechRecognitionBridge(
    private val onStartListening: () -> Unit
) {
    @JavascriptInterface
    fun startListening() {
        onStartListening()
    }
}

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

    fun injectSpeechPolyfill(webView: WebView?) {
        val polyfillJs = """
            (function() {
                if (window.__speechBridgeInstalled) return;
                window.__speechBridgeInstalled = true;

                class SpeechRecognitionPolyfill {
                    constructor() {
                        this.continuous = false;
                        this.interimResults = false;
                        this.lang = 'ta-IN';
                        this.onresult = null;
                        this.onerror = null;
                        this.onend = null;
                        this.onstart = null;

                        window.__myRecognitionOnResult = (text) => {
                            if (this.onresult) {
                                this.onresult({
                                    results: [[{ transcript: text }]],
                                    resultIndex: 0
                                });
                            }
                            if (this.onend) this.onend();
                        };

                        window.__myRecognitionOnError = (err) => {
                            if (this.onerror) this.onerror({ error: err });
                            if (this.onend) this.onend();
                        };
                    }

                    start() {
                        if (window.AndroidBridge) {
                            if (this.onstart) this.onstart();
                            window.AndroidBridge.startListening();
                        }
                    }

                    stop() {
                        if (this.onend) this.onend();
                    }

                    abort() {
                        this.stop();
                    }
                }

                window.webkitSpeechRecognition = SpeechRecognitionPolyfill;
                window.SpeechRecognition = SpeechRecognitionPolyfill;

                // Hook directly into Gemini web mic buttons
                function hookMicButtons() {
                    const micBtns = document.querySelectorAll(
                        'button[aria-label*="mic" i], button[aria-label*="voice" i], button[aria-label*="microphone" i], button[aria-label*="மைக்" i], mat-icon[data-mat-icon-name="mic"], button.mic-button, [data-test-id*="mic"]'
                    );
                    micBtns.forEach(btn => {
                        if (!btn.__androidHooked) {
                            btn.__androidHooked = true;
                            btn.addEventListener('click', function(e) {
                                if (window.AndroidBridge) {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    window.AndroidBridge.startListening();
                                }
                            }, true);
                        }
                    });
                }
                hookMicButtons();
                setInterval(hookMicButtons, 1200);
            })();
        """.trimIndent()
        webView?.evaluateJavascript(polyfillJs, null)
    }

    fun submitRecognizedSpeechToGemini(speechText: String) {
        if (speechText.isBlank()) return
        val escaped = speechText
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", " ")

        val js = """
            (function() {
                const text = '$escaped';
                if (window.__myRecognitionOnResult) {
                    try { window.__myRecognitionOnResult(text); } catch(e) {}
                }

                // Directly insert into prompt textbox as guaranteed fallback
                const inputEl = document.querySelector('rich-textarea p') || 
                                document.querySelector('rich-textarea div[contenteditable="true"]') ||
                                document.querySelector('div[contenteditable="true"][role="textbox"]') ||
                                document.querySelector('div[contenteditable="true"]') ||
                                document.querySelector('textarea');
                if (inputEl) {
                    inputEl.focus();
                    inputEl.textContent = text;
                    inputEl.dispatchEvent(new Event('input', { bubbles: true }));
                    inputEl.dispatchEvent(new Event('change', { bubbles: true }));
                    setTimeout(function() {
                        const sendBtn = document.querySelector('button[aria-label*="Send" i]') ||
                                        document.querySelector('button[aria-label*="அனுப்பு" i]') ||
                                        document.querySelector('button.send-button');
                        if (sendBtn) {
                            sendBtn.click();
                        }
                    }, 400);
                }
            })();
        """.trimIndent()
        webViewInstance?.evaluateJavascript(js, null)
        Toast.makeText(context, "\"$speechText\" அனுப்பப்பட்டது!", Toast.LENGTH_SHORT).show()
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognized = spokenMatches?.firstOrNull().orEmpty()
            if (recognized.isNotEmpty()) {
                submitRecognizedSpeechToGemini(recognized)
            }
        }
    }

    fun startNativeListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Gemini AI — தயவுசெய்து பேசுங்கள்...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "கூகுள் வாய்ஸ் மைக்ரோஃபோன் தேவை", Toast.LENGTH_SHORT).show()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startNativeListening()
        } else {
            Toast.makeText(context, "மைக்ரோஃபோன் அனுமதி தேவை (Mic permission needed)", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndStartListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startNativeListening()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAF5FF),
                        Color(0xFFF3E8FF),
                        Color(0xFFEDE9FE)
                    )
                )
            )
    ) {
        // 1. ULTRA DELUXE TOP HEADER (Matching Home Page & Mental Maths aesthetic)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SubikshaNavy,
                                Color(0xFF1E1B4B),
                                SubikshaRoyalPurple
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Elevated Back Button
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(40.dp)
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

                    // Teacher Avatar with Live Badge
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, SubikshaMintGreen, CircleShape),
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
                        // Online glowing green indicator badge anchored at bottom-end
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.BottomEnd)
                                .background(SubikshaMintGreen, CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }

                    // Title & Subtitle column (now gets full available width without breaking)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "சுபிக்ஷா AI ஆசிரியர்",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SubikshaMintGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Gemini 2.5 Flash • நேரலை",
                                color = Color(0xFFC7D2FE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right Actions: Listen Button & Refresh Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "🔊 Listen Tab" Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("gemini_listen_tab_button")
                        ) {
                            IconButton(onClick = { launchGeminiCustomTab(context, geminiUrl) }) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen Voice Tab",
                                    tint = SubikshaRoyalPurple,
                                    modifier = Modifier.size(20.dp)
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
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sleek Dismissible Tip Pill below header
        var showTipBanner by remember { mutableStateOf(true) }
        if (showTipBanner) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SubikshaPastelPurple.copy(alpha = 0.25f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💡", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "பதில்களின் கீழ் உள்ள '...' அழுத்தி Listen மூலம் பெண் குரலில் கேட்கலாம்",
                        color = SubikshaNavy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = { showTipBanner = false },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tip",
                            tint = SubikshaNavy.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
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

                            // Bind Native Speech Bridge to WebView
                            addJavascriptInterface(
                                SpeechRecognitionBridge(
                                    onStartListening = {
                                        (context as? Activity)?.runOnUiThread {
                                            checkAndStartListening()
                                        } ?: run {
                                            checkAndStartListening()
                                        }
                                    }
                                ),
                                "AndroidBridge"
                            )

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress / 100f
                                    if (newProgress >= 70) {
                                        injectSpeechPolyfill(view)
                                    }
                                    if (newProgress >= 100) {
                                        isLoading = false
                                        injectSpeechPolyfill(view)
                                    }
                                }

                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    (context as? Activity)?.runOnUiThread {
                                        request?.grant(request.resources)
                                    } ?: run {
                                        request?.grant(request?.resources)
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
                                    injectSpeechPolyfill(view)
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

                // Floating Action Button for Direct Voice Speaking
                ExtendedFloatingActionButton(
                    onClick = { checkAndStartListening() },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "குரல் வழி பேசுங்கள்",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "பேசுங்கள்",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    },
                    containerColor = SubikshaRoyalPurple,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp, end = 16.dp)
                        .shadow(10.dp, RoundedCornerShape(24.dp))
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
