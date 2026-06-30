package com.tmrestaurant.ui.data

import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.settings.BackupItem

object BackupManager {
    private const val PREFIX = "backup_"
    private const val EXT = ".tmbak"
    private const val HEADER = "TM_BACKUP_V1"
    private const val MAX_AUTO_BACKUPS = 10

    fun listBackups(): List<BackupItem> {
        return PersistentFiles.listFiles()
            .filter { it.startsWith(PREFIX) && it.endsWith(EXT) }
            .mapNotNull { fileName ->
                val raw = PersistentFiles.readText(fileName) ?: return@mapNotNull null
                val lines = raw.lines()
                if (lines.isEmpty() || lines.firstOrNull() != HEADER) return@mapNotNull null
                val createdAt = lines.getOrNull(1)?.toLongOrNull() ?: extractTimestampFromName(fileName)
                val sizeKb = (raw.encodeToByteArray().size / 1024.0 * 100.0).toInt() / 100.0
                BackupItem(
                    id = createdAt.toInt(),
                    fileName = fileName,
                    date = formatDateTime(createdAt),
                    sizeKb = sizeKb
                )
            }
            .sortedByDescending { it.id }
    }

    fun createBackup(label: String = "manual"): BackupItem {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageBackups(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_BACKUP_CREAR", "Acceso denegado para crear backup", level = "WARN")
            throw IllegalStateException("Acceso denegado")
        }
        val fileName = writeBackup(label)
        AuditLogManager.log("Backups", "CREAR_BACKUP", fileName)
        return listBackups().first { it.fileName == fileName }
    }

    fun createAutomaticBackup(label: String): BackupItem? = try {
        SystemActionContext.runPrivileged {
            val created = createBackup("auto-$label")
            pruneAutomaticBackups()
            created
        }
    } catch (e: Throwable) {
        AuditLogManager.log("Backups", "ERROR_BACKUP_AUTOMATICO", "${label}: ${e.message.orEmpty()}", level = "ERROR")
        null
    }

    fun restoreBackup(fileName: String): Boolean {
        if (!AccessControl.canManageBackups(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_BACKUP_RESTAURAR", "Acceso denegado para restaurar backup", level = "WARN")
            return false
        }
        val raw = PersistentFiles.readText(fileName) ?: return false
        val lines = raw.lines()
        if (lines.isEmpty() || lines.firstOrNull() != HEADER) return false

        val entries = lines.drop(2)
            .filter { it.startsWith("FILE\t") }
            .mapNotNull { line ->
                val parts = line.split("\t")
                if (parts.size < 3) null else unescape(parts[1]) to parts[2].hexToBytes()
            }

        if (entries.isEmpty()) return false

        PersistentFiles.listFiles()
            .filterNot { it.startsWith(PREFIX) && it.endsWith(EXT) }
            .forEach { PersistentFiles.deleteFile(it) }

        entries.forEach { (name, bytes) ->
            PersistentFiles.writeBytes(name, bytes)
        }
        AuditLogManager.log("Backups", "RESTAURAR_BACKUP", fileName, level = "WARN")
        return true
    }

    fun deleteBackup(fileName: String): Boolean {
        if (!AccessControl.canManageBackups(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_BACKUP_ELIMINAR", "Acceso denegado para eliminar backup", level = "WARN")
            return false
        }
        val deleted = PersistentFiles.deleteFile(fileName)
        if (deleted) {
            AuditLogManager.log("Backups", "ELIMINAR_BACKUP", fileName, level = "WARN")
        }
        return deleted
    }

    fun readBackupText(fileName: String): String? = PersistentFiles.readText(fileName)

    private fun writeBackup(label: String): String {
        val timestamp = System.currentTimeMillis()
        val safeLabel = label.lowercase().replace(Regex("[^a-z0-9_-]"), "-")
        val fileName = "${PREFIX}${safeLabel}_${timestamp}${EXT}"
        val payload = buildString {
            appendLine(HEADER)
            appendLine(timestamp.toString())
            PersistentFiles.listFiles()
                .filterNot { it.startsWith(PREFIX) && it.endsWith(EXT) }
                .sorted()
                .forEach { sourceName ->
                    val bytes = PersistentFiles.readBytes(sourceName) ?: ByteArray(0)
                    append("FILE\t")
                    append(escape(sourceName))
                    append('\t')
                    append(bytes.toHex())
                    appendLine()
                }
        }
        PersistentFiles.writeText(fileName, payload)
        return fileName
    }

    private fun pruneAutomaticBackups() {
        PersistentFiles.listFiles()
            .filter { it.startsWith("${PREFIX}auto-") && it.endsWith(EXT) }
            .sortedByDescending { extractTimestampFromName(it) }
            .drop(MAX_AUTO_BACKUPS)
            .forEach { PersistentFiles.deleteFile(it) }
    }

    private fun extractTimestampFromName(fileName: String): Long {
        return fileName.removePrefix(PREFIX).removeSuffix(EXT).substringAfterLast('_').toLongOrNull()
            ?: System.currentTimeMillis()
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

    private fun unescape(value: String): String =
        value.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\")

    private fun ByteArray.toHex(): String = joinToString("") { byte ->
        val hex = (byte.toInt() and 0xFF).toString(16)
        if (hex.length == 1) "0$hex" else hex
    }

    private fun String.hexToBytes(): ByteArray {
        if (isBlank()) return ByteArray(0)
        val out = ByteArray(length / 2)
        var i = 0
        while (i < length) {
            out[i / 2] = substring(i, i + 2).toInt(16).toByte()
            i += 2
        }
        return out
    }
}
