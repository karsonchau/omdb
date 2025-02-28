package com.example.omdb.di

import com.example.omdb.data.MoviesRepository
import com.example.omdb.mock.FakeMovieRepository
import com.example.omdb.mock.FakeNetworkObserver
import com.example.omdb.model.Result
import com.example.omdb.network.NetworkObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestModule {
    @Provides
    @Singleton
    fun provideMoviesRepository(): MoviesRepository {
        return FakeMovieRepository(result = Result.Failure(""))
    }

    @Provides
    @Singleton
    fun provideNetworkObserver(): NetworkObserver {
        return FakeNetworkObserver()
    }

    @Provides
    @Singleton
    fun provideContextProvider(): CoroutineContextProvider {
        return CoroutineContextProvider.Default()
    }
}

