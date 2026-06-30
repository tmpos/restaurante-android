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
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.Category
import com.tmrestaurant.ui.data.CategoryColorType
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun CategoryCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lineColor = when (category.colorType) {
        CategoryColorType.Orange -> AppColors.Orange
        CategoryColorType.Purple -> AppColors.Primary
        CategoryColorType.Green -> AppColors.Green
        CategoryColorType.Gray -> AppColors.Gray
    }
    val iconBgColor = when (category.colorType) {
        CategoryColorType.Orange -> AppColors.OrangeLight
        CategoryColorType.Purple -> AppColors.PrimaryLight
        CategoryColorType.Green -> AppColors.SuccessLight
        CategoryColorType.Gray -> AppColors.Background
    }

    Column(
        modifier = modifier
            .width(320.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = AppColors.CardShadow, spotColor = AppColors.CardShadow)
            .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(lineColor)
        )

        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Category,
                        contentDescription = null,
                        tint = lineColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        color = AppColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = category.description,
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(text = if (category.active) "Activa" else "Inactiva", isActive = category.active)
                if (category.visiblePos) {
                    Badge(
                        text = "Visible POS",
                        backgroundColor = AppColors.InfoLight,
                        textColor = AppColors.Info,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppColors.DividerColor)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Orden: ${category.order}",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.PrimaryLight)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = AppColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.DangerLight)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = AppColors.Danger,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
