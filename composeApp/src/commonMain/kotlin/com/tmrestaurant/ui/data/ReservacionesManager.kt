package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager

data class Reservacion(
    val id: String = genUid("res"),
    val clienteNombre: String = "",
    val clienteTelefono: String = "",
    val clientePersonas: Int = 1,
    val fecha: String = "",
    val hora: String = "",
    val notas: String = "",
    val mesaId: Int? = null,
    val estado: String = "PENDIENTE",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object ReservacionesManager {
    var reservaciones by mutableStateOf<List<Reservacion>>(emptyList())
        private set

    init { load() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("reservaciones")
    } catch (_: Exception) { false }

    fun add(reservacion: Reservacion) {
        if (!AccessControl.canManageReservations(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_RESERVACION_CREAR", "Acceso denegado para crear reservaciones", level = "WARN")
            return
        }
        reservaciones = reservaciones + reservacion
        save()
    }

    fun update(updated: Reservacion) {
        if (!AccessControl.canManageReservations(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_RESERVACION_EDITAR", "Acceso denegado para editar reservaciones", level = "WARN")
            return
        }
        reservaciones = reservaciones.map { if (it.id == updated.id) updated.copy(updatedAt = System.currentTimeMillis()) else it }
        save()
    }

    fun delete(id: String) {
        if (!AccessControl.canDeleteReservations(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_RESERVACION_ELIMINAR", "Acceso denegado para eliminar reservaciones", level = "WARN")
            return
        }
        reservaciones = reservaciones.filter { it.id != id }
        save()
    }

    fun cambiarEstado(id: String, nuevoEstado: String) {
        if (!AccessControl.canManageReservations(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_RESERVACION_ESTADO", "Acceso denegado para cambiar estado de reservaciones", level = "WARN")
            return
        }
        reservaciones = reservaciones.map { if (it.id == id) it.copy(estado = nuevoEstado, updatedAt = System.currentTimeMillis()) else it }
        save()
    }

    fun pendientes(): List<Reservacion> = reservaciones.filter { it.estado == "PENDIENTE" }

    fun activas(): List<Reservacion> = reservaciones.filter { it.estado in listOf("PENDIENTE", "CONFIRMADA") }

    private fun esc(v: String) = v.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t")

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("reservaciones")
                    reservaciones.forEach { r ->
                        DatabaseManager.insert("reservaciones", mapOf(
                            "id" to r.id,
                            "cliente_nombre" to r.clienteNombre,
                            "cliente_telefono" to r.clienteTelefono,
                            "cliente_personas" to r.clientePersonas,
                            "fecha" to r.fecha,
                            "hora" to r.hora,
                            "notas" to r.notas,
                            "mesa_id" to r.mesaId,
                            "estado" to r.estado,
                            "created_at" to r.createdAt,
                            "updated_at" to r.updatedAt
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = reservaciones.map { r ->
            listOf(r.id, r.clienteNombre, r.clienteTelefono, r.clientePersonas.toString(), r.fecha, r.hora, r.notas, r.mesaId?.toString() ?: "", r.estado, r.createdAt.toString(), r.updatedAt.toString())
                .joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText("reservaciones.v1.tsv", lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("reservaciones", orderBy = "created_at DESC") { it }
                if (rows.isNotEmpty()) {
                    reservaciones = rows.map { row ->
                        Reservacion(
                            id = row["id"] as? String ?: "",
                            clienteNombre = row["cliente_nombre"] as? String ?: "",
                            clienteTelefono = row["cliente_telefono"] as? String ?: "",
                            clientePersonas = ((row["cliente_personas"] as? Long) ?: 1L).toInt(),
                            fecha = row["fecha"] as? String ?: "",
                            hora = row["hora"] as? String ?: "",
                            notas = row["notas"] as? String ?: "",
                            mesaId = (row["mesa_id"] as? Long)?.toInt(),
                            estado = row["estado"] as? String ?: "PENDIENTE",
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText("reservaciones.v1.tsv") ?: return
        reservaciones = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val f = line.split("\t")
            if (f.size < 9) null
            else Reservacion(
                id = f[0], clienteNombre = f[1], clienteTelefono = f[2],
                clientePersonas = f[3].toIntOrNull() ?: 1, fecha = f[4], hora = f[5],
                notas = f[6], mesaId = f[7].toIntOrNull(), estado = f[8],
                createdAt = f.getOrElse(9) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = f.getOrElse(10) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
            )
        }
        if (reservaciones.isNotEmpty()) save()
    }
}
