package com.example.weather.di

import com.example.weather.BuildConfig
import com.example.weather.data.WeatherRepository
import com.example.weather.data.remote.WeatherApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Ручной контейнер зависимостей: собирает Retrofit → API → Repository.
 *
 * Заменяет Hilt для приложения с одним экраном. Экземпляр создаётся один раз
 * в [com.example.weather.WeatherApplication].
 */
@OptIn(ExperimentalSerializationApi::class)
class AppContainer {

    private val json = Json {
        // Сервис может отдавать лишние поля — не падаем на них.
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val weatherRepository: WeatherRepository =
        WeatherRepository(retrofit.create(WeatherApi::class.java))
}
