package com.tmrestaurant.platform

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

actual fun formatDateTime(timestamp: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: Exception) {
        timestamp.toString()
    }
}

actual fun formatDateKey(timestamp: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: Exception) {
        ""
    }
}

actual fun currentDateParts(): Triple<Int, Int, Int> {
    val today = LocalDate.now()
    return Triple(today.year, today.monthValue, today.dayOfMonth)
}
