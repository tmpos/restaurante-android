package com.tmrestaurant.platform

import com.tmrestaurant.ui.data.settings.PrintSettings

data class TicketCompany(
    val name: String,
    val commercialName: String,
    val rnc: String,
    val address: String,
    val phone: String,
    val email: String = "",
    val website: String = ""
)

data class TicketCustomer(
    val name: String,
    val rnc: String = "",
    val phone: String = ""
)

data class TicketInvoice(
    val invoiceNumber: String,
    val ncf: String = "",
    val date: String,
    val time: String,
    val cashier: String,
    val paymentMethod: String = "Efectivo"
)

data class TicketItem(
    val description: String,
    val quantity: Int,
    val price: Double,
    val tax: Double = 0.0,
    val total: Double,
    val code: String = ""
)

data class TicketTotals(
    val subtotal: Double,
    val tax: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double = 0.0,
    val changeAmount: Double = 0.0
)

data class TicketPrintRequest(
    val company: TicketCompany,
    val invoice: TicketInvoice,
    val customer: TicketCustomer,
    val items: List<TicketItem>,
    val totals: TicketTotals,
    val payment: Map<String, String> = mapOf("method" to "Efectivo"),
    val copies: Int = 1,
    val cutPaper: Boolean = true,
    val openDrawer: Boolean = false,
    val qrUrl: String = "",
    val branding: TicketBranding = TicketBranding(),
    val note: String = ""
)

data class TicketBranding(
    val fontSize: Int = 28,
    val fontSizeLg: Int = 58,
    val fontSizeTotal: Int = 38,
    val logoMaxWidth: Int = 408,
    val logoMaxHeight: Int = 160,
    val printLogo: Boolean = true,
    val showCompanyName: Boolean = true,
    val showCompanyRnc: Boolean = true,
    val showCompanyAddress: Boolean = true,
    val showCompanyPhone: Boolean = true,
    val showCompanyEmail: Boolean = false,
    val showCustomerName: Boolean = true,
    val showCustomerRnc: Boolean = true,
    val showCustomerPhone: Boolean = false,
    val showDateTime: Boolean = true,
    val showCashierName: Boolean = true,
    val showTaxes: Boolean = true,
    val showDiscounts: Boolean = true,
    val showTip: Boolean = true,
    val showPaymentMethod: Boolean = true,
    val showCashChange: Boolean = true,
    val showThankYouMessage: Boolean = true,
    val showQr: Boolean = true,
    val thankYouMessage: String = "Gracias por su compra!",
    val compact: Boolean = true
)

fun ticketBranding(print: PrintSettings): TicketBranding {
    val (body, large, total) = when (print.textSize.lowercase()) {
        "small" -> Triple(20, 36, 28)
        "large" -> Triple(34, 64, 46)
        else -> Triple(28, 52, 38)
    }
    return TicketBranding(
        fontSize = body,
        fontSizeLg = large,
        fontSizeTotal = total,
        logoMaxWidth = ((print.logoWidthMm.toFloatOrNull() ?: 51f) * 8f).toInt().coerceIn(80, 544),
        logoMaxHeight = ((print.logoHeightMm.toFloatOrNull() ?: 20f) * 8f).toInt().coerceIn(40, 400),
        printLogo = print.showCompanyLogo,
        showCompanyName = print.showCompanyName,
        showCompanyRnc = print.showCompanyRnc,
        showCompanyAddress = print.showCompanyAddress,
        showCompanyPhone = print.showCompanyPhone,
        showCompanyEmail = print.showCompanyEmail,
        showCustomerName = print.showCustomerName,
        showCustomerRnc = print.showCustomerRnc,
        showCustomerPhone = print.showCustomerPhone,
        showDateTime = print.showDateTime,
        showCashierName = print.showCashierName,
        showTaxes = print.showTaxes,
        showDiscounts = print.showDiscounts,
        showTip = print.showTip,
        showPaymentMethod = print.showPaymentMethod,
        showCashChange = print.showCashChange,
        showThankYouMessage = print.showThankYouMessage,
        showQr = print.showQr,
        thankYouMessage = print.thankYouMessage,
        compact = true
    )
}

data class PrintResult(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

expect suspend fun printTicketToServer(
    serverUrl: String,
    request: TicketPrintRequest,
    apiKey: String = "",
    apiRoute: String = ""
): PrintResult

expect suspend fun uploadTicketLogoToServer(
    serverUrl: String,
    fileName: String,
    bytes: ByteArray,
    apiKey: String = "",
    apiRoute: String = ""
): PrintResult

expect suspend fun updateCustomerDisplayOnServer(
    serverUrl: String,
    line1: String,
    line2: String,
    apiKey: String = "",
    apiRoute: String = ""
): PrintResult
