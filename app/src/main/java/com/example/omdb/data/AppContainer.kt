package com.example.omdb.data

import android.content.Context
import com.example.omdb.AndroidNetworkObserver
import com.example.omdb.BuildConfig
import com.example.omdb.network.MoviesApiService
import com.example.omdb.network.NetworkObserver
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit


interface AppContainer {
    val moviesRepository: MoviesRepository
    val networkObserver: NetworkObserver
}

class DefaultAppContainer(private val context: Context): AppContainer {
    private val baseUrl = "https://www.omdbapi.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(RetryInterceptor(BuildConfig.API_KEY))
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    private val retrofitService: MoviesApiService by lazy {
        retrofit.create(MoviesApiService::class.java)
    }

    override val moviesRepository: MoviesRepository by lazy {
        NetworkMoviesRepository(retrofitService)
    }

    override val networkObserver: NetworkObserver by lazy {
        AndroidNetworkObserver(context = context)
    }
}

class RetryInterceptor(
    private val apiKey: String,
    private val maxRetryAttempts: Int = 3,
    private val retryDelayMillis: Long = 1000
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response
        val originalRequest = chain.request()

        // Modify the URL to include the API key as a query parameter
        val modifiedUrl: HttpUrl = originalRequest.url.newBuilder()
            .addQueryParameter("apikey", apiKey) // Add the API key here
            .build()

        // Create a new request with the modified URL
        val modifiedRequest = originalRequest.newBuilder()
            .url(modifiedUrl)
            .build()

        while (true) {
            try {
                response = chain.proceed(modifiedRequest)

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