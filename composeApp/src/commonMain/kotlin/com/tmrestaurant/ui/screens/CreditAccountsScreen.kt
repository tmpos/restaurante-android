@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tmrestaurant.platform.eloPrintTicketCopies
import com.tmrestaurant.platform.localDateTimeLabel
import com.tmrestaurant.ui.components.ClientSelectorModal
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.launch

private val CreditBlue = Color(0xFF2563EB)
private val CreditGreen = Color(0xFF059669)
private val CreditAmber = Color(0xFFD97706)

@Composable
fun CreditAccountsScreen() {
    val canDeleteCreditEntries = AccessControl.canDeleteCreditEntries(TurnoManager.currentUser)
    val creditClients = ClientesManager.clientes.filter { it.tipo.equals("Credito", ignoreCase = true) }
    var search by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf<String?>(null) }
    var showClientSelector by remember { mutableStateOf(false) }

    val filtered = creditClients.filter {
        search.isBlank() || it.nombre.contains(search, true) ||
            it.telefono.contains(search) || it.rnc.contains(search, true)
    }
    val totalReceivable = creditClients.sumOf { CreditAccountsManager.balance(it.id) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF6F8FC)).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Cuentas por cobrar", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text("Planes de almuerzo y consumos a credito", fontSize = 13.sp, color = AppColors.TextSecondary)
            }
            SummaryPill("Clientes", creditClients.size.toString(), CreditBlue)
            Spacer(Modifier.width(10.dp))
            SummaryPill("Por cobrar", money(totalReceivable), CreditAmber)
            Spacer(Modifier.width(14.dp))
            Button(
                onClick = { showClientSelector = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CreditBlue),
                modifier = Modifier.height(46.dp)
            ) {
                Icon(Icons.Outlined.PersonAdd, null)
                Spacer(Modifier.width(7.dp))
                Text("Nueva cuenta", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(18.dp))
        SearchBox(search, "Buscar por cliente, telefono o documento...") { search = it }
        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            EmptyCreditState(onAdd = { showClientSelector = true })
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(260.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { client ->
                    CreditClientCard(client) { selectedClientId = client.id }
                }
            }
        }
    }

    selectedClientId?.let { id ->
        ClientesManager.clientes.find { it.id == id }?.let { client ->
            CreditAccountDetail(client) { selectedClientId = null }
        }
    }
    if (showClientSelector) {
        ClientSelectorModal(
            onDismiss = { showClientSelector = false },
            onSelect = { client ->
                val creditClient = if (client.tipo.equals("Credito", true)) client else client.copy(tipo = "Credito")
                if (creditClient != client) ClientesManager.update(creditClient)
                showClientSelector = false
                selectedClientId = creditClient.id
            }
        )
    }
}

@Composable
private fun CreditClientCard(client: Cliente, onClick: () -> Unit) {
    val orders = CreditAccountsManager.ordersFor(client.id)
    val balance = CreditAccountsManager.balance(client.id)
    val lastOrder = orders.firstOrNull()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.height(190.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(17.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(client.nombre.take(1).uppercase(), color = CreditBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(client.nombre, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(client.telefono.ifBlank { "Sin telefono" }, fontSize = 11.sp, color = AppColors.TextSecondary)
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniMetric("${orders.size}", "Pedidos", Color(0xFFEEF2FF), CreditBlue, Modifier.weight(1f))
                MiniMetric(money(balance), "Pendiente", Color(0xFFFFF7ED), CreditAmber, Modifier.weight(1.4f))
            }
            Text(
                lastOrder?.let { "Ultimo consumo: ${localDateTimeLabel(it.createdAt)}" } ?: "Sin consumos registrados",
                fontSize = 10.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun CreditAccountDetail(client: Cliente, onDismiss: () -> Unit) {
    val canDeleteCreditEntries = AccessControl.canDeleteCreditEntries(TurnoManager.currentUser)
    var showProducts by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var deleteOrderId by remember { mutableStateOf<String?>(null) }
    var deletePaymentId by remember { mutableStateOf<String?>(null) }
    val settings = LocalSettingsState.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val orders = CreditAccountsManager.ordersFor(client.id).filter {
        (fromDate.isBlank() || it.dateKey >= fromDate) && (toDate.isBlank() || it.dateKey <= toDate)
    }
    val payments = CreditAccountsManager.paymentsFor(client.id).filter {
        (fromDate.isBlank() || it.dateKey >= fromDate) && (toDate.isBlank() || it.dateKey <= toDate)
    }
    val balance = CreditAccountsManager.balance(client.id)
    val lunchCount = orders.sumOf { order -> order.items.sumOf { it.quantity } }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.92f).clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFF8FAFC))
            ) {
                Row(
                    Modifier.fillMaxWidth().background(Color.White).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.AccountBalanceWallet, null, tint = CreditBlue)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(client.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text("${client.telefono.ifBlank { "Sin telefono" }}  |  Limite: ${money(client.limiteCredito)}", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Button(onClick = { showProducts = true }, colors = ButtonDefaults.buttonColors(containerColor = CreditBlue), shape = RoundedCornerShape(11.dp)) {
                        Icon(Icons.Outlined.AddShoppingCart, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar almuerzo", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showPayment = true },
                        enabled = balance > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                        shape = RoundedCornerShape(11.dp)
                    ) {
                        Icon(Icons.Outlined.Payments, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Abonar / Pagar", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, null) }
                }

                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountMetric("Saldo pendiente", money(balance), CreditAmber, Modifier.weight(1f))
                    AccountMetric("Consumos", money(CreditAccountsManager.totalCharges(client.id)), CreditBlue, Modifier.weight(1f))
                    AccountMetric("Pagado", money(CreditAccountsManager.totalPayments(client.id)), CreditGreen, Modifier.weight(1f))
                    AccountMetric("Almuerzos filtrados", lunchCount.toString(), Color(0xFF7C3AED), Modifier.weight(1f))
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DateField("Desde (AAAA-MM-DD)", fromDate, Modifier.width(180.dp)) { fromDate = it }
                    DateField("Hasta (AAAA-MM-DD)", toDate, Modifier.width(180.dp)) { toDate = it }
                    OutlinedButton(onClick = { fromDate = ""; toDate = "" }, shape = RoundedCornerShape(10.dp)) {
                        Icon(Icons.Outlined.FilterAltOff, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Limpiar")
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            val text = buildCreditStatement(client, orders, payments, balance, fromDate, toDate)
                            scope.launch {
                                val result = eloPrintTicketCopies(
                                    settings.settings.print.selectedPrinter,
                                    text,
                                    settings.settings.print.paperWidthMm,
                                    settings.settings.print.copies
                                )
                                snackbar.showSnackbar(result.message)
                            }
                        },
                        enabled = orders.isNotEmpty() || payments.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.Print, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Imprimir estado", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HistoryPanel(
                        title = "Pedidos (${orders.size})",
                        emptyText = "No hay pedidos en este rango",
                        isEmpty = orders.isEmpty(),
                        modifier = Modifier.weight(1.35f)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            OrderHistoryCard(order, canDeleteCreditEntries) { deleteOrderId = order.id }
                        }
                    }
                    HistoryPanel(
                        title = "Pagos y abonos (${payments.size})",
                        emptyText = "No hay pagos en este rango",
                        isEmpty = payments.isEmpty(),
                        modifier = Modifier.weight(0.85f)
                    ) {
                        items(payments, key = { it.id }) { payment ->
                            PaymentHistoryCard(payment, canDeleteCreditEntries) { deletePaymentId = payment.id }
                        }
                    }
                }
            }
            SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp))
        }
    }

    if (showProducts) AddCreditOrderDialog(client, onDismiss = { showProducts = false }) { items, note ->
        CreditAccountsManager.addOrder(client.id, items, note)
        showProducts = false
    }
    if (showPayment) PaymentDialog(client, balance, onDismiss = { showPayment = false }) { amount, method, note ->
        CreditAccountsManager.addPayment(client.id, amount, method, note)
        showPayment = false
    }
    deleteOrderId?.let { id ->
        ConfirmDeleteDialog(
            title = "Eliminar pedido",
            message = "El saldo del cliente sera recalculado.",
            onConfirm = { CreditAccountsManager.deleteOrder(id); deleteOrderId = null },
            onDismiss = { deleteOrderId = null }
        )
    }
    deletePaymentId?.let { id ->
        ConfirmDeleteDialog(
            title = "Eliminar abono",
            message = "El saldo pendiente aumentara nuevamente.",
            onConfirm = { CreditAccountsManager.deletePayment(id); deletePaymentId = null },
            onDismiss = { deletePaymentId = null }
        )
    }
}

@Composable
private fun AddCreditOrderDialog(
    client: Cliente,
    onDismiss: () -> Unit,
    onSave: (List<CreditOrderItem>, String) -> Unit
) {
    val products = LocalProductState.current.getPosProducts()
    var search by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val quantities = remember { mutableStateMapOf<Int, Int>() }
    val filtered = products.filter { search.isBlank() || it.name.contains(search, true) || it.code.contains(search, true) }
    val total = products.sumOf { it.price * (quantities[it.id] ?: 0) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.width(760.dp).heightIn(max = 700.dp).clip(RoundedCornerShape(22.dp)).background(Color.White).padding(20.dp)
        ) {
            DialogHeader("Nuevo almuerzo", client.nombre, Icons.Outlined.LunchDining, onDismiss)
            Spacer(Modifier.height(12.dp))
            SearchBox(search, "Buscar productos...") { search = it }
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(170.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it.id }) { product ->
                    val quantity = quantities[product.id] ?: 0
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (quantity > 0) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (quantity > 0) CreditBlue else Color(0xFFE2E8F0))
                    ) {
                        Column(Modifier.padding(11.dp)) {
                            Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(money(product.price), color = CreditGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                RoundAction(Icons.Outlined.Remove, enabled = quantity > 0) {
                                    if (quantity <= 1) quantities.remove(product.id) else quantities[product.id] = quantity - 1
                                }
                                Text(quantity.toString(), fontWeight = FontWeight.Bold)
                                RoundAction(Icons.Outlined.Add) { quantities[product.id] = quantity + 1 }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            InputBox(note, "Nota del pedido: sin cebolla, entregar a las 12...") { note = it }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${quantities.values.sum()} productos", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Text("Total: ${money(total)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CreditBlue)
                }
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSave(products.mapNotNull { product ->
                            val qty = quantities[product.id] ?: 0
                            if (qty > 0) CreditOrderItem(product.id, product.name, product.price, qty) else null
                        }, note)
                    },
                    enabled = quantities.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = CreditBlue),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Guardar a credito", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun PaymentDialog(
    client: Cliente,
    balance: Double,
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("EFECTIVO") }
    var note by remember { mutableStateOf("") }
    val value = amount.toDoubleOrNull() ?: 0.0
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.width(460.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(22.dp)) {
            DialogHeader("Registrar pago", client.nombre, Icons.Outlined.Payments, onDismiss)
            Spacer(Modifier.height(16.dp))
            Text("Saldo pendiente", color = AppColors.TextSecondary, fontSize = 12.sp)
            Text(money(balance), color = CreditAmber, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            InputBox(amount, "Monto a abonar") { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("EFECTIVO", "TRANSFERENCIA", "TARJETA").forEach { option ->
                    FilterChip(
                        selected = method == option,
                        onClick = { method = option },
                        label = { Text(option, fontSize = 10.sp) }
                    )
                }
            }
            InputBox(note, "Referencia o nota del pago") { note = it }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { amount = balance.toString() }, modifier = Modifier.weight(1f)) { Text("Pagar saldo") }
                Button(
                    onClick = { onSave(value, method, note) },
                    enabled = value > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                    modifier = Modifier.weight(1f)
                ) { Text("Registrar", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(order: CreditOrder, canDelete: Boolean, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(localDateTimeLabel(order.createdAt), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("${order.items.sumOf { it.quantity }} articulos", color = AppColors.TextSecondary, fontSize = 10.sp)
                }
                Text(money(order.total), color = CreditBlue, fontWeight = FontWeight.Bold)
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, null, tint = Color(0xFFDC2626), modifier = Modifier.size(17.dp))
                    }
                }
            }
            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("${item.quantity} x ${item.name}", fontSize = 11.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text(money(item.total), fontSize = 11.sp, color = AppColors.TextSecondary)
                }
                if (item.note.isNotBlank()) Text("Nota: ${item.note}", fontSize = 9.sp, color = CreditAmber)
            }
            if (order.note.isNotBlank()) {
                Text(order.note, fontSize = 10.sp, color = CreditAmber, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun PaymentHistoryCard(payment: CreditPayment, canDelete: Boolean, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CheckCircle, null, tint = CreditGreen)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(money(payment.amount), color = CreditGreen, fontWeight = FontWeight.Bold)
                Text("${payment.method} | ${localDateTimeLabel(payment.createdAt)}", fontSize = 9.sp, color = AppColors.TextSecondary)
                if (payment.note.isNotBlank()) Text(payment.note, fontSize = 9.sp, color = AppColors.TextSecondary)
            }
            if (canDelete) {
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Outlined.Delete, null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun buildCreditStatement(
    client: Cliente,
    orders: List<CreditOrder>,
    payments: List<CreditPayment>,
    currentBalance: Double,
    from: String,
    to: String
): String = buildString {
    appendLine("      ESTADO DE CUENTA")
    appendLine(client.nombre.uppercase().take(32))
    if (client.telefono.isNotBlank()) appendLine("Tel: ${client.telefono}")
    appendLine("Rango: ${from.ifBlank { "Inicio" }} a ${to.ifBlank { "Hoy" }}")
    appendLine("-".repeat(40))
    orders.forEach { order ->
        appendLine(localDateTimeLabel(order.createdAt))
        order.items.forEach { item ->
            val left = "${item.quantity}x ${item.name}".take(25)
            appendLine(left.padEnd(27) + money(item.total).takeLast(13))
            if (item.note.isNotBlank()) appendLine("  Nota: ${item.note.take(30)}")
        }
        if (order.note.isNotBlank()) appendLine("Nota: ${order.note.take(32)}")
        appendLine("Pedido: ${money(order.total)}")
        appendLine()
    }
    if (payments.isNotEmpty()) {
        appendLine("-".repeat(40))
        appendLine("PAGOS / ABONOS")
        payments.forEach { appendLine("${it.dateKey} ${it.method.take(12).padEnd(12)} ${money(it.amount)}") }
    }
    appendLine("-".repeat(40))
    appendLine("Almuerzos: ${orders.sumOf { o -> o.items.sumOf { it.quantity } }}")
    appendLine("Consumos rango: ${money(orders.sumOf { it.total })}")
    appendLine("Pagos rango: ${money(payments.sumOf { it.amount })}")
    appendLine("SALDO ACTUAL: ${money(currentBalance)}")
    appendLine("-".repeat(40))
}

@Composable
private fun NewCreditClientDialog(onDismiss: () -> Unit, onSave: (Cliente) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var document by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.width(460.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(22.dp)) {
            DialogHeader("Nueva cuenta de credito", "Cliente suscrito", Icons.Outlined.PersonAdd, onDismiss)
            Spacer(Modifier.height(14.dp))
            InputBox(name, "Nombre completo") { name = it }
            Spacer(Modifier.height(9.dp))
            InputBox(phone, "Telefono") { phone = it }
            Spacer(Modifier.height(9.dp))
            InputBox(document, "Cedula / RNC") { document = it }
            Spacer(Modifier.height(9.dp))
            InputBox(limit, "Limite de credito (opcional)") { limit = it.filter { ch -> ch.isDigit() || ch == '.' } }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSave(Cliente(nombre = name.trim(), telefono = phone.trim(), rnc = document.trim(), tipo = "Credito", limiteCredito = limit.toDoubleOrNull() ?: 0.0))
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CreditBlue),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) { Text("Crear cuenta", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Eliminar", color = Color(0xFFDC2626)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun HistoryPanel(
    title: String,
    emptyText: String,
    isEmpty: Boolean,
    modifier: Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Column(modifier.fillMaxHeight().clip(RoundedCornerShape(14.dp)).background(Color.White).padding(12.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            if (isEmpty) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(emptyText, color = AppColors.TextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                content()
            }
        }
    }
}

@Composable
private fun DialogHeader(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onDismiss: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = CreditBlue)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, fontSize = 11.sp, color = AppColors.TextSecondary)
        }
        IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, null) }
    }
}

@Composable
private fun SearchBox(value: String, hint: String, onChange: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)).padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, null, tint = AppColors.Gray, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isBlank()) Text(hint, color = AppColors.Gray, fontSize = 12.sp)
            BasicTextField(value, onChange, textStyle = TextStyle(fontSize = 13.sp, color = AppColors.TextPrimary), modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
    }
}

@Composable
private fun InputBox(value: String, hint: String, onChange: (String) -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isBlank()) Text(hint, color = AppColors.Gray, fontSize = 12.sp)
        BasicTextField(value, onChange, textStyle = TextStyle(fontSize = 13.sp, color = AppColors.TextPrimary), modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}

@Composable
private fun DateField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
    Column(modifier) {
        Text(label, fontSize = 9.sp, color = AppColors.TextSecondary)
        InputBox(value, "AAAA-MM-DD") { onChange(it.take(10)) }
    }
}

@Composable
private fun SummaryPill(label: String, value: String, color: Color) {
    Column(
        Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 15.dp, vertical = 7.dp)
    ) {
        Text(label, fontSize = 9.sp, color = AppColors.TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun AccountMetric(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(13.dp)).background(Color.White).padding(13.dp)) {
        Text(label, fontSize = 10.sp, color = AppColors.TextSecondary)
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun MiniMetric(value: String, label: String, background: Color, color: Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(background).padding(9.dp)) {
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp, maxLines = 1)
        Text(label, color = AppColors.TextSecondary, fontSize = 9.sp)
    }
}

@Composable
private fun RoundAction(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier.size(28.dp).clip(CircleShape).background(if (enabled) Color.White else Color(0xFFE2E8F0))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = if (enabled) CreditBlue else AppColors.Gray)
    }
}

@Composable
private fun EmptyCreditState(onAdd: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.AccountBalanceWallet, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(70.dp))
            Text("No hay cuentas de credito", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("Cree una cuenta para comenzar a registrar almuerzos.", color = AppColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = CreditBlue)) { Text("Crear primera cuenta") }
        }
    }
}

private fun money(value: Double): String = "RD$ ${"%,.2f".format(value)}"
