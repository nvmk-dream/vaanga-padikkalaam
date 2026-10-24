package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ApiKeyManager
import com.example.ui.theme.*

@Composable
fun ApiKeyDialog(
    onDismiss: () -> Unit,
    onKeySaved: (String) -> Unit
) {
    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager(context) }
    var keyInput by remember { mutableStateOf(apiKeyManager.getApiKey()) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SubikshaPastelPurple.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Gemini Key",
                        tint = SubikshaRoyalPurple,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Gemini API Key அமைப்பு",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )

                Text(
                    text = "Subiksha Teacher AI Chat செயல்பட Google Gemini API Key தேவைப்படுகிறது.",
                    fontSize = 13.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (apiKeyManager.hasValidKey()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SubikshaMintGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SubikshaMintGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (apiKeyManager.isCustomKey()) "தனிப்பயன் API Key செயலில் உள்ளது" else "இயல்புநிலை API Key இணைக்கப்பட்டுள்ளது",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubikshaNavy
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        statusMessage = null
                    },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = SubikshaRoyalPurple
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SubikshaRoyalPurple,
                        unfocusedBorderColor = SubikshaCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = SubikshaPink,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (apiKeyManager.isCustomKey()) {
                        OutlinedButton(
                            onClick = {
                                apiKeyManager.clearApiKey()
                                keyInput = ""
                                statusMessage = "Key அழிக்கப்பட்டது"
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear", color = SubikshaPink, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val trimmed = keyInput.trim()
                            if (trimmed.isEmpty()) {
                                statusMessage = "API Key காலியாக இருக்கக்கூடாது"
                            } else {
                                apiKeyManager.saveApiKey(trimmed)
                                onKeySaved(trimmed)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Key", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("மூடுக / Cancel", color = SubikshaTextMuted, fontSize = 13.sp)
                }
            }
        }
    }
}
