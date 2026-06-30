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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.QuickAccess
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun QuickAccessRow(
    items: List<QuickAccess>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items.forEach { item ->
            QuickAccessCard(item = item, onClick = { onItemClick(item.title) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickAccessCard(item: QuickAccess, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon = iconForType(item.iconType)
    val color = colorForType(item.colorType)
    val lightBg = color.copy(alpha = 0.12f)

    Column(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(lightBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(item.title, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun iconForType(type: String): ImageVector = when (type) {
    "cart" -> Icons.Outlined.ShoppingCart
    "chef" -> Icons.Outlined.RestaurantMenu
    "grid" -> Icons.Outlined.GridView
    "box" -> Icons.Outlined.Inventory2
    "people" -> Icons.Outlined.People
    "money" -> Icons.Outlined.AttachMoney
    "receipt" -> Icons.Outlined.Receipt
    else -> Icons.Outlined.ShoppingCart
}

@Composable
private fun colorForType(type: String): Color = when (type) {
    "purple" -> AppColors.Primary
    "orange" -> AppColors.Orange
    "blue" -> AppColors.Info
    "green" -> AppColors.Green
    "red" -> AppColors.Danger
    else -> AppColors.Primary
}
