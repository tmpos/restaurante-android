package com.tmrestaurant.ui.screens.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.theme.AppColors

@Composable
internal fun FreeSaleModal(
    onDismiss: () -> Unit,
    onAdd: (name: String, amount: Double) -> Unit
) {
    var name by remember { mutableStateOf("Articulo no Registrado") }
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canAdd = amount > 0.0

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(460.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(AppColors.Surface)
                .clickable(onClick = {})
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.AttachMoney, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Venta libre", color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Agrega un articulo no registrado al carrito", color = AppColors.TextSecondary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(22.dp))
            FreeSaleInput(
                label = "Nombre",
                value = name,
                onValueChange = { name = it },
                placeholder = "Articulo no Registrado"
            )
            Spacer(Modifier.height(14.dp))
            FreeSaleInput(
                label = "Total",
                value = amountText,
                onValueChange = { value ->
                    if (value.isEmpty() || value.matches(Regex("""\d{0,8}(\.\d{0,2})?"""))) {
                        amountText = value
                    }
                },
                placeholder = "0.00",
                leadingText = "RD$",
                keyboardType = KeyboardType.Decimal
            )
            if (amountText.isNotEmpty() && !canAdd) {
                Spacer(Modifier.height(6.dp))
                Text("El total debe ser mayor que cero.", color = AppColors.Danger, fontSize = 12.sp)
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (canAdd) AppColors.Primary else AppColors.Gray)
                        .clickable(enabled = canAdd) { onAdd(name, amount) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Agregar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun FreeSaleInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                .background(AppColors.Background)
                .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingText != null) {
                Text(leadingText, color = AppColors.TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(placeholder, color = AppColors.Gray, fontSize = 14.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
