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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun PaymentSuccessModal(
    invoiceNumber: String,
    ncf: String,
    total: Double,
    printerName: String = "",
    onPrint: suspend () -> Unit,
    onNewSale: () -> Unit
) {
    var isPrinting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(780.dp).clip(RoundedCornerShape(22.dp)).background(AppColors.Surface)
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(36.dp)).background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(42.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text("Pago Completado", color = AppColors.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Factura", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Badge(text = invoiceNumber, backgroundColor = AppColors.Background, textColor = AppColors.TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NCF", color = AppColors.TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Badge(text = ncf, backgroundColor = AppColors.InfoLight, textColor = AppColors.Info)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "RD\$ ${"%,.2f".format(total)}",
                color = Color(0xFF16A34A),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            if (printerName.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(AppColors.PrimaryLight).padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text("Impresora: $printerName", color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.height(52.dp).clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
                        .clickable(enabled = !isPrinting) {
                            isPrinting = true
                            scope.launch {
                                onPrint()
                                isPrinting = false
                                onNewSale()
                            }
                        }.padding(horizontal = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPrinting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AppColors.TextPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Print, null, tint = AppColors.TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isPrinting) "Imprimiendo..." else "Imprimir",
                            color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Box(
                    modifier = Modifier.height(52.dp).clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Primary)
                        .clickable(onClick = onNewSale).padding(horizontal = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocalShipping, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Nueva Venta", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
