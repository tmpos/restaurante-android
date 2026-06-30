package com.tmrestaurant.platform

/**
 * Resultado de una prueba de periferico ELO.
 */
data class PeripheralTestResult(
    val success: Boolean,
    val message: String
)

data class TicketPrintStyle(
    val textSize: String = "normal",
    val logoWidthMm: String = "51",
    val logoHeightMm: String = "20",
    val showLogo: Boolean = false,
    val logoBytes: ByteArray? = null
)

/**
 * Descubre impresoras Star Micronics conectadas (USB/LAN).
 */
expect suspend fun eloDiscoverPrinters(): List<DiscoveredPrinter>

/**
 * Imprime un ticket de prueba en la impresora Star Micronics.
 */
expect suspend fun eloPrintTestTicket(
    printerIdentifier: String,
    content: String,
    paperWidthMm: String = "80",
    style: TicketPrintStyle = TicketPrintStyle()
): PeripheralTestResult

suspend fun eloPrintTicketCopies(
    printerIdentifier: String,
    content: String,
    paperWidthMm: String = "80",
    copies: Int = 1,
    style: TicketPrintStyle = TicketPrintStyle()
): PeripheralTestResult {
    val totalCopies = copies.coerceIn(1, 5)
    repeat(totalCopies) { index ->
        val result = eloPrintTestTicket(printerIdentifier, content, paperWidthMm, style)
        if (!result.success) {
            return PeripheralTestResult(
                false,
                "Fallo la copia ${index + 1} de $totalCopies: ${result.message}"
            )
        }
    }
    return PeripheralTestResult(true, "$totalCopies copia(s) enviadas a imprimir")
}

/**
 * Abre la gaveta de efectivo (cash drawer) conectada a la impresora Star.
 */
expect suspend fun eloOpenCashDrawer(printerIdentifier: String): PeripheralTestResult

/**
 * Abre la gaveta sin necesidad de seleccionar impresora.
 * Prueba con todos los dispositivos USB conectados.
 */
expect suspend fun eloOpenCashDrawerNoPrinter(): PeripheralTestResult

/**
 * Activa el lector de codigos de barras y espera un scan.
 */
expect suspend fun eloTestBarcodeScanner(): PeripheralTestResult

/**
 * Obtiene la ultima lectura del scanner (si hay alguna).
 */
expect fun eloGetLastScanCode(): String?

/**
 * Limpia el ultimo codigo leido por el scanner.
 */
expect fun eloClearLastScanCode()

/**
 * Activa la captura HID del scanner a nivel de Activity.
 * Cuando esta activo, las teclas del scanner se acumulan en lastScanCode.
 */
expect fun eloSetScannerHidCapture(enabled: Boolean)

/**
 * Re-envia broadcasts de activacion para mantener el scanner encendido.
 * Es una operacion ligera que no re-registra receivers.
 */
expect fun eloKeepScannerAlive()

/**
 * Activa el lector de tarjetas USB (Magnetic Stripe Reader).
 * Los lectores USB Swipe operan en modo HID (teclado).
 */
expect suspend fun eloTestCardReader(): PeripheralTestResult

/**
 * Obtiene la ultima lectura del lector de tarjetas (si hay alguna).
 */
expect fun eloGetLastCardData(): String?

/**
 * Limpia el ultimo dato leido por el lector de tarjetas.
 */
expect fun eloClearLastCardData()

// ==================== CUSTOMER FACING DISPLAY (CFD) ====================

data class CfdTestResult(
    val success: Boolean,
    val message: String,
    val backend: String = "none"
)

/**
 * Detecta el backend del CFD (Elo SDK, Presentation, Preview).
 */
expect suspend fun cfdDetectBackend(): CfdTestResult

/**
 * Verifica si el CFD esta disponible.
 */
expect suspend fun cfdIsAvailable(): CfdTestResult

/**
 * Muestra un mensaje personalizado de 2 lineas en el CFD.
 */
expect suspend fun cfdShowCustomMessage(line1: String, line2: String): CfdTestResult

/**
 * Muestra la pantalla de bienvenida/idle en el CFD.
 */
expect suspend fun cfdShowIdle(welcomeMessage: String = "Bienvenido", idleMessage: String = "Pase sus productos"): CfdTestResult

/**
 * Muestra un producto en el CFD.
 */
expect suspend fun cfdShowProduct(name: String, price: Double, quantity: Int = 1): CfdTestResult

/**
 * Muestra el resumen del carrito en el CFD.
 */
expect suspend fun cfdShowCart(itemCount: Int, subtotal: Double, total: Double): CfdTestResult

/**
 * Muestra los totales de la venta en el CFD.
 */
expect suspend fun cfdShowTotals(subtotal: Double, discount: Double, tax: Double, total: Double): CfdTestResult

/**
 * Muestra informacion de pago en el CFD.
 */
expect suspend fun cfdShowPayment(total: Double, received: Double, change: Double): CfdTestResult

/**
 * Muestra mensaje de agradecimiento en el CFD.
 */
expect suspend fun cfdShowThankYou(message: String = "Gracias!"): CfdTestResult

/**
 * Limpia el CFD y apaga el backlight.
 */
expect suspend fun cfdClear(): CfdTestResult
