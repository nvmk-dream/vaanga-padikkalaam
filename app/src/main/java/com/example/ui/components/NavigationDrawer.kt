package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SubikshaDrawerContent(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onOpenApiKeyDialog: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SubikshaRoyalPurple, SubikshaPastelPurple.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ST",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SubikshaRoyalPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "subiksha teacher",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = SubikshaLightPink,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "வாங்க படிக்கலாம்",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Yellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ultra Deluxe Learning AI Edition",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Highlighted Option: Configure Gemini API Key
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        onCloseDrawer()
                        onOpenApiKeyDialog()
                    }
                    .testTag("drawer_gemini_key_button"),
                color = SubikshaPastelPurple.copy(alpha = 0.25f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SubikshaRoyalPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Key",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gemini API Key ADD",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )
                        Text(
                            text = "AI Key அமைத்தல் / மாற்றுதல்",
                            fontSize = 11.sp,
                            color = SubikshaTextMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SubikshaRoyalPurple
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = SubikshaCardBorder
            )

            // Section: Main Navigation
            Text(
                text = "பக்கங்கள் / NAVIGATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SubikshaTextMuted,
                modifier = Modifier.padding(start = 24.dp, top = 6.dp, bottom = 6.dp)
            )

            DrawerNavItem(
                title = "Home / முதன்மைப் பக்கம்",
                icon = Icons.Default.Home,
                selected = currentScreen == "home",
                onClick = {
                    onNavigate("home")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                title = "Explore / புதியவை கற்க",
                icon = Icons.Default.Explore,
                selected = currentScreen == "explore",
                onClick = {
                    onNavigate("explore")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                title = "Profile / என் சுயவிவரம்",
                icon = Icons.Default.Person,
                selected = currentScreen == "profile",
                onClick = {
                    onNavigate("profile")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                title = "Settings / அமைப்புகள்",
                icon = Icons.Default.Settings,
                selected = currentScreen == "settings",
                onClick = {
                    onNavigate("settings")
                    onCloseDrawer()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = SubikshaCardBorder
            )

            // Section: AI & Learning Modules
            Text(
                text = "கற்றல் அம்சங்கள் / MODULES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SubikshaTextMuted,
                modifier = Modifier.padding(start = 24.dp, top = 6.dp, bottom = 6.dp)
            )

            DrawerNavItem(
                title = "Gemini AI Chat",
                icon = Icons.Default.AutoAwesome,
                accentColor = SubikshaRoyalPurple,
                selected = currentScreen == "gemini_chat",
                onClick = {
                    onNavigate("gemini_chat")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                title = "Image to Text",
                icon = Icons.Default.DocumentScanner,
                accentColor = SubikshaMintGreen,
                selected = currentScreen == "image_to_text",
                onClick = {
                    onNavigate("image_to_text")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                title = "Voice Chat",
                icon = Icons.Default.Mic,
                accentColor = SubikshaSkyBlue,
                selected = currentScreen == "voice_chat",
                onClick = {
                    onNavigate("voice_chat")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                title = "Spoken English",
                icon = Icons.Default.RecordVoiceOver,
                accentColor = SubikshaWarmOrange,
                selected = currentScreen == "spoken_english",
                onClick = {
                    onNavigate("spoken_english")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                title = "Mental Maths",
                icon = Icons.Default.Calculate,
                accentColor = SubikshaPink,
                selected = currentScreen == "mental_maths",
                onClick = {
                    onNavigate("mental_maths")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                title = "More Features",
                icon = Icons.Default.Widgets,
                accentColor = SubikshaPastelPurple,
                selected = currentScreen == "more_features",
                onClick = {
                    onNavigate("more_features")
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DrawerNavItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color = SubikshaRoyalPurple,
    onClick: () -> Unit
) {
    val bg = if (selected) SubikshaSurfaceVariant else Color.Transparent
    val tint = if (selected) accentColor else SubikshaNavy

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = tint
            )
        }
    }
}
