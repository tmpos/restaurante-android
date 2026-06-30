package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.Usuario
import com.tmrestaurant.ui.data.UserRole
import com.tmrestaurant.ui.data.UsuariosManager
import com.tmrestaurant.ui.data.settings.AdminCard
import com.tmrestaurant.ui.data.settings.AppSettings
import com.tmrestaurant.ui.data.settings.BackupItem
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.cloud.AlanubeConfig
import com.tmrestaurant.cloud.AlanubeConfigStore
import com.tmrestaurant.cloud.AlanubeEnvironment
import com.tmrestaurant.cloud.AlanubeResult
import com.tmrestaurant.cloud.AlanubeService
import com.tmrestaurant.cloud.AlanubeStatus
import com.tmrestaurant.ui.data.settings.SettingsSection
import com.tmrestaurant.ui.data.settings.SyncQueueItem
import com.tmrestaurant.ui.data.settings.availableColors
import com.tmrestaurant.platform.ImageFromBytes
import com.tmrestaurant.platform.rememberImagePickerLauncher
import com.tmrestaurant.platform.DiscoveredPrinter
import com.tmrestaurant.platform.PeripheralTestResult
import com.tmrestaurant.platform.eloClearLastScanCode
import com.tmrestaurant.platform.eloDiscoverPrinters
import com.tmrestaurant.platform.eloGetLastScanCode
import com.tmrestaurant.platform.eloOpenCashDrawerNoPrinter
import com.tmrestaurant.platform.eloPrintTestTicket
import com.tmrestaurant.platform.eloPrintTicketCopies
import com.tmrestaurant.platform.TicketPrintStyle
import com.tmrestaurant.platform.uploadTicketLogoToServer
import com.tmrestaurant.platform.eloTestBarcodeScanner
import com.tmrestaurant.platform.eloTestCardReader
import com.tmrestaurant.platform.eloGetLastCardData
import com.tmrestaurant.platform.eloClearLastCardData
import com.tmrestaurant.platform.CardReaderHelper
import com.tmrestaurant.platform.CardReaderState
import com.tmrestaurant.platform.KeyDiagnostic
import com.tmrestaurant.platform.MagstripeParseResult
import com.tmrestaurant.platform.SmtpConfig
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.platform.sendEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import com.tmrestaurant.platform.CfdTestResult
import com.tmrestaurant.platform.cfdDetectBackend
import com.tmrestaurant.platform.cfdIsAvailable
import com.tmrestaurant.platform.cfdShowCustomMessage
import com.tmrestaurant.platform.cfdShowIdle
import com.tmrestaurant.platform.cfdShowProduct
import com.tmrestaurant.platform.cfdShowCart
import com.tmrestaurant.platform.cfdShowTotals
import com.tmrestaurant.platform.cfdShowPayment
import com.tmrestaurant.platform.cfdShowThankYou
import com.tmrestaurant.platform.cfdClear
import com.tmrestaurant.platform.discoverPrinters
import com.tmrestaurant.platform.eloOpenCashDrawer
import com.tmrestaurant.platform.eloPrintTestTicket
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.FiscalSequence
import com.tmrestaurant.ui.data.NcfManager
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.QrGenerator
import com.tmrestaurant.ui.theme.AppColors
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.tmrestaurant.ui.theme.parseHexColor
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen() {
    if (!AccessControl.canManageSettings(TurnoManager.currentUser)) {
        PlaceholderScreen(
            title = "Configuracion",
            subtitle = "Solo los administradores pueden cambiar la configuracion del sistema."
        )
        return
    }
    val state = LocalSettingsState.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Row(Modifier.fillMaxSize()) {
            SettingsSidebar(
                selected = state.selectedSection,
                onSelect = { state.selectedSection = it },
                modifier = Modifier.width(260.dp).fillMaxHeight()
            )
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Configuración", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text("Configuracion general del sistema", fontSize = 14.sp, color = AppColors.TextSecondary)
                    }
                    Box(
                        Modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                            .clickable {
                                state.save()
                                scope.launch { snack.showSnackbar("Configuración guardada correctamente") }
                            }.padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Save, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar Cambios", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (state.selectedSection == SettingsSection.SOPORTE) {
                    Box(Modifier.weight(1f).padding(horizontal = 32.dp)) {
                        SupportDatabaseSection()
                    }
                } else if (state.selectedSection == SettingsSection.TM_CLOUD) {
                    Box(Modifier.weight(1f)) {
                        CloudScreen()
                    }
                } else {
                    Box(Modifier.weight(1f).padding(horizontal = 32.dp).verticalScroll(rememberScrollState())) {
                        when (state.selectedSection) {
                            SettingsSection.EMPRESA -> CompanySection(state)
                            SettingsSection.VISUAL -> VisualSection(state)
                            SettingsSection.VENTAS -> SalesSection(state)
                            SettingsSection.PAGOS -> PaymentMethodsSection(state)
                            SettingsSection.IMPRESION -> PrintSection(state)
                            SettingsSection.NOTIFICACIONES -> NotificationsSection(state, scope, snack)
                            SettingsSection.LICENCIA -> LicenseSection(state)
                            SettingsSection.FISCAL -> FiscalSection(scope, snack)
                            SettingsSection.SERVIDOR -> ServerSection(state)
                            SettingsSection.TM_CLOUD -> Unit
                            SettingsSection.ALANUBE -> AlanubeSection()
                            SettingsSection.SISTEMA -> SystemSection(state)
                            SettingsSection.ELO -> EloSection(state)
                            SettingsSection.SOPORTE -> Unit
                            SettingsSection.TARJETAS_ADMIN -> AdminCardsSection(state)
                        }
                    }
                }
            }
        }
        SnackbarHost(snack, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SettingsSidebar(selected: SettingsSection, onSelect: (SettingsSection) -> Unit, modifier: Modifier = Modifier) {
    val icons = mapOf(
        SettingsSection.EMPRESA to Icons.Outlined.Business,
        SettingsSection.VISUAL to Icons.Outlined.Palette,
        SettingsSection.VENTAS to Icons.Outlined.ShoppingCart,
        SettingsSection.PAGOS to Icons.Outlined.AccountBalanceWallet,
        SettingsSection.IMPRESION to Icons.Outlined.Print,
        SettingsSection.NOTIFICACIONES to Icons.Outlined.Notifications,
        SettingsSection.LICENCIA to Icons.Outlined.Shield,
        SettingsSection.FISCAL to Icons.Outlined.Receipt,
        SettingsSection.SERVIDOR to Icons.Outlined.Cloud,
        SettingsSection.TM_CLOUD to Icons.Outlined.CloudDownload,
        SettingsSection.ALANUBE to Icons.Outlined.Receipt,
        SettingsSection.SISTEMA to Icons.Outlined.Storage,
        SettingsSection.SOPORTE to Icons.Outlined.Dns,
        SettingsSection.ELO to Icons.Outlined.Build,
        SettingsSection.TARJETAS_ADMIN to Icons.Outlined.CreditCard
    )
    Column(
        modifier.background(AppColors.Surface).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SettingsSection.entries.forEach { section ->
            val isActive = section == selected
            Row(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) AppColors.Background else Color.Transparent)
                    .border(if (isActive) 0.5.dp else 0.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onSelect(section) }.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icons[section]!!, null, tint = if (isActive) AppColors.Primary else AppColors.IconGray, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(14.dp))
                Text(section.title, color = if (isActive) AppColors.Primary else AppColors.TextPrimary, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

// ---------- COMPANY SECTION ----------
@Composable
private fun CompanySection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val s = state.settings.company
    val logoBytes = state.getLogoBytes()
    val scope = rememberCoroutineScope()

    val pickImage = rememberImagePickerLauncher { name, bytes ->
        state.cacheLogo(bytes)
        state.update { it.copy(company = it.company.copy(logoPath = name)) }
        val server = state.settings.server
        if (server.enabled && server.serverUrl.isNotBlank()) {
            scope.launch {
                uploadTicketLogoToServer(server.serverUrl, name, bytes, server.apiKey, server.apiRoute)
            }
        }
    }

    SectionCard("Datos de la Empresa", "Informacion basica del negocio", Icons.Outlined.Business) {
        // Logo section
        Column {
            Text("Logo de la Empresa", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
            Text("Este logo aparecera en los tickets de impresion", fontSize = 12.sp, color = AppColors.TextSecondary)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.width(220.dp).height(110.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                if (logoBytes != null) {
                    ImageFromBytes(logoBytes, modifier = Modifier.width(220.dp).height(110.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Description, null, tint = AppColors.Gray, modifier = Modifier.size(32.dp))
                        Text("Sin logo", color = AppColors.TextSecondary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Primary).clickable { pickImage() }.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                    Text("Cambiar logo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.DangerLight).clickable {
                    state.clearLogo()
                    state.update { it.copy(company = it.company.copy(logoPath = null)) }
                }.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                    Text("Eliminar", color = AppColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Text("Formatos: JPG, PNG. Tamaño recomendado: 200x100 px", fontSize = 11.sp, color = AppColors.TextSecondary, modifier = Modifier.padding(top = 6.dp))
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
        Spacer(Modifier.height(16.dp))
        SettingsInput("Nombre del negocio", s.businessName, onValueChange = { v -> state.update { it -> it.copy(company = it.company.copy(businessName = v)) } })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingsInput("RNC", s.rnc, onValueChange = { v -> state.update { it -> it.copy(company = it.company.copy(rnc = v)) } }, modifier = Modifier.weight(1f))
            SettingsInput("Telefono", s.phone, onValueChange = { v -> state.update { it -> it.copy(company = it.company.copy(phone = v)) } }, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingsInput("Email", s.email, onValueChange = { v -> state.update { it -> it.copy(company = it.company.copy(email = v)) } }, modifier = Modifier.weight(1f))
            SettingsInput("Moneda", s.currency, onValueChange = { v -> state.update { it -> it.copy(company = it.company.copy(currency = v)) } }, modifier = Modifier.weight(1f))
        }
        SettingsInput("Direccion", s.address, onValueChange = { v -> state.update { it -> it.copy(company = it.company.copy(address = v)) } })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingsInput("Impuesto (%)", s.taxPercent, onValueChange = { v -> state.update { it -> it.copy(company = it.company.copy(taxPercent = v)) } })
            SettingsInput("Propina sugerida (%)", s.suggestedTipPercent, onValueChange = { v -> state.update { it -> it.copy(company = it.company.copy(suggestedTipPercent = v)) } })
        }
    }
}

@Composable
private fun VisualSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val s = state.settings.visual
    SectionCard("Configuracion Visual", "Personaliza la apariencia del sistema", Icons.Outlined.Palette) {
        Text("Tema de la aplicacion", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ThemeCard("Modo Claro", Icons.Outlined.Settings, s.themeMode == "light") { state.update { it.copy(visual = it.visual.copy(themeMode = "light")) } }
            ThemeCard("Modo Oscuro", Icons.Outlined.Palette, s.themeMode == "dark") { state.update { it.copy(visual = it.visual.copy(themeMode = "dark")) } }
        }
        Spacer(Modifier.height(20.dp))
        Text("Color principal", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            availableColors.forEach { (hex, _) ->
                val isSelected = s.primaryColor == hex
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(parseHexColor(hex))
                    .border(if (isSelected) 2.dp else 0.dp, if (isSelected) AppColors.Primary else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { state.update { it.copy(visual = it.visual.copy(primaryColor = hex)) } },
                    contentAlignment = Alignment.Center) {
                    if (isSelected) Text("✓", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------- SALES SECTION ----------
@Composable
private fun SalesSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val s = state.settings.sales
    SectionCard("Configuracion de Ventas", "Opciones para el proceso de venta", Icons.Outlined.ShoppingCart) {
        Text("Configuracion de Impuestos (ITBIS)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OptionCard("Agregar al precio", "El impuesto se suma al precio del producto\nEj: Producto RD\$100 + ITBIS 18% = RD\$118", s.taxMode == "added", Modifier.weight(1f)) { state.update { it.copy(sales = it.sales.copy(taxMode = "added")) } }
            OptionCard("Incluido en el precio", "El precio ya incluye el impuesto\nEj: Producto RD\$118 (incluye ITBIS RD\$18)", s.taxMode == "included", Modifier.weight(1f)) { state.update { it.copy(sales = it.sales.copy(taxMode = "included")) } }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Porcentaje de impuesto", fontSize = 14.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
            SettingsInput("", s.taxPercent, { state.update { c -> c.copy(sales = c.sales.copy(taxPercent = it)) } }, Modifier.width(80.dp))
            Text(" %", fontSize = 14.sp, color = AppColors.TextPrimary, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
        Spacer(Modifier.height(12.dp))
        SettingsSwitch("Permitir descuentos", "Los usuarios podran aplicar descuentos a las ordenes", s.allowDiscounts) { state.update { c -> c.copy(sales = c.sales.copy(allowDiscounts = it)) } }
        SettingsSwitch("Requerir cliente", "Obligar a seleccionar un cliente para cada venta", s.requireCustomer) { state.update { c -> c.copy(sales = c.sales.copy(requireCustomer = it)) } }
        SettingsSwitch("Permitir ventas sin stock", "Permitir vender productos aunque no haya stock disponible", s.allowOutOfStockSales) { state.update { c -> c.copy(sales = c.sales.copy(allowOutOfStockSales = it)) } }
        SettingsSwitch("Enviar automaticamente a cocina", "Las ordenes se enviaran automaticamente a cocina al guardar", s.autoSendToKitchen) { state.update { c -> c.copy(sales = c.sales.copy(autoSendToKitchen = it)) } }
    }
}

// ---------- PAYMENT METHODS SECTION ----------
@Composable
private fun PaymentMethodsSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val methods = state.settings.paymentMethods.methods
    SectionCard("Metodos de Pago", "Configura los metodos de pago disponibles", Icons.Outlined.AccountBalanceWallet) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Metodos de Pago", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Text("Agrega, edita o deshabilita metodos de pago", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Box(
                        Modifier.height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Primary)
                            .clickable {
                                val newMethod = com.tmrestaurant.ui.data.settings.PaymentMethod(
                                    name = "NUEVO", percentage = "0", enabled = true
                                )
                                state.update { c ->
                                    c.copy(paymentMethods = c.paymentMethods.copy(
                                        methods = c.paymentMethods.methods + newMethod
                                    ))
                                }
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        methods.forEachIndexed { index, method ->
            val bgColor = if (method.enabled) AppColors.Surface else Color(0xFFF9FAFB)
            val borderColor = if (method.enabled) AppColors.Border else Color(0xFFE5E7EB)
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp)).padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(
                                if (method.enabled) Color(0xFFDCFCE7) else Color(0xFFF3F4F6)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp,
                                color = if (method.enabled) Color(0xFF16A34A) else AppColors.TextSecondary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(method.name.uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                            Text(if (method.enabled) "Activo" else "Inactivo", fontSize = 11.sp, color = if (method.enabled) Color(0xFF16A34A) else AppColors.TextSecondary)
                        }
                        Switch(
                            checked = method.enabled,
                            onCheckedChange = { enabled ->
                                state.update { c ->
                                    val updated = c.paymentMethods.methods.toMutableList()
                                    updated[index] = method.copy(enabled = enabled)
                                    c.copy(paymentMethods = c.paymentMethods.copy(methods = updated))
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Success)
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Nombre", fontSize = 11.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                                BasicTextField(value = method.name, onValueChange = { v ->
                                    state.update { c ->
                                        val updated = c.paymentMethods.methods.toMutableList()
                                        updated[index] = method.copy(name = v.uppercase())
                                        c.copy(paymentMethods = c.paymentMethods.copy(methods = updated))
                                    }
                                }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold), modifier = Modifier.fillMaxSize(), singleLine = true)
                            }
                        }
                        Column(Modifier.width(100.dp)) {
                            Text("Porcentaje", fontSize = 11.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                                    BasicTextField(value = method.percentage, onValueChange = { v ->
                                        if (v.all { it.isDigit() || it == '.' }) {
                                            state.update { c ->
                                                val updated = c.paymentMethods.methods.toMutableList()
                                                updated[index] = method.copy(percentage = v)
                                                c.copy(paymentMethods = c.paymentMethods.copy(methods = updated))
                                            }
                                        }
                                    }, textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
                                }
                                Spacer(Modifier.width(4.dp))
                                Text("%", fontSize = 13.sp, color = AppColors.TextSecondary)
                            }
                        }
                        Box(
                            Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2))
                                .clickable {
                                    state.update { c ->
                                        val updated = c.paymentMethods.methods.toMutableList()
                                        updated.removeAt(index)
                                        c.copy(paymentMethods = c.paymentMethods.copy(methods = updated))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (methods.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay metodos de pago configurados. Presiona \"Agregar\" para crear uno.", fontSize = 13.sp, color = AppColors.TextSecondary)
            }
        }
    }
}

// ---------- PRINT SECTION ----------
@Composable
private fun PrintSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val s = state.settings.print
    val company = state.settings.company
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    var searchType by remember { mutableStateOf(s.searchType) }
    var showPrinterList by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var discoveredPrinters by remember { mutableStateOf(emptyList<DiscoveredPrinter>()) }
    var isPrinting by remember { mutableStateOf(false) }
    var isOpeningDrawer by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var showBluetoothModal by remember { mutableStateOf(false) }
    var btDevices by remember { mutableStateOf(emptyList<DiscoveredPrinter>()) }
    var isBtSearching by remember { mutableStateOf(false) }
    var btError by remember { mutableStateOf<String?>(null) }
    var showTicketPreview by remember { mutableStateOf(false) }

    Box {
        Column {
            SectionCard("Configuracion de Impresion", "Personaliza el ticket de 80mm", Icons.Outlined.Print) {
                // Printer selection
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F5FF)).border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Column {
                        Text("Seleccionar Impresora", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Text("Elige la impresora donde se imprimiran los tickets", fontSize = 12.sp, color = AppColors.TextSecondary)
                        Spacer(Modifier.height(12.dp))

                        // Current printer badge
                        if (s.selectedPrinter.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Impresora actual:", fontSize = 13.sp, color = AppColors.TextSecondary)
                                Spacer(Modifier.width(8.dp))
                                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(AppColors.PrimaryLight).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Text(s.selectedPrinter, color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // Botones de busqueda: USB y Bluetooth separados
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Buscar USB
                            Box(
                                Modifier.height(36.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isSearching && searchType == "USB") AppColors.Gray else AppColors.Primary)
                                    .clickable {
                                        if (!isSearching) {
                                            searchType = "USB"
                                            state.update { c -> c.copy(print = c.print.copy(searchType = "USB")) }
                                            isSearching = true
                                            searchError = null
                                            scope.launch {
                                                try {
                                                    val result = discoverPrinters("USB").toMutableList()
                                                    // Also add ELO built-in printer option
                                                    if (result.none { it.identifier == "elo_builtin" }) {
                                                        result.add(0, DiscoveredPrinter("Impresora Interna ELO", "elo_builtin", "USB"))
                                                    }
                                                    discoveredPrinters = result
                                                    showPrinterList = true
                                                } catch (e: Exception) {
                                                    searchError = "Error al buscar USB: ${e.message}"
                                                    discoveredPrinters = listOf(DiscoveredPrinter("Impresora Interna ELO", "elo_builtin", "USB"))
                                                    showPrinterList = true
                                                } finally {
                                                    isSearching = false
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Print, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (isSearching && searchType == "USB") "Buscando..." else "Buscar USB",
                                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            // Buscar Bluetooth (abre modal)
                            Box(
                                Modifier.height(36.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF3B82F6))
                                    .clickable {
                                        showBluetoothModal = true
                                        btDevices = emptyList()
                                        btError = null
                                        isBtSearching = true
                                        scope.launch {
                                            try {
                                                val result = discoverPrinters("Bluetooth")
                                                btDevices = result
                                                if (result.isEmpty()) {
                                                    btError = "No se encontraron dispositivos Bluetooth emparejados. Empareje la impresora desde Ajustes del sistema."
                                                }
                                            } catch (e: Exception) {
                                                btError = "Error Bluetooth: ${e.message}"
                                            } finally {
                                                isBtSearching = false
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Wifi, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Buscar Bluetooth", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Search error message
                if (searchError != null) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp)).padding(12.dp)
                    ) {
                        Text(searchError!!, fontSize = 13.sp, color = Color(0xFFDC2626))
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Discovered printers list - only visible after searching
                if (showPrinterList && discoveredPrinters.isNotEmpty()) {
                    Text("Impresoras encontradas (${discoveredPrinters.size}):", fontSize = 13.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    discoveredPrinters.forEach { printer ->
                        val isSelected = s.selectedPrinter == printer.name || s.selectedPrinter == printer.identifier
                        // Para USB guardamos identifier (vendorId:productId), para BT guardamos name
                        val selectValue = if (printer.type == "Bluetooth") printer.name else printer.identifier
                        Row(
                            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Surface)
                                .border(0.5.dp, if (isSelected) AppColors.Primary else AppColors.Border, RoundedCornerShape(10.dp))
                                .clickable { state.update { c -> c.copy(print = c.print.copy(selectedPrinter = selectValue)) } }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).border(2.dp, if (isSelected) AppColors.Primary else AppColors.Border, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                if (isSelected) Box(Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Primary))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(printer.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                if (printer.identifier != printer.name) {
                                    Text(printer.identifier, fontSize = 11.sp, color = AppColors.TextSecondary)
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(
                                if (printer.type == "Bluetooth") Color(0xFFDBEAFE) else Color(0xFFDCFCE7)
                            ).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text(printer.type, fontSize = 10.sp, color = if (printer.type == "Bluetooth") Color(0xFF3B82F6) else Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                } else if (!showPrinterList) {
                    Box(
                        Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                            .border(1.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Presiona \"Buscar impresoras\" para descubrir dispositivos", fontSize = 13.sp, color = AppColors.TextSecondary)
                    }
                }

                // Print test + Open drawer buttons
                if (s.selectedPrinter.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.height(40.dp).clip(RoundedCornerShape(10.dp))
                                .background(if (isPrinting) AppColors.Gray else AppColors.Success)
                                .clickable {
                                    if (!isPrinting) {
                                        isPrinting = true
                                        scope.launch {
                                            try {
                                                val ticketContent = buildTestTicket(company, s)
                                                val result = eloPrintTicketCopies(
                                                    s.selectedPrinter,
                                                    ticketContent,
                                                    s.paperWidthMm,
                                                    s.copies,
                                                    TicketPrintStyle(
                                                        textSize = s.textSize,
                                                        logoWidthMm = s.logoWidthMm,
                                                        logoHeightMm = s.logoHeightMm,
                                                        showLogo = s.showCompanyLogo,
                                                        logoBytes = state.getLogoBytes()
                                                    )
                                                )
                                                if (result.success) {
                                                    snack.showSnackbar("Ticket de prueba enviado correctamente a ${s.selectedPrinter}")
                                                } else {
                                                    snack.showSnackbar("Error: No se pudo imprimir en ${s.selectedPrinter}: ${result.message}")
                                                }
                                            } catch (e: Exception) {
                                                snack.showSnackbar("Error de impresion: ${e.message}")
                                            } finally {
                                                isPrinting = false
                                            }
                                        }
                                    }
                                }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Print, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isPrinting) "Imprimiendo..." else "Imprimir prueba",
                                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Box(
                            Modifier.height(40.dp).clip(RoundedCornerShape(10.dp))
                                .background(if (isOpeningDrawer) AppColors.Gray else Color(0xFFD97706))
                                .clickable {
                                    if (!isOpeningDrawer) {
                                        isOpeningDrawer = true
                                        scope.launch {
                                            try {
                                                val result = eloOpenCashDrawer(s.selectedPrinter)
                                                if (result.success) {
                                                    snack.showSnackbar("Gaveta abierta correctamente via ${s.selectedPrinter}")
                                                } else {
                                                    snack.showSnackbar("Error: No se pudo abrir la gaveta via ${s.selectedPrinter}: ${result.message}")
                                                }
                                            } catch (e: Exception) {
                                                snack.showSnackbar("Error al abrir gaveta: ${e.message}")
                                            } finally {
                                                isOpeningDrawer = false
                                            }
                                        }
                                    }
                                }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Lock, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isOpeningDrawer) "Abriendo..." else "Abrir gaveta",
                                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    InfoCard("Cantidad de Copias", "Copias que se imprimiran", Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.PrimaryLight).clickable {
                                state.update { c -> c.copy(print = c.print.copy(copies = c.print.copies + 1)) }
                            }.padding(4.dp), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Add, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp)) }
                            Text(" ${s.copies} ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.DangerLight).clickable {
                                if (s.copies > 1) state.update { c -> c.copy(print = c.print.copy(copies = c.print.copies - 1)) }
                            }.padding(4.dp), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(18.dp)) }
                        }
                    }
                    InfoCard("Tamaño del Texto", "Ajusta la legibilidad del ticket", Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Pequeño" to "small", "Normal" to "normal", "Grande" to "large").forEach { (label, value) ->
                                Box(Modifier.height(34.dp).clip(RoundedCornerShape(8.dp)).background(if (s.textSize == value) AppColors.PrimaryLight else AppColors.Background).border(1.dp, if (s.textSize == value) AppColors.Primary else AppColors.Border, RoundedCornerShape(8.dp)).clickable {
                                    state.update { c -> c.copy(print = c.print.copy(textSize = value)) }
                                }.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                    Text(label, fontSize = 12.sp, color = if (s.textSize == value) AppColors.Primary else AppColors.TextPrimary, fontWeight = if (s.textSize == value) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFFBEB)).border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Column {
                        Text("Márgenes del Ticket (mm)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Text("Ajusta los márgenes si el texto se corta en la impresión", fontSize = 12.sp, color = AppColors.TextSecondary)
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MarginInput("Izquierdo", s.marginLeft) { state.update { c -> c.copy(print = c.print.copy(marginLeft = it)) } }
                            MarginInput("Derecho", s.marginRight) { state.update { c -> c.copy(print = c.print.copy(marginRight = it)) } }
                            MarginInput("Superior", s.marginTop) { state.update { c -> c.copy(print = c.print.copy(marginTop = it)) } }
                            MarginInput("Inferior", s.marginBottom) { state.update { c -> c.copy(print = c.print.copy(marginBottom = it)) } }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ancho útil del ticket (mm)", fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                            SettingsInput("", s.usefulWidthMm, { state.update { c -> c.copy(print = c.print.copy(usefulWidthMm = it)) } }, Modifier.width(80.dp))
                            Text(" mm", fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.padding(start = 4.dp))
                        }
                        Text("Papel 80mm = ancho útil ~72mm", fontSize = 11.sp, color = AppColors.TextSecondary)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0FDF4)).border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Column {
                        Text("Tamaño del Logo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("Ancho (mm):", fontSize = 13.sp, color = AppColors.TextPrimary); Spacer(Modifier.width(8.dp)); SettingsInput("", s.logoWidthMm, { state.update { c -> c.copy(print = c.print.copy(logoWidthMm = it)) } }, Modifier.width(80.dp)) }
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("Alto (mm):", fontSize = 13.sp, color = AppColors.TextPrimary); Spacer(Modifier.width(8.dp)); SettingsInput("", s.logoHeightMm, { state.update { c -> c.copy(print = c.print.copy(logoHeightMm = it)) } }, Modifier.width(80.dp)) }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFFBEB)).border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Column {
                        Text("Ancho del Papel", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("57" to "57 mm", "80" to "80 mm").forEach { (value, label) ->
                                Box(Modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(if (s.paperWidthMm == value) AppColors.PrimaryLight else AppColors.Background)
                                    .border(1.5.dp, if (s.paperWidthMm == value) AppColors.Primary else AppColors.Border, RoundedCornerShape(10.dp))
                                    .clickable { state.update { c -> c.copy(print = c.print.copy(paperWidthMm = value)) } }
                                    .padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (s.paperWidthMm == value) AppColors.Primary else AppColors.TextPrimary)
                                }
                            }
                            Box(Modifier.width(60.dp).height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                                BasicTextField(value = s.paperWidthMm, onValueChange = { v -> if (v.all { it.isDigit() }) state.update { c -> c.copy(print = c.print.copy(paperWidthMm = v)) } },
                                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp), modifier = Modifier.fillMaxWidth(), singleLine = true)
                            }
                            Text("mm", fontSize = 14.sp, color = AppColors.TextSecondary, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F3FF)).border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Column {
                        Text("Cantidad de copias", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Text("Numero de tickets que se imprimiran por cada venta", fontSize = 11.sp, color = AppColors.TextSecondary)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                                    .clickable { state.update { c -> c.copy(print = c.print.copy(copies = (c.print.copies - 1).coerceAtLeast(1))) } },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Remove, "Menos copias", tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                            }
                            Box(Modifier.width(70.dp).height(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Text("${s.copies}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            }
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight)
                                    .clickable { state.update { c -> c.copy(print = c.print.copy(copies = (c.print.copies + 1).coerceAtMost(5))) } },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Add, "Mas copias", tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                PrintSwitches(state)

                // ========== TICKET PREVIEW ==========
                Spacer(Modifier.height(24.dp))
                Text("Vista Previa del Ticket", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                Text("Asi se vera el ticket impreso segun la configuracion actual", fontSize = 12.sp, color = AppColors.TextSecondary)
                Spacer(Modifier.height(12.dp))
                TicketPreview(company, s, state.getLogoBytes())
            }
        }
        SnackbarHost(snack, modifier = Modifier.align(Alignment.BottomCenter))

        // ===== BLUETOOTH SEARCH MODAL =====
        if (showBluetoothModal) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))
                    .clickable { if (!isBtSearching) showBluetoothModal = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.fillMaxWidth(0.6f).clip(RoundedCornerShape(16.dp))
                        .background(AppColors.Surface)
                        .clickable { /* consume click para no cerrar */ }
                        .padding(24.dp)
                ) {
                    Column {
                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Wifi, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Buscar Impresoras Bluetooth", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Mostrando dispositivos Bluetooth emparejados. Si no ve su impresora, emparejela desde los Ajustes del sistema.",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )
                        Spacer(Modifier.height(16.dp))

                        // Estado de busqueda
                        if (isBtSearching) {
                            Box(
                                Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Buscando dispositivos Bluetooth...", fontSize = 14.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Espere un momento", fontSize = 12.sp, color = AppColors.TextSecondary)
                                }
                            }
                        }

                        // Error
                        if (btError != null) {
                            Box(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF2F2)).border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Text(btError!!, fontSize = 13.sp, color = Color(0xFFDC2626))
                            }
                        }

                        // Lista de dispositivos Bluetooth
                        if (btDevices.isNotEmpty()) {
                            Text("Dispositivos encontrados (${btDevices.size}):", fontSize = 13.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            btDevices.forEach { device ->
                                Row(
                                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(10.dp))
                                        .background(if (s.selectedPrinter == device.identifier || s.selectedPrinter == device.name) AppColors.InfoLight else AppColors.Surface)
                                        .border(
                                            1.dp,
                                            if (s.selectedPrinter == device.identifier || s.selectedPrinter == device.name) Color(0xFF3B82F6) else AppColors.Border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            state.update { c -> c.copy(print = c.print.copy(selectedPrinter = device.name, searchType = "Bluetooth")) }
                                            searchType = "Bluetooth"
                                            // Agregar a la lista principal tambien
                                            if (discoveredPrinters.none { it.identifier == device.identifier }) {
                                                discoveredPrinters = discoveredPrinters + device
                                            }
                                            showPrinterList = true
                                            showBluetoothModal = false
                                        }
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Radio button visual
                                    Box(
                                        Modifier.size(20.dp).clip(RoundedCornerShape(10.dp))
                                            .border(2.dp, if (s.selectedPrinter == device.identifier || s.selectedPrinter == device.name) Color(0xFF3B82F6) else AppColors.Border, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (s.selectedPrinter == device.identifier || s.selectedPrinter == device.name) {
                                            Box(Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF3B82F6)))
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(device.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                        Text(device.identifier, fontSize = 11.sp, color = AppColors.TextSecondary)
                                    }
                                    Box(
                                        Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFDBEAFE))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Bluetooth", fontSize = 10.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Botones del modal
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Buscar de nuevo
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isBtSearching) AppColors.Gray else Color(0xFF3B82F6))
                                    .clickable {
                                        if (!isBtSearching) {
                                            isBtSearching = true
                                            btError = null
                                            btDevices = emptyList()
                                            scope.launch {
                                                try {
                                                    val result = discoverPrinters("Bluetooth")
                                                    btDevices = result
                                                    if (result.isEmpty()) {
                                                        btError = "No se encontraron dispositivos Bluetooth emparejados"
                                                    }
                                                } catch (e: Exception) {
                                                    btError = "Error: ${e.message}"
                                                } finally {
                                                    isBtSearching = false
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (isBtSearching) "Buscando..." else "Buscar de nuevo",
                                    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            // Cerrar
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.Background)
                                    .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp))
                                    .clickable { showBluetoothModal = false }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Cerrar", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        AuditLogSection()
    }
}

@Composable
private fun AuditLogSection() {
    var auditItems by remember { mutableStateOf(com.tmrestaurant.ui.data.AuditLogManager.recent(40)) }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Bitacora de auditoria", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Eventos sensibles recientes del sistema", fontSize = 12.sp, color = AppColors.TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionBtn("Actualizar") { auditItems = com.tmrestaurant.ui.data.AuditLogManager.recent(40) }
                    ActionBtn("Limpiar") {
                        com.tmrestaurant.ui.data.AuditLogManager.clear()
                        auditItems = emptyList()
                    }
                }
            }
            if (auditItems.isEmpty()) {
                Text("No hay eventos registrados", fontSize = 12.sp, color = AppColors.TextSecondary)
            } else {
                auditItems.forEach { event ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(event.module, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.TextPrimary)
                                Box(
                                    Modifier.clip(RoundedCornerShape(4.dp))
                                        .background(if (event.level == "WARN") Color(0xFFFEF3C7) else Color(0xFFDBEAFE))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        event.level,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (event.level == "WARN") Color(0xFFD97706) else Color(0xFF2563EB)
                                    )
                                }
                            }
                            Text("${event.action} - ${event.detail}", fontSize = 12.sp, color = AppColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${event.actorName} (${event.actorRole}) - ${formatDateTime(event.createdAt)}", fontSize = 10.sp, color = AppColors.Gray)
                        }
                    }
                }
            }
        }
    }
}

private fun buildTestTicket(
    company: com.tmrestaurant.ui.data.settings.CompanySettings,
    print: com.tmrestaurant.ui.data.settings.PrintSettings
): String {
    val width = if (print.paperWidthMm.trim() == "80") 48 else 32

    fun line(char: Char = '-') = char.toString().repeat(width)
    fun fit(value: String, size: Int): String =
        if (value.length <= size) value.padEnd(size) else value.take((size - 1).coerceAtLeast(0)) + "."
    fun center(value: String): String {
        val text = value.take(width)
        val left = ((width - text.length) / 2).coerceAtLeast(0)
        return " ".repeat(left) + text
    }
    fun keyValue(label: String, value: String): String {
        val cleanValue = value.take(width)
        val spaces = (width - label.length - cleanValue.length).coerceAtLeast(1)
        return label + " ".repeat(spaces) + cleanValue
    }
    fun fiscalRow(left: String, right: String): String {
        val rightText = right.take(width / 2)
        val leftWidth = (width - rightText.length).coerceAtLeast(1)
        return fit(left, leftWidth) + rightText
    }
    fun moneyRow(label: String, tax: String, amount: String): String {
        val amountWidth = 12
        val taxWidth = 10
        val labelWidth = width - taxWidth - amountWidth
        return fit(label, labelWidth) + tax.padStart(taxWidth) + amount.padStart(amountWidth)
    }
    fun barcode(value: String): List<String> {
        val bars = "|||| || ||| | |||| || | ||| |||| | || |||"
        return listOf(center(bars.take(width)), center(value.take(width)))
    }
    fun qrBlock(text: String): List<String> {
        val matrix = QrGenerator.generate(text)
        val fullPattern = QrGenerator.toAscii(matrix)
        return fullPattern.lines().map { line -> center(line) }
    }

    val receiptNo = "00000SF-10000259323"
    val businessName = company.businessName.ifBlank { "TM-RESTAURANTE" }
    val address = company.address.ifBlank { "Direccion no configurada" }
    val phone = company.phone.ifBlank { "809-555-1234" }
    val rnc = company.rnc.ifBlank { "000000000" }

    return buildString {
        appendLine()
        if (print.showCompanyLogo) {
            appendLine(center("TM"))
            appendLine(center("RESTAURANTE"))
        }
        if (print.showCompanyName) appendLine(center(businessName))
        if (print.showCompanyAddress) appendLine(center(address.uppercase()))
        if (print.showCompanyPhone) appendLine(center("Telefono: $phone"))
        if (print.showCompanyRnc) appendLine(center("RNC/Cedula: $rnc"))
        appendLine(line())
        appendLine(keyValue("No. de Recibo:", receiptNo))
        appendLine(keyValue("Orden No.:", "42"))
        if (print.showDateTime) appendLine(fiscalRow("Fecha: 15/5/2026 10:53:34 a. m.", "Trans:258820"))
        appendLine(keyValue("NCF:", "E3200000904226"))
        appendLine(keyValue("NCF Valido Hasta:", "31/12/2027 12:00:00 a. m."))
        appendLine(line())
        appendLine(center("FACTURA PARA CONSUMIDOR FINAL"))
        appendLine(line())
        appendLine(fit("Descripcion", width - 22) + "ITBIS".padStart(10) + "VALOR".padStart(12))
        appendLine(line())
        appendLine("1 X 209.99")
        appendLine("7509546063706")
        appendLine(moneyRow("18%-ITBIS", "RD$32.03", "RD$209.99"))
        appendLine("SPEED STICK DESO.SPR")
        appendLine(line())
        appendLine(moneyRow("SUBTOTAL", "RD$32.03", "RD$177.96"))
        if (print.showTaxes) appendLine(moneyRow("TOTAL", "RD$32.03", "RD$209.99"))
        if (print.showPaymentMethod) appendLine(moneyRow("EFECTIVO", "", "RD$2,000.00"))
        if (print.showCashChange) appendLine(moneyRow("CAMBIO", "", "RD$1,790.01"))
        appendLine(line())
        appendLine("Cantidad Articulos: 1")
        appendLine("Caja No.: SF-10")
        appendLine("Le Atendio: ADMINISTRADOR")
        if (print.showCashierName) appendLine("Cajero: 0503")
        appendLine()
        appendLine(line())
        appendLine(center("TOTAL ITBIS PAGADO"))
        appendLine(center("TOTAL 18%-ITBIS PAGADO: RD$32.03"))
        appendLine(line())
        appendLine(center("**VALIDO PARA DEVOLUCION POR 7 DIAS.**"))
        appendLine(center("**DEBE PRESENTAR FACTURA ORIGINAL.**"))
        appendLine(center("**NO APLICAN ARTICULOS REFRIGERADOS,**"))
        appendLine(center("COSMETICOS, NI ROPA INTIMA.**"))
        appendLine(center("!!NO REEMBOLSAMOS DINERO EN EFECTIVO!!"))
        appendLine()
        if (print.showThankYouMessage) appendLine(center(print.thankYouMessage.ifBlank { "!!GRACIAS POR PREFERIRNOS!!" }.uppercase()))
        appendLine()
        barcode(receiptNo).forEach { appendLine(it) }
        appendLine()
        if (print.showQr) {
            val dgiiUrl = QrGenerator.dgiiUrl(rnc, "E3200000904226", 209.99)
            qrBlock(dgiiUrl).forEach { appendLine(it) }
        }
        appendLine(center("15-05-2026 10:53:48"))
        appendLine()
        appendLine()
    }
}

@Composable
private fun TicketPreview(
    company: com.tmrestaurant.ui.data.settings.CompanySettings,
    print: com.tmrestaurant.ui.data.settings.PrintSettings,
    logoBytes: ByteArray?
) {
    val sep = "================================\n"
    val ticketWidth = if (print.paperWidthMm == "57") 210.dp else 300.dp
    val bodySize = when (print.textSize) {
        "small" -> 8.sp
        "large" -> 12.sp
        else -> 10.sp
    }
    val smallSize = when (print.textSize) {
        "small" -> 7.sp
        "large" -> 10.sp
        else -> 9.sp
    }
    val titleSize = when (print.textSize) {
        "small" -> 10.sp
        "large" -> 15.sp
        else -> 13.sp
    }
    val logoWidth = ((print.logoWidthMm.toFloatOrNull() ?: 51f) * 3f).dp
    val logoHeight = ((print.logoHeightMm.toFloatOrNull() ?: 20f) * 3f).dp
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(Modifier.width(ticketWidth).clip(RoundedCornerShape(4.dp)).background(Color.White)
            .border(1.dp, AppColors.Border, RoundedCornerShape(4.dp)).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (print.showCompanyLogo) {
                Box(
                    Modifier.width(logoWidth.coerceAtMost(ticketWidth - 28.dp))
                        .height(logoHeight.coerceAtLeast(15.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoBytes?.isNotEmpty() == true) {
                        ImageFromBytes(logoBytes, Modifier.fillMaxSize(), cacheKey = "ticket-preview-logo")
                    } else {
                        Text("[LOGO]", fontSize = smallSize, color = AppColors.Gray)
                    }
                }
            }
            if (print.showCompanyName) Text(company.businessName, fontWeight = FontWeight.Bold, fontSize = titleSize, color = Color.Black, textAlign = TextAlign.Center)
            if (print.showCompanyRnc) Text("RNC: ${company.rnc}", fontSize = bodySize, color = Color.Black, textAlign = TextAlign.Center)
            if (print.showCompanyAddress) Text(company.address, fontSize = smallSize, color = Color.DarkGray, textAlign = TextAlign.Center)
            if (print.showCompanyPhone) Text("Tel: ${company.phone}", fontSize = smallSize, color = Color.DarkGray, textAlign = TextAlign.Center)
            if (print.showCompanyEmail) Text(company.email, fontSize = smallSize, color = Color.DarkGray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            if (print.showReceiptNumber || print.showNcf || print.showNcfExpiry || print.showInvoiceTitle || print.showDateTime || print.showCashierName) {
                Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            }
            if (print.showInvoiceTitle) Text("FACTURA DE CONSUMO", fontWeight = FontWeight.Bold, fontSize = titleSize, color = Color.Black)
            if (print.showReceiptNumber) Text("No. Recibo: FAC-10000523", fontSize = bodySize, color = Color.Black)
            if (print.showNcf) Text("NCF: E3200000039", fontSize = bodySize, color = Color.Black)
            if (print.showNcfExpiry) Text("NCF Valido hasta: 31/12/2027", fontSize = smallSize, color = Color.DarkGray)
            if (print.showDateTime || print.showCashierName || print.showInvoiceTitle || print.showReceiptNumber || print.showNcf || print.showNcfExpiry) {
                Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            }
            if (print.showDateTime) Text("Fecha: 15/05/2026 10:30 AM", fontSize = bodySize, color = Color.Black, modifier = Modifier.fillMaxWidth())
            if (print.showCashierName) Text("Cajero: Administrador", fontSize = bodySize, color = Color.Black, modifier = Modifier.fillMaxWidth())
            if (print.showDateTime || print.showCashierName) {
                Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            }
            if (print.showItems) {
                listOf("1x OFETA DE APERTURA" to "RD\$ 290.00", "1x COMBO DE 5 PIEZAS" to "RD\$ 575.00", "2x COCA COLA 2 LITRO" to "RD\$ 400.00").forEach { (name, price) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(name, fontSize = bodySize, color = Color.Black, modifier = Modifier.weight(1f))
                        Text(price, fontSize = bodySize, color = Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    }
                }
                Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            }
            if (print.showSubtotal) Row(Modifier.fillMaxWidth()) { Text("Subtotal:", fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(1f)); Text("RD\$ 1,072.88", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold) }
            if (print.showTaxes) Row(Modifier.fillMaxWidth()) { Text("ITBIS 18%:", fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(1f)); Text("RD\$ 192.12", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold) }
            if (print.showDiscounts) Row(Modifier.fillMaxWidth()) { Text("Descuento:", fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(1f)); Text("RD\$ 0.00", fontSize = 9.sp, color = Color.Black) }
            if (print.showSubtotal || print.showTaxes || print.showDiscounts) {
                Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            }
            if (print.showTotal) Row(Modifier.fillMaxWidth()) { Text("TOTAL:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f)); Text("RD\$ 1,265.00", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
            if (print.showTotal) Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            if (print.showPaymentMethod) Text("Metodo: Efectivo", fontSize = 9.sp, color = Color.Black, modifier = Modifier.fillMaxWidth())
            if (print.showCashChange) { Text("Recibido: RD\$ 1,500.00", fontSize = 9.sp, color = Color.Black, modifier = Modifier.fillMaxWidth()); Text("Cambio: RD\$ 235.00", fontSize = 9.sp, color = Color.Black, modifier = Modifier.fillMaxWidth()) }
            if (print.showPaymentMethod || print.showCashChange) Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            if (print.showItemCount) Text("Cantidad Articulos: 4", fontSize = smallSize, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
            if (print.showCashRegister) Text("Caja No.: SF-10", fontSize = smallSize, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
            if (print.showNote) Text("Nota: Cliente prefiere llamar al llegar", fontSize = smallSize, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
            if (print.showItemCount || print.showCashRegister || print.showNote) Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            if (print.showTaxSummary) {
                Text("TOTAL ITBIS PAGADO", fontWeight = FontWeight.Bold, fontSize = bodySize, color = Color.Black, modifier = Modifier.fillMaxWidth())
                Text("TOTAL 18%-ITBIS PAGADO: RD\$ 192.12", fontSize = smallSize, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
                Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            }
            if (print.showReturnPolicy) {
                Text("**VALIDO PARA DEVOLUCION POR 7 DIAS.**", fontSize = smallSize, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
                Text("**DEBE PRESENTAR FACTURA ORIGINAL.**", fontSize = smallSize, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
                Text(sep, fontSize = 8.sp, color = Color.Black, letterSpacing = (-0.5).sp)
            }
            if (print.showThankYouMessage) { Text(print.thankYouMessage, fontSize = 10.sp, color = Color.Black, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth()) }
            if (print.showBarcode) { Box(Modifier.fillMaxWidth().height(30.dp).padding(vertical = 4.dp).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) { Text("[CODIGO DE BARRAS]", fontSize = smallSize, color = AppColors.Gray) } }
            if (print.showQr) { QrPreview(rnc = company.rnc, total = 1265.00, ncf = "E3200000039", size = 60.dp) }
            if (print.showFooterDate) Text("15-05-2026 10:53:48", fontSize = smallSize, color = Color.DarkGray, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun QrPreview(rnc: String, total: Double, ncf: String, size: androidx.compose.ui.unit.Dp) {
    val matrix = remember(rnc, total, ncf) {
        QrGenerator.generate(QrGenerator.dgiiUrl(rnc, ncf, total))
    }
    val cellSize = size / matrix.size
    Box(Modifier.size(size).padding(top = 4.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            for (r in matrix.indices) for (c in matrix[r].indices) {
                if (matrix[r][c]) drawRect(androidx.compose.ui.graphics.Color.Black, Offset(c * cellSize.toPx(), r * cellSize.toPx()), Size(cellSize.toPx(), cellSize.toPx()))
            }
        }
    }
}

@Composable
private fun PrintSwitches(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().background(AppColors.Background).padding(horizontal = 14.dp, vertical = 10.dp)) { Text("Datos de la Empresa", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        PrintToggle("Logo de la empresa", state.settings.print.showCompanyLogo) { state.update { c -> c.copy(print = c.print.copy(showCompanyLogo = it)) } }
        PrintToggle("Nombre de la empresa", state.settings.print.showCompanyName) { state.update { c -> c.copy(print = c.print.copy(showCompanyName = it)) } }
        PrintToggle("RNC de la empresa", state.settings.print.showCompanyRnc) { state.update { c -> c.copy(print = c.print.copy(showCompanyRnc = it)) } }
        PrintToggle("Direccion", state.settings.print.showCompanyAddress) { state.update { c -> c.copy(print = c.print.copy(showCompanyAddress = it)) } }
        PrintToggle("Telefono", state.settings.print.showCompanyPhone) { state.update { c -> c.copy(print = c.print.copy(showCompanyPhone = it)) } }
        PrintToggle("Email", state.settings.print.showCompanyEmail) { state.update { c -> c.copy(print = c.print.copy(showCompanyEmail = it)) } }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().background(AppColors.Background).padding(horizontal = 14.dp, vertical = 10.dp)) { Text("Datos del Cliente", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        PrintToggle("Nombre del cliente", state.settings.print.showCustomerName) { state.update { c -> c.copy(print = c.print.copy(showCustomerName = it)) } }
        PrintToggle("RNC/Cedula del cliente", state.settings.print.showCustomerRnc) { state.update { c -> c.copy(print = c.print.copy(showCustomerRnc = it)) } }
        PrintToggle("Telefono del cliente", state.settings.print.showCustomerPhone) { state.update { c -> c.copy(print = c.print.copy(showCustomerPhone = it)) } }
        PrintToggle("Email del cliente", state.settings.print.showCustomerEmail) { state.update { c -> c.copy(print = c.print.copy(showCustomerEmail = it)) } }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().background(AppColors.Background).padding(horizontal = 14.dp, vertical = 10.dp)) { Text("Detalles de Pago", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        PrintToggle("Mostrar ITBIS/Impuestos", state.settings.print.showTaxes) { state.update { c -> c.copy(print = c.print.copy(showTaxes = it)) } }
        PrintToggle("Mostrar descuentos", state.settings.print.showDiscounts) { state.update { c -> c.copy(print = c.print.copy(showDiscounts = it)) } }
        PrintToggle("Mostrar propina", state.settings.print.showTip) { state.update { c -> c.copy(print = c.print.copy(showTip = it)) } }
        PrintToggle("Metodo de pago", state.settings.print.showPaymentMethod) { state.update { c -> c.copy(print = c.print.copy(showPaymentMethod = it)) } }
        PrintToggle("Mostrar cambio (efectivo)", state.settings.print.showCashChange) { state.update { c -> c.copy(print = c.print.copy(showCashChange = it)) } }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().background(AppColors.Background).padding(horizontal = 14.dp, vertical = 10.dp)) { Text("Encabezado / Pie", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        PrintToggle("Mostrar fecha y hora", state.settings.print.showDateTime) { state.update { c -> c.copy(print = c.print.copy(showDateTime = it)) } }
        PrintToggle("Mostrar nombre del cajero", state.settings.print.showCashierName) { state.update { c -> c.copy(print = c.print.copy(showCashierName = it)) } }
        PrintToggle("Mostrar mensaje de agradecimiento", state.settings.print.showThankYouMessage) { state.update { c -> c.copy(print = c.print.copy(showThankYouMessage = it)) } }
        PrintToggle("Mostrar codigo QR", state.settings.print.showQr) { state.update { c -> c.copy(print = c.print.copy(showQr = it)) } }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().background(AppColors.Background).padding(horizontal = 14.dp, vertical = 10.dp)) { Text("Documento / Factura", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        PrintToggle("Numero de recibo", state.settings.print.showReceiptNumber) { state.update { c -> c.copy(print = c.print.copy(showReceiptNumber = it)) } }
        PrintToggle("NCF", state.settings.print.showNcf) { state.update { c -> c.copy(print = c.print.copy(showNcf = it)) } }
        PrintToggle("Vencimiento NCF", state.settings.print.showNcfExpiry) { state.update { c -> c.copy(print = c.print.copy(showNcfExpiry = it)) } }
        PrintToggle("Titulo de factura", state.settings.print.showInvoiceTitle) { state.update { c -> c.copy(print = c.print.copy(showInvoiceTitle = it)) } }
        PrintToggle("Articulos / productos", state.settings.print.showItems) { state.update { c -> c.copy(print = c.print.copy(showItems = it)) } }
        PrintToggle("Subtotal", state.settings.print.showSubtotal) { state.update { c -> c.copy(print = c.print.copy(showSubtotal = it)) } }
        PrintToggle("Total", state.settings.print.showTotal) { state.update { c -> c.copy(print = c.print.copy(showTotal = it)) } }
        PrintToggle("Cantidad de articulos", state.settings.print.showItemCount) { state.update { c -> c.copy(print = c.print.copy(showItemCount = it)) } }
        PrintToggle("Numero de caja", state.settings.print.showCashRegister) { state.update { c -> c.copy(print = c.print.copy(showCashRegister = it)) } }
        PrintToggle("Nota del cliente", state.settings.print.showNote) { state.update { c -> c.copy(print = c.print.copy(showNote = it)) } }
        PrintToggle("Resumen ITBIS pagado", state.settings.print.showTaxSummary) { state.update { c -> c.copy(print = c.print.copy(showTaxSummary = it)) } }
        PrintToggle("Politica de devolucion", state.settings.print.showReturnPolicy) { state.update { c -> c.copy(print = c.print.copy(showReturnPolicy = it)) } }
        PrintToggle("Codigo de barras", state.settings.print.showBarcode) { state.update { c -> c.copy(print = c.print.copy(showBarcode = it)) } }
        PrintToggle("Fecha al pie", state.settings.print.showFooterDate) { state.update { c -> c.copy(print = c.print.copy(showFooterDate = it)) } }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Mensaje personalizado:", fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
            if (state.settings.print.thankYouMessage.isEmpty()) Text("Gracias por su compra!", color = AppColors.Gray, fontSize = 13.sp)
            BasicTextField(value = state.settings.print.thankYouMessage, onValueChange = { state.update { c -> c.copy(print = c.print.copy(thankYouMessage = it)) } },
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp), modifier = Modifier.fillMaxSize(), singleLine = true)
        }
    }
}

@Composable
private fun PrintToggle(label: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheck, colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Success))
    }
}

// ---------- NOTIFICATIONS SECTION ----------
@Composable
private fun NotificationsSection(
    state: com.tmrestaurant.ui.data.settings.SettingsState,
    scope: kotlinx.coroutines.CoroutineScope,
    snack: SnackbarHostState
) {
    val s = state.settings.notifications
    SectionCard("Notificaciones", "Configura alertas por correo para eventos importantes", Icons.Outlined.Notifications) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Correo SMTP", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Text("Usa una cuenta Gmail dedicada con contrasena de aplicacion", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Switch(checked = s.enabled, onCheckedChange = { state.update { c -> c.copy(notifications = c.notifications.copy(enabled = it)) } }, colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Success))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsInput("Servidor SMTP", s.smtpServer, { state.update { c -> c.copy(notifications = c.notifications.copy(smtpServer = it)) } }, Modifier.weight(1f))
                    SettingsInput("Puerto", s.smtpPort, { state.update { c -> c.copy(notifications = c.notifications.copy(smtpPort = it)) } }, Modifier.width(100.dp))
                }
                SettingsInput("Correo que envia", s.senderEmail, { state.update { c -> c.copy(notifications = c.notifications.copy(senderEmail = it)) } })
                SettingsInput("Contrasena de aplicacion", s.appPassword, { state.update { c -> c.copy(notifications = c.notifications.copy(appPassword = it)) } }, isPassword = true)
                SettingsInput("Nombre remitente", s.senderName, { state.update { c -> c.copy(notifications = c.notifications.copy(senderName = it)) } })
                SettingsInput("Email destino (separar con coma)", s.destinationEmails, { state.update { c -> c.copy(notifications = c.notifications.copy(destinationEmails = it)) } })
                SettingsSwitch("Conexion segura SSL/TLS", "Para Gmail normalmente debe estar activo con puerto 465.", s.sslTls) { state.update { c -> c.copy(notifications = c.notifications.copy(sslTls = it)) } }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Eventos que enviaran correo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsCheck("Cerrar sesion", "Envia un resumen del ultimo turno del usuario.", s.sendOnLogout) { state.update { c -> c.copy(notifications = c.notifications.copy(sendOnLogout = it)) } }
                SettingsCheck("Cierre de caja", "Envia el cuadre profesional con facturas, gastos y diferencia.", s.sendOnCashClose) { state.update { c -> c.copy(notifications = c.notifications.copy(sendOnCashClose = it)) } }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsCheck("Anular factura", "Notifica el numero, total, cajero y motivo de anulacion.", s.sendOnCancelInvoice) { state.update { c -> c.copy(notifications = c.notifications.copy(sendOnCancelInvoice = it)) } }
                SettingsCheck("Eliminar factura", "Queda preparado para cuando exista eliminacion directa.", s.sendOnDeleteInvoice) { state.update { c -> c.copy(notifications = c.notifications.copy(sendOnDeleteInvoice = it)) } }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionBtn("Actualizar") {
                state.save()
                scope.launch { snack.showSnackbar("Configuración guardada") }
            }
            ActionBtn("Probar correo") {
                val n = state.settings.notifications
                val companyEmail = state.settings.company.email
                if (!n.enabled || n.senderEmail.isBlank() || n.appPassword.isBlank()) {
                    scope.launch { snack.showSnackbar("Habilite notificaciones y configure correo/contraseña") }
                } else if (companyEmail.isBlank() && n.destinationEmails.isBlank()) {
                    scope.launch { snack.showSnackbar("Configure un correo destino en Empresa o Destinatarios") }
                } else {
                    scope.launch {
                        val to = companyEmail.ifBlank { n.destinationEmails.split(",").first().trim() }
                        snack.showSnackbar("Enviando correo de prueba a $to...")
                        val config = SmtpConfig(
                            host = n.smtpServer,
                            port = n.smtpPort.toIntOrNull() ?: 465,
                            username = n.senderEmail,
                            password = n.appPassword,
                            useSsl = n.sslTls
                        )
                        val result = withContext(Dispatchers.IO) {
                            sendEmail(
                                config = config,
                                fromName = n.senderName,
                                fromAddr = n.senderEmail,
                                to = to,
                                subject = "Prueba de correo - TM POS",
                                body = "Este es un correo de prueba desde TM POS.\n\nSi recibe este mensaje, la configuración SMTP funciona correctamente."
                            )
                        }
                        if (result.success) {
                            snack.showSnackbar("Correo de prueba enviado exitosamente a $to")
                        } else {
                            snack.showSnackbar("Error al enviar: ${result.error}")
                        }
                    }
                }
            }
            ActionBtn("Guardar correo", AppColors.Primary) {
                state.save()
                scope.launch { snack.showSnackbar("Configuración guardada") }
            }
        }
    }
}

// ---------- LICENSE SECTION ----------
@Composable
private fun LicenseSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val s = state.settings.license
    SectionCard("Licencia", "Verificacion periodica y soporte offline", Icons.Outlined.Shield) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Estado de licencia", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Text("El sistema puede trabajar offline por 5 dias", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    ActionBtn("Actualizar") {}
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoLine("Estado", s.status)
                        InfoLine("Ultima verificacion", s.lastCheck)
                        InfoLine("Dias offline restantes", s.offlineDaysRemaining)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoLine("Empresa", s.companyName)
                        InfoLine("Proxima verificacion", s.nextCheck)
                        InfoLine("Codigo de equipo", s.deviceCode)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Verificar cada (minutos)", fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    SettingsInput("", s.checkIntervalMinutes, {}, Modifier.width(80.dp))
                }
                Text("El Topbar verificara la licencia automaticamente con este intervalo", fontSize = 11.sp, color = AppColors.TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionBtn("Verificar ahora") {}
                    ActionBtn("Guardar configuracion", AppColors.Primary) {}
                }
            }
        }
    }
}

// ---------- FISCAL SECTION ----------
@Composable
private fun FiscalSection(scope: CoroutineScope, snack: SnackbarHostState) {
    var sequences by remember { mutableStateOf(NcfManager.allSequences()) }

    fun updateLocal(updated: FiscalSequence) {
        sequences = sequences.map { if (it.type == updated.type) updated else it }
        NcfManager.updateSequence(updated)
    }

    SectionCard("Fiscalidad NCF", "Rangos autorizados, secuencias y vencimientos", Icons.Outlined.Receipt) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF8FAFC)).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Receipt, null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
                Column(Modifier.weight(1f)) {
                    Text("Secuencias por tipo de comprobante", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                    Text("El NCF se incrementa por tipo y se valida contra el rango configurado.", fontSize = 11.sp, color = AppColors.TextSecondary)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        sequences.forEach { seq ->
            FiscalSequenceCard(
                sequence = seq,
                onChange = ::updateLocal
            )
            Spacer(Modifier.height(10.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            ActionBtn("Guardar secuencias", AppColors.Primary) {
                sequences.forEach(NcfManager::updateSequence)
                scope.launch { snack.showSnackbar("Secuencias fiscales guardadas") }
            }
        }
    }
}

@Composable
private fun FiscalSequenceCard(
    sequence: FiscalSequence,
    onChange: (FiscalSequence) -> Unit
) {
    val lowRemaining = sequence.enabled && sequence.remaining <= 10
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, if (lowRemaining) Color(0xFFF59E0B) else AppColors.Border, RoundedCornerShape(12.dp)).padding(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(sequence.type.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("Proximo: ${sequence.prefix}${(sequence.current + 1).toString().padStart(8, '0')} | Restantes: ${sequence.remaining}", fontSize = 11.sp, color = if (lowRemaining) Color(0xFFD97706) else AppColors.TextSecondary)
                }
                Switch(
                    checked = sequence.enabled,
                    onCheckedChange = { onChange(sequence.copy(enabled = it)) },
                    colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Success)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsInput("Prefijo", sequence.prefix, { onChange(sequence.copy(prefix = it.uppercase().take(4))) }, Modifier.width(100.dp))
                SettingsInput("Actual", sequence.current.toString(), { onChange(sequence.copy(current = it.filter(Char::isDigit).toIntOrNull() ?: 0)) }, Modifier.weight(1f))
                SettingsInput("Inicio", sequence.rangeStart.toString(), { onChange(sequence.copy(rangeStart = it.filter(Char::isDigit).toIntOrNull() ?: 1)) }, Modifier.weight(1f))
                SettingsInput("Final", sequence.rangeEnd.toString(), { onChange(sequence.copy(rangeEnd = it.filter(Char::isDigit).toIntOrNull() ?: 99999999)) }, Modifier.weight(1f))
                SettingsInput("Valido hasta", sequence.validUntil, { onChange(sequence.copy(validUntil = it.take(10))) }, Modifier.width(130.dp))
            }
        }
    }
}

// ---------- SERVER SECTION ----------
@Composable
private fun ServerSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val s = state.settings.server
    SectionCard("Servidor de Sincronizacion", "Configura el servidor para enviar facturas", Icons.Outlined.Cloud) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Configuracion del Servidor", fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text("Conecta tu POS con tu servidor personal", fontSize = 12.sp, color = AppColors.TextSecondary) }
                    Switch(checked = s.enabled, onCheckedChange = { state.update { c -> c.copy(server = c.server.copy(enabled = it)) } }, colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Success))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsInput("URL del Servidor", s.serverUrl, { state.update { c -> c.copy(server = c.server.copy(serverUrl = it)) } }, Modifier.weight(1f))
                    SettingsInput("Ruta de la API", s.apiRoute, { state.update { c -> c.copy(server = c.server.copy(apiRoute = it)) } }, Modifier.width(140.dp))
                }
                SettingsInput("API Key", s.apiKey, { state.update { c -> c.copy(server = c.server.copy(apiKey = it)) } }, Modifier.fillMaxWidth())
                Text("API Key requerida para autenticarse con el servidor TMPOS. Configurala en el dashboard del servidor.", fontSize = 11.sp, color = AppColors.TextSecondary)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Datos a sincronizar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SyncOptionCard("Facturas", "Envia las facturas al servidor cuando se generan.", s.syncInvoices) { state.update { c -> c.copy(server = c.server.copy(syncInvoices = it)) } }
                SyncOptionCard("Productos", "Sincroniza el catalogo de productos. (Proximamente)", s.syncProducts) { state.update { c -> c.copy(server = c.server.copy(syncProducts = it)) } }
                SyncOptionCard("Cierres de Caja", "Envia los cierres de caja al servidor. (Proximamente)", s.syncCashClosings) { state.update { c -> c.copy(server = c.server.copy(syncCashClosings = it)) } }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SyncOptionCard("Envio automatico", "Envia la factura inmediatamente al crearla", s.automaticSend, Modifier.weight(1f)) { state.update { c -> c.copy(server = c.server.copy(automaticSend = it)) } }
                SyncOptionCard("Clientes", "Sincroniza la base de clientes. (Proximamente)", s.syncCustomers) { state.update { c -> c.copy(server = c.server.copy(syncCustomers = it)) } }
            }
        }
        Spacer(Modifier.height(8.dp))
        InfoLine("Ultima sincronizacion", s.lastSync)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionBtn("Actualizar") {}; ActionBtn("Probar conexion") {}; ActionBtn("Guardar configuracion", AppColors.Primary) {}
        }
        Spacer(Modifier.height(20.dp))
        SyncQueueSection()
    }
}

@Composable
private fun SyncQueueSection() {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Cola de Sincronizacion", fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text("Items pendientes de enviar al servidor", fontSize = 12.sp, color = AppColors.TextSecondary) }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Badge("0 pendientes", Color(0xFFFEF3C7), Color(0xFFD97706))
                    Badge("14 enviados", Color(0xFFDCFCE7), Color(0xFF16A34A))
                    Badge("0 fallidos", Color(0xFFFEE2E2), Color(0xFFEF4444))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Actualizar") {}; ActionBtn("Enviar pendientes") {}; ActionBtn("Reintentar fallidos") {}; ActionBtn("Limpiar enviados") {}
            }
            com.tmrestaurant.ui.data.settings.mockSyncQueue.forEach { item ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("ID ${item.id}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text("${item.type} - ${item.reference}", fontSize = 11.sp, color = AppColors.TextSecondary) }
                    Badge(item.status, Color(0xFFDCFCE7), Color(0xFF16A34A))
                    Spacer(Modifier.width(8.dp))
                    Text("${item.attempts} intento(s)", fontSize = 11.sp, color = AppColors.TextSecondary)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(18.dp).clickable { })
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

// ---------- SYSTEM SECTION ----------
@Composable
private fun SystemSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val s = state.settings.system
    val scope = rememberCoroutineScope()
    var backups by remember { mutableStateOf(com.tmrestaurant.ui.data.BackupManager.listBackups()) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    SectionCard("Sistema", "Configuracion avanzada", Icons.Outlined.Storage) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Servidor local de mesas y comandas", fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text("Configura la direccion para abrir la vista local", fontSize = 12.sp, color = AppColors.TextSecondary) }
                    ActionBtn("Actualizar") {}
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsInput("IP de escucha", s.localServerIp, {}, Modifier.weight(1f))
                    SettingsInput("Puerto", s.localServerPort, {}, Modifier.width(120.dp))
                }
                Text("Usa 0.0.0.0 para permitir acceso desde la red local.", fontSize = 11.sp, color = AppColors.TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { ActionBtn("Cargar IP local") {}; ActionBtn("Guardar y reiniciar servidor", AppColors.Primary) {} }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column { Text("IPs detectadas:", fontWeight = FontWeight.Medium, fontSize = 12.sp); s.detectedIps.forEach { Text(it, fontSize = 12.sp, color = AppColors.Primary) } }
                }
                Text(s.availableUrl, color = AppColors.Info, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        // Backups section
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Backups de base de datos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text("Se crea un backup automatico al cerrar sesion", fontSize = 12.sp, color = AppColors.TextSecondary) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionBtn("Actualizar") {
                            backups = com.tmrestaurant.ui.data.BackupManager.listBackups()
                            backupMessage = "Lista actualizada: ${backups.size} backup(s)"
                        }
                        ActionBtn("Crear backup", AppColors.Primary) {
                            val created = com.tmrestaurant.ui.data.BackupManager.createBackup("manual")
                            backups = com.tmrestaurant.ui.data.BackupManager.listBackups()
                            backupMessage = "Backup creado: ${created.fileName}"
                        }
                    }
                }
                backupMessage?.let { message ->
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFDCFCE7)).padding(10.dp)) {
                        Text(message, color = Color(0xFF166534), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (backups.isEmpty()) {
                    Text("No hay backups creados todavia", fontSize = 12.sp, color = AppColors.TextSecondary)
                }
                backups.forEach { backup ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Background).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(backup.fileName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${backup.date} - ${backup.sizeKb} kB", fontSize = 11.sp, color = AppColors.TextSecondary) }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionBtn("Restaurar") {
                                val restored = com.tmrestaurant.ui.data.BackupManager.restoreBackup(backup.fileName)
                                backupMessage = if (restored) {
                                    "Backup restaurado. Reinicie la app para recargar datos en memoria. ${formatDateTime(System.currentTimeMillis())}"
                                } else {
                                    "No se pudo restaurar ${backup.fileName}"
                                }
                            }
                            ActionBtn("Enviar") {
                                scope.launch {
                                    val companyEmail = state.settings.company.email
                                    val notifications = state.settings.notifications
                                    if (companyEmail.isBlank()) {
                                        backupMessage = "Configure el correo de la empresa para enviar backups"
                                        return@launch
                                    }
                                    if (notifications.senderEmail.isBlank() || notifications.appPassword.isBlank()) {
                                        backupMessage = "Configure SMTP para enviar backups"
                                        return@launch
                                    }
                                    val content = com.tmrestaurant.ui.data.BackupManager.readBackupText(backup.fileName)
                                    if (content.isNullOrBlank()) {
                                        backupMessage = "No se pudo leer el backup ${backup.fileName}"
                                        return@launch
                                    }
                                    val result = withContext(Dispatchers.IO) {
                                        sendEmail(
                                            config = SmtpConfig(
                                                host = notifications.smtpServer,
                                                port = notifications.smtpPort.toIntOrNull() ?: 465,
                                                username = notifications.senderEmail,
                                                password = notifications.appPassword,
                                                useSsl = notifications.sslTls
                                            ),
                                            fromName = notifications.senderName,
                                            fromAddr = notifications.senderEmail,
                                            to = companyEmail,
                                            subject = "Backup TM POS - ${backup.fileName}",
                                            body = "Archivo: ${backup.fileName}\nFecha: ${backup.date}\nTamano: ${backup.sizeKb} kB\n\nContenido:\n$content"
                                        )
                                    }
                                    backupMessage = if (result.success) "Backup enviado a $companyEmail" else "Error enviando backup: ${result.error}"
                                }
                            }
                            Icon(Icons.Outlined.Delete, null, tint = AppColors.Danger, modifier = Modifier.size(20.dp).clickable {
                                if (com.tmrestaurant.ui.data.BackupManager.deleteBackup(backup.fileName)) {
                                    backups = com.tmrestaurant.ui.data.BackupManager.listBackups()
                                    backupMessage = "Backup eliminado: ${backup.fileName}"
                                } else {
                                    backupMessage = "No se pudo eliminar ${backup.fileName}"
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

// ---------- ELO PERIPHERALS SECTION ----------
@Composable
private fun EloSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    // State para impresora
    var eloPrinters by remember { mutableStateOf(emptyList<DiscoveredPrinter>()) }
    var isDiscovering by remember { mutableStateOf(false) }
    var selectedEloPrinter by remember { mutableStateOf("") }

    // State para resultados de pruebas
    var printerResult by remember { mutableStateOf<PeripheralTestResult?>(null) }
    var drawerResult by remember { mutableStateOf<PeripheralTestResult?>(null) }
    var scannerResult by remember { mutableStateOf<PeripheralTestResult?>(null) }
    var isPrintingTest by remember { mutableStateOf(false) }
    var isOpeningDrawer by remember { mutableStateOf(false) }
    var isTestingScanner by remember { mutableStateOf(false) }

    // State para barcode scanner test
    var barcodeInput by remember { mutableStateOf("") }
    var scanCount by remember { mutableIntStateOf(0) }

    // State para card reader test (mejorado con CardReaderHelper)
    val cardReaderHelper = remember { CardReaderHelper() }
    var cardReaderResult by remember { mutableStateOf<PeripheralTestResult?>(null) }
    var cardReaderState by remember { mutableStateOf(CardReaderState.IDLE) }
    var cardInput by remember { mutableStateOf("") }
    var cardNormalized by remember { mutableStateOf("") }
    var cardParseResult by remember { mutableStateOf<MagstripeParseResult?>(null) }
    var showDiagnostics by remember { mutableStateOf(false) }
    val cardDiagnostics = remember { mutableStateListOf<KeyDiagnostic>() }
    var cardPollCount by remember { mutableIntStateOf(0) }

    // Configurar callbacks del helper
    LaunchedEffect(Unit) {
        cardReaderHelper.onStateChanged = { newState ->
            cardReaderState = newState
        }
        cardReaderHelper.onBufferChanged = { data ->
            cardInput = data
        }
        cardReaderHelper.onDiagnosticAdded = { diag ->
            cardDiagnostics.add(diag)
        }
        cardReaderHelper.onReadComplete = { result ->
            cardNormalized = result.normalized
            cardParseResult = result
            cardReaderResult = if (result.hasValidStructure) {
                val detail = result.maskedPan?.let { " - ${result.brand}: $it" } ?: " (${result.normalized.length} chars)"
                PeripheralTestResult(true, "Tarjeta leida exitosamente$detail")
            } else {
                PeripheralTestResult(false, "Lectura invalida o posible problema de layout/configuracion del lector")
            }
        }
    }

    // Auto-limpiar datos sensibles despues de 60 segundos
    if (cardReaderState == CardReaderState.VALID || cardReaderState == CardReaderState.INVALID) {
        LaunchedEffect(cardReaderState) {
            delay(60_000)
            cardReaderHelper.clearCardReaderTest()
            cardInput = ""
            cardNormalized = ""
            cardParseResult = null
            cardDiagnostics.clear()
            cardReaderResult = PeripheralTestResult(true, "Datos limpiados automaticamente por seguridad")
        }
    }

    // Poll for scanner data every 500ms while scanner is active
    if (isTestingScanner) {
        LaunchedEffect(scanCount) {
            delay(500)
            val code = eloGetLastScanCode()
            if (code != null && code != barcodeInput && code.isNotBlank()) {
                barcodeInput = code
                eloClearLastScanCode()
            }
            scanCount++
        }
    }

    // Poll for card reader data from platform (Android broadcast/USB HID) every 500ms
    if (cardReaderState == CardReaderState.WAITING || cardReaderState == CardReaderState.READING) {
        LaunchedEffect(cardPollCount) {
            delay(500)
            val data = eloGetLastCardData()
            if (data != null && data != cardInput && data.isNotBlank()) {
                // Datos recibidos via plataforma nativa (Android USB HID / broadcast)
                cardInput = data
                eloClearLastCardData()
                val result = cardReaderHelper.parseMagstripeData(data)
                cardNormalized = result.normalized
                cardParseResult = result
                cardReaderState = if (result.hasValidStructure) CardReaderState.VALID else CardReaderState.INVALID
                cardReaderResult = if (result.hasValidStructure) {
                    val detail = result.maskedPan?.let { " - ${result.brand}: $it" } ?: " (${data.length} caracteres)"
                    PeripheralTestResult(true, "Tarjeta leida exitosamente$detail")
                } else {
                    PeripheralTestResult(false, "Lectura invalida o posible problema de layout")
                }
            }
            cardPollCount++
        }
    }

    Box {
        Column {
            SectionCard("Perifericos ELO", "Prueba los perifericos integrados del ELO PayPoint", Icons.Outlined.Build) {
                // Info box
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F5FF)).border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("ELO PayPoint Plus", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Terminal POS con impresora Star Micronics TSP100III, gaveta de efectivo, lector de codigos Honeywell N3680 y lector de tarjetas USB integrados.",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Badge("Star TSP100III", Color(0xFFDCFCE7), Color(0xFF16A34A))
                            Badge("Honeywell N3680", Color(0xFFDBEAFE), Color(0xFF3B82F6))
                            Badge("Cash Drawer", Color(0xFFFEF3C7), Color(0xFFD97706))
                            Badge("Card Reader", Color(0xFFF3E8FF), Color(0xFF9333EA))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ===== 1. IMPRESORA =====
                Text("Impresora", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                Text("Descubre e imprime un ticket de prueba en la impresora Star Micronics", fontSize = 12.sp, color = AppColors.TextSecondary)
                Spacer(Modifier.height(10.dp))

                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        // Discover button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isDiscovering) AppColors.Gray else AppColors.Primary)
                                    .clickable {
                                        if (!isDiscovering) {
                                            isDiscovering = true
                                            printerResult = null
                                            scope.launch {
                                                try {
                                                    eloPrinters = eloDiscoverPrinters()
                                                    if (eloPrinters.isEmpty()) {
                                                        printerResult = PeripheralTestResult(false, "No se encontraron impresoras conectadas")
                                                    }
                                                } catch (e: Exception) {
                                                    printerResult = PeripheralTestResult(false, "Error: ${e.message}")
                                                } finally {
                                                    isDiscovering = false
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Print, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (isDiscovering) "Buscando..." else "Buscar impresoras",
                                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (eloPrinters.isNotEmpty()) {
                                Spacer(Modifier.width(10.dp))
                                Badge("${eloPrinters.size} encontrada(s)", Color(0xFFDCFCE7), Color(0xFF16A34A))
                            }
                        }

                        // Printer list
                        if (eloPrinters.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            eloPrinters.forEach { printer ->
                                Row(
                                    Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedEloPrinter == printer.identifier) Color(0xFFF0F5FF) else AppColors.Background)
                                        .border(
                                            1.dp,
                                            if (selectedEloPrinter == printer.identifier) AppColors.Primary else AppColors.Border,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedEloPrinter = printer.identifier }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier.size(18.dp).clip(RoundedCornerShape(9.dp))
                                            .border(2.dp, if (selectedEloPrinter == printer.identifier) AppColors.Primary else AppColors.Border, RoundedCornerShape(9.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedEloPrinter == printer.identifier) {
                                            Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(AppColors.Primary))
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(printer.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                                    Badge(printer.type, Color(0xFFDCFCE7), Color(0xFF16A34A))
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        // Print test button
                        if (selectedEloPrinter.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isPrintingTest) AppColors.Gray else AppColors.Success)
                                    .clickable {
                                        if (!isPrintingTest) {
                                            isPrintingTest = true
                                            printerResult = null
                                            scope.launch {
                                                try {
                                                    val company = state.settings.company
                                                    val content = buildEloTestTicket(company)
                                                    printerResult = eloPrintTicketCopies(selectedEloPrinter, content, state.settings.print.paperWidthMm, state.settings.print.copies)
                                                } catch (e: Exception) {
                                                    printerResult = PeripheralTestResult(false, "Error: ${e.message}")
                                                } finally {
                                                    isPrintingTest = false
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Print, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (isPrintingTest) "Imprimiendo..." else "Imprimir ticket de prueba",
                                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Printer result
                        if (printerResult != null) {
                            Spacer(Modifier.height(8.dp))
                            EloTestResultBox(printerResult!!)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ===== 2. GAVETA DE EFECTIVO =====
                Text("Gaveta de Efectivo (Cash Drawer)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                Text("Abre la gaveta conectada al puerto de la impresora Star", fontSize = 12.sp, color = AppColors.TextSecondary)
                Spacer(Modifier.height(10.dp))

                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Abrir Gaveta", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                                Text("Abre la gaveta conectada al puerto de la impresora", fontSize = 12.sp, color = AppColors.TextSecondary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(
                                Modifier.height(48.dp).clip(RoundedCornerShape(12.dp))
                                    .background(if (isOpeningDrawer) AppColors.Gray else Color(0xFFD97706))
                                    .clickable {
                                        if (!isOpeningDrawer) {
                                            isOpeningDrawer = true
                                            drawerResult = null
                                            scope.launch {
                                                try {
                                                    drawerResult = eloOpenCashDrawerNoPrinter()
                                                } catch (e: Exception) {
                                                    drawerResult = PeripheralTestResult(false, "Error: ${e.message}")
                                                } finally {
                                                    isOpeningDrawer = false
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (isOpeningDrawer) "Abriendo..." else "Abrir gaveta",
                                        color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (drawerResult != null) {
                            Spacer(Modifier.height(8.dp))
                            EloTestResultBox(drawerResult!!)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ===== 3. LECTOR DE CODIGOS DE BARRAS =====
                Text("Lector de Codigos de Barras", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                Text("Prueba el scanner Honeywell N3680 integrado (modo HID)", fontSize = 12.sp, color = AppColors.TextSecondary)
                Spacer(Modifier.height(10.dp))

                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            "El scanner Honeywell opera en modo HID (teclado). Enfoque el campo de abajo y escanee un codigo de barras. El resultado aparecera automaticamente.",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )
                        Spacer(Modifier.height(10.dp))

                        // Barcode input field
                        Text("Resultado del escaneo:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp))
                                .background(AppColors.Background).border(2.dp, AppColors.Primary, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (barcodeInput.isEmpty()) {
                                Text("Esperando escaneo...", color = AppColors.Gray, fontSize = 14.sp)
                            }
                            BasicTextField(
                                value = barcodeInput,
                                onValueChange = { barcodeInput = it },
                                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isTestingScanner) AppColors.Gray else AppColors.Info)
                                    .clickable {
                                        if (!isTestingScanner) {
                                            isTestingScanner = true
                                            scannerResult = null
                                            scope.launch {
                                                try {
                                                    scannerResult = eloTestBarcodeScanner()
                                                } catch (e: Exception) {
                                                    scannerResult = PeripheralTestResult(false, "Error: ${e.message}")
                                                } finally {
                                                    isTestingScanner = false
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Settings, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (isTestingScanner) "Probando..." else "Probar scanner",
                                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (barcodeInput.isNotEmpty()) {
                                Box(
                                    Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                        .background(AppColors.DangerLight)
                                        .clickable { barcodeInput = "" }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Limpiar", color = AppColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (barcodeInput.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            EloTestResultBox(PeripheralTestResult(true, "Codigo escaneado: $barcodeInput"))
                        }

                        if (scannerResult != null) {
                            Spacer(Modifier.height(8.dp))
                            EloTestResultBox(scannerResult!!)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ===== 4. LECTOR DE TARJETAS USB (MEJORADO) =====
                Text("Lector de Tarjetas (MagTek MSR)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                Text("Captura tarjetas magneticas con MagTek Dynamag; usa USB HID solo como respaldo", fontSize = 12.sp, color = AppColors.TextSecondary)
                Spacer(Modifier.height(10.dp))

                val cardFocusRequester = remember { FocusRequester() }
                val isCardActive = cardReaderState == CardReaderState.WAITING || cardReaderState == CardReaderState.READING

                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        // Instrucciones
                        Text(
                            "Presione 'Probar lector' y pase la tarjeta. En PayPoint se usa MagTek Dynamag; " +
                                    "no se aceptan lecturas por teclado/HID porque devuelven caracteres incorrectos.",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )
                        Spacer(Modifier.height(10.dp))

                        // Estado actual
                        val stateLabel = when (cardReaderState) {
                            CardReaderState.IDLE -> "Inactivo"
                            CardReaderState.WAITING -> "Esperando tarjeta..."
                            CardReaderState.READING -> "Leyendo tarjeta..."
                            CardReaderState.DETECTED -> "Tarjeta detectada"
                            CardReaderState.VALID -> "Lectura valida"
                            CardReaderState.INVALID -> "Lectura invalida"
                        }
                        val stateColor = when (cardReaderState) {
                            CardReaderState.IDLE -> AppColors.Gray
                            CardReaderState.WAITING -> Color(0xFF9333EA)
                            CardReaderState.READING -> Color(0xFFD97706)
                            CardReaderState.DETECTED -> Color(0xFF2563EB)
                            CardReaderState.VALID -> Color(0xFF059669)
                            CardReaderState.INVALID -> Color(0xFFDC2626)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(stateColor))
                            Spacer(Modifier.width(6.dp))
                            Text("Estado: $stateLabel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = stateColor)
                            if (cardInput.isNotEmpty()) {
                                Spacer(Modifier.width(12.dp))
                                Text("(${cardInput.length} caracteres)", fontSize = 12.sp, color = AppColors.TextSecondary)
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        // Campo de captura de datos crudos
                        Text("Datos crudos (raw):", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp))
                                .background(if (isCardActive) Color(0xFFF5F3FF) else AppColors.Background)
                                .border(2.dp, if (isCardActive) Color(0xFF9333EA) else AppColors.Border, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp)
                                .focusRequester(cardFocusRequester)
                                .focusable(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (cardInput.isEmpty()) {
                                Text(
                                    if (isCardActive) "Esperando tarjeta... (pase la tarjeta)" else "Presione 'Probar lector' para iniciar",
                                    color = AppColors.Gray, fontSize = 14.sp
                                )
                            } else {
                                // Mostrar numero de tarjeta enmascarado si se pudo extraer.
                                val displayText = cardParseResult?.maskedPan ?: cardParseResult?.masked ?: cardInput
                                Text(displayText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        // Campo normalizado (si hay parseo completo)
                        if (cardParseResult != null) {
                            Spacer(Modifier.height(8.dp))
                            Text("Datos normalizados:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0FDF4)).border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    val pr = cardParseResult!!
                                    if (pr.maskedPan != null) {
                                        Text("Numero: ${pr.maskedPan}", fontSize = 14.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                                        Text("Marca: ${pr.brand}", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    if (pr.track1 != null) {
                                        Text("Track 1: ${pr.track1}", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (pr.track2 != null) {
                                        Text("Track 2: ${pr.track2}", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (pr.track3 != null) {
                                        Text("Track 3: ${pr.track3}", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                                    }
                                    if (pr.track1 == null && pr.track2 == null) {
                                        Text("No se detectaron tracks validos", fontSize = 12.sp, color = Color(0xFFDC2626))
                                    }
                                    if (pr.warnings.isNotEmpty()) {
                                        Spacer(Modifier.height(4.dp))
                                        pr.warnings.forEach { w ->
                                            Text("⚠ $w", fontSize = 11.sp, color = Color(0xFFD97706))
                                        }
                                    }
                                    if (cardReaderHelper.isProbablyCardSwipe()) {
                                        Text("Lectura automatica (swipe detectado)", fontSize = 11.sp, color = Color(0xFF059669))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Botones de accion
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Probar / Detener
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isCardActive) Color(0xFF7C3AED) else Color(0xFF9333EA))
                                    .clickable {
                                        if (!isCardActive) {
                                            // Iniciar prueba
                                            cardReaderHelper.startCardReaderTest()
                                            cardReaderResult = null
                                            cardInput = ""
                                            cardNormalized = ""
                                            cardParseResult = null
                                            cardDiagnostics.clear()
                                            cardFocusRequester.requestFocus()
                                            scope.launch {
                                                try {
                                                    val platformResult = eloTestCardReader()
                                                    cardReaderResult = platformResult
                                                } catch (e: Exception) {
                                                    cardReaderResult = PeripheralTestResult(false, "Error: ${e.message}")
                                                }
                                            }
                                        } else {
                                            // Detener prueba
                                            cardReaderHelper.stopCardReaderTest()
                                            if (cardInput.isEmpty()) {
                                                cardReaderResult = PeripheralTestResult(true, "Lector detenido. No se recibieron datos.")
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Settings, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (isCardActive) "Detener prueba" else "Probar lector",
                                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            // Limpiar
                            if (cardInput.isNotEmpty() || cardParseResult != null) {
                                Box(
                                    Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                        .background(AppColors.DangerLight)
                                        .clickable {
                                            cardReaderHelper.clearCardReaderTest()
                                            cardInput = ""
                                            cardNormalized = ""
                                            cardParseResult = null
                                            cardReaderResult = null
                                            cardDiagnostics.clear()
                                        }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Limpiar", color = AppColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            // Toggle diagnosticos
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (showDiagnostics) Color(0xFFDDD6FE) else Color(0xFFF3F4F6))
                                    .clickable { showDiagnostics = !showDiagnostics }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (showDiagnostics) "Ocultar diagnostico" else "Ver diagnostico",
                                    color = if (showDiagnostics) Color(0xFF7C3AED) else AppColors.TextSecondary,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Resultado general
                        if (cardReaderResult != null) {
                            Spacer(Modifier.height(8.dp))
                            EloTestResultBox(cardReaderResult!!)
                        }

                        // Panel de diagnostico de teclas
                        if (showDiagnostics && cardDiagnostics.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text("Diagnostico de teclas (${cardDiagnostics.size} eventos):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1F2937)).padding(8.dp)
                            ) {
                                val diagScrollState = rememberLazyListState()
                                LaunchedEffect(cardDiagnostics.size) {
                                    if (cardDiagnostics.isNotEmpty()) {
                                        diagScrollState.animateScrollToItem(cardDiagnostics.size - 1)
                                    }
                                }
                                LazyColumn(state = diagScrollState) {
                                    items(cardDiagnostics) { d ->
                                        val charStr = d.charAdded?.let { "'$it'" } ?: "null"
                                        Text(
                                            "key=${d.key} | code=${d.keyCode} | native=${d.nativeKeyCode} | scan=${d.scanCode} | " +
                                                    "shift=${d.isShiftPressed} ctrl=${d.isCtrlPressed} alt=${d.isAltPressed} | " +
                                                    "dt=${d.timeSincePrevMs}ms | char=$charStr",
                                            fontSize = 10.sp, color = Color(0xFF10B981),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }

                        // Advertencia de layout si hay problema
                        if (cardParseResult != null && cardParseResult!!.warnings.any { "layout" in it.lowercase() }) {
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF3C7)).border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "Posible problema de layout: El lector MSR envia scancodes US, pero el sistema puede estar " +
                                            "interpretando con layout espanol/latino. Esta version usa mapeo directo de Key codes " +
                                            "para evitar este problema. Si persiste, verifique la configuracion de idioma del SO.",
                                    fontSize = 11.sp, color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ===== 5. PANTALLA DEL CLIENTE (CFD) =====
                Text("Pantalla del Cliente (Customer Display)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                Text("Prueba la pantalla VFD de 2 lineas para mostrar informacion al cliente", fontSize = 12.sp, color = AppColors.TextSecondary)
                Spacer(Modifier.height(10.dp))

                CustomerDisplayTestPanel(scope)

                Spacer(Modifier.height(20.dp))

                // ===== INFO SDK =====
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFFBEB)).border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Informacion del SDK", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Para integracion completa con los perifericos ELO PayPoint, se recomienda agregar el StarXpand SDK:",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1F2937)).padding(12.dp)
                        ) {
                            Text(
                                "implementation(\"com.starmicronics:stario10:1.12.0\")",
                                fontSize = 12.sp, color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Impresora: Star Micronics TSP100III (80mm, 230mm/s)\n" +
                                    "Scanner: Honeywell N3680 1D/2D (modo HID)\n" +
                                    "Gaveta: Puerto Star Micronics pin-out\n" +
                                    "Lector tarjetas: USB Swipe Reader (modo HID)\n" +
                                    "SDK: StarXpand SDK v1.12.0 - Android 11+",
                            fontSize = 11.sp, color = AppColors.TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
        SnackbarHost(snack, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ---------- CUSTOMER DISPLAY TEST PANEL ----------
@Composable
private fun CustomerDisplayTestPanel(scope: kotlinx.coroutines.CoroutineScope) {
    // State
    var cfdResult by remember { mutableStateOf<CfdTestResult?>(null) }
    var isDetecting by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var isRunningSequence by remember { mutableStateOf(false) }
    var sequenceStep by remember { mutableStateOf("") }

    // Manual test inputs
    var customLine1 by remember { mutableStateOf("Bienvenido") }
    var customLine2 by remember { mutableStateOf("Pase sus productos") }
    var testProductName by remember { mutableStateOf("Pollo Asado") }
    var testProductPrice by remember { mutableStateOf("450.00") }
    var testProductQty by remember { mutableStateOf("2") }
    var testSubtotal by remember { mutableStateOf("900.00") }
    var testDiscount by remember { mutableStateOf("50.00") }
    var testTax by remember { mutableStateOf("153.00") }
    var testTotal by remember { mutableStateOf("1003.00") }
    var testReceived by remember { mutableStateOf("1100.00") }
    var testChange by remember { mutableStateOf("97.00") }

    // Selected test tab
    var selectedTab by remember { mutableStateOf(0) }

    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            // Info box
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F5FF)).border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("VFD Customer Facing Display", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppColors.TextPrimary)
                        Text("Pantalla de 2 lineas x 16 caracteres integrada en el ELO PayPoint", fontSize = 11.sp, color = AppColors.TextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge("VFD", Color(0xFFE0E7FF), Color(0xFF4F46E5))
                        Badge("16 chars", Color(0xFFFEF3C7), Color(0xFFD97706))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Detect + Status row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                        .background(if (isDetecting) AppColors.Gray else Color(0xFF4F46E5))
                        .clickable {
                            if (!isDetecting) {
                                isDetecting = true
                                cfdResult = null
                                scope.launch {
                                    try {
                                        cfdResult = cfdDetectBackend()
                                    } catch (e: Exception) {
                                        cfdResult = CfdTestResult(false, "Error: ${e.message}")
                                    } finally {
                                        isDetecting = false
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isDetecting) "Detectando..." else "Detectar pantalla",
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                        .background(AppColors.DangerLight)
                        .clickable {
                            scope.launch {
                                try { cfdClear() } catch (_: Exception) {}
                                cfdResult = CfdTestResult(true, "Pantalla limpiada")
                            }
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Limpiar", color = AppColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Detection result
            if (cfdResult != null) {
                Spacer(Modifier.height(8.dp))
                CfdResultBox(cfdResult!!)
            }

            Spacer(Modifier.height(14.dp))

            // Tab selector
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Texto libre", "Producto", "Totales/Pago", "Secuencia").forEachIndexed { idx, label ->
                    Box(
                        Modifier.weight(1f).height(34.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == idx) Color.White else Color.Transparent)
                            .border(if (selectedTab == idx) 1.dp else 0.dp, if (selectedTab == idx) AppColors.Border else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { selectedTab = idx }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = if (selectedTab == idx) FontWeight.SemiBold else FontWeight.Normal, color = if (selectedTab == idx) AppColors.Primary else AppColors.TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            when (selectedTab) {
                // === Tab 0: Texto libre ===
                0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsInput("Linea 1 (max 16 chars)", customLine1, { customLine1 = it })
                        SettingsInput("Linea 2 (max 16 chars)", customLine2, { customLine2 = it })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isTesting) AppColors.Gray else Color(0xFF4F46E5))
                                    .clickable {
                                        if (!isTesting) {
                                            isTesting = true
                                            scope.launch {
                                                try {
                                                    cfdResult = cfdShowCustomMessage(customLine1, customLine2)
                                                } catch (e: Exception) {
                                                    cfdResult = CfdTestResult(false, "Error: ${e.message}")
                                                } finally { isTesting = false }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isTesting) "Enviando..." else "Enviar texto", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF059669))
                                    .clickable {
                                        scope.launch {
                                            cfdResult = cfdShowIdle()
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Mostrar Idle", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // === Tab 1: Producto ===
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsInput("Nombre del producto", testProductName, { testProductName = it })
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SettingsInput("Precio", testProductPrice, { testProductPrice = it }, Modifier.weight(1f))
                            SettingsInput("Cantidad", testProductQty, { testProductQty = it }, Modifier.width(100.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isTesting) AppColors.Gray else Color(0xFF4F46E5))
                                    .clickable {
                                        if (!isTesting) {
                                            isTesting = true
                                            scope.launch {
                                                try {
                                                    cfdResult = cfdShowProduct(
                                                        testProductName,
                                                        testProductPrice.toDoubleOrNull() ?: 0.0,
                                                        testProductQty.toIntOrNull() ?: 1
                                                    )
                                                } catch (e: Exception) {
                                                    cfdResult = CfdTestResult(false, "Error: ${e.message}")
                                                } finally { isTesting = false }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isTesting) "Enviando..." else "Mostrar producto", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0891B2))
                                    .clickable {
                                        scope.launch {
                                            cfdResult = cfdShowCart(
                                                3,
                                                testSubtotal.toDoubleOrNull() ?: 0.0,
                                                testTotal.toDoubleOrNull() ?: 0.0
                                            )
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Mostrar carrito", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // === Tab 2: Totales/Pago ===
                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SettingsInput("Subtotal", testSubtotal, { testSubtotal = it }, Modifier.weight(1f))
                            SettingsInput("Descuento", testDiscount, { testDiscount = it }, Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SettingsInput("ITBIS", testTax, { testTax = it }, Modifier.weight(1f))
                            SettingsInput("Total", testTotal, { testTotal = it }, Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SettingsInput("Recibido", testReceived, { testReceived = it }, Modifier.weight(1f))
                            SettingsInput("Cambio", testChange, { testChange = it }, Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4F46E5))
                                    .clickable {
                                        scope.launch {
                                            cfdResult = cfdShowTotals(
                                                testSubtotal.toDoubleOrNull() ?: 0.0,
                                                testDiscount.toDoubleOrNull() ?: 0.0,
                                                testTax.toDoubleOrNull() ?: 0.0,
                                                testTotal.toDoubleOrNull() ?: 0.0
                                            )
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Mostrar totales", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF059669))
                                    .clickable {
                                        scope.launch {
                                            cfdResult = cfdShowPayment(
                                                testTotal.toDoubleOrNull() ?: 0.0,
                                                testReceived.toDoubleOrNull() ?: 0.0,
                                                testChange.toDoubleOrNull() ?: 0.0
                                            )
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Mostrar pago", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD97706))
                                    .clickable {
                                        scope.launch {
                                            cfdResult = cfdShowThankYou()
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Gracias", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // === Tab 3: Secuencia completa ===
                3 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Ejecuta una secuencia completa de venta simulada: Bienvenida → Producto → Carrito → Totales → Pago → Gracias → Idle",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )

                        if (isRunningSequence) {
                            Box(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0F5FF)).border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Paso actual: ", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                                    Text(sequenceStep, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier.height(38.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isRunningSequence) AppColors.Gray else Color(0xFF4F46E5))
                                    .clickable {
                                        if (!isRunningSequence) {
                                            isRunningSequence = true
                                            scope.launch {
                                                try {
                                                    sequenceStep = "1/6 - Bienvenida"
                                                    cfdResult = cfdShowIdle("Bienvenido", "Pase sus productos")
                                                    delay(2500)

                                                    sequenceStep = "2/6 - Producto"
                                                    cfdResult = cfdShowProduct("Pollo Asado", 450.0, 2)
                                                    delay(2500)

                                                    sequenceStep = "3/6 - Carrito"
                                                    cfdResult = cfdShowCart(3, 900.0, 1003.0)
                                                    delay(2500)

                                                    sequenceStep = "4/6 - Totales"
                                                    cfdResult = cfdShowTotals(900.0, 50.0, 153.0, 1003.0)
                                                    delay(2500)

                                                    sequenceStep = "5/6 - Pago"
                                                    cfdResult = cfdShowPayment(1003.0, 1100.0, 97.0)
                                                    delay(2500)

                                                    sequenceStep = "6/6 - Gracias"
                                                    cfdResult = cfdShowThankYou("Gracias!")
                                                    delay(2500)

                                                    cfdResult = cfdShowIdle()
                                                    cfdResult = CfdTestResult(true, "Secuencia completada exitosamente")
                                                } catch (e: Exception) {
                                                    cfdResult = CfdTestResult(false, "Error en secuencia: ${e.message}")
                                                } finally {
                                                    isRunningSequence = false
                                                    sequenceStep = ""
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (isRunningSequence) "Ejecutando..." else "Iniciar secuencia de prueba",
                                    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // VFD Preview (simulated display)
            Spacer(Modifier.height(14.dp))
            Text("Vista previa del VFD:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.TextSecondary)
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A1628)).padding(12.dp)
            ) {
                Column {
                    val displayL1 = when (selectedTab) {
                        0 -> customLine1.take(16).padEnd(16)
                        1 -> testProductName.take(16).padEnd(16)
                        2 -> {
                            val sub = testSubtotal.toDoubleOrNull() ?: 0.0
                            "Sub:  RD\$${String.format(Locale.US, "%,.2f", sub)}".take(16).padEnd(16)
                        }
                        else -> "Bienvenido      "
                    }
                    val displayL2 = when (selectedTab) {
                        0 -> customLine2.take(16).padEnd(16)
                        1 -> {
                            val q = testProductQty.toIntOrNull() ?: 1
                            val p = testProductPrice.toDoubleOrNull() ?: 0.0
                            "x${q}  RD\$${String.format(Locale.US, "%,.2f", p)}".take(16).padEnd(16)
                        }
                        2 -> {
                            val tot = testTotal.toDoubleOrNull() ?: 0.0
                            "Total:RD\$${String.format(Locale.US, "%,.2f", tot)}".take(16).padEnd(16)
                        }
                        else -> "Pase sus product"
                    }
                    Text(displayL1, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Spacer(Modifier.height(2.dp))
                    Text(displayL2, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun CfdResultBox(result: CfdTestResult) {
    val bgColor = if (result.success) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
    val borderColor = if (result.success) Color(0xFFBBF7D0) else Color(0xFFFECACA)
    val textColor = if (result.success) Color(0xFF16A34A) else Color(0xFFDC2626)

    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(bgColor).border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (result.success) "OK" else "ERROR",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor
                )
                if (result.backend != "none") {
                    Spacer(Modifier.width(8.dp))
                    Badge(result.backend, if (result.success) Color(0xFFDCFCE7) else Color(0xFFFEE2E2), textColor)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(result.message, fontSize = 12.sp, color = textColor, lineHeight = 16.sp)
        }
    }
}

private fun buildEloTestTicket(company: com.tmrestaurant.ui.data.settings.CompanySettings): String {
    return buildTestTicket(company, com.tmrestaurant.ui.data.settings.PrintSettings())
}

@Composable
private fun EloTestResultBox(result: PeripheralTestResult) {
    val bgColor = if (result.success) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
    val borderColor = if (result.success) Color(0xFFBBF7D0) else Color(0xFFFECACA)
    val textColor = if (result.success) Color(0xFF16A34A) else Color(0xFFDC2626)

    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(bgColor).border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (result.success) "OK" else "ERROR",
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor
            )
            Spacer(Modifier.width(8.dp))
            Text(result.message, fontSize = 12.sp, color = textColor)
        }
    }
}

// ---------- ADMIN CARDS SECTION ----------
@Composable
private fun AdminCardsSection(state: com.tmrestaurant.ui.data.settings.SettingsState) {
    val cards = state.settings.adminCards.cards
    val usuarios = UsuariosManager.usuarios
    var showAddDialog by remember { mutableStateOf(false) }

    SectionCard("Tarjetas de Administrador", "Tarjetas autorizadas para anular facturas y otras acciones", Icons.Outlined.CreditCard) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tarjetas Registradas", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Text("${cards.size} tarjeta(s) configurada(s)", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Box(
                        Modifier.height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Primary)
                            .clickable { showAddDialog = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (cards.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay tarjetas registradas. Presiona \"Agregar\" para crear una.", fontSize = 13.sp, color = AppColors.TextSecondary)
            }
        } else {
            cards.forEachIndexed { index, card ->
                val user = usuarios.find { it.id == card.userId }
                val displayName = user?.name ?: card.userName
                val displayRole = user?.role?.name ?: card.userRole
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (displayRole == "ADMIN") Color(0xFFFEF3C7) else Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.CreditCard, null, tint = if (displayRole == "ADMIN") Color(0xFFD97706) else Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                            Text("No. ${card.cardNumber}", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(if (displayRole == "ADMIN") Color(0xFFFEF3C7) else Color(0xFFDBEAFE)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text(displayRole, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (displayRole == "ADMIN") Color(0xFFD97706) else Color(0xFF3B82F6))
                            }
                        }
                        Box(
                            Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2))
                                .clickable {
                                    state.update { c ->
                                        val updated = c.adminCards.cards.toMutableList()
                                        updated.removeAt(index)
                                        c.copy(adminCards = c.adminCards.copy(cards = updated))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showAddDialog) {
        AddAdminCardDialog(
            usuarios = usuarios,
            existingCards = cards,
            onDismiss = { showAddDialog = false },
            onConfirm = { card ->
                state.update { c ->
                    c.copy(adminCards = c.adminCards.copy(cards = c.adminCards.cards + card))
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddAdminCardDialog(
    usuarios: List<Usuario>,
    existingCards: List<AdminCard>,
    onDismiss: () -> Unit,
    onConfirm: (AdminCard) -> Unit
) {
    var selectedUser by remember { mutableStateOf<Usuario?>(null) }
    var cardNumber by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val availableUsers = usuarios.filter { u -> existingCards.none { it.userId == u.id } }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier.width(460.dp).clip(RoundedCornerShape(16.dp)).background(AppColors.Surface).padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.CreditCard, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Agregar Tarjeta Admin", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                }

                Text("Seleccionar Usuario", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                if (availableUsers.isEmpty()) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEF3C7)).padding(12.dp)) {
                        Text("Todos los usuarios ya tienen tarjeta asignada", color = Color(0xFF92400E), fontSize = 13.sp)
                    }
                } else {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                            .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                    ) {
                        Column {
                            availableUsers.forEachIndexed { index, user ->
                                Row(
                                    Modifier.fillMaxWidth().clickable { selectedUser = user }.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).border(2.dp, if (selectedUser?.id == user.id) AppColors.Primary else AppColors.Border, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                        if (selectedUser?.id == user.id) Box(Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Primary))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(user.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                        Text(user.role.displayName, fontSize = 11.sp, color = AppColors.TextSecondary)
                                    }
                                    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(if (user.role == UserRole.ADMIN) Color(0xFFFEF3C7) else Color(0xFFDBEAFE)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                        Text(user.role.displayName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (user.role == UserRole.ADMIN) Color(0xFFD97706) else Color(0xFF3B82F6))
                                    }
                                }
                                if (index < availableUsers.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
                            }
                        }
                    }
                }

                Text("Numero de Tarjeta", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                    BasicTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 20) cardNumber = it },
                        textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                        modifier = Modifier.fillMaxSize(),
                        singleLine = true
                    )
                }

                if (error != null) {
                    Text(error!!, color = AppColors.Danger, fontSize = 13.sp)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancelar", color = AppColors.TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                            .clickable(enabled = selectedUser != null && cardNumber.isNotBlank()) {
                                val user = selectedUser
                                if (user == null) {
                                    error = "Seleccione un usuario"
                                } else if (cardNumber.isBlank()) {
                                    error = "Ingrese el numero de tarjeta"
                                } else {
                                    onConfirm(
                                        AdminCard(
                                            userId = user.id,
                                            userName = user.name,
                                            userRole = user.role.name,
                                            cardNumber = cardNumber
                                        )
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Agregar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ---------- ALANUBE DGII SECTION ----------
@Composable
private fun AlanubeSection() {
    val scope = rememberCoroutineScope()
    val initial = remember { AlanubeConfigStore.load() }

    var environment by remember { mutableStateOf(initial.environment) }
    var jwtToken by remember { mutableStateOf(initial.jwtToken) }
    var companyId by remember { mutableStateOf(initial.companyId) }
    var rnc by remember { mutableStateOf(initial.rnc) }
    var companyName by remember { mutableStateOf(initial.companyName) }
    var address by remember { mutableStateOf(initial.address) }
    var stampDate by remember { mutableStateOf(initial.stampDate) }

    var status by remember { mutableStateOf<AlanubeStatus?>(null) }
    var result by remember { mutableStateOf<AlanubeResult?>(null) }
    var loading by remember { mutableStateOf("") }
    var expandedResponse by remember { mutableStateOf(false) }

    var showEmitModal by remember { mutableStateOf(false) }
    var emitDocType by remember { mutableStateOf("32") }
    var emitEncf by remember { mutableStateOf("") }
    var emitTotal by remember { mutableStateOf("") }
    var emitBuyerRnc by remember { mutableStateOf("") }
    var emitBuyerName by remember { mutableStateOf("") }
    var emitDocId by remember { mutableStateOf("") }

    fun currentConfig() = AlanubeConfig(
        environment = environment, jwtToken = jwtToken.trim(), companyId = companyId.trim(),
        rnc = rnc.trim(), companyName = companyName.trim(), address = address.trim(), stampDate = stampDate.trim()
    )

    fun save() { AlanubeConfigStore.save(currentConfig()); AlanubeService.init(currentConfig()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard("Alanube", "Emision de comprobantes electronicos DGII", Icons.Outlined.Receipt) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background).padding(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AlanubeEnvironment.entries.forEach { env ->
                        val selected = environment == env
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (selected) AppColors.Surface else Color.Transparent)
                                .clickable { environment = env }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(env.label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("URL: ${currentConfig().environment.baseUrl}", fontSize = 11.sp, color = AppColors.TextSecondary)
        }

        SectionCard("Autenticacion", "JWT Token proporcionado por Alanube", Icons.Outlined.Lock) {
            SettingsInput("JWT Token", jwtToken, { jwtToken = it }, isPassword = true)
            Text("Usa el mismo token para Sandbox y Produccion.", fontSize = 10.sp, color = AppColors.TextSecondary)
        }

        SectionCard("Compania", "Datos del emisor", Icons.Outlined.Business) {
            SettingsInput("ID Compania (opcional)", companyId, { companyId = it })
            SettingsInput("RNC Emisor", rnc, { rnc = it.take(11) })
            SettingsInput("Razon Social", companyName, { companyName = it })
            SettingsInput("Direccion", address, { address = it })
            SettingsInput("Fecha Emision", stampDate, { stampDate = it })
        }

        SectionCard("Acciones", "Prueba de conexion, consulta y emision", Icons.Outlined.FlashOn) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Guardar", AppColors.Primary) {
                    save(); loading = "save"; AlanubeService.init(currentConfig())
                    scope.launch { status = AlanubeService.testConnection(); loading = "" }
                }
                ActionBtn("Probar conexion", Color(0xFF16A34A)) {
                    loading = "test"; AlanubeService.init(currentConfig())
                    scope.launch { status = AlanubeService.testConnection(); loading = "" }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Info Compania", Color(0xFF0284C7)) {
                    loading = "company"; AlanubeService.init(currentConfig())
                    scope.launch { result = AlanubeService.getCompanyInfo(); expandedResponse = true; loading = "" }
                }
                ActionBtn("Estado DGII", Color(0xFF7C3AED)) {
                    loading = "dgii"; AlanubeService.init(currentConfig())
                    scope.launch { result = AlanubeService.checkDGIIHealth(); loading = "" }
                }
            }
            Spacer(Modifier.height(8.dp))
            AlanubeButton("Emitir Comprobante", Icons.Outlined.Receipt, false, Modifier.fillMaxWidth(), Color(0xFF0891B2)) {
                emitEncf = ""; emitTotal = ""; emitBuyerRnc = ""; emitBuyerName = ""; emitDocId = ""
                showEmitModal = true
            }
            if (loading == "save") { Spacer(Modifier.height(8.dp)); Text("Guardando...", fontSize = 12.sp, color = AppColors.TextSecondary) }
        }

        status?.let { st ->
            val color = if (st.connected) Color(0xFF16A34A) else Color(0xFFEF4444)
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (st.connected) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)).border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (st.connected) Icons.Outlined.CheckCircle else Icons.Outlined.Warning, null, tint = color)
                    Spacer(Modifier.width(8.dp))
                    Text(if (st.connected) "Conexion exitosa${st.companyName?.let { " - $it" } ?: ""}" else st.error.orEmpty(), color = color, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        result?.let { res ->
            val color = if (res.success) Color(0xFF16A34A) else Color(0xFFEF4444)
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (res.success) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)).border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(if (res.success) Icons.Outlined.Check else Icons.Outlined.Close, null, tint = color)
                        Spacer(Modifier.width(8.dp))
                        Column { Text(res.message, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                            if (res.documentId != null) Text("ID: ${res.documentId}", fontSize = 11.sp, color = AppColors.TextSecondary)
                            if (res.legalStatus != null) Text("Estado Legal: ${res.legalStatus}", fontSize = 11.sp, color = AppColors.TextSecondary)
                        }
                    }
                    if (res.responseBody.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().clickable { expandedResponse = !expandedResponse }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                            Text(if (expandedResponse) "Ocultar" else "Ver respuesta", fontSize = 10.sp, color = Color(0xFF0891B2))
                            Icon(if (expandedResponse) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, tint = Color(0xFF0891B2), modifier = Modifier.size(16.dp))
                        }
                        if (expandedResponse) {
                            Box(Modifier.fillMaxWidth().padding(top = 4.dp).heightIn(max = 200.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).verticalScroll(rememberScrollState()).padding(8.dp)) {
                                Text(res.responseBody, fontSize = 9.sp, color = AppColors.TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmitModal) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = AppColors.Surface), modifier = Modifier.widthIn(max = 420.dp).padding(16.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Emitir Comprobante", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)

                    Text("Tipo", color = AppColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("32" to "E32", "31" to "E31", "45" to "E45", "34" to "E34", "33" to "E33").forEach { (code, label) ->
                            val selected = emitDocType == code
                            Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (selected) AppColors.Surface else Color.Transparent).clickable { emitDocType = code }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    SettingsInput("e-NCF", emitEncf, { emitEncf = it.uppercase().take(13) })
                    SettingsInput("Total", emitTotal, { emitTotal = it.filter { c -> c.isDigit() || c == '.' }.take(14) })

                    if (emitDocType != "32") {
                        SettingsInput("RNC Comprador", emitBuyerRnc, { emitBuyerRnc = it.take(11) })
                        SettingsInput("Razon Social Comprador", emitBuyerName, { emitBuyerName = it })
                    }

                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                    Text("Consultar estado", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    SettingsInput("ID Documento", emitDocId, { emitDocId = it })

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AlanubeButton("Consultar", Icons.Outlined.Search, loading == "status", Modifier.weight(1f), Color(0xFF7C3AED)) {
                            if (emitDocId.isNotBlank()) {
                                loading = "status"; AlanubeService.init(currentConfig())
                                scope.launch {
                                    result = when (emitDocType) {
                                        "31" -> AlanubeService.checkFiscalInvoiceStatus(emitDocId)
                                        else -> AlanubeService.checkDocumentStatus(emitDocId)
                                    }; expandedResponse = true; loading = ""
                                }
                            }
                        }
                    }

                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AlanubeButton("Cancelar", Icons.Outlined.Close, false, Modifier.weight(1f), AppColors.TextSecondary, light = true) { showEmitModal = false }
                        AlanubeButton("Emitir", Icons.Outlined.Send, loading == "emit", Modifier.weight(1f), Color(0xFF0891B2)) {
                            if (emitEncf.isNotBlank() && emitTotal.isNotBlank()) {
                                loading = "emit"; AlanubeService.init(currentConfig())
                                scope.launch {
                                    val total = emitTotal.toDoubleOrNull() ?: 0.0
                                    result = when (emitDocType) {
                                        "31" -> AlanubeService.emitCreditFiscalInvoice(emitEncf, total, emitBuyerRnc, emitBuyerName)
                                        "32" -> AlanubeService.emitInvoiceConsumption(emitEncf, total)
                                        "45" -> AlanubeService.emitGubernamentalInvoice(emitEncf, total, emitBuyerRnc, emitBuyerName)
                                        "34" -> AlanubeService.emitCreditNote(emitEncf, total, "", emitBuyerRnc, emitBuyerName)
                                        "33" -> AlanubeService.emitDebitNote(emitEncf, total, "", emitBuyerRnc, emitBuyerName)
                                        else -> AlanubeResult(false, "Tipo no soportado")
                                    }; expandedResponse = true; loading = ""
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========== REUSABLE WIDGETS ==========

@Composable
private fun SectionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AppColors.Surface).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight), contentAlignment = Alignment.Center) { Icon(icon, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary); Text(subtitle, fontSize = 12.sp, color = AppColors.TextSecondary) }
        }
        Spacer(Modifier.height(20.dp))
        content()
    }
}

@Composable
private fun SettingsInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, isPassword: Boolean = false) {
    Column(modifier) {
        if (label.isNotEmpty()) { Text(label, fontSize = 13.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium); Spacer(Modifier.height(4.dp)) }
        Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty() && label.isNotEmpty()) Text(label, color = AppColors.Gray, fontSize = 14.sp)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SettingsSwitch(title: String, subtitle: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontSize = 14.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium); Text(subtitle, fontSize = 11.sp, color = AppColors.TextSecondary) }
        Switch(checked = checked, onCheckedChange = onCheck, colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Success, uncheckedTrackColor = AppColors.Border))
    }
}

@Composable
private fun SettingsCheck(title: String, subtitle: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Background).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheck, colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary, checkmarkColor = Color.White))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary); Text(subtitle, fontSize = 11.sp, color = AppColors.TextSecondary) }
    }
}

@Composable
private fun ThemeCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(Modifier.height(80.dp).clip(RoundedCornerShape(14.dp)).background(if (isSelected) AppColors.PrimaryLight else AppColors.Surface).border(2.dp, if (isSelected) AppColors.Primary else AppColors.Border, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(16.dp)) {
        Column { Icon(icon, null, tint = if (isSelected) AppColors.Primary else AppColors.IconGray, modifier = Modifier.size(24.dp)); Spacer(Modifier.height(4.dp)); Text(label, fontSize = 13.sp, color = if (isSelected) AppColors.Primary else AppColors.TextPrimary, fontWeight = FontWeight.Medium) }
    }
}

@Composable
private fun OptionCard(title: String, subtitle: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(if (isSelected) AppColors.SuccessLight else AppColors.Surface).border(1.5f.dp, if (isSelected) Color(0xFF22C55E) else AppColors.Border, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f)); if (isSelected) Icon(Icons.Outlined.Description, null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.height(6.dp)); Text(subtitle, fontSize = 12.sp, color = AppColors.TextSecondary, lineHeight = 18.sp)
    }
}

@Composable
private fun SyncOptionCard(title: String, subtitle: String, checked: Boolean, modifier: Modifier = Modifier, onCheck: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (checked) Color(0xFFF0FDF4) else AppColors.Background).border(if (checked) 1.dp else 0.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp)).clickable { onCheck(!checked) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheck, colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary))
        Column { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(subtitle, fontSize = 11.sp, color = AppColors.TextSecondary) }
    }
}

@Composable
private fun InfoCard(title: String, subtitle: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(14.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary); Text(subtitle, fontSize = 11.sp, color = AppColors.TextSecondary)
        Spacer(Modifier.height(10.dp)); content()
    }
}

@Composable
private fun MarginInput(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, fontSize = 11.sp, color = AppColors.TextSecondary); SettingsInput("", value, onValueChange, Modifier.width(70.dp)) }
}

@Composable
private fun InfoLine(label: String, value: String) { Row { Text("$label: ", fontSize = 12.sp, color = AppColors.TextSecondary); Text(value, fontSize = 12.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium) } }

@Composable
private fun ActionBtn(text: String, bg: Color = AppColors.Background, onClick: () -> Unit = {}) {
    Box(Modifier.height(36.dp).clip(RoundedCornerShape(8.dp)).background(bg).border(if (bg == AppColors.Background) 1.dp else 0.dp, AppColors.Border, RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp), contentAlignment = Alignment.Center) { Text(text, color = if (bg == AppColors.Background) AppColors.TextPrimary else Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun Badge(text: String, bg: Color, fg: Color) { Box(Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) { Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) } }

// msrKeyToChar() ha sido movido a CardReaderHelper.mapKeyToUsChar()
