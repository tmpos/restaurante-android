package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.RecentOrder
import com.tmrestaurant.ui.data.TopSelling
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun DashboardSectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onViewMore: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.Surface)
            .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Ver mas →", color = AppColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onViewMore))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
        Box(Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun TopSellingList(
    items: List<TopSelling>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (items.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Inventory2, null, tint = AppColors.Border, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Sin productos vendidos hoy", color = AppColors.TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.name, color = AppColors.TextPrimary, fontSize = 14.sp,
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Badge(text = "${item.qtySold} ventas", backgroundColor = AppColors.PrimaryLight, textColor = AppColors.Primary)
                }
            }
        }
    }
}

@Composable
fun RecentOrdersList(
    orders: List<RecentOrder>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        orders.forEach { order ->
            RecentOrderItem(order = order)
        }
    }
}

@Composable
fun RecentOrderItem(
    order: RecentOrder,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeFg) = when (order.status) {
        "CERRADA" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "ABIERTA" -> Color(0xFFDBEAFE) to AppColors.Info
        "EN COCINA" -> Color(0xFFFFF3E0) to AppColors.Orange
        else -> AppColors.Background to AppColors.TextSecondary
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(order.id, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AccessTime, null, tint = AppColors.Gray, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(order.time, color = AppColors.TextSecondary, fontSize = 11.sp)
            }
        }
        Badge(text = order.status, backgroundColor = badgeBg, textColor = badgeFg, fontSize = 9)
        Spacer(Modifier.width(16.dp))
        Text("RD\$ ${"%.2f".format(order.total)}", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
