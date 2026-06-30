package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun TurnosScreen() {
    val turnoActivo = TurnoManager.currentTurno
    val historial = TurnoManager.closedTurnos.reversed()

    Column(
        Modifier.fillMaxSize().background(AppColors.Background).padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE0E7FF)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ContentPaste, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Turnos", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppColors.TextPrimary)
                Text("Gestion de turnos", color = AppColors.TextSecondary, fontSize = 13.sp)
            }
        }

        // Active Turno
        if (turnoActivo != null) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppColors.Surface).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("TURNO ACTIVO", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF16A34A))
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) { Text("ID:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text(turnoActivo.id, color = AppColors.TextPrimary, fontSize = 13.sp) }
                Row(Modifier.fillMaxWidth()) { Text("Cajero:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text(turnoActivo.userName, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                Row(Modifier.fillMaxWidth()) { Text("Inicio:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text(formatDateTime(turnoActivo.startTime), color = AppColors.TextPrimary, fontSize = 13.sp) }
                Row(Modifier.fillMaxWidth()) { Text("Inicial:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(turnoActivo.initialAmount)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            }
        } else {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppColors.Surface).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Close, null, tint = AppColors.Gray, modifier = Modifier.size(28.dp))
                Text("No hay turno activo", color = AppColors.TextSecondary, fontSize = 14.sp)
                Text("Inicie sesion para abrir un turno", color = AppColors.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }

        // Turno History
        if (historial.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.History, null, tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("HISTORIAL DE TURNOS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextSecondary)
            }
            historial.take(20).forEach { t ->
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(Modifier.fillMaxWidth()) { Text("Cajero:", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(t.userName, color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    Row(Modifier.fillMaxWidth()) { Text("Inicio:", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(formatDateTime(t.startTime), color = AppColors.TextPrimary, fontSize = 12.sp) }
                    if (t.endTime != null) {
                        Row(Modifier.fillMaxWidth()) { Text("Fin:", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(formatDateTime(t.endTime), color = AppColors.TextPrimary, fontSize = 12.sp) }
                    }
                    Row(Modifier.fillMaxWidth()) { Text("Inicial:", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(t.initialAmount)}", color = AppColors.TextPrimary, fontSize = 12.sp) }
                }
            }
        } else {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay turnos cerrados", color = AppColors.Gray, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}
