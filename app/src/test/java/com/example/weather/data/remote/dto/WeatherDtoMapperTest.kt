package com.example.weather.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Юнит-тесты на маппинг и десериализацию.
 * Сеть здесь не нужна: тестируем чистые функции.
 */
class WeatherDtoMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `maps dto fields to domain model`() {
        val dto = WeatherResponseDto(
            device = "esp32-meteo",
            ip = "192.168.1.133",
            uptimeSeconds = 40820,
            rssiDbm = -61,
            temperatureCelsius = 8.80,
            humidityPercent = 100.0,
            pressureMmHg = 766.04,
        )

        val weather = dto.toDomain(fetchedAtMillis = 1_700_000_000_000)

        assertEquals(8.80, weather.temperatureCelsius, 0.001)
        assertEquals(100.0, weather.humidityPercent, 0.001)
        assertEquals(766.04, weather.pressureMmHg, 0.001)
        assertEquals("esp32-meteo", weather.device)
        assertEquals("192.168.1.133", weather.ip)
        assertEquals(40820L, weather.uptimeSeconds)
        assertEquals(-61, weather.rssiDbm)
        assertEquals(1_700_000_000_000, weather.fetchedAtMillis)
    }

    @Test
    fun `parses real esp32 response`() {
        val raw = """
            {"device":"esp32-meteo","ip":"192.168.1.133","uptime_s":40820,
             "rssi_dbm":-61,"temperature_c":8.80,"humidity_pct":100.0,
             "pressure_mmhg":766.04}
        """.trimIndent()

        val weather = json.decodeFromString<WeatherResponseDto>(raw)
            .toDomain(fetchedAtMillis = 0L)

        assertEquals(8.80, weather.temperatureCelsius, 0.001)
        assertEquals(100.0, weather.humidityPercent, 0.001)
        assertEquals(766.04, weather.pressureMmHg, 0.001)
        assertEquals("esp32-meteo", weather.device)
        assertEquals(40820L, weather.uptimeSeconds)
    }

    @Test
    fun `tolerates missing optional fields`() {
        val raw = """
            {"temperature_c":-3.5,"humidity_pct":80,"pressure_mmhg":745.2}
        """.trimIndent()

        val weather = json.decodeFromString<WeatherResponseDto>(raw)
            .toDomain(fetchedAtMillis = 0L)

        assertEquals(-3.5, weather.temperatureCelsius, 0.001)
        assertEquals("", weather.device)
        assertEquals(0L, weather.uptimeSeconds)
    }
}
