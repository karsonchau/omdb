package com.example.omdb.mock

import com.example.omdb.network.NetworkObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkObserver: NetworkObserver {
    override var hasConnection: Boolean = true
    private val flow = MutableStateFlow(false)

    override val isConnected: Flow<Boolean>
        get() = flow
}