package com.example.weather.data.remote

import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ConnectRetryInterceptorTest {

    private val request = Request.Builder().url("http://192.168.1.133/data").build()

    private fun okResponse() = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .build()

    private fun call(): Call = OkHttpClient().newCall(request)

    private class FakeChain(
        private val call: Call,
        private val request: Request,
        private val steps: Iterator<Result<Response>>,
    ) : Interceptor.Chain {
        var proceedCount = 0
            private set

        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            proceedCount++
            return steps.next().getOrThrow()
        }

        override fun connection(): Connection? = null

        override fun call(): Call = call

        override fun connectTimeoutMillis(): Int = 1000

        override fun readTimeoutMillis(): Int = 15_000

        override fun writeTimeoutMillis(): Int = 15_000

        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    }

    @Test
    fun `retries failed attempt and returns first success`() {
        val chain = FakeChain(
            call = call(),
            request = request,
            steps = listOf(
                Result.failure<Response>(SocketTimeoutException("failed to connect")),
                Result.success(okResponse()),
            ).iterator(),
        )

        val response = ConnectRetryInterceptor(
            maxAttempts = 10,
            retryDelayMillis = 1,
        ).intercept(chain)

        assertEquals(200, response.code)
        assertEquals(2, chain.proceedCount)
    }

    @Test
    fun `gives up after max attempts and throws last error`() {
        val chain = FakeChain(
            call = call(),
            request = request,
            steps = List(3) {
                Result.failure<Response>(SocketTimeoutException("failed to connect"))
            }.iterator(),
        )

        val error = assertThrows(IOException::class.java) {
            ConnectRetryInterceptor(maxAttempts = 3, retryDelayMillis = 1).intercept(chain)
        }

        assertEquals(3, chain.proceedCount)
        assertEquals("failed to connect", error.message)
    }

    @Test
    fun `does not retry canceled call`() {
        val chain = FakeChain(
            call = call(),
            request = request,
            steps = listOf(
                Result.failure<Response>(SocketTimeoutException("failed to connect")),
                Result.success(okResponse()),
            ).iterator(),
        )
        chain.call().cancel()

        assertThrows(IOException::class.java) {
            ConnectRetryInterceptor(maxAttempts = 10, retryDelayMillis = 1).intercept(chain)
        }

        assertEquals(1, chain.proceedCount)
    }
}
