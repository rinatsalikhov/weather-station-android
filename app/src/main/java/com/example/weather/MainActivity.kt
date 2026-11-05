package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.ui.WeatherScreen
import com.example.weather.ui.WeatherViewModel
import com.example.weather.ui.theme.WeatherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeatherTheme {
                // viewModel() сам создаст WeatherViewModel с нашим Application
                val viewModel: WeatherViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle(
                    minActiveState = Lifecycle.State.RESUMED,
                )

                WeatherScreen(
                    uiState = uiState,
                    onRefresh = viewModel::refresh,
                )
            }
        }
    }
}
