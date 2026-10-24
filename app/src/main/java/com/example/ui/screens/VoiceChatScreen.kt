package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun VoiceChatScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
    var isListening by remember { mutableStateOf(false) }
    var selectedTopic by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("மைக் பட்டனை அழுத்தி பேச தொடங்குங்கள்") }

    val topics = listOf(
        "சுய அறிமுகம் (Self Introduction)",
        "பள்ளியில் உரையாடல் (At School)",
        "ஆங்கில உச்சரிப்புப் பயிற்சி (Phonics & Accent)",
        "தினசரி பொது உரையாடல் (Daily Conversation)"
    )

    // Pulsing animation for mic
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            AppHeader(
                showBackButton = true,
                onBackClick = onBackClick,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SubikshaBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = "குரல் உரையாடல் / Voice Chat",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = "சுபிக்ஷா ஆசிரியருடன் வாய்மொழியாகப் பேசி தமிழ் மற்றும் ஆங்கிலப் பேச்சுத் திறனை வளர்த்துக் கொள்ளுங்கள்.",
                    fontSize = 13.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Voice Avatar & Visualizer Area
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = if (isListening) listOf(SubikshaPink, SubikshaPastelPurple)
                                        else listOf(SubikshaSkyBlue, SubikshaPastelPurple)
                                    )
                                )
                                .clickable {
                                    isListening = !isListening
                                    statusText = if (isListening) "கேட்கிறேன்... பேசுங்கள்! (Listening...)"
                                    else "நன்றாகப் பேசினீர்கள்! உங்கள் குரல் பதிவு செய்யப்பட்டது."
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isListening) SubikshaPink else SubikshaNavy
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isListening) "பேசி முடித்ததும் மீண்டும் அழுத்தவும்" else "தொடங்க மைக்-ஐ தட்டவும்",
                            fontSize = 12.sp,
                            color = SubikshaTextMuted
                        )
                    }
                }
            }

            // Conversation Topics
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "உரையாடல் தலைப்புகள் / Practice Topics:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    topics.forEachIndexed { index, topic ->
                        val selected = selectedTopic == index
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) SubikshaSkyBlue.copy(alpha = 0.12f) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedTopic = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { selectedTopic = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = SubikshaSkyBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = topic,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = SubikshaNavy
                                )
                            }
                        }
                    }
                }
            }

            // Ask Gemini Button
            item {
                Button(
                    onClick = {
                        onAskGemini("நான் '${topics[selectedTopic]}' பற்றி குரல் பயிற்சி செய்ய விரும்புகிறேன். என்னிடம் 3 எளிய கேள்விகள் கேட்டு திருத்தி வழிகாட்டவும்.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "சுபிக்ஷா ஆசிரியருடன் உரையாடலைத் தொடங்கு",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
