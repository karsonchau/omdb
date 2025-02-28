package com.example.omdb.model

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

interface CoroutineContextProvider {

    val main: CoroutineContext
        get() = Dispatchers.Main

    val io: CoroutineContext
        get() = Dispatchers.IO

    val default: CoroutineContext
        get() = Dispatchers.Default

    class Default: CoroutineContextProvider
}