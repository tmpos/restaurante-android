package com.tmrestaurant.ui.screens.caja

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun LatestSalesCard(
    invoices: List<PaymentResult>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Receipt,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Ultimas Ventas",
                    color = AppColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier.background(
                        color = AppColors.PrimaryLight,
                        shape = RoundedCornerShape(8.dp)
                    ).padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${invoices.size}",
                        color = AppColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border.copy(alpha = 0.4f))
            )

            if (invoices.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.Receipt,
                            contentDescription = null,
                            tint = AppColors.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No hay ventas en este turno",
                            color = AppColors.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(invoices.take(20)) { inv ->
                        InvoiceRow(inv)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(invoice: PaymentResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                invoice.invoiceNumber,
                color = AppColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                invoice.paymentMethod,
                color = AppColors.TextSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
        Text(
            "RD$ ${"%,.2f".format(invoice.total)}",
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
