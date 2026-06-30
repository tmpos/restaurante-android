package com.tmrestaurant.platform

expect fun formatDateTime(timestamp: Long): String
expect fun formatDateKey(timestamp: Long): String
expect fun currentDateParts(): Triple<Int, Int, Int>
