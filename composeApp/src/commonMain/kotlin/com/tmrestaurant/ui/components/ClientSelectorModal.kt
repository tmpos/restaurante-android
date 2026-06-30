package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tmrestaurant.ui.data.Cliente
import com.tmrestaurant.ui.data.ClientesManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ClientSelectorModal(
    selectedClientId: String? = null,
    creditOnly: Boolean = false,
    onDismiss: () -> Unit,
    onSelect: (Cliente) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    val clients = ClientesManager.clientes.filter {
        (!creditOnly || it.tipo.equals("Credito", true)) &&
            (search.isBlank() || it.nombre.contains(search, true) ||
                it.telefono.contains(search) || it.rnc.contains(search, true))
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.width(620.dp).heightIn(max = 680.dp).clip(RoundedCornerShape(22.dp))
                .background(Color.White).padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.People, null, tint = Color(0xFF2563EB))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Seleccionar cliente", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Busque uno existente o agregue uno nuevo", color = AppColors.TextSecondary, fontSize = 11.sp)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, null) }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectorInput(search, "Buscar por nombre, telefono o documento", Modifier.weight(1f)) { search = it }
                Button(
                    onClick = { showCreate = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Nuevo")
                }
            }
            Spacer(Modifier.height(12.dp))
            if (clients.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.PersonSearch, null, tint = AppColors.Gray, modifier = Modifier.size(48.dp))
                        Text("No hay clientes disponibles", color = AppColors.TextSecondary)
                    }
                }
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(clients, key = { it.id }) { client ->
                        val selected = client.id == selectedClientId
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(if (selected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                .border(1.dp, if (selected) Color(0xFF2563EB) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable { onSelect(client) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                                Text(client.nombre.take(1).uppercase(), color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(client.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    listOf(client.telefono, client.rnc).filter { it.isNotBlank() }.joinToString(" | ").ifBlank { "Sin datos de contacto" },
                                    color = AppColors.TextSecondary, fontSize = 10.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (client.tipo.equals("Credito", true)) Color(0xFFFFF7ED) else Color(0xFFECFDF5)
                            ) {
                                Text(
                                    client.tipo,
                                    color = if (client.tipo.equals("Credito", true)) Color(0xFFD97706) else Color(0xFF059669),
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Icon(if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.ChevronRight, null, tint = if (selected) Color(0xFF2563EB) else AppColors.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateClientModal(
            defaultCredit = creditOnly,
            onDismiss = { showCreate = false },
            onSave = { client ->
                ClientesManager.add(client)
                showCreate = false
                onSelect(client)
            }
        )
    }
}

@Composable
private fun CreateClientModal(defaultCredit: Boolean, onDismiss: () -> Unit, onSave: (Cliente) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var document by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf(defaultCredit) }
    var limit by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.width(450.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nuevo cliente", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, null) }
            }
            SelectorInput(name, "Nombre completo", Modifier.fillMaxWidth()) { name = it }
            Spacer(Modifier.height(8.dp))
            SelectorInput(phone, "Telefono", Modifier.fillMaxWidth()) { phone = it }
            Spacer(Modifier.height(8.dp))
            SelectorInput(document, "Cedula / RNC", Modifier.fillMaxWidth()) { document = it }
            Spacer(Modifier.height(9.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = credit, onCheckedChange = { credit = it })
                Text("Cliente a credito", fontSize = 12.sp)
                if (credit) {
                    Spacer(Modifier.width(12.dp))
                    SelectorInput(limit, "Limite", Modifier.weight(1f)) { limit = it.filter { ch -> ch.isDigit() || ch == '.' } }
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    onSave(
                        Cliente(
                            nombre = name.trim(),
                            telefono = phone.trim(),
                            rnc = document.trim(),
                            tipo = if (credit) "Credito" else "Consumidor Final",
                            limiteCredito = limit.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) { Text("Guardar y seleccionar", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SelectorInput(value: String, hint: String, modifier: Modifier, onChange: (String) -> Unit) {
    Box(
        modifier.height(44.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isBlank()) Text(hint, color = AppColors.Gray, fontSize = 11.sp)
        BasicTextField(value, onChange, textStyle = TextStyle(fontSize = 13.sp, color = AppColors.TextPrimary), modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}
