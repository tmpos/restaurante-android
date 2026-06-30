package com.tmrestaurant.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

actual suspend fun printTicketToServer(
    serverUrl: String,
    request: TicketPrintRequest,
    apiKey: String,
    apiRoute: String
): PrintResult = withContext(Dispatchers.IO) {
    try {
        val url = URL("${serverUrl.trimEnd('/')}$apiRoute/printer/ticket")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        if (apiKey.isNotBlank()) {
            conn.setRequestProperty("X-API-KEY", apiKey)
        }
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 20000

        val json = buildTicketJson(request)
        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(json)
            writer.flush()
        }

        val responseCode = conn.responseCode
        val responseBody = if (responseCode in 200..299) {
            conn.inputStream.bufferedReader().readText()
        } else {
            conn.errorStream?.bufferedReader()?.readText() ?: "{\"success\":false,\"message\":\"HTTP $responseCode\"}"
        }

        if (responseCode in 200..299) {
            PrintResult(success = true, message = "Ticket enviado a imprimir")
        } else {
            PrintResult(success = false, error = "Error $responseCode: $responseBody")
        }
    } catch (e: Exception) {
        PrintResult(success = false, error = e.message ?: "Error de conexion")
    }
}

private fun buildTicketJson(r: TicketPrintRequest): String {
    val itemsJson = r.items.joinToString(",") { item ->
        """{"description":"${esc(item.description)}","quantity":${item.quantity},"price":${item.price},"tax":${item.tax},"total":${item.total}${if (item.code.isNotBlank()) ",\"code\":\"${esc(item.code)}\"" else ""}}"""
    }

    return """{
        "cutPaper":${r.cutPaper},
        "copies":${r.copies.coerceIn(1, 5)},
        "openDrawer":${r.openDrawer},
        "branding":{
            "fontSize":${r.branding.fontSize},
            "fontSizeLg":${r.branding.fontSizeLg},
            "fontSizeTotal":${r.branding.fontSizeTotal},
            "logoMaxWidth":${r.branding.logoMaxWidth},
            "logoMaxHeight":${r.branding.logoMaxHeight},
            "printLogo":${r.branding.printLogo},
            "showCompanyName":${r.branding.showCompanyName},
            "showCompanyRnc":${r.branding.showCompanyRnc},
            "showCompanyAddress":${r.branding.showCompanyAddress},
            "showCompanyPhone":${r.branding.showCompanyPhone},
            "showCompanyEmail":${r.branding.showCompanyEmail},
            "showCustomerName":${r.branding.showCustomerName},
            "showCustomerRnc":${r.branding.showCustomerRnc},
            "showCustomerPhone":${r.branding.showCustomerPhone},
            "showDateTime":${r.branding.showDateTime},
            "showCashierName":${r.branding.showCashierName},
            "showTaxes":${r.branding.showTaxes},
            "showDiscounts":${r.branding.showDiscounts},
            "showTip":${r.branding.showTip},
            "showPaymentMethod":${r.branding.showPaymentMethod},
            "showCashChange":${r.branding.showCashChange},
            "showThankYouMessage":${r.branding.showThankYouMessage},
            "showQr":${r.branding.showQr},
            "thankYouMessage":"${esc(r.branding.thankYouMessage)}",
            "compact":${r.branding.compact},
            "showWarranty":false,
            "showSupportInfo":false
        },
        "company":{
            "commercialName":"${esc(r.company.commercialName)}",
            "name":"${esc(r.company.name)}",
            "rnc":"${esc(r.company.rnc)}",
            "address":"${esc(r.company.address)}",
            "phone":"${esc(r.company.phone)}",
            "email":"${esc(r.company.email)}"
        },
        "customer":{
            "name":"${esc(r.customer.name)}",
            "rnc":"${esc(r.customer.rnc)}",
            "phone":"${esc(r.customer.phone)}"
        },
        "invoice":{
            "invoiceNumber":"${esc(r.invoice.invoiceNumber)}",
            "ncf":"${esc(r.invoice.ncf)}",
            "date":"${esc(r.invoice.date)}",
            "time":"${esc(r.invoice.time)}",
            "cashier":"${esc(r.invoice.cashier)}",
            "paymentMethod":"${esc(r.invoice.paymentMethod)}"
        },
        "items":[$itemsJson],
        "totals":{
            "subtotal":${r.totals.subtotal},
            "tax":${r.totals.tax},
            "discount":${r.totals.discount},
            "grandTotal":${r.totals.grandTotal},
            "paidAmount":${r.totals.paidAmount},
            "changeAmount":${r.totals.changeAmount}
        },
        "payment":{"method":"${esc(r.payment["method"] ?: "Efectivo")}"},
        "note":"${esc(r.note)}",
        "qrUrl":"${esc(r.qrUrl)}"
    }""".trimIndent()
}

actual suspend fun uploadTicketLogoToServer(
    serverUrl: String,
    fileName: String,
    bytes: ByteArray,
    apiKey: String,
    apiRoute: String
): PrintResult = withContext(Dispatchers.IO) {
    if (bytes.isEmpty()) return@withContext PrintResult(false, error = "Logo vacio")
    if (bytes.size > 500 * 1024) return@withContext PrintResult(false, error = "El logo supera 500KB")

    try {
        val boundary = "----TMRestaurant${UUID.randomUUID()}"
        val conn = URL("${serverUrl.trimEnd('/')}$apiRoute/config/logo").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        if (apiKey.isNotBlank()) conn.setRequestProperty("X-API-KEY", apiKey)
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 20000

        val contentType = when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".bmp", true) -> "image/bmp"
            else -> "image/png"
        }
        conn.outputStream.use { output ->
            output.write("--$boundary\r\n".toByteArray())
            output.write("""Content-Disposition: form-data; name="logo"; filename="${esc(fileName)}"\r\n""".toByteArray())
            output.write("Content-Type: $contentType\r\n\r\n".toByteArray())
            output.write(bytes)
            output.write("\r\n--$boundary--\r\n".toByteArray())
            output.flush()
        }

        val responseCode = conn.responseCode
        val responseBody = if (responseCode in 200..299) {
            conn.inputStream.bufferedReader().readText()
        } else {
            conn.errorStream?.bufferedReader()?.readText().orEmpty()
        }
        if (responseCode in 200..299 && responseBody.contains("\"success\":true")) {
            PrintResult(true, message = "Logo sincronizado")
        } else {
            PrintResult(false, error = "Error $responseCode: $responseBody")
        }
    } catch (e: Exception) {
        PrintResult(false, error = e.message ?: "Error enviando logo")
    }
}

actual suspend fun updateCustomerDisplayOnServer(
    serverUrl: String,
    line1: String,
    line2: String,
    apiKey: String,
    apiRoute: String
): PrintResult = withContext(Dispatchers.IO) {
    try {
        val conn = URL("${serverUrl.trimEnd('/')}$apiRoute/customer-display").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        if (apiKey.isNotBlank()) conn.setRequestProperty("X-API-KEY", apiKey)
        conn.doOutput = true
        conn.connectTimeout = 5000
        conn.readTimeout = 8000
        val body = """{"data":"${esc(line1)}\n${esc(line2)}"}"""
        OutputStreamWriter(conn.outputStream).use { it.write(body) }
        val responseCode = conn.responseCode
        val responseBody = if (responseCode in 200..299) {
            conn.inputStream.bufferedReader().readText()
        } else {
            conn.errorStream?.bufferedReader()?.readText().orEmpty()
        }
        if (responseCode in 200..299 && responseBody.contains("\"success\":true")) {
            PrintResult(true, message = "Pantalla actualizada")
        } else {
            PrintResult(false, error = "Error $responseCode: $responseBody")
        }
    } catch (e: Exception) {
        PrintResult(false, error = e.message ?: "Error actualizando pantalla")
    }
}

private fun esc(s: String): String {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
