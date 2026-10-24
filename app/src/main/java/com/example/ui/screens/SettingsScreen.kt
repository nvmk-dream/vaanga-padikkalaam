package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ApiKeyManager
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager(context) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var hasValidKey by remember { mutableStateOf(apiKeyManager.hasValidKey()) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isTamilNotificationEnabled by remember { mutableStateOf(true) }

    if (showKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showKeyDialog = false },
            onKeySaved = {
                hasValidKey = apiKeyManager.hasValidKey()
            }
        )
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "அமைப்புகள் / Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = "செயலி மற்றும் AI இணைப்பு விருப்பங்களை மாற்றவும்.",
                    fontSize = 13.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Gemini API Key Section
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { showKeyDialog = true }
                        .testTag("settings_api_key_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SubikshaPastelPurple.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = SubikshaRoyalPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gemini AI API Key",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                            Text(
                                text = if (hasValidKey) "இணைக்கப்பட்டுள்ளது (Configured)" else "இணைக்கப்படவில்லை (Tap to configure)",
                                fontSize = 12.sp,
                                color = if (hasValidKey) SubikshaMintGreen else SubikshaPink
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SubikshaRoyalPurple
                        )
                    }
                }
            }

            // Preferences
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "பொதுவான அமைப்புகள் (Preferences)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ஒலி விளைவுகள் (Sound Effects)", fontSize = 13.sp, color = SubikshaNavy)
                            Switch(
                                checked = isSoundEnabled,
                                onCheckedChange = { isSoundEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = SubikshaRoyalPurple)
                            )
                        }

                        HorizontalDivider(color = SubikshaCardBorder, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("தினசரி கற்றல் அறிவிப்புகள் (Daily Tips)", fontSize = 13.sp, color = SubikshaNavy)
                            Switch(
                                checked = isTamilNotificationEnabled,
                                onCheckedChange = { isTamilNotificationEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = SubikshaRoyalPurple)
                            )
                        }
                    }
                }
            }

            // About App Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SubikshaRoyalPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "செயலி பற்றிய தகவல்கள் (About)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "subiksha teacher",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic,
                                color = SubikshaNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  வாங்க படிக்கலாம்",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = SubikshaRoyalPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Repository: https://github.com/nvmk-dream/vaanga-padikkalaam\nVersion: 1.0 (Release Build Ready)\nPowered by Google Gemini AI Model",
                            fontSize = 11.sp,
                            color = SubikshaTextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
