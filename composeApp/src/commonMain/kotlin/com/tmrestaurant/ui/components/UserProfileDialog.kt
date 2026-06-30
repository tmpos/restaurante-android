package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tmrestaurant.ui.data.Usuario
import com.tmrestaurant.ui.data.UsuariosManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun UserProfileDialog(
    userId: String,
    onDismiss: () -> Unit,
    onSaved: (Usuario) -> Unit
) {
    val user = UsuariosManager.usuarios.firstOrNull { it.id == userId } ?: return
    var name by remember(user.uid) { mutableStateOf(user.name) }
    var pin by remember(user.uid) { mutableStateOf("") }
    var password by remember(user.uid) { mutableStateOf("") }
    var confirmPassword by remember(user.uid) { mutableStateOf("") }
    var error by remember(user.uid) { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.52f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.width(460.dp).clip(RoundedCornerShape(22.dp))
                    .background(AppColors.Surface).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(54.dp).clip(CircleShape).background(AppColors.PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, null, tint = AppColors.Primary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Mi perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("Administra tus datos de acceso", fontSize = 12.sp, color = AppColors.TextSecondary)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(Icons.Outlined.Close, "Cerrar", modifier = Modifier.size(19.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Background).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Badge, null, tint = AppColors.IconGray)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Usuario", fontSize = 11.sp, color = AppColors.TextSecondary)
                    Text(user.id, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                }
                Text(
                    user.role.displayName,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(AppColors.PrimaryLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }

            ProfileField("Nombre completo", name, Icons.Outlined.Person) {
                name = it
                error = ""
            }
            ProfileField("Nuevo PIN (opcional)", pin, Icons.Outlined.Lock, password = true) {
                pin = it.filter(Char::isDigit).take(4)
                error = ""
            }
            ProfileField("Nueva contrasena (opcional)", password, Icons.Outlined.Lock, password = true) {
                password = it
                error = ""
            }
            ProfileField("Confirmar contrasena", confirmPassword, Icons.Outlined.Lock, password = true) {
                confirmPassword = it
                error = ""
            }

            if (error.isNotBlank()) {
                Text(error, color = AppColors.Danger, fontSize = 12.sp)
            }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(11.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            error = when {
                                name.isBlank() -> "El nombre es obligatorio."
                                pin.isNotBlank() && pin.length != 4 -> "El PIN debe tener 4 digitos."
                                pin.isNotBlank() && UsuariosManager.isPinInUse(pin, user.id) ->
                                    "Ese PIN ya pertenece a otro usuario."
                                password.isNotEmpty() && password.length < 4 ->
                                    "La contrasena debe tener al menos 4 caracteres."
                                password != confirmPassword -> "Las contrasenas no coinciden."
                                else -> ""
                            }
                            if (error.isEmpty()) {
                                val updated = user.copy(
                                    name = name.trim(),
                                    pin = pin,
                                    password = password
                                )
                                UsuariosManager.update(updated)
                                onSaved(UsuariosManager.usuarios.first { it.id == user.id })
                            }
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(11.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        Text("Guardar cambios", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    password: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(11.dp))
                .background(AppColors.Background).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = AppColors.IconGray, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
            )
        }
    }
}
