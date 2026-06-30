package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tmrestaurant.ui.data.SupportStorage
import com.tmrestaurant.ui.data.SupportTable
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun SupportDatabaseSection() {
    var tableFiles by remember { mutableStateOf(SupportStorage.tableFiles()) }
    var selectedFile by remember { mutableStateOf(tableFiles.firstOrNull().orEmpty()) }
    var table by remember(selectedFile) {
        mutableStateOf(if (selectedFile.isBlank()) null else SupportStorage.load(selectedFile))
    }
    var search by remember { mutableStateOf("") }
    var rowLimit by remember { mutableStateOf("100") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var addingRow by remember { mutableStateOf(false) }
    var deleteIndex by remember { mutableStateOf<Int?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showBackup by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    fun reload() {
        tableFiles = SupportStorage.tableFiles()
        if (selectedFile !in tableFiles) selectedFile = tableFiles.firstOrNull().orEmpty()
        table = selectedFile.takeIf { it.isNotBlank() }?.let(SupportStorage::load)
    }

    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Soporte de base local", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text(
                    "Inspecciona y administra las tablas TSV persistentes del dispositivo.",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }
            SupportAction("Recargar", Icons.Outlined.Refresh) { reload(); message = "Datos recargados." }
        }

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF7ED))
                .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(12.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.WarningAmber, null, tint = Color(0xFFEA580C))
            Spacer(Modifier.width(10.dp))
            Text(
                "Herramienta avanzada. Haz una copia antes de editar. Reinicia la app despues de modificar datos para recargar todos los modulos.",
                fontSize = 12.sp,
                color = Color(0xFF9A3412)
            )
        }

        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(
                Modifier.width(210.dp).fillMaxHeight().clip(RoundedCornerShape(14.dp))
                    .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Storage, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tablas (${tableFiles.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(tableFiles.size) { index ->
                        val file = tableFiles[index]
                        val active = file == selectedFile
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                                .background(if (active) AppColors.PrimaryLight else Color.Transparent)
                                .clickable {
                                    selectedFile = file
                                    table = SupportStorage.load(file)
                                    search = ""
                                }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.TableChart, null, tint = if (active) AppColors.Primary else AppColors.IconGray, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                file.substringBefore(".v1").substringBeforeLast("."),
                                fontSize = 12.sp,
                                color = if (active) AppColors.Primary else AppColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (table == null) {
                Box(
                    Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Storage, null, tint = AppColors.IconGray, modifier = Modifier.size(46.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No hay tablas locales", color = AppColors.TextSecondary)
                    }
                }
            } else {
                SupportTablePanel(
                    table = table!!,
                    search = search,
                    onSearch = { search = it },
                    rowLimit = rowLimit,
                    onRowLimit = { rowLimit = it.filter(Char::isDigit).take(3) },
                    onEdit = { editingIndex = it },
                    onDelete = { deleteIndex = it },
                    onAdd = { addingRow = true },
                    onBackup = { showBackup = true },
                    onClear = { showClearConfirm = true }
                )
            }
        }
        if (message.isNotBlank()) {
            Text(message, color = AppColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }

    val currentTable = table
    if (currentTable != null && (editingIndex != null || addingRow)) {
        val index = editingIndex
        SupportRowDialog(
            title = if (addingRow) "Nuevo registro" else "Editar registro #${index!! + 1}",
            columns = currentTable.columns,
            initialValues = if (addingRow) List(currentTable.columns.size) { "" } else currentTable.rows[index!!],
            onDismiss = { editingIndex = null; addingRow = false },
            onSave = { values ->
                val rows = currentTable.rows.toMutableList()
                if (addingRow) rows += values else rows[index!!] = values
                SupportStorage.saveRows(currentTable.fileName, rows)
                editingIndex = null
                addingRow = false
                table = SupportStorage.load(currentTable.fileName)
                message = "Registro guardado. Reinicia la app para aplicar el cambio en todos los modulos."
            }
        )
    }

    if (currentTable != null && deleteIndex != null) {
        SupportConfirmDialog(
            title = "Eliminar registro",
            message = "Se eliminara permanentemente la fila #${deleteIndex!! + 1} de ${currentTable.displayName}.",
            confirmText = "Eliminar",
            onDismiss = { deleteIndex = null },
            onConfirm = {
                val rows = currentTable.rows.toMutableList().also { it.removeAt(deleteIndex!!) }
                SupportStorage.saveRows(currentTable.fileName, rows)
                deleteIndex = null
                table = SupportStorage.load(currentTable.fileName)
                message = "Registro eliminado."
            }
        )
    }

    if (currentTable != null && showClearConfirm) {
        SupportConfirmDialog(
            title = "Vaciar tabla",
            message = "Se eliminaran los ${currentTable.rows.size} registros de ${currentTable.displayName}. Esta accion no se puede deshacer.",
            confirmText = "Vaciar tabla",
            onDismiss = { showClearConfirm = false },
            onConfirm = {
                SupportStorage.clear(currentTable.fileName)
                showClearConfirm = false
                table = SupportStorage.load(currentTable.fileName)
                message = "Tabla vaciada."
            }
        )
    }

    if (currentTable != null && showBackup) {
        SupportBackupDialog(
            fileName = currentTable.fileName,
            initialContent = SupportStorage.backup(currentTable.fileName),
            onDismiss = { showBackup = false },
            onRestore = { content ->
                SupportStorage.restore(currentTable.fileName, content)
                showBackup = false
                table = SupportStorage.load(currentTable.fileName)
                message = "Contenido restaurado. Reinicia la app para recargar los datos."
            }
        )
    }
}

@Composable
private fun RowScope.SupportTablePanel(
    table: SupportTable,
    search: String,
    onSearch: (String) -> Unit,
    rowLimit: String,
    onRowLimit: (String) -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onAdd: () -> Unit,
    onBackup: () -> Unit,
    onClear: () -> Unit
) {
    val limit = rowLimit.toIntOrNull()?.coerceIn(1, 500) ?: 100
    val filtered = table.rows.withIndex().filter { indexed ->
        search.isBlank() || indexed.value.any { it.contains(search, ignoreCase = true) }
    }.take(limit)

    Column(
        Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(14.dp))
            .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(table.displayName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text(
                    "${table.columns.size} campos | ${table.rows.size} registros | ${table.sizeBytes} bytes",
                    fontSize = 11.sp,
                    color = AppColors.TextSecondary
                )
            }
            SupportAction("Nuevo", Icons.Outlined.Add, Color(0xFF16A34A), onAdd)
            Spacer(Modifier.width(6.dp))
            SupportAction("Copia / Restaurar", Icons.Outlined.ContentCopy, onClick = onBackup)
            Spacer(Modifier.width(6.dp))
            SupportAction("Vaciar", Icons.Outlined.DeleteSweep, AppColors.Danger, onClear)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SupportInput("Buscar en todas las columnas", search, onSearch, Modifier.weight(1f))
            SupportInput("Limite", rowLimit, onRowLimit, Modifier.width(90.dp))
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(AppColors.Background)
                .horizontalScroll(rememberScrollState()).padding(10.dp)
        ) {
            table.columns.forEachIndexed { index, column ->
                Column(Modifier.width(150.dp)) {
                    Text(column, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("Campo ${index + 1}", fontSize = 9.sp, color = AppColors.TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            itemsIndexed(filtered, key = { _, item -> item.index }) { _, indexed ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                        .background(AppColors.Background).clickable { onEdit(indexed.index) }
                        .padding(vertical = 9.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                        indexed.value.forEach { value ->
                            Text(
                                value.ifBlank { "-" },
                                modifier = Modifier.width(150.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp,
                                color = if (value.isBlank()) AppColors.TextSecondary else AppColors.TextPrimary
                            )
                        }
                    }
                    IconButton(onClick = { onDelete(indexed.index) }) {
                        Icon(Icons.Outlined.Delete, "Eliminar", tint = AppColors.Danger, modifier = Modifier.size(18.dp))
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Sin registros", color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportRowDialog(
    title: String,
    columns: List<String>,
    initialValues: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val values = remember(initialValues) { initialValues.map { mutableStateOf(it) } }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.width(680.dp).heightIn(max = 650.dp).clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface).padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "Cerrar") }
            }
            Text("Edita con cuidado: el tipo de cada valor debe coincidir con el modelo original.", fontSize = 11.sp, color = AppColors.TextSecondary)
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(columns.size) { index ->
                    SupportInput(columns[index], values[index].value, { values[index].value = it }, Modifier.fillMaxWidth())
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onSave(values.map { it.value }) }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)) {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun SupportBackupDialog(
    fileName: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    var content by remember { mutableStateOf(initialContent) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.width(760.dp).height(620.dp).clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface).padding(22.dp)
        ) {
            Text("Copia y restauracion", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(fileName, fontSize = 12.sp, color = AppColors.Primary, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AppColors.TextPrimary)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onDismiss) { Text("Cerrar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onRestore(content) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))) {
                    Text("Restaurar contenido")
                }
            }
        }
    }
}

@Composable
private fun SupportConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Danger)) {
                Text(confirmText)
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SupportInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, fontSize = 10.sp, color = AppColors.TextSecondary, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(9.dp))
                .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(9.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 12.sp, color = AppColors.TextPrimary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SupportAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = AppColors.Primary,
    onClick: () -> Unit
) {
    Row(
        Modifier.height(38.dp).clip(RoundedCornerShape(9.dp)).background(color.copy(alpha = 0.12f))
            .clickable(onClick = onClick).padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
