package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
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
                    text = "அறிவுப் பெட்டகம் / Explore Knowledge",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = "தினசரி திருக்குறள், தமிழ் இலக்கிய நயங்கள் மற்றும் சுவாரஸ்ய அறிவியல் தகவல்கள்.",
                    fontSize = 13.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Daily Thirukkural Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "இன்றைய திருக்குறள் (குறள் 391)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaRoyalPurple
                            )
                            Surface(
                                color = SubikshaPastelPurple.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "அதிகாரம்: கல்வி",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubikshaRoyalPurple,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "“கற்க கசடறக் கற்பவை கற்றபின்\nநிற்க அதற்குத் தக.”",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SubikshaNavy,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "விளக்கம்: கற்கத் தகுந்த நூல்களைக் குற்றமறக் கற்க வேண்டும்; அவ்வாறு கற்ற பிறகு, கற்ற கல்விக்கு ஏற்ப நல்ல வழியில் நடக்க வேண்டும்.",
                            fontSize = 13.sp,
                            color = SubikshaTextMuted,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                onAskGemini("திருக்குறள் 'கற்க கசடறக் கற்பவை' குறளின் ஆழ்ந்த கருத்தையும் மாணவர்கள் வாழ்வில் கடைபிடிக்க வேண்டிய முறையையும் அழகாக விளக்குங்கள்.")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SubikshaRoyalPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("மேலும் விரிவான பொருள் கேட்க", color = SubikshaRoyalPurple, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Tamil Proverbs (பழமொழிகள்)
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
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = SubikshaWarmOrange,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "பொன்மொழிகள் & பழமொழிகள்",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "1. “தொட்டில் பழக்கம் சுடுகாடு மட்டும்.” - சிறுவயதிலேயே நல்ல பழக்கங்களை உருவாக்க வேண்டும்.",
                            fontSize = 13.sp,
                            color = SubikshaNavy,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "2. “சித்திரமும் கைப்பழக்கம் செந்தமிழும் நாப்பழக்கம்.” - எந்தத் திறமையும் தொடர் பயிற்சியினாலேயே சிறக்கும்.",
                            fontSize = 13.sp,
                            color = SubikshaNavy,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Science Fact
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
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = SubikshaSkyBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "சுவாரஸ்ய அறிவியல் உண்மை",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "சூரியனிலிருந்து வெளிச்சம் பூமியை வந்தடைய சுமார் 8 நிமிடங்கள் 20 வினாடிகள் ஆகிறது! நாம் இப்போது பார்க்கும் சூரிய ஒளி 8 நிமிடங்களுக்கு முன் புறப்பட்டது.",
                            fontSize = 13.sp,
                            color = SubikshaNavy,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}
