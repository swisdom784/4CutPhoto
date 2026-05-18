package com.fourcut.photo.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    background = WarmWhite,
    surface = PureWhite,
    surfaceVariant = SandSurface,
    primary = MutedOlive,
    secondary = Clay,
    onBackground = Charcoal,
    onSurface = Charcoal
)

@Composable
fun FourCutPhotoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
