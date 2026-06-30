package com.tmrestaurant.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.ImageFromBytes

private val Orange = Color(0xFFF97316)

@Composable
fun BrandPanel(
    companyName: String,
    companyLogo: ByteArray?,
    modifier: Modifier = Modifier
) {
    val displayName = companyName.ifBlank { "TM-RESTAURANTE" }

    Box(
        modifier
            .fillMaxHeight()
            .background(
                Brush.linearGradient(listOf(Color(0xE6141B2C), Color(0xE61A1F3A))),
                RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
            )
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 36.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (companyLogo != null && companyLogo.isNotEmpty()) {
                Box(
                    Modifier.width(220.dp).height(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.96f))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ImageFromBytes(
                        bytes = companyLogo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        cacheKey = "login-company-logo"
                    )
                }
            } else {
                Box(
                    Modifier.size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(listOf(Orange, Color(0xFFEA580C))),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("TM", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                displayName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Sistema de Punto de Venta",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(36.dp))

            BenefitItem(
                icon = Icons.Outlined.Bolt,
                title = "Rápido y Eficiente",
                subtitle = "Optimizado para alta velocidad"
            )
            Spacer(Modifier.height(22.dp))
            BenefitItem(
                icon = Icons.Outlined.Shield,
                title = "100% Seguro",
                subtitle = "Datos protegidos y encriptados"
            )
            Spacer(Modifier.height(22.dp))
            BenefitItem(
                icon = Icons.Outlined.TrendingUp,
                title = "Control Total",
                subtitle = "Gestiona ventas y reportes"
            )
        }
    }
}

@Composable
private fun BenefitItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x1AF97316)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
    }
}
