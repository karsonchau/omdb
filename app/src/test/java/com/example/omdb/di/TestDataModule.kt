package com.example.omdb.di

import com.example.omdb.data.MoviesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito
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
        return Mockito.mock(MoviesRepository::class.java)
    }
}