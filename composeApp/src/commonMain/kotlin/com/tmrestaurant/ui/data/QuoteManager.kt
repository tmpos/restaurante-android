package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager

data class QuoteItem(
    val productId: Int,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val taxPercent: Double = 18.0
) {
    val subtotal: Double get() = unitPrice * quantity
    val taxAmount: Double get() = subtotal - (subtotal / (1.0 + taxPercent / 100.0))
}

data class Quote(
    val id: String = "COT-${System.currentTimeMillis().toString().takeLast(8)}",
    val customerId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val validUntil: String = "",
    val notes: String = "",
    val status: String = "BORRADOR",
    val items: List<QuoteItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double get() = items.sumOf { it.subtotal }
    val taxAmount: Double get() = items.sumOf { it.taxAmount }
    val total: Double get() = items.sumOf { it.subtotal }
}

object QuoteManager {
    private const val FILE = "quotes.v1.tsv"

    var quotes by mutableStateOf<List<Quote>>(emptyList())
        private set

    init {
        load()
    }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("quotes")
    } catch (_: Exception) { false }

    fun addOrUpdate(quote: Quote) {
        if (!AccessControl.canManageQuotes(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_COTIZACION_EDITAR", "Acceso denegado para crear o editar cotizaciones", level = "WARN")
            return
        }
        val existingIndex = quotes.indexOfFirst { it.id == quote.id }
        val updated = quote.copy(updatedAt = System.currentTimeMillis())
        quotes = if (existingIndex >= 0) {
            quotes.map { if (it.id == quote.id) updated else it }
        } else {
            listOf(updated) + quotes
        }
        save()
        AuditLogManager.log("Cotizaciones", if (existingIndex >= 0) "EDITAR_COTIZACION" else "CREAR_COTIZACION", "${updated.id} - ${updated.customerName.ifBlank { "Sin cliente" }}")
    }

    fun delete(id: String) {
        if (!AccessControl.canDeleteQuotes(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_COTIZACION_ELIMINAR", "Acceso denegado para eliminar cotizaciones", level = "WARN")
            return
        }
        quotes = quotes.filter { it.id != id }
        save()
        AuditLogManager.log("Cotizaciones", "ELIMINAR_COTIZACION", id, level = "WARN")
    }

    fun updateStatus(id: String, status: String) {
        if (!AccessControl.canFinalizeQuotes(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_COTIZACION_ESTADO", "Acceso denegado para cambiar estado de cotizaciones", level = "WARN")
            return
        }
        quotes = quotes.map {
            if (it.id == id) it.copy(status = status, updatedAt = System.currentTimeMillis()) else it
        }
        save()
        AuditLogManager.log("Cotizaciones", "CAMBIAR_ESTADO_COTIZACION", "$id -> $status")
    }

    fun duplicate(id: String): Quote? {
        if (!AccessControl.canManageQuotes(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_COTIZACION_DUPLICAR", "Acceso denegado para duplicar cotizaciones", level = "WARN")
            return null
        }
        val quote = quotes.firstOrNull { it.id == id } ?: return null
        val copy = quote.copy(
            id = "COT-${System.currentTimeMillis().toString().takeLast(8)}",
            status = "BORRADOR",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        addOrUpdate(copy)
        return copy
    }

    private fun esc(v: String): String =
        v.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

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

    private fun serializeItems(items: List<QuoteItem>): String =
        items.joinToString("|") { item ->
            listOf(
                item.productId.toString(),
                esc(item.productName),
                item.unitPrice.toString(),
                item.quantity.toString(),
                item.taxPercent.toString()
            ).joinToString(",") { esc(it) }
        }

    private fun deserializeItems(data: String): List<QuoteItem> {
        if (data.isBlank()) return emptyList()
        return data.split("|").mapNotNull { raw ->
            val fields = raw.split(",").map {
                it.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\")
            }
            if (fields.size < 4) null else QuoteItem(
                productId = fields[0].toIntOrNull() ?: 0,
                productName = fields[1],
                unitPrice = fields[2].toDoubleOrNull() ?: 0.0,
                quantity = fields[3].toIntOrNull() ?: 0,
                taxPercent = fields.getOrElse(4) { "18" }.toDoubleOrNull() ?: 18.0
            )
        }
    }

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("quotes")
                    DatabaseManager.deleteAll("quote_items")
                    quotes.forEach { quote ->
                        DatabaseManager.insert("quotes", mapOf(
                            "id" to quote.id,
                            "customer_id" to quote.customerId,
                            "customer_name" to quote.customerName,
                            "customer_email" to quote.customerEmail,
                            "customer_phone" to quote.customerPhone,
                            "valid_until" to quote.validUntil,
                            "notes" to quote.notes,
                            "status" to quote.status,
                            "created_at" to quote.createdAt,
                            "updated_at" to quote.updatedAt
                        ))
                        quote.items.forEach { item ->
                            DatabaseManager.insert("quote_items", mapOf(
                                "quote_id" to quote.id,
                                "product_id" to item.productId,
                                "product_name" to item.productName,
                                "unit_price" to item.unitPrice,
                                "quantity" to item.quantity,
                                "tax_percent" to item.taxPercent
                            ))
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = quotes.map { quote ->
            listOf(
                esc(quote.id),
                esc(quote.customerId),
                esc(quote.customerName),
                esc(quote.customerEmail),
                esc(quote.customerPhone),
                esc(quote.validUntil),
                esc(quote.notes),
                esc(quote.status),
                serializeItems(quote.items),
                quote.createdAt.toString(),
                quote.updatedAt.toString()
            ).joinToString("\t") { it }
        }
        PersistentFiles.writeText(FILE, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("quotes", orderBy = "created_at DESC") { it }
                if (rows.isNotEmpty()) {
                    val itemsMap = mutableMapOf<String, MutableList<QuoteItem>>()
                    try {
                        val itemRows = DatabaseManager.query("quote_items") { it }
                        itemRows.forEach { row ->
                            val quoteId = row["quote_id"] as? String ?: return@forEach
                            itemsMap.getOrPut(quoteId) { mutableListOf() }.add(
                                QuoteItem(
                                    productId = ((row["product_id"] as? Long) ?: (row["product_id"] as? Int)?.toLong() ?: 0L).toInt(),
                                    productName = row["product_name"] as? String ?: "",
                                    unitPrice = (row["unit_price"] as? Double) ?: ((row["unit_price"] as? Long)?.toDouble() ?: 0.0),
                                    quantity = ((row["quantity"] as? Long) ?: (row["quantity"] as? Int)?.toLong() ?: 1L).toInt(),
                                    taxPercent = (row["tax_percent"] as? Double) ?: ((row["tax_percent"] as? Long)?.toDouble() ?: 18.0)
                                )
                            )
                        }
                    } catch (_: Exception) { }
                    quotes = rows.map { row ->
                        val id = row["id"] as? String ?: ""
                        Quote(
                            id = id,
                            customerId = row["customer_id"] as? String ?: "",
                            customerName = row["customer_name"] as? String ?: "",
                            customerEmail = row["customer_email"] as? String ?: "",
                            customerPhone = row["customer_phone"] as? String ?: "",
                            validUntil = row["valid_until"] as? String ?: "",
                            notes = row["notes"] as? String ?: "",
                            status = row["status"] as? String ?: "BORRADOR",
                            items = itemsMap[id].orEmpty(),
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE) ?: return
        if (text.isBlank()) return
        quotes = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val f = descline(line)
            if (f.size < 9) null else Quote(
                id = f[0],
                customerId = f[1],
                customerName = f[2],
                customerEmail = f[3],
                customerPhone = f[4],
                validUntil = f[5],
                notes = f[6],
                status = f[7],
                items = deserializeItems(f[8]),
                createdAt = f.getOrElse(9) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = f.getOrElse(10) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
            )
        }
        if (quotes.isNotEmpty()) save()
    }
}
