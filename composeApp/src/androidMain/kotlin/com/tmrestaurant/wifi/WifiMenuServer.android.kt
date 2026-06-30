package com.tmrestaurant.wifi

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

actual object WifiMenuServer {
    @Volatile actual var isRunning = false
    @Volatile actual var ipAddress = ""
    actual val port = 8585
    actual var onStatusChange: ((Boolean, String) -> Unit)? = null

    private var serverSocket: ServerSocket? = null
    private var serverThread: Thread? = null
    private val api = MenuApiHandler()
    private val posPort = 8586
    private var posServerSocket: ServerSocket? = null
    private var posServerThread: Thread? = null
    private val posApi = PosApiHandler()
    private val adminPort = 8587
    private var adminServerSocket: ServerSocket? = null
    private var adminServerThread: Thread? = null
    private val adminApi = AdminApiHandler()

    @Synchronized
    actual fun start() {
        if (isRunning) return
        try {
            serverSocket = ServerSocket(port)
            posServerSocket = ServerSocket(posPort)
            adminServerSocket = ServerSocket(adminPort)
            isRunning = true
            ipAddress = getLocalIp()
            onStatusChange?.invoke(true, ipAddress)
            serverThread = Thread {
                try {
                    while (isRunning) {
                        val socket = serverSocket?.accept() ?: break
                        Thread { handle(socket) }.apply { isDaemon = true }.start()
                    }
                } catch (_: Exception) {}
            }.apply { isDaemon = true; name = "WifiServer" }
            serverThread!!.start()
            posServerThread = Thread {
                try {
                    while (isRunning) {
                        val socket = posServerSocket?.accept() ?: break
                        Thread { handlePos(socket) }.apply { isDaemon = true }.start()
                    }
                } catch (_: Exception) {}
            }.apply { isDaemon = true; name = "PosWebServer" }
            posServerThread!!.start()
            adminServerThread = Thread {
                try {
                    while (isRunning) {
                        val socket = adminServerSocket?.accept() ?: break
                        Thread { handleAdmin(socket) }.apply { isDaemon = true }.start()
                    }
                } catch (_: Exception) {}
            }.apply { isDaemon = true; name = "AdminWebServer" }
            adminServerThread!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
            isRunning = false
            onStatusChange?.invoke(false, "")
        }
    }

    @Synchronized
    actual fun stop() {
        isRunning = false
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        serverThread?.interrupt()
        serverThread = null
        try { posServerSocket?.close() } catch (_: Exception) {}
        posServerSocket = null
        posServerThread?.interrupt()
        posServerThread = null
        try { adminServerSocket?.close() } catch (_: Exception) {}
        adminServerSocket = null
        adminServerThread?.interrupt()
        adminServerThread = null
        onStatusChange?.invoke(false, "")
    }

    private fun handlePos(socket: Socket) {
        try {
            socket.soTimeout = 10000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val line = reader.readLine() ?: return
            val parts = line.split(" ")
            if (parts.size < 2) return
            val method = parts[0]
            val path = parts[1]
            var body = ""
            var len = 0
            while (true) {
                val h = reader.readLine() ?: break
                if (h.isBlank()) break
                if (h.startsWith("Content-Length:", ignoreCase = true))
                    len = h.substringAfter(":").trim().toIntOrNull() ?: 0
            }
            if (len > 0) {
                val buf = CharArray(len)
                reader.read(buf); body = String(buf)
            }
            val res = posApi.route(HttpRequest(method, path, body))
            val out = socket.getOutputStream()
            val respBytes = res.bodyBytes ?: res.body.toByteArray(Charsets.UTF_8)
            val head = "HTTP/1.1 ${res.status} OK\r\nContent-Type: ${res.contentType}\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\nContent-Length: ${respBytes.size}\r\n\r\n"
            out.write(head.toByteArray(Charsets.UTF_8))
            out.write(respBytes)
            out.flush()
        } catch (_: Exception) {} finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun handleAdmin(socket: Socket) {
        try {
            socket.soTimeout = 10000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val line = reader.readLine() ?: return
            val parts = line.split(" ")
            if (parts.size < 2) return
            val method = parts[0]
            val path = parts[1]
            var body = ""
            var len = 0
            while (true) {
                val h = reader.readLine() ?: break
                if (h.isBlank()) break
                if (h.startsWith("Content-Length:", ignoreCase = true))
                    len = h.substringAfter(":").trim().toIntOrNull() ?: 0
            }
            if (len > 0) {
                val buf = CharArray(len)
                reader.read(buf); body = String(buf)
            }
            val res = adminApi.route(HttpRequest(method, path, body))
            val out = socket.getOutputStream()
            val respBytes = res.bodyBytes ?: res.body.toByteArray(Charsets.UTF_8)
            val head = "HTTP/1.1 ${res.status} OK\r\nContent-Type: ${res.contentType}\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\nContent-Length: ${respBytes.size}\r\n\r\n"
            out.write(head.toByteArray(Charsets.UTF_8))
            out.write(respBytes)
            out.flush()
        } catch (_: Exception) {} finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun handle(socket: Socket) {
        try {
            socket.soTimeout = 10000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val line = reader.readLine() ?: return
            val parts = line.split(" ")
            if (parts.size < 2) return
            val method = parts[0]
            val path = parts[1]

            var body = ""
            var len = 0
            while (true) {
                val h = reader.readLine() ?: break
                if (h.isBlank()) break
                if (h.startsWith("Content-Length:", ignoreCase = true))
                    len = h.substringAfter(":").trim().toIntOrNull() ?: 0
            }
            if (len > 0) {
                val buf = CharArray(len)
                reader.read(buf); body = String(buf)
            }

            val res = api.route(HttpRequest(method, path, body))
            val out = socket.getOutputStream()
            val respBytes = res.bodyBytes ?: res.body.toByteArray(Charsets.UTF_8)
            val head = "HTTP/1.1 ${res.status} OK\r\nContent-Type: ${res.contentType}\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\nContent-Length: ${respBytes.size}\r\n\r\n"
            out.write(head.toByteArray(Charsets.UTF_8))
            out.write(respBytes)
            out.flush()
        } catch (_: Exception) {} finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun getLocalIp(): String = try {
        NetworkInterface.getNetworkInterfaces()?.asSequence()
            ?.flatMap { it.inetAddresses.asSequence() }
            ?.find { !it.isLoopbackAddress && it is Inet4Address }
            ?.hostAddress ?: "0.0.0.0"
    } catch (_: Exception) { "0.0.0.0" }
}
