package com.tmrestaurant.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

actual fun isNetworkAvailable(): Boolean {
    val ctx = appContext ?: return false
    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
