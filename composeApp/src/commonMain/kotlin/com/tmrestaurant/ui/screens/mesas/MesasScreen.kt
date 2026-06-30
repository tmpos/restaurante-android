package com.tmrestaurant.ui.screens.mesas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.MesasManager
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun MesasScreen() {
    var selectedMesaId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Int?>(null) }
    var showCobrarModal by remember { mutableStateOf<Int?>(null) }
    var isFloorPlan by remember { mutableStateOf(true) }
    var mergeMode by remember { mutableStateOf(false) }
    var mergeSelection by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showMoveDialog by remember { mutableStateOf<Int?>(null) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            currentTime = System.currentTimeMillis()
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Gesti\u00f3n de Mesas", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1F2937))
                    Text("Seleccione una mesa para gestionar sus productos", color = Color(0xFF6B7280), fontSize = 13.sp)
                }
                if (mergeMode) {
                    Box(
                        Modifier.height(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7))
                            .padding(horizontal = 14.dp), contentAlignment = Alignment.Center
                    ) {
                        Text("${mergeSelection.size} seleccionada(s)", color = Color(0xFF92400E), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                    if (mergeSelection.size >= 2) {
                        Box(
                            Modifier.height(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF16A34A))
                                .clickable {
                                    val ids = mergeSelection.toList()
                                    MesasManager.mergeTables(ids, ids.last())
                                    mergeMode = false; mergeSelection = emptySet()
                                }.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Unir", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Box(
                        Modifier.height(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEE2E2))
                            .clickable { mergeMode = false; mergeSelection = emptySet() }.padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancelar", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                } else {
                    IconButton(onClick = { mergeMode = true }) {
                        Icon(Icons.Outlined.MergeType, null, tint = AppColors.Primary)
                    }
                    IconButton(onClick = { isFloorPlan = !isFloorPlan }) {
                        Icon(
                            if (isFloorPlan) Icons.Outlined.GridView else Icons.Outlined.Map,
                            contentDescription = if (isFloorPlan) "Vista de cuadr\u00edcula" else "Plano del local",
                            tint = AppColors.Primary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar Mesa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (isFloorPlan) {
                FloorPlanView(
                    mesas = MesasManager.mesas,
                    mergeMode = mergeMode,
                    mergeSelection = mergeSelection,
                    currentTime = currentTime,
                    onTapMesa = { id ->
                        if (mergeMode) {
                            mergeSelection = if (id in mergeSelection) mergeSelection - id else mergeSelection + id
                        } else {
                            selectedMesaId = id
                        }
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(200.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(MesasManager.mesas, key = { it.id }) { mesa ->
                        MesaCard(
                            mesa = mesa,
                            isMergeMode = mergeMode,
                            isMergeSelected = mesa.id in mergeSelection,
                            currentTime = currentTime,
                            onTap = {
                                if (mergeMode) {
                                    mergeSelection = if (mesa.id in mergeSelection) mergeSelection - mesa.id else mergeSelection + mesa.id
                                } else {
                                    selectedMesaId = mesa.id
                                }
                            },
                            onDelete = { showDeleteConfirm = mesa.id },
                            onCobrar = { showCobrarModal = mesa.id }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMesaDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, shape ->
                MesasManager.addMesa(name, shape)
                showAddDialog = false
            }
        )
    }

    showDeleteConfirm?.let { mesaId ->
        DeleteMesaConfirmDialog(
            mesaName = MesasManager.mesas.find { it.id == mesaId }?.name ?: "",
            onDismiss = { showDeleteConfirm = null },
            onConfirm = {
                MesasManager.removeMesa(mesaId)
                showDeleteConfirm = null
            }
        )
    }

    selectedMesaId?.let { mesaId ->
        MesaDetailModal(
            mesaId = mesaId,
            currentTime = currentTime,
            onDismiss = { selectedMesaId = null },
            onCobrar = {
                selectedMesaId = null
                showCobrarModal = mesaId
            },
            onMoveItems = { sourceId -> showMoveDialog = sourceId }
        )
    }

    showMoveDialog?.let { sourceId ->
        MoveToMesaDialog(
            sourceMesaName = MesasManager.mesas.find { it.id == sourceId }?.name ?: "",
            targetMesas = MesasManager.mesas.filter { it.id != sourceId },
            onDismiss = { showMoveDialog = null },
            onMove = { targetId ->
                MesasManager.moveItemsToMesa(sourceId, targetId)
                showMoveDialog = null
                selectedMesaId = null
            }
        )
    }

    showCobrarModal?.let { mesaId ->
        MesaCobrarModal(
            mesaId = mesaId,
            onDismiss = { showCobrarModal = null },
            onComplete = { showCobrarModal = null }
        )
    }
}

internal fun formatElapsedTime(openedAt: Long, now: Long): String {
    if (openedAt <= 0L) return ""
    val diff = now - openedAt
    val minutes = diff / 60_000
    if (minutes < 1) return "Ahora"
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${minutes} min"
}

@Composable
private fun FloorPlanView(
    mesas: List<com.tmrestaurant.ui.data.Mesa>,
    mergeMode: Boolean = false,
    mergeSelection: Set<Int> = emptySet(),
    currentTime: Long = 0L,
    onTapMesa: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        mesas.forEachIndexed { index, mesa ->
            var offsetX by remember(mesa.id) { mutableStateOf(0f) }
            var offsetY by remember(mesa.id) { mutableStateOf(0f) }

            val defaultX = 50f + (index % 4) * 160f
            val defaultY = 50f + (index / 4) * 120f
            val displayX = if (mesa.xPos < 0f) defaultX else mesa.xPos
            val displayY = if (mesa.yPos < 0f) defaultY else mesa.yPos

            val tableColor = when {
                mesa.isOccupied && mesa.openedAt > 0 &&
                    (System.currentTimeMillis() - mesa.openedAt) > 7_200_000 -> Color(0xFFEF4444)
                mesa.isOccupied -> Color(0xFFE68A00)
                else -> Color(0xFF10B981)
            }

            val isSelected = mesa.id in mergeSelection
            Box(
                modifier = Modifier
                    .offset(
                        x = (displayX + offsetX).dp,
                        y = (displayY + offsetY).dp
                    )
                    .size(
                        width = if (isSelected) (mesa.tableWidth + 8).dp else mesa.tableWidth.dp,
                        height = if (isSelected) (mesa.tableHeight + 8).dp else mesa.tableHeight.dp
                    )
                    .clip(
                        if (mesa.shape == "circle") CircleShape
                        else RoundedCornerShape(8.dp)
                    )
                    .background(if (isSelected) Color(0xFF3B82F6) else tableColor)
                    .clickable { onTapMesa(mesa.id) }
                    .then(
                        if (mergeMode) Modifier
                        else Modifier.pointerInput(mesa.id) {
                            detectDragGesturesAfterLongPress(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                },
                                onDragEnd = {
                                    MesasManager.updateMesaPosition(
                                        mesa.id,
                                        (displayX + offsetX).coerceAtLeast(0f),
                                        (displayY + offsetY).coerceAtLeast(0f)
                                    )
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        mesa.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (mesa.isOccupied) {
                        val itemsCount = mesa.items.sumOf { it.quantity }
                        Text(
                            "$itemsCount items",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                        Text(
                            formatElapsedTime(mesa.openedAt, currentTime),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                        if (mesa.waiterName.isNotBlank()) {
                            Text(
                                mesa.waiterName,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 8.sp
                            )
                        }
                    } else {
                        Text(
                            "Disponible",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MesaCard(
    mesa: com.tmrestaurant.ui.data.Mesa,
    isMergeMode: Boolean = false,
    isMergeSelected: Boolean = false,
    currentTime: Long = 0L,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    onCobrar: () -> Unit
) {
    val totalItems = mesa.items.sumOf { it.quantity }
    val totalAmount = mesa.items.sumOf { it.product.price * it.effectiveQuantity }
    val bgColor = if (mesa.isOccupied) Color(0xFFFEF2F2) else AppColors.Surface
    val borderColor = if (mesa.isOccupied) Color(0xFFFECACA) else AppColors.Border
    val statusColor = if (mesa.isOccupied) Color(0xFFEF4444) else Color(0xFF10B981)
    val statusText = if (mesa.isOccupied) "Ocupada" else "Disponible"

    Surface(
        onClick = onTap,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(if (isMergeSelected) 3.dp else 1.dp, if (isMergeSelected) Color(0xFF3B82F6) else borderColor),
        modifier = Modifier.height(180.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (mesa.isOccupied) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (mesa.isOccupied) Icons.Outlined.Restaurant else Icons.Outlined.TableRestaurant,
                        null,
                        tint = if (mesa.isOccupied) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Box(
                    Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2))
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }

            Text(mesa.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (mesa.isOccupied) {
                            Text(
                                " - ${formatElapsedTime(mesa.openedAt, currentTime)}",
                                color = Color(0xFF6B7280),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                    if (mesa.isOccupied) {
                        Text("$totalItems items | RD\$ ${"%,.0f".format(totalAmount)}", color = Color(0xFF6B7280), fontSize = 11.sp)
                    }
                    if (mesa.waiterName.isNotBlank()) {
                        Text("Mesero: ${mesa.waiterName}", color = AppColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
                if (mesa.isOccupied) {
                    Box(
                        Modifier.clip(RoundedCornerShape(10.dp)).background(AppColors.Primary)
                            .clickable(onClick = onCobrar).padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Cobrar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMesaDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedShape by remember { mutableStateOf("rectangle") }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(360.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(26.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.TableRestaurant, null, tint = Color(0xFFD97706), modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Nueva Mesa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (name.isEmpty()) Text("Nombre de la mesa", color = AppColors.Gray, fontSize = 14.sp)
                BasicTextField(value = name, onValueChange = { name = it }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
            }
            Spacer(Modifier.height(16.dp))
            Text("Forma de la mesa", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedShape == "rectangle") AppColors.Primary else AppColors.Background)
                        .border(1.dp, if (selectedShape == "rectangle") AppColors.Primary else AppColors.Border, RoundedCornerShape(10.dp))
                        .clickable { selectedShape = "rectangle" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Rect\u00e1ngulo", color = if (selectedShape == "rectangle") Color.White else AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.weight(1f).height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedShape == "circle") AppColors.Primary else AppColors.Background)
                        .border(1.dp, if (selectedShape == "circle") AppColors.Primary else AppColors.Border, RoundedCornerShape(10.dp))
                        .clickable { selectedShape = "circle" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("C\u00edrculo", color = if (selectedShape == "circle") Color.White else AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(if (name.isNotBlank()) AppColors.Primary else AppColors.Gray).clickable(enabled = name.isNotBlank()) { onAdd(name, selectedShape) }, contentAlignment = Alignment.Center) {
                    Text("Agregar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MoveToMesaDialog(
    sourceMesaName: String,
    targetMesas: List<com.tmrestaurant.ui.data.Mesa>,
    onDismiss: () -> Unit,
    onMove: (Int) -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(400.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.DriveFileMove, null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Mover Productos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                    Text("De: $sourceMesaName", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Seleccione la mesa de destino:", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            targetMesas.forEach { mesa ->
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).clickable { onMove(mesa.id) }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if (mesa.isOccupied) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)), contentAlignment = Alignment.Center) {
                            Icon(
                                if (mesa.isOccupied) Icons.Outlined.Restaurant else Icons.Outlined.TableRestaurant,
                                null,
                                tint = if (mesa.isOccupied) Color(0xFFEF4444) else Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(mesa.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                            Text(if (mesa.isOccupied) "${mesa.items.sumOf { it.quantity }} items" else "Disponible", fontSize = 12.sp, color = AppColors.TextSecondary)
                        }
                        Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.IconGray, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            if (targetMesas.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("No hay otras mesas disponibles", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DeleteMesaConfirmDialog(
    mesaName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(360.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(26.dp)).background(Color(0xFFFEE2E2)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Eliminar Mesa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
            Text("Se eliminara $mesaName permanentemente", color = AppColors.TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEF4444)).clickable(onClick = onConfirm), contentAlignment = Alignment.Center) {
                    Text("Eliminar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
