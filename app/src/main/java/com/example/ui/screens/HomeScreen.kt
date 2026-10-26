package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

data class HomeFeatureItem(
    val id: String,
    val title: String,
    val englishSubtitle: String,
    val description: String,
    val icon: ImageVector,
    val drawableRes: Int?,
    val badge: String,
    val accentColor: Color
)

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val features = listOf(
        HomeFeatureItem(
            id = "gemini_chat",
            title = "சுபிக்ஷா AI ஆசிரியர்",
            englishSubtitle = "Gemini AI Live Chat & Voice",
            description = "பாடம், அறிவியல், சந்தேகம் எதுவானாலும் தமிழில் உடனடி பதில் மற்றும் பெண் குரல் வாசிப்பு.",
            icon = Icons.Default.AutoAwesome,
            drawableRes = R.drawable.card_gemini_chat,
            badge = "LIVE AI",
            accentColor = SubikshaRoyalPurple
        ),
        HomeFeatureItem(
            id = "mental_maths",
            title = "மனக்கணக்கு பயிற்சி",
            englishSubtitle = "Mental Maths (500 எளிய வழிகள்)",
            description = "கூட்டல், கழித்தல், பெருக்கல், வகுத்தல் - தமிழ் பெண் குரல் ஆசிரியை வழிகாட்டும் நேரடி PDF.",
            icon = Icons.Default.Calculate,
            drawableRes = null,
            badge = "500 TRICKS",
            accentColor = SubikshaPink
        ),
        HomeFeatureItem(
            id = "spoken_english",
            title = "ஆங்கிலம் பேசுவோம்",
            englishSubtitle = "Spoken English Daily Practice",
            description = "தினசரி உரையாடல் வாக்கியங்களை தமிழில் எளிதாகப் புரிந்து சரளமாகப் பேசும் பயிற்சி.",
            icon = Icons.Default.RecordVoiceOver,
            drawableRes = R.drawable.card_spoken_english,
            badge = "SPOKEN",
            accentColor = SubikshaWarmOrange
        ),
        HomeFeatureItem(
            id = "image_to_text",
            title = "பட உரை அறிதல்",
            englishSubtitle = "Image to Text & OCR",
            description = "புத்தக பக்கத்தின் புகைப்படத்தை உரையாக மாற்றி AI மூலம் எளிய விளக்கத்தைப் பெறுங்கள்.",
            icon = Icons.Default.DocumentScanner,
            drawableRes = R.drawable.card_image_to_text,
            badge = "SMART OCR",
            accentColor = SubikshaMintGreen
        ),
        HomeFeatureItem(
            id = "voice_chat",
            title = "குரல் உரையாடல்",
            englishSubtitle = "Voice Chat with Teacher",
            description = "நேரடியாக பேசிப் பழகலாம், சுபிக்ஷா ஆசிரியை உங்களுக்குப் பதிலளிப்பார்.",
            icon = Icons.Default.Mic,
            drawableRes = R.drawable.card_voice_chat,
            badge = "VOICE",
            accentColor = SubikshaSkyBlue
        ),
        HomeFeatureItem(
            id = "explore",
            title = "அறிவுப் பெட்டகம்",
            englishSubtitle = "Thirukkural, Proverbs & Science",
            description = "தினசரி திருக்குறள் உரை, அருமையான தமிழ் பழமொழிகள் மற்றும் வியக்க வைக்கும் அறிவியல் தகவல்கள்.",
            icon = Icons.Default.Explore,
            drawableRes = R.drawable.card_more_features,
            badge = "KNOWLEDGE",
            accentColor = SubikshaNavy
        )
    )

    Scaffold(
        topBar = {
            AppHeader(
                showBackButton = false,
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SubikshaBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Welcome Banner
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(SubikshaNavy, SubikshaRoyalPurple)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "வாங்க படிக்கலாம் • Ultra Deluxe",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "வணக்கம் மாணவர்களே! 🌸",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "நான் உங்கள் சுபிக்ஷா ஆசிரியை. கணிதம், அறிவியல், ஆங்கிலம் என எதையும் சுலபமாகக் கற்றுக்கொள்ளலாம் வாருங்கள்!",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF1F5F9),
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { onNavigate("gemini_chat") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("home_hero_start_button")
                                ) {
                                    Text(
                                        text = "ஆசிரியரிடம் பேச 💬",
                                        color = SubikshaRoyalPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Realistic Teacher Avatar
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.5.dp, SubikshaPastelPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.teacher_avatar),
                                    contentDescription = "Teacher Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "கற்றல் பிரிவுகள் / Learning Hub",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Text(
                        text = "அனைத்தும் இலவசம்",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SubikshaRoyalPurple
                    )
                }
            }

            // Feature Cards
            items(features.size) { index ->
                val item = features[index]
                HomeFeatureCard(item = item, onClick = { onNavigate(item.id) })
            }
        }
    }
}

@Composable
fun HomeFeatureCard(
    item: HomeFeatureItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("home_card_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // If realistic drawable image is present, show banner image
            if (item.drawableRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = item.drawableRes),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Badge overlay
                    Surface(
                        color = item.accentColor,
                        shape = RoundedCornerShape(bottomStart = 12.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = item.badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(item.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )
                        if (item.drawableRes == null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = item.accentColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.badge,
                                    color = item.accentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = item.englishSubtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SubikshaRoyalPurple
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = SubikshaTextMuted,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SubikshaRoyalPurple
                )
            }
        }
    }
}
