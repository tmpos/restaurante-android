package com.tmrestaurant.ui.data

import androidx.compose.runtime.mutableStateListOf
import com.tmrestaurant.db.DatabaseManager

data class InventoryAdjustment(
    val id: String = genUid("adj"),
    val productId: Int,
    val productName: String = "",
    val previousStock: Int = 0,
    val newStock: Int = 0,
    val delta: Int = 0,
    val reason: String = "",
    val userId: String = "",
    val userName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

object InventoryAdjustmentManager {
    private const val FILE = "inventory_adjustments.v1.tsv"
    val adjustments = mutableStateListOf<InventoryAdjustment>()

    init { load() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("inventory_adjustments")
    } catch (_: Exception) { false }

    fun log(productId: Int, productName: String, previousStock: Int, newStock: Int, reason: String) {
        val user = TurnoManager.currentUser
        adjustments.add(0, InventoryAdjustment(
            productId = productId, productName = productName,
            previousStock = previousStock, newStock = newStock,
            delta = newStock - previousStock, reason = reason,
            userId = user?.id ?: "", userName = user?.name ?: ""
        ))
        save()
    }

    fun clear() {
        adjustments.clear()
        save()
    }

    private fun esc(v: String) = v.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
    private fun descline(line: String): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        var escaping = false
        for (ch in line) {
            if (escaping) { cur.append(when (ch) { 'n' -> '\n'; 't' -> '\t'; 'r' -> '\r'; else -> ch }); escaping = false }
            else when (ch) { '\\' -> escaping = true; '\t' -> { result.add(cur.toString()); cur.clear() }; else -> cur.append(ch) }
        }
        result.add(cur.toString())
        return result
    }

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("inventory_adjustments")
                    adjustments.forEach { a ->
                        DatabaseManager.insert("inventory_adjustments", mapOf(
                            "id" to a.id,
                            "product_id" to a.productId,
                            "product_name" to a.productName,
                            "previous_stock" to a.previousStock,
                            "new_stock" to a.newStock,
                            "delta" to a.delta,
                            "reason" to a.reason,
                            "user_id" to a.userId,
                            "user_name" to a.userName,
                            "created_at" to a.createdAt
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = adjustments.map { a ->
            listOf(a.id, a.productId.toString(), esc(a.productName), a.previousStock.toString(), a.newStock.toString(), a.delta.toString(), esc(a.reason), esc(a.userId), esc(a.userName), a.createdAt.toString())
                .joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText(FILE, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("inventory_adjustments", orderBy = "created_at DESC") { it }
                if (rows.isNotEmpty()) {
                    adjustments.clear()
                    rows.forEach { row ->
                        adjustments.add(InventoryAdjustment(
                            id = row["id"] as? String ?: "",
                            productId = ((row["product_id"] as? Long) ?: (row["product_id"] as? Int)?.toLong() ?: 0L).toInt(),
                            productName = row["product_name"] as? String ?: "",
                            previousStock = ((row["previous_stock"] as? Long) ?: 0L).toInt(),
                            newStock = ((row["new_stock"] as? Long) ?: 0L).toInt(),
                            delta = ((row["delta"] as? Long) ?: 0L).toInt(),
                            reason = row["reason"] as? String ?: "",
                            userId = row["user_id"] as? String ?: "",
                            userName = row["user_name"] as? String ?: "",
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis()
                        ))
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE) ?: return
        if (text.isBlank()) return
        adjustments.clear()
        text.lines().filter { it.isNotBlank() }.forEach { line ->
            val f = descline(line)
            if (f.size < 7) return@forEach
            adjustments.add(InventoryAdjustment(
                id = f[0], productId = f[1].toIntOrNull() ?: return@forEach,
                productName = f[2], previousStock = f[3].toIntOrNull() ?: 0,
                newStock = f[4].toIntOrNull() ?: 0, delta = f[5].toIntOrNull() ?: 0,
                reason = f[6], userId = f.getOrElse(7) { "" },
                userName = f.getOrElse(8) { "" },
                createdAt = f.getOrElse(9) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
            ))
        }
        if (adjustments.isNotEmpty()) save()
    }
}
