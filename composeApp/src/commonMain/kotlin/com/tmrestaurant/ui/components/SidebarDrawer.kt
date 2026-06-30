package com.tmrestaurant.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun SidebarLayout(
    isOpen: Boolean,
    currentScreen: Screen,
    onClose: () -> Unit,
    onNavigate: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(220))
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onClose)
            )
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(tween(220)) { -it },
            exit = slideOutHorizontally(tween(220)) { -it }
        ) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(AppColors.Surface)
            ) {
                Column(Modifier.fillMaxSize()) {
                    SidebarHeader(onClose = onClose)

                    Column(
                        Modifier.weight(1f).fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        sidebarScreens().forEach { (screen, icon) ->
                            SidebarMenuItem(
                                title = screen.title,
                                icon = icon,
                                isActive = screen == currentScreen,
                                onClick = { onNavigate(screen) }
                            )
                        }
                    }

                    Box(
                        Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor)
                            .padding(horizontal = 16.dp)
                    )

                    SidebarMenuItem(
                        title = "Cerrar sesión",
                        icon = Icons.Outlined.ExitToApp,
                        isActive = false,
                        isDanger = true,
                        onClick = { /* snackbar or dialog later */ },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SidebarHeader(onClose: () -> Unit) {
    val businessName = LocalSettingsState.current.settings.company.businessName
    Row(
        modifier = Modifier.fillMaxWidth().height(90.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text("TM", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(businessName, color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Sistema POS", color = AppColors.TextSecondary, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(18.dp))
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
}

@Composable
private fun SidebarMenuItem(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    isDanger: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) AppColors.PrimaryLight else Color.Transparent
    val textColor = when {
        isDanger -> AppColors.Danger
        isActive -> AppColors.Primary
        else -> AppColors.TextPrimary
    }
    val iconColor = when {
        isDanger -> AppColors.Danger
        isActive -> AppColors.Primary
        else -> AppColors.IconGray
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(start = if (isActive) 0.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isActive) {
            Box(
                Modifier.width(4.dp).height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppColors.Primary)
            )
            Spacer(Modifier.width(10.dp))
        }
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = textColor, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
    }
}

private data class SidebarItem(val screen: Screen, val icon: ImageVector)

private fun sidebarScreens(): List<SidebarItem> {
    val all = listOf(
        SidebarItem(Screen.Dashboard, Icons.Outlined.GridView),
        SidebarItem(Screen.POS, Icons.Outlined.ShoppingCart),
        SidebarItem(Screen.Comandas, Icons.Outlined.RestaurantMenu),
        SidebarItem(Screen.Mesas, Icons.Outlined.GridView),
        SidebarItem(Screen.Clientes, Icons.Outlined.People),
        SidebarItem(Screen.Reservaciones, Icons.Outlined.Event),
        SidebarItem(Screen.CuentasCobrar, Icons.Outlined.AccountBalanceWallet),
        SidebarItem(Screen.Usuarios, Icons.Outlined.People),
        SidebarItem(Screen.Empleados, Icons.Outlined.People),
        SidebarItem(Screen.Products, Icons.Outlined.Inventory2),
        SidebarItem(Screen.Inventario, Icons.Outlined.Inventory2),
        SidebarItem(Screen.Categories, Icons.Outlined.Category),
        SidebarItem(Screen.Extras, Icons.Outlined.Extension),
        SidebarItem(Screen.Modifiers, Icons.Outlined.Build),
        SidebarItem(Screen.Proveedores, Icons.Outlined.LocalShipping),
        SidebarItem(Screen.Facturas, Icons.Outlined.Receipt),
        SidebarItem(Screen.Reportes, Icons.Outlined.TrendingUp),
        SidebarItem(Screen.Cotizaciones, Icons.Outlined.Description),
        SidebarItem(Screen.Delivery, Icons.Outlined.LocalShipping),
        SidebarItem(Screen.Caja, Icons.Outlined.Calculate),
        SidebarItem(Screen.Turnos, Icons.Outlined.History),
        SidebarItem(Screen.Settings, Icons.Outlined.Settings),
        SidebarItem(Screen.Cloud, Icons.Outlined.Cloud),
        SidebarItem(Screen.Recetas, Icons.Outlined.Description),
        SidebarItem(Screen.OrdenesCompra, Icons.Outlined.ShoppingCart),
    )
    return all.filter { AccessControl.canAccess(it.screen, TurnoManager.currentUser) }
}
