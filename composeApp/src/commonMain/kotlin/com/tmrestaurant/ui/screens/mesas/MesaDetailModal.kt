package com.tmrestaurant.ui.screens.mesas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tmrestaurant.platform.DiscoveredPrinter
import com.tmrestaurant.platform.discoverPrinters
import com.tmrestaurant.platform.printTestPage
import com.tmrestaurant.platform.printWithSystemDialog
import com.tmrestaurant.ui.data.CartItem
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.MesasManager
import com.tmrestaurant.ui.data.Product
import com.tmrestaurant.ui.data.Usuario
import com.tmrestaurant.ui.data.UsuariosManager
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun MesaDetailModal(
    mesaId: Int,
    currentTime: Long = 0L,
    onDismiss: () -> Unit,
    onCobrar: () -> Unit,
    onMoveItems: (Int) -> Unit = {}
) {
    val mesa = MesasManager.mesas.find { it.id == mesaId } ?: run {
        onDismiss()
        return
    }

    val productState = LocalProductState.current
    var showAddProducts by remember { mutableStateOf(false) }
    var showPrintPreview by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val posProducts = remember(productState, searchQuery) {
        productState.getPosProducts().filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalItems = mesa.items.sumOf { it.quantity }
    val totalAmount = mesa.items.sumOf { it.product.price * it.effectiveQuantity }
    var showWaiterDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.width(800.dp).heightIn(max = 680.dp).clip(RoundedCornerShape(24.dp))
                    .background(AppColors.Surface).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (mesa.isOccupied) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.TableRestaurant, null,
                            tint = if (mesa.isOccupied) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(mesa.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                        Text(
                            if (mesa.isOccupied) "$totalItems productos - RD\$ ${"%,.0f".format(totalAmount)} - ${formatElapsedTime(mesa.openedAt, currentTime)}"
                            else "Mesa disponible",
                            color = AppColors.TextSecondary, fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Person, null, tint = AppColors.Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (mesa.waiterName.isNotBlank()) "Mesero: ${mesa.waiterName}" else "Sin mesero",
                                color = if (mesa.waiterName.isNotBlank()) AppColors.TextSecondary else AppColors.Gray,
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F4F6)).clickable { showWaiterDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Edit, null, tint = AppColors.Primary, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                if (mesa.isOccupied) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            if (mesa.items.isEmpty()) {
                                Box(
                                    Modifier.fillMaxWidth().height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No hay productos en esta mesa", color = AppColors.Gray, fontSize = 14.sp)
                                }
                            } else {
                                mesa.items.forEach { item ->
                                    MesaItemRow(
                                        item = item,
                                        onIncrement = {
                                            MesasManager.updateProductQuantity(mesaId, item.product.id, item.quantity + 1)
                                        },
                                        onDecrement = {
                                            if (item.quantity <= 1) {
                                                MesasManager.removeProductFromMesa(mesaId, item.product.id)
                                            } else {
                                                MesasManager.updateProductQuantity(mesaId, item.product.id, item.quantity - 1)
                                            }
                                        },
                                        onRemove = {
                                            MesasManager.removeProductFromMesa(mesaId, item.product.id)
                                        }
                                    )
                                }
                            }
                        }

                        Column(Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Resumen", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                                    DetRow("Productos:", "$totalItems")
                                    DetRow("Subtotal:", "RD\$ ${"%,.2f".format(totalAmount / 1.18)}")
                                    DetRow("ITBIS (18%):", "RD\$ ${"%,.2f".format(totalAmount - totalAmount / 1.18)}")
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border.copy(alpha = 0.3f)))
                                    DetRow("Total:", "RD\$ ${"%,.2f".format(totalAmount)}", bold = true)
                                }
                            }
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Danger),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Outlined.ClearAll, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Limpiar Mesa", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showAddProducts = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Agregar Productos", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showPrintPreview = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Outlined.Print, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Vista Previa", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        if (mesa.items.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { onMoveItems(mesaId) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Outlined.DriveFileMove, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Mover a...", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = onCobrar,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.height(48.dp),
                            enabled = mesa.items.isNotEmpty()
                        ) {
                            Icon(Icons.Outlined.Payment, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cobrar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } else {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9FAFB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.TouchApp, null, tint = AppColors.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Mesa disponible", color = AppColors.Gray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    MesasManager.occupyMesa(mesaId)
                                    showAddProducts = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                            ) {
                                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Ocupar Mesa y Agregar Productos", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddProducts) {
        AddProductsToMesaModal(
            mesaId = mesaId,
            products = posProducts,
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onDismiss = { showAddProducts = false }
        )
    }

    if (showPrintPreview) {
        MesaPrintPreviewModal(
            mesaName = mesa.name,
            items = mesa.items,
            onDismiss = { showPrintPreview = false }
        )
    }

    if (showClearConfirm) {
        ClearMesaConfirmDialog(
            mesaName = mesa.name,
            onDismiss = { showClearConfirm = false },
            onConfirm = {
                MesasManager.clearMesa(mesaId)
                showClearConfirm = false
            }
        )
    }

    if (showWaiterDialog) {
        WaiterAssignModal(
            currentWaiter = mesa.waiterName,
            onAssign = { name ->
                MesasManager.assignWaiter(mesaId, name)
                showWaiterDialog = false
            },
            onDismiss = { showWaiterDialog = false }
        )
    }
}

@Composable
private fun MesaItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val itemTotal = item.product.price * item.effectiveQuantity

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AppColors.Background,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.product.name, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("RD\$ ${"%,.2f".format(item.product.price)} x ${item.quantity}", color = AppColors.TextSecondary, fontSize = 11.sp)
            }
            Text("RD\$ ${"%,.0f".format(itemTotal)}", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFFEE2E2)).clickable(onClick = onDecrement), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Remove, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
                Text("${item.quantity}", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFD1FAE5)).clickable(onClick = onIncrement), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Add, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF3F4F6)).clickable(onClick = onRemove), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DetRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
    }
}

@Composable
private fun AddProductsToMesaModal(
    mesaId: Int,
    products: List<Product>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.width(600.dp).heightIn(max = 600.dp).clip(RoundedCornerShape(24.dp))
                    .background(AppColors.Surface).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Agregar Productos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                Box(
                    Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp))
                        .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(Icons.Outlined.Search, null, tint = AppColors.Gray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    if (searchQuery.isEmpty()) Text("Buscar productos...", color = AppColors.Gray, fontSize = 13.sp)
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp),
                        modifier = Modifier.fillMaxSize(), singleLine = true
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(products, key = { it.id }) { product ->
                        Surface(
                            onClick = {
                                MesasManager.addProductToMesa(mesaId, product)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Background,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border),
                            modifier = Modifier.height(80.dp)
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(10.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(product.name, color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                Text("RD\$ ${"%,.2f".format(product.price)}", color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MesaPrintPreviewModal(
    mesaName: String,
    items: List<CartItem>,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val totalAmount = items.sumOf { it.product.price * it.effectiveQuantity }
    var showPrinterPicker by remember { mutableStateOf(false) }
    var printers by remember { mutableStateOf<List<DiscoveredPrinter>>(emptyList()) }
    var loadingPrinters by remember { mutableStateOf(false) }

    fun wrapTicketLine(text: String, width: Int): List<String> {
        val clean = text.trim()
        if (clean.length <= width) return listOf(clean)
        val lines = mutableListOf<String>()
        var remaining = clean
        while (remaining.length > width) {
            val splitAt = remaining.lastIndexOf(' ', startIndex = width).takeIf { it > 0 } ?: width
            lines.add(remaining.take(splitAt).trimEnd())
            remaining = remaining.drop(splitAt).trimStart()
        }
        if (remaining.isNotBlank()) lines.add(remaining)
        return lines
    }

    val receiptText = buildString {
        appendLine("    ${mesaName.uppercase()}")
        appendLine("    CUENTA")
        appendLine("-".repeat(40))
        appendLine()
        items.forEach { item ->
            val price = "RD\$ ${"%,.0f".format(item.product.price * item.effectiveQuantity)}"
            val qty = "x${item.quantity}"
            wrapTicketLine(item.product.name.uppercase(), 40).forEach { appendLine(it) }
            appendLine("$qty${" ".repeat((40 - qty.length - price.length).coerceAtLeast(1))}$price")
        }
        appendLine()
        appendLine("-".repeat(40))
        appendLine("TOTAL:${" ".repeat(28)}RD\$ ${"%,.0f".format(totalAmount)}")
        appendLine("-".repeat(40))
        appendLine("   *** TM-RESTAURANTE ***")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.width(580.dp).clip(RoundedCornerShape(24.dp))
                    .background(AppColors.Surface).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Print, null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Vista Previa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                        Text(mesaName, color = AppColors.TextSecondary, fontSize = 12.sp)
                    }
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
                Box(
                    Modifier.fillMaxWidth().heightIn(max = 360.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC)).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                        .padding(16.dp).verticalScroll(rememberScrollState())
                ) {
                    Text(receiptText, color = Color(0xFF1F2937), fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, lineHeight = 14.sp)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Volver", color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            printWithSystemDialog("Cuenta - $mesaName", receiptText)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Outlined.Print, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sistema", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            loadingPrinters = true
                            scope.launch {
                                val usb = discoverPrinters("USB")
                                val bt = discoverPrinters("Bluetooth")
                                printers = usb + bt
                                loadingPrinters = false
                                showPrinterPicker = true
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !loadingPrinters
                    ) {
                        if (loadingPrinters) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Outlined.Usb, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Directo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (showPrinterPicker) {
        PrinterPickerDialogMesa(
            printers = printers,
            onSelect = { printer ->
                scope.launch {
                    printTestPage(printer.name, receiptText)
                }
                showPrinterPicker = false
                onDismiss()
            },
            onDismiss = { showPrinterPicker = false }
        )
    }
}

@Composable
private fun PrinterPickerDialogMesa(
    printers: List<DiscoveredPrinter>,
    onSelect: (DiscoveredPrinter) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.width(400.dp).clip(RoundedCornerShape(20.dp))
                    .background(AppColors.Surface).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Seleccionar Impresora", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                if (printers.isEmpty()) {
                    Text("No se encontraron impresoras", color = AppColors.TextSecondary, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        printers.forEach { printer ->
                            Surface(
                                onClick = { onSelect(printer) },
                                shape = RoundedCornerShape(10.dp),
                                color = AppColors.Background,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 14.dp).fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Print, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(printer.name, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(printer.type, color = AppColors.TextSecondary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun WaiterAssignModal(
    currentWaiter: String,
    onAssign: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val usuarios = UsuariosManager.usuarios
    var selectedWaiter by remember { mutableStateOf(currentWaiter) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(380.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Person, null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Asignar Mesero", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                    Text("Seleccione un empleado para esta mesa", color = AppColors.TextSecondary, fontSize = 12.sp)
                }
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF9FAFB))
                    .border(1.dp, if (currentWaiter.isNotEmpty()) AppColors.Primary.copy(alpha = 0.3f) else AppColors.Border, RoundedCornerShape(10.dp))
                    .clickable { selectedWaiter = ""; onAssign("") }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(20.dp).clip(CircleShape)
                            .background(if (selectedWaiter.isEmpty()) AppColors.Primary else AppColors.Background)
                            .border(1.dp, if (selectedWaiter.isEmpty()) AppColors.Primary else AppColors.Border, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedWaiter.isEmpty()) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Sin mesero", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            usuarios.forEach { usuario ->
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF9FAFB))
                        .border(1.dp, if (selectedWaiter == usuario.name) AppColors.Primary.copy(alpha = 0.3f) else AppColors.Border, RoundedCornerShape(10.dp))
                        .clickable { selectedWaiter = usuario.name; onAssign(usuario.name) }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(20.dp).clip(CircleShape)
                                .background(if (selectedWaiter == usuario.name) AppColors.Primary else AppColors.Background)
                                .border(1.dp, if (selectedWaiter == usuario.name) AppColors.Primary else AppColors.Border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedWaiter == usuario.name) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(usuario.name, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(usuario.role.displayName, color = AppColors.TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            if (usuarios.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("No hay empleados registrados", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (selectedWaiter != currentWaiter) AppColors.Primary else AppColors.Gray)
                        .clickable(enabled = selectedWaiter != currentWaiter) { onAssign(selectedWaiter) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aplicar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ClearMesaConfirmDialog(
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
                Icon(Icons.Outlined.ClearAll, null, tint = Color(0xFFEF4444), modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Limpiar Mesa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
            Text("Se eliminaran todos los productos de $mesaName", color = AppColors.TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEF4444)).clickable(onClick = onConfirm), contentAlignment = Alignment.Center) {
                    Text("Limpiar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
