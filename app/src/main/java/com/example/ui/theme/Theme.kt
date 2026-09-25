package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ImpostorColorScheme = lightColorScheme(
    primary = PrimaryCyan,
    onPrimary = PureWhite,
    secondary = DarkPill,
    onSecondary = PureWhite,
    background = LightIceBlue,
    onBackground = TextDark,
    surface = PureWhite,
    onSurface = TextDark
)

@Composable
fun ImpostorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ImpostorColorScheme,
        typography = GameTypography,
        content = content
    )
}
