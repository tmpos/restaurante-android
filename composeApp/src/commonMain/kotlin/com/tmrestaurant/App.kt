package com.tmrestaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.cfdShowIdle
import com.tmrestaurant.platform.updateCustomerDisplayOnServer
import com.tmrestaurant.ui.components.AppTopBar
import com.tmrestaurant.ui.components.UserProfileDialog
import com.tmrestaurant.ui.components.Screen
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.data.settings.SettingsState
import com.tmrestaurant.ui.screens.CategoriesScreen
import com.tmrestaurant.ui.screens.SplashScreen
import com.tmrestaurant.ui.screens.TurnosScreen
import com.tmrestaurant.ui.screens.caja.ControlCajaScreen
import com.tmrestaurant.ui.screens.comandas.ComandasScreen
import com.tmrestaurant.ui.screens.clientes.ClientesScreen
import com.tmrestaurant.ui.screens.comandas.ComandasScreen
import com.tmrestaurant.ui.screens.DashboardScreen
import com.tmrestaurant.ui.screens.DeliveryScreen
import com.tmrestaurant.ui.screens.CloudScreen
import com.tmrestaurant.ui.screens.CreditAccountsScreen
import com.tmrestaurant.ui.screens.CotizacionesScreen
import com.tmrestaurant.ui.screens.ExtrasScreen
import com.tmrestaurant.ui.screens.EmpleadosScreen
import com.tmrestaurant.ui.screens.FacturasScreen
import com.tmrestaurant.ui.screens.InventarioScreen
import com.tmrestaurant.ui.screens.ProveedoresScreen
import com.tmrestaurant.ui.screens.UsuariosScreen
import com.tmrestaurant.ui.screens.LoginScreen
import com.tmrestaurant.ui.screens.MesasScreen
import com.tmrestaurant.ui.screens.ModifiersScreen
import com.tmrestaurant.ui.screens.PlaceholderScreen
import com.tmrestaurant.ui.screens.PosScreen
import com.tmrestaurant.ui.screens.ProductsScreen
import com.tmrestaurant.ui.screens.ReportsScreen
import com.tmrestaurant.ui.screens.SettingsScreen
import com.tmrestaurant.ui.screens.reservaciones.ReservacionesScreen
import com.tmrestaurant.ui.screens.recetas.RecetasScreen
import com.tmrestaurant.ui.screens.compras.OrdenesCompraScreen
import com.tmrestaurant.ui.theme.AppColors
import com.tmrestaurant.wifi.WifiMenuServer
import com.tmrestaurant.ui.theme.TMTheme
import com.tmrestaurant.ui.theme.buildColorScheme
import com.tmrestaurant.db.DatabaseManager
import com.tmrestaurant.db.DataMigrator
import com.tmrestaurant.db.platformDatabaseInit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun App(
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {}
) {
    remember {
        DatabaseManager.init(::platformDatabaseInit)
        DataMigrator.migrateAll()
        UsuariosManager.usuarios
        ClientesManager.clientes
        ProveedoresManager.proveedores
    }

    var isLoggedIn by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.POS) }
    var isSidebarOpen by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    var showUserProfile by remember { mutableStateOf(false) }
    val appScope = rememberCoroutineScope()
    val settingsState = remember { SettingsState() }
    val company = settingsState.settings.company
    val server = settingsState.settings.server

    LaunchedEffect(Unit) {
        WifiMenuServer.start()
    }

    LaunchedEffect(
        company.businessName,
        company.phone,
        server.enabled,
        server.serverUrl,
        server.apiKey,
        isLoggedIn
    ) {
        if (isLoggedIn) return@LaunchedEffect

        val businessName = company.businessName.ifBlank { "TMPOS SRL" }
        val phone = company.phone.ifBlank { "829-784-2912" }

        suspend fun showIdleMessage(line1: String, line2: String) {
            val result = if (server.enabled && server.serverUrl.isNotBlank()) {
                updateCustomerDisplayOnServer(server.serverUrl, line1, line2, server.apiKey, server.apiRoute)
            } else {
                null
            }
            if (result?.success != true) {
                cfdShowIdle(line1, line2)
            }
        }

        while (true) {
            showIdleMessage("BIENVENIDO", businessName)
            delay(2500)
            showIdleMessage(businessName, phone)
            delay(2500)
        }
    }

    if (showSplash) {
        CompositionLocalProvider(LocalSettingsState provides settingsState) {
            SplashScreen(onFinished = { showSplash = false })
        }
        return
    }

    if (!isLoggedIn) {
        LoginScreen(
            companyName = company.businessName,
            companyLogo = settingsState.getLogoBytes(),
            onLoginComplete = { isLoggedIn = true; currentScreen = Screen.POS }
        )
        return
    }

    val productState = remember { ProductState() }
    val categoryState = remember { CategoryState() }

    val isDarkMode = settingsState.settings.visual.themeMode == "dark"
    val appColors = buildColorScheme(settingsState.settings.visual.primaryColor, isDarkMode)
    val currentUser = TurnoManager.currentUser
    val hasActiveTurno = TurnoManager.hasActiveTurno()
    val resolvedScreen = if (AccessControl.canAccess(currentScreen, currentUser)) currentScreen else AccessControl.landingScreenFor(currentUser)

    fun navigateTo(screen: Screen) {
        currentScreen = if (AccessControl.canAccess(screen, currentUser)) screen else AccessControl.landingScreenFor(currentUser)
    }

    TMTheme(darkTheme = isDarkMode, appColors = appColors) {
        CompositionLocalProvider(LocalProductState provides productState) {
            CompositionLocalProvider(LocalCategoryState provides categoryState) {
                CompositionLocalProvider(LocalSettingsState provides settingsState) {
                    Column(Modifier.fillMaxSize().background(AppColors.Background)) {
                        AppTopBar(
                            currentScreen = resolvedScreen,
                            onMenuClick = { navigateTo(Screen.Dashboard) },
                            onScreenChange = ::navigateTo,
                            onHomeClick = { navigateTo(Screen.Dashboard) },
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = {
                                    val newMode = if (isDarkMode) "light" else "dark"
                                    settingsState.update { it.copy(visual = it.visual.copy(themeMode = newMode)) }
                                },
                                isFullscreen = isFullscreen,
                                onToggleFullscreen = onToggleFullscreen,
                                onProfileClick = { showUserProfile = true },
                                onLogout = {
                                    isLoggedIn = false
                                    currentScreen = Screen.POS
                                    appScope.launch(Dispatchers.Default) {
                                        BackupManager.createAutomaticBackup("logout")
                                    }
                                },
                                showMenu = AccessControl.canAccess(Screen.Dashboard, currentUser),
                                currentUserName = currentUser?.name ?: "",
                                currentUserRole = currentUser?.role?.displayName ?: "",
                                trailing = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        var serverRunning by remember { mutableStateOf(WifiMenuServer.isRunning) }
                                        var serverIp by remember { mutableStateOf("") }
                                        LaunchedEffect(Unit) {
                                            WifiMenuServer.onStatusChange = { running, ip ->
                                                serverRunning = running
                                                serverIp = if (running) ip else ""
                                            }
                                        }
                                        Box(
                                            Modifier.height(48.dp).clip(RoundedCornerShape(12.dp))
                                                .background(if (serverRunning) Color(0xFFDCFCE7) else AppColors.Background)
                                                .clickable {
                                                    if (serverRunning) WifiMenuServer.stop() else WifiMenuServer.start()
                                                }
                                                .padding(horizontal = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    if (serverRunning) "📶" else "📡",
                                                    fontSize = 16.sp
                                                )
                                                if (serverRunning) {
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(serverIp, color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                        if (hasActiveTurno) {
                                            Box(
                                                Modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFEF3C7))
                                                    .clickable { navigateTo(Screen.Caja) }
                                                    .padding(horizontal = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Outlined.ContentPaste, null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Caja", color = Color(0xFFD97706), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
                                    }
                                }
                            )
                            if (showUserProfile && currentUser != null) {
                                UserProfileDialog(
                                    userId = currentUser.id,
                                    onDismiss = { showUserProfile = false },
                                    onSaved = { updated ->
                                        TurnoManager.updateCurrentUser(
                                            currentUser.copy(
                                                name = updated.name,
                                                clave = ""
                                            )
                                        )
                                        showUserProfile = false
                                    }
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                when (resolvedScreen) {
                                    Screen.Dashboard -> DashboardScreen(
                                        currentScreen = resolvedScreen,
                                        onNavigate = ::navigateTo
                                    )
                                    Screen.POS -> PosScreen(onNavigateToMesas = { navigateTo(Screen.Mesas) })
                                    Screen.Products -> ProductsScreen()
                                    Screen.Categories -> CategoriesScreen()
                    Screen.Caja -> ControlCajaScreen(
                        onNavigateToDashboard = { isLoggedIn = false }
                    )
                    Screen.ControlCaja -> ControlCajaScreen(
                        onNavigateToDashboard = { isLoggedIn = false }
                    )
                    Screen.Mesas -> MesasScreen()
                    Screen.Comandas -> ComandasScreen()
                    Screen.Clientes -> ClientesScreen()
                    Screen.Reservaciones -> ReservacionesScreen()
                    Screen.CuentasCobrar -> CreditAccountsScreen()
                    Screen.Extras -> ExtrasScreen()
                    Screen.Modifiers -> ModifiersScreen()
                    Screen.Proveedores -> ProveedoresScreen()
                    Screen.Usuarios -> UsuariosScreen()
                    Screen.Empleados -> EmpleadosScreen()
                    Screen.Inventario -> InventarioScreen()
                    Screen.Facturas -> FacturasScreen()
                    Screen.Cotizaciones -> CotizacionesScreen()
                    Screen.Delivery -> DeliveryScreen()
                    Screen.Reportes -> ReportsScreen()
                    Screen.Turnos -> TurnosScreen()
                    Screen.Settings -> SettingsScreen()
                    Screen.Cloud -> CloudScreen()
                    Screen.Recetas -> RecetasScreen()
                    Screen.OrdenesCompra -> OrdenesCompraScreen()
                    else -> PlaceholderScreen(title = resolvedScreen.title)
                                }
                            }
                        }
                }
            }
        }
    }
}
