package com.tmrestaurant.platform

expect fun localDateKey(timestamp: Long = System.currentTimeMillis()): String

expect fun localDateTimeLabel(timestamp: Long = System.currentTimeMillis()): String
