package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.Corte
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun CorteModal(
    corte: Corte,
    onDismiss: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(480.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ContentPaste, null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Corte de Caja", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                    Text("Turno: ${corte.turno.id.take(10)}...", fontSize = 12.sp, color = AppColors.TextSecondary)
                }
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Text("Cajero:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(corte.turno.userName, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("Inicio:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(formatDateTime(corte.turno.startTime), color = AppColors.TextPrimary, fontSize = 13.sp)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("Efectivo Inicial:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("RD\$ ${"%,.2f".format(corte.turno.initialAmount)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("RESUMEN DE VENTAS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CorteRow("Facturas Emitidas", "${corte.invoiceCount}")
                    HorizontalDivider2()
                    CorteRow("Total Efectivo", "RD\$ ${"%,.2f".format(corte.totalEfectivo)}")
                    CorteRow("Total Tarjeta", "RD\$ ${"%,.2f".format(corte.totalTarjeta)}")
                    CorteRow("Total Transferencia", "RD\$ ${"%,.2f".format(corte.totalTransferencia)}")
                    CorteRow("Total Credito", "RD\$ ${"%,.2f".format(corte.totalCredito)}")
                    HorizontalDivider2()
                    CorteRow("Total Ventas", "RD\$ ${"%,.2f".format(corte.totalVentas)}", bold = true)
                    CorteRow("Articulos Vendidos", "${corte.totalArticulos}")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("EFECTIVO EN CAJA", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CorteRow("Inicial", "RD\$ ${"%,.2f".format(corte.turno.initialAmount)}")
                    CorteRow("+ Ventas Efectivo", "RD\$ ${"%,.2f".format(corte.totalEfectivo)}")
                    if (corte.totalEntradas > 0) {
                        CorteRow("+ Entradas", "RD\$ ${"%,.2f".format(corte.totalEntradas)}")
                    }
                    if (corte.totalGastos > 0) {
                        CorteRow("- Gastos", "RD\$ ${"%,.2f".format(corte.totalGastos)}")
                    }
                    if (corte.totalRetiros > 0) {
                        CorteRow("- Retiros", "RD\$ ${"%,.2f".format(corte.totalRetiros)}")
                    }
                    HorizontalDivider2()
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFDCFCE7)).padding(12.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text("Esperado en Caja:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF16A34A), modifier = Modifier.weight(1f))
                            Text("RD\$ ${"%,.2f".format(corte.expectedCash)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF16A34A))
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text("Cerrar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CorteRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
    }
}

@Composable
private fun HorizontalDivider2() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border.copy(alpha = 0.5f)))
}
