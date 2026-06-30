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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.cloud.CloudConfig
import com.tmrestaurant.cloud.CloudStatus
import com.tmrestaurant.cloud.SyncMode
import com.tmrestaurant.cloud.SyncResult
import com.tmrestaurant.cloud.TMCloudService
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.CategoryState
import com.tmrestaurant.ui.data.ProductState
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun CloudScreen() {
    if (!AccessControl.canAccessCloud(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "TM Cloud",
            subtitle = "Solo los administradores pueden acceder a TM Cloud."
        )
        return
    }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val initial = remember { TMCloudService.loadSavedConfig() }
    val productState = com.tmrestaurant.ui.data.LocalProductState.current
    val categoryState = com.tmrestaurant.ui.data.LocalCategoryState.current

    var url by remember { mutableStateOf(initial.url) }
    var publicKey by remember { mutableStateOf(initial.publicKey) }
    var secretKey by remember { mutableStateOf(initial.secretKey) }
    var mode by remember { mutableStateOf(initial.mode) }
    var autoSync by remember { mutableStateOf(initial.autoSync) }
    var intervalText by remember { mutableStateOf(initial.intervalSec.toString()) }
    var status by remember { mutableStateOf<CloudStatus?>(null) }
    var result by remember { mutableStateOf<SyncResult?>(null) }
    var loading by remember { mutableStateOf("") }
    var lastSync by remember { mutableStateOf<Long?>(null) }

    val allEssentialTables = listOf("usuarios", "productos", "categorias", "clientes", "proveedores", "facturas", "gastos")
    var showTableSelector by remember { mutableStateOf(false) }
    var selectedTables by remember { mutableStateOf(allEssentialTables.toSet()) }
    var tableDownloadResults by remember { mutableStateOf<Map<String, SyncResult>?>(null) }
    var currentlyDownloading by remember { mutableStateOf<String?>(null) }
    var expandedResponse by remember { mutableStateOf<String?>(null) }

    fun config(): CloudConfig = CloudConfig(
        url = url.trim().trimEnd('/'),
        publicKey = publicKey.trim(),
        secretKey = secretKey.trim(),
        mode = mode,
        autoSync = autoSync,
        intervalSec = intervalText.toIntOrNull()?.coerceIn(10, 86400) ?: 30
    )

    fun runAction(name: String, block: suspend () -> SyncResult) {
        if (loading.isNotEmpty()) return
        loading = name
        result = null
        TMCloudService.init(config())
        scope.launch {
            result = runCatching { block() }
                .getOrElse { SyncResult(false, it.message ?: "Error de sincronizacion", errors = 1) }
            lastSync = System.currentTimeMillis()
            loading = ""
            if (name == "download" || name == "changes" || name == "full") {
                productState.reload()
                categoryState.reload()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (initial.url.isNotBlank() && initial.publicKey.isNotBlank()) {
            loading = "test"
            status = TMCloudService.testConnection()
            loading = ""
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().background(AppColors.Background).verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CloudHeader(status)

            CloudCard("Configuracion", Icons.Outlined.Settings) {
                CloudField("URL API del proyecto", url, "https://tu-dominio.com/api/prj_xxx") { url = it }
                Text("Copiala desde TMPBase. Debe terminar en /api/prj_xxx.", fontSize = 10.sp, color = AppColors.TextSecondary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CloudField("Public Key  |  Lectura", publicKey, "tmp_public_...", Modifier.weight(1f), password = true) { publicKey = it }
                    CloudField("Secret Key  |  Escritura", secretKey, "tmp_secret_...", Modifier.weight(1f), password = true) { secretKey = it }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CloudButton("Guardar", Icons.Outlined.Check, loading == "save", Modifier.weight(1f), Color(0xFF16A34A)) {
                        if (secretKey.isBlank()) {
                            status = CloudStatus(false, "La Secret Key es requerida para sincronizar")
                        } else {
                            loading = "save"
                            TMCloudService.saveConfig(config())
                            scope.launch {
                                status = TMCloudService.testConnection()
                                loading = ""
                            }
                        }
                    }
                    CloudButton("Probar conexion", Icons.Outlined.Power, loading == "test", Modifier.weight(1f), AppColors.TextSecondary, light = true) {
                        loading = "test"
                        TMCloudService.init(config())
                        scope.launch {
                            status = TMCloudService.testConnection()
                            loading = ""
                        }
                    }
                    CloudButton("Panel Admin", Icons.Outlined.OpenInNew, false, Modifier.weight(1f), AppColors.Primary, outlined = true) {
                        val base = url.trim().trimEnd('/').replace(Regex("/api/prj_[A-Za-z0-9]+$", RegexOption.IGNORE_CASE), "")
                        if (base.startsWith("http")) uriHandler.openUri(base)
                        else status = CloudStatus(false, "Configura una URL valida")
                    }
                }
            }

            status?.let { CloudConnectionStatus(it) }

            CloudCard("Sincronizacion", Icons.Outlined.Sync) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Modo", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                        Text(
                            when (mode) {
                                SyncMode.OFFLINE -> "Solo local"
                                SyncMode.ONLINE -> "Subida a la nube"
                                SyncMode.AMBOS -> "Bidireccional"
                            },
                            fontSize = 11.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                    Row(Modifier.clip(RoundedCornerShape(11.dp)).background(AppColors.Background).padding(4.dp)) {
                        listOf(
                            SyncMode.OFFLINE to "Offline",
                            SyncMode.ONLINE to "Online",
                            SyncMode.AMBOS to "Ambos"
                        ).forEach { (value, label) ->
                            Box(
                                Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(if (mode == value) AppColors.Surface else Color.Transparent)
                                    .clickable { mode = value }.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = if (mode == value) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Divider(color = AppColors.Border)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Auto Sync", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Cada ${intervalText.ifBlank { "30" }} segundos", fontSize = 11.sp, color = AppColors.TextSecondary)
                    }
                    Switch(checked = autoSync, onCheckedChange = { autoSync = it })
                }
                if (autoSync) {
                    CloudField("Intervalo (segundos)", intervalText, "30") {
                        intervalText = it.filter(Char::isDigit).take(5)
                    }
                }
                Text(
                    "Guarda los cambios para aplicar el modo y la sincronizacion automatica.",
                    fontSize = 10.sp,
                    color = AppColors.TextSecondary
                )
            }

            CloudCard("Acciones", Icons.Outlined.Bolt) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CloudButton("Crear Tablas", Icons.Outlined.Storage, loading == "tables", Modifier.weight(1f), AppColors.TextSecondary, light = true) {
                        runAction("tables") { TMCloudService.createTables() }
                    }
                    CloudButton("Enviar Todo", Icons.Outlined.CloudUpload, loading == "push", Modifier.weight(1f), Color(0xFF7C3AED)) {
                        runAction("push") {
                            val all = TMCloudService.pushAllTables()
                            val errors = all.sumOf { it.errors }
                            SyncResult(
                                success = errors == 0,
                                message = "${all.size} tablas procesadas",
                                inserts = all.sumOf { it.inserts },
                                errors = errors
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CloudButton("Sync Cambios", Icons.Outlined.CompareArrows, loading == "changes", Modifier.weight(1f), Color(0xFF0284C7)) {
                        runAction("changes") { TMCloudService.syncChanges() }
                    }
                    CloudButton("Sync Completo", Icons.Outlined.Sync, loading == "full", Modifier.weight(1f), AppColors.Primary) {
                        runAction("full") { TMCloudService.fullSync() }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CloudButton("Descarga Selectiva", Icons.Outlined.Download, false, Modifier.weight(1f), Color(0xFF0891B2)) {
                        selectedTables = allEssentialTables.toSet()
                        tableDownloadResults = null
                        expandedResponse = null
                        showTableSelector = true
                    }
                }
            }

            if (loading in setOf("tables", "push", "changes", "full")) {
                CloudProgressCard(loading)
            }
            result?.let { CloudResultCard(it, lastSync) }

            tableDownloadResults?.let { results ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Resultados de Descarga", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        results.entries.forEach { (table, res) ->
                            val isError = !res.success
                            val icon = if (currentlyDownloading == table) Icons.Outlined.HourglassEmpty
                                else if (isError) Icons.Outlined.Close else Icons.Outlined.Check
                            val iconColor = if (currentlyDownloading == table) Color(0xFFFBBF24)
                                else if (isError) Color(0xFFEF4444) else Color(0xFF16A34A)
                            val isExpanded = expandedResponse == table
                            Column {
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        if (res.responseBody.isNotBlank()) {
                                            expandedResponse = if (isExpanded) null else table
                                        }
                                    }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (currentlyDownloading == table) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = iconColor)
                                    } else {
                                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(table, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                                    Text(res.message, fontSize = 11.sp, color = if (isError) Color(0xFFEF4444) else AppColors.TextSecondary)
                                    if (res.responseBody.isNotBlank()) {
                                        Icon(
                                            if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                            null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            if (isExpanded && res.responseBody.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppColors.Background,
                                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, bottom = 4.dp).heightIn(max = 150.dp)
                                ) {
                                    Box(Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
                                        Text(
                                            res.responseBody,
                                            fontSize = 10.sp,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                }
                            }
                                Divider(color = AppColors.Border, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }

    if (loading.isNotEmpty() && loading != "selectiva") {
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = AppColors.Primary
                    )
                    Text(
                        when (loading) {
                            "tables" -> "Creando tablas en el servidor..."
                            "push" -> "Enviando datos a la nube..."
                            "changes" -> "Sincronizando cambios..."
                            "full" -> "Sincronizacion completa en progreso..."
                            "download" -> "Descargando tablas esenciales\n(usuarios, productos, categorias, clientes, proveedores, facturas, gastos)..."
                            "test" -> "Probando conexion..."
                            "save" -> "Guardando configuracion..."
                            else -> "Procesando..."
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )
                }
            }
        }
    }

    if (showTableSelector) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                modifier = Modifier.widthIn(max = 320.dp).padding(16.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Seleccionar Tablas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("Selecciona las tablas que deseas descargar:", fontSize = 12.sp, color = AppColors.TextSecondary)
                    Divider(color = AppColors.Border)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedTables.size == allEssentialTables.size,
                            onCheckedChange = { c ->
                                selectedTables = if (c) allEssentialTables.toSet() else emptySet()
                            }
                        )
                        Text("Seleccionar todo", fontSize = 13.sp, color = AppColors.Primary, fontWeight = FontWeight.Medium)
                    }
                    allEssentialTables.forEach { table ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = table in selectedTables,
                                onCheckedChange = { c ->
                                    selectedTables = if (c) selectedTables + table else selectedTables - table
                                }
                            )
                            Text(table, fontSize = 13.sp, color = AppColors.TextPrimary)
                        }
                    }
                    Divider(color = AppColors.Border)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CloudButton("Cancelar", Icons.Outlined.Close, false, Modifier.weight(1f), AppColors.TextSecondary, light = true) {
                            showTableSelector = false
                        }
                        CloudButton("Descargar", Icons.Outlined.Download, currentlyDownloading != null, Modifier.weight(1f), Color(0xFF0891B2)) {
                            if (selectedTables.isEmpty()) return@CloudButton
                            showTableSelector = false
                            tableDownloadResults = emptyMap()
                            currentlyDownloading = null
                            expandedResponse = null
                                TMCloudService.init(config())
                                scope.launch {
                                    try {
                                        val sorted = allEssentialTables.filter { it in selectedTables }
                                        sorted.forEach { table ->
                                            currentlyDownloading = table
                                            val res = TMCloudService.pullTable(table)
                                            tableDownloadResults = (tableDownloadResults ?: emptyMap()) + (table to res)
                                        }
                                    } catch (e: Exception) {
                                        val table = currentlyDownloading ?: "error"
                                        tableDownloadResults = (tableDownloadResults ?: emptyMap()) + (table to SyncResult(false, e.message ?: "Error", errors = 1, responseBody = e.toString()))
                                    }
                                    currentlyDownloading = null
                                    productState.reload()
                                    categoryState.reload()
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudHeader(status: CloudStatus?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF7C3AED)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Cloud, null, tint = Color.White, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("TM Cloud", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Text("Sincronizacion privada con TMPBase", fontSize = 12.sp, color = AppColors.TextSecondary)
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (connected) Color(0xFF15803D) else Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
private fun CloudConnectionStatus(status: CloudStatus) {
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
                Text(if (status.connected) "Conexion exitosa" else status.error.orEmpty(), color = color, fontWeight = FontWeight.SemiBold)
            }
            if (status.connected) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CloudStat("Proyecto", status.projectName ?: "-", Modifier.weight(1f))
                    CloudStat("UID", status.projectUid ?: "-", Modifier.weight(1f))
                    CloudStat("Tablas", status.tableCount?.toString() ?: "-", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CloudCard(
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
private fun CloudField(
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
                    Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Mostrar clave", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CloudButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    loading: Boolean,
    modifier: Modifier,
    color: Color,
    light: Boolean = false,
    outlined: Boolean = false,
    onClick: () -> Unit
) {
    val background = when {
        outlined -> Color.Transparent
        light -> AppColors.Background
        else -> color
    }
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
private fun CloudStat(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label.uppercase(), fontSize = 9.sp, color = AppColors.TextSecondary)
    }
}

@Composable
private fun CloudProgressCard(action: String) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp, color = AppColors.Primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    when (action) {
                        "tables" -> "Creando tablas..."
                        "push" -> "Enviando todos los datos..."
                        "changes" -> "Sincronizando cambios..."
                        "download" -> "Descargando tablas esenciales..."
                        else -> "Ejecutando sincronizacion completa..."
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text("Espera mientras TM Cloud procesa la informacion.", fontSize = 10.sp, color = AppColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun CloudResultCard(result: SyncResult, lastSync: Long?) {
    val color = if (result.success) Color(0xFF16A34A) else Color(0xFFEF4444)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (result.success) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(if (result.success) Icons.Outlined.Check else Icons.Outlined.Close, null, tint = color)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(result.message, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                Text("Ultima ejecucion: ${lastSync ?: "--"}", fontSize = 9.sp, color = AppColors.TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (result.inserts > 0) Text("+${result.inserts} nuevos", fontSize = 11.sp, color = Color(0xFF16A34A))
                    if (result.updates > 0) Text("${result.updates} actualizados", fontSize = 11.sp, color = Color(0xFFD97706))
                    if (result.errors > 0) Text("${result.errors} errores", fontSize = 11.sp, color = Color(0xFFEF4444))
                }
            }
        }
    }
}
