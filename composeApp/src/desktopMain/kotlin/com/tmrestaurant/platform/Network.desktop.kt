package com.tmrestaurant.platform

import java.net.InetSocketAddress
import java.net.Socket

actual fun isNetworkAvailable(): Boolean {
    return try {
        val socket = Socket()
        socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
        socket.close()
        true
    } catch (_: Exception) {
        false
    }
}
