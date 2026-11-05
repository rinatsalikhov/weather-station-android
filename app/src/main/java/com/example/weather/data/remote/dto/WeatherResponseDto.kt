package com.example.weather.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO — «как есть» ответ вебсервиса ESP32:
 *
 * ```json
 * {"device":"esp32-meteo","ip":"192.168.1.133","uptime_s":40820,
 *  "rssi_dbm":-61,"temperature_c":8.80,"humidity_pct":100.0,"pressure_mmhg":766.04}
 * ```
 *
 * Поля с подчёркиваниями переименовываем через `@SerialName`.
 */
@Serializable
data class WeatherResponseDto(
    val device: String = "",
    val ip: String = "",
    @SerialName("uptime_s") val uptimeSeconds: Long = 0,
    @SerialName("rssi_dbm") val rssiDbm: Int = 0,
    @SerialName("temperature_c") val temperatureCelsius: Double,
    @SerialName("humidity_pct") val humidityPercent: Double,
    @SerialName("pressure_mmhg") val pressureMmHg: Double,
)
