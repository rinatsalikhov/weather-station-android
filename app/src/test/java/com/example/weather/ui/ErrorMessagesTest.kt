package com.example.weather.ui

import com.example.weather.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMessagesTest {

    @Test
    fun `connect exception is reported as no connection`() {
        val phoneIp = deviceIpAddress()
        val ephemeralPort = java.net.ServerSocket(0).use { it.localPort }
        val error = ConnectException(
            "Failed to connect to /${BuildConfig.DEVICE_IP} (port 80) from /$phoneIp (port $ephemeralPort) after 15000 ms",
        )

        val message = error.errorMessage()

        assertTrue(message.startsWith("Нет связи с сервером"))
        assertTrue(message.contains("java.net.ConnectException"))
        assertTrue(message.contains("${BuildConfig.DEVICE_IP}"))
        assertTrue(message.contains("IP телефона: $phoneIp"))
    }

    @Test
    fun `root cause of wrapped exception is classified`() {
        val error = RuntimeException("request failed", UnknownHostException("${BuildConfig.DEVICE_IP}"))

        val message = error.errorMessage()

        assertTrue(message.startsWith("Не удалось определить адрес сервера"))
        assertTrue(message.contains("RuntimeException: request failed"))
        assertTrue(message.contains("UnknownHostException: ${BuildConfig.DEVICE_IP}"))
        assertTrue(message.contains("IP телефона:"))
    }

    @Test
    fun `timeout is reported as timeout`() {
        val message = SocketTimeoutException("connect timed out").errorMessage()

        assertTrue(message.startsWith("Сервер не ответил"))
        assertEquals(3, message.lines().size)
        assertTrue(message.contains("IP телефона:"))
    }

    @Test
    fun `unknown error falls back to exception details`() {
        val message = IllegalStateException("boom").errorMessage()

        assertTrue(message.startsWith("Не удалось получить данные"))
        assertTrue(message.contains("java.lang.IllegalStateException: boom"))
        assertTrue(message.contains("IP телефона:"))
    }
}
