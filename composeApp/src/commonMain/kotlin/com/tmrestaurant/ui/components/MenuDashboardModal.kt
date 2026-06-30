package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun MenuDashboardModal(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onDismiss: () -> Unit
) {
    val all = allScreenItems().filter { AccessControl.canAccess(it.screen, TurnoManager.currentUser) }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(780.dp).clip(RoundedCornerShape(22.dp)).background(AppColors.Surface).padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Menú del Sistema", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(18.dp))
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.heightIn(max = 520.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(all) { (screen, icon) ->
                    MenuCard(
                        title = screen.title,
                        icon = icon,
                        isActive = screen == currentScreen,
                        onClick = { onNavigate(screen) }
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    Modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.DangerLight).clickable(onClick = onDismiss).padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cerrar", color = AppColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MenuCard(title: String, icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    val bg = if (isActive) AppColors.PrimaryLight else AppColors.Background
    val fg = if (isActive) AppColors.Primary else AppColors.TextPrimary

    Column(
        Modifier.height(100.dp).clip(RoundedCornerShape(14.dp)).background(bg).clickable(onClick = onClick).padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (isActive) AppColors.Primary.copy(alpha = 0.15f) else AppColors.Background), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = fg, fontSize = 12.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 14.sp)
    }
}

private data class ScreenItem(val screen: Screen, val icon: ImageVector)

private fun allScreenItems(): List<ScreenItem> = listOf(
    ScreenItem(Screen.Dashboard, Icons.Outlined.GridView),
    ScreenItem(Screen.POS, Icons.Outlined.ShoppingCart),
    ScreenItem(Screen.Comandas, Icons.Outlined.RestaurantMenu),
    ScreenItem(Screen.Mesas, Icons.Outlined.GridView),
    ScreenItem(Screen.Clientes, Icons.Outlined.People),
    ScreenItem(Screen.Reservaciones, Icons.Outlined.Event),
    ScreenItem(Screen.CuentasCobrar, Icons.Outlined.AccountBalanceWallet),
    ScreenItem(Screen.Usuarios, Icons.Outlined.People),
    ScreenItem(Screen.Empleados, Icons.Outlined.Badge),
    ScreenItem(Screen.Products, Icons.Outlined.Inventory2),
    ScreenItem(Screen.Inventario, Icons.Outlined.Inventory2),
    ScreenItem(Screen.Categories, Icons.Outlined.Category),
    ScreenItem(Screen.Extras, Icons.Outlined.Extension),
    ScreenItem(Screen.Modifiers, Icons.Outlined.Build),
    ScreenItem(Screen.Proveedores, Icons.Outlined.LocalShipping),
    ScreenItem(Screen.Facturas, Icons.Outlined.Receipt),
    ScreenItem(Screen.Reportes, Icons.Outlined.TrendingUp),
    ScreenItem(Screen.Cotizaciones, Icons.Outlined.Description),
    ScreenItem(Screen.Delivery, Icons.Outlined.LocalShipping),
    ScreenItem(Screen.Caja, Icons.Outlined.Calculate),
    ScreenItem(Screen.Turnos, Icons.Outlined.History),
    ScreenItem(Screen.Settings, Icons.Outlined.Settings),
    ScreenItem(Screen.Cloud, Icons.Outlined.Cloud),
    ScreenItem(Screen.Recetas, Icons.Outlined.Description),
    ScreenItem(Screen.OrdenesCompra, Icons.Outlined.ShoppingCart),
)
