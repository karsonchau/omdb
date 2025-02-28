package com.example.omdb.di

import com.example.omdb.data.MoviesRepository
import com.example.omdb.data.NetworkMoviesRepository
import com.example.omdb.network.MoviesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideMoviesRepository(moviesApiService: MoviesApiService): MoviesRepository {
        return NetworkMoviesRepository(moviesApiService)
    }
}