package com.tmrestaurant.platform

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

// SPP UUID para impresoras Bluetooth
private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

// Vendor IDs conocidos de impresoras
private const val STAR_VENDOR_ID = 0x0519
private const val EPSON_VENDOR_ID = 0x04B8
private const val BIXOLON_VENDOR_ID = 0x1504
private const val CITIZEN_VENDOR_ID = 0x1D90

/**
 * Obtiene el BluetoothAdapter de forma segura usando BluetoothManager (recomendado)
 * con fallback al metodo legacy.
 */
@SuppressLint("MissingPermission")
private fun getBluetoothAdapter(): BluetoothAdapter? {
    return try {
        val ctx = appContext ?: return null
        val btManager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        btManager?.adapter
    } catch (_: Exception) {
        // Fallback al metodo legacy
        try {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Determina si un USB device es probablemente una impresora.
 * Las impresoras USB usan class 7 (Printer) o class 0xFF (Vendor-specific, comun en POS).
 */
private fun isLikelyPrinter(dev: android.hardware.usb.UsbDevice): Boolean {
    // Show ALL USB devices that have interfaces (broad search like ELO section)
    return dev.interfaceCount > 0
}

/**
 * Envia bytes crudos a un dispositivo USB que sea impresora,
 * buscando un endpoint Bulk OUT.
 */
@SuppressLint("MissingPermission")
private fun sendBytesToUsbPrinter(identifier: String, data: ByteArray): Boolean {
    val ctx = appContext ?: return false
    val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return false

    // Buscar dispositivo por identifier (vendorId:productId o nombre)
    val device = usbMgr.deviceList.values.firstOrNull { dev ->
        "${dev.vendorId}:${dev.productId}" == identifier ||
                dev.productName == identifier ||
                dev.deviceName == identifier
    } ?: return false

    val conn = usbMgr.openDevice(device) ?: return false
    try {
        // Buscar interface con endpoint Bulk OUT
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            for (j in 0 until iface.endpointCount) {
                val ep = iface.getEndpoint(j)
                if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                    ep.direction == UsbConstants.USB_DIR_OUT
                ) {
                    conn.claimInterface(iface, true)
                    try {
                        var offset = 0
                        while (offset < data.size) {
                            val chunk = data.size - offset
                            val sent = conn.bulkTransfer(ep, data, offset, chunk, 5000)
                            if (sent < 0) return false
                            offset += sent
                        }
                        return true
                    } finally {
                        conn.releaseInterface(iface)
                    }
                }
            }
        }
        return false
    } finally {
        try { conn.close() } catch (_: Exception) {}
    }
}

/**
 * Envia bytes via Bluetooth SPP a una impresora.
 */
@SuppressLint("MissingPermission")
private fun sendBytesViaBluetooth(printerIdentifier: String, data: ByteArray): Boolean {
    val adapter = getBluetoothAdapter() ?: return false
    if (!adapter.isEnabled) return false

    val bondedDevices = adapter.bondedDevices ?: return false
    val device: BluetoothDevice = bondedDevices.firstOrNull {
        it.address == printerIdentifier || it.name == printerIdentifier
    } ?: return false

    val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
    return try {
        socket.use { s ->
            s.connect()
            s.outputStream.write(data)
            s.outputStream.flush()
        }
        true
    } catch (_: Exception) {
        false
    }
}

// ==================== DISCOVER PRINTERS ====================

@SuppressLint("MissingPermission")
actual suspend fun discoverPrinters(type: String): List<DiscoveredPrinter> = withContext(Dispatchers.IO) {
    when (type) {
        "USB" -> {
            val ctx = appContext ?: return@withContext emptyList()
            val printers = mutableListOf<DiscoveredPrinter>()
            try {
                val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as? UsbManager
                    ?: return@withContext emptyList()

                usbMgr.deviceList.values.forEach { dev ->
                    if (dev.interfaceCount > 0 && isLikelyPrinter(dev)) {
                        val name = dev.productName ?: "USB Device"
                        val vendorLabel = when (dev.vendorId) {
                            STAR_VENDOR_ID -> "Star Micronics"
                            EPSON_VENDOR_ID -> "Epson"
                            BIXOLON_VENDOR_ID -> "Bixolon"
                            CITIZEN_VENDOR_ID -> "Citizen"
                            else -> "VID:${dev.vendorId}"
                        }
                        printers.add(
                            DiscoveredPrinter(
                                name = "$name ($vendorLabel)",
                                identifier = "${dev.vendorId}:${dev.productId}",
                                type = "USB"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("PrinterService", "USB discovery error: ${e.message}")
            }
            printers
        }

        "Bluetooth" -> {
            try {
                val adapter = getBluetoothAdapter()
                if (adapter == null) {
                    android.util.Log.w("PrinterService", "Bluetooth no soportado en este dispositivo")
                    return@withContext emptyList()
                }
                if (!adapter.isEnabled) {
                    // Intentar encender Bluetooth
                    try { adapter.enable() } catch (_: Exception) {}
                    // Si sigue apagado, retornar vacio con log
                    if (!adapter.isEnabled) {
                        android.util.Log.w("PrinterService", "Bluetooth desactivado")
                        return@withContext emptyList()
                    }
                }
                val bondedDevices: Set<BluetoothDevice> = adapter.bondedDevices ?: emptySet()
                if (bondedDevices.isEmpty()) {
                    android.util.Log.w("PrinterService", "No hay dispositivos Bluetooth emparejados")
                }
                bondedDevices.map { device ->
                    DiscoveredPrinter(
                        name = device.name ?: "Dispositivo desconocido",
                        identifier = device.address,
                        type = "Bluetooth"
                    )
                }
            } catch (_: SecurityException) {
                android.util.Log.w("PrinterService", "Sin permiso Bluetooth")
                emptyList()
            } catch (e: Exception) {
                android.util.Log.w("PrinterService", "Bluetooth discovery error: ${e.message}")
                emptyList()
            }
        }

        else -> emptyList()
    }
}

// ==================== PRINT TEST PAGE ====================

@SuppressLint("MissingPermission")
actual suspend fun printTestPage(printerName: String, content: String): Boolean = withContext(Dispatchers.IO) {
    try {
        // Build ESC/POS data with initialization and cut
        val out = java.io.ByteArrayOutputStream()
        out.write(byteArrayOf(0x1B, 0x40)) // Initialize
        out.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center
        out.write(byteArrayOf(0x1B, 0x45, 0x01)) // Bold on
        out.write("*** TICKET DE PRUEBA ***\n".toByteArray())
        out.write(byteArrayOf(0x1B, 0x45, 0x00)) // Bold off
        out.write(byteArrayOf(0x1B, 0x61, 0x00)) // Left
        out.write(content.toByteArray(Charsets.UTF_8))
        out.write("\n".toByteArray())
        out.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center
        out.write(byteArrayOf(0x1B, 0x45, 0x01)) // Bold on
        out.write("TM-RESTAURANTE\n".toByteArray())
        out.write("Gracias por su compra!\n".toByteArray())
        out.write(byteArrayOf(0x1B, 0x45, 0x00))
        out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00)) // Cut
        val data = out.toByteArray()

        // 1) Try ELO SDK exactly as in ELO section (same implementation that works)
        try {
            val dm = com.elo.device.DeviceManager.getInstance(appContext?.applicationContext!!)
            val printer: com.elo.device.peripherals.Printer = dm.getPrinter()
            printer.print(data)
            printer.feed(3)
            return@withContext true
        } catch (e: Exception) {
            android.util.Log.w("PrinterService", "ELO SDK print failed: ${e.message}")
        }

        // 2) Try USB bulk transfer
        if (printerName.contains(":") && printerName.first().isDigit()) {
            if (sendBytesToUsbPrinter(printerName, data)) return@withContext true
        }

        // 3) Try Bluetooth
        if (sendBytesViaBluetooth(printerName, data)) return@withContext true

        // 4) Fallback: search by name in USB
        val ctx = appContext
        if (ctx != null) {
            val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as? UsbManager
            val dev = usbMgr?.deviceList?.values?.firstOrNull {
                it.productName == printerName || "${it.vendorId}:${it.productId}" == printerName
            }
            if (dev != null && sendBytesToUsbPrinter("${dev.vendorId}:${dev.productId}", data)) return@withContext true
        }

        android.util.Log.e("PrinterService", "All print methods failed for: $printerName")
        false
    } catch (e: Exception) {
        android.util.Log.e("PrinterService", "Print error: ${e.message}")
        false
    }
}

// ==================== OPEN CASH DRAWER ====================

@SuppressLint("MissingPermission")
actual suspend fun openCashDrawer(printerName: String): Boolean = withContext(Dispatchers.IO) {
    try {
        // 1) Try ELO SDK first
        try {
            val dm = com.elo.device.DeviceManager.getInstance(appContext?.applicationContext!!)
            val drawer: com.elo.device.peripherals.CashDrawer = dm.getCashDrawer()
            drawer.open()
            return@withContext true
        } catch (_: Exception) {}

        // ESC/POS drawer command
        val cmd = byteArrayOf(0x1B, 0x70, 0x00, 0x32, 0xFA.toByte())

        // 2) Try USB
        if (printerName.contains(":") && printerName.first().isDigit()) {
            if (sendBytesToUsbPrinter(printerName, cmd)) return@withContext true
        }

        // 3) Try Bluetooth
        if (sendBytesViaBluetooth(printerName, cmd)) return@withContext true

        // 4) Fallback by name
        val ctx = appContext
        if (ctx != null) {
            val usbMgr = ctx.getSystemService(Context.USB_SERVICE) as? UsbManager
            val dev = usbMgr?.deviceList?.values?.firstOrNull {
                it.productName == printerName || "${it.vendorId}:${it.productId}" == printerName
            }
            if (dev != null && sendBytesToUsbPrinter("${dev.vendorId}:${dev.productId}", cmd))
                return@withContext true
        }

        false
    } catch (e: Exception) {
        android.util.Log.e("PrinterService", "Cash drawer error: ${e.message}")
        false
    }
}

actual fun printWithSystemDialog(title: String, content: String) {
    val act = currentActivity ?: run {
        android.util.Log.e("PrinterService", "No activity reference available for print dialog")
        return
    }
    try {
        val printManager = act.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
        if (printManager == null) {
            android.util.Log.e("PrinterService", "PrintManager not available")
            return
        }
        printManager.print(title, object : android.print.PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: android.print.PrintAttributes?,
                newAttributes: android.print.PrintAttributes?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: android.print.PrintDocumentAdapter.LayoutResultCallback?,
                bundle: android.os.Bundle?
            ) {
                val info = android.print.PrintDocumentInfo.Builder(title)
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(android.print.PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: android.print.PrintDocumentAdapter.WriteResultCallback?
            ) {
                try {
                    val fontSize = 20f
                    val lineHeight = fontSize * 1.5f
                    val margin = 30f

                    val lines = content.split("\n")
                    val maxLineLen = lines.maxOfOrNull { it.length } ?: 48
                    val charWidth = fontSize * 0.6f
                    val contentWidth = maxLineLen * charWidth
                    val pdfW = (contentWidth + margin * 2).coerceAtLeast(200f)
                    val totalHeight = (lines.size * lineHeight + margin * 2).coerceAtLeast(200f)

                    val document = android.graphics.pdf.PdfDocument()
                    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(
                        pdfW.toInt(), totalHeight.toInt(), 1
                    ).create()
                    val page = document.startPage(pageInfo)
                    val canvas = page.canvas

                    val paint = android.graphics.Paint().apply {
                        textSize = fontSize
                        color = android.graphics.Color.BLACK
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                        isAntiAlias = false
                        isFakeBoldText = true
                    }

                    var y = margin + fontSize
                    for (line in lines) {
                        canvas.drawText(line, margin, y, paint)
                        y += lineHeight
                    }

                    document.finishPage(page)
                    val fos = java.io.FileOutputStream(destination?.fileDescriptor)
                    document.writeTo(fos)
                    fos.close()
                    document.close()
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    android.util.Log.e("PrinterService", "Print write error: ${e.message}")
                    callback?.onWriteFailed(e.message)
                }
            }
        }, null)
    } catch (e: Exception) {
        android.util.Log.e("PrinterService", "System print error: ${e.message}")
    }
}
