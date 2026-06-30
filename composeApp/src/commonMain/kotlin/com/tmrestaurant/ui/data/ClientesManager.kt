package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager

object ClientesManager {
    var clientes by mutableStateOf<List<Cliente>>(emptyList())
        private set

    init {
        load()
    }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("clientes")
    } catch (_: Exception) { false }

    fun add(cliente: Cliente) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageCustomers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CLIENTE_CREAR", "Acceso denegado para crear clientes", level = "WARN")
            return
        }
        clientes = clientes + cliente
        save()
    }

    fun update(updated: Cliente) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageCustomers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CLIENTE_EDITAR", "Acceso denegado para editar clientes", level = "WARN")
            return
        }
        clientes = clientes.map { if (it.id == updated.id) updated.copy(updatedAt = System.currentTimeMillis()) else it }
        save()
    }

    fun delete(id: String) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canDeleteCustomers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CLIENTE_ELIMINAR", "Acceso denegado para eliminar clientes", level = "WARN")
            return
        }
        clientes = clientes.filter { it.id != id }
        save()
    }

    fun search(query: String): List<Cliente> {
        val q = query.lowercase()
        return clientes.filter {
            it.nombre.lowercase().contains(q) ||
            it.rnc.lowercase().contains(q) ||
            it.telefono.contains(q)
        }
    }

    fun registerPurchase(clientId: String, amount: Double) {
        if (clientId.isBlank() || amount <= 0.0) return
        val index = clientes.indexOfFirst { it.id == clientId }
        if (index < 0) return
        val earned = (amount / 100.0).toInt().coerceAtLeast(1)
        val current = clientes[index]
        clientes = clientes.mapIndexed { i, cliente ->
            if (i == index) {
                cliente.copy(
                    loyaltyPoints = cliente.loyaltyPoints + earned,
                    totalSpent = cliente.totalSpent + amount,
                    updatedAt = System.currentTimeMillis()
                )
            } else cliente
        }
        save()
        AuditLogManager.log("Fidelizacion", "SUMAR_PUNTOS", "${current.nombre}: +$earned puntos por RD\$ ${"%,.2f".format(amount)}")
    }

    private fun esc(v: String) = v.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t")

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("clientes")
                    clientes.forEach { c ->
                        DatabaseManager.insert("clientes", mapOf(
                            "id" to c.id,
                            "nombre" to c.nombre,
                            "rnc" to c.rnc,
                            "telefono" to c.telefono,
                            "email" to c.email,
                            "direccion" to c.direccion,
                            "tipo" to c.tipo,
                            "limite_credito" to c.limiteCredito,
                            "loyalty_points" to c.loyaltyPoints,
                            "total_spent" to c.totalSpent,
                            "uid" to c.uid,
                            "created_at" to c.createdAt,
                            "updated_at" to c.updatedAt
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = clientes.map { c ->
            listOf(
                c.id, c.nombre, c.rnc, c.telefono, c.email, c.direccion, c.tipo,
                c.limiteCredito.toString(), c.uid, c.createdAt.toString(), c.updatedAt.toString(),
                c.loyaltyPoints.toString(), c.totalSpent.toString()
            )
                .joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText("clientes.v1.tsv", lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("clientes") { it }
                if (rows.isNotEmpty()) {
                    clientes = rows.map { row ->
                        Cliente(
                            id = row["id"] as? String ?: "",
                            nombre = row["nombre"] as? String ?: "",
                            rnc = row["rnc"] as? String ?: "",
                            telefono = row["telefono"] as? String ?: "",
                            email = row["email"] as? String ?: "",
                            direccion = row["direccion"] as? String ?: "",
                            tipo = row["tipo"] as? String ?: "Consumidor Final",
                            limiteCredito = (row["limite_credito"] as? Double) ?: ((row["limite_credito"] as? Long)?.toDouble() ?: 0.0),
                            uid = row["uid"] as? String ?: genUid("cli"),
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis(),
                            loyaltyPoints = (row["loyalty_points"] as? Long)?.toInt() ?: 0,
                            totalSpent = (row["total_spent"] as? Double) ?: ((row["total_spent"] as? Long)?.toDouble() ?: 0.0)
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText("clientes.v1.tsv") ?: return
        clientes = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val f = line.split("\t")
            if (f.size < 9) null
            else Cliente(
                id = f[0], nombre = f[1], rnc = f[2], telefono = f[3], email = f[4],
                direccion = f[5], tipo = f[6], limiteCredito = f[7].toDoubleOrNull() ?: 0.0,
                uid = f.getOrElse(8) { genUid("cli") },
                createdAt = f.getOrElse(9) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = f.getOrElse(10) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                loyaltyPoints = f.getOrElse(11) { "0" }.toIntOrNull() ?: 0,
                totalSpent = f.getOrElse(12) { "0" }.toDoubleOrNull() ?: 0.0
            )
        }
        if (clientes.isNotEmpty()) save()
    }
}
