package com.tmrestaurant.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.CartItem
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ReturnModal(
    invoiceNumber: String,
    items: List<CartItem>,
    onConfirm: (List<ReturnedItem>) -> Unit,
    onDismiss: () -> Unit
) {
    val returnQuantities = remember {
        mutableStateListOf<Int>().apply {
            items.forEach { add(it.quantity) }
        }
    }
    val reasons = remember {
        mutableStateListOf<String>().apply {
            items.forEach { add("") }
        }
    }

    val totalRefund = items.indices.sumOf { idx ->
        val qty = returnQuantities[idx].coerceIn(0, items[idx].quantity)
        qty * items[idx].product.price
    }

    val hasReturns = returnQuantities.any { it > 0 }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(580.dp).heightIn(max = 680.dp)
                .clip(RoundedCornerShape(22.dp)).background(AppColors.Surface)
        ) {
            Column(
                Modifier.fillMaxWidth().background(Color(0xFFFFF7EA))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Receipt, null, tint = AppColors.Danger, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Devolucion / Reembolso", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Factura: $invoiceNumber", color = AppColors.TextSecondary, fontSize = 12.sp)
                    }
                    Box(
                        Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Surface)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Column(
                Modifier.weight(1f).fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Seleccione los productos a devolver:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(4.dp))

                items.forEachIndexed { idx, item ->
                    val maxQty = item.quantity
                    val returnQty = returnQuantities[idx].coerceIn(0, maxQty)
                    val itemRefund = returnQty * item.product.price

                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(if (returnQty > 0) Color(0xFFF0FDF4) else AppColors.CartItemBg)
                            .border(1.dp, if (returnQty > 0) Color(0xFFBBF7D0) else AppColors.Border, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(item.product.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("RD\$ ${"%,.2f".format(item.product.price)} x $maxQty", fontSize = 11.sp, color = AppColors.TextSecondary)
                                if (returnQty > 0) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("A devolver: $returnQty  |  Reembolso: RD\$ ${"%,.2f".format(itemRefund)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A))
                                }
                                Spacer(Modifier.height(4.dp))
                                BasicTextField(
                                    value = reasons[idx],
                                    onValueChange = { reasons[idx] = it },
                                    textStyle = TextStyle(color = AppColors.TextSecondary, fontSize = 11.sp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        Box(
                                            Modifier.fillMaxWidth().height(32.dp)
                                                .clip(RoundedCornerShape(6.dp)).background(Color.White)
                                                .border(0.5.dp, AppColors.Border, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (reasons[idx].isEmpty()) {
                                                Text("Motivo (opcional)", color = AppColors.Gray, fontSize = 11.sp)
                                            }
                                            inner()
                                        }
                                    }
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                        .background(if (returnQty > 0) AppColors.DangerLight else AppColors.Background)
                                        .clickable {
                                            returnQuantities[idx] = (returnQty - 1).coerceAtLeast(0)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = if (returnQty > 0) AppColors.Danger else AppColors.Gray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "${returnQty}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary,
                                    modifier = Modifier.width(30.dp),
                                    textAlign = TextAlign.Center
                                )
                                Box(
                                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                        .background(if (returnQty < maxQty) Color(0xFFDCFCE7) else AppColors.Background)
                                        .clickable {
                                            returnQuantities[idx] = (returnQty + 1).coerceAtMost(maxQty)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = if (returnQty < maxQty) Color(0xFF16A34A) else AppColors.Gray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC)).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("RESUMEN DE DEVOLUCION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Text("Productos a devolver:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("${returnQuantities.sum()} unidades", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Text("Total a reembolsar:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("RD\$ ${"%,.2f".format(totalRefund)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Danger)
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.weight(2f).height(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (hasReturns) AppColors.Danger else Color(0xFFFECACA))
                        .clickable(enabled = hasReturns) {
                            val returned = items.indices.mapNotNull { idx ->
                                val qty = returnQuantities[idx].coerceIn(0, items[idx].quantity)
                                if (qty > 0) ReturnedItem(
                                    productId = items[idx].product.id,
                                    productName = items[idx].product.name,
                                    quantity = qty,
                                    refundAmount = qty * items[idx].product.price,
                                    reason = reasons[idx]
                                ) else null
                            }
                            onConfirm(returned)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Procesar Devolucion", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
