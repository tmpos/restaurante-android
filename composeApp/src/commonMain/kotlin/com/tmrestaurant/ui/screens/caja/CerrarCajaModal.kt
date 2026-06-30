package com.tmrestaurant.ui.screens.caja

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tmrestaurant.platform.DiscoveredPrinter
import com.tmrestaurant.platform.SmtpConfig
import com.tmrestaurant.platform.discoverPrinters
import com.tmrestaurant.platform.isNetworkAvailable
import com.tmrestaurant.platform.printTestPage
import com.tmrestaurant.platform.printWithSystemDialog
import com.tmrestaurant.platform.sendEmail
import com.tmrestaurant.ui.data.Corte
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import com.tmrestaurant.ui.screens.buildCorteReceiptText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private data class Denom(val label: String, val value: Int)

private val billetes = listOf(
    Denom("RD\$ 2000", 2000), Denom("RD\$ 1000", 1000), Denom("RD\$ 500", 500),
    Denom("RD\$ 200", 200), Denom("RD\$ 100", 100),
    Denom("RD\$ 50", 50), Denom("RD\$ 25", 25)
)
private val monedas = listOf(
    Denom("RD\$ 25", 25), Denom("RD\$ 10", 10),
    Denom("RD\$ 5", 5), Denom("RD\$ 1", 1)
)

@Composable
fun CerrarCajaModal(
    corte: Corte,
    onDismiss: () -> Unit,
    onClosed: () -> Unit
) {
    var showPreview by remember { mutableStateOf(false) }
    var showCloseConfirm by remember { mutableStateOf(false) }
    var billetCounts by remember { mutableStateOf(billetes.map { 0 }.toMutableList()) }
    var monedaCounts by remember { mutableStateOf(monedas.map { 0 }.toMutableList()) }

    val totalBilletes = billetes.mapIndexed { i, d -> billetCounts[i] * d.value }.sum()
    val totalMonedasVal = monedas.mapIndexed { i, d -> monedaCounts[i] * d.value }.sum()
    val totalFisico = totalBilletes + totalMonedasVal
    val diferencia = totalFisico - corte.expectedCash.toInt()

    val scope = rememberCoroutineScope()
    val settings = LocalSettingsState.current

    fun enviarEmailCierre() {
        val notif = settings.settings.notifications
        if (!notif.enabled || !notif.sendOnCashClose) return
        if (notif.destinationEmails.isBlank() || notif.appPassword.isBlank()) return
        if (!isNetworkAvailable()) return

        val htmlBody = buildCorteEmailBody(
            corte, settings.settings.company,
            billetCounts.toList(), monedaCounts.toList(),
            totalBilletes, totalMonedasVal, totalFisico, diferencia
        )

        scope.launch(Dispatchers.Default) {
            val config = SmtpConfig(
                host = notif.smtpServer,
                port = notif.smtpPort.toIntOrNull() ?: 465,
                username = notif.senderEmail,
                password = notif.appPassword,
                useSsl = notif.sslTls
            )
            val destEmails = notif.destinationEmails
                .split(",", ";")
                .map { it.trim() }
                .filter { it.isNotBlank() }
            destEmails.forEach { email ->
                sendEmail(
                    config = config,
                    fromName = notif.senderName,
                    fromAddr = notif.senderEmail,
                    to = email,
                    subject = "Cierre de Caja - ${corte.turno.id} - ${corte.turno.userName}",
                    body = htmlBody
                )
            }
        }
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
                Modifier.width(680.dp).clip(RoundedCornerShape(24.dp))
                    .background(AppColors.Surface).padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ContentPaste, null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Cerrar Caja", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary)
                        Text("Conteo de billetes y monedas", color = AppColors.TextSecondary, fontSize = 12.sp)
                    }
                    Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                // Two-column layout
                Row(
                    Modifier.fillMaxWidth().heightIn(max = 480.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left: Bills & Coins
                    Column(
                        Modifier.weight(1.3f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Billetes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        billetes.forEachIndexed { i, d ->
                            DenomInput(
                                label = d.label,
                                value = billetCounts[i],
                                onValueChange = { v -> billetCounts = billetCounts.toMutableList().also { it[i] = v } }
                            )
                        }

                        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border.copy(alpha = 0.5f)))

                        Text("Monedas", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        monedas.forEachIndexed { i, d ->
                            DenomInput(
                                label = d.label,
                                value = monedaCounts[i],
                                onValueChange = { v -> monedaCounts = monedaCounts.toMutableList().also { it[i] = v } }
                            )
                        }
                    }

                    // Right: Totals
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Resumen", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TotRow("Total Billetes:", "RD\$ ${"%,d".format(totalBilletes)}", AppColors.TextPrimary)
                                TotRow("Total Monedas:", "RD\$ ${"%,d".format(totalMonedasVal)}", AppColors.TextPrimary)
                                Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border.copy(alpha = 0.3f)))
                                TotRow("Total Fisico:", "RD\$ ${"%,d".format(totalFisico)}", AppColors.TextPrimary, bold = true)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TotRow("Esperado:", "RD\$ ${"%,.2f".format(corte.expectedCash)}", Color(0xFF16A34A))
                                TotRow(
                                    "Diferencia:",
                                    if (diferencia >= 0) "RD\$ ${"%,d".format(diferencia)}" else "-RD\$ ${"%,d".format(-diferencia)}",
                                    if (diferencia >= 0) Color(0xFF16A34A) else AppColors.Danger,
                                    bold = true
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showPreview = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary)
                            ) {
                                Icon(Icons.Outlined.Print, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Previsualizar Ticket", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }

                            Button(
                                onClick = { showCloseConfirm = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(Icons.Outlined.ContentPaste, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Cerrar Caja", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("Cancelar", color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPreview) {
        TicketPreviewModal(
            corte = corte,
            onDismiss = { showPreview = false }
        )
    }

    if (showCloseConfirm) {
        Dialog(
            onDismissRequest = { showCloseConfirm = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.width(420.dp).clip(RoundedCornerShape(24.dp))
                        .background(AppColors.Surface).padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Confirmar Cierre de Caja", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                            Text("El turno actual se cerrara definitivamente", color = AppColors.TextSecondary, fontSize = 12.sp)
                        }
                    }

                    Text(
                        "¿Desea imprimir el ticket de cierre?",
                        fontSize = 14.sp,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = {
                            enviarEmailCierre()
                            val receiptText = buildCorteReceiptText(corte, settings.settings.company, settings.settings.print.paperWidthMm)
                            printWithSystemDialog("Corte de Caja", receiptText)
                            TurnoManager.closeTurno()
                            showCloseConfirm = false
                            onClosed()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Outlined.Print, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Imprimir y Cerrar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                enviarEmailCierre()
                                TurnoManager.closeTurno()
                                showCloseConfirm = false
                                onClosed()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Outlined.ContentPaste, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Solo Cerrar", color = AppColors.Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showCloseConfirm = false },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Cancelar", color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DenomInput(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var text by remember(value) { mutableStateOf(if (value > 0) value.toString() else "") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(40.dp)
    ) {
        Text(label, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(80.dp))
        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)).clickable { onValueChange((value - 1).coerceAtLeast(0)) },
                contentAlignment = Alignment.Center
            ) { Text("-", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

            Box(
                Modifier.width(64.dp).height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { newVal ->
                        if (newVal.all { it.isDigit() }) {
                            text = newVal
                            onValueChange(newVal.toIntOrNull() ?: 0)
                        }
                    },
                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    singleLine = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)).clickable { onValueChange(value + 1) },
                contentAlignment = Alignment.Center
            ) { Text("+", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun TotRow(label: String, value: String, valueColor: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold)
    }
}

@Composable
private fun TicketPreviewModal(
    corte: Corte,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val settings = LocalSettingsState.current
    var showPrinterPicker by remember { mutableStateOf(false) }
    var printers by remember { mutableStateOf<List<DiscoveredPrinter>>(emptyList()) }
    var loadingPrinters by remember { mutableStateOf(false) }

    val receiptText = remember {
        buildCorteReceiptText(corte, settings.settings.company, settings.settings.print.paperWidthMm)
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
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Print, null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Vista Previa del Ticket", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                        Text("Corte de Caja", color = AppColors.TextSecondary, fontSize = 12.sp)
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
                    Text(
                        receiptText,
                        color = Color(0xFF1F2937),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
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
                            printWithSystemDialog("Corte de Caja", receiptText)
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
        PrinterPickerDialog(
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
private fun PrinterPickerDialog(
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
