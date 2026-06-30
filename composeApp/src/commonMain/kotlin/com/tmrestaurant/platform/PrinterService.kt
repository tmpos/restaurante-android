package com.tmrestaurant.platform

data class DiscoveredPrinter(
    val name: String,
    val identifier: String,
    val type: String // "USB", "Bluetooth", "Red"
)

expect suspend fun discoverPrinters(type: String): List<DiscoveredPrinter>

expect suspend fun printTestPage(printerName: String, content: String): Boolean

expect suspend fun openCashDrawer(printerName: String): Boolean

expect fun printWithSystemDialog(title: String, content: String)
