package com.tmrestaurant.ui.screens.compras

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.screens.PlaceholderScreen
import com.tmrestaurant.ui.theme.AppColors
import com.tmrestaurant.ui.util.formatCurrency

@Composable
fun OrdenesCompraScreen() {
    if (!AccessControl.canManagePurchases(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Ordenes de Compra",
            subtitle = "Solo los administradores pueden gestionar ordenes de compra."
        )
        return
    }
    val productState = LocalProductState.current
    val orders = PurchaseOrderManager.orders
    var showForm by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<PurchaseOrder?>(null) }
    var filterStatus by remember { mutableStateOf("TODAS") }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(orders, filterStatus, searchQuery) {
        val byStatus = when (filterStatus) {
            "TODAS" -> orders
            else -> orders.filter { it.status == filterStatus }
        }
        if (searchQuery.isBlank()) byStatus
        else byStatus.filter { it.providerName.contains(searchQuery, ignoreCase = true) }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Ordenes de Compra", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppColors.TextPrimary)
                    Text("Gestione las ordenes de compra a proveedores", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
                Button(
                    onClick = { editingOrder = null; showForm = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Nueva Orden", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Surface).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Search, null, tint = AppColors.Gray, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(value = searchQuery, onValueChange = { searchQuery = it }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                    }
                }
                Spacer(Modifier.width(8.dp))
                listOf("TODAS", "PENDIENTE", "RECIBIDA", "CANCELADA").forEach { f ->
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(if (filterStatus == f) AppColors.Primary else AppColors.Background)
                            .clickable { filterStatus = f }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(f, color = if (filterStatus == f) Color.White else AppColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ShoppingCart, null, tint = AppColors.Gray, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No hay ordenes de compra", color = AppColors.TextSecondary, fontSize = 16.sp)
                        Text("Cree una nueva orden para comenzar", color = AppColors.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { order ->
                        OrdenCompraCard(
                            order = order,
                            onEdit = { editingOrder = order; showForm = true },
                            onDelete = { PurchaseOrderManager.remove(order.id) },
                            onRecibida = {
                                if (order.status == "PENDIENTE") {
                                    PurchaseOrderManager.markReceived(order.id, productState)
                                }
                            },
                            onCancelar = {
                                PurchaseOrderManager.cancelOrder(order.id, productState)
                            }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showForm) {
        OrdenCompraFormModal(
            order = editingOrder,
            products = productState.products,
            onDismiss = { showForm = false; editingOrder = null },
            onSave = { o ->
                PurchaseOrderManager.addOrUpdate(o)
                showForm = false; editingOrder = null
            }
        )
    }
}

@Composable
private fun OrdenCompraCard(
    order: PurchaseOrder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRecibida: () -> Unit,
    onCancelar: () -> Unit
) {
    val statusColor = when (order.status) {
        "PENDIENTE" -> Color(0xFFD97706)
        "RECIBIDA" -> Color(0xFF059669)
        "CANCELADA" -> Color(0xFFEF4444)
        else -> AppColors.TextSecondary
    }
    val statusBg = when (order.status) {
        "PENDIENTE" -> Color(0xFFFEF3C7)
        "RECIBIDA" -> Color(0xFFD1FAE5)
        "CANCELADA" -> Color(0xFFFEE2E2)
        else -> AppColors.Background
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ShoppingCart, null, tint = statusColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(order.providerName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                    Text("${order.items.size} item(s) - ${formatCurrency(order.total)}", color = AppColors.TextSecondary, fontSize = 11.sp)
                }
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(statusBg).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(order.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                if (order.status == "PENDIENTE") {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFD1FAE5)).clickable(onClick = onRecibida).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Recibida", color = Color(0xFF059669), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2)).clickable(onClick = onCancelar).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Cancelar", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (order.notes.isNotBlank()) {
                Text(order.notes, color = AppColors.TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun OrdenCompraFormModal(
    order: PurchaseOrder?,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSave: (PurchaseOrder) -> Unit
) {
    var providerName by remember(order) { mutableStateOf(order?.providerName ?: "") }
    var notes by remember(order) { mutableStateOf(order?.notes ?: "") }
    var items by remember(order) { mutableStateOf(order?.items?.toMutableList() ?: mutableListOf<PurchaseOrderItem>()) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var itemQuantity by remember { mutableStateOf("1") }
    var itemPrice by remember { mutableStateOf("0") }
    var showProductPicker by remember { mutableStateOf(false) }
    var prodSearch by remember { mutableStateOf("") }

    val filteredProducts = remember(products, prodSearch) {
        if (prodSearch.isBlank()) products
        else products.filter { it.name.contains(prodSearch, ignoreCase = true) }
    }

    val total = remember(items) { items.sumOf { it.subtotal } }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(560.dp).heightIn(max = 700.dp).clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ShoppingCart, null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (order != null) "Editar Orden de Compra" else "Nueva Orden de Compra", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                    Text("Complete los datos de la orden", color = AppColors.TextSecondary, fontSize = 12.sp)
                }
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Column {
                Text("Proveedor", color = AppColors.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    BasicTextField(value = providerName, onValueChange = { providerName = it }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
            }

            Column {
                Text("Notas", color = AppColors.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.TopStart) {
                    BasicTextField(value = notes, onValueChange = { notes = it }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp), modifier = Modifier.fillMaxSize())
                }
            }

            Divider(color = AppColors.Border)
            Text("Productos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)

            if (items.isNotEmpty()) {
                items.forEachIndexed { idx, item ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                            Text("Qty: ${item.quantity} x ${formatCurrency(item.unitPrice)} = ${formatCurrency(item.subtotal)}", fontSize = 11.sp, color = AppColors.TextSecondary)
                        }
                        Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFFEE2E2)).clickable {
                            items = items.toMutableList().apply { removeAt(idx) }
                        }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Close, null, tint = AppColors.Danger, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            if (showProductPicker) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Surface).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Search, null, tint = AppColors.Gray, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            BasicTextField(value = prodSearch, onValueChange = { prodSearch = it }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 12.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                        }
                    }
                    filteredProducts.take(8).forEach { product ->
                        val alreadyAdded = items.any { it.productId == product.id }
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                                .background(if (selectedProductId == product.id) AppColors.PrimaryLight else Color.Transparent)
                                .clickable(enabled = !alreadyAdded) {
                                    selectedProductId = product.id
                                    itemQuantity = "1"
                                    itemPrice = product.cost.toString()
                                    showProductPicker = false
                                }
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(product.name, color = if (alreadyAdded) AppColors.Gray else AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                if (alreadyAdded) Text("Agregado", color = AppColors.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    if (selectedProductId == null) Text("Seleccionar producto", color = AppColors.Gray, fontSize = 13.sp)
                    else Text(products.find { it.id == selectedProductId }?.name ?: "", color = AppColors.TextPrimary, fontSize = 13.sp)
                }
                Box(Modifier.width(60.dp).height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                    BasicTextField(value = itemQuantity, onValueChange = { itemQuantity = it.filter { c -> c.isDigit() } }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
                Box(Modifier.width(80.dp).height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                    BasicTextField(value = itemPrice, onValueChange = { itemPrice = it.filter { c -> c.isDigit() || c == '.' } }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(if (selectedProductId != null) AppColors.Primary else AppColors.Gray).clickable(enabled = selectedProductId != null) {
                    val prod = products.find { it.id == selectedProductId }
                    if (prod != null) {
                        val qty = itemQuantity.toIntOrNull() ?: 1
                        val price = itemPrice.toDoubleOrNull() ?: 0.0
                        if (qty > 0 && price > 0) {
                            items = (items + PurchaseOrderItem(productId = prod.id, productName = prod.name, quantity = qty, unitPrice = price)).toMutableList()
                            selectedProductId = null; itemQuantity = "1"; itemPrice = "0"
                        }
                    }
                }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).clickable { showProductPicker = !showProductPicker }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Search, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                }
            }

            Divider(color = AppColors.Border)

            Row(Modifier.fillMaxWidth()) {
                Text("Total:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Text(formatCurrency(total), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Primary)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (providerName.isNotBlank() && items.isNotEmpty()) AppColors.Primary else AppColors.Gray)
                        .clickable(enabled = providerName.isNotBlank() && items.isNotEmpty()) {
                            onSave(PurchaseOrder(
                                id = order?.id ?: "",
                                providerName = providerName, items = items,
                                notes = notes, status = order?.status ?: "PENDIENTE",
                                createdAt = order?.createdAt ?: System.currentTimeMillis()
                            ))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Guardar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
