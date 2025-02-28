package com.example.omdb.di

import android.content.Context
import com.example.omdb.network.AndroidNetworkObserver
import com.example.omdb.model.CoroutineContextProvider
import com.example.omdb.network.NetworkObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideContextProvider(): CoroutineContextProvider {
        return CoroutineContextProvider.Default()
    }

    @Provides
    @Singleton
    fun provideNetworkObserver(@ApplicationContext context: Context): NetworkObserver {
        return AndroidNetworkObserver(context)
    }
}