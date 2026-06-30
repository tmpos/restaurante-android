package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import com.tmrestaurant.platform.formatDateTime

@Composable
fun InventarioScreen() {
    val productState = LocalProductState.current
    val products = productState.products
    var searchQuery by remember { mutableStateOf("") }
    var showAdjustDialog by remember { mutableStateOf(false) }
    var adjustProduct by remember { mutableStateOf<Product?>(null) }
    var showHistory by remember { mutableStateOf(false) }
    var showValuation by remember { mutableStateOf(false) }

    val filtered = if (searchQuery.isBlank()) products
    else products.filter { it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery) }

    val bajoStock = filtered.filter { it.controlInventory && it.stock > 0 && it.stock <= it.stockAlert }
    val sinStock = filtered.filter { it.controlInventory && it.stock == 0 }

    val totalCosto = products.filter { it.controlInventory }.sumOf { it.cost * it.stock }
    val totalVenta = products.filter { it.controlInventory }.sumOf { it.price * it.stock }
    val totalGanancia = totalVenta - totalCosto

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Row(Modifier.fillMaxWidth().padding(16.dp, 12.dp, 16.dp, 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Surface).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Search, null, tint = AppColors.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    if (searchQuery.isEmpty()) Text("Buscar...", color = AppColors.Gray, fontSize = 13.sp)
                    BasicTextField(value = searchQuery, onValueChange = { searchQuery = it }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
            }
            Box(Modifier.height(38.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF0FDF4)).clickable { showValuation = true }.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccountBalance, null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Valoración", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A))
                }
            }
            Box(Modifier.height(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).clickable { showHistory = true }.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.History, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ajustes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Primary)
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AlertBadge("Bajo Stock", bajoStock.size, Color(0xFFD97706))
            AlertBadge("Sin Stock", sinStock.size, Color(0xFFEF4444))
            Text("${filtered.size} productos", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
        }

        Spacer(Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Inventory2, null, tint = AppColors.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No se encontraron productos", color = AppColors.TextSecondary, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.id }) { product ->
                    val isLow = product.controlInventory && product.stock <= product.stockAlert
                    val isOut = product.controlInventory && product.stock == 0
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isOut) Color(0xFFFEF2F2) else if (isLow) Color(0xFFFFFBEB) else AppColors.Surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(
                                when { isOut -> Color(0xFFFEE2E2); isLow -> Color(0xFFFEF3C7); else -> AppColors.PrimaryLight }
                            ), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Inventory2, null, tint = when { isOut -> AppColors.Danger; isLow -> Color(0xFFD97706); else -> AppColors.Primary }, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Código: ${product.code.ifBlank { "-" }}", fontSize = 10.sp, color = AppColors.TextSecondary)
                                    if (isLow || isOut) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(Modifier.clip(RoundedCornerShape(3.dp)).background(if (isOut) AppColors.Danger.copy(alpha = 0.15f) else Color(0xFFD97706).copy(alpha = 0.15f)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                                            Text(if (isOut) "SIN STOCK" else "STOCK BAJO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isOut) AppColors.Danger else Color(0xFFD97706))
                                        }
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${product.stock}", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                    color = when { !product.controlInventory -> AppColors.TextSecondary; isOut -> AppColors.Danger; isLow -> Color(0xFFD97706); else -> Color(0xFF16A34A) })
                                if (product.controlInventory) Text("Alerta: ${product.stockAlert}", fontSize = 9.sp, color = AppColors.TextSecondary)
                                else Text("Sin control", fontSize = 9.sp, color = AppColors.Gray)
                            }
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.width(32.dp).height(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable { adjustProduct = product; showAdjustDialog = true }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Edit, null, tint = AppColors.Primary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showAdjustDialog && adjustProduct != null) {
        AdjustStockDialog(
            product = adjustProduct!!,
            onConfirm = { qty, reason ->
                productState.adjustStock(adjustProduct!!.id, qty, reason)
                showAdjustDialog = false
                adjustProduct = null
            },
            onDismiss = { showAdjustDialog = false; adjustProduct = null }
        )
    }

    if (showHistory) {
        AdjustmentHistoryModal(onDismiss = { showHistory = false })
    }

    if (showValuation) {
        ValuationModal(totalCosto = totalCosto, totalVenta = totalVenta, totalGanancia = totalGanancia, productCount = products.filter { it.controlInventory }.size, onDismiss = { showValuation = false })
    }
}

@Composable
private fun AdjustStockDialog(product: Product, onConfirm: (Int, String) -> Unit, onDismiss: () -> Unit) {
    var qtyText by remember { mutableStateOf("0") }
    var reason by remember { mutableStateOf("") }
    val presetReasons = listOf("Ajuste manual", "Merma", "Devolucion", "Transferencia", "Conteo fisico")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Stock: ${product.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Stock actual: ${product.stock}", color = AppColors.TextSecondary, fontSize = 13.sp)
                Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    if (qtyText.isEmpty()) Text("Cantidad (+/-)", color = AppColors.Gray, fontSize = 13.sp)
                    BasicTextField(value = qtyText, onValueChange = { if (it.all { c -> c.isDigit() || c == '-' }) qtyText = it }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
                Text("Motivo:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSecondary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetReasons.forEach { r ->
                        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(if (reason == r) AppColors.PrimaryLight else AppColors.Background).clickable { reason = r }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(r, fontSize = 11.sp, color = if (reason == r) AppColors.Primary else AppColors.TextSecondary, fontWeight = if (reason == r) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                    if (reason.isBlank()) Text("O escriba un motivo...", color = AppColors.Gray, fontSize = 12.sp)
                    BasicTextField(value = reason, onValueChange = { reason = it }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 12.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val qty = qtyText.toIntOrNull() ?: 0
                if (qty != 0 && reason.isNotBlank()) onConfirm(qty, reason)
            }) { Text("Ajustar", color = if (qtyText.toIntOrNull() != 0 && reason.isNotBlank()) AppColors.Primary else AppColors.Gray) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AdjustmentHistoryModal(onDismiss: () -> Unit) {
    val adjustments = InventoryAdjustmentManager.adjustments
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(600.dp).heightIn(max = 600.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Historial de Ajustes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            if (adjustments.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No hay ajustes registrados", color = AppColors.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(adjustments.take(100)) { adj ->
                        val deltaColor = if (adj.delta > 0) Color(0xFF16A34A) else AppColors.Danger
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(adj.productName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                Text("${adj.reason} · ${adj.userName}", fontSize = 10.sp, color = AppColors.TextSecondary)
                            }
                            Text("${adj.previousStock} → ${adj.newStock}", fontSize = 13.sp, color = AppColors.TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text(if (adj.delta > 0) "+${adj.delta}" else "${adj.delta}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = deltaColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValuationModal(totalCosto: Double, totalVenta: Double, totalGanancia: Double, productCount: Int, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(400.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0FDF4)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AccountBalance, null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Valoración de Inventario", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("$productCount productos con control", fontSize = 12.sp, color = AppColors.TextSecondary)
                }
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
            Spacer(Modifier.height(16.dp))
            ValuationRow("Valor al Costo", totalCosto, Color(0xFF0891B2))
            Spacer(Modifier.height(12.dp))
            ValuationRow("Valor al Precio Venta", totalVenta, Color(0xFFD97706))
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
            Spacer(Modifier.height(12.dp))
            ValuationRow("Ganancia Potencial", totalGanancia, Color(0xFF16A34A), bold = true)
        }
    }
}

@Composable
private fun ValuationRow(label: String, value: Double, color: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = AppColors.TextSecondary, modifier = Modifier.weight(1f))
        Text("RD\$ ${"%,.2f".format(value)}", fontSize = if (bold) 18.sp else 16.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun AlertBadge(label: String, count: Int, color: Color) {
    Row(Modifier.clip(RoundedCornerShape(20.dp)).background(if (count > 0) color.copy(alpha = 0.12f) else AppColors.Background).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (count > 0) color else AppColors.Gray)
        if (count > 0) {
            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(color).padding(horizontal = 6.dp, vertical = 1.dp)) {
                Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
