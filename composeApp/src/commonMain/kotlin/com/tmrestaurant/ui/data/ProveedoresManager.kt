package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager

object ProveedoresManager {
    var proveedores by mutableStateOf<List<Proveedor>>(emptyList())
        private set

    init { load() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("proveedores")
    } catch (_: Exception) { false }

    fun add(p: Proveedor) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageSuppliers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_PROVEEDOR_CREAR", "Acceso denegado para crear proveedores", level = "WARN")
            return
        }
        proveedores = proveedores + p
        save()
    }
    fun update(p: Proveedor) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageSuppliers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_PROVEEDOR_EDITAR", "Acceso denegado para editar proveedores", level = "WARN")
            return
        }
        proveedores = proveedores.map { if (it.id == p.id) p.copy(updatedAt = System.currentTimeMillis()) else it }
        save()
    }
    fun delete(id: String) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageSuppliers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_PROVEEDOR_ELIMINAR", "Acceso denegado para eliminar proveedores", level = "WARN")
            return
        }
        proveedores = proveedores.filter { it.id != id }
        save()
    }

    private fun esc(v: String) = v.replace("\\","\\\\").replace("\n","\\n").replace("\t","\\t")
    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("proveedores")
                    proveedores.forEach { p ->
                        DatabaseManager.insert("proveedores", mapOf(
                            "id" to p.id,
                            "nombre" to p.nombre,
                            "rnc" to p.rnc,
                            "contacto" to p.contacto,
                            "telefono" to p.telefono,
                            "email" to p.email,
                            "direccion" to p.direccion,
                            "rubro" to p.rubro,
                            "uid" to p.uid,
                            "created_at" to p.createdAt,
                            "updated_at" to p.updatedAt
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = proveedores.map { p ->
            listOf(p.id, p.nombre, p.rnc, p.contacto, p.telefono, p.email, p.direccion, p.rubro, p.uid, p.createdAt.toString(), p.updatedAt.toString())
                .joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText("proveedores.v1.tsv", lines.joinToString("\n"))
    }
    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("proveedores") { it }
                if (rows.isNotEmpty()) {
                    proveedores = rows.map { row ->
                        Proveedor(
                            id = row["id"] as? String ?: "",
                            nombre = row["nombre"] as? String ?: "",
                            rnc = row["rnc"] as? String ?: "",
                            contacto = row["contacto"] as? String ?: "",
                            telefono = row["telefono"] as? String ?: "",
                            email = row["email"] as? String ?: "",
                            direccion = row["direccion"] as? String ?: "",
                            rubro = row["rubro"] as? String ?: "",
                            uid = row["uid"] as? String ?: genUid("prov"),
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText("proveedores.v1.tsv") ?: return
        proveedores = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val f = line.split("\t")
            if (f.size < 8) null
            else Proveedor(
                id = f[0], nombre = f[1], rnc = f[2], contacto = f[3], telefono = f[4], email = f[5], direccion = f[6], rubro = f[7],
                uid = f.getOrElse(8) { genUid("prov") },
                createdAt = f.getOrElse(9) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = f.getOrElse(10) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
            )
        }
        if (proveedores.isNotEmpty()) save()
    }
}

data class Proveedor(
    val id: String = "prov_${(10000..99999).random()}",
    val nombre: String = "",
    val rnc: String = "",
    val contacto: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val rubro: String = "",
    val uid: String = genUid("prov"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
