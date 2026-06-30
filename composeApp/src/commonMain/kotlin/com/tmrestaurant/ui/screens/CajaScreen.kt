package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.printTestPage
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.launch

private data class DenomEntry(val label: String, val value: Double)

private val billetes = listOf(
    DenomEntry("RD\$ 1000", 1000.0), DenomEntry("RD\$ 500", 500.0),
    DenomEntry("RD\$ 200", 200.0), DenomEntry("RD\$ 100", 100.0),
    DenomEntry("RD\$ 50", 50.0), DenomEntry("RD\$ 25", 25.0)
)
private val monedas = listOf(
    DenomEntry("RD\$ 25", 25.0), DenomEntry("RD\$ 10", 10.0),
    DenomEntry("RD\$ 5", 5.0), DenomEntry("RD\$ 1", 1.0)
)

@Composable
fun CajaScreen(
    onNavigateToDashboard: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val settings = LocalSettingsState.current
    var showGastoModal by remember { mutableStateOf(false) }
    var printing by remember { mutableStateOf(false) }
    var printMsg by remember { mutableStateOf<String?>(null) }

    val turno = TurnoManager.currentTurno
    if (turno == null) {
        Box(Modifier.fillMaxSize().background(AppColors.Background), contentAlignment = Alignment.Center) {
            Text("No hay turno activo", color = AppColors.TextSecondary, fontSize = 16.sp)
        }
        return
    }

    val corte = TurnoManager.getCorte(InvoiceHistory.invoices, turno)

    // Bill/coin counter state
    var cantBilletes by remember { mutableStateOf(billetes.map { 0 }.toMutableList()) }
    var cantMonedas by remember { mutableStateOf(monedas.map { 0 }.toMutableList()) }

    val totalBilletes = billetes.mapIndexed { i, d -> cantBilletes[i] * d.value.toInt() }.sum()
    val totalMonedasCount = monedas.mapIndexed { i, d -> cantMonedas[i] * d.value.toInt() }.sum()
    val totalFisico = totalBilletes + totalMonedasCount
    val diferencia = totalFisico - corte.expectedCash.toInt()

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(
            Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ContentPaste, null, tint = Color(0xFFD97706), modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Caja", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppColors.TextPrimary)
                    Text("Turno activo - ${turno.userName}", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }

            // Turno Info
            Card13("INFORMACION DEL TURNO") {
                Row(Modifier.fillMaxWidth()) {
                    Text("Cajero:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(corte.turno.userName, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Inicio:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(formatDateTime(corte.turno.startTime), color = AppColors.TextPrimary, fontSize = 13.sp)
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Efectivo Inicial:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("RD\$ ${"%,.2f".format(corte.turno.initialAmount)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Sales Summary
            Card13("RESUMEN DE VENTAS") {
                Row(Modifier.fillMaxWidth()) { Text("Facturas:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("${corte.invoiceCount}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                Divider2()
                Row(Modifier.fillMaxWidth()) { Text("Efectivo:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(corte.totalEfectivo)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                Row(Modifier.fillMaxWidth()) { Text("Tarjeta:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(corte.totalTarjeta)}", color = AppColors.TextPrimary, fontSize = 13.sp) }
                Row(Modifier.fillMaxWidth()) { Text("Transferencia:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(corte.totalTransferencia)}", color = AppColors.TextPrimary, fontSize = 13.sp) }
                Row(Modifier.fillMaxWidth()) { Text("Credito:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(corte.totalCredito)}", color = AppColors.TextPrimary, fontSize = 13.sp) }
                Divider2()
                Row(Modifier.fillMaxWidth()) { Text("Total Ventas:", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(corte.totalVentas)}", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }

            // Expenses
            Card13("GASTOS DEL TURNO") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Total Gastos:", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("RD\$ ${"%,.2f".format(corte.totalGastos)}", color = AppColors.Danger, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight)
                        .clickable { showGastoModal = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Add, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar Gasto", color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (corte.gastos.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Divider2()
                    corte.gastos.forEach { gasto ->
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(gasto.description, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${gasto.userName} - ${formatDateTime(gasto.createdAt)}", color = AppColors.TextSecondary, fontSize = 10.sp)
                            }
                            Text("-RD\$ ${"%,.2f".format(gasto.amount)}", color = AppColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Bill/Coin Counter
            Card13("CONTEO DE BILLETES Y MONEDAS") {
                Text("Billetes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(6.dp))
                billetes.forEachIndexed { i, d ->
                    DenomRow(d.label, cantBilletes[i], { cantBilletes = cantBilletes.toMutableList().also { it[i] = it[i].coerceAtLeast(1) - 1 } }, { cantBilletes = cantBilletes.toMutableList().also { it[i]++ } })
                }
                Spacer(Modifier.height(10.dp))
                Divider2()
                Spacer(Modifier.height(6.dp))
                Text("Monedas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(6.dp))
                monedas.forEachIndexed { i, d ->
                    DenomRow(d.label, cantMonedas[i], { cantMonedas = cantMonedas.toMutableList().also { it[i] = it[i].coerceAtLeast(1) - 1 } }, { cantMonedas = cantMonedas.toMutableList().also { it[i]++ } })
                }
                Spacer(Modifier.height(10.dp))
                Divider2()
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) { Text("Total Billetes:", modifier = Modifier.weight(1f), color = AppColors.TextSecondary, fontSize = 13.sp); Text("RD\$ ${"%,d".format(totalBilletes)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                Row(Modifier.fillMaxWidth()) { Text("Total Monedas:", modifier = Modifier.weight(1f), color = AppColors.TextSecondary, fontSize = 13.sp); Text("RD\$ ${"%,d".format(totalMonedasCount)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                Row(Modifier.fillMaxWidth()) { Text("Total Fisico:", modifier = Modifier.weight(1f), color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("RD\$ ${"%,d".format(totalFisico)}", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }

            // Expected vs Physical
            Card13("EFECTIVO EN CAJA") {
                Row(Modifier.fillMaxWidth()) { Text("Inicial:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(corte.turno.initialAmount)}", color = AppColors.TextPrimary, fontSize = 13.sp) }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth()) { Text("+ Ventas Efectivo:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(corte.totalEfectivo)}", color = Color(0xFF16A34A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                if (corte.totalGastos > 0) {
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth()) { Text("- Gastos:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(corte.totalGastos)}", color = AppColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                }
                Divider2()
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFDCFCE7)).padding(12.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Text("Esperado:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF16A34A), modifier = Modifier.weight(1f))
                        Text("RD\$ ${"%,.2f".format(corte.expectedCash)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF16A34A))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Divider2()
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) { Text("Total Fisico Contado:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,d".format(totalFisico)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                Row(Modifier.fillMaxWidth()) {
                    Text("Diferencia:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(
                        if (diferencia >= 0) "RD\$ ${"%,d".format(diferencia)}" else "-RD\$ ${"%,d".format(-diferencia)}",
                        color = if (diferencia >= 0) Color(0xFF16A34A) else AppColors.Danger,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            // Print and Close buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Surface)
                        .border(1.5.dp, AppColors.Border, RoundedCornerShape(12.dp))
                        .clickable(enabled = !printing) {
                            printing = true; printMsg = null
                            scope.launch {
                                val content = buildCorteReceiptText(corte, settings.settings.company, settings.settings.print.paperWidthMm)
                                val ok = printTestPage(settings.settings.print.selectedPrinter, content)
                                printMsg = if (ok) "Impreso correctamente" else "Error al imprimir"; printing = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Print, null, tint = AppColors.TextPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (printing) "Imprimiendo..." else if (printMsg != null) printMsg!! else "Imprimir Corte", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Box(
                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                        .clickable {
                            TurnoManager.closeTurno()
                            onNavigateToDashboard()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar Caja", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }

        // Gasto modal overlay
        if (showGastoModal) {
            GastoModal(
                onSave = { desc, amount ->
                    TurnoManager.addGasto(desc, amount)
                    showGastoModal = false
                },
                onDismiss = { showGastoModal = false }
            )
        }
    }
}

@Composable
private fun DenomRow(label: String, cant: Int, onDec: () -> Unit, onInc: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(70.dp))
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background)
                .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)).clickable(onClick = onDec),
            contentAlignment = Alignment.Center
        ) { Text("-", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        Text("$cant", color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background)
                .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)).clickable(onClick = onInc),
            contentAlignment = Alignment.Center
        ) { Text("+", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
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
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                if (desc.isEmpty()) Text("Ej: Compra de hielo", color = AppColors.Gray, fontSize = 14.sp)
                BasicTextField(value = desc, onValueChange = { desc = it; error = null }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
            }
            Spacer(Modifier.height(14.dp))
            Text("Monto RD\$", color = AppColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                if (amountText.isEmpty()) Text("0.00", color = AppColors.Gray, fontSize = 14.sp)
                BasicTextField(value = amountText, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { amountText = it; error = null } }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxSize(), singleLine = true)
            }
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
private fun Card13(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(AppColors.Surface).padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextSecondary)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun Divider2() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border.copy(alpha = 0.5f)))
}
