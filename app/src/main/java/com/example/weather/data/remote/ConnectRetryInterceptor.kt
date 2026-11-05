package com.example.weather.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ConnectRetryInterceptor(
    private val maxAttempts: Int,
    private val retryDelayMillis: Long,
) : Interceptor {

    init {
        require(maxAttempts >= 1) { "maxAttempts must be >= 1" }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        repeat(maxAttempts) { attempt ->
            try {
                return chain.proceed(chain.request())
            } catch (e: IOException) {
                val isLastAttempt = attempt == maxAttempts - 1
                if (isLastAttempt || chain.call().isCanceled()) {
                    throw e
                }
                try {
                    Thread.sleep(retryDelayMillis)
                } catch (interrupted: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw e
                }
            }
        }
        error("unreachable: maxAttempts >= 1")
    }
}
