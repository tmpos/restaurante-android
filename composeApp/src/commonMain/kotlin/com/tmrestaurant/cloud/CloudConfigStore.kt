package com.tmrestaurant.cloud

import com.tmrestaurant.ui.data.PersistentFiles

object CloudConfigStore {
    private const val FILE_NAME = "tmcloud.v1.props"

    fun load(): CloudConfig {
        val values = PersistentFiles.readText(FILE_NAME)
            ?.lineSequence()
            ?.mapNotNull { line ->
                val separator = line.indexOf('=')
                if (separator <= 0) null else line.substring(0, separator) to unescape(line.substring(separator + 1))
            }
            ?.toMap()
            .orEmpty()

        return CloudConfig(
            url = values["url"].orEmpty(),
            publicKey = values["publicKey"].orEmpty(),
            secretKey = values["secretKey"].orEmpty(),
            mode = SyncMode.entries.firstOrNull { it.name == values["mode"] } ?: SyncMode.AMBOS,
            autoSync = values["autoSync"]?.toBooleanStrictOrNull() ?: false,
            intervalSec = values["intervalSec"]?.toIntOrNull()?.coerceIn(10, 86400) ?: 30
        )
    }

    fun save(config: CloudConfig) {
        val values = listOf(
            "url" to config.url.trim().trimEnd('/'),
            "publicKey" to config.publicKey.trim(),
            "secretKey" to config.secretKey.trim(),
            "mode" to config.mode.name,
            "autoSync" to config.autoSync.toString(),
            "intervalSec" to config.intervalSec.coerceIn(10, 86400).toString()
        )
        PersistentFiles.writeText(FILE_NAME, values.joinToString("\n") { "${it.first}=${escape(it.second)}" })
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\n", "\\n").replace("=", "\\e")

    private fun unescape(value: String): String {
        val result = StringBuilder()
        var escaped = false
        value.forEach { char ->
            if (escaped) {
                result.append(if (char == 'n') '\n' else if (char == 'e') '=' else char)
                escaped = false
            } else if (char == '\\') {
                escaped = true
            } else {
                result.append(char)
            }
        }
        if (escaped) result.append('\\')
        return result.toString()
    }
}
