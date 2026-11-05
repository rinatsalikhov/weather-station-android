package com.example.weather.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.domain.model.Weather
import com.example.weather.ui.state.WeatherUiState
import com.example.weather.ui.theme.DarkPressureDown
import com.example.weather.ui.theme.DarkPressureUp
import com.example.weather.ui.theme.WeatherTheme
import com.example.weather.ui.theme.WebPressureDown
import com.example.weather.ui.theme.WebPressureUp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Чистый (stateless) экран: принимает состояние и колбэки, ничего не знает о ViewModel.
 *
 * Вёрстка повторяет веб-страницу устройства (http://192.168.1.133/):
 * заголовок, три белые карточки «иконка + подпись слева, значение справа»
 * и мелкая строка со служебной информацией внизу.
 */
@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            WeatherUiState.Loading -> Centered {
                CircularProgressIndicator()
            }

            is WeatherUiState.Error -> Centered {
                ErrorState(message = state.message, onRetry = onRefresh)
            }

            is WeatherUiState.Success -> WeatherContent(
                weather = state.weather,
                isStale = state.isStale,
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun WeatherContent(
    weather: Weather,
    isStale: Boolean,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.screen_title),
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(28.dp))

        MetricCard(
            icon = stringResource(R.string.icon_temperature),
            label = stringResource(R.string.temperature),
            value = stringResource(R.string.temperature_value, weather.temperatureCelsius),
        )
        Spacer(Modifier.height(14.dp))
        MetricCard(
            icon = stringResource(R.string.icon_humidity),
            label = stringResource(R.string.humidity),
            value = stringResource(R.string.humidity_value, weather.humidityPercent),
        )
        Spacer(Modifier.height(14.dp))
        val pressureDeviation = weather.pressureMmHg - NORMAL_PRESSURE_MMHG
        val pressureDeviationText = stringResource(R.string.pressure_deviation, pressureDeviation)
        val pressureDeviationColor = deviationColor(pressureDeviation)

        MetricCard(
            icon = stringResource(R.string.icon_pressure),
            label = stringResource(R.string.pressure),
            value = stringResource(R.string.pressure_value, weather.pressureMmHg),
            valueSuffix = pressureDeviationText,
            valueSuffixColor = pressureDeviationColor,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = metaText(weather = weather, isStale = isStale),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            lineHeight = 22.sp,
            color = if (isStale) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            },
        )

        if (!isStale) {
            Spacer(Modifier.height(4.dp))
            WifiSignalIndicator(rssiDbm = weather.rssiDbm)
        }

        TextButton(onClick = onRefresh) {
            Text(text = stringResource(R.string.action_refresh), fontSize = 13.sp)
        }
    }
}

/** Размер крупного значения в карточке. */
private val ValueTextSize = 28.sp

/** Размер приписки после значения (отклонение давления). */
private val ValueSuffixTextSize = 18.sp

/**
 * Текст значения с припиской: крупное значение и, если есть, мелкое отклонение
 * сразу после него («767.5 мм +7.5»).
 */
private fun valueText(value: String, suffix: String?, suffixColor: Color) =
    buildAnnotatedString {
        append(value)
        if (suffix != null) {
            withStyle(
                SpanStyle(
                    fontSize = ValueSuffixTextSize,
                    fontWeight = FontWeight.Medium,
                    color = suffixColor,
                ),
            ) {
                append(' ')
                append(suffix)
            }
        }
    }

/**
 * Одна карточка: слева эмодзи-иконка и подпись, справа крупное значение.
 * [valueSuffix] — необязательная приписка после значения, [valueSuffixColor] — её цвет.
 */
@Composable
private fun MetricCard(
    icon: String,
    label: String,
    value: String,
    valueSuffix: String? = null,
    valueSuffixColor: Color = Color.Unspecified,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = icon, fontSize = 26.sp)
                Text(
                    text = label,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = remember(value, valueSuffix, valueSuffixColor) {
                    valueText(value, valueSuffix, valueSuffixColor)
                },
                fontSize = ValueTextSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

/** Цвет отклонения давления: выше нормы — зелёный, ниже — красный. */
@Composable
private fun deviationColor(deviation: Double): Color {
    val darkTheme = isSystemInDarkTheme()
    return when {
        deviation > 0 -> if (darkTheme) DarkPressureUp else WebPressureUp
        deviation < 0 -> if (darkTheme) DarkPressureDown else WebPressureDown
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * Ширина приписки к значению в тех же единицах, что и вёрстка, — на неё сдвигается
 * шкала давления, чтобы центральная отметка осталась под цифрами значения.
 */
@Composable
private fun valueSuffixWidth(value: String, suffix: String): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val baseStyle = LocalTextStyle.current.merge(
        TextStyle(fontSize = ValueTextSize, fontWeight = FontWeight.Bold),
    )
    return remember(value, suffix, baseStyle, density) {
        with(density) {
            val plain = measurer.measure(text = AnnotatedString(value), style = baseStyle)
            val full = measurer.measure(
                text = valueText(value, suffix, Color.Unspecified),
                style = baseStyle,
            )
            (full.size.width - plain.size.width).toDp()
        }
    }
}

/** Нормальное атмосферное давление, мм рт. ст. */
private const val NORMAL_PRESSURE_MMHG = 760.0

/** Сколько делений шкалы с каждой стороны от центральной отметки. */
private const val PRESSURE_SCALE_TICKS_SIDE = 3

/**
 * Во сколько раз видимая шкала уже своего контейнера. Контейнер (половина карточки,
 * прижатая к правому краю) держит центральную отметку под цифрами значения, а сами
 * деления рисуются плотнее.
 */
private const val PRESSURE_SCALE_WIDTH_RATIO = 1.5f

/** Цена одного деления шкалы давления, мм рт. ст. */
private const val PRESSURE_MMHG_PER_TICK = 3.0

/**
 * На каком делении шкалы стоит норма «760», в шагах от центральной отметки:
 * отрицательное — слева (давление выше нормы), положительное — справа (ниже).
 *
 * Норма занимает ближайшее к краю деление, поэтому шкала читается как обычная
 * линейка, а не как плавающая подпись; больше [PRESSURE_SCALE_TICKS_SIDE]
 * делений она не уходит.
 */
internal fun pressureNormalTick(currentMmHg: Double): Int {
    val delta = currentMmHg - NORMAL_PRESSURE_MMHG
    if (delta == 0.0) return 0
    val steps = ceil(abs(delta) / PRESSURE_MMHG_PER_TICK).toInt()
        .coerceAtMost(PRESSURE_SCALE_TICKS_SIDE)
    return if (delta > 0) -steps else steps
}

/**
 * Шкала давления внизу карточки: узкая, примерно треть ширины карточки, стоит под
 * цифрами значения так, чтобы центральная отметка была ровно под их серединой.
 *
 * Отметок всего семь — центральная (выше и толще остальных) и по три с каждой
 * стороны. Центральная отметка — текущее значение, а на месте одного из делений
 * стоит норма «760»: слева от центра, если давление выше нормы, справа — если ниже.
 */
@Composable
private fun PressureScale(currentMmHg: Double, modifier: Modifier = Modifier) {
    val activeColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tickColor = activeColor.copy(alpha = 0.3f)
    val normalTick = pressureNormalTick(currentMmHg)
    val normalLabel = stringResource(R.string.pressure_normal, NORMAL_PRESSURE_MMHG)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember(activeColor) {
        TextStyle(fontSize = 10.sp, color = activeColor.copy(alpha = 0.8f))
    }

    BoxWithConstraints(modifier = modifier) {
        val step = maxWidth / (PRESSURE_SCALE_TICKS_SIDE * 2f * PRESSURE_SCALE_WIDTH_RATIO)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
        ) {
            val stepPx = step.toPx()
            val centerX = size.width / 2f
            val baseline = size.height
            val shortTick = 6.dp.toPx()
            val longTick = 16.dp.toPx()

            for (index in -PRESSURE_SCALE_TICKS_SIDE..PRESSURE_SCALE_TICKS_SIDE) {
                // На месте нормы стоит цифра «760», отдельную отметку там не рисуем.
                if (index == normalTick) continue

                val isCenter = index == 0
                val x = centerX + index * stepPx
                drawLine(
                    color = if (isCenter) activeColor else tickColor,
                    start = Offset(x = x, y = baseline),
                    end = Offset(
                        x = x,
                        y = baseline - if (isCenter) longTick else shortTick,
                    ),
                    strokeWidth = if (isCenter) 3.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            // Сама норма — как подписанное деление шкалы: цифры стоят на её базисе.
            if (normalTick != 0) {
                val layout = textMeasurer.measure(text = normalLabel, style = labelStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = centerX + normalTick * stepPx - layout.size.width / 2f,
                        y = baseline - layout.firstBaseline,
                    ),
                )
            }
        }
    }
}

/**
 * Нижняя служебная строка, как на веб-странице:
 * «обновлено 12:39:46 · аптайм 685 мин 26 сек · esp32-meteo · IP 192.168.1.133».
 *
 * Уровень Wi-Fi показывается отдельным индикатором ([WifiSignalIndicator]).
 */
@Composable
private fun metaText(weather: Weather, isStale: Boolean): String {
    if (isStale) return stringResource(R.string.no_connection)

    val time = remember(weather.fetchedAtMillis) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(weather.fetchedAtMillis))
    }

    return listOfNotNull(
        stringResource(R.string.meta_updated, time),
        stringResource(
            R.string.meta_uptime,
            weather.uptimeSeconds / 60,
            weather.uptimeSeconds % 60,
        ),
        weather.device.takeIf { it.isNotBlank() },
        stringResource(R.string.meta_ip, weather.ip),
    ).joinToString(separator = " · ")
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.error_title),
            fontSize = 18.sp,
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.action_retry))
        }
    }
}

private val previewWeather = Weather(
    temperatureCelsius = 8.8,
    humidityPercent = 100.0,
    pressureMmHg = 766.0,
    device = "esp32-meteo",
    ip = "192.168.1.133",
    uptimeSeconds = 41_026,
    rssiDbm = -62,
    fetchedAtMillis = System.currentTimeMillis(),
)

@Preview(showBackground = true)
@Composable
private fun WeatherContentPreview() {
    WeatherTheme {
        WeatherScreen(
            uiState = WeatherUiState.Success(previewWeather),
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherStalePreview() {
    WeatherTheme {
        WeatherScreen(
            uiState = WeatherUiState.Success(previewWeather, isStale = true),
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherErrorPreview() {
    WeatherTheme {
        WeatherScreen(
            uiState = WeatherUiState.Error("Нет соединения с сервером"),
            onRefresh = {},
        )
    }
}
