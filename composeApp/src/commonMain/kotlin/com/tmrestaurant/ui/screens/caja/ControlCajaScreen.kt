package com.tmrestaurant.ui.screens.caja

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.CajaMovimiento
import com.tmrestaurant.ui.data.CajaMovimientoTipo
import com.tmrestaurant.ui.data.InvoiceHistory
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ControlCajaScreen(
    onNavigateToDashboard: () -> Unit = {}
) {
    val turno = TurnoManager.currentTurno

    if (turno == null) {
        Box(Modifier.fillMaxSize().background(AppColors.Background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.ContentPaste, null, tint = AppColors.Gray, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("No hay turno activo", color = AppColors.TextSecondary, fontSize = 16.sp)
            }
        }
        return
    }

    val corte = TurnoManager.getCorte(InvoiceHistory.invoices, turno)
    val elapsedTime = formatElapsedTime(turno.startTime)
    var showGastoModal by remember { mutableStateOf(false) }
    var movimientoTipo by remember { mutableStateOf<CajaMovimientoTipo?>(null) }
    var showCerrarCajaModal by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF8FAFC))
    ) {
        // Content area
        Row(
            Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Left 70%
            Column(
                Modifier.weight(0.7f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Badge row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFD1FAE5)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981))
                            )
                            Text(
                                "Turno Abierto",
                                color = Color(0xFF065F46),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.Background
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                elapsedTime,
                                color = AppColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // CashSummaryCard
                CashSummaryCard(
                    efectivoEnCaja = corte.expectedCash,
                    inicial = turno.initialAmount,
                    ventasEfectivo = corte.totalEfectivo,
                    entradas = corte.totalEntradas,
                    gastos = corte.totalGastos,
                    retiros = corte.totalRetiros
                )

                // Payment Summary Grid 2x2
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        PaymentMethodCard(
                            icon = Icons.Outlined.AccountBalance,
                            iconBgColor = Color(0xFFD1FAE5),
                            iconColor = Color(0xFF10B981),
                            title = "Efectivo",
                            amount = corte.totalEfectivo,
                            modifier = Modifier.weight(1f)
                        )
                        PaymentMethodCard(
                            icon = Icons.Outlined.CreditCard,
                            iconBgColor = Color(0xFFDBEAFE),
                            iconColor = Color(0xFF3B82F6),
                            title = "Tarjeta",
                            amount = corte.totalTarjeta,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        PaymentMethodCard(
                            icon = Icons.Outlined.SwapHoriz,
                            iconBgColor = Color(0xFFFEF3C7),
                            iconColor = Color(0xFFD97706),
                            title = "Transferencia",
                            amount = corte.totalTransferencia,
                            modifier = Modifier.weight(1f)
                        )
                        PaymentMethodCard(
                            icon = Icons.Outlined.TrendingDown,
                            iconBgColor = Color(0xFFFEE2E2),
                            iconColor = Color(0xFFEF4444),
                            title = "Gastos",
                            amount = corte.totalGastos,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // SalesSummaryCard
                SalesSummaryCard(
                    totalVentas = corte.totalVentas,
                    facturas = corte.invoiceCount
                )

                CashMovementsCard(
                    movimientos = corte.movimientos,
                    gastos = corte.gastos
                )
            }

            // Right 30%
            LatestSalesCard(
                invoices = corte.invoices,
                modifier = Modifier.weight(0.3f)
            )
        }

        // Bottom actions
        ActionButtons(
            onEntrada = { movimientoTipo = CajaMovimientoTipo.ENTRADA },
            onRetiro = { movimientoTipo = CajaMovimientoTipo.RETIRO },
            onGasto = { showGastoModal = true },
            onCerrarCaja = { showCerrarCajaModal = true }
        )
    }

    if (showGastoModal) {
        GastoModal(
            onSave = { desc, amount ->
                TurnoManager.addGasto(desc, amount)
                showGastoModal = false
            },
            onDismiss = { showGastoModal = false }
        )
    }

    movimientoTipo?.let { tipo ->
        MovimientoCajaModal(
            tipo = tipo,
            onSave = { desc, amount ->
                TurnoManager.addMovimientoCaja(tipo, desc, amount)
                movimientoTipo = null
            },
            onDismiss = { movimientoTipo = null }
        )
    }

    if (showCerrarCajaModal) {
        CerrarCajaModal(
            corte = corte,
            onDismiss = { showCerrarCajaModal = false },
            onClosed = {
                showCerrarCajaModal = false
                onNavigateToDashboard()
            }
        )
    }
}

@Composable
private fun CashMovementsCard(
    movimientos: List<CajaMovimiento>,
    gastos: List<com.tmrestaurant.ui.data.Gasto>
) {
    val rows = (
        movimientos.map { movement ->
            CashHistoryRow(
                title = movement.description,
                user = movement.userName,
                amount = movement.amount,
                createdAt = movement.createdAt,
                color = if (movement.tipo == CajaMovimientoTipo.ENTRADA) Color(0xFF16A34A) else AppColors.Danger,
                prefix = if (movement.tipo == CajaMovimientoTipo.ENTRADA) "+" else "-"
            )
        } + gastos.map { gasto ->
            CashHistoryRow(
                title = gasto.description,
                user = gasto.userName,
                amount = gasto.amount,
                createdAt = gasto.createdAt,
                color = AppColors.Orange,
                prefix = "-"
            )
        }
    ).sortedByDescending { it.createdAt }.take(8)

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(18.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.SwapHoriz, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Movimientos de caja", color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${rows.size}", color = AppColors.TextSecondary, fontSize = 12.sp)
        }
        if (rows.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                Text("Sin entradas, retiros o gastos", color = AppColors.TextSecondary, fontSize = 12.sp)
            }
        } else {
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(row.title, color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text("${row.user} - ${formatDateTime(row.createdAt)}", color = AppColors.TextSecondary, fontSize = 9.sp)
                    }
                    Text(
                        "${row.prefix} RD\$ ${"%,.2f".format(row.amount)}",
                        color = row.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class CashHistoryRow(
    val title: String,
    val user: String,
    val amount: Double,
    val createdAt: Long,
    val color: Color,
    val prefix: String
)

@Composable
private fun MovimientoCajaModal(
    tipo: CajaMovimientoTipo,
    onSave: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val isEntrada = tipo == CajaMovimientoTipo.ENTRADA
    var desc by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val accent = if (isEntrada) Color(0xFF16A34A) else AppColors.Danger

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(380.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    if (isEntrada) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                    null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isEntrada) "Registrar Entrada" else "Registrar Retiro", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Motivo", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(6.dp))
            CajaInput(
                value = desc,
                placeholder = if (isEntrada) "Ej: Fondo adicional" else "Ej: Retiro para deposito",
                onChange = { desc = it; error = null }
            )
            Spacer(Modifier.height(14.dp))
            Text("Monto RD\$", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(6.dp))
            CajaInput(
                value = amountText,
                placeholder = "0.00",
                bold = true,
                onChange = { if (it.all { c -> c.isDigit() || c == '.' }) { amountText = it; error = null } }
            )
            if (error != null) { Spacer(Modifier.height(8.dp)); Text(error!!, color = AppColors.Danger, fontSize = 12.sp) }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(accent).clickable {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        desc.isBlank() -> error = "Ingrese un motivo"
                        amount == null || amount <= 0 -> error = "Ingrese un monto valido"
                        else -> onSave(desc, amount)
                    }
                }, contentAlignment = Alignment.Center) {
                    Text("Guardar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



@Composable
private fun GastoModal(
    onSave: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var desc by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(380.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar Gasto", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Descripcion", color = AppColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            CajaInput(value = desc, placeholder = "Ej: Compra de hielo", onChange = { desc = it; error = null })
            Spacer(Modifier.height(14.dp))
            Text("Monto RD\$", color = AppColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            CajaInput(value = amountText, placeholder = "0.00", bold = true, onChange = { if (it.all { c -> c.isDigit() || c == '.' }) { amountText = it; error = null } })
            if (error != null) { Spacer(Modifier.height(8.dp)); Text(error!!, color = AppColors.Danger, fontSize = 12.sp) }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) { Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Primary).clickable {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        desc.isBlank() -> error = "Ingrese una descripcion"
                        amount == null || amount <= 0 -> error = "Ingrese un monto valido"
                        else -> onSave(desc, amount)
                    }
                }, contentAlignment = Alignment.Center) { Text("Guardar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun CajaInput(
    value: String,
    placeholder: String,
    bold: Boolean = false,
    onChange: (String) -> Unit
) {
    Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) Text(placeholder, color = AppColors.Gray, fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal),
            modifier = Modifier.fillMaxSize(),
            singleLine = true
        )
    }
}

private fun formatElapsedTime(startTime: Long): String {
    val elapsed = System.currentTimeMillis() - startTime
    val hours = elapsed / 3600000
    val minutes = (elapsed % 3600000) / 60000
    return "-${hours}h -${minutes}m"
}
