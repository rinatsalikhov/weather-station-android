package com.example.weather.ui.theme

import androidx.compose.ui.graphics.Color

// Палитра взята с веб-страницы ESP32 (http://192.168.1.133/).
val WebBackground = Color(0xFFEEF1F6)
val WebSurface = Color(0xFFFFFFFF)
val WebText = Color(0xFF2C3E50)
val WebMuted = Color(0xFF7F8C8D)
val WebError = Color(0xFFC0392B)

// Тёмный вариант — та же структура, но приглушённые цвета.
val DarkBackground = Color(0xFF15181D)
val DarkSurface = Color(0xFF1F242B)
val DarkText = Color(0xFFE6EAF0)
val DarkMuted = Color(0xFFA3ADB8)
val DarkError = Color(0xFFE57373)

// Отклонение давления от нормы 760: выше нормы — зелёное, ниже — красное.
val WebPressureUp = Color(0xFF1E8E3E)
val WebPressureDown = Color(0xFFC0392B)
val DarkPressureUp = Color(0xFF66BB6A)
val DarkPressureDown = Color(0xFFE57373)
