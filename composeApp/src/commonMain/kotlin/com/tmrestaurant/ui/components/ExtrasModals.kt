package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun DescuentoModal(
    subtotal: Double,
    onApply: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf("porcentaje") }
    var valueText by remember { mutableStateOf("") }

    val discountAmount = when (selectedOption) {
        "porcentaje" -> subtotal * (valueText.toDoubleOrNull() ?: 0.0) / 100.0
        "fijo" -> valueText.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(360.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Percent, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Aplicar Descuento", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("porcentaje" to "%", "fijo" to "RD$").forEach { (opt, label) ->
                    Box(Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(10.dp)).background(if (selectedOption == opt) AppColors.PrimaryLight else AppColors.Background).clickable { selectedOption = opt }, contentAlignment = Alignment.Center) {
                        Text(if (opt == "porcentaje") "Porcentaje" else "Monto fijo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (selectedOption == opt) AppColors.Primary else AppColors.TextSecondary)
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedOption == "porcentaje") "%" else "RD$", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(value = valueText, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) valueText = it }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
            }

            if (discountAmount > 0) {
                Text("Descuento: RD\$ ${"%.2f".format(discountAmount)}  →  Total: RD\$ ${"%.2f".format(subtotal - discountAmount)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(44.dp)) { Text("Cancelar") }
                Button(onClick = {
                    val v = if (selectedOption == "porcentaje") "$valueText%" else "RD\$$valueText"
                    if (valueText.isNotBlank() && discountAmount > 0) onApply(v, discountAmount)
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary), modifier = Modifier.weight(1f).height(44.dp)) { Text("Aplicar", color = Color.White) }
            }
        }
    }
}

@Composable
fun DeliveryModal(
    onApply: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(400.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocalShipping, null, tint = AppColors.Orange, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Delivery / Para Llevar", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Field("Direccion de entrega", direccion) { direccion = it }
            Field("Telefono", telefono) { telefono = it }
            Field("Notas (ej: apto 3B, timbre roto)", notas, height = 70) { notas = it }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(44.dp)) { Text("Cancelar") }
                Button(onClick = { onApply(direccion, telefono, notas) }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Orange), modifier = Modifier.weight(1f).height(44.dp)) { Text("Aplicar", color = Color.White) }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, height: Int = 44, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = height <= 44)
        }
    }
}
