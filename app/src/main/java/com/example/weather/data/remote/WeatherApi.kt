package com.example.weather.data.remote

import com.example.weather.data.remote.dto.WeatherResponseDto
import retrofit2.http.GET

/**
 * Описание вебсервиса ESP32. Retrofit сам генерирует реализацию интерфейса.
 *
 * Полный URL = BuildConfig.BASE_URL + путь метода:
 *   http://192.168.1.133/ + "data"  →  http://192.168.1.133/data
 */
interface WeatherApi {

    @GET("data")
    suspend fun getCurrentWeather(): WeatherResponseDto
}
