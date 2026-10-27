package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onKeyClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SubikshaBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header Banner with status bar padding and interactive hamburger / profile targets
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.header_banner),
                        contentDescription = "Subiksha Teacher Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp),
                        contentScale = ContentScale.FillWidth
                    )

                    // Hamburger Menu button overlay (aligned with left circular button on banner)
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .padding(start = 8.dp, top = 4.dp)
                            .size(46.dp)
                            .testTag("hamburger_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = SubikshaNavy,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Profile Avatar clickable overlay (aligned with teacher avatar on top right of banner)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp, top = 4.dp)
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick() }
                            .testTag("header_profile_button")
                    )
                }
            }

            // Welcome Card matching Image 2
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "👋",
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Welcome back, ",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                            Text(
                                text = "Sarah!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaRoyalPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left pill: Last accessed
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate("spoken_english") }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Last accessed: Spoken English Book",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E293B),
                                        maxLines = 1
                                    )
                                }
                            }

                            // Right pill: New chat started
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { onNavigate("gemini_chat") }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = SubikshaNavy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "New chat",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B),
                                            lineHeight = 11.sp
                                        )
                                        Text(
                                            text = "started",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B),
                                            lineHeight = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2-Column Grid matching Image 2 (3 rows x 2 cards)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: Gemini Chat & Image to Text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeGridCard(
                            drawableRes = R.drawable.card_gemini_chat,
                            title = "Gemini Chat",
                            description = "Ask questions & get instant help",
                            testTag = "home_card_gemini_chat",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("gemini_chat") }
                        )
                        HomeGridCard(
                            drawableRes = R.drawable.card_image_to_text,
                            title = "Image to Text",
                            description = "Scan notes & extract text",
                            testTag = "home_card_image_to_text",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("image_to_text") }
                        )
                    }

                    // Row 2: Voice Chat & Spoken English
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeGridCard(
                            drawableRes = R.drawable.card_voice_chat,
                            title = "Voice Chat",
                            description = "Practice speaking & listen to feedback",
                            testTag = "home_card_voice_chat",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("voice_chat") }
                        )
                        HomeGridCard(
                            drawableRes = R.drawable.card_spoken_english,
                            title = "Spoken English",
                            description = "Access interactive grammar & vocab",
                            testTag = "home_card_spoken_english",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("spoken_english") }
                        )
                    }

                    // Row 3: Mental Maths & More Features (My Library)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeGridCard(
                            drawableRes = R.drawable.card_mental_maths,
                            title = "Mental Maths",
                            description = "Practice arithmetic & calculations",
                            testTag = "home_card_mental_maths",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("mental_maths") }
                        )
                        HomeGridCard(
                            drawableRes = R.drawable.card_more_features,
                            title = "More Features",
                            description = "Explore library & resources",
                            testTag = "home_card_my_library",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate("my_library") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeGridCard(
    drawableRes: Int,
    title: String,
    description: String,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .aspectRatio(1.85f)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "$title - $description",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}
