package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ApiKeyManager
import com.example.data.ChatMessage
import com.example.data.GeminiService
import com.example.ui.components.ApiKeyDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GeminiChatScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager(context) }
    val geminiService = remember { GeminiService(apiKeyManager) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showKeyDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasValidKey by remember { mutableStateOf(apiKeyManager.hasValidKey()) }

    // Initial welcome message from Subiksha Teacher
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "வணக்கம்! நான் உங்கள் சுபிக்ஷா ஆசிரியர் (Subiksha Teacher AI) 💕.\n\nதமிழ், ஆங்கிலம், கணிதம், அறிவியல், திருக்குறள் என எந்த சந்தேகமானாலும் கேளுங்கள். நாம் ஒன்றாகக் கற்கலாம்!",
                isUser = false
            )
        )
    }

    val suggestionChips = listOf(
        "ஒளிச்சேர்க்கை பற்றி விளக்குங்கள்",
        "5 எளிய கணித குறுக்கு வழிகள்",
        "Spoken English Daily Sentences",
        "திருக்குறள் - கல்வி அதிகாரம்",
        "சூரிய குடும்பம் (Solar System)"
    )

    fun sendMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isEmpty() || isLoading) return

        if (!apiKeyManager.hasValidKey()) {
            errorMessage = "தயவுசெய்து Gemini API Key-ஐ உள்ளிடவும்."
            showKeyDialog = true
            return
        }

        errorMessage = null
        inputText = ""
        val userMsg = ChatMessage(text = trimmed, isUser = true)
        messages.add(userMsg)

        isLoading = true
        coroutineScope.launch {
            listState.animateScrollToItem(messages.size)
            val result = geminiService.generateResponse(messages)
            isLoading = false
            result.onSuccess { reply ->
                messages.add(ChatMessage(text = reply, isUser = false))
                listState.animateScrollToItem(messages.size)
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "பிழை ஏற்பட்டது. தயவுசெய்து மீண்டும் முயற்சிக்கவும்."
            }
        }
    }

    if (showKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showKeyDialog = false },
            onKeySaved = {
                hasValidKey = apiKeyManager.hasValidKey()
                errorMessage = null
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SubikshaSurfaceVariant)
                            .testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SubikshaNavy
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(SubikshaRoyalPurple, SubikshaPastelPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ST",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "subiksha teacher AI",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                color = SubikshaNavy
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = SubikshaPink,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SubikshaMintGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "இணைப்பில் உள்ளார் (Online Tutor)",
                                fontSize = 11.sp,
                                color = SubikshaTextMuted
                            )
                        }
                    }

                    // Key Config Button
                    IconButton(
                        onClick = { showKeyDialog = true },
                        modifier = Modifier.testTag("chat_key_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Configure Key",
                            tint = if (hasValidKey) SubikshaRoyalPurple else SubikshaPink
                        )
                    }

                    // New Chat Button
                    IconButton(
                        onClick = {
                            messages.clear()
                            messages.add(
                                ChatMessage(
                                    text = "வணக்கம்! புதிய அரட்டை தொடங்கியது. உங்கள் கேள்வியை கேட்கலாம் 💕.",
                                    isUser = false
                                )
                            )
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("chat_new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Chat",
                            tint = SubikshaNavy
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Suggestion Chips (visible when not loading)
                    AnimatedVisibility(visible = messages.size <= 2 && !isLoading) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(suggestionChips) { chip ->
                                Surface(
                                    color = SubikshaPastelPurple.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { sendMessage(chip) }
                                ) {
                                    Text(
                                        text = chip,
                                        fontSize = 12.sp,
                                        color = SubikshaNavy,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Ask me anything... / எதையும் கேளுங்கள்...",
                                    fontSize = 13.sp,
                                    color = SubikshaTextMuted
                                )
                            },
                            maxLines = 4,
                            shape = RoundedCornerShape(22.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SubikshaRoyalPurple,
                                unfocusedBorderColor = SubikshaCardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { sendMessage(inputText) },
                            enabled = inputText.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank() && !isLoading) SubikshaRoyalPurple
                                    else SubikshaCardBorder
                                )
                                .testTag("send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SubikshaBackground)
                .padding(innerPadding)
        ) {
            // Missing API Key Banner
            if (!hasValidKey) {
                Surface(
                    color = SubikshaWarmOrange.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SubikshaWarmOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Key அமைக்கப்படவில்லை. AI பதில் பெற Key சேர்க்கவும்.",
                                fontSize = 12.sp,
                                color = SubikshaNavy
                            )
                        }
                        TextButton(onClick = { showKeyDialog = true }) {
                            Text("Key சேர்க்க", fontWeight = FontWeight.Bold, color = SubikshaRoyalPurple)
                        }
                    }
                }
            }

            // Error banner with retry
            errorMessage?.let { err ->
                Surface(
                    color = SubikshaPink.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = SubikshaNavy,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                val lastUser = messages.lastOrNull { it.isUser }
                                if (lastUser != null) {
                                    sendMessage(lastUser.text)
                                }
                            }
                        ) {
                            Text("மீண்டும் முயல", fontWeight = FontWeight.Bold, color = SubikshaPink)
                        }
                    }
                }
            }

            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }

                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    if (message.isUser) {
        // User message on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(SubikshaRoyalPurple, Color(0xFF9333EA))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    } else {
        // Teacher message on left
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SubikshaPastelPurple.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ST",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaRoyalPurple
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Card(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "சுபிக்ஷா ஆசிரியர்",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaRoyalPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.text,
                        color = SubikshaNavy,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SubikshaPastelPurple.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SubikshaRoyalPurple
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = SubikshaRoyalPurple
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "சுபிக்ஷா ஆசிரியர் யோசிக்கிறார்...",
                    fontSize = 12.sp,
                    color = SubikshaTextMuted,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}
