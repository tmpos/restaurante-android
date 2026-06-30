package com.tmrestaurant.ui.screens.reservaciones

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.MesasManager
import com.tmrestaurant.ui.data.Reservacion
import com.tmrestaurant.ui.data.ReservacionesManager
import com.tmrestaurant.ui.data.ClientesManager
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ReservacionesScreen() {
    val canDeleteReservations = AccessControl.canDeleteReservations(TurnoManager.currentUser)
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReservacion by remember { mutableStateOf<Reservacion?>(null) }
    var filterEstado by remember { mutableStateOf("TODAS") }

    val reservaciones = when (filterEstado) {
        "TODAS" -> ReservacionesManager.reservaciones
        "PENDIENTE" -> ReservacionesManager.pendientes()
        "ACTIVAS" -> ReservacionesManager.activas()
        else -> ReservacionesManager.reservaciones.filter { it.estado == filterEstado }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Reservaciones", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1F2937))
                    Text("Gestione las reservaciones de sus clientes", color = Color(0xFF6B7280), fontSize = 13.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf("TODAS", "PENDIENTE", "CONFIRMADA", "CANCELADA", "COMPLETADA").forEach { f ->
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp))
                                .background(if (filterEstado == f) AppColors.Primary else AppColors.Background)
                                .clickable { filterEstado = f }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(f, color = if (filterEstado == f) Color.White else AppColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Nueva Reservacion", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (reservaciones.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Event, null, tint = AppColors.Gray, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No hay reservaciones", color = AppColors.TextSecondary, fontSize = 16.sp)
                        Text("Cree una nueva reservacion para comenzar", color = AppColors.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(320.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reservaciones, key = { it.id }) { reservacion ->
                        ReservacionCard(
                            reservacion = reservacion,
                            onEdit = { editingReservacion = reservacion },
                            onDelete = { ReservacionesManager.delete(reservacion.id) },
                            onConfirmar = { ReservacionesManager.cambiarEstado(reservacion.id, "CONFIRMADA") },
                            onCompletar = { ReservacionesManager.cambiarEstado(reservacion.id, "COMPLETADA") },
                            onCancelar = { ReservacionesManager.cambiarEstado(reservacion.id, "CANCELADA") },
                            canDelete = canDeleteReservations
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || editingReservacion != null) {
        ReservacionFormModal(
            reservacion = editingReservacion,
            onDismiss = { showAddDialog = false; editingReservacion = null },
            onSave = { r ->
                if (editingReservacion != null) ReservacionesManager.update(r)
                else ReservacionesManager.add(r)
                showAddDialog = false; editingReservacion = null
            }
        )
    }
}

@Composable
private fun ReservacionCard(
    reservacion: Reservacion,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onConfirmar: () -> Unit,
    onCompletar: () -> Unit,
    onCancelar: () -> Unit,
    canDelete: Boolean
) {
    val estadoColor = when (reservacion.estado) {
        "PENDIENTE" -> Color(0xFFD97706)
        "CONFIRMADA" -> Color(0xFF059669)
        "CANCELADA" -> Color(0xFFEF4444)
        "COMPLETADA" -> Color(0xFF6B7280)
        else -> AppColors.TextSecondary
    }
    val mesaName = reservacion.mesaId?.let { id -> MesasManager.mesas.find { it.id == id }?.name }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(estadoColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Event, null, tint = estadoColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(reservacion.clienteNombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F2937))
                    Text("${reservacion.clientePersonas} persona(s) - ${reservacion.fecha} ${reservacion.hora}", color = Color(0xFF6B7280), fontSize = 11.sp)
                }
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(estadoColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(reservacion.estado, color = estadoColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (reservacion.clienteTelefono.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, null, tint = AppColors.TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(reservacion.clienteTelefono, color = AppColors.TextSecondary, fontSize = 12.sp)
                }
            }

            if (mesaName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.TableRestaurant, null, tint = AppColors.Primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Mesa: $mesaName", color = AppColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (reservacion.notas.isNotBlank()) {
                Text(reservacion.notas, color = Color(0xFF6B7280), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                }
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.weight(1f))
                if (reservacion.estado == "PENDIENTE") {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFD1FAE5)).clickable(onClick = onConfirmar).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Confirmar", color = Color(0xFF059669), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (reservacion.estado == "CONFIRMADA") {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFDBEAFE)).clickable(onClick = onCompletar).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Completar", color = Color(0xFF2563EB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (reservacion.estado in listOf("PENDIENTE", "CONFIRMADA")) {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2)).clickable(onClick = onCancelar).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Cancelar", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservacionFormModal(
    reservacion: Reservacion?,
    onDismiss: () -> Unit,
    onSave: (Reservacion) -> Unit
) {
    var nombre by remember(reservacion) { mutableStateOf(reservacion?.clienteNombre ?: "") }
    var telefono by remember(reservacion) { mutableStateOf(reservacion?.clienteTelefono ?: "") }
    var personas by remember(reservacion) { mutableStateOf(reservacion?.clientePersonas?.toString() ?: "1") }
    var fecha by remember(reservacion) { mutableStateOf(reservacion?.fecha ?: "") }
    var hora by remember(reservacion) { mutableStateOf(reservacion?.hora ?: "") }
    var notas by remember(reservacion) { mutableStateOf(reservacion?.notas ?: "") }
    var selectedMesaId by remember(reservacion) { mutableStateOf(reservacion?.mesaId) }
    var showClienteSearch by remember { mutableStateOf(false) }
    var searchClienteQuery by remember { mutableStateOf("") }

    val clientesFiltrados = remember(searchClienteQuery) {
        if (searchClienteQuery.isBlank()) ClientesManager.clientes.take(5)
        else ClientesManager.search(searchClienteQuery)
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(480.dp).heightIn(max = 640.dp).clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Event, null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (reservacion != null) "Editar Reservacion" else "Nueva Reservacion", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                    Text("Complete los datos de la reservacion", color = AppColors.TextSecondary, fontSize = 12.sp)
                }
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("Nombre del cliente", nombre, { nombre = it }, Modifier.weight(1f))
                Box(Modifier.size(44.dp).align(Alignment.Bottom).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).clickable { showClienteSearch = !showClienteSearch }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Search, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                }
            }

            if (showClienteSearch) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FormField("Buscar cliente...", searchClienteQuery, { searchClienteQuery = it }, Modifier.fillMaxWidth())
                    clientesFiltrados.forEach { c ->
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).clickable {
                                nombre = c.nombre; telefono = c.telefono; showClienteSearch = false
                            }.padding(8.dp)
                        ) {
                            Text(c.nombre, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("Telefono", telefono, { telefono = it }, Modifier.weight(1f))
                FormField("Personas", personas, { personas = it }, Modifier.width(100.dp))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField("Fecha (dd/mm/aaaa)", fecha, { fecha = it }, Modifier.weight(1f))
                FormField("Hora (hh:mm)", hora, { hora = it }, Modifier.weight(1f))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mesa (opcional):", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.width(120.dp))
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        val selectedMesa = selectedMesaId?.let { id -> MesasManager.mesas.find { it.id == id } }
                        Text(
                            selectedMesa?.name ?: "Seleccionar mesa",
                            color = if (selectedMesa != null) AppColors.TextPrimary else AppColors.Gray,
                            fontSize = 13.sp, modifier = Modifier.weight(1f)
                        )
                        if (selectedMesa != null) {
                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFFEE2E2)).clickable { selectedMesaId = null }.padding(4.dp)) {
                                Icon(Icons.Outlined.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                        .clickable {
                            val freeMesas = MesasManager.mesas.filter { !it.isOccupied }
                            if (freeMesas.isNotEmpty()) {
                                selectedMesaId = if (selectedMesaId != null) {
                                    val idx = freeMesas.indexOfFirst { it.id == selectedMesaId }
                                    freeMesas.getOrElse((idx + 1) % freeMesas.size) { freeMesas.first() }.id
                                } else freeMesas.first().id
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.TableRestaurant, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                }
            }

            Box(
                Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(12.dp)
            ) {
                if (notas.isEmpty()) Text("Notas (opcional)", color = AppColors.Gray, fontSize = 13.sp)
                BasicTextField(
                    value = notas, onValueChange = { notas = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (nombre.isNotBlank()) AppColors.Primary else AppColors.Gray)
                        .clickable(enabled = nombre.isNotBlank()) {
                            onSave(Reservacion(
                                id = reservacion?.id ?: "",
                                clienteNombre = nombre, clienteTelefono = telefono,
                                clientePersonas = personas.toIntOrNull() ?: 1,
                                fecha = fecha, hora = hora, notas = notas, mesaId = selectedMesaId,
                                estado = reservacion?.estado ?: "PENDIENTE",
                                createdAt = reservacion?.createdAt ?: System.currentTimeMillis()
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

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
            .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) Text(label, color = AppColors.Gray, fontSize = 13.sp)
        BasicTextField(value = value, onValueChange = onValueChange, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
    }
}
