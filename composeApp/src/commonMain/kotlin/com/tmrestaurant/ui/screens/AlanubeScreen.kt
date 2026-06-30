package com.tmrestaurant.ui.screens

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.cloud.AlanubeConfig
import com.tmrestaurant.cloud.AlanubeConfigStore
import com.tmrestaurant.cloud.AlanubeEnvironment
import com.tmrestaurant.cloud.AlanubeResult
import com.tmrestaurant.cloud.AlanubeService
import com.tmrestaurant.cloud.AlanubeStatus
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun AlanubeScreen() {
    if (!AccessControl.canAccessCloud(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Alanube - DGII",
            subtitle = "Solo los administradores pueden acceder a la configuracion de Alanube."
        )
        return
    }

    val scope = rememberCoroutineScope()
    val initial = remember { AlanubeConfigStore.load() }

    var environment by remember { mutableStateOf(initial.environment) }
    var jwtToken by remember { mutableStateOf(initial.jwtToken) }
    var companyId by remember { mutableStateOf(initial.companyId) }
    var rnc by remember { mutableStateOf(initial.rnc) }
    var companyName by remember { mutableStateOf(initial.companyName) }
    var address by remember { mutableStateOf(initial.address) }
    var stampDate by remember { mutableStateOf(initial.stampDate) }

    var status by remember { mutableStateOf<AlanubeStatus?>(null) }
    var result by remember { mutableStateOf<AlanubeResult?>(null) }
    var loading by remember { mutableStateOf("") }
    var visibleToken by remember { mutableStateOf(false) }

    var showEmitModal by remember { mutableStateOf(false) }
    var emitDocType by remember { mutableStateOf("32") }
    var emitEncf by remember { mutableStateOf("") }
    var emitTotal by remember { mutableStateOf("") }
    var emitBuyerRnc by remember { mutableStateOf("") }
    var emitBuyerName by remember { mutableStateOf("") }
    var emitDocId by remember { mutableStateOf("") }

    var expandedResponse by remember { mutableStateOf(false) }

    fun currentConfig() = AlanubeConfig(
        environment = environment,
        jwtToken = jwtToken.trim(),
        companyId = companyId.trim(),
        rnc = rnc.trim(),
        companyName = companyName.trim(),
        address = address.trim(),
        stampDate = stampDate.trim()
    )

    fun save() {
        val cfg = currentConfig()
        AlanubeConfigStore.save(cfg)
        AlanubeService.init(cfg)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().background(AppColors.Background).verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AlanubeHeader(status)

            AlanubeCard("Entorno", Icons.Outlined.CloudQueue) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AlanubeEnvironment.entries.forEach { env ->
                        val selected = environment == env
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (selected) AppColors.Surface else Color.Transparent)
                                .clickable { environment = env }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                env.label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) AppColors.TextPrimary else AppColors.TextSecondary
                            )
                        }
                    }
                }
                val url = currentConfig().environment.baseUrl
                Text("URL: $url", fontSize = 11.sp, color = AppColors.TextSecondary)
            }

            AlanubeCard("Autenticacion", Icons.Outlined.Lock) {
                AlanubeField("JWT Token", jwtToken, "Bearer token de Alanube", password = true) { jwtToken = it }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Obten tu JWT contactando a Alanube. Usa el mismo token para Sandbox y Produccion.",
                    fontSize = 10.sp,
                    color = AppColors.TextSecondary
                )
            }

            AlanubeCard("Compania", Icons.Outlined.Business) {
                AlanubeField("ID Compania (opcional)", companyId, "ulid de la compania") { companyId = it }
                AlanubeField("RNC Emisor", rnc, "123456789") { rnc = it }
                AlanubeField("Razon Social", companyName, "Mi Compania SRL") { companyName = it }
                AlanubeField("Direccion", address, "Calle Principal #123") { address = it }
                AlanubeField("Fecha Emision", stampDate, "2025-01-15") { stampDate = it }
            }

            AlanubeCard("Acciones", Icons.Outlined.Bolt) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlanubeButton("Guardar", Icons.Outlined.Check, loading == "save", Modifier.weight(1f), Color(0xFF16A34A)) {
                        save()
                        loading = "save"
                        AlanubeService.init(currentConfig())
                        scope.launch {
                            status = AlanubeService.testConnection()
                            loading = ""
                        }
                    }
                    AlanubeButton("Probar Conexion", Icons.Outlined.Power, loading == "test", Modifier.weight(1f), AppColors.TextSecondary, light = true) {
                        loading = "test"
                        AlanubeService.init(currentConfig())
                        scope.launch {
                            status = AlanubeService.testConnection()
                            loading = ""
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlanubeButton("Info Compania", Icons.Outlined.Info, loading == "company", Modifier.weight(1f), Color(0xFF0284C7)) {
                        loading = "company"
                        AlanubeService.init(currentConfig())
                        scope.launch {
                            result = AlanubeService.getCompanyInfo()
                            if (result?.success == true) expandedResponse = true
                            loading = ""
                        }
                    }
                    AlanubeButton("Estado DGII", Icons.Outlined.MedicalServices, loading == "dgii", Modifier.weight(1f), Color(0xFF7C3AED)) {
                        loading = "dgii"
                        AlanubeService.init(currentConfig())
                        scope.launch {
                            result = AlanubeService.checkDGIIHealth()
                            loading = ""
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                AlanubeButton("Emitir Comprobante", Icons.Outlined.Receipt, false, Modifier.fillMaxWidth(), Color(0xFF0891B2)) {
                    emitEncf = ""
                    emitTotal = ""
                    emitBuyerRnc = ""
                    emitBuyerName = ""
                    emitDocId = ""
                    showEmitModal = true
                }
            }

            status?.let { AlanubeConnectionStatus(it) }

            if (loading == "save") {
                AlanubeProgressCard("Guardando configuracion...")
            }

            result?.let { res ->
                AlanubeResultCard(res, expandedResponse) { expandedResponse = !expandedResponse }
            }
        }

        if (loading.isNotEmpty() && loading != "save" && loading != "emit") {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(
                        Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(Modifier.size(48.dp), strokeWidth = 4.dp, color = AppColors.Primary)
                        Text(
                            when (loading) {
                                "test" -> "Probando conexion con Alanube..."
                                "company" -> "Consultando informacion..."
                                "dgii" -> "Consultando estado DGII..."
                                else -> "Procesando..."
                            },
                            fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary
                        )
                    }
                }
            }
        }
    }

    if (showEmitModal) {
        AlanubeEmitModal(
            emitDocType = emitDocType,
            onDocTypeChange = { emitDocType = it },
            emitEncf = emitEncf,
            onEncfChange = { emitEncf = it },
            emitTotal = emitTotal,
            onTotalChange = { emitTotal = it },
            emitBuyerRnc = emitBuyerRnc,
            onBuyerRncChange = { emitBuyerRnc = it },
            emitBuyerName = emitBuyerName,
            onBuyerNameChange = { emitBuyerName = it },
            emitDocId = emitDocId,
            onDocIdChange = { emitDocId = it },
            loading = loading == "emit",
            onEmit = {
                loading = "emit"
                AlanubeService.init(currentConfig())
                scope.launch {
                    val total = emitTotal.toDoubleOrNull() ?: 0.0
                    result = when (emitDocType) {
                        "31" -> AlanubeService.emitCreditFiscalInvoice(
                            emitEncf, total, emitBuyerRnc, emitBuyerName
                        )
                        "32" -> AlanubeService.emitInvoiceConsumption(emitEncf, total)
                        "45" -> AlanubeService.emitGubernamentalInvoice(
                            emitEncf, total, emitBuyerRnc, emitBuyerName
                        )
                        "34" -> AlanubeService.emitCreditNote(
                            emitEncf, total, "", emitBuyerRnc, emitBuyerName
                        )
                        "33" -> AlanubeService.emitDebitNote(
                            emitEncf, total, "", emitBuyerRnc, emitBuyerName
                        )
                        else -> AlanubeResult(false, "Tipo de documento no soportado")
                    }
                    expandedResponse = true
                    loading = ""
                }
            },
            onCheckStatus = {
                loading = "status"
                AlanubeService.init(currentConfig())
                scope.launch {
                    result = when (emitDocType) {
                        "31" -> AlanubeService.checkFiscalInvoiceStatus(emitDocId)
                        else -> AlanubeService.checkDocumentStatus(emitDocId)
                    }
                    expandedResponse = true
                    loading = ""
                }
            },
            onDismiss = { showEmitModal = false }
        )
    }
}

@Composable
private fun AlanubeHeader(status: AlanubeStatus?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF0891B2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Receipt, null, tint = Color.White, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Alanube - DGII", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Text("Emision de comprobantes electronicos", fontSize = 12.sp, color = AppColors.TextSecondary)
        }
        val connected = status?.connected == true
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (connected) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
        ) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (connected) Color(0xFF16A34A) else Color(0xFFEF4444)))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (connected) "Conectado" else "Desconectado",
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = if (connected) Color(0xFF15803D) else Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
private fun AlanubeConnectionStatus(status: AlanubeStatus) {
    val color = if (status.connected) Color(0xFF16A34A) else Color(0xFFEF4444)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (status.connected) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)),
        modifier = Modifier.fillMaxWidth().border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (status.connected) Icons.Outlined.CheckCircle else Icons.Outlined.Warning, null, tint = color)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (status.connected) "Conexion exitosa${status.companyName?.let { " - $it" } ?: ""}"
                    else status.error.orEmpty(),
                    color = color, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AlanubeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = AppColors.IconGray, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp))
                Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
            }
            content()
        }
    }
}

@Composable
private fun AlanubeField(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    onValueChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, color = AppColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(5.dp))
        Row(
            Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(10.dp))
                .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(placeholder, color = AppColors.Gray, fontSize = 12.sp)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 12.sp),
                    visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None
                )
            }
            if (password) {
                IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AlanubeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    loading: Boolean,
    modifier: Modifier,
    color: Color,
    light: Boolean = false,
    outlined: Boolean = false,
    onClick: () -> Unit
) {
    val background = when { outlined -> Color.Transparent; light -> AppColors.Background; else -> color }
    Row(
        modifier.height(44.dp).clip(RoundedCornerShape(10.dp)).background(background)
            .then(if (outlined) Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp)) else Modifier)
            .clickable(enabled = !loading, onClick = onClick).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp, color = color)
        } else {
            Icon(icon, null, tint = if (light || outlined) color else Color.White, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text(label, color = if (light || outlined) color else Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AlanubeProgressCard(message: String) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp, color = AppColors.Primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(message, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("Espera mientras se procesa la informacion.", fontSize = 10.sp, color = AppColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun AlanubeResultCard(result: AlanubeResult, expanded: Boolean, onToggle: () -> Unit) {
    val color = if (result.success) Color(0xFF16A34A) else Color(0xFFEF4444)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (result.success) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(if (result.success) Icons.Outlined.Check else Icons.Outlined.Close, null, tint = color)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(result.message, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    if (result.documentId != null) {
                        Text("Documento ID: ${result.documentId}", fontSize = 11.sp, color = AppColors.TextSecondary)
                    }
                    if (result.legalStatus != null) {
                        Text("Estado Legal: ${result.legalStatus}", fontSize = 11.sp, color = AppColors.TextSecondary)
                    }
                }
            }
            if (result.responseBody.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().clickable(onClick = onToggle),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(if (expanded) "Ocultar respuesta" else "Ver respuesta completa", fontSize = 10.sp, color = Color(0xFF0891B2))
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        null, tint = Color(0xFF0891B2), modifier = Modifier.size(16.dp)
                    )
                }
                if (expanded) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Background,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).heightIn(max = 200.dp)
                    ) {
                        Box(Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
                            Text(result.responseBody, fontSize = 9.sp, color = AppColors.TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlanubeEmitModal(
    emitDocType: String,
    onDocTypeChange: (String) -> Unit,
    emitEncf: String,
    onEncfChange: (String) -> Unit,
    emitTotal: String,
    onTotalChange: (String) -> Unit,
    emitBuyerRnc: String,
    onBuyerRncChange: (String) -> Unit,
    emitBuyerName: String,
    onBuyerNameChange: (String) -> Unit,
    emitDocId: String,
    onDocIdChange: (String) -> Unit,
    loading: Boolean,
    onEmit: () -> Unit,
    onCheckStatus: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            modifier = Modifier.widthIn(max = 420.dp).padding(16.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Emitir Comprobante", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)

                Text("Tipo de Comprobante", color = AppColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("32" to "E32", "31" to "E31", "45" to "E45", "34" to "E34", "33" to "E33").forEach { (code, label) ->
                        val selected = emitDocType == code
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(if (selected) AppColors.Surface else Color.Transparent)
                                .clickable { onDocTypeChange(code) }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                AlanubeField("e-NCF", emitEncf, "E320000000005") { onEncfChange(it.uppercase().take(13)) }
                AlanubeField("Total", emitTotal, "0.00") { onTotalChange(it.filter { c -> c.isDigit() || c == '.' }.take(14)) }

                if (emitDocType != "32") {
                    AlanubeField("RNC Comprador", emitBuyerRnc, "123456789") { onBuyerRncChange(it.take(11)) }
                    AlanubeField("Razon Social Comprador", emitBuyerName, "Cliente SRL") { onBuyerNameChange(it) }
                }

                Divider(color = AppColors.Border)
                Text("Consultar estado de un documento", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                AlanubeField("ID Documento", emitDocId, "ulid del documento") { onDocIdChange(it) }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlanubeButton("Consultar Estado", Icons.Outlined.Search, loading, Modifier.weight(1f), Color(0xFF7C3AED)) {
                        if (emitDocId.isNotBlank()) onCheckStatus()
                    }
                }

                Divider(color = AppColors.Border)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlanubeButton("Cancelar", Icons.Outlined.Close, false, Modifier.weight(1f), AppColors.TextSecondary, light = true) { onDismiss() }
                    AlanubeButton("Emitir", Icons.Outlined.Send, loading, Modifier.weight(1f), Color(0xFF0891B2)) {
                        if (emitEncf.isNotBlank() && emitTotal.isNotBlank()) onEmit()
                    }
                }
            }
        }
    }
}
