package com.tmrestaurant.cloud

expect class CloudHttpClient() {
    suspend fun get(path: String, bearerToken: String): CloudHttpResponse
    suspend fun post(path: String, bearerToken: String, body: String): CloudHttpResponse
    suspend fun put(path: String, bearerToken: String, body: String): CloudHttpResponse
}

data class CloudHttpResponse(val code: Int, val body: String, val ok: Boolean = code in 200..299)

fun buildJsonObject(vararg pairs: Pair<String, String>): String =
    pairs.joinToString(",", "{", "}") { (k, v) -> "\"$k\":$v" }

fun buildJsonString(key: String, value: String) = "\"$key\":\"${value.replace("\"", "\\\"")}\""

fun buildJsonNumber(key: String, value: Number) = "\"$key\":$value"

fun buildJsonArray(key: String, items: List<String>) = "\"$key\":[${items.joinToString(",")}]"
