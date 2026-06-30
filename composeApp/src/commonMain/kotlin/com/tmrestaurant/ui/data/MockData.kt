package com.tmrestaurant.ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.Color

data class QuickAccess(val title: String, val iconType: String, val colorType: String)
data class RecentOrder(val id: String, val status: String, val time: String, val total: Double)
data class TopSelling(val name: String, val qtySold: Int)

object MockData {

    val categories = listOf(
        Category(1, "COMBOS", "COMBOS", CategoryColorType.Orange, order = 0),
        Category(2, "GUARNICIONES", "GUARNICIONES", CategoryColorType.Purple, order = 0),
        Category(3, "ENTRADA", "Ensaladas frescas", CategoryColorType.Green, order = 6),
        Category(4, "Bebidas", "Bebidas y refrescos", CategoryColorType.Purple, order = 8),
        Category(5, "Extras", "Acompañantes y extras", CategoryColorType.Gray, order = 10)
    )

    val products = listOf(
        Product(id=1, name="OFETA DE APERTURA", code="0000", category="COMBOS", price=290.00, stock=494, favorite=true, description="2 pieza de pollo + 3 nugg..."),
        Product(id=2, name="AGUA", code="1010", category="Bebidas", price=25.00, stock=500),
        Product(id=3, name="ALITAS", code="0018", category="Extras", price=85.00, stock=500),
        Product(id=4, name="COCA COLA 1.25", category="Bebidas", price=125.00, stock=500),
        Product(id=5, name="COCA COLA 16OZ", code="4040", category="Bebidas", price=50.00, stock=500),
        Product(id=6, name="COCA COLA 2 LITRO", code="2525", category="Bebidas", price=200.00, stock=500),
        Product(id=7, name="COMBO 2 PIEZA DE POLLO", code="003", category="COMBOS", price=250.00, stock=500, description="2 pieza de pollo"),
        Product(id=8, name="COMBO DE 4 PIEZA", code="005", category="COMBOS", price=450.00, stock=500, description="4 pieza de pollo"),
        Product(id=9, name="COMBO DE 5 PIEZAS", code="006", category="COMBOS", price=575.00, stock=500, description="5 piezas de pollo"),
        Product(id=10, name="COMBOS DE ALITAS 5 PIEZAS", code="0014", category="Extras", price=400.00, stock=500),
        Product(id=11, name="COMBOS DE ALITAS 6 PIEZA", code="0015", category="Extras", price=450.00, stock=500),
        Product(id=12, name="COMBOS DE ALITAS 8 PIEZA", code="0016", category="Extras", price=600.00, stock=500)
    )

    val posCategories = listOf(
        POSCategory("Favoritos", Icons.Outlined.Star, Color(0xFF111827)),
        POSCategory("COMBOS", Icons.Outlined.Restaurant, Color(0xFFE68A00)),
        POSCategory("GUARNICIONES", Icons.Outlined.Category, Color(0xFF8758F2)),
        POSCategory("ENTRADA", Icons.Outlined.Spa, Color(0xFF22C55E)),
        POSCategory("Bebidas", Icons.Outlined.LocalDrink, Color(0xFF8758F2)),
        POSCategory("Extras", Icons.Outlined.Extension, Color(0xFF9CA3AF))
    )

    val initialCartItems = listOf(
        CartItem(
            product = Product(id=1, name="OFETA DE APERTURA", code="0000", category="COMBOS", price=290.00, stock=494, favorite=true, description="2 pieza de pollo + 3 nugg..."),
            quantity = 1
        )
    )

    val quickAccessList = listOf(
        QuickAccess("Nueva Venta", "cart", "purple"),
        QuickAccess("Comandas", "chef", "orange"),
        QuickAccess("Mesas", "grid", "blue"),
        QuickAccess("Productos", "box", "green"),
        QuickAccess("Facturas", "receipt", "purple"),
        QuickAccess("Clientes", "people", "purple"),
        QuickAccess("Caja", "money", "red")
    )

    val recentOrders = listOf(
        RecentOrder("ORD-20260514-796555", "CERRADA", "07:09 p. m.", 290.00),
        RecentOrder("ORD-20260514-796556", "CERRADA", "07:15 p. m.", 575.00),
        RecentOrder("ORD-20260514-796557", "ABIERTA", "07:20 p. m.", 250.00)
    )

    val topSellingProducts = listOf(
        TopSelling("OFETA DE APERTURA", 12),
        TopSelling("COMBO DE 5 PIEZAS", 8),
        TopSelling("COCA COLA 2 LITRO", 6)
    )
}
