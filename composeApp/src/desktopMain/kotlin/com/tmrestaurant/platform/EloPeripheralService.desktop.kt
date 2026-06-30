package com.tmrestaurant.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.print.SimpleDoc
import javax.print.DocFlavor
import javax.print.attribute.HashPrintRequestAttributeSet

actual suspend fun eloDiscoverPrinters(): List<DiscoveredPrinter> = withContext(Dispatchers.IO) {
    try {
        val services = javax.print.PrintServiceLookup.lookupPrintServices(null, null) ?: emptyArray()
        services.map { service ->
            DiscoveredPrinter(
                name = service.name,
                identifier = service.name,
                type = "USB"
            )
        }
    } catch (_: Throwable) {
        emptyList()
    }
}

actual suspend fun eloPrintTestTicket(
    printerIdentifier: String,
    content: String,
    paperWidthMm: String,
    style: TicketPrintStyle
): PeripheralTestResult =
    withContext(Dispatchers.IO) {
        try {
            val services = javax.print.PrintServiceLookup.lookupPrintServices(null, null) ?: emptyArray()
            val printService = services.firstOrNull { it.name == printerIdentifier }
                ?: return@withContext PeripheralTestResult(false, "Impresora '$printerIdentifier' no encontrada")

            val out = java.io.ByteArrayOutputStream()
            val separator = if (paperWidthMm.trim() == "80") {
                "================================================\n"
            } else {
                "================================\n"
            }
            // Initialize printer
            out.write(byteArrayOf(0x1B, 0x40))
            // Center align + bold + double size header
            out.write(byteArrayOf(0x1B, 0x61, 0x01))
            out.write(byteArrayOf(0x1B, 0x45, 0x01))
            out.write(byteArrayOf(0x1D, 0x21, 0x11))
            out.write("TICKET DE PRUEBA\n".toByteArray())
            // Normal size + bold off + left align
            out.write(byteArrayOf(0x1D, 0x21, 0x00))
            out.write(byteArrayOf(0x1B, 0x45, 0x00))
            out.write(byteArrayOf(0x1B, 0x61, 0x00))
            out.write(separator.toByteArray())
            // Content
            content.lines().forEach { line ->
                out.write("$line\n".toByteArray())
            }
            out.write("\n".toByteArray())
            // Center footer
            out.write(byteArrayOf(0x1B, 0x61, 0x01))
            out.write(byteArrayOf(0x1B, 0x45, 0x01))
            out.write("TM-RESTAURANTE\n".toByteArray())
            out.write(byteArrayOf(0x1B, 0x45, 0x00))
            out.write("Gracias por su compra!\n".toByteArray())
            out.write(separator.toByteArray())
            // Feed 5 lines + partial cut
            out.write(byteArrayOf(0x1B, 0x64, 0x05))
            out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00))

            val flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE
            val doc = SimpleDoc(out.toByteArray(), flavor, null)
            val job = printService.createPrintJob()
            job.print(doc, HashPrintRequestAttributeSet())
            PeripheralTestResult(true, "Ticket de prueba enviado a $printerIdentifier")
        } catch (e: Throwable) {
            PeripheralTestResult(false, "Error al imprimir: ${e.message}")
        }
    }

actual suspend fun eloOpenCashDrawer(printerIdentifier: String): PeripheralTestResult =
    withContext(Dispatchers.IO) {
        try {
            val services = javax.print.PrintServiceLookup.lookupPrintServices(null, null) ?: emptyArray()
            val printService = services.firstOrNull { it.name == printerIdentifier }
                ?: return@withContext PeripheralTestResult(false, "Impresora '$printerIdentifier' no encontrada")

            // Comando ESC/POS para abrir gaveta: ESC p 0 50 250
            val openDrawerCmd = byteArrayOf(0x1B, 0x70, 0x00, 0x32, 0xFA.toByte())
            val flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE
            val doc = SimpleDoc(openDrawerCmd, flavor, null)
            val job = printService.createPrintJob()
            job.print(doc, HashPrintRequestAttributeSet())
            PeripheralTestResult(true, "Comando de apertura enviado a gaveta via $printerIdentifier")
        } catch (e: Throwable) {
            PeripheralTestResult(false, "Error al abrir gaveta: ${e.message}")
        }
    }

actual suspend fun eloTestBarcodeScanner(): PeripheralTestResult {
    return PeripheralTestResult(
        false,
        "Scanner de codigos: En escritorio los scanners USB HID envian datos como teclado. " +
                "Enfoque un campo de texto y escanee un codigo de barras."
    )
}

actual fun eloGetLastScanCode(): String? = null
actual fun eloClearLastScanCode() {}
actual fun eloSetScannerHidCapture(enabled: Boolean) {}
actual fun eloKeepScannerAlive() {}

actual suspend fun eloOpenCashDrawerNoPrinter(): PeripheralTestResult =
    PeripheralTestResult(false, "Gaveta: Seleccione una impresora y use 'Abrir Gaveta' desde la seccion de impresora.")

actual suspend fun eloTestCardReader(): PeripheralTestResult {
    return PeripheralTestResult(
        true,
        "Lector de tarjetas: En escritorio los lectores USB Swipe envian datos como teclado. " +
                "Enfoque el campo de texto y pase una tarjeta por el lector."
    )
}

actual fun eloGetLastCardData(): String? = null
actual fun eloClearLastCardData() {}

// ==================== CFD (Customer Facing Display) - Desktop stubs ====================

actual suspend fun cfdDetectBackend(): CfdTestResult =
    CfdTestResult(false, "Pantalla de cliente no disponible en escritorio. Funciona solo en terminales ELO.", "none")

actual suspend fun cfdIsAvailable(): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")

actual suspend fun cfdShowCustomMessage(line1: String, line2: String): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")

actual suspend fun cfdShowIdle(welcomeMessage: String, idleMessage: String): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")

actual suspend fun cfdShowProduct(name: String, price: Double, quantity: Int): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")

actual suspend fun cfdShowCart(itemCount: Int, subtotal: Double, total: Double): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")

actual suspend fun cfdShowTotals(subtotal: Double, discount: Double, tax: Double, total: Double): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")

actual suspend fun cfdShowPayment(total: Double, received: Double, change: Double): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")

actual suspend fun cfdShowThankYou(message: String): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")

actual suspend fun cfdClear(): CfdTestResult =
    CfdTestResult(false, "No disponible en escritorio", "none")
