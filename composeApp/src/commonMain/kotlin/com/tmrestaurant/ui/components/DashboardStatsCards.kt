package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun MainStatsRow(
    ventasDia: String = "RD\$ 0.00",
    ordenesAbiertas: String = "0",
    enCocina: String = "0",
    facturasEmitidas: String = "0",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MainStatCard(
            icon = Icons.Outlined.AttachMoney,
            iconBg = Color(0xFFDCFCE7),
            iconTint = Color(0xFF16A34A),
            value = ventasDia,
            label = "Ventas del dia",
            badge = "HOY",
            badgeBg = Color(0xFFDCFCE7),
            badgeFg = Color(0xFF16A34A),
            circleColor = Color(0xFF22C55E).copy(alpha = 0.08f),
            modifier = Modifier.weight(1f)
        )
        MainStatCard(
            icon = Icons.Outlined.ShoppingCart,
            iconBg = Color(0xFFDBEAFE),
            iconTint = AppColors.Info,
            value = ordenesAbiertas,
            label = "Ordenes activas",
            badge = "ACTIVAS",
            badgeBg = Color(0xFFDBEAFE),
            badgeFg = AppColors.Info,
            circleColor = AppColors.Info.copy(alpha = 0.08f),
            modifier = Modifier.weight(1f)
        )
        MainStatCard(
            icon = Icons.Outlined.RestaurantMenu,
            iconBg = Color(0xFFFFF3E0),
            iconTint = AppColors.Orange,
            value = enCocina,
            label = "En cocina",
            badge = "PREPARANDO",
            badgeBg = Color(0xFFFFF3E0),
            badgeFg = AppColors.Orange,
            circleColor = AppColors.Orange.copy(alpha = 0.08f),
            modifier = Modifier.weight(1f)
        )
        MainStatCard(
            icon = Icons.Outlined.Receipt,
            iconBg = AppColors.PrimaryLight,
            iconTint = AppColors.Primary,
            value = facturasEmitidas,
            label = "Facturas emitidas",
            badge = "$facturasEmitidas",
            badgeBg = AppColors.PrimaryLight,
            badgeFg = AppColors.Primary,
            circleColor = AppColors.Primary.copy(alpha = 0.08f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MainStatCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String,
    badge: String,
    badgeBg: Color,
    badgeFg: Color,
    circleColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(circleColor)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 30.dp)
        )
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Badge(text = badge, backgroundColor = badgeBg, textColor = badgeFg, fontSize = 9)
            }
            Spacer(Modifier.weight(1f))
            Text(value, color = AppColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun SecondaryStatsRow(
    efectivo: String = "RD\$ 0.00",
    tarjeta: String = "RD\$ 0.00",
    transferencia: String = "RD\$ 0.00",
    mesasOcupadas: String = "0 / 0",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SmallStatCard(
            icon = Icons.Outlined.AttachMoney,
            iconBg = Color(0xFFDCFCE7),
            iconTint = Color(0xFF16A34A),
            value = efectivo,
            label = "Efectivo",
            modifier = Modifier.weight(1f)
        )
        SmallStatCard(
            icon = Icons.Outlined.CreditCard,
            iconBg = Color(0xFFDBEAFE),
            iconTint = AppColors.Info,
            value = tarjeta,
            label = "Tarjeta",
            modifier = Modifier.weight(1f)
        )
        SmallStatCard(
            icon = Icons.Outlined.AttachMoney,
            iconBg = AppColors.PrimaryLight,
            iconTint = AppColors.Primary,
            value = transferencia,
            label = "Transferencia",
            modifier = Modifier.weight(1f)
        )
        SmallStatCard(
            icon = Icons.Outlined.ShoppingCart,
            iconBg = Color(0xFFFFF3E0),
            iconTint = AppColors.Orange,
            value = mesasOcupadas,
            label = "Mesas ocupadas",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SmallStatCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(value, color = AppColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        }
    }
}
