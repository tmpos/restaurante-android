package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.ImageFromBytes
import com.tmrestaurant.platform.rememberImagePickerLauncher
import com.tmrestaurant.ui.data.LocalCategoryState
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.ModifierManager
import com.tmrestaurant.ui.data.Product
import com.tmrestaurant.ui.data.ProductFormData
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ProductFormModal(
    initial: ProductFormData = ProductFormData(),
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (ProductFormData) -> Unit
) {
    var form by remember { mutableStateOf(initial) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    val productState = LocalProductState.current
    val categoryState = LocalCategoryState.current
    val categoryOptions = listOf("Sin categoría") + categoryState.categories.map { it.name }

    LaunchedEffect(initial.imagePath) {
        if (!initial.imagePath.isNullOrEmpty()) {
            imageBytes = productState.getImageBytes(initial.imagePath)
        }
    }

    fun validate(): Boolean {
        val e = mutableMapOf<String, String>()
        if (form.name.isBlank()) e["name"] = "El nombre es obligatorio"
        val price = form.price.toDoubleOrNull() ?: -1.0
        if (price < 0) e["price"] = "Precio inválido"
        form.cost.toDoubleOrNull().let { if (it != null && it < 0) e["cost"] = "Costo inválido" }
        form.taxPercent.toDoubleOrNull().let { if (it != null && it < 0) e["tax"] = "Impuesto inválido" }
        form.stock.toIntOrNull().let { if (it != null && it < 0) e["stock"] = "Stock inválido" }
        errors = e
        return e.isEmpty()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(1000.dp).heightIn(max = 740.dp)
                .clip(RoundedCornerShape(22.dp)).background(AppColors.Surface)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isEditing) "Editar Producto" else "Nuevo Producto",
                    color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(20.dp))
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))

            Row(
                Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormInput(
                        label = "Nombre *",
                        value = form.name,
                        onValueChange = { form = form.copy(name = it) },
                        placeholder = "Nombre del producto",
                        error = errors["name"]
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormInput(
                            label = "Código interno",
                            value = form.code,
                            onValueChange = { form = form.copy(code = it) },
                            placeholder = "PRD001",
                            modifier = Modifier.weight(1f)
                        )
                        FormInput(
                            label = "Código de barras",
                            value = form.barcode,
                            onValueChange = { form = form.copy(barcode = it) },
                            placeholder = "7501234567890",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FormSelect(
                        label = "Categoría",
                        value = form.category,
                        options = categoryOptions,
                        onSelect = { form = form.copy(category = it) }
                    )
                    FormTextArea(
                        label = "Descripción",
                        value = form.description,
                        onValueChange = { form = form.copy(description = it) },
                        placeholder = "Descripción del producto..."
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormInput(
                            label = "Precio de venta *",
                            value = form.price,
                            onValueChange = { form = form.copy(price = it) },
                            placeholder = "0",
                            modifier = Modifier.weight(1f),
                            error = errors["price"]
                        )
                        FormInput(
                            label = "Precio de costo",
                            value = form.cost,
                            onValueChange = { form = form.copy(cost = it) },
                            placeholder = "0",
                            modifier = Modifier.weight(1f),
                            error = errors["cost"]
                        )
                    }
                    FormInput(
                        label = "Impuesto (%)",
                        value = form.taxPercent,
                        onValueChange = { form = form.copy(taxPercent = it) },
                        placeholder = "18"
                    )
                }

                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ImagePickerSection(
                        imagePath = form.imagePath,
                        imageBytes = imageBytes,
                        onImageSelected = { path, bytes ->
                            form = form.copy(imagePath = path)
                            imageBytes = bytes
                            if (bytes != null) productState.cacheImage(path, bytes)
                        }
                    )

                    OptionsSection(
                        active = form.active,
                        sellInPos = form.sellInPos,
                        sendToKitchen = form.sendToKitchen,
                        sendToBar = form.sendToBar,
                        favorite = form.favorite,
                        controlInventory = form.controlInventory,
                        onActiveChange = { form = form.copy(active = it) },
                        onSellInPosChange = { form = form.copy(sellInPos = it) },
                        onSendToKitchenChange = { form = form.copy(sendToKitchen = it) },
                        onSendToBarChange = { form = form.copy(sendToBar = it) },
                        onFavoriteChange = { form = form.copy(favorite = it) },
                        onControlInventoryChange = { form = form.copy(controlInventory = it) }
                    )

                    if (ModifierManager.groups.isNotEmpty()) {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Background).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Grupos de modificadores", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Seleccione los grupos disponibles para este producto", color = AppColors.TextSecondary, fontSize = 11.sp)
                            ModifierManager.groups.forEach { group ->
                                val isAssigned = form.modifierGroupIds.split(",").contains(group.id)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    val ids = form.modifierGroupIds.split(",").filter { it.isNotBlank() }.toMutableList()
                                    if (isAssigned) ids.remove(group.id) else ids.add(group.id)
                                    form = form.copy(modifierGroupIds = ids.joinToString(","))
                                }) {
                                    Checkbox(checked = isAssigned, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary, checkmarkColor = Color.White))
                                    Spacer(Modifier.width(6.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(group.name, color = AppColors.TextPrimary, fontSize = 13.sp)
                                        Text("${group.options.size} opcion(es) · Max ${group.maxSelections}", color = AppColors.TextSecondary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (form.controlInventory) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FormInput(
                                label = "Stock",
                                value = form.stock,
                                onValueChange = { form = form.copy(stock = it) },
                                placeholder = "0",
                                modifier = Modifier.weight(1f),
                                error = errors["stock"]
                            )
                            FormInput(
                                label = "Alerta de stock",
                                value = form.stockAlert,
                                onValueChange = { form = form.copy(stockAlert = it) },
                                placeholder = "1",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onDismiss)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                        .clickable {
                            if (validate()) onSave(form)
                        }.padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isEditing) "Guardar Cambios" else "Crear Producto",
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    Column(modifier = modifier) {
        Text(label, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(10.dp))
                .background(if (error != null) AppColors.DangerLight else AppColors.Background)
                .border(1.dp, if (error != null) AppColors.Danger else AppColors.Border, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(placeholder, color = AppColors.Gray, fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                modifier = Modifier.fillMaxSize()
            )
        }
        if (error != null) {
            Text(error, color = AppColors.Danger, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun FormTextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Text(label, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(10.dp))
                .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            if (value.isEmpty()) Text(placeholder, color = AppColors.Gray, fontSize = 14.sp)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FormSelect(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Box {
            Row(
                Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                    .clickable { expanded = true }.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, color = if (value == "Sin categoría") AppColors.Gray else AppColors.TextPrimary,
                    fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = AppColors.IconGray, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, fontSize = 14.sp) },
                        onClick = { onSelect(opt); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary, checkmarkColor = Color.White)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = AppColors.TextPrimary, fontSize = 14.sp)
    }
}

@Composable
private fun ImagePickerSection(
    imagePath: String?,
    imageBytes: ByteArray?,
    onImageSelected: (path: String, bytes: ByteArray?) -> Unit
) {
    val launchPicker = rememberImagePickerLauncher { name, bytes ->
        onImageSelected(name, bytes)
    }
    val hasImage = imagePath != null

    Column {
        Text("Imagen del producto", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                    .clickable { launchPicker() }.padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (hasImage) "Cambiar Imagen" else "Seleccionar Imagen", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (hasImage) {
                Box(
                    Modifier.height(48.dp).clip(RoundedCornerShape(12.dp))
                        .background(AppColors.DangerLight).clickable { onImageSelected("", null) }.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Quitar", color = AppColors.Danger, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Formatos: JPG, PNG, GIF, WebP", color = AppColors.TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(14.dp))
                .background(AppColors.PlaceholderBg).border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (hasImage && imageBytes != null) {
                ImageFromBytes(bytes = imageBytes, modifier = Modifier.fillMaxSize())
            } else if (hasImage) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = AppColors.Success, modifier = Modifier.size(52.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Imagen seleccionada", color = AppColors.Success, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(imagePath ?: "", color = AppColors.TextSecondary, fontSize = 11.sp)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Image, null, tint = AppColors.Gray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Vista previa", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun OptionsSection(
    active: Boolean, sellInPos: Boolean, sendToKitchen: Boolean,
    sendToBar: Boolean, favorite: Boolean, controlInventory: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onSellInPosChange: (Boolean) -> Unit,
    onSendToKitchenChange: (Boolean) -> Unit,
    onSendToBarChange: (Boolean) -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    onControlInventoryChange: (Boolean) -> Unit
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Background)
            .padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        FormCheckbox("Producto activo", active, onActiveChange)
        FormCheckbox("Vendible en POS", sellInPos, onSellInPosChange)
        FormCheckbox("Enviar a cocina", sendToKitchen, onSendToKitchenChange)
        FormCheckbox("Enviar a bar", sendToBar, onSendToBarChange)
        FormCheckbox("Marcar como favorito", favorite, onFavoriteChange)
        FormCheckbox("Controlar inventario", controlInventory, onControlInventoryChange)
    }
}
