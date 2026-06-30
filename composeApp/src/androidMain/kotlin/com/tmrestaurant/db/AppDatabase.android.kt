package com.tmrestaurant.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.tmrestaurant.platform.appContext

actual class AppDatabase private constructor(private val db: SQLiteDatabase) {

    actual fun execSQL(sql: String, bindArgs: List<Any?>) {
        val args = bindArgs.map { it }.toTypedArray()
        db.execSQL(sql, args)
    }

    actual fun <T> rawQuery(sql: String, bindArgs: List<Any?>, mapper: (Map<String, Any?>) -> T): List<T> {
        val args = bindArgs.map { it?.toString() }.toTypedArray()
        val cursor: Cursor = db.rawQuery(sql, args)
        val result = mutableListOf<T>()
        cursor.use { c ->
            val colNames = c.columnNames
            while (c.moveToNext()) {
                val row = colNames.associateWith { col ->
                    val idx = c.getColumnIndex(col)
                    if (idx < 0) null
                    else when (c.getType(idx)) {
                        Cursor.FIELD_TYPE_NULL -> null
                        Cursor.FIELD_TYPE_INTEGER -> c.getLong(idx)
                        Cursor.FIELD_TYPE_FLOAT -> c.getDouble(idx)
                        Cursor.FIELD_TYPE_BLOB -> c.getBlob(idx)
                        else -> c.getString(idx)
                    }
                }
                result.add(mapper(row))
            }
        }
        return result
    }

    actual fun rawQueryCount(sql: String, bindArgs: List<Any?>): Int {
        val args = bindArgs.map { it?.toString() }.toTypedArray()
        val cursor = db.rawQuery(sql, args)
        cursor.use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    actual fun transaction(block: () -> Unit) {
        db.beginTransaction()
        try {
            block()
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    actual fun close() {
        db.close()
    }

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(): AppDatabase {
            instance?.let { return it }
            val ctx = appContext ?: throw IllegalStateException("Android context not available")
            val sqLiteDb = ctx.openOrCreateDatabase("tmrestaurant.db", 0, null)
            sqLiteDb.setVersion(DatabaseSchema.VERSION)
            val db = AppDatabase(sqLiteDb)
            instance = db
            return db
        }

        fun resetInstance() {
            instance?.close()
            instance = null
        }
    }
}
