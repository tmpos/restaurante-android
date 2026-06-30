package com.tmrestaurant.ui.screens.clientes

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
import com.tmrestaurant.ui.data.Cliente
import com.tmrestaurant.ui.data.ClientesManager
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ClientesScreen() {
    val clientes = ClientesManager.clientes
    val canDeleteCustomers = AccessControl.canDeleteCustomers(TurnoManager.currentUser)
    var searchQuery by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }
    var editingCliente by remember { mutableStateOf<Cliente?>(null) }

    val filtered = if (searchQuery.isBlank()) clientes
    else ClientesManager.search(searchQuery)

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    if (searchQuery.isEmpty()) Text("Buscar cliente...", color = AppColors.Gray, fontSize = 13.sp)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp),
                        modifier = Modifier.fillMaxSize(),
                        singleLine = true
                    )
                }
            }
            Button(
                onClick = { editingCliente = null; showForm = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Nuevo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Nombre" to 3f, "RNC" to 1.4f, "Telefono" to 1.4f, "Puntos" to 1f, "Tipo" to 1.5f).forEach { (label, w) ->
                Text(label, color = AppColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(w))
            }
            Spacer(Modifier.width(80.dp))
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.People, null, tint = AppColors.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay clientes", color = AppColors.TextSecondary, fontSize = 16.sp)
                    Text("Agregue clientes para facturacion", color = AppColors.Gray, fontSize = 12.sp)
                }
            }
        } else {
            Text("${filtered.size} clientes", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.id }) { cliente ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(12.dp).clickable { editingCliente = cliente; showForm = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(3f)) {
                                Text(cliente.nombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                if (cliente.email.isNotBlank()) Text(cliente.email, fontSize = 11.sp, color = AppColors.TextSecondary)
                            }
                            Text(cliente.rnc.ifBlank { "-" }, color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1.4f))
                            Text(cliente.telefono.ifBlank { "-" }, color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1.4f))
                            Column(Modifier.weight(1f)) {
                                Text("${cliente.loyaltyPoints}", color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("RD$ ${"%,.0f".format(cliente.totalSpent)}", color = AppColors.TextSecondary, fontSize = 9.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (cliente.tipo == "Credito") Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                            ) {
                                Text(cliente.tipo, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (cliente.tipo == "Credito") Color(0xFFD97706) else Color(0xFF16A34A),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            if (canDeleteCustomers) {
                                Spacer(Modifier.width(8.dp))
                                Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.DangerLight).clickable {
                                    ClientesManager.delete(cliente.id)
                                }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(16.dp))
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
        ClienteFormModal(
            cliente = editingCliente,
            onSave = { c ->
                if (editingCliente != null) ClientesManager.update(c) else ClientesManager.add(c)
                showForm = false
            },
            onDismiss = { showForm = false }
        )
    }
}

@Composable
private fun ClienteFormModal(
    cliente: Cliente?,
    onSave: (Cliente) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember(cliente) { mutableStateOf(cliente?.nombre ?: "") }
    var rnc by remember(cliente) { mutableStateOf(cliente?.rnc ?: "") }
    var telefono by remember(cliente) { mutableStateOf(cliente?.telefono ?: "") }
    var email by remember(cliente) { mutableStateOf(cliente?.email ?: "") }
    var direccion by remember(cliente) { mutableStateOf(cliente?.direccion ?: "") }
    var tipo by remember(cliente) { mutableStateOf(cliente?.tipo ?: "Consumidor Final") }
    var limiteStr by remember(cliente) { mutableStateOf(if (cliente != null) cliente.limiteCredito.toString() else "") }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(500.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (cliente != null) "Editar Cliente" else "Nuevo Cliente",
                    fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f)
                )
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField("Nombre", nombre, Modifier.weight(2f)) { nombre = it }
                FormField("RNC/Cedula", rnc, Modifier.weight(1f)) { rnc = it }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField("Telefono", telefono, Modifier.weight(1f)) { telefono = it }
                FormField("Email", email, Modifier.weight(1f)) { email = it }
            }
            FormField("Direccion", direccion, Modifier.fillMaxWidth()) { direccion = it }
            if (cliente != null) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF0FDF4)).padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Stars, null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("${cliente.loyaltyPoints} puntos | Total consumido RD$ ${"%,.2f".format(cliente.totalSpent)}", color = Color(0xFF166534), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Tipo", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Consumidor Final", "Credito").forEach { opt ->
                            Box(
                                Modifier.clip(RoundedCornerShape(8.dp)).background(if (tipo == opt) AppColors.PrimaryLight else AppColors.Background)
                                    .clickable { tipo = opt }.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(opt, fontSize = 12.sp, fontWeight = if (tipo == opt) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (tipo == opt) AppColors.Primary else AppColors.TextSecondary)
                            }
                        }
                    }
                }
                if (tipo == "Credito") {
                    FormField("Limite Credito RD$", limiteStr, Modifier.weight(1f)) { limiteStr = it }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss, shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) { Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp) }
                Button(
                    onClick = {
                        val c = Cliente(
                            id = cliente?.id ?: "",
                            nombre = nombre, rnc = rnc, telefono = telefono,
                            email = email, direccion = direccion, tipo = tipo,
                            limiteCredito = limiteStr.toDoubleOrNull() ?: 0.0,
                            loyaltyPoints = cliente?.loyaltyPoints ?: 0,
                            totalSpent = cliente?.totalSpent ?: 0.0
                        )
                        if (c.nombre.isNotBlank()) onSave(c)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) { Text("Guardar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun FormField(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Column(modifier) {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp))
                .background(AppColors.Background).padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                modifier = Modifier.fillMaxSize(),
                singleLine = true
            )
        }
    }
}
