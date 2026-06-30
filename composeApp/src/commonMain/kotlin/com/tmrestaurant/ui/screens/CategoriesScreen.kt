package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.components.CategoryCard
import com.tmrestaurant.ui.components.CategoryFormData
import com.tmrestaurant.ui.components.CategoryFormModal
import com.tmrestaurant.ui.components.DangerButton
import com.tmrestaurant.ui.components.PrimaryButton
import com.tmrestaurant.ui.components.StatsCard
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.LocalCategoryState
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun CategoriesScreen() {
    if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Categorias",
            subtitle = "Solo los administradores pueden gestionar categorias."
        )
        return
    }
    val catState = LocalCategoryState.current
    var showForm by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Int?>(null) }

    val categories = catState.categories

    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.Background).padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Categorías", color = AppColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Gestión de categorías de productos", color = AppColors.TextSecondary, fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatsCard(label = "Total", value = "${categories.size}")
                StatsCard(label = "Activas", value = "${categories.count { it.active }}")
                StatsCard(label = "POS", value = "${categories.count { it.visiblePos }}")
            }
            Spacer(Modifier.width(16.dp))
            DangerButton(text = "Vaciar tabla", icon = Icons.Outlined.Delete, onClick = { })
            Spacer(Modifier.width(10.dp))
            PrimaryButton(text = "+ Nueva Categoría", icon = Icons.Outlined.Add, onClick = { editingId = null; showForm = true })
        }

        Spacer(Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(320.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryCard(
                    category = category,
                    onEdit = { editingId = category.id; showForm = true },
                    onDelete = { catState.delete(category.id) }
                )
            }
        }
    }

    if (showForm) {
        val initial = if (editingId != null) {
            val c = catState.categories.find { it.id == editingId }
            CategoryFormData(
                name = c?.name ?: "",
                description = c?.description ?: "",
                colorType = c?.colorType ?: com.tmrestaurant.ui.data.CategoryColorType.Purple,
                active = c?.active ?: true,
                visiblePos = c?.visiblePos ?: true,
                order = (c?.order ?: 0).toString()
            )
        } else CategoryFormData()

        CategoryFormModal(
            initial = initial,
            isEditing = editingId != null,
            onDismiss = { showForm = false; editingId = null },
            onSave = { data ->
                if (editingId != null) {
                    val old = catState.categories.find { it.id == editingId }
                    if (old != null) {
                        catState.update(old.copy(
                            name = data.name, description = data.description,
                            colorType = data.colorType, active = data.active,
                            visiblePos = data.visiblePos, order = data.order.toIntOrNull() ?: 0
                        ))
                    }
                } else {
                    catState.add(com.tmrestaurant.ui.data.Category(
                        id = catState.nextId,
                        name = data.name, description = data.description,
                        colorType = data.colorType, active = data.active,
                        visiblePos = data.visiblePos, order = data.order.toIntOrNull() ?: 0
                    ))
                }
                showForm = false; editingId = null
            }
        )
    }
}
