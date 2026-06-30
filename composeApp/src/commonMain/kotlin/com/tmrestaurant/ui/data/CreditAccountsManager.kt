package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager
import com.tmrestaurant.platform.localDateKey

data class CreditOrderItem(
    val productId: Int,
    val name: String,
    val unitPrice: Double,
    val quantity: Int,
    val note: String = ""
) {
    val total: Double get() = unitPrice * quantity
}

data class CreditOrder(
    val id: String = genUid("cco"),
    val clientId: String,
    val items: List<CreditOrderItem>,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dateKey: String = localDateKey(createdAt)
) {
    val total: Double get() = items.sumOf { it.total }
}

data class CreditPayment(
    val id: String = genUid("ccp"),
    val clientId: String,
    val amount: Double,
    val method: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dateKey: String = localDateKey(createdAt)
)

object CreditAccountsManager {
    private const val FILE_NAME = "credit_accounts.v1.tsv"

    var orders by mutableStateOf<List<CreditOrder>>(emptyList())
        private set
    var payments by mutableStateOf<List<CreditPayment>>(emptyList())
        private set

    init {
        load()
    }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("credit_orders")
    } catch (_: Exception) { false }

    fun ordersFor(clientId: String): List<CreditOrder> =
        orders.filter { it.clientId == clientId }.sortedByDescending { it.createdAt }

    fun paymentsFor(clientId: String): List<CreditPayment> =
        payments.filter { it.clientId == clientId }.sortedByDescending { it.createdAt }

    fun totalCharges(clientId: String): Double = ordersFor(clientId).sumOf { it.total }

    fun totalPayments(clientId: String): Double = paymentsFor(clientId).sumOf { it.amount }

    fun balance(clientId: String): Double =
        (totalCharges(clientId) - totalPayments(clientId)).coerceAtLeast(0.0)

    fun addOrder(clientId: String, items: List<CreditOrderItem>, note: String = "") {
        if (!AccessControl.canManageCreditAccounts(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CXC_CARGO", "Acceso denegado para registrar cargos a credito", level = "WARN")
            return
        }
        if (items.isEmpty()) return
        orders = listOf(CreditOrder(clientId = clientId, items = items, note = note.trim())) + orders
        save()
    }

    fun deleteOrder(orderId: String) {
        if (!AccessControl.canDeleteCreditEntries(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CXC_ELIMINAR_CARGO", "Acceso denegado para eliminar cargos a credito", level = "WARN")
            return
        }
        orders = orders.filterNot { it.id == orderId }
        save()
    }

    fun addPayment(clientId: String, amount: Double, method: String, note: String = "") {
        if (!AccessControl.canManageCreditAccounts(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CXC_PAGO", "Acceso denegado para registrar pagos de credito", level = "WARN")
            return
        }
        val applied = amount.coerceAtMost(balance(clientId))
        if (applied <= 0.0) return
        payments = listOf(
            CreditPayment(
                clientId = clientId,
                amount = applied,
                method = method,
                note = note.trim()
            )
        ) + payments
        save()
    }

    fun deletePayment(paymentId: String) {
        if (!AccessControl.canDeleteCreditEntries(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CXC_ELIMINAR_PAGO", "Acceso denegado para eliminar pagos de credito", level = "WARN")
            return
        }
        payments = payments.filterNot { it.id == paymentId }
        save()
    }

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("credit_orders")
                    DatabaseManager.deleteAll("credit_order_items")
                    DatabaseManager.deleteAll("credit_payments")
                    orders.forEach { order ->
                        DatabaseManager.insert("credit_orders", mapOf(
                            "id" to order.id,
                            "client_id" to order.clientId,
                            "note" to order.note,
                            "created_at" to order.createdAt,
                            "date_key" to order.dateKey
                        ))
                        order.items.forEach { item ->
                            DatabaseManager.insert("credit_order_items", mapOf(
                                "order_id" to order.id,
                                "product_id" to item.productId,
                                "name" to item.name,
                                "unit_price" to item.unitPrice,
                                "quantity" to item.quantity,
                                "note" to item.note
                            ))
                        }
                    }
                    payments.forEach { payment ->
                        DatabaseManager.insert("credit_payments", mapOf(
                            "id" to payment.id,
                            "client_id" to payment.clientId,
                            "amount" to payment.amount,
                            "method" to payment.method,
                            "note" to payment.note,
                            "created_at" to payment.createdAt,
                            "date_key" to payment.dateKey
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = mutableListOf<String>()
        orders.forEach { order ->
            lines += join("O", order.id, order.clientId, order.createdAt.toString(), order.dateKey, order.note)
            order.items.forEach { item ->
                lines += join(
                    "I", order.id, item.productId.toString(), item.name,
                    item.unitPrice.toString(), item.quantity.toString(), item.note
                )
            }
        }
        payments.forEach { payment ->
            lines += join(
                "P", payment.id, payment.clientId, payment.amount.toString(), payment.method,
                payment.createdAt.toString(), payment.dateKey, payment.note
            )
        }
        PersistentFiles.writeText(FILE_NAME, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val orderRows = DatabaseManager.query("credit_orders", orderBy = "created_at DESC") { it }
                val paymentRows = DatabaseManager.query("credit_payments", orderBy = "created_at DESC") { it }
                if (orderRows.isNotEmpty() || paymentRows.isNotEmpty()) {
                    val itemsMap = mutableMapOf<String, MutableList<CreditOrderItem>>()
                    try {
                        val itemRows = DatabaseManager.query("credit_order_items") { it }
                        itemRows.forEach { row ->
                            val orderId = row["order_id"] as? String ?: return@forEach
                            itemsMap.getOrPut(orderId) { mutableListOf() }.add(
                                CreditOrderItem(
                                    productId = ((row["product_id"] as? Long) ?: (row["product_id"] as? Int)?.toLong() ?: 0L).toInt(),
                                    name = row["name"] as? String ?: "",
                                    unitPrice = (row["unit_price"] as? Double) ?: ((row["unit_price"] as? Long)?.toDouble() ?: 0.0),
                                    quantity = ((row["quantity"] as? Long) ?: (row["quantity"] as? Int)?.toLong() ?: 1L).toInt(),
                                    note = row["note"] as? String ?: ""
                                )
                            )
                        }
                    } catch (_: Exception) { }
                    orders = orderRows.map { row ->
                        val id = row["id"] as? String ?: ""
                        CreditOrder(
                            id = id,
                            clientId = row["client_id"] as? String ?: "",
                            items = itemsMap[id].orEmpty(),
                            note = row["note"] as? String ?: "",
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            dateKey = row["date_key"] as? String ?: ""
                        )
                    }
                    payments = paymentRows.map { row ->
                        CreditPayment(
                            id = row["id"] as? String ?: "",
                            clientId = row["client_id"] as? String ?: "",
                            amount = (row["amount"] as? Double) ?: ((row["amount"] as? Long)?.toDouble() ?: 0.0),
                            method = row["method"] as? String ?: "",
                            note = row["note"] as? String ?: "",
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            dateKey = row["date_key"] as? String ?: ""
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE_NAME) ?: return
        val loadedOrders = linkedMapOf<String, CreditOrder>()
        val loadedItems = mutableMapOf<String, MutableList<CreditOrderItem>>()
        val loadedPayments = mutableListOf<CreditPayment>()

        text.lineSequence().filter { it.isNotBlank() }.forEach { line ->
            val f = split(line)
            when (f.firstOrNull()) {
                "O" -> if (f.size >= 6) {
                    val createdAt = f[3].toLongOrNull() ?: System.currentTimeMillis()
                    loadedOrders[f[1]] = CreditOrder(
                        id = f[1],
                        clientId = f[2],
                        items = emptyList(),
                        createdAt = createdAt,
                        dateKey = f[4].ifBlank { localDateKey(createdAt) },
                        note = f[5]
                    )
                }
                "I" -> if (f.size >= 7) {
                    loadedItems.getOrPut(f[1]) { mutableListOf() }.add(
                        CreditOrderItem(
                            productId = f[2].toIntOrNull() ?: 0,
                            name = f[3],
                            unitPrice = f[4].toDoubleOrNull() ?: 0.0,
                            quantity = f[5].toIntOrNull() ?: 1,
                            note = f[6]
                        )
                    )
                }
                "P" -> if (f.size >= 8) {
                    val createdAt = f[5].toLongOrNull() ?: System.currentTimeMillis()
                    loadedPayments += CreditPayment(
                        id = f[1],
                        clientId = f[2],
                        amount = f[3].toDoubleOrNull() ?: 0.0,
                        method = f[4],
                        createdAt = createdAt,
                        dateKey = f[6].ifBlank { localDateKey(createdAt) },
                        note = f[7]
                    )
                }
            }
        }
        orders = loadedOrders.values.map { it.copy(items = loadedItems[it.id].orEmpty()) }
            .sortedByDescending { it.createdAt }
        payments = loadedPayments.sortedByDescending { it.createdAt }
        if (orders.isNotEmpty() || payments.isNotEmpty()) save()
    }

    private fun join(vararg values: String): String = values.joinToString("\t") { escape(it) }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

    private fun split(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var escaping = false
        line.forEach { ch ->
            if (escaping) {
                current.append(when (ch) { 'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'; else -> ch })
                escaping = false
            } else {
                when (ch) {
                    '\\' -> escaping = true
                    '\t' -> { result += current.toString(); current.clear() }
                    else -> current.append(ch)
                }
            }
        }
        result += current.toString()
        return result
    }
}
