package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun Badge(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier,
    fontSize: Int = 11
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) AppColors.SuccessLight else AppColors.DangerLight
    val textColor = if (isActive) AppColors.Success else AppColors.Danger

    Badge(
        text = text,
        backgroundColor = bgColor,
        textColor = textColor,
        modifier = modifier
    )
}

@Composable
fun CategoryBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Badge(
        text = text,
        backgroundColor = AppColors.PrimaryLight,
        textColor = AppColors.Primary,
        modifier = modifier
    )
}

@Composable
fun NotificationDot(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.NotificationDot)
            .padding(4.dp)
    )
}

@Composable
fun CounterBadge(
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.BadgeRed
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(50))
                .background(backgroundColor)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
