package com.borrownest.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GivenDeep,
    onPrimary = Color0xWhite,
    secondary = BorrowedDeep,
    onSecondary = Color0xWhite,
    tertiary = SpineAccent,
    background = AppBackground,
    onBackground = DeepText,
    surface = Surface,
    onSurface = DeepText,
    surfaceVariant = SpineLight,
    onSurfaceVariant = SecondaryText,
    error = StatusOverdue,
    outline = DividerColor
)

private val DarkColors = darkColorScheme(
    primary = GivenTerracotta,
    secondary = BorrowedIndigo,
    tertiary = SpineAccent,
    background = Color0xDarkBg,
    surface = Color0xDarkSurface,
    onBackground = SpineLight,
    onSurface = SpineLight,
    error = StatusOverdue
)

@Composable
fun BorrowNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep the calm paper-ledger identity: use the light scheme by default,
    // and a restrained dark scheme only when the system requests it.
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = BorrowNestTypography,
        content = content
    )
}
