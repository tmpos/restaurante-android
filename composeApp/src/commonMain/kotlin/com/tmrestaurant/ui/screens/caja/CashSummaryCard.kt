package com.tmrestaurant.ui.screens.caja

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
fun CashSummaryCard(
    efectivoEnCaja: Double,
    inicial: Double,
    ventasEfectivo: Double,
    entradas: Double,
    gastos: Double,
    retiros: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Efectivo en Caja",
                    color = Color(0xFF6EE7B7),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                "RD\$ ${"%,.2f".format(efectivoEnCaja)}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF065F46)))

            CashRow("Inicial", inicial, Color(0xFFA7F3D0))
            CashRow("Ventas Efectivo", ventasEfectivo, Color(0xFF34D399))
            CashRow("Entradas", entradas, Color(0xFF34D399), Icons.Outlined.ArrowDownward)
            CashRow("Gastos", -gastos, Color(0xFFFCA5A5), Icons.Outlined.RemoveShoppingCart)
            CashRow("Retiros", -retiros, Color(0xFFFCA5A5), Icons.Outlined.ArrowUpward)
        }
    }
}

@Composable
private fun CashRow(
    label: String,
    amount: Double,
    amountColor: Color,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = amountColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            label,
            color = Color(0xFF6EE7B7),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            "RD\$ ${"%,.2f".format(amount)}",
            color = amountColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
