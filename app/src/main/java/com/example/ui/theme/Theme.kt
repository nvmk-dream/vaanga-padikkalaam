package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SubikshaColorScheme = lightColorScheme(
    primary = SubikshaRoyalPurple,
    onPrimary = Color.White,
    primaryContainer = SubikshaPastelPurple.copy(alpha = 0.35f),
    onPrimaryContainer = SubikshaNavy,
    secondary = SubikshaSkyBlue,
    onSecondary = Color.White,
    secondaryContainer = SubikshaSkyBlue.copy(alpha = 0.18f),
    onSecondaryContainer = SubikshaNavy,
    tertiary = SubikshaPink,
    onTertiary = Color.White,
    background = SubikshaBackground,
    onBackground = SubikshaNavy,
    surface = SubikshaCardSurface,
    onSurface = SubikshaNavy,
    surfaceVariant = SubikshaSurfaceVariant,
    onSurfaceVariant = SubikshaTextMuted,
    outline = SubikshaCardBorder
)

@Composable
fun SubikshaTeacherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SubikshaColorScheme,
        typography = Typography,
        content = content
    )
}
