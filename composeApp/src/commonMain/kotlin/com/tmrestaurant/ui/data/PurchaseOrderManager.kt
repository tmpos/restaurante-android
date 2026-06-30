package com.tmrestaurant.ui.data

import androidx.compose.runtime.mutableStateListOf
import com.tmrestaurant.db.DatabaseManager

data class PurchaseOrderItem(
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val subtotal: Double get() = quantity * unitPrice
}

data class PurchaseOrder(
    val id: String = genUid("po"),
    val providerName: String,
    val items: List<PurchaseOrderItem> = emptyList(),
    val status: String = "PENDIENTE",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val total: Double get() = items.sumOf { it.subtotal }
}

object PurchaseOrderManager {
    private const val FILE = "purchase_orders.v1.tsv"
    val orders = mutableStateListOf<PurchaseOrder>()

    init { load() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("purchase_orders")
    } catch (_: Exception) { false }

    fun addOrUpdate(order: PurchaseOrder) {
        if (!AccessControl.canManagePurchases(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_COMPRA_EDITAR", "Acceso denegado para crear o editar ordenes de compra", level = "WARN")
            return
        }
        val idx = orders.indexOfFirst { it.id == order.id }
        if (idx >= 0) {
            orders[idx] = order.copy(updatedAt = System.currentTimeMillis())
        } else {
            orders.add(order)
        }
        save()
    }

    fun remove(orderId: String) {
        if (!AccessControl.canManagePurchases(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_COMPRA_ELIMINAR", "Acceso denegado para eliminar ordenes de compra", level = "WARN")
            return
        }
        val order = orders.firstOrNull { it.id == orderId } ?: return
        if (order.status == "RECIBIDA") {
            AuditLogManager.log("Compras", "DENEGAR_ELIMINAR_RECIBIDA", "No se puede eliminar una orden recibida sin revertir stock", level = "WARN")
            return
        }
        orders.removeAll { it.id == orderId }
        save()
    }

    fun markReceived(orderId: String, productState: ProductState) {
        if (!AccessControl.canManagePurchases(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_COMPRA_RECIBIR", "Acceso denegado para recibir ordenes de compra", level = "WARN")
            return
        }
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx < 0) return
        val order = orders[idx]
        if (order.status != "PENDIENTE") return
        for (item in order.items) {
            productState.adjustStock(item.productId, item.quantity)
        }
        orders[idx] = order.copy(status = "RECIBIDA", updatedAt = System.currentTimeMillis())
        save()
    }

    fun cancelOrder(orderId: String, productState: ProductState) {
        if (!AccessControl.canManagePurchases(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_COMPRA_CANCELAR", "Acceso denegado para cancelar ordenes de compra", level = "WARN")
            return
        }
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx < 0) return
        val order = orders[idx]
        if (order.status == "CANCELADA") return
        if (order.status == "RECIBIDA") {
            for (item in order.items) {
                productState.adjustStock(item.productId, -item.quantity, "Reversion por cancelacion de compra")
            }
        }
        orders[idx] = order.copy(status = "CANCELADA", updatedAt = System.currentTimeMillis())
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
                    DatabaseManager.deleteAll("purchase_orders")
                    DatabaseManager.deleteAll("purchase_order_items")
                    orders.forEach { o ->
                        DatabaseManager.insert("purchase_orders", mapOf(
                            "id" to o.id,
                            "provider_name" to o.providerName,
                            "status" to o.status,
                            "notes" to o.notes,
                            "created_at" to o.createdAt,
                            "updated_at" to o.updatedAt
                        ))
                        o.items.forEach { item ->
                            DatabaseManager.insert("purchase_order_items", mapOf(
                                "order_id" to o.id,
                                "product_id" to item.productId,
                                "product_name" to item.productName,
                                "quantity" to item.quantity,
                                "unit_price" to item.unitPrice
                            ))
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = orders.map { o ->
            val itemsStr = o.items.joinToString(";") { i ->
                "${i.productId},${esc(i.productName)},${i.quantity},${i.unitPrice}"
            }
            listOf(o.id, esc(o.providerName), itemsStr, esc(o.status), esc(o.notes), o.createdAt.toString(), o.updatedAt.toString())
                .joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText(FILE, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("purchase_orders", orderBy = "created_at DESC") { it }
                if (rows.isNotEmpty()) {
                    val itemsMap = mutableMapOf<String, MutableList<PurchaseOrderItem>>()
                    try {
                        val itemRows = DatabaseManager.query("purchase_order_items") { it }
                        itemRows.forEach { row ->
                            val orderId = row["order_id"] as? String ?: return@forEach
                            itemsMap.getOrPut(orderId) { mutableListOf() }.add(
                                PurchaseOrderItem(
                                    productId = ((row["product_id"] as? Long) ?: (row["product_id"] as? Int)?.toLong() ?: 0L).toInt(),
                                    productName = row["product_name"] as? String ?: "",
                                    quantity = ((row["quantity"] as? Long) ?: (row["quantity"] as? Int)?.toLong() ?: 1L).toInt(),
                                    unitPrice = (row["unit_price"] as? Double) ?: ((row["unit_price"] as? Long)?.toDouble() ?: 0.0)
                                )
                            )
                        }
                    } catch (_: Exception) { }
                    orders.clear()
                    rows.forEach { row ->
                        val id = row["id"] as? String ?: return@forEach
                        orders.add(PurchaseOrder(
                            id = id,
                            providerName = row["provider_name"] as? String ?: "",
                            items = itemsMap[id].orEmpty(),
                            status = row["status"] as? String ?: "PENDIENTE",
                            notes = row["notes"] as? String ?: "",
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis()
                        ))
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE) ?: return
        if (text.isBlank()) return
        orders.clear()
        text.lines().filter { it.isNotBlank() }.forEach { line ->
            val f = descline(line)
            if (f.size < 5) return@forEach
            val items = f.getOrElse(2) { "" }.split(";").mapNotNull { part ->
                val p = part.split(",")
                if (p.size >= 4) {
                    PurchaseOrderItem(
                        productId = p[0].toIntOrNull() ?: return@mapNotNull null,
                        productName = p[1],
                        quantity = p[2].toIntOrNull() ?: 0,
                        unitPrice = p[3].toDoubleOrNull() ?: 0.0
                    )
                } else null
            }
            orders.add(PurchaseOrder(
                id = f[0], providerName = f[1], items = items,
                status = f.getOrElse(3) { "PENDIENTE" },
                notes = f.getOrElse(4) { "" },
                createdAt = f.getOrElse(5) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = f.getOrElse(6) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
            ))
        }
        if (orders.isNotEmpty()) save()
    }
}
