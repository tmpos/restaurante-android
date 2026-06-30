package com.tmrestaurant.ui.screens.mesas

import androidx.compose.runtime.*
import com.tmrestaurant.platform.TicketCompany
import com.tmrestaurant.platform.TicketCustomer
import com.tmrestaurant.platform.TicketInvoice
import com.tmrestaurant.platform.TicketItem
import com.tmrestaurant.platform.TicketPrintRequest
import com.tmrestaurant.platform.TicketTotals
import com.tmrestaurant.platform.eloPrintTestTicket
import com.tmrestaurant.platform.eloPrintTicketCopies
import com.tmrestaurant.platform.TicketPrintStyle
import com.tmrestaurant.platform.ticketBranding
import com.tmrestaurant.platform.eloOpenCashDrawer
import com.tmrestaurant.platform.printTicketToServer
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.components.PaymentModal
import com.tmrestaurant.ui.components.PaymentSuccessModal
import com.tmrestaurant.ui.data.InvoiceHistory
import com.tmrestaurant.ui.data.QrGenerator
import com.tmrestaurant.ui.data.MesasManager
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.RecipeInventoryManager
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.screens.pos.buildReceiptText

@Composable
fun MesaCobrarModal(
    mesaId: Int,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val mesa = MesasManager.mesas.find { it.id == mesaId }
    if (mesa == null) {
        onDismiss()
        return
    }

    var showSuccess by remember { mutableStateOf(false) }
    var fullResult by remember { mutableStateOf<com.tmrestaurant.ui.components.PaymentResult?>(null) }

    val settings = LocalSettingsState.current
    val productState = LocalProductState.current
    val items = mesa.items
    val total = items.sumOf { it.product.price * it.effectiveQuantity }
    val subtotalPreTax = total / 1.18
    val taxAmount = total - subtotalPreTax

    if (!showSuccess) {
        val invoiceNumber = "MESA-${System.currentTimeMillis().toString().takeLast(8)}"
        val ncf = remember(invoiceNumber) { com.tmrestaurant.ui.data.NcfManager.getNextNcf() }

        PaymentModal(
            cartItems = items,
            subtotalPreTax = subtotalPreTax,
            taxAmount = taxAmount,
            total = total,
            invoiceNumber = invoiceNumber,
            ncf = ncf,
            paymentMethods = settings.settings.paymentMethods.methods,
            onDismiss = onDismiss,
            onPaymentComplete = { result ->
                val r = result.copy(turnoId = TurnoManager.currentTurno?.id ?: "")
                RecipeInventoryManager.applySale(r.items, productState)
                val saved = InvoiceHistory.add(r)
                MesasManager.clearMesa(mesaId)
                fullResult = saved
                showSuccess = true
            }
        )
    }

    fullResult?.let { result ->
        if (showSuccess) {
            val pw = settings.settings.print.paperWidthMm
            val receipt = buildReceiptText(result, settings.settings.company, settings.settings.print, pw)

            PaymentSuccessModal(
                invoiceNumber = result.invoiceNumber,
                ncf = result.ncf,
                total = result.total,
                printerName = settings.settings.print.selectedPrinter,
                onPrint = {
                    var printed = false
                    if (settings.settings.server.enabled && settings.settings.server.serverUrl.isNotBlank()) {
                        val company = settings.settings.company
                        val invoiceDt = formatDateTime(result.timestamp).split(" ")
                        val ticketRequest = TicketPrintRequest(
                            company = TicketCompany(name = company.businessName, commercialName = company.businessName, rnc = company.rnc, address = company.address, phone = company.phone),
                            invoice = TicketInvoice(invoiceNumber = result.invoiceNumber, ncf = result.ncf, date = invoiceDt[0], time = invoiceDt.getOrElse(1) { "" }, cashier = "", paymentMethod = result.paymentMethod),
                            customer = TicketCustomer(name = "Cliente", rnc = "", phone = ""),
                            items = result.items.map { item ->
                                val sf = if (result.surchargePercent > 0) 1.0 + result.surchargePercent / 100.0 else 1.0
                                val itemPrice = item.product.price * sf
                                val itemTotal = itemPrice * item.effectiveQuantity
                                TicketItem(description = if (item.extrasNote.isBlank()) item.product.name else "${item.product.name} - ${item.extrasNote}", quantity = item.quantity, price = itemPrice, tax = itemTotal - (itemTotal / 1.18), total = itemTotal, code = item.product.code)
                            },
                            totals = TicketTotals(subtotal = result.subtotalPreTax, tax = result.taxAmount, grandTotal = result.total, paidAmount = result.receivedAmount, changeAmount = result.change),
                            payment = mapOf("method" to result.paymentMethod),
                            copies = settings.settings.print.copies,
                            openDrawer = result.paymentMethod.uppercase().contains("EFECTIVO"),
                            branding = ticketBranding(settings.settings.print),
                            note = result.note,
                            qrUrl = QrGenerator.dgiiUrl(company.rnc, result.ncf, result.total)
                        )
                        val sr = printTicketToServer(settings.settings.server.serverUrl, ticketRequest, settings.settings.server.apiKey, settings.settings.server.apiRoute)
                        printed = sr.success
                    }
                    if (!printed) {
                        val print = settings.settings.print
                        val printResult = eloPrintTicketCopies(
                            print.selectedPrinter,
                            receipt,
                            pw,
                            print.copies,
                            TicketPrintStyle(
                                textSize = print.textSize,
                                logoWidthMm = print.logoWidthMm,
                                logoHeightMm = print.logoHeightMm,
                                showLogo = print.showCompanyLogo,
                                logoBytes = settings.getLogoBytes()
                            )
                        )
                        if (printResult.success && result.paymentMethod.uppercase().contains("EFECTIVO")) {
                            eloOpenCashDrawer(settings.settings.print.selectedPrinter)
                        }
                    }
                },
                onNewSale = {
                    showSuccess = false
                    fullResult = null
                    onComplete()
                }
            )
        }
    }
}
