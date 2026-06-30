package com.tmrestaurant.ui.screens.pos

import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.data.settings.CompanySettings
import com.tmrestaurant.ui.data.settings.PrintSettings
import com.tmrestaurant.ui.data.QrGenerator
import com.tmrestaurant.platform.formatDateTime

fun buildReceiptText(
    result: PaymentResult,
    company: CompanySettings,
    print: PrintSettings,
    paperWidthMm: String = "80"
): String {
    val width = if (paperWidthMm.trim() == "80") 48 else 32

    fun money(value: Double) = "RD$ ${"%,.2f".format(value)}"
    fun line(char: Char = '-') = char.toString().repeat(width)
    fun fit(value: String, size: Int): String =
        if (value.length <= size) value.padEnd(size) else value.take((size - 1).coerceAtLeast(0)) + "."
    fun center(value: String): String {
        val text = value.take(width)
        val left = ((width - text.length) / 2).coerceAtLeast(0)
        return " ".repeat(left) + text
    }
    fun keyValue(label: String, value: String): String {
        val cleanValue = value.take(width)
        val spaces = (width - label.length - cleanValue.length).coerceAtLeast(1)
        return label + " ".repeat(spaces) + cleanValue
    }
    fun fiscalRow(left: String, right: String): String {
        val rightText = right.take(width / 2)
        val leftWidth = (width - rightText.length).coerceAtLeast(1)
        return fit(left, leftWidth) + rightText
    }
    fun moneyRow(label: String, tax: String, amount: String): String {
        val amountWidth = 12
        val taxWidth = 10
        val labelWidth = width - taxWidth - amountWidth
        return fit(label, labelWidth) + tax.padStart(taxWidth) + amount.padStart(amountWidth)
    }
    fun wrap(text: String): List<String> {
        if (text.length <= width) return listOf(text)
        val lines = mutableListOf<String>()
        var remaining = text
        while (remaining.length > width) {
            lines.add(remaining.take(width))
            remaining = remaining.drop(width)
        }
        if (remaining.isNotEmpty()) lines.add(remaining)
        return lines
    }
    fun barcode(value: String): List<String> {
        val bars = "|||| || ||| | |||| || | ||| |||| | || |||"
        return listOf(center(bars.take(width)), center(value.take(width)))
    }
    fun qrBlock(text: String): List<String> {
        val matrix = QrGenerator.generate(text)
        val fullPattern = QrGenerator.toAscii(matrix)
        val lines = fullPattern.lines()
        val qrWidth = lines.firstOrNull()?.length ?: 0
        return lines.map { line -> center(line) }
    }

    val receiptNo = result.invoiceNumber.ifBlank { "00000SF-10000259323" }
    val businessName = company.businessName.ifBlank { "TM-RESTAURANTE" }
    val address = company.address.ifBlank { "Direccion no configurada" }
    val phone = company.phone.ifBlank { "809-555-1234" }
    val rnc = company.rnc.ifBlank { "000000000" }

    return buildString {
        if (print.showCompanyName) appendLine(center(businessName))
        if (print.showCompanyAddress) appendLine(center(address.uppercase()))
        if (print.showCompanyPhone) appendLine(center("Telefono: $phone"))
        if (print.showCompanyRnc) appendLine(center("RNC/Cedula: $rnc"))
        if (print.showCompanyName || print.showCompanyAddress || print.showCompanyPhone || print.showCompanyRnc) {
            appendLine(line())
        }
        if (print.showReceiptNumber) appendLine(keyValue("No. de Recibo:", receiptNo))
        if (print.showDateTime) appendLine(fiscalRow("Fecha: ${formatDateTime(result.timestamp)}", "Trans:${result.invoiceNumber.takeLast(6)}"))
        if (print.showNcf) appendLine(keyValue("NCF:", result.ncf))
        if (print.showNcfExpiry) appendLine(keyValue("NCF Valido Hasta:", "31/12/2027 12:00:00 a. m."))
        if (print.showReceiptNumber || print.showDateTime || print.showNcf || print.showNcfExpiry) {
            appendLine(line())
        }
        if (print.showInvoiceTitle) {
            appendLine(center("FACTURA PARA CONSUMIDOR FINAL"))
            appendLine(line())
        }
        if (print.showItems) {
            appendLine(fit("Descripcion", width - 22) + "ITBIS".padStart(10) + "VALOR".padStart(12))
            appendLine(line())
            val sf = if (result.surchargePercent > 0) 1.0 + result.surchargePercent / 100.0 else 1.0
            result.items.forEach { item ->
                val itemSurchargedPrice = item.product.price * sf
                val itemTotal = itemSurchargedPrice * item.effectiveQuantity + item.extrasCost
                val isPropina = item.product.code == "PROPINA-LEY"
                val displayPrice = itemSurchargedPrice + item.extrasCost / item.quantity.coerceAtLeast(1)
                appendLine("${item.quantity} X ${"%.2f".format(displayPrice)}")
                if (!isPropina) {
                    val itemTax = itemTotal - (itemTotal / 1.18)
                    appendLine(item.product.id.toString().padStart(13, '0'))
                    appendLine(moneyRow("18%-ITBIS", money(itemTax), money(itemTotal)))
                } else {
                    appendLine("0000000000000")
                    appendLine(moneyRow("", "", money(itemTotal)))
                }
                val itemName = if (item.extrasNote.isNotBlank()) "${item.product.name} + ${item.extrasNote}" else item.product.name
                wrap(itemName.uppercase()).forEach { appendLine(it) }
                appendLine(line())
            }
        }
        if (print.showSubtotal) appendLine(moneyRow("SUBTOTAL", money(result.taxAmount), money(result.subtotalPreTax)))
        if (print.showTotal) appendLine(moneyRow("TOTAL", money(result.taxAmount), money(result.total)))
        if (result.discountAmount > 0 && print.showDiscounts) {
            appendLine(moneyRow("DESCUENTO", "", money(-result.discountAmount)))
        }
        if (print.showPaymentMethod) appendLine(moneyRow(result.paymentMethod.uppercase(), "", money(result.receivedAmount.takeIf { it > 0 } ?: result.total)))
        if (result.change > 0 && print.showCashChange) appendLine(moneyRow("CAMBIO", "", money(result.change)))
        val totalsOrPaymentShown = print.showSubtotal || print.showTotal || print.showDiscounts || print.showPaymentMethod || print.showCashChange
        if (totalsOrPaymentShown) appendLine(line())
        if (print.showItemCount) appendLine("Cantidad Articulos: ${result.items.sumOf { it.quantity } + if (result.tipAmount > 0) 1 else 0}")
        if (print.showCashRegister) appendLine("Caja No.: SF-10")
        if (print.showCashierName) {
            appendLine("Le Atendio: ADMINISTRADOR")
            appendLine("Cajero: 0503")
        }
        if (result.note.isNotBlank() && print.showNote) appendLine("Nota: ${result.note}")
        val footerShown = print.showItemCount || print.showCashRegister || print.showCashierName || print.showNote
        if (footerShown || print.showTaxSummary || print.showReturnPolicy) {
            appendLine()
            appendLine(line())
        }
        if (print.showTaxSummary) {
            appendLine(center("TOTAL ITBIS PAGADO"))
            appendLine(center("TOTAL 18%-ITBIS PAGADO: ${money(result.taxAmount)}"))
            appendLine(line())
        }
        if (print.showReturnPolicy) {
            appendLine(center("**VALIDO PARA DEVOLUCION POR 7 DIAS.**"))
            appendLine(center("**DEBE PRESENTAR FACTURA ORIGINAL.**"))
            appendLine(center("**NO APLICAN ARTICULOS REFRIGERADOS,**"))
            appendLine(center("COSMETICOS, NI ROPA INTIMA.**"))
            appendLine(center("!!NO REEMBOLSAMOS DINERO EN EFECTIVO!!"))
            appendLine()
        }
        if (print.showThankYouMessage) appendLine(center("!!GRACIAS POR PREFERIRNOS!!"))
        if (print.showBarcode) {
            appendLine()
            barcode(receiptNo).forEach { appendLine(it) }
        }
        if (print.showQr) {
            appendLine()
            val dgiiUrl = QrGenerator.dgiiUrl(
                rnc = company.rnc,
                ncf = result.ncf,
                total = result.total
            )
            qrBlock(dgiiUrl).forEach { appendLine(it) }
        }
        if (print.showFooterDate) {
            if (!print.showBarcode && !print.showQr) appendLine()
            append(center(formatDateTime(result.timestamp)))
        }
    }
}
