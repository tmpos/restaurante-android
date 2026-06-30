package com.tmrestaurant.ui.screens.comandas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.Comanda
import com.tmrestaurant.ui.data.ComandaStatus
import com.tmrestaurant.ui.data.ComandasManager
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun ComandasScreen() {
    val comandas = ComandasManager.activeComandas
    var filterArea by remember { mutableStateOf<String?>(null) }
    var filterCourse by remember { mutableStateOf<String?>(null) }
    var previousCount by remember { mutableStateOf(comandas.size) }
    var newOrderFlash by remember { mutableStateOf(false) }

    LaunchedEffect(comandas.size) {
        if (comandas.size > previousCount) {
            newOrderFlash = true
            delay(150)
            newOrderFlash = false
        }
        previousCount = comandas.size
    }

    val pendientes = comandas.filter { it.status == ComandaStatus.Pendiente && (filterArea == null || it.area == filterArea) && (filterCourse == null || it.items.any { i -> i.courseType == filterCourse }) }
    val enPreparacion = comandas.filter { it.status == ComandaStatus.EnPreparacion && (filterArea == null || it.area == filterArea) && (filterCourse == null || it.items.any { i -> i.courseType == filterCourse }) }
    val listos = comandas.filter { it.status == ComandaStatus.Listo && (filterArea == null || it.area == filterArea) && (filterCourse == null || it.items.any { i -> i.courseType == filterCourse }) }

    val courseColors = mapOf("Entrada" to Color(0xFF0891B2), "Fuerte" to Color(0xFFD97706), "Postre" to Color(0xFF7C3AED))
    val courseCounts = comandas.flatMap { it.items }.groupBy { it.courseType }.mapValues { it.value.size }

    val columnDefs = listOf(
        Triple("Pendiente", pendientes, Color(0xFFEF4444)),
        Triple("En Preparacion", enPreparacion, Color(0xFFD97706)),
        Triple("Listo", listos, Color(0xFF16A34A))
    )

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columnDefs.forEach { (label, list, color) ->
                ColumnChip(label, list.size, color)
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip("Cocina", filterArea == "Cocina") { filterArea = if (filterArea == "Cocina") null else "Cocina" }
                FilterChip("Bar", filterArea == "Bar") { filterArea = if (filterArea == "Bar") null else "Bar" }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CourseChip("Todas", comandas.size, filterCourse == null, AppColors.Primary) { filterCourse = null }
            courseColors.forEach { (course, color) ->
                val count = courseCounts[course] ?: 0
                if (count > 0) {
                    CourseChip(course, count, filterCourse == course, color) { filterCourse = if (filterCourse == course) null else course }
                }
            }
        }

        Row(
            Modifier.fillMaxSize().padding(horizontal = 12.dp).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            columnDefs.forEach { (label, list, color) ->
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp)).background(Color.White)
                        .padding(8.dp)
                ) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)).padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                    Spacer(Modifier.height(6.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(list, key = { it.id }) { comanda ->
                            ComandaCard(comanda = comanda, color = color, isNew = newOrderFlash && comanda.status == ComandaStatus.Pendiente)
                        }
                        if (list.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                    Text("Sin ordenes", fontSize = 12.sp, color = AppColors.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComandaCard(comanda: Comanda, color: Color, isNew: Boolean) {
    val elapsed = (System.currentTimeMillis() - comanda.createdAt) / 1000
    val elapsedMin = elapsed / 60
    val timeColor = when {
        elapsedMin < 5 -> Color(0xFF16A34A)
        elapsedMin < 15 -> Color(0xFFD97706)
        else -> Color(0xFFEF4444)
    }
    val elapsedText = when {
        elapsedMin < 1 -> "Ahora"
        elapsedMin < 60 -> "${elapsedMin}m"
        else -> "${elapsedMin / 60}h ${elapsedMin % 60}m"
    }

    val flashAlpha by if (isNew) {
        val transition = rememberInfiniteTransition(label = "newFlash")
        transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "flash")
    } else remember { mutableStateOf(1f) }

    val itemCount = comanda.items.sumOf { it.quantity }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isNew) color.copy(alpha = flashAlpha * 0.15f) else AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(comanda.mesaName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(1.dp))
                    Text(
                        "${comanda.area}  |  ${comanda.items.size} items (${itemCount}uds)",
                        fontSize = 10.sp, color = AppColors.TextSecondary
                    )
                }
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(timeColor.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(elapsedText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = timeColor)
                }
            }

            Spacer(Modifier.height(6.dp))

            Box(
                Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(
                    when {
                        elapsedMin >= 15 -> Color(0xFFEF4444)
                        elapsedMin >= 5 -> Color(0xFFD97706)
                        else -> Color(0xFF16A34A)
                    }
                )
            )

            Spacer(Modifier.height(6.dp))

            comanda.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${item.quantity}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.width(28.dp))
                    Text(item.productName, fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    if (item.courseType.isNotBlank()) {
                        val courseColor = when (item.courseType) {
                            "Entrada" -> Color(0xFF0891B2); "Fuerte" -> Color(0xFFD97706); "Postre" -> Color(0xFF7C3AED)
                            else -> AppColors.TextSecondary
                        }
                        Box(Modifier.clip(RoundedCornerShape(3.dp)).background(courseColor.copy(alpha = 0.15f)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(item.courseType, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = courseColor)
                        }
                        Spacer(Modifier.width(3.dp))
                    }
                    if (item.notes.isNotBlank()) {
                        Text(item.notes, fontSize = 10.sp, color = AppColors.Orange, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                when (comanda.status) {
                    ComandaStatus.Pendiente -> {
                        ActionBtn("Cocinar", Icons.Outlined.PlayArrow, Color(0xFFD97706), Modifier.weight(1f)) {
                            ComandasManager.updateStatus(comanda.id, ComandaStatus.EnPreparacion)
                        }
                        ActionBtn("Listo", Icons.Outlined.Check, Color(0xFF16A34A), Modifier.weight(1f)) {
                            ComandasManager.updateStatus(comanda.id, ComandaStatus.Listo)
                        }
                    }
                    ComandaStatus.EnPreparacion -> {
                        ActionBtn("Listo", Icons.Outlined.Check, Color(0xFF16A34A), Modifier.fillMaxWidth()) {
                            ComandasManager.updateStatus(comanda.id, ComandaStatus.Listo)
                        }
                    }
                    ComandaStatus.Listo -> {
                        ActionBtn("Entregado", Icons.Outlined.Delete, AppColors.TextSecondary, Modifier.fillMaxWidth()) {
                            ComandasManager.removeComanda(comanda.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBtn(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier.height(34.dp)
    ) {
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun ColumnChip(label: String, count: Int, color: Color) {
    val bgColor = when (label) {
        "Pendiente" -> Color(0xFFFEE2E2)
        "En Preparacion" -> Color(0xFFFEF3C7)
        "Listo" -> Color(0xFFDCFCE7)
        else -> AppColors.Background
    }
    val textColor = when (label) {
        "Pendiente" -> Color(0xFFDC2626)
        "En Preparacion" -> Color(0xFFD97706)
        "Listo" -> Color(0xFF16A34A)
        else -> AppColors.TextSecondary
    }
    Box(
        Modifier.clip(RoundedCornerShape(20.dp)).background(bgColor).padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(textColor.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(18.dp))
            .background(if (active) AppColors.PrimaryLight else AppColors.Background)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, color = if (active) AppColors.Primary else AppColors.TextSecondary)
    }
}

@Composable
private fun CourseChip(label: String, count: Int, active: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(20.dp))
            .background(if (active) color.copy(alpha = 0.15f) else AppColors.Background)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, color = if (active) color else AppColors.TextSecondary)
            if (count > 0) {
                Box(Modifier.clip(RoundedCornerShape(10.dp)).background(if (active) color else AppColors.Gray).padding(horizontal = 7.dp, vertical = 2.dp)) {
                    Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
