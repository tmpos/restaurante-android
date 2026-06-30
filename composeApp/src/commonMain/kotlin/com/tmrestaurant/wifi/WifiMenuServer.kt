package com.tmrestaurant.wifi

data class HttpRequest(val method: String, val path: String, val body: String = "")
data class HttpResponse(val status: Int = 200, val body: String = "", val contentType: String = "application/json", val bodyBytes: ByteArray? = null)

expect object WifiMenuServer {
    var isRunning: Boolean
    var ipAddress: String
    val port: Int
    var onStatusChange: ((Boolean, String) -> Unit)?

    fun start()
    fun stop()
}
