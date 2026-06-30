package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.RestaurantMenu
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

private val DinerColors = listOf(
    Color(0xFF3B82F6), Color(0xFF22C55E), Color(0xFFF59E0B),
    Color(0xFFEF4444), Color(0xFF8B5CF6), Color(0xFFEC4899),
    Color(0xFF14B8A6), Color(0xFFF97316), Color(0xFF6366F1),
    Color(0xFF84CC16)
)

@Composable
fun SplitBillModal(
    cartItems: List<CartItem>,
    onApply: (items: List<CartItem>, dinerNames: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val dinerNames = remember { mutableStateListOf("Comensal 1", "Comensal 2") }
    val itemAssignments = remember {
        mutableStateListOf<Int>().apply {
            cartItems.forEach { add(1) }
        }
    }

    fun ensureDiners(count: Int) {
        while (dinerNames.size < count) {
            dinerNames.add("Comensal ${dinerNames.size + 1}")
        }
    }

    fun distributeEqually() {
        if (cartItems.isEmpty() || dinerNames.isEmpty()) return
        var dinerIdx = 1
        itemAssignments.indices.forEach { i ->
            itemAssignments[i] = dinerIdx
            dinerIdx = if (dinerIdx >= dinerNames.size) 1 else dinerIdx + 1
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(780.dp).heightIn(max = 680.dp)
                .clip(RoundedCornerShape(22.dp)).background(AppColors.Surface)
        ) {
            Column(
                Modifier.fillMaxWidth().background(Color(0xFFFFF7EA))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                            .background(AppColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.RestaurantMenu, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dividir Cuenta", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Asigne los productos a cada comensal", color = AppColors.TextSecondary, fontSize = 12.sp)
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

            Row(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Text("Comensales", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dinerNames.size) { idx ->
                            DinerCard(
                                index = idx,
                                name = dinerNames[idx],
                                itemCount = itemAssignments.count { it == idx + 1 },
                                totalAmount = cartItems.filterIndexed { i, _ -> itemAssignments[i] == idx + 1 }
                                    .sumOf { it.product.price * it.effectiveQuantity + it.extrasCost },
                                color = DinerColors[idx % DinerColors.size],
                                onNameChange = { dinerNames[idx] = it },
                                onRemove = { dinerNames.removeAt(idx) }
                            )
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                                    .clickable { dinerNames.add("Comensal ${dinerNames.size + 1}") },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Add, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Agregar comensal", color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Productos", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                        Box(
                            Modifier.height(30.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.PrimaryLight)
                                .clickable { distributeEqually() }.padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Dividir en partes iguales", color = AppColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(cartItems.size) { idx ->
                            val item = cartItems[idx]
                            val assignedDiner = itemAssignments[idx]
                            ItemAssignmentRow(
                                item = item,
                                assignedDinerIndex = assignedDiner,
                                dinerCount = dinerNames.size,
                                dinerColors = DinerColors,
                                onAssign = { newIdx ->
                                    itemAssignments[idx] = newIdx
                                }
                            )
                        }
                    }
                }
            }

            val totalsByDiner = dinerNames.indices.map { dIdx ->
                cartItems.filterIndexed { i, _ -> itemAssignments[i] == dIdx + 1 }
                    .sumOf { it.product.price * it.effectiveQuantity + it.extrasCost }
            }

            Box(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC)).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text("RESUMEN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    dinerNames.forEachIndexed { dIdx, name ->
                        val total = totalsByDiner[dIdx]
                        Row(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    Modifier.size(10.dp).clip(CircleShape)
                                        .background(DinerColors[dIdx % DinerColors.size])
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(name, fontSize = 13.sp, color = AppColors.TextPrimary)
                                Text(" (${itemAssignments.count { it == dIdx + 1 }} items)", fontSize = 11.sp, color = AppColors.TextSecondary)
                            }
                            Text("RD\$ ${"%,.2f".format(total)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                        }
                        if (dIdx < dinerNames.size - 1) Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                        Text("RD\$ ${"%,.2f".format(totalsByDiner.sum())}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.Primary)
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
                        .background(Color(0xFF16A34A)).clickable {
                            val updatedItems = cartItems.mapIndexed { i, item ->
                                item.copy(dinerIndex = itemAssignments[i])
                            }
                            onApply(updatedItems, dinerNames.toList())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Aplicar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DinerCard(
    index: Int,
    name: String,
    itemCount: Int,
    totalAmount: Double,
    color: Color,
    onNameChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Surface)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    textStyle = TextStyle(
                        color = AppColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("$itemCount items", fontSize = 11.sp, color = AppColors.TextSecondary, modifier = Modifier.weight(1f))
                Text("RD\$ ${"%,.2f".format(totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
            }
            if (index >= 2) {
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.align(Alignment.End).size(26.dp).clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEE2E2)).clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun ItemAssignmentRow(
    item: CartItem,
    assignedDinerIndex: Int,
    dinerCount: Int,
    dinerColors: List<Color>,
    onAssign: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.CartItemBg).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.product.name, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val itemQtyLabel = if (item.weightQuantity > 0) "${item.weightQuantity} lbs" else "x${item.quantity}"
            Text("$itemQtyLabel  RD\$ ${"%,.2f".format(item.product.price * item.effectiveQuantity + item.extrasCost)}", color = AppColors.TextSecondary, fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..dinerCount).forEach { dIdx ->
                val isSelected = assignedDinerIndex == dIdx
                val dc = dinerColors[(dIdx - 1) % dinerColors.size]
                Box(
                    Modifier.size(32.dp).clip(CircleShape)
                        .background(if (isSelected) dc else Color(0xFFE5E7EB))
                        .clickable { onAssign(dIdx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$dIdx",
                        color = if (isSelected) Color.White else AppColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
