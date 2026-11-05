package com.example.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.WeatherApplication
import com.example.weather.domain.model.Weather
import com.example.weather.ui.state.WeatherUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * ViewModel экрана: держит состояние и опрашивает ESP32.
 *
 * Зависимость (репозиторий) достаётся из [WeatherApplication.container] —
 * ручная инъекция без DI-фреймворка.
 */
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as WeatherApplication).container.weatherRepository

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /** Не даёт опросу и ручному обновлению выполняться одновременно. */
    private val loadMutex = Mutex()

    /** Последние успешные данные: показываем их, если очередной опрос не удался. */
    private var lastWeather: Weather? = null

    init {
        // Опрашиваем устройство каждые 5 секунд — как веб-страница ESP32.
        // Цикл работает только пока экран на переднем плане: collectAsStateWithLifecycle
        // в фоне отписывается, subscriptionCount становится 0 и цикл отменяется.
        viewModelScope.launch {
            _uiState.subscriptionCount
                .map { count -> count > 0 }
                .distinctUntilChanged()
                .collectLatest { active ->
                    if (active) {
                        while (true) {
                            load(silent = lastWeather != null)
                            delay(POLL_INTERVAL_MS)
                        }
                    }
                }
        }
    }

    /** Ручное обновление (кнопка на экране и «Повторить» на ошибке). */
    fun refresh() {
        viewModelScope.launch { load(silent = false) }
    }

    private suspend fun load(silent: Boolean) {
        if (!loadMutex.tryLock()) return
        try {
            // При фоновом опросе не мигаем спиннером поверх уже показанных данных.
            if (!silent) {
                _uiState.value = WeatherUiState.Loading
            }

            repository.getCurrentWeather()
                .onSuccess { weather ->
                    lastWeather = weather
                    _uiState.value = WeatherUiState.Success(weather = weather)
                }
                .onFailure { error ->
                    val cached = lastWeather
                    _uiState.value = if (cached != null) {
                        // Связи нет, но старые показания не выбрасываем.
                        WeatherUiState.Success(weather = cached, isStale = true)
                    } else {
                        WeatherUiState.Error(message = error.errorMessage())
                    }
                }
        } finally {
            loadMutex.unlock()
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 5_000L
    }
}
