package com.tmrestaurant.platform

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun localDateKey(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))

actual fun localDateTimeLabel(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(timestamp))
