package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SubikshaBottomNavigation(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onPlusClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = currentScreen == "home",
                onClick = { onNavigate("home") },
                testTag = "nav_home"
            )

            BottomNavItem(
                icon = Icons.Default.Explore,
                label = "Explore",
                selected = currentScreen == "explore",
                onClick = { onNavigate("explore") },
                testTag = "nav_explore"
            )

            // Central Plus (+) Floating Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SubikshaRoyalPurple, SubikshaPink)
                        )
                    )
                    .clickable { onPlusClick() }
                    .testTag("nav_central_plus"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Actions",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                selected = currentScreen == "profile",
                onClick = { onNavigate("profile") },
                testTag = "nav_profile"
            )

            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                selected = currentScreen == "settings",
                onClick = { onNavigate("settings") },
                testTag = "nav_settings"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = SubikshaRoyalPurple
    val inactiveColor = SubikshaTextMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) activeColor else inactiveColor
        )
    }
}
