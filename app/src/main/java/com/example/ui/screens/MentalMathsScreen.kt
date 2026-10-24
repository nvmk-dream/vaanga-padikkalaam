package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import kotlin.random.Random

data class MathQuestion(
    val num1: Int,
    val num2: Int,
    val operator: String,
    val answer: Int,
    val options: List<Int>
)

@Composable
fun MentalMathsScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    fun generateQuestion(): MathQuestion {
        val op = listOf("+", "-", "x").random()
        val n1: Int
        val n2: Int
        val ans: Int
        when (op) {
            "+" -> {
                n1 = Random.nextInt(12, 99)
                n2 = Random.nextInt(11, 89)
                ans = n1 + n2
            }
            "-" -> {
                n1 = Random.nextInt(30, 99)
                n2 = Random.nextInt(10, n1)
                ans = n1 - n2
            }
            else -> {
                n1 = Random.nextInt(4, 15)
                n2 = Random.nextInt(3, 12)
                ans = n1 * n2
            }
        }
        val opts = mutableSetOf(ans)
        while (opts.size < 4) {
            val delta = listOf(-10, -2, -1, 1, 2, 10, 5, -5).random()
            val candidate = ans + delta
            if (candidate > 0) opts.add(candidate)
        }
        return MathQuestion(n1, n2, op, ans, opts.toList().shuffled())
    }

    var currentQuestion by remember { mutableStateOf(generateQuestion()) }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "மனக்கணக்கு / Mental Maths",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubikshaNavy
                )
                Text(
                    text = "காகிதம் இல்லாமல் மனதிலேயே கணக்கிட்டு வேகத்தை சோதியுங்கள்!",
                    fontSize = 13.sp,
                    color = SubikshaTextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Score & Streak Board
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("மதிப்பெண் (Score)", fontSize = 12.sp, color = SubikshaTextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$score",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SubikshaPink
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("தொடர் வெற்றி (Streak)", fontSize = 12.sp, color = SubikshaTextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = SubikshaWarmOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streak",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SubikshaWarmOrange
                                )
                            }
                        }
                    }
                }
            }

            // Question Box
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = SubikshaPink.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "வேகக் கணக்கு சவால்",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaPink,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "${currentQuestion.num1}  ${currentQuestion.operator}  ${currentQuestion.num2} = ?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SubikshaNavy,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Options Grid (2x2)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (row in currentQuestion.options.chunked(2)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (opt in row) {
                                        val isThisOptionSelected = selectedOption == opt
                                        val isCorrect = opt == currentQuestion.answer
                                        val btnColor = when {
                                            isAnswered && isCorrect -> SubikshaMintGreen
                                            isAnswered && isThisOptionSelected && !isCorrect -> SubikshaPink
                                            else -> SubikshaSurfaceVariant
                                        }
                                        val textColor = when {
                                            isAnswered && (isCorrect || isThisOptionSelected) -> Color.White
                                            else -> SubikshaNavy
                                        }

                                        Surface(
                                            color = btnColor,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable(enabled = !isAnswered) {
                                                    selectedOption = opt
                                                    isAnswered = true
                                                    if (opt == currentQuestion.answer) {
                                                        score += 10
                                                        streak += 1
                                                    } else {
                                                        streak = 0
                                                    }
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$opt",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isAnswered) {
                            Spacer(modifier = Modifier.height(18.dp))
                            val isCorrect = selectedOption == currentQuestion.answer
                            Text(
                                text = if (isCorrect) "மிகச் சரி! அருமை! 🎉 (+10 புள்ளிகள்)"
                                else "தவறு! சரியான விடை: ${currentQuestion.answer}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) SubikshaMintGreen else SubikshaPink
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    currentQuestion = generateQuestion()
                                    selectedOption = null
                                    isAnswered = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SubikshaRoyalPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("அடுத்த கணக்கு / Next Question", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Mental Math Trick / Ask Gemini AI
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = SubikshaWarmOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "வேகக் கணித உத்தி அறிய வேண்டுமா?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SubikshaNavy
                            )
                            Text(
                                text = "${currentQuestion.num1} ${currentQuestion.operator} ${currentQuestion.num2} ஐ மனதில் எளிதாக கணக்கிடும் விதியை ஆசிரியரிடம் கேளுங்கள்",
                                fontSize = 11.sp,
                                color = SubikshaTextMuted
                            )
                        }
                        IconButton(
                            onClick = {
                                onAskGemini("${currentQuestion.num1} ${currentQuestion.operator} ${currentQuestion.num2} இந்தக் கணக்கை மனதில் எளிதில் கணக்கிட ஏதேனும் எளிய mental math trick / சூத்திரம் உள்ளதா? தமிழ் விளக்கத்துடன் கற்றுத் தரவும்.")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Ask Trick",
                                tint = SubikshaRoyalPurple
                            )
                        }
                    }
                }
            }
        }
    }
}
