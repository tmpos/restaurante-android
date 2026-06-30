package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.eloPrintTestTicket
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.Cliente
import com.tmrestaurant.ui.data.ClientesManager
import com.tmrestaurant.ui.data.HeldOrder
import com.tmrestaurant.ui.data.HoldOrderManager
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.Product
import com.tmrestaurant.ui.data.Quote
import com.tmrestaurant.ui.data.QuoteItem
import com.tmrestaurant.ui.data.QuoteManager
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import com.tmrestaurant.ui.util.formatCurrency

@Composable
fun CotizacionesScreen() {
    val canDeleteQuotes = AccessControl.canDeleteQuotes(TurnoManager.currentUser)
    val canFinalizeQuotes = AccessControl.canFinalizeQuotes(TurnoManager.currentUser)
    val quotes = QuoteManager.quotes
    val productState = LocalProductState.current
    val settingsState = LocalSettingsState.current
    val settings = settingsState.settings
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("TODAS") }
    var showForm by remember { mutableStateOf(false) }
    var editingQuote by remember { mutableStateOf<Quote?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val filtered = remember(quotes, searchQuery, statusFilter) {
        val byStatus = if (statusFilter == "TODAS") quotes else quotes.filter { it.status == statusFilter }
        if (searchQuery.isBlank()) byStatus else byStatus.filter {
            it.id.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.customerPhone.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Cotizaciones", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppColors.TextPrimary)
                Text("Presupuestos por cliente con conversion al POS", color = AppColors.TextSecondary, fontSize = 13.sp)
            }
            Button(
                onClick = { editingQuote = null; showForm = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Nueva", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Surface).padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Search, null, tint = AppColors.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    if (searchQuery.isEmpty()) Text("Buscar por numero o cliente...", color = AppColors.Gray, fontSize = 13.sp)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp),
                        modifier = Modifier.fillMaxSize(),
                        singleLine = true
                    )
                }
            }
            listOf("TODAS", "BORRADOR", "ENVIADA", "ACEPTADA", "RECHAZADA").forEach { filter ->
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (statusFilter == filter) AppColors.Primary else AppColors.Background)
                        .clickable { statusFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(filter, color = if (statusFilter == filter) Color.White else AppColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (!feedback.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFDCFCE7)).padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(feedback!!, color = Color(0xFF166534), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Description, null, tint = AppColors.Gray, modifier = Modifier.size(60.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay cotizaciones", color = AppColors.TextSecondary, fontSize = 16.sp)
                    Text("Cree una cotizacion para comenzar", color = AppColors.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { quote ->
                    QuoteCard(
                        quote = quote,
                        onEdit = { editingQuote = quote; showForm = true },
                        onDelete = { QuoteManager.delete(quote.id); feedback = "Cotizacion eliminada" },
                        onDuplicate = { QuoteManager.duplicate(quote.id); feedback = "Cotizacion duplicada" },
                        onStatusChange = { status ->
                            QuoteManager.updateStatus(quote.id, status)
                            feedback = "Estado actualizado a $status"
                        },
                        onConvertToPos = {
                            HoldOrderManager.hold(
                                HeldOrder(
                                    id = "QUOTE-${quote.id}",
                                    label = "Cotizacion ${quote.id} - ${quote.customerName.ifBlank { "Cliente" }}",
                                    items = quote.items.map { item ->
                                        val product = productState.getById(item.productId) ?: Product(
                                            id = item.productId,
                                            name = item.productName,
                                            price = item.unitPrice,
                                            taxPercent = item.taxPercent
                                        )
                                        com.tmrestaurant.ui.data.CartItem(
                                            product = product.copy(price = item.unitPrice),
                                            quantity = item.quantity
                                        )
                                    },
                                    clientId = quote.customerId,
                                    clientName = quote.customerName
                                )
                            )
                            QuoteManager.updateStatus(quote.id, "ACEPTADA")
                            feedback = "Cotizacion enviada al POS como orden pausada"
                        },
                        onPrint = {
                            val result = kotlinx.coroutines.runBlocking {
                                eloPrintTestTicket(
                                    settings.print.selectedPrinter,
                                    buildQuoteReceipt(quote),
                                    settings.print.paperWidthMm,
                                    com.tmrestaurant.platform.TicketPrintStyle(
                                        textSize = settings.print.textSize,
                                        logoWidthMm = settings.print.logoWidthMm,
                                        logoHeightMm = settings.print.logoHeightMm,
                                        showLogo = settings.print.showCompanyLogo,
                                        logoBytes = settingsState.getLogoBytes()
                                    )
                                )
                            }
                            feedback = if (result.success) "Cotizacion enviada a imprimir" else result.message
                        },
                        canDelete = canDeleteQuotes,
                        canFinalize = canFinalizeQuotes
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showForm) {
        QuoteFormModal(
            quote = editingQuote,
            clients = ClientesManager.clientes,
            products = productState.products.filter { it.active },
            onDismiss = { showForm = false; editingQuote = null },
            onSave = { quote ->
                QuoteManager.addOrUpdate(quote)
                showForm = false
                editingQuote = null
                feedback = "Cotizacion guardada"
            }
        )
    }
}

@Composable
private fun QuoteCard(
    quote: Quote,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onStatusChange: (String) -> Unit,
    onConvertToPos: () -> Unit,
    onPrint: () -> Unit,
    canDelete: Boolean,
    canFinalize: Boolean
) {
    val statusColor = when (quote.status) {
        "BORRADOR" -> Color(0xFF64748B)
        "ENVIADA" -> Color(0xFFD97706)
        "ACEPTADA" -> Color(0xFF059669)
        "RECHAZADA" -> Color(0xFFDC2626)
        else -> AppColors.TextSecondary
    }
    val statusBg = statusColor.copy(alpha = 0.12f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Description, null, tint = statusColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(quote.id, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                    Text(
                        "${quote.customerName.ifBlank { "Sin cliente" }} - ${quote.items.size} item(s) - ${formatCurrency(quote.total)}",
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(statusBg).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(quote.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Vence: ${quote.validUntil.ifBlank { "-" }}", color = AppColors.TextSecondary, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Text(formatDateTime(quote.createdAt), color = AppColors.TextSecondary, fontSize = 11.sp)
            }

            if (quote.notes.isNotBlank()) {
                Text(quote.notes, color = AppColors.TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                SmallAction("Editar", Icons.Outlined.Edit, AppColors.Primary, onEdit)
                SmallAction("Duplicar", Icons.Outlined.ContentPaste, Color(0xFF2563EB), onDuplicate)
                SmallAction("Imprimir", Icons.Outlined.Print, Color(0xFF7C3AED), onPrint)
                Spacer(Modifier.weight(1f))
                if (canFinalize && quote.status != "RECHAZADA") {
                    SmallStatusButton("Enviada", quote.status == "ENVIADA") { onStatusChange("ENVIADA") }
                    SmallStatusButton("Aceptada", quote.status == "ACEPTADA") { onStatusChange("ACEPTADA") }
                    SmallStatusButton("Rechazada", quote.status == "RECHAZADA") { onStatusChange("RECHAZADA") }
                }
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFD1FAE5)).clickable(onClick = onConvertToPos).padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.TaskAlt, null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Pasar a POS", color = Color(0xFF059669), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (canDelete) {
                    Box(
                        Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2)).clickable(onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SmallStatusButton(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(if (active) AppColors.Primary else AppColors.Background)
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = if (active) Color.White else AppColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuoteFormModal(
    quote: Quote?,
    clients: List<Cliente>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSave: (Quote) -> Unit
) {
    var selectedClientId by remember(quote) { mutableStateOf(quote?.customerId ?: "") }
    var validUntil by remember(quote) { mutableStateOf(quote?.validUntil ?: "") }
    var notes by remember(quote) { mutableStateOf(quote?.notes ?: "") }
    var status by remember(quote) { mutableStateOf(quote?.status ?: "BORRADOR") }
    val quoteItems = remember(quote) { mutableStateListOf<QuoteItem>().apply { addAll(quote?.items ?: emptyList()) } }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var unitPriceText by remember { mutableStateOf("0") }
    var productSearch by remember { mutableStateOf("") }
    var showClientPicker by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }

    val selectedClient = clients.firstOrNull { it.id == selectedClientId }
    val filteredProducts = remember(products, productSearch) {
        if (productSearch.isBlank()) products.take(40)
        else products.filter {
            it.name.contains(productSearch, ignoreCase = true) ||
                it.code.contains(productSearch, ignoreCase = true) ||
                it.barcode.contains(productSearch, ignoreCase = true)
        }.take(60)
    }
    val selectedProduct = products.firstOrNull { it.id == selectedProductId }
    val subtotal = quoteItems.sumOf { it.subtotal }
    val tax = quoteItems.sumOf { it.taxAmount }
    val total = quoteItems.sumOf { it.subtotal }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(960.dp).heightIn(max = 720.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (quote == null) "Nueva Cotizacion" else "Editar Cotizacion",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(0.42f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerField("Cliente", selectedClient?.nombre ?: "Seleccionar cliente") { showClientPicker = true }
                    FormField("Valida hasta", validUntil, "AAAA-MM-DD") { validUntil = it }
                    FormField("Notas", notes, "Condiciones o comentarios", minHeight = 92.dp) { notes = it }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Estado", color = AppColors.TextSecondary, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BORRADOR", "ENVIADA", "ACEPTADA", "RECHAZADA").forEach { option ->
                                Box(
                                    Modifier.clip(RoundedCornerShape(8.dp)).background(if (status == option) AppColors.PrimaryLight else AppColors.Background)
                                        .clickable { status = option }.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(option, color = if (status == option) AppColors.Primary else AppColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))

                    Text("Agregar producto", color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    PickerField("Producto", selectedProduct?.name ?: "Seleccionar producto") { showProductPicker = true }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        FormField("Cantidad", quantityText, "1", Modifier.weight(1f)) { quantityText = it.filter { ch -> ch.isDigit() } }
                        FormField("Precio", unitPriceText, "0.00", Modifier.weight(1f)) { unitPriceText = it.filter { ch -> ch.isDigit() || ch == '.' } }
                    }
                    Button(
                        onClick = {
                            val product = selectedProduct ?: return@Button
                            val qty = quantityText.toIntOrNull() ?: 0
                            val price = unitPriceText.toDoubleOrNull() ?: product.price
                            if (qty <= 0) return@Button
                            quoteItems.add(
                                QuoteItem(
                                    productId = product.id,
                                    productName = product.name,
                                    unitPrice = price,
                                    quantity = qty,
                                    taxPercent = product.taxPercent
                                )
                            )
                            quantityText = "1"
                            unitPriceText = product.price.toString()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        enabled = selectedProduct != null
                    ) {
                        Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar item", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    Modifier.weight(0.58f).fillMaxSize().clip(RoundedCornerShape(16.dp)).background(AppColors.Background).padding(16.dp)
                ) {
                    Text("Items de la cotizacion", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(10.dp))
                    if (quoteItems.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No hay productos agregados", color = AppColors.TextSecondary, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quoteItems.size) { index ->
                                val item = quoteItems[index]
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AppColors.Surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(item.productName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                                            Text("${item.quantity} x ${formatCurrency(item.unitPrice)}", color = AppColors.TextSecondary, fontSize = 11.sp)
                                        }
                                        Text(formatCurrency(item.subtotal), color = AppColors.Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2)).clickable { quoteItems.removeAt(index) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
                    Spacer(Modifier.height(12.dp))
                    SummaryRow("Subtotal", formatCurrency(subtotal))
                    SummaryRow("ITBIS", formatCurrency(tax))
                    SummaryRow("Total", formatCurrency(total), true)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Background),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        if (quoteItems.isEmpty()) return@Button
                        val resolvedClient = selectedClient
                        onSave(
                            Quote(
                                id = quote?.id ?: "COT-${System.currentTimeMillis().toString().takeLast(8)}",
                                customerId = resolvedClient?.id ?: "",
                                customerName = resolvedClient?.nombre ?: "",
                                customerEmail = resolvedClient?.email ?: "",
                                customerPhone = resolvedClient?.telefono ?: "",
                                validUntil = validUntil,
                                notes = notes,
                                status = status,
                                items = quoteItems.toList(),
                                createdAt = quote?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    modifier = Modifier.weight(1f).height(46.dp),
                    enabled = quoteItems.isNotEmpty()
                ) {
                    Text("Guardar cotizacion", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showClientPicker) {
        SelectionModal(
            title = "Seleccionar cliente",
            onDismiss = { showClientPicker = false }
        ) {
            clients.forEach { client ->
                SelectionRow(
                    title = client.nombre,
                    subtitle = listOf(client.telefono, client.email).filter { it.isNotBlank() }.joinToString(" - ")
                ) {
                    selectedClientId = client.id
                    showClientPicker = false
                }
            }
        }
    }

    if (showProductPicker) {
        SelectionModal(
            title = "Seleccionar producto",
            showSearch = true,
            searchQuery = productSearch,
            onSearchChange = { productSearch = it },
            onDismiss = { showProductPicker = false }
        ) {
            filteredProducts.forEach { product ->
                SelectionRow(
                    title = product.name,
                    subtitle = "${product.code.ifBlank { "Sin codigo" }} - ${formatCurrency(product.price)}"
                ) {
                    selectedProductId = product.id
                    unitPriceText = product.price.toString()
                    showProductPicker = false
                }
            }
        }
    }
}

@Composable
private fun PickerField(label: String, value: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Box(
            Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(value, color = if (value.startsWith("Seleccionar")) AppColors.Gray else AppColors.TextPrimary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier.fillMaxWidth(),
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
    onChange: (String) -> Unit
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Box(
            Modifier.fillMaxWidth().heightIn(min = minHeight).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            if (value.isBlank()) {
                Text(placeholder, color = AppColors.Gray, fontSize = 13.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SelectionModal(
    title: String,
    showSearch: Boolean = false,
    searchQuery: String = "",
    onSearchChange: (String) -> Unit = {},
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(560.dp).heightIn(max = 620.dp).clip(RoundedCornerShape(18.dp)).background(AppColors.Surface).padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
            if (showSearch) {
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isBlank()) Text("Buscar...", color = AppColors.Gray, fontSize = 13.sp)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
    }
}

@Composable
private fun SelectionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background).clickable(onClick = onClick).padding(12.dp)
    ) {
        Column {
            Text(title, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = AppColors.TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = if (strong) AppColors.TextPrimary else AppColors.TextSecondary, fontSize = if (strong) 15.sp else 13.sp, fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(value, color = if (strong) AppColors.Primary else AppColors.TextPrimary, fontSize = if (strong) 16.sp else 13.sp, fontWeight = if (strong) FontWeight.Bold else FontWeight.SemiBold)
    }
}

private fun buildQuoteReceipt(quote: Quote): String = buildString {
    appendLine("      COTIZACION")
    appendLine("==============================")
    appendLine("Numero: ${quote.id}")
    appendLine("Cliente: ${quote.customerName.ifBlank { "General" }}")
    appendLine("Telefono: ${quote.customerPhone.ifBlank { "-" }}")
    appendLine("Vigencia: ${quote.validUntil.ifBlank { "-" }}")
    appendLine("Estado: ${quote.status}")
    appendLine("------------------------------")
    quote.items.forEach { item ->
        appendLine(item.productName)
        appendLine("${item.quantity} x ${formatCurrency(item.unitPrice)} = ${formatCurrency(item.subtotal)}")
    }
    appendLine("------------------------------")
    appendLine("Subtotal: ${formatCurrency(quote.subtotal)}")
    appendLine("ITBIS: ${formatCurrency(quote.taxAmount)}")
    appendLine("Total: ${formatCurrency(quote.total)}")
    if (quote.notes.isNotBlank()) {
        appendLine("------------------------------")
        appendLine("Notas: ${quote.notes}")
    }
}
