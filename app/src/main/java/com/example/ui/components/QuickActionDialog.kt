package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun QuickActionDialog(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "விரைவுச் செயல்கள் / Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = "உடனே தொடங்க விரும்பும் செயலைத் தேர்ந்தெடுக்கவும்",
                    fontSize = 12.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                QuickActionItem(
                    title = "ஆசிரியரிடம் கேள்வி கேட்க (Gemini AI)",
                    desc = "எந்த பாடச் சந்தேகத்தையும் தமிழில் கேட்கலாம்",
                    icon = Icons.Default.AutoAwesome,
                    color = SubikshaRoyalPurple,
                    onClick = {
                        onDismiss()
                        onNavigate("gemini_chat")
                    }
                )

                QuickActionItem(
                    title = "1-நிமிட வேகக் கணக்கு (Mental Maths)",
                    desc = "உங்கள் மூளைக்கு உடனடி சுறுசுறுப்பு பயிற்சி",
                    icon = Icons.Default.Calculate,
                    color = SubikshaPink,
                    onClick = {
                        onDismiss()
                        onNavigate("mental_maths")
                    }
                )

                QuickActionItem(
                    title = "ஆங்கில உச்சரிப்புப் பயிற்சி (Spoken English)",
                    desc = "இன்றைய வாக்கியங்களை பேசப் பழகவும்",
                    icon = Icons.Default.RecordVoiceOver,
                    color = SubikshaWarmOrange,
                    onClick = {
                        onDismiss()
                        onNavigate("spoken_english")
                    }
                )

                QuickActionItem(
                    title = "பட உரை அறிதல் (Image to Text)",
                    desc = "புத்தக பக்கத்தின் உரையை மாற்றிப் படிக்க",
                    icon = Icons.Default.DocumentScanner,
                    color = SubikshaMintGreen,
                    onClick = {
                        onDismiss()
                        onNavigate("image_to_text")
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onDismiss) {
                    Text("மூடுக / Close", color = SubikshaTextMuted)
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = SubikshaTextMuted
                )
            }
        }
    }
}
