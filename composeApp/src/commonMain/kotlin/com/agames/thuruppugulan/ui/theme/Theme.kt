package com.agames.thuruppugulan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FeltGreen = Color(0xFF1B5E3B)
private val FeltDeep = Color(0xFF0F3D28)
private val Cream = Color(0xFFF3EDE2)
private val Gold = Color(0xFFC9A227)
private val Ink = Color(0xFF1A1A1A)

private val scheme = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    secondary = Cream,
    onSecondary = Ink,
    background = FeltDeep,
    onBackground = Cream,
    surface = FeltGreen,
    onSurface = Cream,
    surfaceVariant = Color(0xFF245C40),
    onSurfaceVariant = Cream,
    error = Color(0xFFCF6679),
)

@Composable
fun ThuruppuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme,
        content = content,
    )
}

object TableColors {
    val felt = FeltGreen
    val feltDeep = FeltDeep
    val cream = Cream
    val gold = Gold
    val redSuit = Color(0xFFC62828)
    val blackSuit = Color(0xFF212121)
    val cardFace = Color(0xFFFFFBF5)
}
