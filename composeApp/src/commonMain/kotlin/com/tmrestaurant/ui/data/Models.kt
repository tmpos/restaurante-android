package com.tmrestaurant.ui.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class CategoryColorType(val displayName: String) {
    Orange("Naranja"),
    Purple("Morado"),
    Green("Verde"),
    Gray("Gris")
}

data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val colorType: CategoryColorType,
    val active: Boolean = true,
    val visiblePos: Boolean = true,
    val order: Int = 0,
    val uid: String = genUid("cat"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Product(
    val id: Int,
    val name: String,
    val code: String = "",
    val barcode: String = "",
    val category: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val cost: Double = 0.0,
    val taxPercent: Double = 18.0,
    val stock: Int = 0,
    val stockAlert: Int = 1,
    val imagePath: String? = null,
    val active: Boolean = true,
    val sellInPos: Boolean = true,
    val sendToKitchen: Boolean = true,
    val sendToBar: Boolean = false,
    val favorite: Boolean = false,
    val controlInventory: Boolean = true,
    val sellByWeight: Boolean = false,
    val modifierGroupIds: String = "",
    val uid: String = genUid("prod"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun genUid(prefix: String): String {
    val ts = System.currentTimeMillis().toString(36)
    val rand = (1000..9999).random().toString(36)
    return "${prefix}_${ts}_${rand}"
}

data class ProductFormData(
    val name: String = "",
    val code: String = "",
    val barcode: String = "",
    val category: String = "Sin categoría",
    val description: String = "",
    val price: String = "0",
    val cost: String = "0",
    val taxPercent: String = "18",
    val stock: String = "0",
    val stockAlert: String = "1",
    val imagePath: String? = null,
    val active: Boolean = true,
    val sellInPos: Boolean = true,
    val sendToKitchen: Boolean = true,
    val sendToBar: Boolean = false,
    val favorite: Boolean = false,
    val controlInventory: Boolean = true,
    val sellByWeight: Boolean = false,
    val modifierGroupIds: String = ""
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val extrasCost: Double = 0.0,
    val extrasNote: String = "",
    val dinerIndex: Int = 0,
    val weightQuantity: Double = 0.0,
    val selectedModifiers: List<ModifierSelection> = emptyList(),
    val courseType: String = ""
) {
    val effectiveQuantity: Double get() = if (weightQuantity > 0) weightQuantity else quantity.toDouble()
}

data class POSCategory(
    val label: String,
    val icon: ImageVector,
    val iconColor: Color
)
