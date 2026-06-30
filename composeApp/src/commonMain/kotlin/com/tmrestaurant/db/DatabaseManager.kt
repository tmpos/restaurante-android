package com.tmrestaurant.db

object DatabaseManager {
    private var _db: AppDatabase? = null
    val db: AppDatabase get() = _db ?: throw IllegalStateException("Database not initialized")

    fun init(platformInit: () -> AppDatabase) {
        if (_db != null) return
        _db = platformInit()
        val now = System.currentTimeMillis()
        DatabaseSchema.TABLES.forEach { sql ->
            db.execSQL(sql)
        }
    }

    fun reset() {
        _db?.close()
        _db = null
    }

    // ─── SYNC-AWARE INSERT / UPDATE ──────────────────────────────────────────

    fun insert(table: String, values: Map<String, Any?>, sync: Boolean = true) {
        val cols = values.keys.toMutableList()
        val allValues = mutableMapOf<String, Any?>()
        allValues.putAll(values)
        allValues.putIfAbsent("_updated_at", System.currentTimeMillis())
        if (sync) {
            allValues.putIfAbsent("_sync_status", "pending")
        }
        cols.add("_updated_at")
        if (sync) cols.add("_sync_status")
        if (!cols.contains("id") && !table.contains("_")) allValues.putIfAbsent("id", null)
        val placeholders = cols.joinToString(", ") { "?" }
        val sql = "INSERT OR REPLACE INTO $table (${cols.joinToString(", ")}) VALUES ($placeholders)"
        db.execSQL(sql, cols.map { allValues[it] })
    }

    fun update(table: String, values: Map<String, Any?>, whereClause: String, whereArgs: List<Any?>, sync: Boolean = true) {
        val vals = values.toMutableMap()
        vals.putIfAbsent("_updated_at", System.currentTimeMillis())
        if (sync) vals.putIfAbsent("_sync_status", "pending")
        val setClause = vals.keys.joinToString(", ") { "$it = ?" }
        val allArgs = vals.values.toList() + whereArgs
        val sql = "UPDATE $table SET $setClause WHERE $whereClause"
        db.execSQL(sql, allArgs)
    }

    fun delete(table: String, whereClause: String, whereArgs: List<Any?>, sync: Boolean = true) {
        if (sync) {
            update(table, mapOf("_sync_status" to "deleted"), whereClause, whereArgs, sync = true)
        } else {
            val sql = "DELETE FROM $table WHERE $whereClause"
            db.execSQL(sql, whereArgs)
        }
    }

    fun hardDelete(table: String, whereClause: String, whereArgs: List<Any?>) {
        val sql = "DELETE FROM $table WHERE $whereClause"
        db.execSQL(sql, whereArgs)
    }

    fun transaction(block: () -> Unit) = db.transaction(block)

    fun deleteAll(table: String) {
        db.execSQL("DELETE FROM $table")
    }

    fun <T> query(table: String, columns: List<String> = listOf("*"), whereClause: String = "1=1",
                  whereArgs: List<Any?> = emptyList(), orderBy: String = "",
                  mapper: (Map<String, Any?>) -> T): List<T> {
        val cols = columns.joinToString(", ")
        val order = if (orderBy.isNotBlank()) " ORDER BY $orderBy" else ""
        val sql = "SELECT $cols FROM $table WHERE $whereClause$order"
        return db.rawQuery(sql, whereArgs, mapper)
    }

    fun count(table: String, whereClause: String = "1=1", whereArgs: List<Any?> = emptyList()): Int {
        val sql = "SELECT COUNT(*) FROM $table WHERE $whereClause"
        return db.rawQueryCount(sql, whereArgs)
    }

    fun exists(table: String, whereClause: String, whereArgs: List<Any?>): Boolean {
        return count(table, whereClause, whereArgs) > 0
    }

    // ─── SETTINGS HELPERS ─────────────────────────────────────────────────────

    fun getSingleRow(table: String): Map<String, Any?>? {
        return query(table, whereClause = "id = 1") { it }.firstOrNull()
    }

    fun upsertSingleRow(table: String, values: Map<String, Any?>, sync: Boolean = true) {
        insert(table, values + ("id" to 1), sync)
    }

    // ─── SYNC QUEUE ──────────────────────────────────────────────────────────

    fun enqueueSync(tableName: String, rowId: String, operation: String, payload: String = "") {
        val now = System.currentTimeMillis()
        insert("sync_queue", mapOf(
            "table_name" to tableName,
            "row_id" to rowId,
            "operation" to operation,
            "payload" to payload,
            "status" to "pending",
            "attempts" to 0,
            "created_at" to now,
            "_sync_status" to "pending",
            "_updated_at" to now
        ), sync = false)
    }

    fun getPendingSyncItems(limit: Int = 50): List<Map<String, Any?>> {
        return query("sync_queue",
            whereClause = "status = 'pending'",
            orderBy = "created_at ASC",
            mapper = { it }
        ).take(limit)
    }

    fun markSyncCompleted(id: Long) {
        update("sync_queue", mapOf("status" to "completed", "attempts" to 0),
            "id = ?", listOf(id), sync = false)
    }

    fun markSyncFailed(id: Long, error: String) {
        db.execSQL("UPDATE sync_queue SET attempts = attempts + 1, last_error = ?, status = CASE WHEN attempts >= 4 THEN 'failed' ELSE 'pending' END WHERE id = ?",
            listOf(error, id))
    }

    fun clearSyncQueue() {
        db.execSQL("DELETE FROM sync_queue WHERE status = 'completed'")
    }

    // ─── MIGRATION HELPERS ───────────────────────────────────────────────────

    fun tableExists(tableName: String): Boolean {
        return db.rawQueryCount("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", listOf(tableName)) > 0
    }

    fun getPendingSyncRows(table: String): List<Map<String, Any?>> {
        return query(table,
            whereClause = "_sync_status IN ('pending', 'failed')",
            mapper = { it }
        )
    }

    fun markTableSynced(table: String, serverId: String, localRowIdCol: String, localRowId: Any?) {
        db.execSQL("UPDATE $table SET _sync_status = 'synced', _server_id = ? WHERE $localRowIdCol = ?",
            listOf(serverId, localRowId))
    }

    fun markAllTableSynced(table: String) {
        db.execSQL("UPDATE $table SET _sync_status = 'synced' WHERE _sync_status IN ('pending', 'failed')")
    }
}
