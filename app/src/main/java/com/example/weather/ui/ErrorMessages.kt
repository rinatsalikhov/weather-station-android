package com.example.weather.ui

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal fun Throwable.errorMessage(): String {
    val root = generateSequence(this) { it.cause }.last()
    val reason = when (root) {
        is UnknownHostException -> "Не удалось определить адрес сервера"
        is ConnectException -> "Нет связи с сервером: соединение не установлено"
        is SocketTimeoutException -> "Сервер не ответил: превышено время ожидания"
        is HttpException -> "Сервер вернул ошибку HTTP ${root.code()}"
        is SerializationException -> "Сервер вернул данные в неожиданном формате"
        is IOException -> "Ошибка сети"
        else -> "Не удалось получить данные с сервера"
    }
    return "$reason\n${describeChain()}\nIP телефона: ${deviceIpAddress()}"
}

private fun Throwable.describeChain(): String =
    generateSequence(this) { it.cause }
        .joinToString(separator = "\n") { "${it.javaClass.name}: ${it.message}" }

/** Локальный IPv4-адрес устройства (Wi-Fi/сеть), чтобы его было видно в сообщении об ошибке. */
internal fun deviceIpAddress(): String {
    return runCatching {
        NetworkInterface.getNetworkInterfaces()
            ?.toList()
            .orEmpty()
            .asSequence()
            .filter { iface -> runCatching { iface.isUp && !iface.isLoopback }.getOrDefault(false) }
            .flatMap { iface -> iface.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .filter { addr -> !addr.isLoopbackAddress && !addr.isLinkLocalAddress }
            .mapNotNull { addr -> addr.hostAddress?.takeIf { it.isNotBlank() } }
            .firstOrNull()
    }.getOrNull() ?: "недоступен"
}
