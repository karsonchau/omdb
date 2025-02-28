package com.example.omdb.fake

import com.example.omdb.model.CoroutineContextProvider
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class TestCoroutineContextProvider: CoroutineContextProvider {

    override val main: CoroutineContext
        get() = Dispatchers.Unconfined

    override val io: CoroutineContext
        get() = Dispatchers.Unconfined

    override val default: CoroutineContext
        get() = Dispatchers.Unconfined
}