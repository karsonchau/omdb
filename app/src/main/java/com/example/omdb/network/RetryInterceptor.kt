package com.example.omdb.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptor(
    private val maxRetryAttempts: Int = 3,
    private val retryDelayMillis: Long = 1000
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response

        while (true) {
            try {
                response = chain.proceed(chain.request())

                // Check for status codes to retry on:
                if (shouldRetry(response.code) && attempt < maxRetryAttempts) {
                    attempt++
                    val delay = calculateRetryDelay(response)
                    TimeUnit.MILLISECONDS.sleep(delay)
                } else {
                    break
                }

            } catch (e: IOException) {
                // Handle network-related errors or timeouts
                if (attempt < maxRetryAttempts) {
                    attempt++
                    TimeUnit.MILLISECONDS.sleep(retryDelayMillis)
                } else {
                    throw e // If max retries exceeded, propagate exception
                }
            }
        }
        return response
    }

    private fun shouldRetry(statusCode: Int): Boolean {
        return when (statusCode) {
            502, 503, 504, 429 -> true  // Retry on 502, 503, 504, and 429 (rate limit)
            else -> false
        }
    }

    private fun calculateRetryDelay(response: Response): Long {
        // If Retry-After header is present, use it to determine delay (in seconds)
        val retryAfterHeader = response.headers["Retry-After"]
        return if (retryAfterHeader != null) {
            try {
                val retryAfter = retryAfterHeader.toLong()
                TimeUnit.SECONDS.toMillis(retryAfter)
            } catch (e: NumberFormatException) {
                retryDelayMillis
            }
        } else {
            retryDelayMillis
        }
    }
}