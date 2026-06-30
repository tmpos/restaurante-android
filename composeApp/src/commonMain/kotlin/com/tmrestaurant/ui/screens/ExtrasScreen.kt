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
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ExtrasScreen() {
    if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Extras",
            subtitle = "Solo los administradores pueden gestionar extras y guarniciones."
        )
        return
    }
    val extras by remember { mutableStateOf(ExtrasManager.extras) }
    val productState = LocalProductState.current
    val products = productState.products
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Extra?>(null) }

    val filtered = extras

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Extras / Guarniciones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Button(onClick = { editing = null; showForm = true }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)) {
                Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Nuevo", color = Color.White, fontSize = 13.sp)
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddCircle, null, tint = AppColors.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay extras", color = AppColors.TextSecondary, fontSize = 16.sp)
                    Text("Agregue modificadores o guarniciones", color = AppColors.Gray, fontSize = 12.sp)
                }
            }
        } else {
            Text("${filtered.size} elementos", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.id }) { extra ->
                    val product = products.find { it.id == extra.productId }
                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp).clickable { editing = extra; showForm = true }, verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (extra.type == "guarnicion") Color(0xFFFEF3C7) else AppColors.PrimaryLight), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.AddCircle, null, tint = if (extra.type == "guarnicion") Color(0xFFD97706) else AppColors.Primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(extra.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                val scopeLabel = if (extra.type == "guarnicion") {
                                    "Todos excepto bebidas"
                                } else {
                                    product?.name ?: "Producto #${extra.productId}"
                                }
                                Text("$scopeLabel  |  ${if (extra.type == "guarnicion") "Guarnicion" else "Extra"}  |  +RD\$ ${"%.2f".format(extra.price)}", fontSize = 11.sp, color = AppColors.TextSecondary)
                            }
                            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.DangerLight).clickable { ExtrasManager.delete(extra.id) }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showForm) {
        ExtraFormModal(
            extra = editing,
            onSave = { e ->
                if (editing != null) ExtrasManager.update(e) else ExtrasManager.add(e)
                showForm = false
            },
            onDismiss = { showForm = false }
        )
    }
}

@Composable
private fun ExtraFormModal(
    extra: Extra?,
    onSave: (Extra) -> Unit,
    onDismiss: () -> Unit
) {
    val productState = LocalProductState.current
    val products = productState.products
    var name by remember { mutableStateOf(extra?.name ?: "") }
    var priceStr by remember { mutableStateOf(if (extra != null) extra.price.toString() else "") }
    var selectedProductId by remember { mutableStateOf(extra?.productId ?: products.firstOrNull()?.id ?: 0) }
    var tipo by remember { mutableStateOf(extra?.type ?: "extra") }
    var showPicker by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(420.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (extra != null) "Editar" else "Nuevo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            ExtField("Nombre", name) { name = it }
            ExtField("Precio RD\$", priceStr) { priceStr = it.filter { it.isDigit() || it == '.' } }

            Column {
                Text("Tipo", color = AppColors.TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("extra" to "Extra", "guarnicion" to "Guarnicion").forEach { (v, l) ->
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (tipo == v) AppColors.PrimaryLight else AppColors.Background).clickable { tipo = v }.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(l, fontSize = 12.sp, fontWeight = if (tipo == v) FontWeight.Bold else FontWeight.Normal, color = if (tipo == v) AppColors.Primary else AppColors.TextSecondary)
                        }
                    }
                }
            }

            if (tipo == "guarnicion") {
                Column {
                    Text("Disponible para", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEF3C7)).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Text("Todos los productos excepto bebidas", color = Color(0xFFD97706), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Column {
                    Text("Producto", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).clickable { showPicker = true }.padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        Text(products.find { it.id == selectedProductId }?.name ?: "Seleccionar producto", color = AppColors.TextPrimary, fontSize = 14.sp)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(44.dp)) { Text("Cancelar") }
                Button(onClick = {
                    val p = priceStr.toDoubleOrNull() ?: 0.0
                    val targetProductId = if (tipo == "guarnicion") 0 else selectedProductId
                    if (name.isNotBlank() && (tipo == "guarnicion" || targetProductId > 0)) {
                        val e = if (extra != null) extra.copy(name = name, price = p, productId = targetProductId, type = tipo, updatedAt = System.currentTimeMillis())
                        else Extra(name = name, price = p, productId = targetProductId, type = tipo)
                        onSave(e)
                    }
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary), modifier = Modifier.weight(1f).height(44.dp)) {
                    Text("Guardar", color = Color.White)
                }
            }
        }
    }

    if (showPicker) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)).clickable { showPicker = false }, contentAlignment = Alignment.Center) {
            Column(Modifier.width(400.dp).heightIn(max = 500.dp).clip(RoundedCornerShape(16.dp)).background(AppColors.Surface).padding(16.dp)) {
                Text("Seleccionar Producto", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(products.filter { it.active }) { p ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (selectedProductId == p.id) AppColors.PrimaryLight else Color.Transparent).clickable { selectedProductId = p.id; showPicker = false }.padding(10.dp)) {
                            Text(p.name, fontSize = 14.sp, color = AppColors.TextPrimary, fontWeight = if (selectedProductId == p.id) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
        }
    }
}
