package com.tmrestaurant.db

expect class AppDatabase {
    fun execSQL(sql: String, bindArgs: List<Any?> = emptyList())
    fun <T> rawQuery(sql: String, bindArgs: List<Any?> = emptyList(), mapper: (Map<String, Any?>) -> T): List<T>
    fun rawQueryCount(sql: String, bindArgs: List<Any?> = emptyList()): Int
    fun transaction(block: () -> Unit)
    fun close()
}
