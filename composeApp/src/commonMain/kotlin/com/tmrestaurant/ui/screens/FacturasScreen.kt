package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tmrestaurant.platform.SmtpConfig
import com.tmrestaurant.platform.eloPrintTestTicket
import com.tmrestaurant.platform.eloPrintTicketCopies
import com.tmrestaurant.platform.TicketPrintStyle
import com.tmrestaurant.platform.ticketBranding
import com.tmrestaurant.platform.printTicketToServer
import com.tmrestaurant.platform.sendEmail
import com.tmrestaurant.platform.TicketPrintRequest
import com.tmrestaurant.platform.TicketCompany
import com.tmrestaurant.platform.TicketCustomer
import com.tmrestaurant.platform.TicketInvoice
import com.tmrestaurant.platform.TicketItem
import com.tmrestaurant.platform.TicketTotals
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.components.ReturnModal
import com.tmrestaurant.ui.components.ReturnedItem
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.QrGenerator
import com.tmrestaurant.ui.data.InvoiceHistory
import com.tmrestaurant.ui.data.ProductState
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import com.tmrestaurant.ui.screens.pos.buildReceiptText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun FacturasScreen() {
    val invoices = InvoiceHistory.invoices
    val settingsState = LocalSettingsState.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var expandedInvoice by remember { mutableStateOf<String?>(null) }
    val selectedInvoices = remember { mutableStateListOf<PaymentResult>() }

    var showOtpDialog by remember { mutableStateOf(false) }
    var currentOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var sendingOtp by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf<String?>(null) }
    val canManageInvoices = AccessControl.canManageInvoices(TurnoManager.currentUser)

    var returnInvoiceIndex by remember { mutableStateOf(-1) }
    val productState = LocalProductState.current

    val filteredInvoices = remember(invoices, searchQuery) {
        if (searchQuery.isBlank()) invoices
        else invoices.filter {
            it.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
            it.ncf.contains(searchQuery, ignoreCase = true) ||
            it.paymentMethod.contains(searchQuery, ignoreCase = true)
        }
    }

    fun sendOtpForSelection() {
        currentOtp = Random.nextInt(100000, 999999).toString()
        enteredOtp = ""
        otpError = null
        sendingOtp = true
        showOtpDialog = true
        scope.launch {
            val s = settingsState.settings
            val companyEmail = s.company.email
            val smtp = s.notifications
            if (companyEmail.isBlank()) {
                otpError = "No hay correo configurado en Empresa"; sendingOtp = false; return@launch
            }
            if (smtp.senderEmail.isBlank() || smtp.appPassword.isBlank()) {
                otpError = "Configure SMTP en Notificaciones"; sendingOtp = false; return@launch
            }
            val config = SmtpConfig(
                host = smtp.smtpServer, port = smtp.smtpPort.toIntOrNull() ?: 465,
                username = smtp.senderEmail, password = smtp.appPassword, useSsl = smtp.sslTls
            )
            val nums = selectedInvoices.joinToString(", ") { it.invoiceNumber }
            val result = withContext(Dispatchers.IO) {
                sendEmail(
                    config = config, fromName = smtp.senderName, fromAddr = smtp.senderEmail,
                    to = companyEmail,
                    subject = "OTP - Eliminar ${selectedInvoices.size} factura(s)",
                    body = "Se solicita eliminar las facturas: $nums\n\nCódigo OTP: $currentOtp\n\nSi no solicitó esta operación, ignore este mensaje."
                )
            }
            sendingOtp = false
            if (!result.success) otpError = "Error enviando OTP: ${result.error}"
        }
    }

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Outlined.Receipt, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
                Text("Facturas", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Text("${invoices.size} registros", color = AppColors.TextSecondary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                placeholder = { Text("Buscar por factura, NCF o método...", color = AppColors.TextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = AppColors.IconGray, modifier = Modifier.size(20.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary, unfocusedBorderColor = AppColors.Border,
                    focusedContainerColor = AppColors.Surface, unfocusedContainerColor = AppColors.Surface, cursorColor = AppColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            if (selectedInvoices.isNotEmpty() && canManageInvoices) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7)).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("${selectedInvoices.size} seleccionada(s)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF92400E), modifier = Modifier.weight(1f))
                    Box(
                        Modifier.height(48.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Danger)
                            .clickable(onClick = { sendOtpForSelection() }).padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Delete, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Eliminar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFDE68A))
                            .clickable(onClick = { selectedInvoices.clear() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Close, null, tint = Color(0xFF92400E), modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (filteredInvoices.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Receipt, null, tint = AppColors.Border, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (searchQuery.isNotBlank()) "No se encontraron facturas" else "No hay facturas registradas",
                            color = AppColors.TextSecondary, fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredInvoices, key = { it.hashCode() }) { invoice ->
                        FacturaCard(
                            invoice = invoice,
                            index = filteredInvoices.indexOf(invoice),
                            isExpanded = expandedInvoice == invoice.invoiceNumber,
                            isSelected = invoice in selectedInvoices,
                            onToggleExpand = {
                                expandedInvoice = if (expandedInvoice == invoice.invoiceNumber) null else invoice.invoiceNumber
                            },
                            onToggleSelect = {
                                if (canManageInvoices) {
                                    if (invoice in selectedInvoices) selectedInvoices.remove(invoice)
                                    else selectedInvoices.add(invoice)
                                }
                            },
                            onReturn = { idx -> if (canManageInvoices) returnInvoiceIndex = idx },
                            onReprint = {
                                scope.launch {
                                    val s = settingsState
                                    val pw = s.settings.print.paperWidthMm
                                    val receipt = buildReceiptText(invoice, s.settings.company, s.settings.print, pw)
                                    var printed = false
                                    if (s.settings.server.enabled && s.settings.server.serverUrl.isNotBlank()) {
                                        val company = s.settings.company
                                        val invoiceDt = formatDateTime(invoice.timestamp).split(" ")
                                        val ticketRequest = TicketPrintRequest(
                                            company = TicketCompany(name = company.businessName, commercialName = company.businessName, rnc = company.rnc, address = company.address, phone = company.phone),
                                            invoice = TicketInvoice(invoiceNumber = invoice.invoiceNumber, ncf = invoice.ncf, date = invoiceDt[0], time = invoiceDt.getOrElse(1) { "" }, cashier = "", paymentMethod = invoice.paymentMethod),
                                            customer = TicketCustomer(name = "Cliente", rnc = "", phone = ""),
                                            items = invoice.items.map { item ->
                                                val sf = if (invoice.surchargePercent > 0) 1.0 + invoice.surchargePercent / 100.0 else 1.0
                                                val itemPrice = item.product.price * sf
                                                val itemTotal = itemPrice * item.effectiveQuantity
                                                TicketItem(description = if (item.extrasNote.isBlank()) item.product.name else "${item.product.name} - ${item.extrasNote}", quantity = item.quantity, price = itemPrice, tax = itemTotal - (itemTotal / 1.18), total = itemTotal, code = item.product.code)
                                            },
                                            totals = TicketTotals(subtotal = invoice.subtotalPreTax, tax = invoice.taxAmount, grandTotal = invoice.total, paidAmount = invoice.receivedAmount, changeAmount = invoice.change),
                                            payment = mapOf("method" to invoice.paymentMethod),
                                            copies = s.settings.print.copies,
                                            openDrawer = false,
                                            branding = ticketBranding(s.settings.print),
                                            note = invoice.note,
                                            qrUrl = QrGenerator.dgiiUrl(company.rnc, invoice.ncf, invoice.total)
                                        )
                                        val sr = printTicketToServer(s.settings.server.serverUrl, ticketRequest, s.settings.server.apiKey, s.settings.server.apiRoute)
                                        if (sr.success) { snack.showSnackbar("Imprimiendo en servidor..."); printed = true }
                                        else snack.showSnackbar("Servidor: ${sr.error}")
                                    }
                                    if (!printed) {
                                        val print = s.settings.print
                                        val pr = eloPrintTicketCopies(
                                            print.selectedPrinter,
                                            receipt,
                                            pw,
                                            print.copies,
                                            TicketPrintStyle(
                                                textSize = print.textSize,
                                                logoWidthMm = print.logoWidthMm,
                                                logoHeightMm = print.logoHeightMm,
                                                showLogo = print.showCompanyLogo,
                                                logoBytes = s.getLogoBytes()
                                            )
                                        )
                                        if (pr.success) snack.showSnackbar("Imprimiendo recibo...")
                                        else snack.showSnackbar("Error: ${pr.message}")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        SnackbarHost(hostState = snack, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    if (returnInvoiceIndex >= 0 && returnInvoiceIndex < invoices.size) {
        val inv = invoices[returnInvoiceIndex]
        ReturnModal(
            invoiceNumber = inv.invoiceNumber,
            items = inv.items,
            onConfirm = { returned ->
                if (!canManageInvoices) {
                    return@ReturnModal
                }
                com.tmrestaurant.ui.data.RecipeInventoryManager.revertReturn(inv.items, returned, productState)
                InvoiceHistory.processReturn(returnInvoiceIndex, returned)
                returnInvoiceIndex = -1
                scope.launch { snack.showSnackbar("Devolucion procesada: ${returned.sumOf { it.quantity }} unidades") }
            },
            onDismiss = { returnInvoiceIndex = -1 }
        )
    }

    if (showOtpDialog) {
        OtpDialog(
            invoiceCount = selectedInvoices.size,
            invoiceLabel = if (selectedInvoices.size == 1) selectedInvoices.first().invoiceNumber else "${selectedInvoices.size} facturas",
            sending = sendingOtp,
            error = otpError,
            enteredOtp = enteredOtp,
            onOtpChange = { enteredOtp = it },
            onConfirm = {
                if (enteredOtp == currentOtp) {
                    val toRemove = selectedInvoices.toList()
                    selectedInvoices.clear()
                    toRemove.forEach { inv ->
                        val idx = invoices.indexOf(inv)
                        if (idx >= 0) InvoiceHistory.removeAt(idx, productState)
                    }
                    showOtpDialog = false
                    scope.launch { snack.showSnackbar("${toRemove.size} factura(s) eliminada(s)") }
                } else {
                    otpError = "Código incorrecto"
                }
            },
            onDismiss = {
                showOtpDialog = false
            }
        )
    }
}

@Composable
private fun OtpDialog(
    invoiceCount: Int,
    invoiceLabel: String,
    sending: Boolean,
    error: String?,
    enteredOtp: String,
    onOtpChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier.width(420.dp).clip(RoundedCornerShape(16.dp)).background(AppColors.Surface).padding(28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(40.dp))
                Text("Eliminar ${if (invoiceCount > 1) "Facturas" else "Factura"}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary)
                Text(invoiceLabel, fontSize = 16.sp, color = AppColors.TextSecondary)
                if (sending) {
                    Text("Enviando código OTP al correo...", fontSize = 15.sp, color = AppColors.TextSecondary)
                } else {
                    Text("Ingrese el código OTP enviado al correo", fontSize = 15.sp, color = AppColors.TextSecondary)
                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { if (it.length <= 6) onOtpChange(it.filter { c -> c.isDigit() }) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        placeholder = { Text("000000", fontSize = 22.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (enteredOtp.length == 6) onConfirm() }),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Primary, unfocusedBorderColor = AppColors.Border,
                            focusedContainerColor = AppColors.Background, unfocusedContainerColor = AppColors.Background, cursorColor = AppColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (error != null) Text(error, color = AppColors.Danger, fontSize = 13.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Cancelar", color = AppColors.TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Box(
                            Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Danger)
                                .clickable(enabled = enteredOtp.length == 6, onClick = onConfirm),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Eliminar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FacturaCard(
    invoice: PaymentResult,
    index: Int,
    isExpanded: Boolean,
    isSelected: Boolean,
    onToggleExpand: () -> Unit,
    onToggleSelect: () -> Unit,
    onReturn: (Int) -> Unit,
    onReprint: () -> Unit
) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFFFEFCE8) else AppColors.Surface)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFEAB308) else AppColors.Border, RoundedCornerShape(10.dp))
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onToggleSelect),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        null,
                        tint = if (isSelected) Color(0xFFEAB308) else AppColors.IconGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f).clickable(onClick = onToggleExpand)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (invoice.orderNumber > 0) {
                            Box(
                                Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFEDE9FE)).padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text("ORD #${invoice.orderNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                            }
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        invoice.paymentMethod.uppercase().contains("EFECTIVO") -> Color(0xFFDCFCE7)
                                        invoice.paymentMethod.uppercase().contains("TARJETA") -> Color(0xFFDBEAFE)
                                        invoice.paymentMethod.uppercase().contains("TRANSFERENCIA") -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFF3F4F6)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(invoice.paymentMethod, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                color = when {
                                    invoice.paymentMethod.uppercase().contains("EFECTIVO") -> Color(0xFF16A34A)
                                    invoice.paymentMethod.uppercase().contains("TARJETA") -> Color(0xFF2563EB)
                                    invoice.paymentMethod.uppercase().contains("TRANSFERENCIA") -> Color(0xFFD97706)
                                    else -> AppColors.TextSecondary
                                },
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("RD\$ ${"%,.2f".format(invoice.total)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("${invoice.items.size} artículos", color = AppColors.TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(invoice.ncf.ifBlank { "NCF: N/A" }, color = AppColors.TextSecondary, fontSize = 11.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        Modifier.height(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEFF6FF))
                            .clickable(onClick = onReprint).padding(horizontal = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()) {
                            Icon(Icons.Outlined.Print, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reimp.", color = AppColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (invoice.status == "ACTIVA") {
                        Box(
                            Modifier.height(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2))
                                .clickable { onReturn(index) }.padding(horizontal = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()) {
                                Icon(Icons.Outlined.Receipt, null, tint = AppColors.Danger, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Devolver", color = AppColors.Danger, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Icon(
                        if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        null, tint = AppColors.IconGray, modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (isExpanded) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
                Column(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF8FAFC)).padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            if (invoice.orderNumber > 0) {
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Orden #:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                    Spacer(Modifier.width(6.dp))
                                    Text(invoice.orderNumber.toString(), fontSize = 11.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Text("Fecha:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                Spacer(Modifier.width(6.dp))
                                Text(formatDateTime(invoice.timestamp), fontSize = 11.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Text("NCF:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                Spacer(Modifier.width(6.dp))
                                Text(invoice.ncf.ifBlank { "N/A" }, fontSize = 11.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Text("Estado:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                Spacer(Modifier.width(6.dp))
                                Text(invoice.status, fontSize = 11.sp, color = if (invoice.status == "ACTIVA") Color(0xFF16A34A) else AppColors.Danger, fontWeight = FontWeight.SemiBold)
                            }
                            if (invoice.customerName.isNotBlank()) {
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Cliente:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                    Spacer(Modifier.width(6.dp))
                                    Text(invoice.customerName, fontSize = 11.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (invoice.customerRnc.isNotBlank()) {
                                Row(Modifier.fillMaxWidth()) {
                                    Text("RNC/Cedula:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                    Spacer(Modifier.width(6.dp))
                                    Text(invoice.customerRnc, fontSize = 11.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (invoice.customerPhone.isNotBlank()) {
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Telefono:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                    Spacer(Modifier.width(6.dp))
                                    Text(invoice.customerPhone, fontSize = 11.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (invoice.deliveryAddress.isNotBlank()) {
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Direccion:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                    Spacer(Modifier.width(6.dp))
                                    Text(invoice.deliveryAddress, fontSize = 11.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (invoice.deliveryStatus.isNotBlank()) {
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Delivery:", fontSize = 11.sp, color = AppColors.TextSecondary)
                                    Spacer(Modifier.width(6.dp))
                                    Text(invoice.deliveryStatus, fontSize = 11.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("Articulo", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.TextSecondary, modifier = Modifier.weight(2f))
                        Text("Cant.", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.TextSecondary, modifier = Modifier.width(48.dp))
                        Text("Precio", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.TextSecondary, modifier = Modifier.width(64.dp))
                        Text("Total", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.TextSecondary, modifier = Modifier.width(72.dp))
                    }
                    Spacer(Modifier.height(2.dp))
                    invoice.items.forEach { item ->
                        val sf = if (invoice.surchargePercent > 0) 1.0 + invoice.surchargePercent / 100.0 else 1.0
                        val itemPrice = item.product.price * sf
                        val itemTotal = itemPrice * item.effectiveQuantity
                        Row(Modifier.fillMaxWidth()) {
                            Text(item.product.name, fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val qtyLabel = if (item.weightQuantity > 0) "${item.weightQuantity} lbs" else "x${item.quantity}"
                            Text(qtyLabel, fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.width(48.dp))
                            Text("RD\$ ${"%,.2f".format(itemPrice)}", fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.width(64.dp))
                            Text("RD\$ ${"%,.2f".format(itemTotal)}", fontSize = 12.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        Text("Subtotal:", fontSize = 12.sp, color = AppColors.TextSecondary)
                        Spacer(Modifier.width(8.dp))
                        Text("RD\$ ${"%.2f".format(invoice.subtotalPreTax)}", fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.width(80.dp))
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        Text("ITBIS:", fontSize = 12.sp, color = AppColors.TextSecondary)
                        Spacer(Modifier.width(8.dp))
                        Text("RD\$ ${"%.2f".format(invoice.taxAmount)}", fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.width(80.dp))
                    }
                    if (invoice.discountAmount > 0) {
                        Row(Modifier.fillMaxWidth()) {
                            Spacer(Modifier.weight(1f))
                            Text("Descuento:", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("-RD\$ ${"%.2f".format(invoice.discountAmount)}", fontSize = 12.sp, color = AppColors.Danger, modifier = Modifier.width(80.dp))
                        }
                    }
                    if (invoice.tipAmount > 0) {
                        Row(Modifier.fillMaxWidth()) {
                            Spacer(Modifier.weight(1f))
                            Text("Propina:", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("RD\$ ${"%.2f".format(invoice.tipAmount)}", fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.width(80.dp))
                        }
                    }
                    if (invoice.surchargeAmount > 0) {
                        Row(Modifier.fillMaxWidth()) {
                            Spacer(Modifier.weight(1f))
                            Text("Recargo (${"%.0f".format(invoice.surchargePercent)}%):", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("RD\$ ${"%.2f".format(invoice.surchargeAmount)}", fontSize = 12.sp, color = AppColors.Danger, modifier = Modifier.width(80.dp))
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        Text("Total:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("RD\$ ${"%.2f".format(invoice.total)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.Primary, modifier = Modifier.width(80.dp))
                    }
                    if (invoice.change > 0) {
                        Spacer(Modifier.height(2.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Spacer(Modifier.weight(1f))
                            Text("Recibido:", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("RD\$ ${"%.2f".format(invoice.receivedAmount)}", fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.width(80.dp))
                        }
                        Row(Modifier.fillMaxWidth()) {
                            Spacer(Modifier.weight(1f))
                            Text("Cambio:", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("RD\$ ${"%.2f".format(invoice.change)}", fontSize = 12.sp, color = Color(0xFF16A34A), modifier = Modifier.width(80.dp))
                        }
                    }
                    if (invoice.note.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text("Nota: ${invoice.note}", fontSize = 11.sp, color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}
