package com.example.weather.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R

/**
 * Уровень сигнала Wi-Fi: 0..4 «палочки», как у индикатора сотовой связи.
 *
 * Пороги подобраны под типичный диапазон RSSI ESP32 (-40…-90 dBm):
 * -55 и выше — отлично, -88 и ниже — связи практически нет.
 */
internal fun wifiSignalLevel(rssiDbm: Int): Int = when {
    rssiDbm >= -55 -> 4
    rssiDbm >= -67 -> 3
    rssiDbm >= -78 -> 2
    rssiDbm >= -88 -> 1
    else -> 0
}

/**
 * Индикатор «WiFi ▂▄▆█ -62 dBm»: подпись, четыре полоски и значение в dBm.
 */
@Composable
internal fun WifiSignalIndicator(
    rssiDbm: Int,
    modifier: Modifier = Modifier,
) {
    val level = wifiSignalLevel(rssiDbm)
    val activeColor = MaterialTheme.colorScheme.onSurfaceVariant
    val inactiveColor = activeColor.copy(alpha = 0.25f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.wifi_label),
            fontSize = 13.sp,
            color = activeColor,
        )

        Canvas(modifier = Modifier.size(width = 22.dp, height = 14.dp)) {
            val bars = 4
            val gap = 2.dp.toPx()
            val barWidth = (size.width - gap * (bars - 1)) / bars
            val corner = CornerRadius(barWidth / 2f, barWidth / 2f)

            repeat(bars) { index ->
                val height = size.height * (index + 1) / bars
                drawRoundRect(
                    color = if (index < level) activeColor else inactiveColor,
                    topLeft = Offset(
                        x = index * (barWidth + gap),
                        y = size.height - height,
                    ),
                    size = Size(barWidth, height),
                    cornerRadius = corner,
                )
            }
        }

        Text(
            text = stringResource(R.string.wifi_dbm, rssiDbm),
            fontSize = 13.sp,
            color = activeColor,
        )
    }
}
