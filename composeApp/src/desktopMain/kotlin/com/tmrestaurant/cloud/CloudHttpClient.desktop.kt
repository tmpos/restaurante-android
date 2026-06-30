package com.tmrestaurant.cloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

actual class CloudHttpClient actual constructor() {
    actual suspend fun get(path: String, bearerToken: String, headers: Map<String, String>): CloudHttpResponse = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(path)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $bearerToken")
            conn.setRequestProperty("apikey", bearerToken)
            conn.setRequestProperty("Accept", "application/json")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            readResponse(conn)
        }.getOrElse { CloudHttpResponse(-1, it.message ?: "Error de red", false) }
    }

    actual suspend fun post(path: String, bearerToken: String, body: String, headers: Map<String, String>): CloudHttpResponse = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(path)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $bearerToken")
            conn.setRequestProperty("apikey", bearerToken)
            conn.setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream).use { it.write(body) }
            readResponse(conn)
        }.getOrElse { CloudHttpResponse(-1, it.message ?: "Error de red", false) }
    }

    actual suspend fun put(path: String, bearerToken: String, body: String, headers: Map<String, String>): CloudHttpResponse = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(path)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Authorization", "Bearer $bearerToken")
            conn.setRequestProperty("apikey", bearerToken)
            conn.setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream).use { it.write(body) }
            readResponse(conn)
        }.getOrElse { CloudHttpResponse(-1, it.message ?: "Error de red", false) }
    }

    private fun readResponse(conn: HttpURLConnection): CloudHttpResponse {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: ""
        return CloudHttpResponse(code, body)
    }
}
