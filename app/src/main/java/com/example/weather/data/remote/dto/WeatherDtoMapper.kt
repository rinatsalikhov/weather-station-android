package com.example.weather.data.remote.dto

import com.example.weather.domain.model.Weather

/**
 * Маппинг DTO → доменная модель.
 *
 * [fetchedAtMillis] передаётся снаружи, чтобы маппер оставался чистой функцией
 * и его можно было проверить юнит-тестом без сети и без текущего времени.
 */
internal fun WeatherResponseDto.toDomain(fetchedAtMillis: Long): Weather = Weather(
    temperatureCelsius = temperatureCelsius,
    humidityPercent = humidityPercent,
    pressureMmHg = pressureMmHg,
    device = device,
    ip = ip,
    uptimeSeconds = uptimeSeconds,
    rssiDbm = rssiDbm,
    fetchedAtMillis = fetchedAtMillis,
)
