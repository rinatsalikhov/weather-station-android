package com.example.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.ui.WeatherScreen
import com.example.weather.ui.WeatherViewModel
import com.example.weather.ui.theme.WeatherTheme

class MainActivity : ComponentActivity() {

    private val requestLocalNetwork = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestLocalNetworkIfNeeded()

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

    private fun requestLocalNetworkIfNeeded() {
        if (Build.VERSION.SDK_INT < 36) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCAL_NETWORK)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestLocalNetwork.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
    }
}
