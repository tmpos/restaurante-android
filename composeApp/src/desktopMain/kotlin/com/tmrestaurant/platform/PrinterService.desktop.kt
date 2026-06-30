package com.tmrestaurant.platform

import javax.print.PrintServiceLookup
import javax.print.Doc
import javax.print.SimpleDoc
import javax.print.DocFlavor
import javax.print.attribute.HashPrintRequestAttributeSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun lookupServices(): Array<javax.print.PrintService> {
    return try {
        PrintServiceLookup.lookupPrintServices(null, null) ?: emptyArray()
    } catch (_: Throwable) {
        emptyArray()
    }
}

actual suspend fun discoverPrinters(type: String): List<DiscoveredPrinter> = withContext(Dispatchers.IO) {
    try {
        when (type) {
            "USB", "Red" -> {
                val services = lookupServices()
                services.map { service ->
                    DiscoveredPrinter(
                        name = service.name,
                        identifier = service.name,
                        type = "USB"
                    )
                }
            }
            "Bluetooth" -> {
                // En Desktop/JVM no hay API nativa de Bluetooth.
                // Las impresoras Bluetooth emparejadas aparecen como impresoras del sistema
                // y ya se listan via PrintServiceLookup en la busqueda USB/Red.
                emptyList()
            }
            else -> emptyList()
        }
    } catch (_: Throwable) {
        emptyList()
    }
}

actual suspend fun printTestPage(printerName: String, content: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val services = lookupServices()
        val printService = services.firstOrNull { it.name == printerName }
            ?: return@withContext false

        val flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE
        val bytes = content.toByteArray(Charsets.UTF_8)
        val doc: Doc = SimpleDoc(bytes, flavor, null)
        val attributes = HashPrintRequestAttributeSet()

        val job = printService.createPrintJob()
        job.print(doc, attributes)
        true
    } catch (_: Throwable) {
        false
    }
}

actual fun printWithSystemDialog(title: String, content: String) {
    try {
        val services = lookupServices()
        val service = services.firstOrNull() ?: return
        val flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE
        val bytes = content.toByteArray(Charsets.UTF_8)
        val doc: Doc = SimpleDoc(bytes, flavor, null)
        val job = service.createPrintJob()
        job.print(doc, HashPrintRequestAttributeSet())
    } catch (_: Throwable) { }
}

actual suspend fun openCashDrawer(printerName: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val services = lookupServices()
        val printService = services.firstOrNull { it.name == printerName }
            ?: return@withContext false

        // Comando ESC/POS para abrir gaveta: ESC p 0 50 250
        val openDrawerCmd = byteArrayOf(0x1B, 0x70, 0x00, 0x32, 0xFA.toByte())
        val flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE
        val doc: Doc = SimpleDoc(openDrawerCmd, flavor, null)
        val job = printService.createPrintJob()
        job.print(doc, HashPrintRequestAttributeSet())
        true
    } catch (_: Throwable) {
        false
    }
}
