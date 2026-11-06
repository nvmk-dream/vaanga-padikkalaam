package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
    onKeySaved: () -> Unit
) {
    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager(context) }
    var keyText by remember { mutableStateOf(apiKeyManager.getUserCustomKey()) }
    var passwordVisible by remember { mutableStateOf(false) }
    val isCustomKey = apiKeyManager.isCustomKey()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SubikshaPastelPurple.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Key Icon",
                        tint = SubikshaRoyalPurple,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Gemini AI Key அமைப்புகள்",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )

                Text(
                    text = if (isCustomKey) {
                        "தற்போது நீங்கள் உள்ளிட்ட தனிப்பட்ட Gemini API Key வெற்றிகரமாக இயங்குகிறது."
                    } else if (apiKeyManager.hasValidKey()) {
                        "பயன்பாட்டில் Build Key இயங்குகிறது. விருப்பப்பட்டால் உங்கள் சொந்த Google AI Studio Key-ஐ இங்கே மாற்றலாம்."
                    } else {
                        "சுபிக்ஷா AI ஆசிரியர் படத்தை துல்லியமாக வாசிக்கவும், வினாக்களுக்கு விடையளிக்கவும் Google AI Studio API Key-ஐ இங்கே உள்ளிடவும்."
                    },
                    fontSize = 12.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide key" else "Show key",
                                tint = SubikshaTextMuted
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isCustomKey) {
                    OutlinedButton(
                        onClick = {
                            apiKeyManager.rollbackToDefaultApiKey()
                            keyText = ""
                            onKeySaved()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Rollback",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rollback: Default Key-க்கு மாற்று")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ரத்து செய்", color = SubikshaNavy)
                    }

                    Button(
                        onClick = {
                            if (keyText.isNotBlank()) {
                                apiKeyManager.saveApiKey(keyText)
                            } else {
                                apiKeyManager.rollbackToDefaultApiKey()
                            }
                            onKeySaved()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_api_key_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("சேமி", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
