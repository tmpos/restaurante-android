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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.data.InvoiceHistory
import com.tmrestaurant.ui.theme.AppColors

private val deliveryStates = listOf("TODOS", "PENDIENTE", "PREPARANDO", "EN_RUTA", "ENTREGADO")

@Composable
fun DeliveryScreen() {
    var selectedStatus by remember { mutableStateOf("TODOS") }
    val deliveries = InvoiceHistory.invoices
        .filter { it.deliveryAddress.isNotBlank() || it.deliveryPhone.isNotBlank() }
        .filter { selectedStatus == "TODOS" || it.deliveryStatus.ifBlank { "PENDIENTE" } == selectedStatus }

    Column(Modifier.fillMaxSize().background(AppColors.Background).padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Delivery", color = AppColors.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Despacho y seguimiento de pedidos a domicilio", color = AppColors.TextSecondary, fontSize = 13.sp)
            }
            Icon(Icons.Outlined.LocalShipping, null, tint = AppColors.Orange, modifier = Modifier.size(32.dp))
        }

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            deliveryStates.forEach { status ->
                StatusFilterChip(status, selectedStatus == status) { selectedStatus = status }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (deliveries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay pedidos de delivery en este estado", color = AppColors.TextSecondary, fontSize = 15.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(deliveries, key = { it.invoiceNumber }) { invoice ->
                    DeliveryCard(invoice)
                }
            }
        }
    }
}

@Composable
private fun DeliveryCard(invoice: PaymentResult) {
    val status = invoice.deliveryStatus.ifBlank { "PENDIENTE" }
    val nextStatus = when (status) {
        "PENDIENTE" -> "PREPARANDO"
        "PREPARANDO" -> "EN_RUTA"
        "EN_RUTA" -> "ENTREGADO"
        else -> ""
    }
    val accent = when (status) {
        "PENDIENTE" -> AppColors.Orange
        "PREPARANDO" -> AppColors.Info
        "EN_RUTA" -> AppColors.Primary
        "ENTREGADO" -> AppColors.Success
        else -> AppColors.TextSecondary
    }

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(if (status == "ENTREGADO") Icons.Outlined.CheckCircle else Icons.Outlined.LocalShipping, null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(invoice.invoiceNumber, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(8.dp))
                StatusBadge(status, accent)
            }
            Text(formatDateTime(invoice.timestamp), color = AppColors.TextSecondary, fontSize = 10.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Place, null, tint = AppColors.TextSecondary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.size(5.dp))
                Text(invoice.deliveryAddress.ifBlank { "Sin direccion" }, color = AppColors.TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Phone, null, tint = AppColors.TextSecondary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.size(5.dp))
                Text(invoice.deliveryPhone.ifBlank { "Sin telefono" }, color = AppColors.TextSecondary, fontSize = 12.sp)
            }
            if (invoice.deliveryNotes.isNotBlank()) {
                Text(invoice.deliveryNotes, color = AppColors.TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("RD$ ${"%,.2f".format(invoice.total)}", color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (nextStatus.isNotBlank()) {
                Box(
                    Modifier.height(38.dp).clip(RoundedCornerShape(10.dp)).background(accent)
                        .clickable { InvoiceHistory.updateDeliveryStatus(invoice.invoiceNumber, nextStatus) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(nextStatus.replace("_", " "), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatusFilterChip(status: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.height(38.dp).clip(RoundedCornerShape(10.dp))
            .background(if (selected) AppColors.Primary else AppColors.Surface)
            .border(1.dp, if (selected) AppColors.Primary else AppColors.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(status.replace("_", " "), color = if (selected) Color.White else AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusBadge(status: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(7.dp)).background(color.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(status.replace("_", " "), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
