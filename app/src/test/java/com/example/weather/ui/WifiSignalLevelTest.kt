package com.example.weather.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Пороги индикатора уровня Wi-Fi. Чистая функция — сеть и Android не нужны.
 */
class WifiSignalLevelTest {

    @Test
    fun `very strong signal gives four bars`() {
        assertEquals(4, wifiSignalLevel(-40))
        assertEquals(4, wifiSignalLevel(-55))
    }

    @Test
    fun `good signal gives three bars`() {
        assertEquals(3, wifiSignalLevel(-56))
        assertEquals(3, wifiSignalLevel(-67))
    }

    @Test
    fun `medium signal gives two bars`() {
        assertEquals(2, wifiSignalLevel(-68))
        assertEquals(2, wifiSignalLevel(-78))
    }

    @Test
    fun `weak signal gives one bar`() {
        assertEquals(1, wifiSignalLevel(-79))
        assertEquals(1, wifiSignalLevel(-88))
    }

    @Test
    fun `almost no signal gives zero bars`() {
        assertEquals(0, wifiSignalLevel(-89))
        assertEquals(0, wifiSignalLevel(-100))
    }
}
