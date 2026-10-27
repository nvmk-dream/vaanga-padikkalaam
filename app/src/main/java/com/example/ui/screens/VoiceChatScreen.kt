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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

    // Theme Colors matching the 12-screen UI Collage (Midnight Navy & Cyan/Royal Blue)
    val bgDark = Color(0xFF030712)
    val cardDark = Color(0xFF0B1528)
    val cyanAccent = Color(0xFF00B4D8)
    val cyanDark = Color(0xFF0077B6)
    val geminiPurple = Color(0xFF7C3AED)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = bgDark
    ) { innerPadding ->
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

    // Manual Text Input Dialog
    if (showTextInputDialog) {
        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            title = {
                Text(
                    text = "AI-யிடம் கேள்வி கேட்க",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                OutlinedTextField(
                    value = manualInputText,
                    onValueChange = { manualInputText = it },
                    placeholder = { Text("உங்கள் கேள்வியை இங்கே தட்டச்சு செய்யவும்...") },
                    modifier = Modifier.fillMaxWidth(),
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
                        val query = manualInputText
                        manualInputText = ""
                        showTextInputDialog = false
                        handleUserUtterance(query)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cyanDark)
                ) {
                    Text("அனுப்பு (Send)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("ரத்து (Cancel)", color = Color.LightGray)
                }
            },
            containerColor = cardDark
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
    val cyanAccent = Color(0xFF00B4D8)
    val cyanDark = Color(0xFF0077B6)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Subiksha Teacher",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "AI Voice Chat • தமிழ் & English",
                        fontSize = 11.sp,
                        color = cyanAccent
                    )
                }

                IconButton(
                    onClick = onNavigateSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(cyanAccent.copy(alpha = 0.15f))
                        .testTag("voice_home_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Cute Robot AI Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.2.dp, cyanAccent.copy(alpha = 0.6f)), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B172B)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Robot Icon Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00B4D8), Color(0xFF0077B6))
                                )
                            )
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Robot",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Voice Chat",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "உங்கள் குரலில் பேசுங்கள்... AI உங்களைப் புரிந்து கொண்டு பதில் அளிக்கும்!",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Two AI Selection Cards Side by Side
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ChatGPT Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(115.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(
                                width = if (selectedProvider == AiProvider.CHATGPT) 2.dp else 1.dp,
                                color = if (selectedProvider == AiProvider.CHATGPT) Color(0xFF10B981) else Color.White.copy(alpha = 0.15f)
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectProvider(AiProvider.CHATGPT) }
                        .testTag("provider_chatgpt_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedProvider == AiProvider.CHATGPT) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFF0C192E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "ChatGPT",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ChatGPT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "AI Assistant",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }

                // Gemini Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(115.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(
                                width = if (selectedProvider == AiProvider.GEMINI) 2.dp else 1.dp,
                                color = if (selectedProvider == AiProvider.GEMINI) cyanAccent else Color.White.copy(alpha = 0.15f)
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectProvider(AiProvider.GEMINI) }
                        .testTag("provider_gemini_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedProvider == AiProvider.GEMINI) Color(0xFF0369A1).copy(alpha = 0.4f) else Color(0xFF0C192E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini",
                            tint = cyanAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Gemini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Google AI",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // Features Bullet Points Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B172B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureCheckRow("தமிழ், English அல்லது கலவை மொழியில் பேசலாம்")
                    FeatureCheckRow("AI குரலில் உடனடி பதில் சொல்லும்")
                    FeatureCheckRow("Text + Voice இரண்டும் ஒரே நேரத்தில் கிடைக்கும்")
                    FeatureCheckRow("உங்கள் விருப்பமான AI-ஐத் தேர்ந்தெடுத்துக் கொள்ளலாம்")
                }
            }
        }

        // Main CTA Button "தொடங்குங்கள்"
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(cyanAccent, cyanDark)
                        )
                    )
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
private fun FeatureCheckRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFFE2E8F0),
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
    val cyanAccent = Color(0xFF00B4D8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AI Provider Select",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "உங்களுக்கு பிடித்த AI ஐத் தேர்ந்தெடுக்கவும்",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ChatGPT Selector
        ProviderRadioCard(
            title = "ChatGPT",
            subtitle = "உலகின் சக்திவாய்ந்த AI உதவியாளர்",
            isSelected = selectedProvider == AiProvider.CHATGPT,
            icon = Icons.Default.ChatBubble,
            accentColor = Color(0xFF10B981),
            onClick = { onSelectProvider(AiProvider.CHATGPT) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gemini Selector
        ProviderRadioCard(
            title = "Gemini",
            subtitle = "Google இன் AI உதவியாளர்",
            isSelected = selectedProvider == AiProvider.GEMINI,
            icon = Icons.Default.AutoAwesome,
            accentColor = cyanAccent,
            onClick = { onSelectProvider(AiProvider.GEMINI) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Why choose card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B172B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "உங்கள் AI-யை ஏன் தேர்வு செய்ய வேண்டும்?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE68A)
                )
                Spacer(modifier = Modifier.height(10.dp))
                FeatureCheckRow("உடனடி மற்றும் துல்லியமான பதில்")
                Spacer(modifier = Modifier.height(6.dp))
                FeatureCheckRow("தமிழ், English மற்றும் கலவை மொழி ஆதரவு")
                Spacer(modifier = Modifier.height(6.dp))
                FeatureCheckRow("குரல் மூலம் இயல்பான உரையாடல்")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
            Text("தொடரவும் (Continue) ->", color = Color.White, fontWeight = FontWeight.Bold)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accentColor else Color.White.copy(alpha = 0.15f)),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C192E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 12.sp, color = Color.LightGray)
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
    val cyanAccent = Color(0xFF00B4D8)
    var showOpenAiKey by remember { mutableStateOf(false) }
    var showGeminiKey by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Voice Chat Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 1. மொழித் தேர்வு
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C192E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "மொழித் தேர்வு (Language)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = cyanAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LanguageRadioRow("தமிழ் (Tamil)", preferredLanguage == "ta") { onLanguageChange("ta") }
                    LanguageRadioRow("English", preferredLanguage == "en") { onLanguageChange("en") }
                    LanguageRadioRow("தமிழ் + English (Mixed)", preferredLanguage == "mixed") { onLanguageChange("mixed") }
                }
            }
        }

        // 2. AI பதில் குரல் & தானியங்கி Play
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C192E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AI பதில் குரல்", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("AI குரலில் பதில் பேசும்", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(checked = voiceReply, onCheckedChange = onVoiceReplyChange)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("தானியங்கி Play (Auto Play)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("AI பதிலை தானாகவே கேட்கவும்", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(checked = autoPlayVoice, onCheckedChange = onAutoPlayChange)
                    }
                }
            }
        }

        // 3. குரல் வேகம்
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C192E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("குரல் வேகம் (Voice Speed)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cyanAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C192E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Secret AI Keys (உங்கள் சாவிகள்)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE68A))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "சாவிகள் உங்கள் மொபைல் சாதனத்தில் மட்டுமே பாதுகாப்பாகச் சேமிக்கப்படும்.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // OpenAI Key
                    Text("OPENAI_API_KEY (ChatGPT)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    OutlinedTextField(
                        value = openAiKey,
                        onValueChange = onOpenAiKeyChange,
                        placeholder = { Text("sk-proj-...") },
                        visualTransformation = if (showOpenAiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showOpenAiKey = !showOpenAiKey }) {
                                Icon(if (showOpenAiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gemini Key
                    Text("GEMINI_API_KEY (Google AI)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = onGeminiKeyChange,
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                Icon(if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("சேமி (Save Settings)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LanguageRadioRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun RowScope.SpeedButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.LightGray
            )
        }
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
            .padding(16.dp),
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Surface(
                color = Color(0xFF064E3B).copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${provider.displayName} ● Online",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                            Color(0xFF00B4D8).copy(alpha = 0.4f),
                            Color(0xFF0077B6).copy(alpha = 0.15f),
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
                    .background(Color(0xFF0284C7))
                    .shadow(16.dp, CircleShape),
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
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "தயவுசெய்து பேசுங்கள்...",
            fontSize = 14.sp,
            color = Color(0xFF90E0EF)
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
                        .background(Color(0xFF00B4D8))
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
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444))
            }

            IconButton(
                onClick = onSpeakAgain,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7))
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Speak Again", tint = Color.White, modifier = Modifier.size(28.dp))
            }

            IconButton(
                onClick = onTextInput,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Keyboard, contentDescription = "Text Input", tint = Color.White)
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
    val cyanAccent = Color(0xFF00B4D8)

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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                color = Color(0xFF064E3B).copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${provider.displayName} ● Online",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.sender == "user") {
                    // User Speech Bubble (Right aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            color = Color(0xFF0284C7),
                            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "You • ${msg.timestamp}",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    // AI Response Bubble (Left aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (provider == AiProvider.GEMINI) cyanAccent else Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (provider == AiProvider.GEMINI) Icons.Default.AutoAwesome else Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = Color(0xFF0B172B),
                            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 14.sp,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 21.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Audio Player Bar matching Screenshot 5 & 6
                                Surface(
                                    color = Color(0xFF030712),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isPlaying = currentlyPlayingId == msg.id

                                        IconButton(
                                            onClick = {
                                                if (isPlaying) onStopAudio() else onPlayAudio(msg.text, msg.id)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play/Pause",
                                                tint = cyanAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Text(
                                            text = if (isPlaying) "0:10 / 0:15" else "0:00 / 0:15",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = null,
                                            tint = cyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${provider.displayName} • ${msg.timestamp}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
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
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = cyanAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${provider.displayName} பதில் தயாரிக்கிறது...",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // Bottom Action Toolbar matching Collage (மைக், நிறுத்து, மீண்டும் கேள், உரை)
        Surface(
            color = Color(0xFF070F1E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onMicClick,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.White)
                    }
                    Text("மைக்", fontSize = 10.sp, color = Color.White)
                }

                // Stop
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onStopSession,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF4444))
                    }
                    Text("நிறுத்து", fontSize = 10.sp, color = Color.LightGray)
                }

                // Replay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            val lastAi = messages.lastOrNull { it.sender == "ai" }
                            if (lastAi != null) onPlayAudio(lastAi.text, lastAi.id)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Replay", tint = Color.White)
                    }
                    Text("மீண்டும் கேள்", fontSize = 10.sp, color = Color.LightGray)
                }

                // Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onTextInput,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = "Text", tint = Color.White)
                    }
                    Text("உரை", fontSize = 10.sp, color = Color.LightGray)
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(0.8f))

        // Checkmark Icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFF064E3B).copy(alpha = 0.4f))
                .border(BorderStroke(2.dp, Color(0xFF10B981)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Stopped",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "பதிவு நிறுத்தப்பட்டது",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "மீண்டும் பேச விரும்பினால் மைக் பொத்தானை அழுத்தவும்.",
            fontSize = 13.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("மீண்டும் தொடங்கவும் (Start Again)", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onViewChat,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("உரையாடலைப் பார்க்க (View Chat)", color = Color(0xFF00B4D8))
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Voice Chat History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { historyTab = AiProvider.CHATGPT },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (historyTab == AiProvider.CHATGPT) Color(0xFF0284C7) else Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("ChatGPT")
            }

            Button(
                onClick = { historyTab = AiProvider.GEMINI },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (historyTab == AiProvider.GEMINI) Color(0xFF0284C7) else Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Gemini")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val list = if (historyTab == AiProvider.CHATGPT) chatGptHistory else geminiHistory
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list) { (title, date) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectHistory(title) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B172B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = date, fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
