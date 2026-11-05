package com.example.weather.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = WebText,
    onPrimary = Color.White,
    background = WebBackground,
    onBackground = WebText,
    surface = WebSurface,
    onSurface = WebText,
    // Подписи в карточках: «Температура», «Влажность», «Давление»
    onSurfaceVariant = WebMuted,
    error = WebError,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkText,
    onPrimary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    onSurfaceVariant = DarkMuted,
    error = DarkError,
)

/**
 * Тема приложения. Dynamic Color (Material You) отключён намеренно:
 * цвета должны совпадать с веб-страницей устройства.
 */
@Composable
fun WeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
