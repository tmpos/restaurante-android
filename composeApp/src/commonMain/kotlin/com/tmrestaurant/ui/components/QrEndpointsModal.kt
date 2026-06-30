package com.tmrestaurant.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.wifi.WifiMenuServer
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun QrEndpointsModal(onDismiss: () -> Unit) {
    val ip = WifiMenuServer.ipAddress.ifBlank { "192.168.1.x" }
    val port = WifiMenuServer.port

    val endpoints = listOf(
        "Menu Web" to "http://$ip:$port",
        "API Menu" to "http://$ip:$port/api/menu",
        "API Mesas" to "http://$ip:$port/api/mesas",
        "API Mesa/{id}" to "http://$ip:$port/api/mesa/1",
        "API Comandas" to "http://$ip:$port/api/comandas",
    )

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(560.dp).clip(RoundedCornerShape(24.dp)).background(AppColors.Surface).padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.QrCode, null, tint = AppColors.Primary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Codigos QR - Endpoints", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                    Text("Escanee para acceder desde otro dispositivo", fontSize = 11.sp, color = AppColors.TextSecondary)
                }
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(18.dp))
                }
            }

            if (!WifiMenuServer.isRunning) {
                Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Warning, null, tint = Color(0xFFD97706), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("El servidor WiFi no esta activo", color = Color(0xFFD97706), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Active el servidor desde la barra superior", color = AppColors.TextSecondary, fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(endpoints) { (label, url) ->
                        QrEndpointCard(label = label, url = url)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(Modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).clickable(onClick = onDismiss).padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                    Text("Cerrar", color = AppColors.TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun QrEndpointCard(label: String, url: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Background),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            // QR Code
            Box(Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)) {
                QrCodeCanvas(url = url, modifier = Modifier.fillMaxSize().padding(4.dp))
            }

            Spacer(Modifier.width(18.dp))

            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(url, fontSize = 10.sp, color = AppColors.TextSecondary, maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = AppColors.PrimaryLight) {
                    Text(
                        if (label.contains("Menu")) "Menu Digital" else "REST API",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Icon(Icons.Outlined.QrCode, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun QrCodeCanvas(url: String, modifier: Modifier = Modifier) {
    val matrix = remember(url) { generateQrMatrix(url, 25) }

    Canvas(modifier = modifier) {
        val size = this.size.minDimension
        val cellSize = size / matrix.size
        val offsetX = (this.size.width - cellSize * matrix.size) / 2
        val offsetY = (this.size.height - cellSize * matrix.size) / 2

        // White background
        drawRect(Color.White, Offset(offsetX, offsetY), Size(cellSize * matrix.size, cellSize * matrix.size))

        // Finder patterns (top-left, top-right, bottom-left)
        drawFinder(offsetX, offsetY, cellSize)
        drawFinder(offsetX + cellSize * (matrix.size - 7), offsetY, cellSize)
        drawFinder(offsetX, offsetY + cellSize * (matrix.size - 7), cellSize)

        // Data modules
        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                val inFinder = (r < 8 && c < 8) || (r < 8 && c >= matrix.size - 8) || (r >= matrix.size - 8 && c < 8)
                if (!inFinder && matrix[r][c]) {
                    drawRect(
                        Color.Black,
                        Offset(offsetX + c * cellSize, offsetY + r * cellSize),
                        Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFinder(x: Float, y: Float, cellSize: Float) {
    // Outer square
    drawRect(Color.Black, Offset(x, y), Size(cellSize * 7, cellSize * 7))
    // Inner white
    drawRect(Color.White, Offset(x + cellSize, y + cellSize), Size(cellSize * 5, cellSize * 5))
    // Core black square
    drawRect(Color.Black, Offset(x + cellSize * 2, y + cellSize * 2), Size(cellSize * 3, cellSize * 3))
}

private fun generateQrMatrix(text: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) }
    val data = text.encodeToByteArray()

    var row = 0; var col = 0
    for (byte in data) {
        for (bit in 7 downTo 0) {
            if (row < size && col < size) {
                // Skip finder pattern areas
                if (!isFinderArea(row, col, size)) {
                    matrix[row][col] = (byte.toInt() shr bit) and 1 == 1
                }
            }
            col++
            if (col >= size) { col = 0; row++ }
        }
        if (row >= size) break
    }

    // Fill remaining with alternating pattern
    val rng = java.util.Random(text.hashCode().toLong())
    for (r in matrix.indices) {
        for (c in matrix[r].indices) {
            if (!isFinderArea(r, c, size)) {
                matrix[r][c] = rng.nextBoolean()
            }
        }
    }

    return matrix
}

private fun isFinderArea(r: Int, c: Int, size: Int): Boolean =
    (r < 8 && c < 8) || (r < 8 && c >= size - 8) || (r >= size - 8 && c < 8)
