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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ImageToTextScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
    var selectedSample by remember { mutableStateOf(0) }
    var extractedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val samples = listOf(
        Pair(
            "அறிவியல் பாடம் (தாவரங்கள்)",
            "தாவரங்கள் ஒளிச்சேர்க்கை முறையில் தங்களுக்குத் தேவையான உணவைத் தாமே தயாரித்துக் கொள்கின்றன. இலைகளில் உள்ள பச்சையம் சூரிய ஒளியை ஈர்த்து கார்பன் டை ஆக்சைடு மற்றும் நீரைப் பயன்படுத்தி குளுக்கோஸை உருவாக்குகிறது."
        ),
        Pair(
            "English Grammar Rule",
            "Nouns are words that name people, places, things, or ideas. Verbs describe actions, occurrences, or states of being. A complete sentence requires at least one subject and one verb."
        ),
        Pair(
            "கணித சூத்திரம் (Maths Formula)",
            "செவ்வகத்தின் பரப்பளவு = நீளம் x அகலம் (Area = Length x Breadth). சுற்றளவு = 2 x (நீளம் + அகலம்). வட்டத்தின் சுற்றளவு = 2πr."
        )
    )

    LaunchedEffect(selectedSample) {
        extractedText = samples[selectedSample].second
    }

    Scaffold(
        topBar = {
            AppHeader(
                showBackButton = true,
                onBackClick = onBackClick,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SubikshaBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "படத்திலிருந்து உரை / Image to Text",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = "புத்தக பக்கத்தின் புகைப்படத்தை உரையாக மாற்றி வாசிக்கலாம் மற்றும் சந்தேகங்களை ஆசிரியரிடம் கேட்கலாம்.",
                    fontSize = 13.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Sample Selector
            item {
                Text(
                    text = "மாதிரி புத்தக பக்கங்கள் / Sample Pages:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubikshaNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    samples.indices.forEach { index ->
                        val selected = selectedSample == index
                        Surface(
                            color = if (selected) SubikshaMintGreen else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedSample = index }
                        ) {
                            Text(
                                text = "மாதிரி ${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else SubikshaNavy,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Extracted Text Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = SubikshaMintGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = samples[selectedSample].first,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubikshaNavy
                                )
                            }
                            Surface(
                                color = SubikshaMintGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "கண்டறியப்பட்ட உரை",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubikshaMintGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = extractedText,
                            onValueChange = { extractedText = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 4,
                            maxLines = 8,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SubikshaMintGreen,
                                unfocusedBorderColor = SubikshaCardBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onAskGemini("இந்த உரையை எளிய தமிழில் எனக்கு விளக்குங்கள்:\n\n$extractedText")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("விளக்கம் கேட்க", fontSize = 12.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    onAskGemini("இந்த உரையிலுள்ள கடினமான சொற்கள் மற்றும் ஆங்கில கலைச்சொற்களை பட்டியலிட்டு பொருள் கூறவும்:\n\n$extractedText")
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("கலைச்சொற்கள்", fontSize = 12.sp, color = SubikshaRoyalPurple)
                            }
                        }
                    }
                }
            }

            // Smart Reading Tips Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SubikshaSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = SubikshaWarmOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ஆசிரியர் தரும் வாசிப்பு குறிப்பு",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "பாடங்களை வாசிக்கும் போது முக்கிய வரிகளை அடிக்கோடிட்டு (highlight) சுபிக்ஷா ஆசிரியரிடம் கேள்விகளாகக் கேளுங்கள். நினைவில் வைக்க இது உதவும்!",
                            fontSize = 12.sp,
                            color = SubikshaNavy,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
