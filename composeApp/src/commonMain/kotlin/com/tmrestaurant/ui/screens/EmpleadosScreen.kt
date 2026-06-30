package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.Employee
import com.tmrestaurant.ui.data.EmployeeManager
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun EmpleadosScreen() {
    if (!AccessControl.canManageEmployees(TurnoManager.currentUser)) {
        PlaceholderScreen("Empleados", "Solo los administradores pueden gestionar empleados.")
        return
    }

    val employees = EmployeeManager.employees
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Employee?>(null) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Gestion de Empleados", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text("${employees.count { it.active }} activos | ${employees.count { it.clockedIn }} marcados dentro", fontSize = 12.sp, color = AppColors.TextSecondary)
            }
            Button(
                onClick = { editing = null; showForm = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
            ) {
                Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Nuevo", color = Color.White, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        if (employees.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.People, null, tint = AppColors.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay empleados registrados", color = AppColors.TextSecondary, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(employees, key = { it.id }) { employee ->
                    EmployeeCard(
                        employee = employee,
                        onEdit = { editing = employee; showForm = true },
                        onDelete = { EmployeeManager.delete(employee.id) },
                        onClock = {
                            if (employee.clockedIn) EmployeeManager.clockOut(employee.id)
                            else EmployeeManager.clockIn(employee.id)
                        }
                    )
                }
            }
        }
    }

    if (showForm) {
        EmployeeFormModal(
            employee = editing,
            onDismiss = { showForm = false },
            onSave = {
                if (editing == null) EmployeeManager.add(it) else EmployeeManager.update(it)
                showForm = false
            }
        )
    }
}

@Composable
private fun EmployeeCard(employee: Employee, onEdit: () -> Unit, onDelete: () -> Unit, onClock: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(if (employee.clockedIn) Color(0xFFDCFCE7) else AppColors.Background), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Badge, null, tint = if (employee.clockedIn) Color(0xFF16A34A) else AppColors.IconGray, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(employee.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(employee.position.ifBlank { "Sin puesto" }, fontSize = 11.sp, color = AppColors.TextSecondary)
                Text("${employee.phone.ifBlank { "Sin telefono" }} | ${formatMinutes(employee.totalWorkedMinutes)} acumuladas", fontSize = 10.sp, color = AppColors.TextSecondary)
            }
            Box(Modifier.height(32.dp).clip(RoundedCornerShape(8.dp)).background(if (employee.clockedIn) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)).clickable(onClick = onClock).padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (employee.clockedIn) Icons.Outlined.Logout else Icons.Outlined.Login, null, tint = if (employee.clockedIn) AppColors.Danger else Color(0xFF16A34A), modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(if (employee.clockedIn) "Salida" else "Entrada", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (employee.clockedIn) AppColors.Danger else Color(0xFF16A34A))
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable(onClick = onEdit), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Edit, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.DangerLight).clickable(onClick = onDelete), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmployeeFormModal(employee: Employee?, onDismiss: () -> Unit, onSave: (Employee) -> Unit) {
    var name by remember(employee) { mutableStateOf(employee?.name ?: "") }
    var position by remember(employee) { mutableStateOf(employee?.position ?: "") }
    var phone by remember(employee) { mutableStateOf(employee?.phone ?: "") }
    var email by remember(employee) { mutableStateOf(employee?.email ?: "") }
    var hireDate by remember(employee) { mutableStateOf(employee?.hireDate ?: "") }
    var hourlyRate by remember(employee) { mutableStateOf(employee?.hourlyRate?.takeIf { it > 0.0 }?.toString() ?: "") }
    var commission by remember(employee) { mutableStateOf(employee?.commissionPercent?.takeIf { it > 0.0 }?.toString() ?: "") }
    var active by remember(employee) { mutableStateOf(employee?.active ?: true) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(520.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (employee == null) "Nuevo Empleado" else "Editar Empleado", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            EmployeeField("Nombre", name) { name = it }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EmployeeField("Puesto", position, Modifier.weight(1f)) { position = it }
                EmployeeField("Fecha ingreso", hireDate, Modifier.weight(1f)) { hireDate = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EmployeeField("Telefono", phone, Modifier.weight(1f)) { phone = it }
                EmployeeField("Email", email, Modifier.weight(1f)) { email = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EmployeeField("Tarifa hora RD$", hourlyRate, Modifier.weight(1f)) { hourlyRate = it.filter { c -> c.isDigit() || c == '.' } }
                EmployeeField("Comision %", commission, Modifier.weight(1f)) { commission = it.filter { c -> c.isDigit() || c == '.' } }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AccessTime, null, tint = AppColors.IconGray, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Empleado activo", color = AppColors.TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Switch(checked = active, onCheckedChange = { active = it }, colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Success))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(44.dp)) { Text("Cancelar") }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                (employee ?: Employee()).copy(
                                    name = name.trim(),
                                    position = position.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    hireDate = hireDate.trim(),
                                    hourlyRate = hourlyRate.toDoubleOrNull() ?: 0.0,
                                    commissionPercent = commission.toDoubleOrNull() ?: 0.0,
                                    active = active
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) { Text("Guardar", color = Color.White) }
            }
        }
    }
}

@Composable
private fun EmployeeField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    Column(modifier) {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(value = value, onValueChange = onChange, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
        }
    }
}

private fun formatMinutes(minutes: Long): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return "${hours}h ${rest}m"
}
