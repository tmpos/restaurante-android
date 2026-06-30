package com.tmrestaurant.cloud

data class CloudConfig(
    val url: String = "",
    val publicKey: String = "",
    val secretKey: String = "",
    val mode: SyncMode = SyncMode.OFFLINE,
    val autoSync: Boolean = false,
    val intervalSec: Int = 30
)

enum class SyncMode { OFFLINE, ONLINE, AMBOS }

data class CloudStatus(
    val connected: Boolean = false,
    val error: String? = null,
    val projectName: String? = null,
    val projectUid: String? = null,
    val tableCount: Int? = null
)

data class SyncResult(
    val success: Boolean = false,
    val message: String = "",
    val inserts: Int = 0,
    val updates: Int = 0,
    val errors: Int = 0,
    val responseBody: String = ""
)

@Serializable
data class ColumnDef(
    val name: String,
    val type: String,
    val nullable: Boolean = true,
    val primary: Boolean = false
)

annotation class Serializable
