package com.example.omdb.di

import com.example.omdb.BuildConfig
import com.example.omdb.network.AuthInterceptor
import com.example.omdb.network.MoviesApiService
import com.example.omdb.network.RetryInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(BuildConfig.API_KEY))
            .addInterceptor(RetryInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideMoviesApiService(okHttpClient: OkHttpClient): MoviesApiService {
        return Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
            .create(MoviesApiService::class.java)
    }
}