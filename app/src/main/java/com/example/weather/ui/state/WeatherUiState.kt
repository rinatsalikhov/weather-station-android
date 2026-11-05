package com.example.weather.ui.state

import com.example.weather.domain.model.Weather

/**
 * Состояние экрана.
 *
 * `Success` умеет быть «устаревшим» ([isStale]): если очередной опрос не удался,
 * но данные уже были получены раньше, экран продолжает показывать последние
 * значения и сообщает, что связи с устройством нет.
 */
sealed interface WeatherUiState {

    data object Loading : WeatherUiState

    data class Success(
        val weather: Weather,
        val isStale: Boolean = false,
    ) : WeatherUiState

    data class Error(val message: String) : WeatherUiState
}
