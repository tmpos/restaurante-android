package com.tmrestaurant.ui.data

import com.tmrestaurant.platform.appContext
import java.io.File

actual object PersistentFiles {
    actual fun readText(fileName: String): String? {
        val ctx = appContext ?: return null
        val file = File(ctx.filesDir, fileName)
        return runCatching { if (file.exists()) file.readText() else null }.getOrNull()
    }

    actual fun writeText(fileName: String, text: String) {
        val ctx = appContext ?: return
        runCatching {
            val file = File(ctx.filesDir, fileName)
            file.parentFile?.mkdirs()
            file.writeText(text)
        }
    }

    actual fun readBytes(fileName: String): ByteArray? {
        val ctx = appContext ?: return null
        val file = File(ctx.filesDir, fileName)
        return runCatching { if (file.exists()) file.readBytes() else null }.getOrNull()
    }

    actual fun writeBytes(fileName: String, bytes: ByteArray) {
        val ctx = appContext ?: return
        runCatching {
            val file = File(ctx.filesDir, fileName)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
        }
    }

    actual fun listFiles(): List<String> {
        val ctx = appContext ?: return emptyList()
        return runCatching { ctx.filesDir.listFiles()?.map { it.name }?.sorted() ?: emptyList() }
            .getOrDefault(emptyList())
    }

    actual fun deleteFile(fileName: String): Boolean {
        val ctx = appContext ?: return false
        return runCatching { File(ctx.filesDir, fileName).delete() }.getOrDefault(false)
    }
}
