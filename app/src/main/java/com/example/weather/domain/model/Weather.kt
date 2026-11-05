package com.example.weather.domain.model

/**
 * Доменная модель: то, с чем работает UI.
 *
 * Не зависит ни от Retrofit, ни от kotlinx.serialization — при смене формата
 * ответа меняется только DTO и маппер.
 */
data class Weather(
    val temperatureCelsius: Double,
    /** Влажность, % */
    val humidityPercent: Double,
    /** Атмосферное давление, мм рт. ст. */
    val pressureMmHg: Double,
    /** Имя устройства, от которого пришли данные (например, esp32-meteo). */
    val device: String,
    /** IP-адрес устройства. */
    val ip: String,
    /** Время работы устройства с момента включения, секунды. */
    val uptimeSeconds: Long,
    /** Уровень сигнала Wi-Fi, dBm. */
    val rssiDbm: Int,
    /** Момент получения данных (System.currentTimeMillis), для «обновлено …». */
    val fetchedAtMillis: Long,
)
