package com.example.omdb.di

import com.example.omdb.data.MoviesRepository
import com.example.omdb.mock.FakeMovieRepository
import com.example.omdb.model.Result
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
object TestDataModule {
    @Provides
    @Singleton
    fun provideMoviesRepository(): MoviesRepository {
        return FakeMovieRepository(result = Result.Failure(""))
    }
}