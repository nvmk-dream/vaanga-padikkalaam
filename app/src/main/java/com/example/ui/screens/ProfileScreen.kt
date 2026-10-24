package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppHeader(
                showBackButton = false,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SubikshaBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(SubikshaRoyalPurple, SubikshaPink)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ST",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "சுபிக்ஷா மாணவர் (Student)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )

                        Text(
                            text = "வகுப்பு: 8 - 12 (Higher Secondary)",
                            fontSize = 12.sp,
                            color = SubikshaTextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("850", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SubikshaRoyalPurple)
                                Text("கற்றல் புள்ளிகள் (XP)", fontSize = 11.sp, color = SubikshaTextMuted)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("12 நாட்கள்", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SubikshaWarmOrange)
                                Text("தொடர் வருகை (Streak)", fontSize = 11.sp, color = SubikshaTextMuted)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("48", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SubikshaMintGreen)
                                Text("கேள்விகள்", fontSize = 11.sp, color = SubikshaTextMuted)
                            }
                        }
                    }
                }
            }

            // Badges Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "சான்றிதழ் & பதக்கங்கள் / Badges",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubikshaNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BadgeItem(
                            title = "கணித வீரன்",
                            sub = "Math Master",
                            icon = Icons.Default.Calculate,
                            color = SubikshaPink,
                            modifier = Modifier.weight(1f)
                        )
                        BadgeItem(
                            title = "ஆங்கிலச் சுடர்",
                            sub = "English Star",
                            icon = Icons.Default.RecordVoiceOver,
                            color = SubikshaWarmOrange,
                            modifier = Modifier.weight(1f)
                        )
                        BadgeItem(
                            title = "தமிழ் நயம்",
                            sub = "Tamil Scholar",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            color = SubikshaMintGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(
    title: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SubikshaNavy
            )
            Text(
                text = sub,
                fontSize = 10.sp,
                color = SubikshaTextMuted
            )
        }
    }
}
