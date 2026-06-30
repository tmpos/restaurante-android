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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ViewList
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.components.DangerButton
import com.tmrestaurant.ui.components.IconButtonBox
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.components.PrimaryButton
import com.tmrestaurant.ui.components.ProductCardAdmin
import com.tmrestaurant.ui.components.ProductFormModal
import com.tmrestaurant.ui.components.SearchInput
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.ProductFormData
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ProductsScreen() {
    if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Productos",
            subtitle = "Solo los administradores pueden gestionar el catalogo de productos."
        )
        return
    }
    val productState = LocalProductState.current
    var isGridView by remember { mutableStateOf(true) }
    var showForm by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Int?>(null) }

    val products = productState.products

    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.Background).padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Productos", color = AppColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("${products.size} productos registrados", color = AppColors.TextSecondary, fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            DangerButton(text = "Vaciar tabla", icon = Icons.Outlined.Delete, onClick = { })
            Spacer(Modifier.width(10.dp))
            PrimaryButton(
                text = "+ Nuevo Producto",
                icon = Icons.Outlined.Add,
                onClick = { editingId = null; showForm = true }
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchInput(value = "", onValueChange = { }, placeholder = "Buscar por nombre, código...", modifier = Modifier.weight(1f))
            Row(
                Modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Todas las categorías", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = com.tmrestaurant.ui.theme.AppColors.IconGray, modifier = Modifier.size(20.dp))
            }
            IconButtonBox(icon = Icons.Outlined.GridView, onClick = { isGridView = true }, isActive = isGridView, size = 48.dp)
            IconButtonBox(icon = Icons.Outlined.ViewList, onClick = { isGridView = false }, isActive = !isGridView, size = 48.dp)
        }

        Spacer(Modifier.height(16.dp))

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCardAdmin(
                        product = product,
                        onEdit = { editingId = product.id; showForm = true },
                        onDelete = { productState.delete(product.id) }
                    )
                }
            }
        } else {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                products.forEach { product ->
                    ProductCardAdmin(
                        product = product,
                        onEdit = { editingId = product.id; showForm = true },
                        onDelete = { productState.delete(product.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showForm) {
        val initial = if (editingId != null) {
            val p = productState.getById(editingId!!)
            ProductFormData(
                name = p?.name ?: "",
                code = p?.code ?: "",
                barcode = p?.barcode ?: "",
                category = if (p?.category?.isNotEmpty() == true) p.category else "Sin categoría",
                description = p?.description ?: "",
                price = if (p != null) p.price.toString() else "0",
                cost = if (p != null) p.cost.toString() else "0",
                taxPercent = if (p != null) p.taxPercent.toString() else "18",
                stock = if (p != null) p.stock.toString() else "0",
                stockAlert = if (p != null) p.stockAlert.toString() else "1",
                imagePath = p?.imagePath,
                active = p?.active ?: true,
                sellInPos = p?.sellInPos ?: true,
                sendToKitchen = p?.sendToKitchen ?: true,
                sendToBar = p?.sendToBar ?: false,
                favorite = p?.favorite ?: false,
                controlInventory = p?.controlInventory ?: true,
                modifierGroupIds = p?.modifierGroupIds ?: ""
            )
        } else ProductFormData()

        ProductFormModal(
            initial = initial,
            isEditing = editingId != null,
            onDismiss = { showForm = false; editingId = null },
            onSave = { data ->
                if (editingId != null) {
                    val old = productState.getById(editingId!!)
                    if (old != null) {
                        productState.update(old.copy(
                            name = data.name, code = data.code, barcode = data.barcode,
                            category = data.category, description = data.description,
                            price = data.price.toDoubleOrNull() ?: 0.0,
                            cost = data.cost.toDoubleOrNull() ?: 0.0,
                            taxPercent = data.taxPercent.toDoubleOrNull() ?: 0.0,
                            stock = data.stock.toIntOrNull() ?: 0,
                            stockAlert = data.stockAlert.toIntOrNull() ?: 1,
                            imagePath = data.imagePath?.ifEmpty { null },
                            active = data.active, sellInPos = data.sellInPos,
                            sendToKitchen = data.sendToKitchen, sendToBar = data.sendToBar,
                            favorite = data.favorite, controlInventory = data.controlInventory,
                            modifierGroupIds = data.modifierGroupIds
                        ))
                    }
                } else {
                    productState.add(com.tmrestaurant.ui.data.Product(
                        id = productState.nextId,
                        name = data.name, code = data.code, barcode = data.barcode,
                        category = data.category, description = data.description,
                        price = data.price.toDoubleOrNull() ?: 0.0,
                        cost = data.cost.toDoubleOrNull() ?: 0.0,
                        taxPercent = data.taxPercent.toDoubleOrNull() ?: 0.0,
                        stock = data.stock.toIntOrNull() ?: 0,
                        stockAlert = data.stockAlert.toIntOrNull() ?: 1,
                        imagePath = data.imagePath?.ifEmpty { null },
                        active = data.active, sellInPos = data.sellInPos,
                        sendToKitchen = data.sendToKitchen, sendToBar = data.sendToBar,
                        favorite = data.favorite, controlInventory = data.controlInventory,
                        modifierGroupIds = data.modifierGroupIds
                    ))
                }
                showForm = false; editingId = null
            }
        )
    }
}
