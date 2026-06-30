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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.HeldOrder
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun RecallOrdersModal(
    heldOrders: List<HeldOrder>,
    onRecall: (HeldOrder) -> Unit,
    onDelete: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(520.dp).heightIn(max = 600.dp)
                .clip(RoundedCornerShape(22.dp)).background(AppColors.Surface)
        ) {
            Column(
                Modifier.fillMaxWidth().background(Color(0xFFFFF7EA))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Pause, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Ordenes Suspendidas", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${heldOrders.size} orden(es) guardada(s)", color = AppColors.TextSecondary, fontSize = 12.sp)
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

            if (heldOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Pause, null, tint = AppColors.Border, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No hay ordenes suspendidas", color = AppColors.TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(heldOrders) { order ->
                        val idx = heldOrders.indexOf(order)
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                                .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.ShoppingCart, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(order.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextPrimary)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(order.label, fontSize = 12.sp, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (order.clientName.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.Person, null, tint = AppColors.IconGray, modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(order.clientName, fontSize = 11.sp, color = AppColors.TextSecondary)
                                        }
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    Modifier.height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.PrimaryLight)
                                        .clickable { onRecall(order) }.padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Recuperar", color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2))
                                        .clickable { onDelete(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
