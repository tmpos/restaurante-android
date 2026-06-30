package com.tmrestaurant.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Orange = Color(0xFFF97316)

@Composable
fun CredentialsLoginPanel(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Ingresa tus credenciales",
            color = Color(0xFF64748B),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(4.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Usuario", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Box(
                Modifier.fillMaxWidth().height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (username.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Ingresa tu usuario", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                }
                BasicTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 15.sp),
                    singleLine = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Contraseña", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Box(
                Modifier.fillMaxWidth().height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (password.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Lock, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Ingresa tu contraseña", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                }
                BasicTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 15.sp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier.fillMaxWidth().height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (username.isNotBlank() && password.isNotBlank()) Orange
                    else Color(0xFFF1F5F9)
                )
                .clickable(enabled = username.isNotBlank() && password.isNotBlank(), onClick = onLogin),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Iniciar Sesión",
                color = if (username.isNotBlank() && password.isNotBlank()) Color.White
                else Color(0xFF94A3B8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
