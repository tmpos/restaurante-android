package com.tmrestaurant.ui.screens.caja

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun SalesSummaryCard(
    totalVentas: Double,
    facturas: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Total Ventas del Turno",
                            color = Color(0xFF92400E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "RD$ ${"%,.2f".format(totalVentas)}",
                            color = Color(0xFF78350F),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$facturas",
                    color = Color(0xFF78350F),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (facturas == 1) "factura" else "facturas",
                    color = Color(0xFF92400E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
