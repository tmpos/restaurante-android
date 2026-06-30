package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.data.WebCheckoutManager
import com.tmrestaurant.platform.playCheckoutNotificationSound
import com.tmrestaurant.ui.theme.AppColors

enum class Screen {
    Dashboard, POS, Comandas, Mesas, Clientes, CuentasCobrar, Products, Categories, Extras, Modifiers, Proveedores, Facturas, Reportes, Cotizaciones, Delivery, Caja, ControlCaja, Turnos, Settings, Usuarios, Empleados, Inventario, Cloud, Reservaciones, Recetas, OrdenesCompra;

    val title: String get() = when (this) {
        Dashboard -> "Dashboard"
        POS -> "Punto de Venta"
        Comandas -> "Comandas"
        Mesas -> "Mesas"
        Clientes -> "Clientes"
        CuentasCobrar -> "Cuentas por cobrar"
        Products -> "Productos"
        Categories -> "Categorías"
        Extras -> "Extras"
        Modifiers -> "Modificadores"
        Proveedores -> "Proveedores"
        Facturas -> "Facturas"
        Reportes -> "Reportes"
        Cotizaciones -> "Cotizaciones"
        Delivery -> "Delivery"
        Caja -> "Caja"
        ControlCaja -> "Control Caja"
        Turnos -> "Turnos"
        Settings -> "Configuración"
        Usuarios -> "Usuarios"
        Empleados -> "Empleados"
        Inventario -> "Inventario"
        Cloud -> "TM Cloud"
        Reservaciones -> "Reservaciones"
        Recetas -> "Recetas"
        OrdenesCompra -> "Órdenes de Compra"
    }
}

@Composable
fun AppTopBar(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    onMenuClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    currentUserName: String = "",
    currentUserRole: String = "",
    showMenu: Boolean = true,
    modifier: Modifier = Modifier,
    trailing: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
) {
    var showNotifications by remember { mutableStateOf(false) }
    var showUserMenu by remember { mutableStateOf(false) }
    val businessName = LocalSettingsState.current.settings.company.businessName
    val checkoutRequests = WebCheckoutManager.requests
    var previousCheckoutCount by remember { mutableStateOf(checkoutRequests.size) }

    LaunchedEffect(checkoutRequests.size) {
        if (checkoutRequests.size > previousCheckoutCount) {
            playCheckoutNotificationSound()
        }
        previousCheckoutCount = checkoutRequests.size
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(AppColors.Surface)
            .border(0.5.dp, AppColors.TopBarBorder)
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showMenu) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Menu, "Menú", tint = AppColors.TextPrimary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.clickable(onClick = onHomeClick)) {
            Text(text = currentScreen.title, color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = businessName, color = AppColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                    .clickable { onScreenChange(Screen.POS) }.padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ShoppingCart, "POS", tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("POS", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            trailing()
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onToggleFullscreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isFullscreen) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
                    "Pantalla completa",
                    tint = AppColors.IconGray,
                    modifier = Modifier.size(26.dp)
                )
            }

            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onToggleDarkMode),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isDarkMode) Icons.Outlined.Brightness4 else Icons.Outlined.DarkMode,
                    "Modo oscuro",
                    tint = if (isDarkMode) AppColors.Orange else AppColors.IconGray,
                    modifier = Modifier.size(26.dp)
                )
            }

            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).clickable { showNotifications = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Notifications, "Notificaciones", tint = AppColors.IconGray, modifier = Modifier.size(26.dp))
                }
                if (checkoutRequests.isNotEmpty()) {
                    Box(
                        modifier = Modifier.size(18.dp).clip(CircleShape).background(AppColors.NotificationDot)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(checkoutRequests.size.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                DropdownMenu(expanded = showNotifications, onDismissRequest = { showNotifications = false }) {
                    if (checkoutRequests.isEmpty()) {
                        DropdownMenuItem(text = { Text("No hay notificaciones", fontSize = 13.sp) }, onClick = { showNotifications = false })
                    } else {
                        checkoutRequests.sortedByDescending { it.createdAt }.forEach { request ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${request.mesaName} lista para cobrar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("${request.itemCount} productos · RD$ ${"%.2f".format(request.total)}", fontSize = 11.sp, color = AppColors.TextSecondary)
                                    }
                                },
                                onClick = {
                                    WebCheckoutManager.select(request.id)
                                    showNotifications = false
                                    onScreenChange(Screen.POS)
                                }
                            )
                        }
                    }
                }
            }

            Box(Modifier.width(1.dp).height(38.dp).background(AppColors.Border))

            Box {
                Row(
                    modifier = Modifier.height(52.dp).clip(RoundedCornerShape(12.dp)).clickable { showUserMenu = true }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Person, "Avatar", tint = AppColors.IconGray, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(if (currentUserName.isNotBlank()) currentUserName else "Usuario", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(currentUserRole.ifBlank { if (currentUserName.isNotBlank()) "Usuario" else "Soporte" }, color = AppColors.TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Outlined.ArrowDropDown, "Desplegar", tint = AppColors.IconGray, modifier = Modifier.size(24.dp))
                }
                DropdownMenu(expanded = showUserMenu, onDismissRequest = { showUserMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Perfil") },
                        onClick = {
                            showUserMenu = false
                            onProfileClick()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Person, null, tint = AppColors.IconGray) }
                    )
                    DropdownMenuItem(
                        text = { Text("Configuración") },
                        onClick = {
                            showUserMenu = false
                            onScreenChange(Screen.Settings)
                        },
                        leadingIcon = { Icon(Icons.Outlined.Settings, null, tint = AppColors.IconGray) }
                    )
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión", color = AppColors.Danger) },
                        onClick = {
                            showUserMenu = false
                            onLogout()
                        },
                        leadingIcon = { Icon(Icons.Outlined.ExitToApp, null, tint = AppColors.Danger) }
                    )
                }
            }
        }
    }
}
