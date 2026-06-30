package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager

object ComandasManager {
    var activeComandas by mutableStateOf<List<Comanda>>(emptyList())
        private set

    init {
        load()
    }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("comandas")
    } catch (_: Exception) { false }

    fun enviarACocina(items: List<CartItem>, mesaName: String): Comanda {
        val turnoId = TurnoManager.currentTurno?.id ?: ""
        val comandaItems = items.map { ci ->
            ComandaItem(
                productName = ci.product.name,
                quantity = ci.quantity,
                notes = ci.extrasNote,
                courseType = ci.courseType
            )
        }
        val area = if (items.any { it.product.sendToBar }) "Bar" else "Cocina"
        val pending = activeComandas.find {
            it.mesaName == mesaName &&
                it.turnoId == turnoId &&
                it.status == ComandaStatus.Pendiente
        }
        if (pending != null) {
            val updated = pending.copy(
                items = comandaItems,
                area = area,
                updatedAt = System.currentTimeMillis()
            )
            activeComandas = activeComandas.map { if (it.id == pending.id) updated else it }
            save()
            return updated
        }
        val comanda = Comanda(
            mesaName = mesaName,
            items = comandaItems,
            turnoId = turnoId,
            area = area,
            createdAt = System.currentTimeMillis()
        )
        activeComandas = activeComandas + comanda
        save()
        return comanda
    }

    fun apartar(items: List<CartItem>, mesaName: String) {
        val comanda = Comanda(
            mesaName = mesaName,
            items = items.map { ComandaItem(productName = it.product.name, quantity = it.quantity, courseType = it.courseType) },
            turnoId = TurnoManager.currentTurno?.id ?: "",
            status = ComandaStatus.EnPreparacion
        )
        activeComandas = activeComandas + comanda
        save()
    }

    fun updateStatus(comandaId: String, newStatus: ComandaStatus) {
        activeComandas = activeComandas.map {
            if (it.id == comandaId) it.copy(status = newStatus, updatedAt = System.currentTimeMillis()) else it
        }
        save()
    }

    fun removeComanda(comandaId: String) {
        activeComandas = activeComandas.filter { it.id != comandaId }
        save()
    }

    fun clearTurnoComandas(turnoId: String) {
        activeComandas = activeComandas.filter { it.turnoId != turnoId }
        save()
    }

    private fun esc(v: String) = v.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t")

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("comandas")
                    DatabaseManager.deleteAll("comanda_items")
                    activeComandas.forEach { c ->
                        DatabaseManager.insert("comandas", mapOf(
                            "id" to c.id,
                            "mesa_name" to c.mesaName,
                            "status" to c.status.name,
                            "created_at" to c.createdAt,
                            "turno_id" to c.turnoId,
                            "area" to c.area,
                            "uid" to c.uid,
                            "updated_at" to c.updatedAt
                        ))
                        c.items.forEach { item ->
                            DatabaseManager.insert("comanda_items", mapOf(
                                "comanda_id" to c.id,
                                "product_name" to item.productName,
                                "quantity" to item.quantity,
                                "notes" to item.notes,
                                "course_type" to item.courseType
                            ))
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = activeComandas.map { c ->
            val itemsStr = c.items.joinToString(";") { it.productName + "|" + it.quantity + "|" + it.notes + "|" + it.courseType }
            listOf(c.id, c.mesaName, c.status.name, c.createdAt.toString(), c.turnoId, c.area, itemsStr, c.uid, c.updatedAt.toString())
                .joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText("comandas.v1.tsv", lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("comandas", orderBy = "created_at ASC") { it }
                if (rows.isNotEmpty()) {
                    val itemsMap = mutableMapOf<String, MutableList<ComandaItem>>()
                    try {
                        val itemRows = DatabaseManager.query("comanda_items") { it }
                        itemRows.forEach { row ->
                            val comandaId = row["comanda_id"] as? String ?: return@forEach
                            itemsMap.getOrPut(comandaId) { mutableListOf() }.add(
                                ComandaItem(
                                    productName = row["product_name"] as? String ?: "",
                                    quantity = ((row["quantity"] as? Long) ?: (row["quantity"] as? Int)?.toLong() ?: 1L).toInt(),
                                    notes = row["notes"] as? String ?: "",
                                    courseType = row["course_type"] as? String ?: ""
                                )
                            )
                        }
                    } catch (_: Exception) { }
                    activeComandas = rows.map { row ->
                        val id = row["id"] as? String ?: ""
                        Comanda(
                            id = id,
                            mesaName = row["mesa_name"] as? String ?: "General",
                            status = ComandaStatus.entries.firstOrNull { it.name == (row["status"] as? String) } ?: ComandaStatus.Pendiente,
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            turnoId = row["turno_id"] as? String ?: "",
                            area = row["area"] as? String ?: "Cocina",
                            items = itemsMap[id].orEmpty(),
                            uid = row["uid"] as? String ?: genUid("cmd"),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText("comandas.v1.tsv") ?: return
        val loaded = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val f = line.split("\t")
            if (f.size < 7) return@mapNotNull null
            val items = f[6].split(";").mapNotNull { part ->
                val p = part.split("|")
                if (p.size >= 2) ComandaItem(p[0], p[1].toIntOrNull() ?: 1, p.getOrElse(2) { "" }, p.getOrElse(3) { "" }) else null
            }
            Comanda(
                id = f[0], mesaName = f[1],
                status = ComandaStatus.entries.firstOrNull { it.name == f[2] } ?: ComandaStatus.Pendiente,
                createdAt = f[3].toLongOrNull() ?: System.currentTimeMillis(),
                turnoId = f[4], area = f[5], items = items,
                uid = f.getOrElse(7) { genUid("cmd") },
                updatedAt = f.getOrElse(8) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
            )
        }
        activeComandas = loaded
        if (activeComandas.isNotEmpty()) save()
    }
}
