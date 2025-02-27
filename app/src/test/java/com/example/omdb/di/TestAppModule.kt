package com.example.omdb.di

import com.example.omdb.data.MoviesRepository
import com.example.omdb.network.NetworkObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.Dispatchers
import org.mockito.Mockito
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideMoviesRepository(): MoviesRepository {
        return Mockito.mock(MoviesRepository::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkObserver(): NetworkObserver {
        return Mockito.mock(NetworkObserver::class.java)
    }

    @Provides
    @Singleton
    fun provideCoroutineContext(): CoroutineContextProvider {
        return TestCoroutineContextProvider()
    }
}


class TestCoroutineContextProvider: CoroutineContextProvider {

    override val main: CoroutineContext
        get() = Dispatchers.Unconfined

    override val io: CoroutineContext
        get() = Dispatchers.Unconfined

    override val default: CoroutineContext
        get() = Dispatchers.Unconfined
}