package com.example.omdb.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * A custom OkHttp interceptor that automatically retries requests if they fail with certain status codes or due to network-related issues.
 *
 * @param maxRetryAttempts The maximum number of retry attempts. Default is 3.
 * @param retryDelayMillis The delay between retry attempts in milliseconds. Default is 1000ms.
 */
class RetryInterceptor(
    private val maxRetryAttempts: Int = 3,
    private val retryDelayMillis: Long = 1000
) : Interceptor {

    /**
     * Intercepts the request and retries if it encounters network-related failures or specific HTTP status codes.
     *
     * @param chain The interceptor chain used to process the request.
     * @return The response from the request, either after successful retry or a failed attempt.
     * @throws IOException If the maximum retry attempts are exceeded or network issues persist.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response

        while (true) {
            try {
                // Proceed with the request
                response = chain.proceed(chain.request())

                // Check if retry is needed based on status code and retry limit
                if (shouldRetry(response.code) && attempt < maxRetryAttempts) {
                    attempt++
                    val delay = calculateRetryDelay(response)
                    // Wait for the calculated delay before retrying
                    TimeUnit.MILLISECONDS.sleep(delay)
                } else {
                    break
                }

            } catch (e: IOException) {
                // Handle network-related errors or timeouts
                if (attempt < maxRetryAttempts) {
                    attempt++
                    // Wait before retrying
                    TimeUnit.MILLISECONDS.sleep(retryDelayMillis)
                } else {
                    // If max retries exceeded, propagate the exception
                    throw e
                }
            }
        }
        return response
    }

    /**
     * Determines whether a request should be retried based on its HTTP status code.
     *
     * @param statusCode The HTTP status code returned in the response.
     * @return `true` if the request should be retried based on the status code, otherwise `false`.
     */
    private fun shouldRetry(statusCode: Int): Boolean {
        return when (statusCode) {
            502, 503, 504, 429 -> true  // Retry on 502 (Bad Gateway), 503 (Service Unavailable), 504 (Gateway Timeout), and 429 (Too Many Requests)
            else -> false
        }
    }

    /**
     * Calculates the delay before retrying the request.
     * If the "Retry-After" header is present, it uses that value. Otherwise, a default delay is used.
     *
     * @param response The HTTP response.
     * @return The calculated delay in milliseconds before the next retry attempt.
     */
    private fun calculateRetryDelay(response: Response): Long {
        // If Retry-After header is present, use it to determine delay (in seconds)
        val retryAfterHeader = response.headers["Retry-After"]
        return if (retryAfterHeader != null) {
            try {
                val retryAfter = retryAfterHeader.toLong()
                TimeUnit.SECONDS.toMillis(retryAfter)  // Convert Retry-After to milliseconds
            } catch (e: NumberFormatException) {
                // In case of parsing failure, fall back to default retry delay
                retryDelayMillis
            }
        } else {
            retryDelayMillis  // Use default delay if Retry-After is not available
        }
    }
}
