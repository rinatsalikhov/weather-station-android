package com.example.weather

import android.app.Application
import com.example.weather.di.AppContainer

/**
 * Точка входа в приложение.
 *
 * Здесь создаётся [AppContainer] — простой ручной «DI» (без Hilt).
 * Если позже понадобится Hilt, достаточно удалить этот класс и контейнер,
 * а зависимости объявить аннотациями @HiltViewModel / @Inject.
 */
class WeatherApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
