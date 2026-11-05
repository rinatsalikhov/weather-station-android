package com.example.weather.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Положение нормы «760» на шкале давления.
 *
 * Центральная (большая) отметка шкалы — текущее значение. Норма стоит на одном из
 * делений: слева от центра, если давление выше нормы, справа — если ниже.
 * На шкале по три деления в каждую сторону.
 */
class PressureScaleTest {

    @Test
    fun `above normal puts 760 left of center`() {
        assertEquals(-1, pressureNormalTick(761.0))
        assertEquals(-2, pressureNormalTick(765.0))
        assertEquals(-3, pressureNormalTick(767.6))
    }

    @Test
    fun `below normal puts 760 right of center`() {
        assertEquals(1, pressureNormalTick(759.0))
        assertEquals(2, pressureNormalTick(755.0))
        assertEquals(3, pressureNormalTick(752.0))
    }

    @Test
    fun `exactly normal keeps 760 at center`() {
        assertEquals(0, pressureNormalTick(760.0))
    }

    @Test
    fun `normal never leaves the scale`() {
        assertEquals(-3, pressureNormalTick(800.0))
        assertEquals(-3, pressureNormalTick(770.0))
        assertEquals(3, pressureNormalTick(700.0))
        assertEquals(3, pressureNormalTick(750.0))
    }

    @Test
    fun `normal occupies a whole tick`() {
        // 1 мм рт. ст. — уже отдельное деление, округления к нулю нет
        assertEquals(-1, pressureNormalTick(760.1))
        assertEquals(1, pressureNormalTick(759.9))
    }
}
