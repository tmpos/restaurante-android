package com.tmrestaurant.ui.screens.recetas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.Recipe
import com.tmrestaurant.ui.data.RecipeIngredient
import com.tmrestaurant.ui.data.RecipeManager
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.genUid
import com.tmrestaurant.ui.screens.PlaceholderScreen
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun RecetasScreen() {
    if (!AccessControl.canManageRecipes(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Recetas",
            subtitle = "Solo los administradores pueden gestionar recetas."
        )
        return
    }
    val productState = LocalProductState.current
    val products = productState.products
    var search by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }
    var editingRecipe by remember { mutableStateOf<Recipe?>(null) }

    val filtered = RecipeManager.recipes.filter { r ->
        search.isBlank() || r.productName.contains(search, ignoreCase = true)
    }.sortedByDescending { it.updatedAt }

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Recetas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.width(200.dp).height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (search.isEmpty()) Text("Buscar...", color = AppColors.Gray, fontSize = 13.sp)
                BasicTextField(value = search, onValueChange = { search = it }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Primary).clickable { editingRecipe = null; showForm = true }.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Nueva Receta", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.MenuBook, null, tint = AppColors.Gray, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No hay recetas", color = AppColors.TextSecondary, fontSize = 16.sp)
                    Text("Crea recetas para gestionar ingredientes por producto", color = AppColors.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.id }) { recipe ->
                    RecipeCard(recipe, onEdit = { editingRecipe = recipe; showForm = true }, onDelete = { RecipeManager.remove(recipe.productId) })
                }
            }
        }
    }

    if (showForm) {
        RecipeFormModal(
            recipe = editingRecipe,
            products = products,
            onSave = { r -> RecipeManager.addOrUpdate(r); showForm = false },
            onDismiss = { showForm = false }
        )
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.MenuBook, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(recipe.productName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("${recipe.ingredients.size} ingredientes · ${recipe.servings} porciones", fontSize = 12.sp, color = AppColors.TextSecondary)
                }
                Box(
                    Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Edit, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp)) }
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable { showDelete = true },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(16.dp)) }
            }
            if (recipe.ingredients.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                Spacer(Modifier.height(8.dp))
                recipe.ingredients.forEach { ing ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(20.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFF0FDF4)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Circle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(8.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(ing.productName, fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                        Text("${ing.quantity} ${ing.unit}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Primary)
                    }
                }
            }
        }
    }
    if (showDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Eliminar receta") },
            text = { Text("Eliminar receta de ${recipe.productName}?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDelete = false }) { Text("Eliminar", color = AppColors.Danger) } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun RecipeFormModal(recipe: Recipe?, products: List<com.tmrestaurant.ui.data.Product>, onSave: (Recipe) -> Unit, onDismiss: () -> Unit) {
    var selectedProductId by remember { mutableStateOf(recipe?.productId ?: 0) }
    var servings by remember { mutableStateOf(recipe?.servings?.toString() ?: "1") }
    var ingredients by remember { mutableStateOf(recipe?.ingredients ?: emptyList()) }
    var showProductPicker by remember { mutableStateOf(false) }
    var showIngredientPicker by remember { mutableStateOf(false) }

    val selectedProduct = products.find { it.id == selectedProductId }
    val posProducts = products.filter { it.sellInPos }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(520.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (recipe != null) "Editar Receta" else "Nueva Receta", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Producto", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSecondary)
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).clickable { showProductPicker = true }.padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(selectedProduct?.name ?: "Seleccionar producto...", color = if (selectedProduct != null) AppColors.TextPrimary else AppColors.Gray, fontSize = 14.sp)
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Porciones:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSecondary)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.width(80.dp).height(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    BasicTextField(value = servings, onValueChange = { if (it.all { c -> c.isDigit() }) servings = it }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxSize(), singleLine = true)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ingredientes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(
                    Modifier.height(34.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.PrimaryLight).clickable { showIngredientPicker = true }.padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Add, null, tint = AppColors.Primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (ingredients.isEmpty()) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("No hay ingredientes", color = AppColors.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(ingredients) { ing ->
                        var qty by remember { mutableStateOf(ing.quantity.toString()) }
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(ing.productName, fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                            Box(Modifier.width(70.dp).height(32.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Surface).padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                                BasicTextField(value = qty, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) { qty = it; ingredients = ingredients.map { i -> if (i.productId == ing.productId) i.copy(quantity = it.toDoubleOrNull() ?: 0.0) else i } } }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxSize(), singleLine = true)
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(ing.unit, fontSize = 11.sp, color = AppColors.TextSecondary)
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable { ingredients = ingredients.filter { it.productId != ing.productId } }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Close, null, tint = AppColors.Danger, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).border(1.5.dp, AppColors.Border, RoundedCornerShape(10.dp)).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) { Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                Box(
                    Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(10.dp)).background(if (selectedProductId > 0) AppColors.Primary else AppColors.Gray).clickable(enabled = selectedProductId > 0) {
                        onSave(Recipe(
                            id = recipe?.id ?: genUid("rec"),
                            productId = selectedProductId,
                            productName = selectedProduct?.name ?: "",
                            ingredients = ingredients,
                            servings = servings.toIntOrNull() ?: 1
                        ))
                    },
                    contentAlignment = Alignment.Center
                ) { Text("Guardar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showProductPicker) {
        PickerModal(
            items = posProducts,
            title = "Seleccionar Producto",
            onSelect = { p -> selectedProductId = p.id; ingredients = emptyList(); showProductPicker = false },
            onDismiss = { showProductPicker = false }
        )
    }
    if (showIngredientPicker) {
        val available = posProducts.filter { p -> p.id != selectedProductId && ingredients.none { i -> i.productId == p.id } }
        PickerModal(
            items = available,
            title = "Agregar Ingrediente",
            onSelect = { p -> ingredients = ingredients + RecipeIngredient(p.id, p.name, 1.0); showIngredientPicker = false },
            onDismiss = { showIngredientPicker = false }
        )
    }
}

@Composable
private fun PickerModal(items: List<com.tmrestaurant.ui.data.Product>, title: String, onSelect: (com.tmrestaurant.ui.data.Product) -> Unit, onDismiss: () -> Unit) {
    var search by remember { mutableStateOf("") }
    val filtered = items.filter { search.isBlank() || it.name.contains(search, ignoreCase = true) || it.code.contains(search, ignoreCase = true) }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(400.dp).heightIn(max = 500.dp).clip(RoundedCornerShape(16.dp)).background(AppColors.Surface).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                if (search.isEmpty()) Text("Buscar...", color = AppColors.Gray, fontSize = 13.sp)
                BasicTextField(value = search, onValueChange = { search = it }, textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filtered) { product ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable { onSelect(product) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.name, fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("RD\$ ${"%.2f".format(product.price)}", fontSize = 12.sp, color = AppColors.Primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (filtered.isEmpty()) {
                    item { Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { Text("Sin resultados", color = AppColors.Gray, fontSize = 13.sp) } }
                }
            }
        }
    }
}

