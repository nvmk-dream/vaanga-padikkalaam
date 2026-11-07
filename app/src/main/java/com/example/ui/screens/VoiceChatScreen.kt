package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VoiceChatScreenState {
    HOME,
    PROVIDER_SELECT,
    ACTIVE_LISTENING,
    CHAT_CONVERSATION,
    STOPPED,
    SETTINGS,
    HISTORY
}

@Composable
fun VoiceChatScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiKeyManager = remember { ApiKeyManager(context) }
    val voiceService = remember { AiVoiceChatService(apiKeyManager) }

    // Screen State
    var screenState by remember { mutableStateOf(VoiceChatScreenState.HOME) }
    var selectedProvider by remember {
        mutableStateOf(if (apiKeyManager.getDefaultProvider() == "gemini") AiProvider.GEMINI else AiProvider.CHATGPT)
    }

    // Voice Settings state
    var voiceReplyEnabled by remember { mutableStateOf(apiKeyManager.isVoiceReplyEnabled()) }
    var preferredLanguage by remember { mutableStateOf(apiKeyManager.getPreferredLanguage()) }
    var voiceSpeed by remember { mutableStateOf(apiKeyManager.getVoiceSpeed()) }
    var autoPlayVoice by remember { mutableStateOf(apiKeyManager.isAutoPlayVoice()) }

    var openAiKeyInput by remember { mutableStateOf(apiKeyManager.getOpenAiKey()) }
    var geminiKeyInput by remember { mutableStateOf(apiKeyManager.getApiKey()) }

    // Conversation state
    val messages = remember {
        mutableStateListOf(
            VoiceChatMessage(
                id = "msg_init",
                sender = "ai",
                provider = selectedProvider,
                text = "வணக்கம்! நான் Subiksha Teacher AI. உங்களுடன் குரல் வழியாகப் பேசத் தயாராக இருக்கிறேன். என்னிடம் சந்தேகங்கள் அல்லது ஆங்கிலப் பயிற்சி பற்றிக் கேளுங்கள்!",
                timestamp = "10:15 AM",
                audioDurationSec = 10
            )
        )
    }

    var isAiResponding by remember { mutableStateOf(false) }
    var currentlyPlayingMessageId by remember { mutableStateOf<String?>(null) }
    var recognizedSpeechText by remember { mutableStateOf("") }
    var showTextInputDialog by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf("") }

    // Text to Speech Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = if (preferredLanguage == "en") Locale.US else Locale("ta", "IN")
                val res = ttsEngine?.setLanguage(locale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsEngine?.language = Locale.US
                }
                ttsEngine?.setPitch(1.15f)
                val rate = when (voiceSpeed) {
                    "slow" -> 0.75f
                    "fast" -> 1.15f
                    else -> 0.90f
                }
                ttsEngine?.setSpeechRate(rate)
                isTtsReady = true
            }
        }
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun playTts(text: String, msgId: String) {
        if (!isTtsReady || ttsEngine == null) return
        ttsEngine?.stop()
        currentlyPlayingMessageId = msgId
        val rate = when (voiceSpeed) {
            "slow" -> 0.72f
            "fast" -> 1.15f
            else -> 0.88f
        }
        ttsEngine?.setSpeechRate(rate)
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, msgId)
    }

    fun stopTts() {
        ttsEngine?.stop()
        currentlyPlayingMessageId = null
    }

    fun handleUserUtterance(userInput: String) {
        if (userInput.isBlank()) return
        stopTts()

        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val userMsg = VoiceChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = "user",
            provider = selectedProvider,
            text = userInput,
            timestamp = timeStr
        )
        messages.add(userMsg)
        screenState = VoiceChatScreenState.CHAT_CONVERSATION
        isAiResponding = true

        coroutineScope.launch {
            val replyText = voiceService.getAiResponse(userInput, selectedProvider, preferredLanguage)
            isAiResponding = false

            val aiMsg = VoiceChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = "ai",
                provider = selectedProvider,
                text = replyText,
                timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            )
            messages.add(aiMsg)

            if (voiceReplyEnabled && autoPlayVoice) {
                delay(300)
                playTts(replyText, aiMsg.id)
            }
        }
    }

    // Android Speech Recognizer Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognized = spokenMatches?.firstOrNull().orEmpty()
            if (recognized.isNotEmpty()) {
                recognizedSpeechText = recognized
                handleUserUtterance(recognized)
            } else {
                screenState = VoiceChatScreenState.STOPPED
            }
        } else {
            screenState = VoiceChatScreenState.STOPPED
        }
    }

    fun startListening() {
        screenState = VoiceChatScreenState.ACTIVE_LISTENING
        stopTts()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val langTag = if (preferredLanguage == "en") "en-US" else "ta-IN"
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Subiksha Teacher AI — தயவுசெய்து பேசுங்கள்...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "குரல் பதிவு செய்ய அனுமதி அல்லது கூகுள் வாய்ஸ் தேவை", Toast.LENGTH_SHORT).show()
            screenState = VoiceChatScreenState.CHAT_CONVERSATION
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(context, "மைக்ரோஃபோன் அனுமதி தேவை (Mic permission needed)", Toast.LENGTH_LONG).show()
        }
    }

    // BackHandler
    BackHandler {
        stopTts()
        when (screenState) {
            VoiceChatScreenState.SETTINGS,
            VoiceChatScreenState.HISTORY,
            VoiceChatScreenState.PROVIDER_SELECT -> screenState = VoiceChatScreenState.HOME
            VoiceChatScreenState.ACTIVE_LISTENING,
            VoiceChatScreenState.STOPPED -> screenState = VoiceChatScreenState.CHAT_CONVERSATION
            VoiceChatScreenState.CHAT_CONVERSATION -> screenState = VoiceChatScreenState.HOME
            VoiceChatScreenState.HOME -> onBackClick()
        }
    }

    // Theme Colors matching User Technical Spec & Glassmorphism CSS
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80) // CSS: #b45b80
    val mauveDeep = Color(0xFF633249)
    val deepTextColor = Color(0xFF2E1A23)
    val mutedTextColor = Color(0xFF5A3A4A)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF85405F), Color(0xFF71334F))
    )
    val glassCardBackground = Color.White.copy(alpha = 0.08f)
    val glassBorder = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f))

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        // Glassmorphism Architecture: Image 2 Violet Flower Wallpaper (100% visible, no dense overlay)
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg_violet_flower),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
            when (screenState) {
                VoiceChatScreenState.HOME -> {
                    VoiceChatHomeScreen(
                        selectedProvider = selectedProvider,
                        onSelectProvider = { selectedProvider = it },
                        onStart = {
                            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        },
                        onNavigateProviderSelect = { screenState = VoiceChatScreenState.PROVIDER_SELECT },
                        onNavigateSettings = { screenState = VoiceChatScreenState.SETTINGS },
                        onBack = onBackClick
                    )
                }

                VoiceChatScreenState.PROVIDER_SELECT -> {
                    AiProviderSelectScreen(
                        selectedProvider = selectedProvider,
                        onSelectProvider = {
                            selectedProvider = it
                            apiKeyManager.setDefaultProvider(it.id)
                        },
                        onContinue = {
                            screenState = VoiceChatScreenState.HOME
                        },
                        onBack = { screenState = VoiceChatScreenState.HOME }
                    )
                }

                VoiceChatScreenState.SETTINGS -> {
                    VoiceChatSettingsScreen(
                        preferredLanguage = preferredLanguage,
                        onLanguageChange = {
                            preferredLanguage = it
                            apiKeyManager.setPreferredLanguage(it)
                        },
                        voiceReply = voiceReplyEnabled,
                        onVoiceReplyChange = {
                            voiceReplyEnabled = it
                            apiKeyManager.setVoiceReplyEnabled(it)
                        },
                        voiceSpeed = voiceSpeed,
                        onVoiceSpeedChange = {
                            voiceSpeed = it
                            apiKeyManager.setVoiceSpeed(it)
                        },
                        autoPlayVoice = autoPlayVoice,
                        onAutoPlayChange = {
                            autoPlayVoice = it
                            apiKeyManager.setAutoPlayVoice(it)
                        },
                        openAiKey = openAiKeyInput,
                        onOpenAiKeyChange = { openAiKeyInput = it },
                        geminiKey = geminiKeyInput,
                        onGeminiKeyChange = { geminiKeyInput = it },
                        onSave = {
                            apiKeyManager.saveOpenAiKey(openAiKeyInput)
                            apiKeyManager.saveApiKey(geminiKeyInput)
                            Toast.makeText(context, "அமைப்புகள் சேமிக்கப்பட்டன!", Toast.LENGTH_SHORT).show()
                            screenState = VoiceChatScreenState.HOME
                        },
                        onBack = { screenState = VoiceChatScreenState.HOME }
                    )
                }

                VoiceChatScreenState.ACTIVE_LISTENING -> {
                    ActiveListeningScreen(
                        provider = selectedProvider,
                        onStop = {
                            stopTts()
                            screenState = VoiceChatScreenState.STOPPED
                        },
                        onSpeakAgain = {
                            startListening()
                        },
                        onTextInput = {
                            showTextInputDialog = true
                        },
                        onBack = { screenState = VoiceChatScreenState.CHAT_CONVERSATION }
                    )
                }

                VoiceChatScreenState.CHAT_CONVERSATION -> {
                    ChatConversationScreen(
                        provider = selectedProvider,
                        messages = messages,
                        isAiResponding = isAiResponding,
                        currentlyPlayingId = currentlyPlayingMessageId,
                        onPlayAudio = { text, id -> playTts(text, id) },
                        onStopAudio = { stopTts() },
                        onMicClick = {
                            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        },
                        onStopSession = {
                            stopTts()
                            screenState = VoiceChatScreenState.STOPPED
                        },
                        onTextInput = { showTextInputDialog = true },
                        onOpenSettings = { screenState = VoiceChatScreenState.SETTINGS },
                        onBack = { screenState = VoiceChatScreenState.HOME }
                    )
                }

                VoiceChatScreenState.STOPPED -> {
                    StoppedSessionScreen(
                        onRestart = {
                            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        },
                        onViewChat = {
                            screenState = VoiceChatScreenState.CHAT_CONVERSATION
                        },
                        onBack = { screenState = VoiceChatScreenState.HOME }
                    )
                }

                VoiceChatScreenState.HISTORY -> {
                    VoiceHistoryScreen(
                        onBack = { screenState = VoiceChatScreenState.HOME },
                        onSelectHistory = { prompt ->
                            handleUserUtterance(prompt)
                        }
                    )
                }
            }
        }
    }
}

    // Manual Text Input Dialog
    if (showTextInputDialog) {
        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            title = {
                Text(
                    text = "AI-யிடம் கேள்வி கேட்க",
                    fontWeight = FontWeight.Bold,
                    color = mauveDeep
                )
            },
            text = {
                OutlinedTextField(
                    value = manualInputText,
                    onValueChange = { manualInputText = it },
                    placeholder = { Text("உங்கள் கேள்வியை இங்கே தட்டச்சு செய்யவும்...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = deepTextColor,
                        unfocusedTextColor = deepTextColor,
                        focusedBorderColor = roseGoldAccent,
                        unfocusedBorderColor = mauveHeader.copy(alpha = 0.5f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val query = manualInputText
                        manualInputText = ""
                        showTextInputDialog = false
                        handleUserUtterance(query)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = mauveHeader),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("அனுப்பு (Send)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("ரத்து (Cancel)", color = mutedTextColor)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ==============================================================
// 1. Voice Chat Home Screen (Image 1 in Collage)
// ==============================================================
@Composable
private fun VoiceChatHomeScreen(
    selectedProvider: AiProvider,
    onSelectProvider: (AiProvider) -> Unit,
    onStart: () -> Unit,
    onNavigateProviderSelect: () -> Unit,
    onNavigateSettings: () -> Unit,
    onBack: () -> Unit
) {
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80)
    val mauveDeep = Color(0xFF633249)
    val deepTextColor = Color(0xFF2E1A23)
    val mutedTextColor = Color(0xFF5A3A4A)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF85405F), Color(0xFF71334F))
    )
    val glassCardBackground = Color.White.copy(alpha = 0.08f)
    val glassBorder = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = mauveHeader
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Subiksha Teacher",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = mauveDeep
                    )
                    Text(
                        text = "AI Voice Chat • தமிழ் & English",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = roseGoldAccent
                    )
                }

                IconButton(
                    onClick = onNavigateSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                        .testTag("voice_home_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = mauveHeader,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Cute Robot AI Banner (.main-text-card)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(glassBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = glassCardBackground),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Robot Icon Avatar
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(buttonGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Robot",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Voice Chat",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = mauveDeep
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "உங்கள் குரலில் பேசுங்கள்... AI உங்களைப் புரிந்து கொண்டு பதில் அளிக்கும்!",
                            fontSize = 12.sp,
                            color = deepTextColor,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Two AI Selection Cards Side by Side
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ChatGPT Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            BorderStroke(
                                width = 0.5.dp,
                                color = if (selectedProvider == AiProvider.CHATGPT) roseGoldAccent else Color.White.copy(alpha = 0.30f)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectProvider(AiProvider.CHATGPT) }
                        .testTag("provider_chatgpt_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedProvider == AiProvider.CHATGPT) roseGoldAccent.copy(alpha = 0.15f) else glassCardBackground
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "ChatGPT",
                            tint = if (selectedProvider == AiProvider.CHATGPT) roseGoldAccent else Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ChatGPT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = deepTextColor
                        )
                        Text(
                            text = "AI Assistant",
                            fontSize = 10.sp,
                            color = mutedTextColor
                        )
                    }
                }

                // Gemini Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            BorderStroke(
                                width = 0.5.dp,
                                color = if (selectedProvider == AiProvider.GEMINI) roseGoldAccent else Color.White.copy(alpha = 0.30f)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectProvider(AiProvider.GEMINI) }
                        .testTag("provider_gemini_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedProvider == AiProvider.GEMINI) roseGoldAccent.copy(alpha = 0.15f) else glassCardBackground
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini",
                            tint = roseGoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gemini",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = deepTextColor
                        )
                        Text(
                            text = "Google AI",
                            fontSize = 10.sp,
                            color = mutedTextColor
                        )
                    }
                }
            }
        }

        // Features Bullet Points Card (.result-card)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(glassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = glassCardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FeatureCheckRow("தமிழ், English அல்லது கலவை மொழியில் பேசலாம்", deepTextColor, roseGoldAccent)
                    FeatureCheckRow("AI குரலில் உடனடி பதில் சொல்லும்", deepTextColor, roseGoldAccent)
                    FeatureCheckRow("Text + Voice இரண்டும் ஒரே நேரத்தில் கிடைக்கும்", deepTextColor, roseGoldAccent)
                    FeatureCheckRow("உங்கள் விருப்பமான AI-ஐத் தேர்ந்தெடுத்துக் கொள்ளலாம்", deepTextColor, roseGoldAccent)
                }
            }
        }


        // Main CTA Button "தொடங்குங்கள்" (.copy-button gradient & rounded 50.dp)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(5.dp, RoundedCornerShape(50.dp), ambientColor = Color(0x33642841))
                    .clip(RoundedCornerShape(50.dp))
                    .background(buttonGradient)
                    .testTag("voice_chat_start_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start Voice",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "தொடங்குங்கள் (Start Voice)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCheckRow(text: String, textColor: Color, checkColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = checkColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==============================================================
// 2. AI Provider Selection Screen (Image 2 in Collage)
// ==============================================================
@Composable
private fun AiProviderSelectScreen(
    selectedProvider: AiProvider,
    onSelectProvider: (AiProvider) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80)
    val mauveDeep = Color(0xFF633249)
    val deepTextColor = Color(0xFF2E1A23)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF85405F), Color(0xFF71334F))
    )
    val glassCardBackground = Color.White.copy(alpha = 0.08f)
    val glassBorder = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = mauveHeader)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AI Provider Select",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = mauveDeep
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "உங்களுக்கு பிடித்த AI ஐத் தேர்ந்தெடுக்கவும்",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = mauveDeep
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ChatGPT Selector
        ProviderRadioCard(
            title = "ChatGPT",
            subtitle = "உலகின் சக்திவாய்ந்த AI உதவியாளர்",
            isSelected = selectedProvider == AiProvider.CHATGPT,
            icon = Icons.Default.ChatBubble,
            accentColor = roseGoldAccent,
            onClick = { onSelectProvider(AiProvider.CHATGPT) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gemini Selector
        ProviderRadioCard(
            title = "Gemini",
            subtitle = "Google இன் AI உதவியாளர்",
            isSelected = selectedProvider == AiProvider.GEMINI,
            icon = Icons.Default.AutoAwesome,
            accentColor = roseGoldAccent,
            onClick = { onSelectProvider(AiProvider.GEMINI) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Why choose card (.result-card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(32.dp), ambientColor = Color(0x1A50323C))
                .border(glassBorder, RoundedCornerShape(32.dp)),
            colors = CardDefaults.cardColors(containerColor = glassCardBackground),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "உங்கள் AI-யை ஏன் தேர்வு செய்ய வேண்டும்?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = mauveDeep
                )
                Spacer(modifier = Modifier.height(10.dp))
                FeatureCheckRow("உடனடி மற்றும் துல்லியமான பதில்", deepTextColor, roseGoldAccent)
                Spacer(modifier = Modifier.height(6.dp))
                FeatureCheckRow("தமிழ், English மற்றும் கலவை மொழி ஆதரவு", deepTextColor, roseGoldAccent)
                Spacer(modifier = Modifier.height(6.dp))
                FeatureCheckRow("குரல் மூலம் இயல்பான உரையாடல்", deepTextColor, roseGoldAccent)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(5.dp, RoundedCornerShape(50.dp), ambientColor = Color(0x33642841))
                .clip(RoundedCornerShape(50.dp))
                .background(buttonGradient),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text("தொடரவும் (Continue) ->", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ProviderRadioCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val glassCardBackground = Color.White.copy(alpha = 0.10f)
    val deepTextColor = Color(0xFF2E1A23)
    val mutedTextColor = Color(0xFF5A3A4A)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                BorderStroke(
                    width = 0.5.dp,
                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.30f)
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.15f) else glassCardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = deepTextColor)
                Text(text = subtitle, fontSize = 12.sp, color = mutedTextColor)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
            )
        }
    }
}

// ==============================================================
// 3. Language & Voice Settings Screen (Images 3 & 9 in Collage)
// ==============================================================
@Composable
private fun VoiceChatSettingsScreen(
    preferredLanguage: String,
    onLanguageChange: (String) -> Unit,
    voiceReply: Boolean,
    onVoiceReplyChange: (Boolean) -> Unit,
    voiceSpeed: String,
    onVoiceSpeedChange: (String) -> Unit,
    autoPlayVoice: Boolean,
    onAutoPlayChange: (Boolean) -> Unit,
    openAiKey: String,
    onOpenAiKeyChange: (String) -> Unit,
    geminiKey: String,
    onGeminiKeyChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80)
    val mauveDeep = Color(0xFF633249)
    val deepTextColor = Color(0xFF2E1A23)
    val mutedTextColor = Color(0xFF5A3A4A)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF85405F), Color(0xFF71334F))
    )
    val glassCardBackground = Color.White.copy(alpha = 0.08f)
    val glassBorder = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f))

    var showOpenAiKey by remember { mutableStateOf(false) }
    var showGeminiKey by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = mauveHeader)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Voice Chat Settings",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = mauveDeep
                )
            }
        }

        // 1. மொழித் தேர்வு (.main-text-card)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(glassBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = glassCardBackground),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "மொழித் தேர்வு (Language)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = mauveDeep
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LanguageRadioRow("தமிழ் (Tamil)", preferredLanguage == "ta", roseGoldAccent, deepTextColor) { onLanguageChange("ta") }
                    LanguageRadioRow("English", preferredLanguage == "en", roseGoldAccent, deepTextColor) { onLanguageChange("en") }
                    LanguageRadioRow("தமிழ் + English (Mixed)", preferredLanguage == "mixed", roseGoldAccent, deepTextColor) { onLanguageChange("mixed") }
                }
            }
        }

        // 2. AI பதில் குரல் & தானியங்கி Play
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(glassBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = glassCardBackground),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AI பதில் குரல்", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = deepTextColor)
                            Text("AI குரலில் பதில் பேசும்", fontSize = 11.sp, color = mutedTextColor)
                        }
                        Switch(
                            checked = voiceReply,
                            onCheckedChange = onVoiceReplyChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = roseGoldAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("தானியங்கி Play (Auto Play)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = deepTextColor)
                            Text("AI பதிலை தானாகவே கேட்கவும்", fontSize = 11.sp, color = mutedTextColor)
                        }
                        Switch(
                            checked = autoPlayVoice,
                            onCheckedChange = onAutoPlayChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = roseGoldAccent
                            )
                        )
                    }
                }
            }
        }

        // 3. குரல் வேகம் (.language-tabs)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(glassBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = glassCardBackground),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("குரல் வேகம் (Voice Speed)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = mauveDeep)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(20.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SpeedButton("மெதுவாக", voiceSpeed == "slow") { onVoiceSpeedChange("slow") }
                        SpeedButton("சாதாரண", voiceSpeed == "normal") { onVoiceSpeedChange("normal") }
                        SpeedButton("வேகமாக", voiceSpeed == "fast") { onVoiceSpeedChange("fast") }
                    }
                }
            }
        }

        // 4. Secret AI Keys (OpenAI & Gemini)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(glassBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = glassCardBackground),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Secret AI Keys (உங்கள் சாவிகள்)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = mauveDeep)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "சாவிகள் உங்கள் மொபைல் சாதனத்தில் மட்டுமே பாதுகாப்பாகச் சேமிக்கப்படும்.",
                        fontSize = 11.sp,
                        color = mutedTextColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // OpenAI Key
                    Text("OPENAI_API_KEY (ChatGPT)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = deepTextColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = openAiKey,
                        onValueChange = onOpenAiKeyChange,
                        placeholder = { Text("sk-proj-...") },
                        visualTransformation = if (showOpenAiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showOpenAiKey = !showOpenAiKey }) {
                                Icon(if (showOpenAiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = mauveHeader)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = deepTextColor,
                            unfocusedTextColor = deepTextColor,
                            focusedBorderColor = roseGoldAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White.copy(alpha = 0.12f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gemini Key
                    Text("GEMINI_API_KEY (Google AI)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = deepTextColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = onGeminiKeyChange,
                        placeholder = { Text("AQ.Ab8RN6...") },
                        visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                Icon(if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = mauveHeader)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = deepTextColor,
                            unfocusedTextColor = deepTextColor,
                            focusedBorderColor = roseGoldAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White.copy(alpha = 0.12f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }

        // Action Buttons: Horizontal Row with Cancel & Save (.copy-button style)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(0.5.dp, mauveHeader)
                ) {
                    Text("ரத்து செய்", color = mauveHeader, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(buttonGradient),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("சேமி (Save)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun LanguageRadioRow(label: String, isSelected: Boolean, accentColor: Color, textColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = accentColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = textColor, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun RowScope.SpeedButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isSelected) Color(0xFFB45B80) else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF7D405D)
        )
    }
}

// ==============================================================
// 4. Active Listening Screen with Pulsing Soundwaves (Image 4 in Collage)
// ==============================================================
@Composable
private fun ActiveListeningScreen(
    provider: AiProvider,
    onStop: () -> Unit,
    onSpeakAgain: () -> Unit,
    onTextInput: () -> Unit,
    onBack: () -> Unit
) {
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80)
    val mauveDeep = Color(0xFF633249)
    val deepTextColor = Color(0xFF2E1A23)
    val mutedTextColor = Color(0xFF5A3A4A)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF85405F), Color(0xFF71334F))
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Provider Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = mauveHeader)
            }

            Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(roseGoldAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${provider.displayName} ● Online",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = deepTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.weight(0.7f))

        // Large Glowing Pulsing Mic Circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            roseGoldAccent.copy(alpha = 0.35f),
                            roseGoldAccent.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(buttonGradient)
                    .shadow(12.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Listening",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Listening...",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = mauveDeep
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "தயவுசெய்து பேசுங்கள்...",
            fontSize = 14.sp,
            color = mutedTextColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Soundwave simulation lines
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val heights = listOf(14, 24, 38, 20, 48, 30, 16, 42, 28, 18, 34, 12)
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height((h * pulseScale).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(roseGoldAccent)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom 3 Action Buttons (நிறுத்து, மீண்டும் பேசு, உரை)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f)), CircleShape)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
            }

            IconButton(
                onClick = onSpeakAgain,
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(buttonGradient)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Speak Again", tint = Color.White, modifier = Modifier.size(28.dp))
            }

            IconButton(
                onClick = onTextInput,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
            ) {
                Icon(Icons.Default.Keyboard, contentDescription = "Text Input", tint = mauveHeader, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ==============================================================
// 5. Chat Conversation Screen with Audio Player (Images 5, 6, 7 in Collage)
// ==============================================================
@Composable
private fun ChatConversationScreen(
    provider: AiProvider,
    messages: List<VoiceChatMessage>,
    isAiResponding: Boolean,
    currentlyPlayingId: String?,
    onPlayAudio: (String, String) -> Unit,
    onStopAudio: () -> Unit,
    onMicClick: () -> Unit,
    onStopSession: () -> Unit,
    onTextInput: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80)
    val mauveDeep = Color(0xFF633249)
    val deepTextColor = Color(0xFF2E1A23)
    val mutedTextColor = Color(0xFF5A3A4A)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF85405F), Color(0xFF71334F))
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header with provider status and settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = mauveHeader)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(roseGoldAccent))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${provider.displayName} ● Online",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = deepTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = mauveHeader)
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.sender == "user") {
                    // User Speech Bubble (Right aligned with gradient)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                            modifier = Modifier.widthIn(max = 280.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(buttonGradient)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.text,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        lineHeight = 19.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "You • ${msg.timestamp}",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // AI Response Bubble (.inner-text-box: alpha 0.12f, border 0.5px, radius 16dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(buttonGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (provider == AiProvider.GEMINI) Icons.Default.AutoAwesome else Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 14.sp,
                                    color = deepTextColor,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Audio Player Bar (.language-tabs style)
                                Surface(
                                    color = Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isPlaying = currentlyPlayingId == msg.id

                                        IconButton(
                                            onClick = {
                                                if (isPlaying) onStopAudio() else onPlayAudio(msg.text, msg.id)
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play/Pause",
                                                tint = roseGoldAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = if (isPlaying) "0:10 / 0:15" else "0:00 / 0:15",
                                            fontSize = 10.sp,
                                            color = mutedTextColor
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = null,
                                            tint = roseGoldAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${provider.displayName} • ${msg.timestamp}",
                                    fontSize = 10.sp,
                                    color = mutedTextColor
                                )
                            }
                        }
                    }
                }
            }

            if (isAiResponding) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = roseGoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${provider.displayName} பதில் தயாரிக்கிறது...",
                            fontSize = 12.sp,
                            color = mutedTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Bottom Action Toolbar matching CSS (.result-card / .language-tabs)
        Surface(
            color = Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onMicClick,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(buttonGradient)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("மைக்", fontSize = 10.sp, color = deepTextColor, fontWeight = FontWeight.Bold)
                }

                // Stop
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onStopSession,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f)), CircleShape)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("நிறுத்து", fontSize = 10.sp, color = deepTextColor)
                }

                // Replay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            val lastAi = messages.lastOrNull { it.sender == "ai" }
                            if (lastAi != null) onPlayAudio(lastAi.text, lastAi.id)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Replay", tint = mauveHeader, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("மீண்டும் கேள்", fontSize = 10.sp, color = deepTextColor)
                }

                // Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onTextInput,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = "Text", tint = mauveHeader, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("உரை", fontSize = 10.sp, color = deepTextColor)
                }
            }
        }
    }
}

// ==============================================================
// 6. Stopped Session Screen (Image 8 in Collage)
// ==============================================================
@Composable
private fun StoppedSessionScreen(
    onRestart: () -> Unit,
    onViewChat: () -> Unit,
    onBack: () -> Unit
) {
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80)
    val mauveDeep = Color(0xFF633249)
    val deepTextColor = Color(0xFF2E1A23)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF85405F), Color(0xFF71334F))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = mauveHeader)
            }
        }

        Spacer(modifier = Modifier.weight(0.8f))

        // Checkmark Icon (.inner-text-box)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .border(BorderStroke(1.dp, roseGoldAccent), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Stopped",
                tint = roseGoldAccent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "பதிவு நிறுத்தப்பட்டது",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = mauveDeep
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "மீண்டும் பேச விரும்பினால் மைக் பொத்தானை அழுத்தவும்.",
            fontSize = 12.sp,
            color = deepTextColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(buttonGradient),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("மீண்டும் தொடங்கவும் (Start Again)", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onViewChat,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(50.dp),
            border = BorderStroke(0.5.dp, mauveHeader)
        ) {
            Text("உரையாடலைப் பார்க்க (View Chat)", color = mauveHeader, fontWeight = FontWeight.Bold)
        }
    }
}

// ==============================================================
// 7. Voice Chat History Screen (Image 11 in Collage)
// ==============================================================
@Composable
private fun VoiceHistoryScreen(
    onBack: () -> Unit,
    onSelectHistory: (String) -> Unit
) {
    val mauveHeader = Color(0xFF7D405D)
    val roseGoldAccent = Color(0xFFB45B80)
    val mauveDeep = Color(0xFF633249)
    val deepTextColor = Color(0xFF2E1A23)
    val mutedTextColor = Color(0xFF5A3A4A)

    var historyTab by remember { mutableStateOf(AiProvider.CHATGPT) }

    val chatGptHistory = listOf(
        "English Speaking Practice" to "12 Apr 2026 • 10:30 AM",
        "Past Tense Explanation" to "11 Apr 2026 • 8:15 PM",
        "Tamil to English Translation" to "10 Apr 2026 • 4:45 PM",
        "Daily Conversation" to "9 Apr 2026 • 7:20 PM"
    )

    val geminiHistory = listOf(
        "General Science Doubts" to "12 Apr 2026 • 11:00 AM",
        "Classroom English Practice" to "10 Apr 2026 • 2:15 PM"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(BorderStroke(0.5.dp, mauveHeader.copy(alpha = 0.25f)), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = mauveHeader)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Voice Chat History",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = mauveDeep
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs (.language-tabs: radius 20dp, border 0.5dp, active: #b45b80)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(20.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(AiProvider.CHATGPT to "ChatGPT", AiProvider.GEMINI to "Gemini").forEach { (prov, label) ->
                val isSelected = historyTab == prov
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) roseGoldAccent else Color.Transparent)
                        .clickable { historyTab = prov }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else mauveHeader
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val list = if (historyTab == AiProvider.CHATGPT) chatGptHistory else geminiHistory
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(list) { (title, date) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)), RoundedCornerShape(20.dp))
                        .clickable { onSelectHistory(title) },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = roseGoldAccent, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = deepTextColor)
                            Text(text = date, fontSize = 10.sp, color = mutedTextColor)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = mauveHeader, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}
