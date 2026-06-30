package com.tmrestaurant.db

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

actual class AppDatabase private constructor(private val conn: Connection) {

    actual fun execSQL(sql: String, bindArgs: List<Any?>) {
        conn.prepareStatement(sql).use { stmt ->
            bindArgs.forEachIndexed { i, arg ->
                stmt.setObject(i + 1, arg)
            }
            stmt.execute()
        }
    }

    actual fun <T> rawQuery(sql: String, bindArgs: List<Any?>, mapper: (Map<String, Any?>) -> T): List<T> {
        conn.prepareStatement(sql).use { stmt ->
            bindArgs.forEachIndexed { i, arg ->
                stmt.setObject(i + 1, arg)
            }
            stmt.executeQuery().use { rs ->
                val meta = rs.metaData
                val colCount = meta.columnCount
                val colNames = (1..colCount).map { meta.getColumnLabel(it) }
                val result = mutableListOf<T>()
                while (rs.next()) {
                    val row = colNames.associateWith { col ->
                        val v = rs.getObject(col)
                        rs.wasNull() ?: v
                    }
                    result.add(mapper(row))
                }
                return result
            }
        }
    }

    actual fun rawQueryCount(sql: String, bindArgs: List<Any?>): Int {
        conn.prepareStatement(sql).use { stmt ->
            bindArgs.forEachIndexed { i, arg ->
                stmt.setObject(i + 1, arg)
            }
            stmt.executeQuery().use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    actual fun transaction(block: () -> Unit) {
        val autoCommit = conn.autoCommit
        try {
            conn.autoCommit = false
            block()
            conn.commit()
        } catch (e: Exception) {
            conn.rollback()
            throw e
        } finally {
            conn.autoCommit = autoCommit
        }
    }

    actual fun close() {
        conn.close()
    }

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(dbFile: String = "tmrestaurant.db"): AppDatabase {
            instance?.let { return it }
            Class.forName("org.sqlite.JDBC")
            val dataDir = File(System.getProperty("user.home"), ".tmrestaurant")
            dataDir.mkdirs()
            val dbPath = File(dataDir, dbFile).absolutePath
            val conn = DriverManager.getConnection("jdbc:sqlite:$dbPath")
            conn.autoCommit = true
            val db = AppDatabase(conn)
            instance = db
            return db
        }

        fun resetInstance() {
            instance?.close()
            instance = null
        }
    }
}
