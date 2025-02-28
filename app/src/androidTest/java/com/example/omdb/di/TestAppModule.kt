package com.example.omdb.di

import com.example.omdb.mock.FakeNetworkObserver
import com.example.omdb.model.CoroutineContextProvider
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
object TestAppModule {
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

