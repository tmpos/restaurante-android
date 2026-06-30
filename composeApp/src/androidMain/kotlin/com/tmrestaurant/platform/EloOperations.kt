package com.tmrestaurant.platform

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

object EloOperations {

    fun openCashDrawer(context: Context): Boolean {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = findPrinterDevice(usbManager) ?: return false
        if (!usbManager.hasPermission(device)) return false
        val connection = usbManager.openDevice(device) ?: return false
        return try {
            val iface = device.getInterface(0)
            connection.claimInterface(iface, true)
            val ep = getBulkOutEndpoint(iface) ?: return false
            val result = connection.bulkTransfer(ep, byteArrayOf(0x07), 1, 2000)
            result >= 0
        } catch (_: Exception) { false } finally { connection.close() }
    }

    fun printText(context: Context, text: String): Boolean {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = findPrinterDevice(usbManager) ?: return false
        if (!usbManager.hasPermission(device)) return false
        val connection = usbManager.openDevice(device) ?: return false
        return try {
            val iface = device.getInterface(0)
            connection.claimInterface(iface, true)
            val ep = getBulkOutEndpoint(iface) ?: return false
            val data = buildEscPos(text)
            var offset = 0
            while (offset < data.size) {
                val chunk = minOf(4096, data.size - offset)
                val sent = connection.bulkTransfer(ep, data.copyOfRange(offset, offset + chunk), chunk, 5000)
                if (sent < 0) return false
                offset += sent
            }
            true
        } catch (_: Exception) { false } finally { connection.close() }
    }

    fun testScanner(context: Context) {
        // Send broadcast to turn on scanner
        val intents = listOf(
            "com.elotouch.peripheral.action.SCANNER_ON",
            "com.oem.zbcr.TRIGGER_ON",
            "com.oem.zbcr.SET_VCOM_MODE",
            "com.oem.zbcr.SET_AUTO_AIM_ON",
            "com.oem.zbcr.PRESENTATION_MODE_ON",
            "com.elotouch.peripheral.action.BCR_AUTO_MODE"
        )
        intents.forEach { action ->
            try { context.sendBroadcast(android.content.Intent(action)) } catch (_: Exception) {}
        }
        // Also register SDK scanners
        try {
            val dm = com.elo.device.DeviceManager.getInstance(context)
            dm.getBarCodeReader()?.setBarcodeReadCallback(object : com.elo.device.peripherals.BarCodeReader.BarcodeReadCallback {
                override fun onBarcodeRead(data: ByteArray) {
                    // Barcode received: String(data, Charsets.UTF_8)
                }
            })
        } catch (_: Exception) {}
    }

    fun findPrinters(context: Context): List<String> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        return usbManager.deviceList.values
            .filter { it.deviceClass == 7 || it.interfaceCount > 0 }
            .map { "${it.productName ?: "Unknown"} (VID:${it.vendorId} PID:${it.productId})" }
    }

    private fun findPrinterDevice(manager: UsbManager): UsbDevice? {
        return manager.deviceList.values.firstOrNull { device ->
            device.interfaceCount > 0 && (0..<device.interfaceCount).any { i ->
                val iface = device.getInterface(i)
                iface.interfaceClass == 7 || iface.interfaceSubclass == 1
            }
        }
    }

    private fun getBulkOutEndpoint(iface: android.hardware.usb.UsbInterface): android.hardware.usb.UsbEndpoint? {
        for (i in 0..<iface.endpointCount) {
            val ep = iface.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == UsbConstants.USB_DIR_OUT)
                return ep
        }
        return null
    }

    private fun buildEscPos(text: String): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        out.write(byteArrayOf(0x1B, 0x40)) // ESC @ (Initialize)
        text.lines().forEach { line ->
            out.write(line.toByteArray(Charsets.UTF_8))
            out.write(0x0A) // LF
        }
        out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00)) // GS V A (cut paper)
        return out.toByteArray()
    }
}
