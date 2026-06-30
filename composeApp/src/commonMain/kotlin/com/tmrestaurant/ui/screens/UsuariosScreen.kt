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
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun UsuariosScreen() {
    val currentUser = TurnoManager.currentUser
    if (!AccessControl.canManageUsers(currentUser)) {
        PlaceholderScreen(
            title = "Usuarios",
            subtitle = "Solo los administradores pueden gestionar usuarios."
        )
        return
    }
    val users = UsuariosManager.usuarios
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Usuario?>(null) }
    var showPermissions by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Gestion de Usuarios", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { showPermissions = true }, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Outlined.Security, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Permisos", color = AppColors.Primary, fontSize = 13.sp)
                }
                Button(onClick = { editing = null; showForm = true }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)) {
                    Icon(Icons.Outlined.PersonAdd, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Nuevo", color = Color.White, fontSize = 13.sp)
                }
            }
        }

        if (users.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.People, null, tint = AppColors.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay usuarios", color = AppColors.TextSecondary, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(users, key = { it.id }) { user ->
                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(if (user.role == UserRole.ADMIN) Color(0xFFFEF3C7) else Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Person, null, tint = if (user.role == UserRole.ADMIN) Color(0xFFD97706) else AppColors.Info, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                Text("ID: ${user.id}  |  PIN protegido", fontSize = 11.sp, color = AppColors.TextSecondary)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = if (user.role == UserRole.ADMIN) Color(0xFFFEF3C7) else AppColors.PrimaryLight) {
                                Text(user.role.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (user.role == UserRole.ADMIN) Color(0xFFD97706) else AppColors.Primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable { editing = user; showForm = true }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Edit, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(4.dp))
                            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.DangerLight).clickable { UsuariosManager.delete(user.id) }, contentAlignment = Alignment.Center) {
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
        UsuarioFormModal(
            usuario = editing,
            onSave = { u ->
                if (editing != null) UsuariosManager.update(u) else UsuariosManager.add(u)
                showForm = false
            },
            onDismiss = { showForm = false }
        )
    }

    if (showPermissions) {
        RolePermissionsModal(onDismiss = { showPermissions = false })
    }
}

@Composable
private fun RolePermissionsModal(onDismiss: () -> Unit) {
    var selectedRole by remember { mutableStateOf(UserRole.SUPERVISOR) }
    var selectedPermissions by remember(selectedRole) {
        mutableStateOf(AccessControl.permissionsForRole(selectedRole))
    }
    val roleIsAdmin = selectedRole == UserRole.ADMIN

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(680.dp).heightIn(max = 620.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Permisos por rol", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                    Text("Define que acciones puede ejecutar cada rol operativo.", color = AppColors.TextSecondary, fontSize = 12.sp)
                }
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.entries.forEach { role ->
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(if (selectedRole == role) AppColors.PrimaryLight else AppColors.Background)
                            .clickable { selectedRole = role }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Text(role.displayName, fontSize = 12.sp, fontWeight = if (selectedRole == role) FontWeight.Bold else FontWeight.Normal, color = if (selectedRole == role) AppColors.Primary else AppColors.TextSecondary)
                    }
                }
            }

            if (roleIsAdmin) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7)).padding(14.dp)) {
                    Text("Administrador siempre conserva todos los permisos para evitar bloquear el sistema.", color = Color(0xFF92400E), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AccessControl.Permission.entries, key = { it.name }) { permission ->
                    val checked = roleIsAdmin || permission in selectedPermissions
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            enabled = !roleIsAdmin,
                            onCheckedChange = { enabled ->
                                selectedPermissions = if (enabled) selectedPermissions + permission else selectedPermissions - permission
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(permission.title, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(permission.name, color = AppColors.TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        AccessControl.resetPermissionsForRole(selectedRole)
                        selectedPermissions = AccessControl.permissionsForRole(selectedRole)
                    },
                    enabled = !roleIsAdmin,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) { Text("Restaurar") }
                Button(
                    onClick = {
                        AccessControl.setPermissionsForRole(selectedRole, selectedPermissions)
                        onDismiss()
                    },
                    enabled = !roleIsAdmin,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) { Text("Guardar", color = Color.White) }
            }
        }
    }
}

@Composable
private fun UsuarioFormModal(
    usuario: Usuario?,
    onSave: (Usuario) -> Unit,
    onDismiss: () -> Unit
) {
    var id by remember(usuario) { mutableStateOf(usuario?.id ?: "") }
    var name by remember(usuario) { mutableStateOf(usuario?.name ?: "") }
    var pin by remember(usuario) { mutableStateOf("") }
    var password by remember(usuario) { mutableStateOf("") }
    var role by remember(usuario) { mutableStateOf(usuario?.role ?: UserRole.CAJERO) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(420.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (usuario != null) "Editar Usuario" else "Nuevo Usuario", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            UserField("Nombre", name) { name = it }
            UserField("ID de Usuario", id, enabled = usuario == null) { id = it }
            UserField(if (usuario == null) "PIN (4 digitos)" else "Nuevo PIN (opcional)", pin) { pin = it.take(4).filter { it.isDigit() } }
            UserField(if (usuario == null) "Contrasena" else "Nueva contrasena (opcional)", password) { password = it }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.entries.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { r ->
                            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (role == r) AppColors.PrimaryLight else AppColors.Background).clickable { role = r }.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(r.displayName, fontSize = 12.sp, fontWeight = if (role == r) FontWeight.Bold else FontWeight.Normal, color = if (role == r) AppColors.Primary else AppColors.TextSecondary)
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(44.dp)) { Text("Cancelar") }
                Button(
                    onClick = {
                        val creating = usuario == null
                        val pinValid = if (creating) pin.length == 4 else pin.isBlank() || pin.length == 4
                        val passwordValid = if (creating) password.length >= 4 else password.isBlank() || password.length >= 4
                        val pinAvailable = pin.isBlank() || !UsuariosManager.isPinInUse(pin, usuario?.id)
                        if (id.isNotBlank() && name.isNotBlank() && pinValid && passwordValid && pinAvailable) {
                            onSave(
                                (usuario ?: Usuario()).copy(
                                    id = id,
                                    name = name,
                                    pin = pin,
                                    password = password,
                                    role = role
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
private fun UserField(label: String, value: String, enabled: Boolean = true, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(value = value, enabled = enabled, onValueChange = onValueChange, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
        }
    }
}
