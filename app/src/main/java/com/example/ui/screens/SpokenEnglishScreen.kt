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

data class EnglishLesson(
    val title: String,
    val tamilDesc: String,
    val englishPhrase: String,
    val tamilMeaning: String,
    val tip: String
)

@Composable
fun SpokenEnglishScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
    val lessons = listOf(
        EnglishLesson(
            title = "1. அறிமுகம் & வாழ்த்துக்கள் (Greetings)",
            tamilDesc = "புதியவர்களை சந்திக்கும் போது பயன்படுத்தும் எளிய சொற்றொடர்கள்",
            englishPhrase = "“Good morning! It's a pleasure to meet you. My name is...”",
            tamilMeaning = "காலை வணக்கம்! உங்களை சந்தித்ததில் மகிழ்ச்சி. என் பெயர்...",
            tip = "Pleasure என்பதை 'பிளஷர்' என்று மென்மையாக உச்சரிக்கவும்."
        ),
        EnglishLesson(
            title = "2. உதவி கேட்டல் (Asking for Help)",
            tamilDesc = "வகுப்பறையில் அல்லது வழியில் பணிவாக உதவி கோருதல்",
            englishPhrase = "“Could you please help me with this question?”",
            tamilMeaning = "தயவுசெய்து இந்தக் கேள்விக்கு எனக்கு உதவ முடியுமா?",
            tip = "'Can you' என்பதை விட 'Could you please' என்பது மிகுந்த பணிவான சொல்."
        ),
        EnglishLesson(
            title = "3. நன்றி தெரிவித்தல் (Expressing Gratitude)",
            tamilDesc = "ஒருவருக்கு உளமார நன்றி தெரிவிக்கும் வழிகள்",
            englishPhrase = "“Thank you so much! I really appreciate your timely help.”",
            tamilMeaning = "மிக்க நன்றி! உங்கள் சரியான நேர உதவியை நான் மிகவும் மதிக்கிறேன்.",
            tip = "Appreciate என்பதை 'அப்ரிஷியேட்' என உச்சரிக்க வேண்டும்."
        ),
        EnglishLesson(
            title = "4. வழிகேட்டல் (Asking Directions)",
            tamilDesc = "புதிய ஊர் அல்லது கட்டிடத்தில் பாதை கேட்க",
            englishPhrase = "“Excuse me, how can I get to the nearest library from here?”",
            tamilMeaning = "மன்னிக்கவும், இங்கிருந்து மிக அருகில் உள்ள நூலகத்திற்கு எப்படி செல்வது?",
            tip = "எப்போதும் 'Excuse me' என்று உரையாடலைத் தொடங்கவும்."
        )
    )

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "ஆங்கிலம் பேசுவோம் / Spoken English",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = "தினசரி உரையாடல் வாக்கியங்களை தமிழில் புரிந்துகொண்டு எளிதாக ஆங்கிலத்தில் பேசப் பழகுங்கள்.",
                    fontSize = 13.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(lessons.size) { index ->
                val lesson = lessons[index]
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
                            Text(
                                text = lesson.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = SubikshaWarmOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = lesson.tamilDesc,
                            fontSize = 12.sp,
                            color = SubikshaTextMuted,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        Surface(
                            color = SubikshaWarmOrange.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = lesson.englishPhrase,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubikshaNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lesson.tamilMeaning,
                                    fontSize = 13.sp,
                                    color = Color(0xFF9A3412)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = SubikshaWarmOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = lesson.tip,
                                fontSize = 11.sp,
                                color = SubikshaTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onAskGemini("Spoken English பயிற்சி: '${lesson.englishPhrase}' இந்த வாக்கியத்தை வெவ்வேறு சூழல்களில் எப்படி பயன்படுத்துவது என்று தமிழ் விளக்கத்துடன் 3 எளிய உதாரண உரையாடல்களைக் கொடுங்கள்.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SubikshaWarmOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ஆசிரியரிடம் பயிற்சி பெறுக (Practice with AI)",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
