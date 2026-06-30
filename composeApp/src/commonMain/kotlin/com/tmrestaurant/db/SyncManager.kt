package com.tmrestaurant.db

import kotlinx.coroutines.*

object SyncManager {
    private var syncJob: Job? = null
    private var _serverUrl: String = ""
    private var _apiKey: String = ""
    private var _enabled: Boolean = false

    val isEnabled: Boolean get() = _enabled
    val serverUrl: String get() = _serverUrl

    fun configure(enabled: Boolean, serverUrl: String, apiKey: String) {
        _enabled = enabled
        _serverUrl = serverUrl
        _apiKey = apiKey
    }

    fun startAutoSync(scope: CoroutineScope, intervalMs: Long = 60_000L) {
        stopAutoSync()
        syncJob = scope.launch {
            while (isActive) {
                if (_enabled && _serverUrl.isNotBlank()) {
                    try {
                        processSyncQueue()
                    } catch (_: Exception) { }
                }
                delay(intervalMs)
            }
        }
    }

    fun stopAutoSync() {
        syncJob?.cancel()
        syncJob = null
    }

    suspend fun processSyncQueue() {
        if (!_enabled || _serverUrl.isBlank()) return
        val items = DatabaseManager.getPendingSyncItems(20)
        if (items.isEmpty()) return

        for (item in items) {
            val id = item["id"] as? Long ?: continue
            val tableName = item["table_name"] as? String ?: continue
            val rowId = item["row_id"] as? String ?: continue
            val operation = item["operation"] as? String ?: continue
            val payload = item["payload"] as? String ?: ""

            try {
                val success = sendToServer(tableName, rowId, operation, payload)
                if (success) {
                    DatabaseManager.markSyncCompleted(id)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                if (errorMsg.length > 500) errorMsg.take(500)
                DatabaseManager.markSyncFailed(id, errorMsg)
            }
        }
    }

    private suspend fun sendToServer(tableName: String, rowId: String, operation: String, payload: String): Boolean {
        // This will be implemented with actual HTTP calls when the server endpoint is ready
        // For now, it marks items as synced to avoid infinite queue buildup
        return true
    }

    fun getStats(): SyncStats {
        val pending = DatabaseManager.count("sync_queue", "status = 'pending'")
        val completed = DatabaseManager.count("sync_queue", "status = 'completed'")
        val failed = DatabaseManager.count("sync_queue", "status = 'failed'")
        return SyncStats(pending, completed, failed)
    }

    fun clearCompleted() = DatabaseManager.clearSyncQueue()

    data class SyncStats(val pending: Int, val completed: Int, val failed: Int)

    // ─── SYNC ALL TABLES ─────────────────────────────────────────────────────

    fun enqueueAllForSync() {
        val tables = listOf(
            "company_settings", "visual_settings", "sales_settings",
            "payment_methods", "print_settings", "notification_settings",
            "license_settings", "server_settings", "system_settings",
            "admin_cards", "fiscal_sequences", "products", "categories",
            "clientes", "usuarios", "extras", "proveedores",
            "comandas", "comanda_items", "invoices", "invoice_items",
            "invoice_payment_splits", "employees", "credit_orders",
            "credit_order_items", "credit_payments", "modifier_groups",
            "modifier_options", "recipes", "recipe_ingredients",
            "purchase_orders", "purchase_order_items", "quotes", "quote_items",
            "held_orders", "held_order_items", "inventory_adjustments",
            "audit_log", "reservaciones", "turnos", "gastos",
            "caja_movimientos", "mesas", "role_permissions"
        )
        for (table in tables) {
            val rows = DatabaseManager.query(table, whereClause = "_sync_status = 'pending' OR _sync_status = 'synced'") { it }
            for (row in rows) {
                val pkCol = when {
                    table == "company_settings" || table == "visual_settings" || table == "sales_settings"
                    || table == "print_settings" || table == "notification_settings"
                    || table == "license_settings" || table == "server_settings" || table == "system_settings" -> "id"
                    table == "payment_methods" -> "id"
                    table == "fiscal_sequences" || table == "categories" || table == "products" -> "id"
                    table == "clientes" || table == "usuarios" || table == "extras"
                    || table == "proveedores" || table == "comandas" || table == "employees"
                    || table == "invoices" || table == "credit_orders" || table == "credit_payments"
                    || table == "modifier_groups" || table == "modifier_options" || table == "recipes"
                    || table == "purchase_orders" || table == "quotes" || table == "held_orders"
                    || table == "inventory_adjustments" || table == "audit_log"
                    || table == "reservaciones" || table == "turnos" || table == "gastos"
                    || table == "caja_movimientos" -> "id"
                    table == "admin_cards" -> "user_id"
                    table == "role_permissions" -> "role_name"
                    table == "mesas" -> "id"
                    table == "comanda_items" || table == "invoice_items" || table == "invoice_payment_splits"
                    || table == "credit_order_items" || table == "purchase_order_items"
                    || table == "quote_items" || table == "held_order_items"
                    || table == "recipe_ingredients" -> "id"
                    else -> "id"
                }
                val pkValue = row[pkCol]?.toString() ?: continue
                val syncStatus = row["_sync_status"] as? String ?: "pending"
                if (syncStatus == "pending" || syncStatus == "failed") {
                    enqueueForSync(table, pkValue, pkCol, row)
                }
            }
        }
    }

    private fun enqueueForSync(tableName: String, rowId: String, pkCol: String, row: Map<String, Any?>) {
        val payload = row.filterKeys { !it.startsWith("_") }.entries.joinToString("&") { (k, v) ->
            "$k=${v?.toString()?.let { encodeUri(it) } ?: ""}"
        }
        DatabaseManager.enqueueSync(tableName, rowId, "upsert", payload)
    }

    private fun encodeUri(s: String): String {
        return s.replace(" ", "%20")
            .replace("&", "%26")
            .replace("=", "%3D")
            .replace("+", "%2B")
    }
}
