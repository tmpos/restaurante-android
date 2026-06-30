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
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.ModifierGroup
import com.tmrestaurant.ui.data.ModifierManager
import com.tmrestaurant.ui.data.ModifierOption
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ModifiersScreen() {
    if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Modificadores",
            subtitle = "Solo los administradores pueden gestionar modificadores."
        )
        return
    }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ModifierGroup?>(null) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Modificadores por Producto", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text("${ModifierManager.groups.size} grupos de modificadores", fontSize = 12.sp, color = AppColors.TextSecondary)
            }
            Button(onClick = { editing = null; showForm = true }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)) {
                Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Nuevo Grupo", color = Color.White, fontSize = 13.sp)
            }
        }

        if (ModifierManager.groups.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AddCircle, null, tint = AppColors.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay grupos de modificadores", color = AppColors.TextSecondary, fontSize = 16.sp)
                    Text("Cree grupos como \"Tipo de pan\", \"Salsa\", etc.", color = AppColors.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ModifierManager.groups, key = { it.id }) { group ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Dashboard, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(group.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                        if (group.required) {
                                            Spacer(Modifier.width(6.dp))
                                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFFEE2E2)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                Text("Obligatorio", fontSize = 9.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    Text("Max ${group.maxSelections} seleccion(es) · ${group.options.size} opcion(es)", fontSize = 11.sp, color = AppColors.TextSecondary)
                                }
                                Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable { editing = group; showForm = true }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Edit, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.DangerLight).clickable { ModifierManager.delete(group.id) }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (group.options.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                                Spacer(Modifier.height(8.dp))
                                group.options.forEach { opt ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(AppColors.Primary))
                                        Spacer(Modifier.width(10.dp))
                                        Text(opt.name, fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                                        if (opt.price > 0) {
                                            Text("+RD\$ ${"%.2f".format(opt.price)}", fontSize = 12.sp, color = AppColors.Primary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showForm) {
        ModifierGroupFormModal(
            group = editing,
            onSave = { g ->
                if (editing != null) ModifierManager.update(g) else ModifierManager.add(g)
                showForm = false
            },
            onDismiss = { showForm = false }
        )
    }
}

@Composable
private fun ModifierGroupFormModal(
    group: ModifierGroup?,
    onSave: (ModifierGroup) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(group?.name ?: "") }
    var required by remember { mutableStateOf(group?.required ?: false) }
    var maxSelectionsStr by remember { mutableStateOf((group?.maxSelections ?: 1).toString()) }
    var options by remember { mutableStateOf(group?.options?.map { it.copy() } ?: listOf(ModifierOption(name = "", price = 0.0))) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(480.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (group != null) "Editar Grupo" else "Nuevo Grupo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            FormField("Nombre del grupo", name) { name = it }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = required, onCheckedChange = { required = it }, colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary, checkmarkColor = Color.White))
                Spacer(Modifier.width(6.dp))
                Text("Obligatorio", color = AppColors.TextPrimary, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Text("Max. selecciones:", color = AppColors.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.width(60.dp).height(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                    BasicTextField(value = maxSelectionsStr, onValueChange = { if (it.all { c -> c.isDigit() } && it.isNotEmpty()) maxSelectionsStr = it }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))

            Text("Opciones", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
            options.forEachIndexed { idx, opt ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                        if (opt.name.isEmpty()) Text("Nombre", color = AppColors.Gray, fontSize = 13.sp)
                        BasicTextField(value = opt.name, onValueChange = { v -> options = options.toMutableList().also { it[idx] = it[idx].copy(name = v) } }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                    }
                    Box(Modifier.width(100.dp).height(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                        if (opt.price == 0.0) Text("RD\$", color = AppColors.Gray, fontSize = 13.sp)
                        BasicTextField(value = if (opt.price > 0) opt.price.toString() else "", onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) options = options.toMutableList().also { it[idx] = it[idx].copy(price = v.toDoubleOrNull() ?: 0.0) } }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                    }
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.DangerLight).clickable { if (options.size > 1) options = options.toMutableList().also { it.removeAt(idx) } }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable { options = options + ModifierOption(name = "", price = 0.0) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Add, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar opcion", color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(44.dp)) { Text("Cancelar") }
                Button(onClick = {
                    if (name.isNotBlank()) {
                        val cleanOptions = options.filter { it.name.isNotBlank() }
                        val maxSel = maxSelectionsStr.toIntOrNull() ?: 1
                        val g = if (group != null) group.copy(name = name, options = cleanOptions, required = required, maxSelections = maxSel)
                        else ModifierGroup(name = name, options = cleanOptions, required = required, maxSelections = maxSel)
                        onSave(g)
                    }
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary), modifier = Modifier.weight(1f).height(44.dp)) {
                    Text("Guardar", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
        }
    }
}
