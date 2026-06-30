package com.tmrestaurant.platform

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.content.BroadcastReceiver
import com.elo.device.DeviceManager
import com.elo.device.peripherals.Printer as EloPrinter
import com.elo.device.peripherals.CashDrawer as EloCashDrawer
import com.elo.device.peripherals.CFD as EloCFD
import com.elo.device.enums.EloPlatform
import com.elotouch.library.EloPeripheralManager
import com.elotouch.library.EloPeripheralEventListener
import com.elo.device.enums.Alignment
import com.elo.device.enums.TriggerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

var appContext: Context? = null
var currentActivity: android.app.Activity? = null
@Volatile private var lastScanCode: String? = null
private var scannerReceiver: BroadcastReceiver? = null
@Volatile var scannerHidCaptureEnabled: Boolean = false
    private set
private val scannerHidBuffer: StringBuilder = StringBuilder()
private var lastCardData: String? = null
private var cardReaderReceiver: BroadcastReceiver? = null
private var magTekMsr: Any? = null
private var magTekHandler: Handler? = null

// ==================== CFD (Customer Facing Display) STATE ====================
private const val CFD_LINE_W = 16
private var eloCfd: EloCFD? = null
private var cfdLegacy: Any? = null
private var cfdBackend: String = "none"
@Volatile private var cfdLastL1: String = "Bienvenido"
@Volatile private var cfdLastL2: String = "Pase sus productos"
@Volatile private var cfdLastWriteMs: Long = 0
private val cfdIoLock = Any()
private val cfdScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
private var cfdKeepaliveFuture: ScheduledFuture<*>? = null

actual fun eloGetLastScanCode(): String? = lastScanCode
actual fun eloClearLastScanCode() { lastScanCode = null }

actual fun eloSetScannerHidCapture(enabled: Boolean) {
    scannerHidCaptureEnabled = enabled
    if (!enabled) scannerHidBuffer.clear()
}

/**
 * Called from MainActivity.dispatchKeyEvent to feed HID scanner keys.
 */
fun onHidKeyEvent(keyCode: Int, action: Int, unicodeChar: Int): Boolean {
    if (!scannerHidCaptureEnabled) return false
    // Only process ACTION_DOWN, let ACTION_UP pass through
    if (action != android.view.KeyEvent.ACTION_DOWN) return false
    android.util.Log.d("SCANNER_HID", "keyCode=$keyCode unicodeChar=$unicodeChar buffer=${scannerHidBuffer}")
    if (keyCode == android.view.KeyEvent.KEYCODE_ENTER || keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER) {
        val code = scannerHidBuffer.toString().trim()
        android.util.Log.d("SCANNER_HID", "ENTER detected, code='$code'")
        if (code.isNotEmpty()) {
            lastScanCode = code
        }
        scannerHidBuffer.clear()
        reactivateScanner()
        return true
    }
    // Try unicodeChar first, fallback to keyCode mapping for digits
    val char = when {
        unicodeChar != 0 -> unicodeChar.toChar()
        keyCode in android.view.KeyEvent.KEYCODE_0..android.view.KeyEvent.KEYCODE_9 ->
            ('0' + (keyCode - android.view.KeyEvent.KEYCODE_0))
        keyCode in android.view.KeyEvent.KEYCODE_A..android.view.KeyEvent.KEYCODE_Z ->
            ('a' + (keyCode - android.view.KeyEvent.KEYCODE_A))
        keyCode == android.view.KeyEvent.KEYCODE_MINUS -> '-'
        keyCode == android.view.KeyEvent.KEYCODE_PERIOD -> '.'
        keyCode == android.view.KeyEvent.KEYCODE_SPACE -> ' '
        else -> null
    }
    if (char != null) {
        scannerHidBuffer.append(char)
        return true
    }
    return false
}

actual fun eloKeepScannerAlive() { reactivateScanner() }

private fun reactivateScanner() {
    val ctx = appContext ?: return
    listOf(
        "com.elotouch.peripheral.action.SCANNER_ON", "com.oem.zbcr.TRIGGER_ON",
        "com.oem.zbcr.SET_VCOM_MODE", "com.oem.zbcr.SET_AUTO_AIM_ON",
        "com.oem.zbcr.PRESENTATION_MODE_ON", "com.elotouch.peripheral.action.BCR_AUTO_MODE"
    ).forEach { try { ctx.sendBroadcast(Intent(it)) } catch (_: Throwable) {} }
    // Also try re-enabling via SDK
    try {
        val dm = DeviceManager.getInstance(ctx.applicationContext)
        val bcr = dm.getBarCodeReader()
        bcr?.setEnabled(true)
        bcr?.setTriggerMode(TriggerMode.AUTO)
        // Re-register callback in case it was lost
        bcr?.setBarcodeReadCallback(object : com.elo.device.peripherals.BarCodeReader.BarcodeReadCallback {
            override fun onBarcodeRead(data: ByteArray) {
                val code = String(data, Charsets.UTF_8).trim()
                android.util.Log.d("SCANNER_SDK", "BarcodeReadCallback (reactivate): '$code'")
                if (code.isNotBlank()) lastScanCode = code
            }
        })
    } catch (_: Throwable) {}
}

actual fun eloGetLastCardData(): String? = lastCardData
actual fun eloClearLastCardData() { lastCardData = null }

private const val MAGTEK_MSG_CARD_SWIPED = 3
private const val MAGTEK_MSG_CONNECTED = 4
private const val MAGTEK_MSG_DISCONNECTED = 5

private fun nvl(value: Any?): String = value?.toString() ?: ""

private fun hexToAscii(hex: String): String {
    if (hex.length < 2 || !hex.matches(Regex("[0-9A-Fa-f]+"))) return hex
    return try {
        buildString {
            var i = 0
            while (i + 1 < hex.length) {
                append(hex.substring(i, i + 2).toInt(16).toChar())
                i += 2
            }
        }
    } catch (_: Exception) {
        hex
    }
}

private fun maskPan(data: String): String =
    Regex("(\\d{12,19})").replace(data) { match ->
        val pan = match.value
        if (pan.length > 8) pan.take(4) + "*".repeat(pan.length - 8) + pan.takeLast(4) else "*".repeat(pan.length)
    }

private fun setupMagTekDynamag(ctx: Context): PeripheralTestResult {
    return try {
        val cls = Class.forName("com.magtek.mobile.android.libDynamag.MagTeklibDynamag")
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MAGTEK_MSG_CONNECTED -> android.util.Log.i("MSR", "MagTek connected")
                    MAGTEK_MSG_DISCONNECTED -> android.util.Log.i("MSR", "MagTek disconnected")
                    MAGTEK_MSG_CARD_SWIPED -> {
                        try {
                            val msr = magTekMsr ?: return
                            val rawObj = msg.obj?.toString().orEmpty()
                            msr.javaClass.getMethod("setCardData", String::class.java).invoke(msr, rawObj)

                            val track1 = hexToAscii(nvl(msr.javaClass.getMethod("getTrack1").invoke(msr)))
                            val track2 = hexToAscii(nvl(msr.javaClass.getMethod("getTrack2").invoke(msr)))
                            val track1Masked = hexToAscii(nvl(msr.javaClass.getMethod("getTrack1Masked").invoke(msr)))
                            val track2Masked = hexToAscii(nvl(msr.javaClass.getMethod("getTrack2Masked").invoke(msr)))
                            val best = listOf(track1, track2, track1Masked, track2Masked).firstOrNull { it.length > 4 }.orEmpty()

                            android.util.Log.i("MSR", "MagTek swipe track1=${maskPan(track1)} track2=${maskPan(track2)}")
                            lastCardData = best.ifBlank { "$track1$track2" }.trim()
                        } catch (e: Exception) {
                            android.util.Log.e("MSR", "MagTek swipe parse error: ${e.message}")
                            lastCardData = "ERROR: ${e.message}"
                        }
                    }
                }
            }
        }
        magTekHandler = handler

        val msr = magTekMsr ?: cls.getConstructor(Context::class.java, Handler::class.java)
            .newInstance(ctx.applicationContext, handler)
            .also { magTekMsr = it }

        val connected = runCatching {
            msr.javaClass.getMethod("isDeviceConnected").invoke(msr) as? Boolean ?: false
        }.getOrDefault(false)
        if (!connected) {
            msr.javaClass.getMethod("openDevice").invoke(msr)
        }

        PeripheralTestResult(true, "Lector MagTek Dynamag activado. Pase la tarjeta.")
    } catch (e: ClassNotFoundException) {
        PeripheralTestResult(false, "Falta magtek-usb.jar. Copielo a composeApp/libs y reinstale la app.")
    } catch (e: Exception) {
        PeripheralTestResult(false, "Error MagTek: ${e.message}")
    }
}

// ==================== STAR RASTER CONSTANTS ====================
private const val STAR_VENDOR_ID = 0x0519
private const val STAR_PRODUCT_ID = 0x0003
private const val RASTER_WIDTH_DOTS_57MM = 384
private const val RASTER_WIDTH_DOTS_80MM = 576
private const val USB_TIMEOUT = 5000

private fun rasterWidthDots(paperWidthMm: String): Int =
    if (paperWidthMm.trim() == "80") RASTER_WIDTH_DOTS_80MM else RASTER_WIDTH_DOTS_57MM

private fun rasterWidthBytes(paperWidthMm: String): Int =
    rasterWidthDots(paperWidthMm) / 8

private fun normalizedPaperWidth(paperWidthMm: String): String =
    if (paperWidthMm.trim() == "80") "80" else "57"

// ==================== STAR RASTER ENGINE ====================

/**
 * Renders text to a Bitmap using Android Canvas (monospace font).
 * This is exactly how the Supermercado project prints to the Star TSP100III.
 */
private fun textToBitmap(
    text: String,
    paperWidthMm: String = "80",
    fontSize: Float = 22f,
    bold: Boolean = false,
    style: TicketPrintStyle = TicketPrintStyle()
): Bitmap {
    val width = rasterWidthDots(paperWidthMm)
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = fontSize
        typeface = if (bold) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                   else Typeface.MONOSPACE
    }
    @Suppress("DEPRECATION")
    val layout = StaticLayout(
        text, textPaint, width,
        Layout.Alignment.ALIGN_NORMAL,
        1.0f, 0f, false
    )
    val logo = if (style.showLogo && style.logoBytes?.isNotEmpty() == true) {
        runCatching {
            BitmapFactory.decodeByteArray(style.logoBytes, 0, style.logoBytes.size)
        }.getOrNull()
    } else null
    val maxLogoWidthMm = if (normalizedPaperWidth(paperWidthMm) == "80") 72f else 48f
    val logoWidth = minOf(
        ((style.logoWidthMm.toFloatOrNull() ?: 51f).coerceIn(10f, maxLogoWidthMm) * 8f).toInt(),
        width
    )
    val logoHeight = ((style.logoHeightMm.toFloatOrNull() ?: 20f).coerceIn(5f, 50f) * 8f).toInt()
    val logoSpacing = if (logo != null) 12 else 0
    val topMargin = 8
    val bottomFeed = 24
    val bitmapHeight = layout.height + topMargin + bottomFeed +
        if (logo != null) logoHeight + logoSpacing else 0
    val bitmap = Bitmap.createBitmap(width, bitmapHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    var textTop = topMargin
    if (logo != null) {
        val left = (width - logoWidth) / 2
        val destination = android.graphics.Rect(left, topMargin, left + logoWidth, topMargin + logoHeight)
        canvas.drawBitmap(logo, null, destination, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        textTop += logoHeight + logoSpacing
        logo.recycle()
    }
    canvas.save()
    canvas.translate(0f, textTop.toFloat())
    layout.draw(canvas)
    canvas.restore()
    return bitmap
}

/**
 * Converts a Bitmap to Star Raster byte data.
 * Protocol: ESC @ (init), ESC * r R (raster mode), ESC * r A (start),
 * b <width> 0x00 <row bytes> (per scanline), ESC * r B (end raster).
 */
private fun bitmapToStarRaster(bitmap: Bitmap, paperWidthMm: String = "80"): ByteArray {
    val widthBytes = rasterWidthBytes(paperWidthMm)
    val output = ByteArrayOutputStream()
    // Initialize printer + enter raster mode
    output.write(byteArrayOf(
        0x1B, 0x40,                          // ESC @ - Initialize
        0x1B, 0x2A, 0x72, 0x52, 0x00,       // ESC * r R 0 - Initialize raster mode
        0x1B, 0x2A, 0x72, 0x41              // ESC * r A - Enter raster mode
    ))
    // Send each scanline
    for (y in 0 until bitmap.height) {
        output.write(byteArrayOf(0x62, widthBytes.toByte(), 0x00))
        for (byteIdx in 0 until widthBytes) {
            var b = 0
            for (bit in 0 until 8) {
                val x = byteIdx * 8 + bit
                if (x < bitmap.width) {
                    val pixel = bitmap.getPixel(x, y)
                    val lum = (Color.red(pixel) * 299 + Color.green(pixel) * 587 + Color.blue(pixel) * 114) / 1000
                    if (lum < 128) b = b or (0x80 shr bit)
                }
            }
            output.write(b)
        }
    }
    // End raster mode
    output.write(byteArrayOf(0x1B, 0x2A, 0x72, 0x42))
    output.write(byteArrayOf(0x1B, 0x64, 0x05)) // Feed 5
    // Paper cut
    output.write(byteArrayOf(0x1B, 0x64, 0x02))
    output.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00))
    return output.toByteArray()
}

/**
 * Renders text as Star Raster data ready to send via USB.
 */
private fun textToStarRaster(
    text: String,
    paperWidthMm: String = "80",
    style: TicketPrintStyle = TicketPrintStyle()
): ByteArray {
    val width = normalizedPaperWidth(paperWidthMm)
    val fontSize = when (style.textSize.lowercase()) {
        "small" -> 18f
        "large" -> 28f
        else -> 22f
    }
    val bitmap = textToBitmap(text, width, fontSize = fontSize, style = style)
    val data = bitmapToStarRaster(bitmap, width)
    bitmap.recycle()
    return data
}

/**
 * Finds the Star TSP100III USB device by vendor/product ID.
 */
private fun findStarDevice(usbMgr: UsbManager): UsbDevice? {
    return usbMgr.deviceList.values.firstOrNull {
        it.vendorId == STAR_VENDOR_ID && it.productId == STAR_PRODUCT_ID
    }
}

/**
 * Finds a BULK OUT endpoint on a USB device.
 */
private fun findBulkOutEndpoint(device: UsbDevice): Pair<UsbInterface, UsbEndpoint>? {
    for (i in 0 until device.interfaceCount) {
        val iface = device.getInterface(i)
        for (j in 0 until iface.endpointCount) {
            val ep = iface.getEndpoint(j)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == UsbConstants.USB_DIR_OUT)
                return Pair(iface, ep)
        }
    }
    return null
}

/**
 * Sends raw bytes to a USB device via bulk transfer (chunked).
 */
private fun sendUsbData(usbMgr: UsbManager, device: UsbDevice, data: ByteArray): Boolean {
    val bulkOut = findBulkOutEndpoint(device) ?: return false
    val conn = usbMgr.openDevice(device) ?: return false
    try {
        if (!conn.claimInterface(bulkOut.first, true)) return false
        val maxChunk = bulkOut.second.maxPacketSize.coerceAtLeast(512)
        var offset = 0
        while (offset < data.size) {
            val chunkSize = minOf(maxChunk, data.size - offset)
            val chunk = data.copyOfRange(offset, offset + chunkSize)
            val result = conn.bulkTransfer(bulkOut.second, chunk, chunk.size, USB_TIMEOUT)
            if (result < 0) return false
            offset += chunkSize
        }
        conn.releaseInterface(bulkOut.first)
        return true
    } catch (_: Exception) {
        return false
    } finally {
        try { conn.close() } catch (_: Exception) {}
    }
}

// ==================== PRINTER DISCOVERY ====================

actual suspend fun eloDiscoverPrinters(): List<DiscoveredPrinter> = withContext(Dispatchers.IO) {
    val ctx = appContext ?: return@withContext emptyList()
    val printers = mutableListOf<DiscoveredPrinter>()
    // Always add the built-in ELO option
    printers.add(DiscoveredPrinter("Impresora Interna ELO (Star TSP100III)", "elo_builtin", "ELO"))
    // List USB devices
    try {
        val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
        usbMgr.deviceList.values.forEach { dev ->
            if (dev.interfaceCount > 0) {
                val isStar = dev.vendorId == STAR_VENDOR_ID
                val name = dev.productName ?: "USB Device"
                val label = if (isStar) "$name (Star Micronics)" else "$name (${dev.vendorId}:${dev.productId})"
                printers.add(DiscoveredPrinter(
                    name = label,
                    identifier = "${dev.vendorId}:${dev.productId}",
                    type = if (isStar) "Star USB" else "USB"
                ))
            }
        }
    } catch (_: Exception) {}
    printers
}

// ==================== PRINT TEST TICKET ====================

actual suspend fun eloPrintTestTicket(
    printerIdentifier: String,
    content: String,
    paperWidthMm: String,
    style: TicketPrintStyle
): PeripheralTestResult =
    withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext PeripheralTestResult(false, "Contexto no disponible")
        try {
            if (printerIdentifier == "elo_builtin") {
                // STRATEGY 1: Try ELO SDK printer.print(String)
                if (!style.showLogo && style.textSize.equals("normal", ignoreCase = true)) {
                    try {
                        val dm = DeviceManager.getInstance(ctx.applicationContext)
                        val printer: EloPrinter = dm.getPrinter()
                        printer.print(content)
                        printer.feed(1)
                        return@withContext PeripheralTestResult(true, "Ticket enviado via SDK ELO")
                    } catch (sdkErr: Throwable) {
                        android.util.Log.w("ELO", "SDK printer.print() failed: ${sdkErr.message}, trying Star Raster USB...")
                    }
                }

                // STRATEGY 2: Star Raster via USB (text → bitmap → raster)
                // This is what actually works with the Star TSP100III
                try {
                    val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
                    val starDev = findStarDevice(usbMgr)
                    if (starDev != null) {
                        val ticketText = buildString {
                            appendLine("    *** TICKET DE PRUEBA ***")
                            appendLine("  ================================")
                            appendLine("")
                            content.lines().forEach { appendLine(it) }
                            appendLine("")
                            appendLine("  ================================")
                            appendLine("      TM-RESTAURANTE POS")
                            appendLine("   Sistema de Punto de Venta")
                            appendLine("  ================================")
                            appendLine("  Si puede leer este ticket,")
                            appendLine("  la impresora esta funcionando")
                            appendLine("  correctamente.")
                            appendLine("  ================================")
                            appendLine("")
                            appendLine("    Gracias por su compra!")
                            appendLine("")
                        }
                        val rasterData = textToStarRaster(ticketText, paperWidthMm, style)
                        if (sendUsbData(usbMgr, starDev, rasterData)) {
                            return@withContext PeripheralTestResult(true, "Ticket enviado via Star Raster USB")
                        }
                    }
                } catch (e: Throwable) {
                    android.util.Log.w("ELO", "Star Raster failed: ${e.message}")
                }

                // STRATEGY 3: Find any USB printer (class 7) and try Star Raster
                try {
                    val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
                    for ((_, dev) in usbMgr.deviceList) {
                        if (dev.interfaceCount == 0) continue
                        val iface = dev.getInterface(0)
                        if (iface.interfaceClass == 7 || dev.vendorId == STAR_VENDOR_ID) {
                            val ticketText = "    *** TICKET DE PRUEBA ***\n================================\n\n$content\n\n================================\n    Gracias por su compra!\n\n"
                            val rasterData = textToStarRaster(ticketText, paperWidthMm, style)
                            if (sendUsbData(usbMgr, dev, rasterData)) {
                                return@withContext PeripheralTestResult(true, "Ticket enviado via USB (clase impresora)")
                            }
                        }
                    }
                } catch (_: Throwable) {}

                return@withContext PeripheralTestResult(false, "No se pudo imprimir. Verifique que la impresora Star esta conectada y tiene permiso USB.")
            }

            // External USB printer by vendor:product ID
            val parts = printerIdentifier.split(":")
            if (parts.size >= 2) {
                val vid = parts[0].toIntOrNull()
                val pid = parts[1].toIntOrNull()
                if (vid != null && pid != null) {
                    val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
                    val device = usbMgr.deviceList.values.firstOrNull { it.vendorId == vid && it.productId == pid }
                    if (device != null) {
                        // Try Star Raster first for Star devices, then ESC/POS for others
                        if (vid == STAR_VENDOR_ID) {
                            val rasterData = textToStarRaster(content, paperWidthMm, style)
                            if (sendUsbData(usbMgr, device, rasterData)) {
                                return@withContext PeripheralTestResult(true, "Ticket enviado en ${normalizedPaperWidth(paperWidthMm)}mm via Star Raster")
                            }
                        }
                        // ESC/POS fallback for non-Star printers
                        val escPosData = buildEscPosTicket(content, paperWidthMm)
                        if (sendUsbData(usbMgr, device, escPosData)) {
                            return@withContext PeripheralTestResult(true, "Ticket enviado via ESC/POS")
                        }
                        return@withContext PeripheralTestResult(false, "Error al enviar datos a la impresora")
                    }
                }
            }
            // Fallback for saved display names such as "POS80 Printer".
            // Raster must run first because the SDK text method ignores logo and font sizes.
            val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
            val starDev = findStarDevice(usbMgr)
            if (starDev != null) {
                val rasterData = textToStarRaster(content, paperWidthMm, style)
                if (sendUsbData(usbMgr, starDev, rasterData)) {
                    android.util.Log.i(
                        "ELO",
                        "Printed styled raster: textSize=${style.textSize}, logo=${style.showLogo}, " +
                            "logoSize=${style.logoWidthMm}x${style.logoHeightMm}mm"
                    )
                    return@withContext PeripheralTestResult(true, "Ticket enviado via Star Raster USB")
                }
            }
            for ((_, dev) in usbMgr.deviceList) {
                if (dev.interfaceCount > 0) {
                    try {
                        val rasterData = textToStarRaster(content, paperWidthMm, style)
                        if (sendUsbData(usbMgr, dev, rasterData)) {
                            return@withContext PeripheralTestResult(true, "Ticket enviado via USB")
                        }
                    } catch (_: Throwable) {}
                }
            }
            try {
                val dm = DeviceManager.getInstance(ctx.applicationContext)
                val eloPrinter: EloPrinter = dm.getPrinter()
                eloPrinter.print(content)
                eloPrinter.feed(1)
                return@withContext PeripheralTestResult(
                    true,
                    "Ticket enviado via SDK ELO sin estilos raster"
                )
            } catch (_: Throwable) {}
            return@withContext PeripheralTestResult(false, "No se pudo imprimir. Verifique que la impresora Star esta conectada y tiene permiso USB.")
        } catch (e: Throwable) { PeripheralTestResult(false, "Error: ${e.message}") }
    }

// ==================== CASH DRAWER ====================

actual suspend fun eloOpenCashDrawer(printerIdentifier: String): PeripheralTestResult =
    withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext PeripheralTestResult(false, "Contexto no disponible")
        try { eloOpenCashDrawerNoPrinter() } catch (e: Throwable) { PeripheralTestResult(false, "Error: ${e.message}") }
    }

actual suspend fun eloOpenCashDrawerNoPrinter(): PeripheralTestResult = withContext(Dispatchers.IO) {
    val ctx = appContext ?: return@withContext PeripheralTestResult(false, "Contexto no disponible")
    try {
        // PRIMARY: ELO SDK CashDrawer
        try {
            val dm = DeviceManager.getInstance(ctx.applicationContext)
            val drawer: EloCashDrawer = dm.getCashDrawer()
            drawer.open()
            return@withContext PeripheralTestResult(true, "Gaveta abierta via SDK ELO")
        } catch (e: Throwable) {
            android.util.Log.w("ELO", "SDK drawer failed: ${e.message}")
        }
        // FALLBACK: USB cash drawer commands
        val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
        for ((_, dev) in usbMgr.deviceList) {
            try {
                val conn = usbMgr.openDevice(dev) ?: continue
                try {
                    if (dev.interfaceCount > 0) {
                        val iface = dev.getInterface(0)
                        conn.claimInterface(iface, true)
                        val ep = (0..<iface.endpointCount).firstOrNull { iface.getEndpoint(it).direction == UsbConstants.USB_DIR_OUT }?.let { iface.getEndpoint(it) } ?: continue
                        for (cmd in listOf(
                            byteArrayOf(0x1B, 0x70, 0x00, 0x32, 0xFA.toInt().toByte()),
                            byteArrayOf(0x07),
                            byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toInt().toByte())
                        )) {
                            if (conn.bulkTransfer(ep, cmd, cmd.size, 1000) >= 0)
                                return@withContext PeripheralTestResult(true, "Gaveta abierta via USB")
                        }
                    }
                } finally { conn.close() }
            } catch (_: Exception) {}
        }
        PeripheralTestResult(false, "No se pudo abrir la gaveta. Verifique que el ELO tiene gaveta conectada.")
    } catch (e: Exception) { PeripheralTestResult(false, "Error: ${e.message}") }
}

// ==================== BARCODE SCANNER ====================

actual suspend fun eloTestBarcodeScanner(): PeripheralTestResult = withContext(Dispatchers.IO) {
    val ctx = appContext ?: return@withContext PeripheralTestResult(false, "Contexto no disponible")
    try {
        scannerReceiver?.let { try { ctx.unregisterReceiver(it) } catch (_: Exception) {} }
        val filter = IntentFilter().apply {
            addAction("com.oem.zbcr.BCRDATA"); addAction("com.elotouch.peripheral.action.SCANNER_DATA")
            addAction("com.elo.bcr.DATA"); addAction("com.elotouch.peripheral.action.BCR_DATA")
            addAction("com.elotouch.paypoint.action.BCR_DATA"); addAction("com.elotouch.paypoint.action.SCANNER_DATA")
            addAction("com.symbol.datawedge.action.RESULT_ACTION")
        }
        scannerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                android.util.Log.d("SCANNER_BCR", "Broadcast received: ${intent.action}")
                var code: String? = intent.getStringExtra("DATA") ?: intent.getStringExtra("data")
                if (code == null && intent.action == "com.symbol.datawedge.action.RESULT_ACTION")
                    code = intent.getStringExtra("com.symbol.datawedge.api.RESULT_DATA_STRING")
                if (code == null) {
                    // Log all extras to see what the scanner sends
                    intent.extras?.let { bundle ->
                        android.util.Log.d("SCANNER_BCR", "Extras: ${bundle.keySet().joinToString { "$it=${bundle.get(it)}" }}")
                    }
                }
                if (code != null && code.trim().length in 2..100) {
                    android.util.Log.d("SCANNER_BCR", "Code from broadcast: '${code.trim()}'")
                    lastScanCode = code.trim()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 33)
            ctx.registerReceiver(scannerReceiver, filter, Context.RECEIVER_EXPORTED)
        else
            ctx.registerReceiver(scannerReceiver, filter)

        try {
            val listener = object : EloPeripheralEventListener {
                override fun onEvent(state: Int, data: String) {
                    android.util.Log.d("SCANNER_EPM", "EloPeripheralManager event: state=$state data='$data'")
                    if (state == EloPeripheralEventListener.BCR_STATE_DATA_RECEIVIED && data.isNotBlank())
                        lastScanCode = data.trim()
                }
                override fun onEvent(state: Int, n: Int) {
                    android.util.Log.d("SCANNER_EPM", "EloPeripheralManager event: state=$state n=$n")
                }
                override fun onEvent(state: Int) {
                    android.util.Log.d("SCANNER_EPM", "EloPeripheralManager event: state=$state")
                }
            }
            val mgr = EloPeripheralManager(ctx, listener)
            try { mgr.activeBcr() } catch (_: Throwable) {}
            android.util.Log.d("SCANNER_EPM", "EloPeripheralManager setup OK")
        } catch (e: Throwable) {
            android.util.Log.w("SCANNER_EPM", "EloPeripheralManager failed: ${e.message}")
        }

        try {
            val dm = DeviceManager.getInstance(ctx.applicationContext)
            val bcr = dm.getBarCodeReader()
            bcr.let {
                it.setEnabled(true)
                it.setTriggerMode(TriggerMode.AUTO)
                // Do NOT call setKbMode() - it routes data as keyboard events
                // instead of delivering via BarcodeReadCallback
                it.setBarcodeReadCallback(object : com.elo.device.peripherals.BarCodeReader.BarcodeReadCallback {
                    override fun onBarcodeRead(data: ByteArray) {
                        val code = String(data, Charsets.UTF_8).trim()
                        android.util.Log.d("SCANNER_SDK", "BarcodeReadCallback: '$code'")
                        if (code.isNotBlank()) lastScanCode = code
                    }
                })
            }
            android.util.Log.d("SCANNER_SDK", "DeviceManager barcode reader setup OK")
        } catch (e: Throwable) {
            android.util.Log.w("SCANNER_SDK", "DeviceManager barcode reader failed: ${e.message}")
        }

        listOf("com.elotouch.peripheral.action.SCANNER_ON", "com.oem.zbcr.TRIGGER_ON",
            "com.oem.zbcr.SET_VCOM_MODE", "com.oem.zbcr.SET_AUTO_AIM_ON",
            "com.oem.zbcr.PRESENTATION_MODE_ON", "com.elotouch.peripheral.action.BCR_AUTO_MODE"
        ).forEach { try { ctx.sendBroadcast(Intent(it)) } catch (_: Throwable) {} }

        PeripheralTestResult(true, "Scanner activado. Apunte un codigo al lector.")
    } catch (e: Throwable) { PeripheralTestResult(false, "Error: ${e.message}") }
}

// ==================== CARD READER (MagTek Dynamag) ====================

actual suspend fun eloTestCardReader(): PeripheralTestResult = withContext(Dispatchers.IO) {
    val ctx = appContext ?: return@withContext PeripheralTestResult(false, "Contexto no disponible")
    lastCardData = null
    setupMagTekDynamag(ctx)
}

/*
        // Legacy fallbacks intentionally disabled for PayPoint MSR.
        // The working Capacitor implementation and Elo PayPoint behavior require
        // MagTeklibDynamag. HID/KeyEvent paths produce corrupted/fake PAN values.
        // 1. Broadcast receiver para MSR intents
        cardReaderReceiver?.let { try { ctx.unregisterReceiver(it) } catch (_: Exception) {} }
        val filter = IntentFilter().apply {
            addAction("com.elotouch.peripheral.action.MSR_DATA")
            addAction("com.elotouch.paypoint.action.MSR_DATA")
            addAction("com.elo.msr.DATA")
            addAction("com.elotouch.peripheral.action.CARD_DATA")
            addAction("com.elotouch.paypoint.action.CARD_DATA")
        }
        cardReaderReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                android.util.Log.d("MSR", "Broadcast received: ${intent.action}")
                var data: String? = intent.getStringExtra("DATA")
                    ?: intent.getStringExtra("data")
                    ?: intent.getStringExtra("TRACK_DATA")
                    ?: intent.getStringExtra("track_data")
                if (data == null) {
                    val tracks = mutableListOf<String>()
                    intent.getStringExtra("TRACK1")?.let { tracks.add("T1: $it") }
                    intent.getStringExtra("TRACK2")?.let { tracks.add("T2: $it") }
                    intent.getStringExtra("TRACK3")?.let { tracks.add("T3: $it") }
                    if (tracks.isNotEmpty()) data = tracks.joinToString(" | ")
                }
                // Try all bundle extras as fallback
                if (data == null) {
                    intent.extras?.let { bundle ->
                        val allData = bundle.keySet().mapNotNull { key ->
                            bundle.getString(key)?.let { "$key: $it" }
                        }
                        if (allData.isNotEmpty()) data = allData.joinToString(" | ")
                    }
                }
                if (data != null && data!!.trim().length >= 2) {
                    android.util.Log.d("MSR", "Card data received via broadcast: ${data!!.take(30)}")
                    lastCardData = data!!.trim()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 33)
            ctx.registerReceiver(cardReaderReceiver, filter, Context.RECEIVER_EXPORTED)
        else
            ctx.registerReceiver(cardReaderReceiver, filter)
        activatedVia.add("Broadcast receiver")

        // 2. EloPeripheralManager listener para MSR
        try {
            val listener = object : EloPeripheralEventListener {
                override fun onEvent(state: Int, data: String) {
                    android.util.Log.d("MSR", "EloPeripheralManager event: state=$state data=${data.take(30)}")
                    if (data.isNotBlank() && data.trim().length >= 2) {
                        lastCardData = data.trim()
                    }
                }
                override fun onEvent(state: Int, n: Int) {}
                override fun onEvent(state: Int) {}
            }
            val mgr = EloPeripheralManager(ctx, listener)
            try {
                mgr.javaClass.getMethod("activeMsr").invoke(mgr)
            } catch (_: Exception) {}
            activatedVia.add("EloPeripheralManager")
        } catch (e: Exception) {
            android.util.Log.w("MSR", "EloPeripheralManager failed: ${e.message}")
        }

        // 3. Elo SDK DeviceManager MSR (via reflection - API puede no existir)
        try {
            val dm = DeviceManager.getInstance(ctx.applicationContext)
            val getMsrMethod = dm.javaClass.methods.firstOrNull {
                it.name == "getMsr" || it.name == "getMagneticStripeReader"
            }
            if (getMsrMethod != null) {
                val msr = getMsrMethod.invoke(dm)
                if (msr != null) {
                    try { msr.javaClass.getMethod("setEnabled", Boolean::class.javaPrimitiveType).invoke(msr, true) } catch (_: Exception) {}
                    activatedVia.add("Elo SDK MSR")
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("MSR", "DeviceManager MSR failed: ${e.message}")
        }

        // 4. USB HID direct read - buscar dispositivos USB HID que puedan ser MSR
        msrUsbRunning = false
        msrUsbThread?.interrupt()
        try {
            val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
            // MSR devices typically have USB class 0 (use interface class) with HID interface class 3
            val msrDevice = usbMgr.deviceList.values.firstOrNull { dev ->
                // Skip known devices (Star printer, barcode scanner)
                if (dev.vendorId == STAR_VENDOR_ID) return@firstOrNull false
                for (i in 0 until dev.interfaceCount) {
                    val iface = dev.getInterface(i)
                    // HID class = 3, typical for MSR in keyboard mode
                    if (iface.interfaceClass == 3) return@firstOrNull true
                }
                false
            }
            if (msrDevice != null) {
                // Find HID interface with IN endpoint
                var hidIface: UsbInterface? = null
                var hidEndpoint: UsbEndpoint? = null
                for (i in 0 until msrDevice.interfaceCount) {
                    val iface = msrDevice.getInterface(i)
                    if (iface.interfaceClass == 3) {
                        for (j in 0 until iface.endpointCount) {
                            val ep = iface.getEndpoint(j)
                            if (ep.direction == UsbConstants.USB_DIR_IN) {
                                hidIface = iface
                                hidEndpoint = ep
                                break
                            }
                        }
                        if (hidEndpoint != null) break
                    }
                }
                if (hidIface != null && hidEndpoint != null) {
                    val conn = usbMgr.openDevice(msrDevice)
                    if (conn != null) {
                        conn.claimInterface(hidIface, true)
                        msrUsbRunning = true
                        val fEndpoint = hidEndpoint
                        val fIface = hidIface
                        val fConn = conn
                        msrUsbThread = Thread {
                            val buffer = ByteArray(fEndpoint.maxPacketSize.coerceAtLeast(64))
                            val sb = StringBuilder()
                            // HID keyboard scancode → ASCII mapping (US layout)
                            val hidToAscii = mapOf(
                                4 to 'a', 5 to 'b', 6 to 'c', 7 to 'd', 8 to 'e', 9 to 'f',
                                10 to 'g', 11 to 'h', 12 to 'i', 13 to 'j', 14 to 'k', 15 to 'l',
                                16 to 'm', 17 to 'n', 18 to 'o', 19 to 'p', 20 to 'q', 21 to 'r',
                                22 to 's', 23 to 't', 24 to 'u', 25 to 'v', 26 to 'w', 27 to 'x',
                                28 to 'y', 29 to 'z', 30 to '1', 31 to '2', 32 to '3', 33 to '4',
                                34 to '5', 35 to '6', 36 to '7', 37 to '8', 38 to '9', 39 to '0',
                                40 to '\n', 44 to ' ', 45 to '-', 46 to '=', 47 to '[', 48 to ']',
                                49 to '\\', 51 to ';', 52 to '\'', 53 to '`', 54 to ',', 55 to '.',
                                56 to '/'
                            )
                            val hidToAsciiShift = mapOf(
                                30 to '!', 31 to '@', 32 to '#', 33 to '$', 34 to '%', 35 to '^',
                                36 to '&', 37 to '*', 38 to '(', 39 to ')', 45 to '_', 46 to '+',
                                47 to '{', 48 to '}', 49 to '|', 51 to ':', 52 to '"', 53 to '~',
                                54 to '<', 55 to '>', 56 to '?'
                            )
                            try {
                                while (msrUsbRunning && !Thread.currentThread().isInterrupted) {
                                    val len = fConn.bulkTransfer(fEndpoint, buffer, buffer.size, 200)
                                    if (len >= 3) {
                                        // HID keyboard report: [modifier, reserved, keycode1, keycode2, ...]
                                        val modifier = buffer[0].toInt() and 0xFF
                                        val shifted = (modifier and 0x22) != 0 // Left or Right Shift
                                        for (k in 2 until len) {
                                            val keycode = buffer[k].toInt() and 0xFF
                                            if (keycode == 0) continue
                                            if (keycode == 40) {
                                                // Enter key = end of swipe
                                                if (sb.isNotEmpty()) {
                                                    val result = sb.toString()
                                                    android.util.Log.d("MSR", "USB HID card data: ${result.take(30)}")
                                                    lastCardData = result
                                                    sb.clear()
                                                }
                                                continue
                                            }
                                            val ch = if (shifted) {
                                                hidToAsciiShift[keycode]
                                                    ?: hidToAscii[keycode]?.uppercaseChar()
                                            } else {
                                                hidToAscii[keycode]
                                            }
                                            if (ch != null) sb.append(ch)
                                        }
                                        // Si hay datos acumulados, actualizar parcialmente
                                        if (sb.length >= 5) {
                                            lastCardData = sb.toString()
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                            } finally {
                                try { fConn.releaseInterface(fIface) } catch (_: Exception) {}
                                try { fConn.close() } catch (_: Exception) {}
                            }
                        }.apply {
                            isDaemon = true
                            name = "MSR-USB-Reader"
                            start()
                        }
                        activatedVia.add("USB HID (${msrDevice.productName ?: "MSR"})")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("MSR", "USB HID setup failed: ${e.message}")
        }

        // 5. Enviar broadcasts de activacion
        listOf(
            "com.elotouch.peripheral.action.MSR_ON",
            "com.elotouch.paypoint.action.MSR_ON",
            "com.elotouch.peripheral.action.MSR_ENABLE",
            "com.elotouch.paypoint.action.MSR_ENABLE"
        ).forEach { try { ctx.sendBroadcast(Intent(it)) } catch (_: Exception) {} }

        val methods = activatedVia.joinToString(", ")
        PeripheralTestResult(true, "Lector activado via respaldos: $methods. MagTek SDK no disponible o no abrio. Pase la tarjeta.")
*/

// ==================== ESC/POS FALLBACK (for non-Star printers) ====================

private fun buildEscPosTicket(text: String, paperWidthMm: String): ByteArray {
    val out = ByteArrayOutputStream()
    val separator = if (normalizedPaperWidth(paperWidthMm) == "80") {
        "================================================\n"
    } else {
        "================================\n"
    }
    out.write(byteArrayOf(0x1B, 0x40)) // Init
    out.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center
    out.write(byteArrayOf(0x1B, 0x45, 0x01)) // Bold ON
    out.write(byteArrayOf(0x1D, 0x21, 0x11)) // Double width+height
    out.write("TICKET DE PRUEBA\n".toByteArray())
    out.write(byteArrayOf(0x1D, 0x21, 0x00)) // Normal size
    out.write(byteArrayOf(0x1B, 0x45, 0x00)) // Bold OFF
    out.write(byteArrayOf(0x1B, 0x61, 0x00)) // Left align
    out.write(separator.toByteArray())
    text.lines().forEach { line -> out.write("$line\n".toByteArray()) }
    out.write("\n".toByteArray())
    out.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center
    out.write(byteArrayOf(0x1B, 0x45, 0x01)) // Bold
    out.write("TM-RESTAURANTE\n".toByteArray())
    out.write(byteArrayOf(0x1B, 0x45, 0x00))
    out.write("Gracias por su compra!\n".toByteArray())
    out.write(separator.toByteArray())
    out.write(byteArrayOf(0x1B, 0x64, 0x05)) // Feed 5 lines
    out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00)) // Partial cut
    return out.toByteArray()
}

// ==================== CFD (Customer Facing Display) ====================

// -- CFD text formatting helpers (same as CFD-CustomerDisplay example) --

private fun cfdPad(s: String?, len: Int): String {
    val str = s ?: ""
    if (str.length > len) return str.substring(0, len)
    return str.padEnd(len)
}

private fun cfdTrunc(s: String?): String {
    if (s == null) return ""
    return if (s.length > CFD_LINE_W) s.substring(0, CFD_LINE_W) else s
}

private fun cfdCols(left: String, right: String): String {
    val sp = CFD_LINE_W - left.length - right.length
    if (sp < 1) {
        val avail = CFD_LINE_W - left.length - 1
        return if (avail > 0) left + " " + right.substring(0, minOf(right.length, avail))
        else left.substring(0, CFD_LINE_W)
    }
    return left + " ".repeat(sp) + right
}

private fun cfdFmtMoney(v: Double): String = String.format(Locale.US, "%,.2f", v)

// -- CFD backend initialization (mirrors CFD-CustomerDisplay plugin) --

private fun cfdTryEloSdkAuto(): Boolean {
    if (eloCfd != null) return true
    val ctx = appContext ?: return false
    return try {
        val dm = DeviceManager.getInstance(ctx.applicationContext)
        val c = dm.getCfd() ?: run {
            android.util.Log.w("CFD", "DeviceManager(auto).getCfd()=null")
            return false
        }
        c.setBacklight(true)
        eloCfd = c
        cfdBackend = "elo_sdk"
        android.util.Log.d("CFD", "Elo SDK CFD (auto): ${c.javaClass.name}")
        true
    } catch (e: Throwable) {
        android.util.Log.w("CFD", "Elo SDK (auto): ${e.javaClass.simpleName}: ${e.message}")
        false
    }
}

private fun cfdTryEloSdkPlatform(platform: EloPlatform): Boolean {
    if (eloCfd != null) return true
    val ctx = appContext ?: return false
    return try {
        val dm = DeviceManager.getInstance(platform, ctx)
        val c = dm.getCfd() ?: run {
            android.util.Log.w("CFD", "DeviceManager($platform).getCfd()=null")
            return false
        }
        c.setBacklight(true)
        eloCfd = c
        cfdBackend = "elo_sdk"
        android.util.Log.d("CFD", "Elo SDK CFD ($platform): ${c.javaClass.name}")
        true
    } catch (e: Throwable) {
        android.util.Log.w("CFD", "Elo SDK ($platform): ${e.javaClass.simpleName}: ${e.message}")
        false
    }
}

private fun cfdTryEloLegacy(): Boolean {
    if (eloCfd != null || cfdLegacy != null) return true
    for (cn in arrayOf(
        "com.elotouch.paypoint.register2.cfd.CFD",
        "com.elotouch.paypoint.register.cfd.CFD"
    )) {
        try {
            val cls = Class.forName(cn)
            val obj = cls.newInstance()
            try { cls.getMethod("setBacklight", Boolean::class.javaPrimitiveType).invoke(obj, true) } catch (_: Exception) {}
            cfdLegacy = obj
            cfdBackend = "elo_sdk"
            android.util.Log.d("CFD", "Elo Legacy CFD ($cn) via reflection")
            return true
        } catch (e: Exception) {
            android.util.Log.w("CFD", "Legacy $cn: ${e.javaClass.simpleName}")
        }
    }
    return false
}

@Synchronized
private fun cfdEnsureBackend(): Boolean {
    return eloCfd != null || cfdLegacy != null
            || cfdTryEloSdkAuto()
            || cfdTryEloSdkPlatform(EloPlatform.PAYPOINT_REFRESH)
            || cfdTryEloSdkPlatform(EloPlatform.PAYPOINT_2)
            || cfdTryEloLegacy()
}

// -- CFD low-level write --

private fun cfdWriteRaw(l1: String, l2: String) {
    val padL1 = cfdPad(l1, CFD_LINE_W)
    val padL2 = cfdPad(l2, CFD_LINE_W)
    if (eloCfd != null) {
        eloCfd!!.setBacklight(true)
        eloCfd!!.setLine(1, padL1)
        eloCfd!!.setLine(2, padL2)
        return
    }
    if (cfdLegacy != null) {
        val cls = cfdLegacy!!.javaClass
        try { cls.getMethod("setBacklight", Boolean::class.javaPrimitiveType).invoke(cfdLegacy, true) } catch (_: Exception) {}
        try {
            val m = cls.getMethod("setLine", Int::class.javaPrimitiveType, String::class.java)
            m.invoke(cfdLegacy, 1, padL1)
            m.invoke(cfdLegacy, 2, padL2)
        } catch (_: NoSuchMethodException) {
            cls.getMethod("setLine1", String::class.java).invoke(cfdLegacy, padL1)
            cls.getMethod("setLine2", String::class.java).invoke(cfdLegacy, padL2)
        }
        return
    }
    throw Exception("No CFD backend")
}

private fun cfdWriteLines(l1: String, l2: String) {
    val truncL1 = cfdTrunc(l1)
    val truncL2 = cfdTrunc(l2)
    cfdLastL1 = truncL1
    cfdLastL2 = truncL2
    try {
        cfdWriteRaw(truncL1, truncL2)
        cfdLastWriteMs = System.currentTimeMillis()
        android.util.Log.d("CFD", "writeLines [$truncL1|$truncL2]")
    } catch (e: Exception) {
        android.util.Log.w("CFD", "writeLines failed, retrying: ${e.message}")
        eloCfd = null
        cfdLegacy = null
        cfdLastWriteMs = 0
        if (cfdEnsureBackend()) {
            try {
                cfdWriteRaw(truncL1, truncL2)
                cfdLastWriteMs = System.currentTimeMillis()
            } catch (_: Exception) {}
        }
    }
}

private fun cfdStartKeepalive() {
    if (cfdKeepaliveFuture != null) return
    cfdKeepaliveFuture = cfdScheduler.scheduleAtFixedRate({
        synchronized(cfdIoLock) {
            if (System.currentTimeMillis() - cfdLastWriteMs < 3000) return@scheduleAtFixedRate
            try {
                if (!cfdEnsureBackend()) return@scheduleAtFixedRate
                cfdWriteRaw(cfdLastL1, cfdLastL2)
                android.util.Log.d("CFD", "keepalive [$cfdLastL1|$cfdLastL2]")
            } catch (e: Exception) {
                android.util.Log.w("CFD", "keepalive failed: ${e.message}")
                eloCfd = null
                cfdLegacy = null
            }
        }
    }, 4, 4, TimeUnit.SECONDS)
    android.util.Log.d("CFD", "CFD keepalive started (4s interval)")
}

// -- CFD actual implementations --

actual suspend fun cfdDetectBackend(): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        eloCfd = null; cfdLegacy = null; cfdBackend = "none"
    }
    val steps = mutableListOf<String>()

    val ok1 = cfdTryEloSdkAuto()
    steps.add("Elo SDK (auto): ${if (ok1) "OK" else "no disponible"}")

    val ok2 = !ok1 && cfdTryEloSdkPlatform(EloPlatform.PAYPOINT_REFRESH)
    steps.add("Elo SDK (PAYPOINT_REFRESH): ${if (ok2) "OK" else "no disponible"}")

    val ok3 = !ok1 && !ok2 && cfdTryEloSdkPlatform(EloPlatform.PAYPOINT_2)
    steps.add("Elo SDK (PAYPOINT_2): ${if (ok3) "OK" else "no disponible"}")

    val ok4 = !ok1 && !ok2 && !ok3 && cfdTryEloLegacy()
    steps.add("Elo Legacy (reflection): ${if (ok4) "OK" else "no disponible"}")

    val physical = ok1 || ok2 || ok3 || ok4
    if (!physical) cfdBackend = "preview"

    if (physical) {
        cfdStartKeepalive()
    }

    CfdTestResult(
        success = physical,
        message = if (physical) "Backend detectado: $cfdBackend\n${steps.joinToString("\n")}"
                  else "No se detecto pantalla fisica.\n${steps.joinToString("\n")}",
        backend = cfdBackend
    )
}

actual suspend fun cfdIsAvailable(): CfdTestResult = withContext(Dispatchers.IO) {
    val available = cfdEnsureBackend()
    CfdTestResult(
        success = available,
        message = if (available) "Pantalla disponible ($cfdBackend)" else "Pantalla no disponible",
        backend = cfdBackend
    )
}

actual suspend fun cfdShowCustomMessage(line1: String, line2: String): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        if (!cfdEnsureBackend()) return@withContext CfdTestResult(false, "CFD no disponible")
        try {
            cfdWriteLines(line1, line2)
            cfdStartKeepalive()
            CfdTestResult(true, "Mensaje enviado", cfdBackend)
        } catch (e: Exception) {
            CfdTestResult(false, "Error: ${e.message}")
        }
    }
}

actual suspend fun cfdShowIdle(welcomeMessage: String, idleMessage: String): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        if (!cfdEnsureBackend()) return@withContext CfdTestResult(false, "CFD no disponible")
        try {
            cfdWriteLines(welcomeMessage, idleMessage)
            cfdStartKeepalive()
            CfdTestResult(true, "Pantalla idle", cfdBackend)
        } catch (e: Exception) {
            CfdTestResult(false, "Error: ${e.message}")
        }
    }
}

actual suspend fun cfdShowProduct(name: String, price: Double, quantity: Int): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        if (!cfdEnsureBackend()) return@withContext CfdTestResult(false, "CFD no disponible")
        try {
            val l1 = cfdTrunc(name)
            val l2 = cfdCols("x$quantity", "RD\$${cfdFmtMoney(price)}")
            cfdWriteLines(l1, l2)
            CfdTestResult(true, "Producto mostrado", cfdBackend)
        } catch (e: Exception) {
            CfdTestResult(false, "Error: ${e.message}")
        }
    }
}

actual suspend fun cfdShowCart(itemCount: Int, subtotal: Double, total: Double): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        if (!cfdEnsureBackend()) return@withContext CfdTestResult(false, "CFD no disponible")
        try {
            val l1 = cfdCols("It: $itemCount", "RD\$${cfdFmtMoney(subtotal)}")
            val l2 = cfdCols("Total:", "RD\$${cfdFmtMoney(total)}")
            cfdWriteLines(l1, l2)
            CfdTestResult(true, "Carrito mostrado", cfdBackend)
        } catch (e: Exception) {
            CfdTestResult(false, "Error: ${e.message}")
        }
    }
}

actual suspend fun cfdShowTotals(subtotal: Double, discount: Double, tax: Double, total: Double): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        if (!cfdEnsureBackend()) return@withContext CfdTestResult(false, "CFD no disponible")
        try {
            val l1 = cfdCols("Sub:", "RD\$${cfdFmtMoney(subtotal)}")
            val l2 = cfdCols("Total:", "RD\$${cfdFmtMoney(total)}")
            cfdWriteLines(l1, l2)
            CfdTestResult(true, "Totales mostrados", cfdBackend)
        } catch (e: Exception) {
            CfdTestResult(false, "Error: ${e.message}")
        }
    }
}

actual suspend fun cfdShowPayment(total: Double, received: Double, change: Double): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        if (!cfdEnsureBackend()) return@withContext CfdTestResult(false, "CFD no disponible")
        try {
            val l1 = cfdCols("Rec:", "RD\$${cfdFmtMoney(received)}")
            val l2 = cfdCols("Camb:", "RD\$${cfdFmtMoney(change)}")
            cfdWriteLines(l1, l2)
            CfdTestResult(true, "Pago mostrado", cfdBackend)
        } catch (e: Exception) {
            CfdTestResult(false, "Error: ${e.message}")
        }
    }
}

actual suspend fun cfdShowThankYou(message: String): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        if (!cfdEnsureBackend()) return@withContext CfdTestResult(false, "CFD no disponible")
        try {
            cfdWriteLines(message, "Vuelva pronto!")
            CfdTestResult(true, "Gracias mostrado", cfdBackend)
        } catch (e: Exception) {
            CfdTestResult(false, "Error: ${e.message}")
        }
    }
}

actual suspend fun cfdClear(): CfdTestResult = withContext(Dispatchers.IO) {
    synchronized(cfdIoLock) {
        try {
            if (eloCfd != null) {
                eloCfd!!.clear()
                eloCfd!!.setBacklight(false)
                android.util.Log.d("CFD", "clear: Elo SDK (backlight OFF)")
            } else if (cfdLegacy != null) {
                val cls = cfdLegacy!!.javaClass
                try { cls.getMethod("clear").invoke(cfdLegacy) }
                catch (_: NoSuchMethodException) { cls.getMethod("clearDisplay").invoke(cfdLegacy) }
            }
            cfdLastL1 = ""
            cfdLastL2 = ""
            cfdLastWriteMs = System.currentTimeMillis()
            CfdTestResult(true, "Pantalla limpiada", cfdBackend)
        } catch (e: Exception) {
            android.util.Log.e("CFD", "clear error: ${e.message}")
            CfdTestResult(false, "Error: ${e.message}")
        }
    }
}
