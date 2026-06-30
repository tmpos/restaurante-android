package com.tmrestaurant.ui.screens.caja

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButtons(
    onEntrada: () -> Unit,
    onRetiro: () -> Unit,
    onGasto: () -> Unit,
    onCerrarCaja: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            label = "Entrada",
            icon = Icons.Outlined.ArrowDownward,
            bgColor = Color(0xFF065F46),
            iconColor = Color(0xFF34D399),
            onClick = onEntrada,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            label = "Retiro",
            icon = Icons.Outlined.ArrowUpward,
            bgColor = Color(0xFF7C2D12),
            iconColor = Color(0xFFFCA5A5),
            onClick = onRetiro,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            label = "Gasto",
            icon = Icons.Outlined.RemoveShoppingCart,
            bgColor = Color(0xFF451A03),
            iconColor = Color(0xFFFDBA74),
            onClick = onGasto,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            label = "Cerrar Caja",
            icon = Icons.Outlined.ContentPaste,
            bgColor = Color(0xFF4F46E5),
            iconColor = Color(0xFFA5B4FC),
            onClick = onCerrarCaja,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
