package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager

data class Extra(
    val id: String = "ext_${(10000..99999).random()}",
    val name: String = "",
    val price: Double = 0.0,
    val productId: Int = 0,
    val type: String = "extra",
    val uid: String = genUid("ext"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object ExtrasManager {
    var extras by mutableStateOf<List<Extra>>(emptyList())
        private set

    init { load() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("extras")
    } catch (_: Exception) { false }

    fun forProduct(productId: Int): List<Extra> = extras.filter { it.productId == productId }
    fun guarnicionesFor(productId: Int): List<Extra> = extras.filter { it.productId == productId && it.type == "guarnicion" }
    fun allGuarniciones(): List<Extra> = extras
        .filter { it.type == "guarnicion" }
        .distinctBy { it.name.trim().lowercase() }
    fun extrasFor(productId: Int): List<Extra> = extras.filter { it.productId == productId && it.type == "extra" }

    fun add(extra: Extra) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_EXTRA_CREAR", "Acceso denegado para crear extras", level = "WARN")
            return
        }
        extras = extras + extra; save()
    }
    fun update(extra: Extra) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_EXTRA_EDITAR", "Acceso denegado para editar extras", level = "WARN")
            return
        }
        extras = extras.map { if (it.id == extra.id) extra.copy(updatedAt = System.currentTimeMillis()) else it }; save()
    }
    fun delete(id: String) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_EXTRA_ELIMINAR", "Acceso denegado para eliminar extras", level = "WARN")
            return
        }
        extras = extras.filter { it.id != id }; save()
    }

    private fun esc(v: String) = v.replace("\\","\\\\").replace("\n","\\n").replace("\t","\\t")
    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("extras")
                    extras.forEach { e ->
                        DatabaseManager.insert("extras", mapOf(
                            "id" to e.id,
                            "name" to e.name,
                            "price" to e.price,
                            "product_id" to e.productId,
                            "type" to e.type,
                            "uid" to e.uid,
                            "created_at" to e.createdAt,
                            "updated_at" to e.updatedAt
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = extras.map { e ->
            listOf(e.id, e.name, e.price.toString(), e.productId.toString(), e.type, e.uid, e.createdAt.toString(), e.updatedAt.toString()).joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText("extras.v1.tsv", lines.joinToString("\n"))
    }
    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("extras") { it }
                if (rows.isNotEmpty()) {
                    val seenIds = mutableSetOf<String>()
                    extras = rows.mapNotNull { row ->
                        val eid = row["id"] as? String ?: ""
                        val id = if (eid.isBlank() || eid in seenIds) "ext_${(10000..99999).random()}" else eid
                        seenIds.add(id)
                        Extra(
                            id = id,
                            name = row["name"] as? String ?: "",
                            price = (row["price"] as? Double) ?: ((row["price"] as? Long)?.toDouble() ?: 0.0),
                            productId = ((row["product_id"] as? Long)?.toInt()) ?: 0,
                            type = row["type"] as? String ?: "extra",
                            uid = row["uid"] as? String ?: genUid("ext"),
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        extras = try {
            val text = PersistentFiles.readText("extras.v1.tsv") ?: return
            val loaded = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
                val f = line.split("\t")
                if (f.size < 4) return@mapNotNull null
                runCatching {
                    var id = f[0]
                    if (id.isBlank()) id = "ext_${(10000..99999).random()}"
                    val hasType = f.size >= 6 && f[4].length <= 12 && (f[4] == "extra" || f[4] == "guarnicion")
                    Extra(
                        id = id, name = f[1],
                        price = f[2].toDoubleOrNull() ?: 0.0,
                        productId = f[3].toIntOrNull() ?: 0,
                        type = if (hasType) f[4] else "extra",
                        uid = if (hasType) f.getOrElse(5) { genUid("ext") }
                              else f.getOrElse(4) { genUid("ext") },
                        createdAt = (if (hasType) f.getOrElse(6) { "" } else f.getOrElse(5) { "" })
                            .toLongOrNull() ?: System.currentTimeMillis(),
                        updatedAt = (if (hasType) f.getOrElse(7) { "" } else f.getOrElse(6) { "" })
                            .toLongOrNull() ?: System.currentTimeMillis()
                    )
                }.getOrNull()
            }
            val seenIds = mutableSetOf<String>()
            val fixed = loaded.filterNotNull().map { extra ->
                var changed = extra
                if (changed.id.isBlank() || changed.id in seenIds) {
                    changed = changed.copy(id = "ext_${(10000..99999).random()}")
                }
                seenIds.add(changed.id)
                changed
            }
            if (fixed != extras) save()
            if (fixed != extras) save()
            fixed
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
