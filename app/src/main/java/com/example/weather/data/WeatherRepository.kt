package com.example.weather.data

import com.example.weather.data.remote.WeatherApi
import com.example.weather.data.remote.dto.toDomain
import com.example.weather.domain.model.Weather

/**
 * Единственная точка доступа к данным.
 *
 * ViewModel не знает про Retrofit и DTO — только про эту модель.
 */
class WeatherRepository(private val api: WeatherApi) {

    /**
     * Запрашивает текущие показания с ESP32.
     *
     * Возвращает [Result]: успех — данные, ошибка — описание проблемы
     * (сеть недоступна, устройство выключено, JSON не распарсился и т.д.).
     */
    suspend fun getCurrentWeather(): Result<Weather> = runCatching {
        api.getCurrentWeather().toDomain(fetchedAtMillis = System.currentTimeMillis())
    }
}
