package com.tmrestaurant.ui.data

import androidx.compose.runtime.mutableStateListOf
import com.tmrestaurant.db.DatabaseManager
import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.components.PaymentSplit
import com.tmrestaurant.ui.components.ReturnedItem

private const val INVOICES_FILE = "invoices.v1.tsv"

private fun esc(value: String): String =
    value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

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
            item.product.id.toString(),
            esc(item.product.name),
            item.product.price.toString(),
            item.product.taxPercent.toString(),
            item.quantity.toString(),
            item.extrasCost.toString(),
            esc(item.extrasNote),
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

object InvoiceHistory {
    val invoices = mutableStateListOf<PaymentResult>()

    init { loadInvoices() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("invoices")
    } catch (_: Exception) { false }

    fun add(result: PaymentResult): PaymentResult {
        val turnoId = result.turnoId.ifBlank { TurnoManager.currentTurno?.id ?: "" }
        val maxOrder = invoices.filter { it.turnoId == turnoId }.maxOfOrNull { it.orderNumber } ?: 0
        val orderNum = maxOrder + 1
        val orderNote = if (result.note.isNotBlank()) "ORD #$orderNum | ${result.note}" else "ORD #$orderNum"
        val withOrder = result.copy(turnoId = turnoId, orderNumber = orderNum, note = orderNote)
        invoices.add(0, withOrder)
        saveInvoices()
        AuditLogManager.log("Facturas", "CREAR_FACTURA", "${withOrder.invoiceNumber} - ${withOrder.paymentMethod} - RD\$ ${"%,.2f".format(withOrder.total)}")
        return withOrder
    }

    fun removeAt(index: Int, productState: ProductState? = null) {
        if (index in invoices.indices) {
            val invoice = invoices[index]
            restoreInventoryForInvoice(invoice, productState, "Eliminacion de factura")
            invoices.removeAt(index)
            saveInvoices()
            AuditLogManager.log("Facturas", "ELIMINAR_FACTURA", "${invoice.invoiceNumber} eliminada", level = "WARN")
        }
    }

    fun cancelInvoice(index: Int, productState: ProductState? = null) {
        if (index in invoices.indices) {
            val invoice = invoices[index]
            if (invoice.status == "ANULADA" || invoice.status == "DEVUELTA") return
            restoreInventoryForInvoice(invoice, productState, "Anulacion de factura")
            invoices[index] = invoice.copy(status = "ANULADA")
            saveInvoices()
            AuditLogManager.log("Facturas", "ANULAR_FACTURA", "${invoice.invoiceNumber} anulada", level = "WARN")
        }
    }

    fun processReturn(index: Int, returnedItems: List<ReturnedItem>) {
        if (index in invoices.indices) {
            val inv = invoices[index]
            val allReturned = returnedItems.sumOf { it.quantity } >= inv.items.sumOf { it.quantity }
            invoices[index] = inv.copy(
                status = if (allReturned) "DEVUELTA" else "DEVOLUCION_PARCIAL",
                returnedItems = returnedItems
            )
            saveInvoices()
            AuditLogManager.log("Facturas", "DEVOLUCION_FACTURA", "${inv.invoiceNumber} - ${returnedItems.sumOf { it.quantity }} item(s)", level = "WARN")
        }
    }

    fun clearAll() {
        invoices.clear()
        if (isDbReady()) {
            try {
                DatabaseManager.deleteAll("invoices")
            } catch (_: Exception) { }
        }
        PersistentFiles.writeText(INVOICES_FILE, "")
    }

    private fun restoreInventoryForInvoice(invoice: PaymentResult, productState: ProductState?, reason: String) {
        val state = productState ?: return
        val returned = invoice.items.map { item ->
            ReturnedItem(
                productId = item.product.id,
                productName = item.product.name,
                quantity = item.quantity,
                refundAmount = item.product.price * item.effectiveQuantity,
                reason = reason
            )
        }
        RecipeInventoryManager.revertReturn(invoice.items, returned, state)
    }

    private fun invoiceToMap(inv: PaymentResult) = mapOf<String, Any?>(
        "invoice_number" to inv.invoiceNumber,
        "ncf" to inv.ncf,
        "total" to inv.total,
        "subtotal_pre_tax" to inv.subtotalPreTax,
        "tax_amount" to inv.taxAmount,
        "payment_method" to inv.paymentMethod,
        "received_amount" to inv.receivedAmount,
        "change_amount" to inv.change,
        "note" to inv.note,
        "surcharge_amount" to inv.surchargeAmount,
        "surcharge_percent" to inv.surchargePercent,
        "turno_id" to inv.turnoId,
        "timestamp" to inv.timestamp,
        "discount_label" to inv.discountLabel,
        "discount_amount" to inv.discountAmount,
        "tip_label" to inv.tipLabel,
        "tip_amount" to inv.tipAmount,
        "customer_id" to inv.customerId,
        "customer_name" to inv.customerName,
        "customer_rnc" to inv.customerRnc,
        "customer_phone" to inv.customerPhone,
        "status" to inv.status,
        "diner_names" to inv.dinerNames.joinToString(","),
        "delivery_address" to inv.deliveryAddress,
        "delivery_phone" to inv.deliveryPhone,
        "delivery_notes" to inv.deliveryNotes,
        "delivery_status" to inv.deliveryStatus,
        "order_number" to inv.orderNumber,
        "items" to serializeItems(inv.items),
        "payment_splits" to serializeSplits(inv.paymentSplits),
        "returned_items" to serializeReturnedItems(inv.returnedItems)
    )

    private fun saveInvoices() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("invoices")
                    invoices.forEach { inv ->
                        DatabaseManager.insert("invoices", invoiceToMap(inv))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = invoices.map { inv ->
            listOf(
                esc(inv.invoiceNumber), esc(inv.ncf), inv.total.toString(),
                inv.subtotalPreTax.toString(), inv.taxAmount.toString(),
                esc(inv.paymentMethod), inv.receivedAmount.toString(),
                inv.change.toString(), esc(inv.note),
                inv.surchargeAmount.toString(), inv.surchargePercent.toString(),
                esc(inv.turnoId), serializeItems(inv.items),
                inv.timestamp.toString(),
                esc(inv.discountLabel), inv.discountAmount.toString(),
                esc(inv.tipLabel), inv.tipAmount.toString(),
                esc(inv.customerId), esc(inv.customerName), esc(inv.customerRnc), esc(inv.customerPhone),
                esc(inv.status),
                esc(serializeSplits(inv.paymentSplits)),
                esc(inv.dinerNames.joinToString(",")),
                esc(serializeReturnedItems(inv.returnedItems)),
                esc(inv.deliveryAddress),
                esc(inv.deliveryPhone),
                esc(inv.deliveryNotes),
                esc(inv.deliveryStatus),
                inv.orderNumber.toString()
            ).joinToString("\t") { it }
        }
        PersistentFiles.writeText(INVOICES_FILE, lines.joinToString("\n"))
    }

    private fun serializeSplits(splits: List<PaymentSplit>): String =
        splits.joinToString("|") { s ->
            listOf(esc(s.method), s.amount.toString(), esc(s.percentage)).joinToString(",") { esc(it) }
        }

    private fun deserializeSplits(data: String): List<PaymentSplit> {
        if (data.isBlank()) return emptyList()
        return data.split("|").mapNotNull { part ->
            val f = part.split(",").map { v ->
                v.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\")
            }
            if (f.size < 2) null
            else PaymentSplit(method = f[0], amount = f[1].toDoubleOrNull() ?: 0.0, percentage = f.getOrElse(2) { "0" })
        }
    }

    private fun serializeReturnedItems(items: List<ReturnedItem>): String =
        items.joinToString("|") { ri ->
            listOf(
                ri.productId.toString(),
                esc(ri.productName),
                ri.quantity.toString(),
                ri.refundAmount.toString(),
                esc(ri.reason)
            ).joinToString(",") { esc(it) }
        }

    private fun deserializeReturnedItems(data: String): List<ReturnedItem> {
        if (data.isBlank()) return emptyList()
        return data.split("|").mapNotNull { part ->
            val f = part.split(",").map { v ->
                v.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\")
            }
            if (f.size < 4) null
            else ReturnedItem(
                productId = f[0].toIntOrNull() ?: 0,
                productName = f[1],
                quantity = f[2].toIntOrNull() ?: 0,
                refundAmount = f[3].toDoubleOrNull() ?: 0.0,
                reason = f.getOrElse(4) { "" }
            )
        }
    }

    private fun rowToPaymentResult(row: Map<String, Any?>): PaymentResult {
        val rawDinerNames = row["diner_names"] as? String ?: ""
        return PaymentResult(
            invoiceNumber = row["invoice_number"] as? String ?: "",
            ncf = row["ncf"] as? String ?: "",
            total = (row["total"] as? Double) ?: ((row["total"] as? Long)?.toDouble() ?: 0.0),
            subtotalPreTax = (row["subtotal_pre_tax"] as? Double) ?: ((row["subtotal_pre_tax"] as? Long)?.toDouble() ?: 0.0),
            taxAmount = (row["tax_amount"] as? Double) ?: ((row["tax_amount"] as? Long)?.toDouble() ?: 0.0),
            paymentMethod = row["payment_method"] as? String ?: "",
            receivedAmount = (row["received_amount"] as? Double) ?: ((row["received_amount"] as? Long)?.toDouble() ?: 0.0),
            change = (row["change_amount"] as? Double) ?: ((row["change_amount"] as? Long)?.toDouble() ?: 0.0),
            note = row["note"] as? String ?: "",
            surchargeAmount = (row["surcharge_amount"] as? Double) ?: ((row["surcharge_amount"] as? Long)?.toDouble() ?: 0.0),
            surchargePercent = (row["surcharge_percent"] as? Double) ?: ((row["surcharge_percent"] as? Long)?.toDouble() ?: 0.0),
            turnoId = row["turno_id"] as? String ?: "",
            items = deserializeItems(row["items"] as? String ?: ""),
            timestamp = (row["timestamp"] as? Long) ?: System.currentTimeMillis(),
            discountLabel = row["discount_label"] as? String ?: "",
            discountAmount = (row["discount_amount"] as? Double) ?: ((row["discount_amount"] as? Long)?.toDouble() ?: 0.0),
            tipLabel = row["tip_label"] as? String ?: "",
            tipAmount = (row["tip_amount"] as? Double) ?: ((row["tip_amount"] as? Long)?.toDouble() ?: 0.0),
            customerId = row["customer_id"] as? String ?: "",
            customerName = row["customer_name"] as? String ?: "",
            customerRnc = row["customer_rnc"] as? String ?: "",
            customerPhone = row["customer_phone"] as? String ?: "",
            status = row["status"] as? String ?: "ACTIVA",
            paymentSplits = deserializeSplits(row["payment_splits"] as? String ?: ""),
            dinerNames = if (rawDinerNames.isBlank()) emptyList()
                         else rawDinerNames.split(",").map { it.replace("\\,", ",").replace("\\\\", "\\") },
            returnedItems = deserializeReturnedItems(row["returned_items"] as? String ?: ""),
            deliveryAddress = row["delivery_address"] as? String ?: "",
            deliveryPhone = row["delivery_phone"] as? String ?: "",
            deliveryNotes = row["delivery_notes"] as? String ?: "",
            deliveryStatus = row["delivery_status"] as? String ?: "",
            orderNumber = ((row["order_number"] as? Long)?.toInt()) ?: 0
        )
    }

    private fun loadInvoices() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("invoices", orderBy = "timestamp DESC") { it }
                if (rows.isNotEmpty()) {
                    invoices.clear()
                    for (row in rows) {
                        invoices.add(rowToPaymentResult(row))
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(INVOICES_FILE) ?: return
        if (text.isBlank()) return

        invoices.clear()
        for (line in text.lines()) {
            if (line.isBlank()) continue
            val f = descline(line)
            if (f.size < 13) continue
            invoices.add(
                PaymentResult(
                    invoiceNumber = f[0],
                    ncf = f[1],
                    total = f[2].toDoubleOrNull() ?: 0.0,
                    subtotalPreTax = f[3].toDoubleOrNull() ?: 0.0,
                    taxAmount = f[4].toDoubleOrNull() ?: 0.0,
                    paymentMethod = f[5],
                    receivedAmount = f[6].toDoubleOrNull() ?: 0.0,
                    change = f[7].toDoubleOrNull() ?: 0.0,
                    note = f[8],
                    surchargeAmount = f[9].toDoubleOrNull() ?: 0.0,
                    surchargePercent = f[10].toDoubleOrNull() ?: 0.0,
                    turnoId = f[11],
                    items = deserializeItems(f.getOrElse(12) { "" }),
                    timestamp = f.getOrElse(13) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                    discountLabel = f.getOrElse(14) { "" },
                    discountAmount = f.getOrElse(15) { "0" }.toDoubleOrNull() ?: 0.0,
                    tipLabel = f.getOrElse(16) { "" },
                    tipAmount = f.getOrElse(17) { "0" }.toDoubleOrNull() ?: 0.0,
                    customerId = f.getOrElse(18) { "" },
                    customerName = f.getOrElse(19) { "" },
                    customerRnc = f.getOrElse(20) { "" },
                    customerPhone = f.getOrElse(21) { "" },
                    status = f.getOrElse(22) { "ACTIVA" },
                    paymentSplits = deserializeSplits(f.getOrElse(23) { "" }),
                    dinerNames = f.getOrElse(24) { "" }.let { raw ->
                        if (raw.isBlank()) emptyList()
                        else raw.split(",").map { it.replace("\\,", ",").replace("\\\\", "\\") }
                    },
                    returnedItems = deserializeReturnedItems(f.getOrElse(25) { "" }),
                    deliveryAddress = f.getOrElse(26) { "" },
                    deliveryPhone = f.getOrElse(27) { "" },
                    deliveryNotes = f.getOrElse(28) { "" },
                    deliveryStatus = f.getOrElse(29) { "" },
                    orderNumber = f.getOrElse(30) { "0" }.toIntOrNull() ?: 0
                )
            )
        }
        if (invoices.isNotEmpty()) saveInvoices()
    }

    fun updateDeliveryStatus(invoiceNumber: String, status: String) {
        val index = invoices.indexOfFirst { it.invoiceNumber == invoiceNumber }
        if (index < 0) return
        val invoice = invoices[index]
        if (invoice.deliveryAddress.isBlank()) return
        invoices[index] = invoice.copy(deliveryStatus = status)
        saveInvoices()
        AuditLogManager.log("Delivery", "CAMBIAR_ESTADO_DELIVERY", "$invoiceNumber -> $status")
    }
}
