package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.tmrestaurant.platform.sendEmail
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.InvoiceHistory
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
fun InvoiceDrawer(
    visible: Boolean,
    onDismiss: () -> Unit,
    onReprint: (com.tmrestaurant.ui.components.PaymentResult) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val settingsState = LocalSettingsState.current
    var searchQuery by remember { mutableStateOf("") }

    var showOtpDialog by remember { mutableStateOf(false) }
    var currentOtp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var sendingOtp by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var invoiceToCancel by remember { mutableStateOf<PaymentResult?>(null) }
    var invoiceToReturn by remember { mutableStateOf<PaymentResult?>(null) }
    val productState = LocalProductState.current
    val canManageInvoices = AccessControl.canManageInvoices(TurnoManager.currentUser)

    val filteredInvoices = remember(searchQuery, InvoiceHistory.invoices.toList()) {
        if (searchQuery.isBlank()) InvoiceHistory.invoices.toList()
        else InvoiceHistory.invoices.filter { invoice ->
            invoice.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
            invoice.ncf.contains(searchQuery, ignoreCase = true) ||
            invoice.paymentMethod.contains(searchQuery, ignoreCase = true) ||
            invoice.customerName.contains(searchQuery, ignoreCase = true)
        }
    }

    fun sendOtpFor(invoice: PaymentResult) {
        invoiceToCancel = invoice
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
            val result = withContext(Dispatchers.IO) {
                sendEmail(
                    config = config, fromName = smtp.senderName, fromAddr = smtp.senderEmail,
                    to = companyEmail,
                    subject = "OTP - Anular Factura ${invoice.invoiceNumber}",
                    body = "Se solicita anular la factura: ${invoice.invoiceNumber}\nTotal: RD\$ ${"%,.2f".format(invoice.total)}\n\nCódigo OTP: $currentOtp\n\nSi no solicitó esta operación, ignore este mensaje."
                )
            }
            sendingOtp = false
            if (!result.success) otpError = "Error enviando OTP: ${result.error}"
        }
    }

    if (!visible) return
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(onClick = onDismiss))
        Box(
            Modifier.width(380.dp).fillMaxHeight().align(Alignment.CenterEnd)
                .background(AppColors.Surface).padding(16.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Receipt, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Historial de Facturas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar factura, NCF, metodo o cliente...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(Icons.Outlined.Clear, null, tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp).clickable { searchQuery = "" })
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = AppColors.TextPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                if (filteredInvoices.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Receipt, null, tint = AppColors.Border, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "Sin resultados" else "No hay facturas",
                                color = AppColors.TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredInvoices, key = { it.hashCode() }) { invoice ->
                            InvoiceCard(invoice,
                                onReprint = { onReprint(invoice) },
                                onCancel = { if (canManageInvoices) sendOtpFor(invoice) },
                                onReturn = { if (canManageInvoices) invoiceToReturn = invoice }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showOtpDialog) {
        CancelOtpDialog(
            invoice = invoiceToCancel,
            sending = sendingOtp,
            error = otpError,
            enteredOtp = enteredOtp,
            onOtpChange = { enteredOtp = it },
            onConfirm = {
                if (enteredOtp == currentOtp) {
                    invoiceToCancel?.let { inv ->
                        val idx = InvoiceHistory.invoices.indexOf(inv)
                        if (idx >= 0) InvoiceHistory.cancelInvoice(idx, productState)
                    }
                    showOtpDialog = false
                    invoiceToCancel = null
                } else {
                    otpError = "Codigo incorrecto"
                }
            },
            onDismiss = {
                showOtpDialog = false
                invoiceToCancel = null
            }
        )
    }

    invoiceToReturn?.let { inv ->
        val idx = InvoiceHistory.invoices.indexOf(inv)
        if (idx >= 0) {
            ReturnModal(
                invoiceNumber = inv.invoiceNumber,
                items = inv.items,
                onConfirm = { returned ->
                    if (!canManageInvoices) return@ReturnModal
                    com.tmrestaurant.ui.data.RecipeInventoryManager.revertReturn(inv.items, returned, productState)
                    InvoiceHistory.processReturn(idx, returned)
                    invoiceToReturn = null
                },
                onDismiss = { invoiceToReturn = null }
            )
        }
    }
}

@Composable
private fun InvoiceCard(invoice: com.tmrestaurant.ui.components.PaymentResult, onReprint: () -> Unit, onCancel: () -> Unit, onReturn: () -> Unit = {}) {
    val isCancelled = invoice.status == "ANULADA"
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (isCancelled) Color(0xFFFEF2F2) else AppColors.Background)
            .border(1.dp, if (isCancelled) Color(0xFFFECACA) else AppColors.Border, RoundedCornerShape(10.dp)).padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isCancelled) AppColors.TextSecondary else AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(if (isCancelled) Color(0xFFFEE2E2) else AppColors.PrimaryLight).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(invoice.paymentMethod, color = if (isCancelled) AppColors.Danger else AppColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (isCancelled) {
                    Spacer(Modifier.width(4.dp))
                    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFFEE2E2)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("ANULADA", color = AppColors.Danger, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("RD\$ ${"%,.2f".format(invoice.total)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isCancelled) AppColors.TextSecondary else AppColors.TextPrimary)
                Spacer(Modifier.weight(1f))
                Text(invoice.ncf.ifBlank { "N/A" }, color = AppColors.TextSecondary, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(invoice.customerName.ifBlank { "Consumidor final" }, color = AppColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${invoice.items.size} articulos", color = AppColors.TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                if (!isCancelled) {
                    Box(
                        Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFFEE2E2))
                            .clickable(onClick = onCancel).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Block, null, tint = AppColors.Danger, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Anular", color = AppColors.Danger, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    if (invoice.status == "ACTIVA") {
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFFEF3C7))
                                .clickable(onClick = onReturn).padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Receipt, null, tint = Color(0xFFD97706), modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Devolver", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                }
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFEFF6FF))
                        .clickable(onClick = onReprint).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Print, null, tint = AppColors.Primary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reimprimir", color = AppColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CancelOtpDialog(
    invoice: PaymentResult?,
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
                Icon(Icons.Outlined.Block, null, tint = AppColors.Danger, modifier = Modifier.size(40.dp))
                Text("Anular Factura", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary)
                Text(invoice?.invoiceNumber ?: "", fontSize = 16.sp, color = AppColors.TextSecondary)
                if (sending) {
                    Text("Enviando codigo OTP al correo...", fontSize = 15.sp, color = AppColors.TextSecondary)
                } else {
                    Text("Ingrese el codigo OTP enviado al correo", fontSize = 15.sp, color = AppColors.TextSecondary)
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
                            Text("Anular", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
