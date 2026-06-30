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
fun ProveedoresScreen() {
    if (!AccessControl.canManageSuppliers(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Proveedores",
            subtitle = "Solo los administradores pueden gestionar proveedores."
        )
        return
    }
    val proveedores = ProveedoresManager.proveedores
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Proveedor?>(null) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Proveedores", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Button(onClick = { editing = null; showForm = true }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)) {
                Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Nuevo", color = Color.White, fontSize = 13.sp)
            }
        }

        if (proveedores.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.LocalShipping, null, tint = AppColors.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay proveedores", color = AppColors.TextSecondary, fontSize = 16.sp)
                }
            }
        } else {
            Text("${proveedores.size} proveedores", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(proveedores, key = { it.id }) { prov ->
                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp).clickable { editing = prov; showForm = true }, verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.LocalShipping, null, tint = AppColors.Info, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(prov.nombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                Text("RNC: ${prov.rnc.ifBlank { "-" }}  |  ${prov.telefono.ifBlank { "" }}", fontSize = 11.sp, color = AppColors.TextSecondary)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = AppColors.PrimaryLight) {
                                Text(prov.rubro.ifBlank { "General" }, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showForm) {
        ProveedorFormModal(
            proveedor = editing,
            onSave = { p ->
                if (editing != null) ProveedoresManager.update(p) else ProveedoresManager.add(p)
                showForm = false
            },
            onDismiss = { showForm = false }
        )
    }
}

@Composable
private fun ProveedorFormModal(proveedor: Proveedor?, onSave: (Proveedor) -> Unit, onDismiss: () -> Unit) {
    var nombre by remember(proveedor) { mutableStateOf(proveedor?.nombre ?: "") }
    var rnc by remember(proveedor) { mutableStateOf(proveedor?.rnc ?: "") }
    var telefono by remember(proveedor) { mutableStateOf(proveedor?.telefono ?: "") }
    var email by remember(proveedor) { mutableStateOf(proveedor?.email ?: "") }
    var direccion by remember(proveedor) { mutableStateOf(proveedor?.direccion ?: "") }
    var contacto by remember(proveedor) { mutableStateOf(proveedor?.contacto ?: "") }
    var rubro by remember(proveedor) { mutableStateOf(proveedor?.rubro ?: "") }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(480.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (proveedor != null) "Editar Proveedor" else "Nuevo Proveedor", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp)) }
            }
            ProvField("Nombre", nombre) { nombre = it }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProvField("RNC", rnc, Modifier.weight(1f)) { rnc = it }
                ProvField("Telefono", telefono, Modifier.weight(1f)) { telefono = it }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProvField("Email", email, Modifier.weight(1f)) { email = it }
                ProvField("Rubro", rubro, Modifier.weight(1f)) { rubro = it }
            }
            ProvField("Contacto", contacto) { contacto = it }
            ProvField("Direccion", direccion) { direccion = it }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(44.dp)) { Text("Cancelar") }
                Button(onClick = {
                    if (nombre.isNotBlank()) onSave(Proveedor(id = proveedor?.id ?: "", nombre = nombre, rnc = rnc, telefono = telefono, email = email, direccion = direccion, contacto = contacto, rubro = rubro))
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary), modifier = Modifier.weight(1f).height(44.dp)) { Text("Guardar", color = Color.White) }
            }
        }
    }
}

@Composable
private fun ProvField(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Column(modifier) {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
        }
    }
}
