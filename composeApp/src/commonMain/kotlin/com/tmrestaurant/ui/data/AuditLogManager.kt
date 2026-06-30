package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager

data class AuditEvent(
    val id: String = "AUD-${System.currentTimeMillis()}",
    val module: String,
    val action: String,
    val detail: String,
    val actorId: String = TurnoManager.currentUser?.id ?: "",
    val actorName: String = TurnoManager.currentUser?.name ?: "Sistema",
    val actorRole: String = TurnoManager.currentUser?.role?.name ?: "SYSTEM",
    val turnoId: String = TurnoManager.currentTurno?.id ?: "",
    val level: String = "INFO",
    val createdAt: Long = System.currentTimeMillis()
)

object AuditLogManager {
    private const val FILE = "audit_log.v1.tsv"

    var events by mutableStateOf<List<AuditEvent>>(emptyList())
        private set

    init {
        load()
    }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("audit_log")
    } catch (_: Exception) { false }

    fun log(module: String, action: String, detail: String, level: String = "INFO") {
        val event = AuditEvent(
            module = module,
            action = action,
            detail = detail,
            level = level
        )
        events = listOf(event) + events
        save()
    }

    fun recent(limit: Int = 50): List<AuditEvent> = events.take(limit)

    fun clear() {
        events = emptyList()
        if (isDbReady()) {
            try {
                DatabaseManager.deleteAll("audit_log")
            } catch (_: Exception) { }
        }
        PersistentFiles.writeText(FILE, "")
    }

    private fun esc(v: String) = v.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

    private fun descline(line: String): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        var escaping = false
        for (ch in line) {
            if (escaping) {
                cur.append(when (ch) { 'n' -> '\n'; 't' -> '\t'; 'r' -> '\r'; else -> ch })
                escaping = false
            } else when (ch) {
                '\\' -> escaping = true
                '\t' -> { result.add(cur.toString()); cur.clear() }
                else -> cur.append(ch)
            }
        }
        result.add(cur.toString())
        return result
    }

    private fun save() {
        val trimmed = events.take(500)
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("audit_log")
                    trimmed.forEach { event ->
                        DatabaseManager.insert("audit_log", mapOf(
                            "id" to event.id,
                            "module" to event.module,
                            "action" to event.action,
                            "detail" to event.detail,
                            "actor_id" to event.actorId,
                            "actor_name" to event.actorName,
                            "actor_role" to event.actorRole,
                            "turno_id" to event.turnoId,
                            "level" to event.level,
                            "created_at" to event.createdAt
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = trimmed.map { event ->
            listOf(
                event.id,
                esc(event.module),
                esc(event.action),
                esc(event.detail),
                esc(event.actorId),
                esc(event.actorName),
                esc(event.actorRole),
                esc(event.turnoId),
                esc(event.level),
                event.createdAt.toString()
            ).joinToString("\t")
        }
        PersistentFiles.writeText(FILE, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("audit_log", orderBy = "created_at DESC") { it }
                if (rows.isNotEmpty()) {
                    events = rows.map { row ->
                        AuditEvent(
                            id = row["id"] as? String ?: "",
                            module = row["module"] as? String ?: "",
                            action = row["action"] as? String ?: "",
                            detail = row["detail"] as? String ?: "",
                            actorId = row["actor_id"] as? String ?: "",
                            actorName = row["actor_name"] as? String ?: "Sistema",
                            actorRole = row["actor_role"] as? String ?: "SYSTEM",
                            turnoId = row["turno_id"] as? String ?: "",
                            level = row["level"] as? String ?: "INFO",
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE) ?: return
        if (text.isBlank()) return
        events = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val f = descline(line)
            if (f.size < 10) null else AuditEvent(
                id = f[0],
                module = f[1],
                action = f[2],
                detail = f[3],
                actorId = f[4],
                actorName = f[5],
                actorRole = f[6],
                turnoId = f[7],
                level = f[8],
                createdAt = f[9].toLongOrNull() ?: System.currentTimeMillis()
            )
        }
        if (events.isNotEmpty()) save()
    }
}
