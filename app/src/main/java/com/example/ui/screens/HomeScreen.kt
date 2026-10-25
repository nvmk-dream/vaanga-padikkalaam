package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SubikshaBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. HEADER (Reproducing the exact large pastel header from Reference Image 1)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // Header Banner Image
                Image(
                    painter = painterResource(id = R.drawable.header_banner),
                    contentDescription = "subiksha teacher header",
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    contentScale = ContentScale.FillWidth
                )

                // Top Interactive Buttons: Hamburger Menu (Left) and Profile (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Transparent hit target over Hamburger Menu Icon
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable { onMenuClick() }
                            .testTag("hamburger_menu_button")
                    )

                    // Transparent hit target over Teacher/Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick() }
                            .testTag("header_profile_button")
                    )
                }
            }
        }

        // 2. WELCOME BACK SECTION (Matching Reference Image 1)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👋",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = SubikshaNavy,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("Welcome back, ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = SubikshaRoyalPurple,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                ) {
                                    append("Sarah!")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pill 1: Last accessed: Spoken English Book
                        // Pill 1: Last accessed (empty placeholder)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SubikshaSurfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SubikshaMintGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Last accessed: Spoken English Book",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SubikshaNavy
                                )
                            }
                        }

                        // Pill 2: New chat started (Opens Gemini Chat)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SubikshaSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onNavigate("gemini_chat") }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "💬",
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "New chat started",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SubikshaNavy
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. TWO-COLUMN GRID OF REALISTIC FEATURE CARDS
        // Gemini Chat is fully functional; other 5 cards remain visual-only (empty of navigation)
        // Row 1: Gemini Chat (Active) & Image to Text (Empty)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExactFeatureCard(
                    imageResId = R.drawable.card_gemini_chat,
                    contentDescription = "Gemini Chat",
                    testTag = "card_gemini_chat",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("gemini_chat") }
                )

                ExactFeatureCard(
                    imageResId = R.drawable.card_image_to_text,
                    contentDescription = "Image to Text",
                    testTag = "card_image_to_text",
                    modifier = Modifier.weight(1f),
                    onClick = { /* Empty: Feature to be added when requested */ }
                )
            }
        }

        // Row 2: Voice Chat (Empty) & Spoken English (Empty)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExactFeatureCard(
                    imageResId = R.drawable.card_voice_chat,
                    contentDescription = "Voice Chat",
                    testTag = "card_voice_chat",
                    modifier = Modifier.weight(1f),
                    onClick = { /* Empty: Feature to be added when requested */ }
                )

                ExactFeatureCard(
                    imageResId = R.drawable.card_spoken_english,
                    contentDescription = "Spoken English",
                    testTag = "card_spoken_english",
                    modifier = Modifier.weight(1f),
                    onClick = { /* Empty: Feature to be added when requested */ }
                )
            }
        }

        // Row 3: Mental Maths (Empty) & More Features (Empty)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExactFeatureCard(
                    imageResId = R.drawable.card_mental_maths,
                    contentDescription = "Mental Maths",
                    testTag = "card_mental_maths",
                    modifier = Modifier.weight(1f),
                    onClick = { /* Empty: Feature to be added when requested */ }
                )

                ExactFeatureCard(
                    imageResId = R.drawable.card_more_features,
                    contentDescription = "More Features",
                    testTag = "card_more_features",
                    modifier = Modifier.weight(1f),
                    onClick = { /* Empty: Feature to be added when requested */ }
                )
            }
        }
    }
}

@Composable
fun ExactFeatureCard(
    imageResId: Int,
    contentDescription: String,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
    }
}
