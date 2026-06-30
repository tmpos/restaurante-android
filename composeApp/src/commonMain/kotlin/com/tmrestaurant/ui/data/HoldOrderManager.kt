package com.tmrestaurant.ui.data

import androidx.compose.runtime.mutableStateListOf
import com.tmrestaurant.db.DatabaseManager

data class HeldOrder(
    val id: String,
    val label: String,
    val items: List<CartItem>,
    val discountLabel: String = "",
    val discountAmount: Double = 0.0,
    val clientId: String = "",
    val clientName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object HoldOrderManager {
    private const val FILE = "held_orders.v1.tsv"

    val heldOrders = mutableStateListOf<HeldOrder>()

    init { load() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("held_orders")
    } catch (_: Exception) { false }

    fun hold(order: HeldOrder) {
        heldOrders.add(0, order)
        save()
    }

    fun removeAt(index: Int) {
        if (index in heldOrders.indices) {
            heldOrders.removeAt(index)
            save()
        }
    }

    private fun esc(v: String) = v
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

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

    private fun serializeItems(items: List<CartItem>): String =
        items.joinToString("|") { item ->
            listOf(
                item.product.id.toString(), esc(item.product.name),
                item.product.price.toString(), item.product.taxPercent.toString(),
                item.quantity.toString(), item.extrasCost.toString(), esc(item.extrasNote),
                item.dinerIndex.toString(),
                item.weightQuantity.toString(),
                esc(item.selectedModifiers.joinToString("|") { m ->
                    listOf(esc(m.groupId), esc(m.groupName), esc(m.optionId), esc(m.optionName), m.price.toString()).joinToString(",") { esc(it) }
                }),
                esc(item.courseType)
            ).joinToString(",") { esc(it) }
        }

    private fun deserializeItems(data: String): List<CartItem> {
        if (data.isBlank()) return emptyList()
        return data.split("|").map { part ->
            val f = part.split(",").map { v ->
                v.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\")
            }
            CartItem(
                product = Product(
                    id = f.getOrElse(0) { "0" }.toIntOrNull() ?: 0,
                    name = f.getOrElse(1) { "" },
                    price = f.getOrElse(2) { "0" }.toDoubleOrNull() ?: 0.0,
                    taxPercent = f.getOrElse(3) { "18" }.toDoubleOrNull() ?: 18.0
                ),
                quantity = f.getOrElse(4) { "1" }.toIntOrNull() ?: 1,
                extrasCost = f.getOrElse(5) { "0" }.toDoubleOrNull() ?: 0.0,
                extrasNote = f.getOrElse(6) { "" },
                dinerIndex = f.getOrElse(7) { "0" }.toIntOrNull() ?: 0,
                weightQuantity = f.getOrElse(8) { "0.0" }.toDoubleOrNull() ?: 0.0,
                selectedModifiers = deserializeModifiers(f.getOrElse(9) { "" }),
                courseType = f.getOrElse(10) { "" }
            )
        }
    }

    private fun deserializeModifiers(data: String): List<ModifierSelection> {
        if (data.isBlank()) return emptyList()
        return data.split("|").mapNotNull { part ->
            val f = part.split(",").map { v -> v.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\") }
            if (f.size < 4) null
            else ModifierSelection(groupId = f[0], groupName = f[1], optionId = f[2], optionName = f[3], price = f.getOrElse(4) { "0" }.toDoubleOrNull() ?: 0.0)
        }
    }

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("held_orders")
                    DatabaseManager.deleteAll("held_order_items")
                    heldOrders.forEach { h ->
                        DatabaseManager.insert("held_orders", mapOf(
                            "id" to h.id,
                            "label" to h.label,
                            "discount_label" to h.discountLabel,
                            "discount_amount" to h.discountAmount,
                            "client_id" to h.clientId,
                            "client_name" to h.clientName,
                            "timestamp" to h.timestamp
                        ))
                        h.items.forEach { item ->
                            DatabaseManager.insert("held_order_items", mapOf(
                                "held_order_id" to h.id,
                                "product_id" to item.product.id,
                                "product_name" to item.product.name,
                                "price" to item.product.price,
                                "tax_percent" to item.product.taxPercent,
                                "quantity" to item.quantity,
                                "extras_cost" to item.extrasCost,
                                "extras_note" to item.extrasNote,
                                "diner_index" to item.dinerIndex,
                                "weight_quantity" to item.weightQuantity,
                                "modifier_data" to item.selectedModifiers.joinToString("|") { m ->
                                    "${m.groupId},${m.groupName},${m.optionId},${m.optionName},${m.price}"
                                },
                                "course_type" to item.courseType
                            ))
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = heldOrders.map { h ->
            listOf(
                esc(h.id), esc(h.label), serializeItems(h.items),
                esc(h.discountLabel), h.discountAmount.toString(),
                esc(h.clientId), esc(h.clientName), h.timestamp.toString()
            ).joinToString("\t") { it }
        }
        PersistentFiles.writeText(FILE, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("held_orders", orderBy = "timestamp DESC") { it }
                if (rows.isNotEmpty()) {
                    val itemsMap = mutableMapOf<String, MutableList<CartItem>>()
                    try {
                        val itemRows = DatabaseManager.query("held_order_items") { it }
                        itemRows.forEach { row ->
                            val orderId = row["held_order_id"] as? String ?: return@forEach
                            val pId = ((row["product_id"] as? Long) ?: (row["product_id"] as? Int)?.toLong() ?: 0L).toInt()
                            itemsMap.getOrPut(orderId) { mutableListOf() }.add(
                                CartItem(
                                    product = Product(
                                        id = pId,
                                        name = row["product_name"] as? String ?: "",
                                        price = (row["price"] as? Double) ?: ((row["price"] as? Long)?.toDouble() ?: 0.0),
                                        taxPercent = (row["tax_percent"] as? Double) ?: ((row["tax_percent"] as? Long)?.toDouble() ?: 18.0)
                                    ),
                                    quantity = ((row["quantity"] as? Long) ?: 1L).toInt(),
                                    extrasCost = (row["extras_cost"] as? Double) ?: ((row["extras_cost"] as? Long)?.toDouble() ?: 0.0),
                                    extrasNote = row["extras_note"] as? String ?: "",
                                    dinerIndex = ((row["diner_index"] as? Long) ?: 0L).toInt(),
                                    weightQuantity = (row["weight_quantity"] as? Double) ?: ((row["weight_quantity"] as? Long)?.toDouble() ?: 0.0),
                                    selectedModifiers = deserializeModifiers2(row["modifier_data"] as? String ?: ""),
                                    courseType = row["course_type"] as? String ?: ""
                                )
                            )
                        }
                    } catch (_: Exception) { }
                    heldOrders.clear()
                    rows.forEach { row ->
                        val id = row["id"] as? String ?: return@forEach
                        heldOrders.add(HeldOrder(
                            id = id,
                            label = row["label"] as? String ?: "",
                            items = itemsMap[id].orEmpty(),
                            discountLabel = row["discount_label"] as? String ?: "",
                            discountAmount = (row["discount_amount"] as? Double) ?: ((row["discount_amount"] as? Long)?.toDouble() ?: 0.0),
                            clientId = row["client_id"] as? String ?: "",
                            clientName = row["client_name"] as? String ?: "",
                            timestamp = (row["timestamp"] as? Long) ?: System.currentTimeMillis()
                        ))
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE) ?: return
        if (text.isBlank()) return
        heldOrders.clear()
        for (line in text.lines()) {
            if (line.isBlank()) continue
            val f = descline(line)
            if (f.size < 8) continue
            heldOrders.add(
                HeldOrder(
                    id = f[0], label = f[1],
                    items = deserializeItems(f.getOrElse(2) { "" }),
                    discountLabel = f.getOrElse(3) { "" },
                    discountAmount = f.getOrElse(4) { "0" }.toDoubleOrNull() ?: 0.0,
                    clientId = f.getOrElse(5) { "" },
                    clientName = f.getOrElse(6) { "" },
                    timestamp = f.getOrElse(7) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
                )
            )
        }
        if (heldOrders.isNotEmpty()) save()
    }

    private fun deserializeModifiers2(data: String): List<ModifierSelection> {
        if (data.isBlank()) return emptyList()
        return data.split("|").mapNotNull { part ->
            val f = part.split(",")
            if (f.size < 4) null
            else ModifierSelection(groupId = f[0], groupName = f[1], optionId = f[2], optionName = f[3], price = f.getOrElse(4) { "0" }.toDoubleOrNull() ?: 0.0)
        }
    }
}
