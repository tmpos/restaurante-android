package com.tmrestaurant.ui.data

import java.io.File

actual object PersistentFiles {
    private val dataDir: File
        get() = File(System.getProperty("user.home"), ".tmrestaurant")

    actual fun readText(fileName: String): String? {
        val file = File(dataDir, fileName)
        return runCatching { if (file.exists()) file.readText() else null }.getOrNull()
    }

    actual fun writeText(fileName: String, text: String) {
        runCatching {
            dataDir.mkdirs()
            File(dataDir, fileName).writeText(text)
        }
    }

    actual fun readBytes(fileName: String): ByteArray? {
        val file = File(dataDir, fileName)
        return runCatching { if (file.exists()) file.readBytes() else null }.getOrNull()
    }

    actual fun writeBytes(fileName: String, bytes: ByteArray) {
        runCatching {
            dataDir.mkdirs()
            File(dataDir, fileName).writeBytes(bytes)
        }
    }

    actual fun listFiles(): List<String> =
        runCatching { dataDir.listFiles()?.map { it.name }?.sorted() ?: emptyList() }
            .getOrDefault(emptyList())

    actual fun deleteFile(fileName: String): Boolean =
        runCatching { File(dataDir, fileName).delete() }.getOrDefault(false)
}
