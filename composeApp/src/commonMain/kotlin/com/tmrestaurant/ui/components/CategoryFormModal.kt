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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.Category
import com.tmrestaurant.ui.data.CategoryColorType
import com.tmrestaurant.ui.theme.AppColors

data class CategoryFormData(
    val name: String = "",
    val description: String = "",
    val colorType: CategoryColorType = CategoryColorType.Purple,
    val active: Boolean = true,
    val visiblePos: Boolean = true,
    val order: String = "0"
)

@Composable
fun CategoryFormModal(
    initial: CategoryFormData = CategoryFormData(),
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (CategoryFormData) -> Unit
) {
    var form by remember { mutableStateOf(initial) }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(560.dp).clip(RoundedCornerShape(22.dp)).background(AppColors.Surface)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isEditing) "Editar Categoría" else "Nueva Categoría",
                    color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(20.dp))
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))

            Column(
                Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CatInput("Nombre", form.name, { form = form.copy(name = it) }, "Nombre de la categoría")
                CatInput("Descripción", form.description, { form = form.copy(description = it) }, "Descripción de la categoría")

                CatColorSelector(
                    selected = form.colorType,
                    onSelect = { form = form.copy(colorType = it) }
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CatInput("Orden", form.order, { form = form.copy(order = it) }, "0", Modifier.weight(1f))
                }

                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Background).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CatCheckbox("Categoría activa", form.active) { form = form.copy(active = it) }
                    CatCheckbox("Visible en POS", form.visiblePos) { form = form.copy(visiblePos = it) }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
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
                        .clickable { onSave(form) }.padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isEditing) "Guardar Cambios" else "Crear Categoría", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CatInput(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) Text(placeholder, color = AppColors.Gray, fontSize = 14.sp)
            BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun CatColorSelector(selected: CategoryColorType, onSelect: (CategoryColorType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val colorMap = mapOf(
        CategoryColorType.Orange to "Naranja",
        CategoryColorType.Purple to "Morado",
        CategoryColorType.Green to "Verde",
        CategoryColorType.Gray to "Gris"
    )
    Column {
        Text("Color", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Box {
            Row(
                Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).clickable { expanded = true }.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(colorMap[selected] ?: "Morado", color = AppColors.TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = AppColors.IconGray, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                colorMap.entries.forEach { (type, label) ->
                    DropdownMenuItem(text = { Text(label, fontSize = 14.sp) }, onClick = { onSelect(type); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun CatCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary, checkmarkColor = Color.White))
        Spacer(Modifier.width(8.dp))
        Text(label, color = AppColors.TextPrimary, fontSize = 14.sp)
    }
}
