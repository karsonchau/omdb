package com.example.omdb.di

import android.content.Context
import com.example.omdb.network.AndroidNetworkObserver
import com.example.omdb.BuildConfig
import com.example.omdb.data.MoviesRepository
import com.example.omdb.data.NetworkMoviesRepository
import com.example.omdb.network.MoviesApiService
import com.example.omdb.network.NetworkObserver
import com.example.omdb.network.RetryInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoviesApiService(): MoviesApiService {

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(BuildConfig.API_KEY))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
            .create(MoviesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMoviesRepository(moviesApiService: MoviesApiService): MoviesRepository {
        return NetworkMoviesRepository(moviesApiService)
    }

    @Provides
    @Singleton
    fun provideNetworkObserver(@ApplicationContext context: Context): NetworkObserver {
        return AndroidNetworkObserver(context)
    }
}