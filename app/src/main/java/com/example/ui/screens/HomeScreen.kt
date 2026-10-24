package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SubikshaBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Greeting Card
        item {
            HeroBanner(onGeminiClick = { onNavigate("gemini_chat") })
        }

        // Section Title: Feature Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "கற்றல் சேவைகள் / Services",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = "6 அம்சங்கள்",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubikshaRoyalPurple
                )
            }
        }

        // Feature Grid Row 1: Gemini Chat & Image to Text
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Gemini Chat",
                    tamilTitle = "ஏஐ ஆசிரியர்",
                    badge = "AI Powered 💕",
                    desc = "பாடச் சந்தேகங்கள் & உரையாடல்",
                    icon = Icons.Default.AutoAwesome,
                    gradient = listOf(SubikshaRoyalPurple, SubikshaPastelPurple),
                    testTag = "card_gemini_chat",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("gemini_chat") }
                )

                FeatureCard(
                    title = "Image to Text",
                    tamilTitle = "பட உரை மாற்றி",
                    badge = "OCR Tool",
                    desc = "புத்தக பக்கங்களை வாசிக்க",
                    icon = Icons.Default.DocumentScanner,
                    gradient = listOf(SubikshaMintGreen, Color(0xFF6EE7B7)),
                    testTag = "card_image_to_text",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("image_to_text") }
                )
            }
        }

        // Feature Grid Row 2: Voice Chat & Spoken English
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Voice Chat",
                    tamilTitle = "குரல் உரையாடல்",
                    badge = "Audio AI",
                    desc = "பேசிப் பழகி கற்கலாம்",
                    icon = Icons.Default.Mic,
                    gradient = listOf(SubikshaSkyBlue, Color(0xFF93C5FD)),
                    testTag = "card_voice_chat",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("voice_chat") }
                )

                FeatureCard(
                    title = "Spoken English",
                    tamilTitle = "ஆங்கிலப் பயிற்சி",
                    badge = "Daily Talk",
                    desc = "தினசரி எளிய உரையாடல்கள்",
                    icon = Icons.Default.RecordVoiceOver,
                    gradient = listOf(SubikshaWarmOrange, Color(0xFFFDE68A)),
                    testTag = "card_spoken_english",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("spoken_english") }
                )
            }
        }

        // Feature Grid Row 3: Mental Maths & More Features
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Mental Maths",
                    tamilTitle = "மனக்கணக்கு",
                    badge = "Speed Quiz",
                    desc = "வேகக் கணித சவால்கள்",
                    icon = Icons.Default.Calculate,
                    gradient = listOf(SubikshaPink, SubikshaLightPink),
                    testTag = "card_mental_maths",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("mental_maths") }
                )

                FeatureCard(
                    title = "More Features",
                    tamilTitle = "மேலும் பல",
                    badge = "Library",
                    desc = "திருக்குறள் & பொது அறிவு",
                    icon = Icons.Default.Widgets,
                    gradient = listOf(Color(0xFF6366F1), Color(0xFFA5B4FC)),
                    testTag = "card_more_features",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("more_features") }
                )
            }
        }

        // Recent Activity Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "சமீபத்திய பயிற்சி / Recent Activity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                TextButton(onClick = { onNavigate("explore") }) {
                    Text("அனைத்தும்", fontSize = 12.sp, color = SubikshaRoyalPurple)
                }
            }
        }

        item {
            ActivityCard(
                title = "Mental Maths Speed Sprint",
                subtitle = "பெருக்கல் & கூட்டல் - 95% மதிப்பெண்!",
                time = "10 நிமிடங்கள் முன்",
                icon = Icons.Default.Calculate,
                iconBg = SubikshaPink,
                onClick = { onNavigate("mental_maths") }
            )
        }

        item {
            ActivityCard(
                title = "English Grammar Doubts",
                subtitle = "Present Tense விதிகள் தீர்க்கப்பட்டது",
                time = "நேற்று",
                icon = Icons.Default.AutoAwesome,
                iconBg = SubikshaRoyalPurple,
                onClick = { onNavigate("gemini_chat") }
            )
        }

        item {
            ActivityCard(
                title = "Thirukkural of the Day",
                subtitle = "அகர முதல எழுத்தெல்லாம் ஆதி பகவன் முதற்றே உலகு",
                time = "இன்று காலை",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconBg = SubikshaMintGreen,
                onClick = { onNavigate("explore") }
            )
        }
    }
}

@Composable
fun HeroBanner(
    onGeminiClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(SubikshaRoyalPurple, SubikshaPastelPurple)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "✨ சுபிக்ஷா ஆசிரியர் AI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Smart Tutor",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "வணக்கம் மாணவர்களே!\nஇன்று நாம் என்ன படிக்கலாம்?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onGeminiClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = SubikshaRoyalPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "சந்தேகம் கேட்க தொடங்குங்கள்",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaRoyalPurple
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    tamilTitle: String,
    badge: String,
    desc: String,
    icon: ImageVector,
    gradient: List<Color>,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Surface(
                    color = gradient[0].copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = gradient[0],
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SubikshaNavy
            )

            Text(
                text = tamilTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubikshaRoyalPurple
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                fontSize = 11.sp,
                color = SubikshaTextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun ActivityCard(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SubikshaTextMuted,
                    maxLines = 1
                )
            }

            Text(
                text = time,
                fontSize = 10.sp,
                color = SubikshaTextMuted
            )
        }
    }
}
