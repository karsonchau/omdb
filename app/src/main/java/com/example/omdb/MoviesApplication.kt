package com.example.omdb

import android.app.Application
import com.example.omdb.data.AppContainer
import com.example.omdb.data.DefaultAppContainer

class MoviesApplication: Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}