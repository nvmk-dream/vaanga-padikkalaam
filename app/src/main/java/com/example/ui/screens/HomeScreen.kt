package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ApiKeyManager
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onKeyClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager(context) }
    var customProfileUriString by remember { mutableStateOf(apiKeyManager.getCustomProfileUri()) }
    var showProfileOptionsDialog by remember { mutableStateOf(false) }

    // Photo picker launcher for custom avatar upload
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Ignore if takePersistableUriPermission is not supported
                }
                val uriStr = uri.toString()
                apiKeyManager.saveCustomProfileUri(uriStr)
                customProfileUriString = uriStr
            } catch (_: Exception) {
                val uriStr = uri.toString()
                apiKeyManager.saveCustomProfileUri(uriStr)
                customProfileUriString = uriStr
            }
        }
    }

    // Decode custom profile image if set
    val customBitmap = remember(customProfileUriString) {
        customProfileUriString?.let { uriStr ->
            try {
                context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    // Profile photo options dialog
    if (showProfileOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showProfileOptionsDialog = false },
            title = {
                Text(
                    text = "சுயவிவரப் படம்",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SubikshaNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Header Banner-ல் உள்ள Profile படத்தை உங்கள் மொபைல் Gallery-யில் இருந்து மாற்றலாம்:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )

                    Button(
                        onClick = {
                            showProfileOptionsDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery-யில் இருந்து படம் தேர்வு செய்")
                    }

                    OutlinedButton(
                        onClick = {
                            showProfileOptionsDialog = false
                            onProfileClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("சுயவிவர பக்கம் செல்ல")
                    }

                    if (customProfileUriString != null) {
                        TextButton(
                            onClick = {
                                apiKeyManager.saveCustomProfileUri(null)
                                customProfileUriString = null
                                showProfileOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("இயல்புநிலை படத்திற்கு மாற்று", color = Color(0xFFDC2626))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showProfileOptionsDialog = false }) {
                    Text("மூடு", color = SubikshaNavy)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Layer 1: Background wallpaper with soft stars and hearts
        Image(
            painter = painterResource(id = R.drawable.bg_home),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay to ensure contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color(0xFFFFF0F5).copy(alpha = 0.20f)
                        )
                    )
                )
        )

        // Layer 2: Clean, compact Zero-Scroll Column with strictly controlled spacing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 6.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // 1. Header Banner with integrated subiksha banner and custom gallery avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.header_banner_subiksha),
                    contentDescription = "Subiksha Teacher Header Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                // Hamburger Menu Button (overlay on left)
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                        .size(44.dp)
                        .testTag("hamburger_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = SubikshaNavy,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Profile Avatar Container (overlay on right avatar location)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Profile Image: Custom photo if selected, otherwise Default Teacher Avatar
                    if (customBitmap != null) {
                        Image(
                            bitmap = customBitmap.asImageBitmap(),
                            contentDescription = "Custom Profile Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { showProfileOptionsDialog = true }
                                .testTag("header_profile_button"),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.teacher_avatar),
                            contentDescription = "Default Teacher Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { showProfileOptionsDialog = true }
                                .testTag("header_profile_button"),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Online green indicator dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-3).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(1.5.dp, Color.White, CircleShape)
                    )

                    // Direct 1-tap Camera/Gallery upload badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(SubikshaRoyalPurple)
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Compact Welcome Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👋",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Welcome back, ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaNavy
                        )
                        Text(
                            text = "Sarah!",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubikshaRoyalPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left pill: Last accessed
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("spoken_english") }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Last accessed: Spoken English",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1
                                )
                            }
                        }

                        // Right pill: New chat started
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.clickable { onNavigate("gemini_chat") }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = SubikshaNavy,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "New chat started",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Grid of 6 Feature Cards (3 rows x 2 columns)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Gemini Chat (100 Easy Additional Steps) & Image to Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeGridCard(
                        drawableRes = R.drawable.card_gemini_chat,
                        title = "100 Easy Additional Steps",
                        description = "Speed addition tricks & Vedic math steps",
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Bottom Student Image Card (Fills all remaining empty space below 6 cards seamlessly)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kids_reading_tight),
                    contentDescription = "Students Learning together with Tablet",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .aspectRatio(2.35f)
            .clip(RoundedCornerShape(16.dp))
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
