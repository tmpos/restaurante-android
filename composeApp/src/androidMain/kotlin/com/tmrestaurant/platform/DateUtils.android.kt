package com.tmrestaurant.platform

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale

actual fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

actual fun formatDateKey(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

actual fun currentDateParts(): Triple<Int, Int, Int> {
    val calendar = Calendar.getInstance()
    return Triple(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
