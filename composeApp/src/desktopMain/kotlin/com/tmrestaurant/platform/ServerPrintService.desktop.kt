package com.tmrestaurant.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun printTicketToServer(
    serverUrl: String,
    request: TicketPrintRequest,
    apiKey: String,
    apiRoute: String
): PrintResult = withContext(Dispatchers.IO) {
    try {
        val json = buildJsonString {
            append("{")
            append("\"company\":{")
            append("\"name\":\"${esc(request.company.name)}\",")
            append("\"commercialName\":\"${esc(request.company.commercialName)}\",")
            append("\"rnc\":\"${esc(request.company.rnc)}\",")
            append("\"address\":\"${esc(request.company.address)}\",")
            append("\"phone\":\"${esc(request.company.phone)}\"")
            append("},")
            append("\"invoice\":{")
            append("\"invoiceNumber\":\"${esc(request.invoice.invoiceNumber)}\",")
            append("\"ncf\":\"${esc(request.invoice.ncf)}\",")
            append("\"date\":\"${esc(request.invoice.date)}\",")
            append("\"time\":\"${esc(request.invoice.time)}\",")
            append("\"cashier\":\"${esc(request.invoice.cashier)}\",")
            append("\"paymentMethod\":\"${esc(request.invoice.paymentMethod)}\"")
            append("},")
            append("\"items\":[")
            request.items.forEachIndexed { i, item ->
                if (i > 0) append(",")
                append("{")
                append("\"description\":\"${esc(item.description)}\",")
                append("\"quantity\":${item.quantity},")
                append("\"price\":${item.price},")
                append("\"total\":${item.total}")
                append("}")
            }
            append("],")
            append("\"totals\":{")
            append("\"subtotal\":${request.totals.subtotal},")
            append("\"tax\":${request.totals.tax},")
            append("\"discount\":${request.totals.discount},")
            append("\"grandTotal\":${request.totals.grandTotal}")
            append("},")
            append("\"copies\":${request.copies},")
            append("\"cutPaper\":${request.cutPaper},")
            append("\"openDrawer\":${request.openDrawer},")
            append("\"note\":\"${esc(request.note)}\"")
            append(",\"qrUrl\":\"${esc(request.qrUrl)}\"")
            append(",\"branding\":{")
            append("\"fontSize\":${request.branding.fontSize},")
            append("\"fontSizeLg\":${request.branding.fontSizeLg},")
            append("\"fontSizeTotal\":${request.branding.fontSizeTotal},")
            append("\"logoMaxWidth\":${request.branding.logoMaxWidth},")
            append("\"logoMaxHeight\":${request.branding.logoMaxHeight},")
            append("\"printLogo\":${request.branding.printLogo},")
            append("\"showCompanyName\":${request.branding.showCompanyName},")
            append("\"showCompanyRnc\":${request.branding.showCompanyRnc},")
            append("\"showCompanyAddress\":${request.branding.showCompanyAddress},")
            append("\"showCompanyPhone\":${request.branding.showCompanyPhone},")
            append("\"showCompanyEmail\":${request.branding.showCompanyEmail},")
            append("\"showCustomerName\":${request.branding.showCustomerName},")
            append("\"showCustomerRnc\":${request.branding.showCustomerRnc},")
            append("\"showCustomerPhone\":${request.branding.showCustomerPhone},")
            append("\"showDateTime\":${request.branding.showDateTime},")
            append("\"showCashierName\":${request.branding.showCashierName},")
            append("\"showTaxes\":${request.branding.showTaxes},")
            append("\"showDiscounts\":${request.branding.showDiscounts},")
            append("\"showTip\":${request.branding.showTip},")
            append("\"showPaymentMethod\":${request.branding.showPaymentMethod},")
            append("\"showCashChange\":${request.branding.showCashChange},")
            append("\"showThankYouMessage\":${request.branding.showThankYouMessage},")
            append("\"showQr\":${request.branding.showQr},")
            append("\"thankYouMessage\":\"${esc(request.branding.thankYouMessage)}\",")
            append("\"compact\":${request.branding.compact},")
            append("\"showWarranty\":false,")
            append("\"showSupportInfo\":false")
            append("}")
            append("}")
        }
        val conn = URL("${serverUrl.trimEnd('/')}$apiRoute/printer/ticket").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        if (apiKey.isNotBlank()) conn.setRequestProperty("X-API-Key", apiKey)
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        OutputStreamWriter(conn.outputStream).use { it.write(json) }
        val code = conn.responseCode
        conn.disconnect()
        if (code in 200..299) PrintResult(true, "Impreso exitosamente")
        else PrintResult(false, error = "Error HTTP $code")
    } catch (e: Exception) {
        PrintResult(false, error = e.message ?: "Error de conexion")
    }
}

actual suspend fun uploadTicketLogoToServer(
    serverUrl: String,
    fileName: String,
    bytes: ByteArray,
    apiKey: String,
    apiRoute: String
): PrintResult = withContext(Dispatchers.IO) {
    try {
        val boundary = "Boundary${System.currentTimeMillis()}"
        val conn = URL("${serverUrl.trimEnd('/')}$apiRoute/config/logo").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        if (apiKey.isNotBlank()) conn.setRequestProperty("X-API-Key", apiKey)
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        val crlf = "\r\n"
        val body = buildString {
            append("--$boundary$crlf")
            append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$crlf")
            append("Content-Type: application/octet-stream$crlf$crlf")
        }.toByteArray(Charsets.UTF_8)
        val footer = "$crlf--$boundary--$crlf".toByteArray(Charsets.UTF_8)
        conn.outputStream.use { os ->
            os.write(body)
            os.write(bytes)
            os.write(footer)
        }
        val code = conn.responseCode
        conn.disconnect()
        if (code in 200..299) PrintResult(true, "Logo subido exitosamente")
        else PrintResult(false, error = "Error HTTP $code")
    } catch (e: Exception) {
        PrintResult(false, error = e.message ?: "Error de conexion")
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
        val json = "{\"line1\":\"${esc(line1)}\",\"line2\":\"${esc(line2)}\"}"
        val conn = URL("${serverUrl.trimEnd('/')}$apiRoute/customer-display").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        if (apiKey.isNotBlank()) conn.setRequestProperty("X-API-Key", apiKey)
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        OutputStreamWriter(conn.outputStream).use { it.write(json) }
        val code = conn.responseCode
        conn.disconnect()
        if (code in 200..299) PrintResult(true, "Display actualizado")
        else PrintResult(false, error = "Error HTTP $code")
    } catch (e: Exception) {
        PrintResult(false, error = e.message ?: "Error de conexion")
    }
}

private fun esc(s: String): String = s
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")
    .replace("\t", "\\t")

private fun buildJsonString(block: StringBuilder.() -> Unit): String {
    val sb = StringBuilder()
    sb.block()
    return sb.toString()
}
