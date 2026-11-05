package com.example.weather.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMessagesTest {

    @Test
    fun `connect exception is reported as no connection`() {
        val error = ConnectException(
            "Failed to connect to /192.168.1.133 (port 80) from /192.168.1.166 (port 60874) after 15000 ms",
        )

        val message = error.errorMessage()

        assertTrue(message.startsWith("Нет связи с сервером"))
        assertTrue(message.contains("java.net.ConnectException"))
        assertTrue(message.contains("192.168.1.133"))
    }

    @Test
    fun `root cause of wrapped exception is classified`() {
        val error = RuntimeException("request failed", UnknownHostException("192.168.1.133"))

        val message = error.errorMessage()

        assertTrue(message.startsWith("Не удалось определить адрес сервера"))
        assertTrue(message.contains("RuntimeException: request failed"))
        assertTrue(message.contains("UnknownHostException: 192.168.1.133"))
    }

    @Test
    fun `timeout is reported as timeout`() {
        val message = SocketTimeoutException("connect timed out").errorMessage()

        assertTrue(message.startsWith("Сервер не ответил"))
        assertEquals(2, message.lines().size)
    }

    @Test
    fun `unknown error falls back to exception details`() {
        val message = IllegalStateException("boom").errorMessage()

        assertTrue(message.startsWith("Не удалось получить данные"))
        assertTrue(message.contains("java.lang.IllegalStateException: boom"))
    }
}
