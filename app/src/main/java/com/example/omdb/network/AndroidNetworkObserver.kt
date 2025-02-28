package com.example.omdb.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidNetworkObserver(context: Context) : NetworkObserver {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!

    override val isConnected: Flow<Boolean>
        get() = callbackFlow {
            val callback = object : NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val connected =
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    trySend(connected)
                    hasConnection = connected
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    trySend(false)
                    hasConnection = false
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    trySend(false)
                    hasConnection = false
                }

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    trySend(true)
                    hasConnection = true
                }

            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    override var hasConnection: Boolean = false
        private set
}