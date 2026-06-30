package com.tmrestaurant.ui.screens.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.eloOpenCashDrawer
import com.tmrestaurant.platform.eloPrintTestTicket
import com.tmrestaurant.platform.eloPrintTicketCopies
import com.tmrestaurant.platform.cfdShowCart
import com.tmrestaurant.platform.cfdShowIdle
import com.tmrestaurant.platform.cfdShowProduct
import com.tmrestaurant.platform.printTicketToServer
import com.tmrestaurant.platform.updateCustomerDisplayOnServer
import com.tmrestaurant.platform.TicketPrintRequest
import com.tmrestaurant.platform.TicketCompany
import com.tmrestaurant.platform.TicketCustomer
import com.tmrestaurant.platform.TicketInvoice
import com.tmrestaurant.platform.TicketItem
import com.tmrestaurant.platform.TicketTotals
import com.tmrestaurant.platform.TicketPrintStyle
import com.tmrestaurant.platform.ticketBranding
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.components.*
import com.tmrestaurant.ui.components.QrEndpointsModal
import com.tmrestaurant.ui.data.HeldOrder
import com.tmrestaurant.ui.data.QrGenerator
import com.tmrestaurant.ui.data.HoldOrderManager
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun PosScreen(
    onNavigateToMesas: () -> Unit = {}
) {
    val productState = LocalProductState.current
    val categoryState = LocalCategoryState.current
    val viewModel = remember { PosViewModel(productState, categoryState) }
    val state = viewModel.state
    val onEvent = viewModel::onEvent

    val searchFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var showInvoiceDrawer by remember { mutableStateOf(false) }
    var showClientSelector by remember { mutableStateOf(false) }
    var showRecallModal by remember { mutableStateOf(false) }
    val settingsState = LocalSettingsState.current
    val webCheckout = WebCheckoutManager.selectedRequest
    var rawQuery by remember(state.searchQuery) { mutableStateOf(state.searchQuery) }
    LaunchedEffect(rawQuery) {
        if (rawQuery == state.searchQuery) return@LaunchedEffect
        delay(200)
        onEvent(PosEvent.UpdateSearchQuery(rawQuery))
    }

    LaunchedEffect(webCheckout?.id) {
        webCheckout?.let { viewModel.loadWebCheckout(it) }
    }

    val printInvoice: (com.tmrestaurant.ui.components.PaymentResult) -> Unit = remember(settingsState) { { invoice ->
        val s = settingsState
        val pw = s.settings.print.paperWidthMm
        val receipt = buildReceiptText(invoice, s.settings.company, s.settings.print, pw)
        scope.launch {
            var printed = false
            if (s.settings.server.enabled && s.settings.server.serverUrl.isNotBlank()) {
                val company = s.settings.company
                val invoiceDt = formatDateTime(invoice.timestamp).split(" ")
                val ticketRequest = TicketPrintRequest(
                    company = TicketCompany(name = company.businessName, commercialName = company.businessName, rnc = company.rnc, address = company.address, phone = company.phone),
                    invoice = TicketInvoice(invoiceNumber = invoice.invoiceNumber, ncf = invoice.ncf, date = invoiceDt[0], time = invoiceDt.getOrElse(1) { "" }, cashier = "", paymentMethod = invoice.paymentMethod),
                    customer = TicketCustomer(
                        name = invoice.customerName.ifBlank { "Cliente" },
                        rnc = invoice.customerRnc,
                        phone = invoice.customerPhone
                    ),
                    items = invoice.items.map { item ->
                        val sf = if (invoice.surchargePercent > 0) 1.0 + invoice.surchargePercent / 100.0 else 1.0
                        val itemPrice = item.product.price * sf
                        val itemTotal = itemPrice * item.effectiveQuantity
                        TicketItem(description = itemDisplayName(item), quantity = item.quantity, price = itemPrice, tax = itemTotal - (itemTotal / 1.18), total = itemTotal, code = item.product.code)
                    },
                    totals = TicketTotals(subtotal = invoice.subtotalPreTax, tax = invoice.taxAmount, grandTotal = invoice.total, paidAmount = invoice.receivedAmount, changeAmount = invoice.change),
                    payment = mapOf("method" to invoice.paymentMethod),
                    copies = s.settings.print.copies,
                    openDrawer = invoice.paymentMethod.uppercase().contains("EFECTIVO"),
                    branding = ticketBranding(s.settings.print),
                    note = invoice.note,
                    qrUrl = QrGenerator.dgiiUrl(company.rnc, invoice.ncf, invoice.total)
                )
                val sr = printTicketToServer(s.settings.server.serverUrl, ticketRequest, s.settings.server.apiKey, s.settings.server.apiRoute)
                if (sr.success) { snack.showSnackbar("Imprimiendo ticket en servidor..."); printed = true }
                else snack.showSnackbar("Servidor: ${sr.error}")
            }
            if (!printed) {
                val print = s.settings.print
                val pr = eloPrintTicketCopies(
                    print.selectedPrinter,
                    receipt,
                    pw,
                    print.copies,
                    TicketPrintStyle(
                        textSize = print.textSize,
                        logoWidthMm = print.logoWidthMm,
                        logoHeightMm = print.logoHeightMm,
                        showLogo = print.showCompanyLogo,
                        logoBytes = s.getLogoBytes()
                    )
                )
                if (pr.success) {
                    if (invoice.paymentMethod.uppercase().contains("EFECTIVO")) {
                        eloOpenCashDrawer(s.settings.print.selectedPrinter)
                    }
                    snack.showSnackbar("Imprimiendo recibo en ${s.settings.print.selectedPrinter}...")
                }
                else snack.showSnackbar("Error: ${pr.message}")
            }
        }
    } }

    var previousCart by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    val cartItemCount = remember(state.cartItems) { state.cartItems.sumOf { it.quantity } }
    LaunchedEffect(state.cartItems) {
        val currentCart = state.cartItems
        val changedItem = currentCart.lastOrNull { current ->
            val previous = previousCart.find {
                it.product.id == current.product.id && it.extrasNote == current.extrasNote
            }
            previous == null ||
                previous.quantity != current.quantity ||
                previous.extrasCost != current.extrasCost
        }
        previousCart = currentCart.map { it.copy() }

        suspend fun showDisplay(line1: String, line2: String, fallback: suspend () -> Unit) {
            val server = settingsState.settings.server
            val result = if (server.enabled && server.serverUrl.isNotBlank()) {
                updateCustomerDisplayOnServer(server.serverUrl, line1, line2, server.apiKey, server.apiRoute)
            } else {
                null
            }
            if (result?.success != true) fallback()
        }

        when {
            currentCart.isEmpty() -> {
                val company = settingsState.settings.company
                val businessName = company.businessName.ifBlank { "TMPOS SRL" }
                val phone = company.phone.ifBlank { "829-784-2912" }
                while (true) {
                    showDisplay("BIENVENIDO", businessName) {
                        cfdShowIdle("BIENVENIDO", businessName)
                    }
                    delay(2500)
                    showDisplay(businessName, phone) {
                        cfdShowIdle(businessName, phone)
                    }
                    delay(2500)
                }
            }
            changedItem != null -> {
                val unitPrice = changedItem.product.price +
                    changedItem.extrasCost / changedItem.quantity.coerceAtLeast(1)
                showDisplay(
                    changedItem.product.name,
                    "x${changedItem.effectiveQuantity}  RD$ ${"%.2f".format(unitPrice)}"
                ) {
                    cfdShowProduct(changedItem.product.name, unitPrice, changedItem.quantity)
                }
                delay(1400)
                showDisplay(
                    "$cartItemCount PRODUCTOS",
                    "TOTAL RD$ ${"%.2f".format(state.total)}"
                ) {
                    cfdShowCart(cartItemCount, state.total, state.total)
                }
            }
            else -> showDisplay(
                "$cartItemCount PRODUCTOS",
                "TOTAL RD$ ${"%.2f".format(state.total)}"
            ) {
                cfdShowCart(cartItemCount, state.total, state.total)
            }
        }
    }

    // Install scanner effects
    PosScannerEffect(state = state, onEvent = onEvent, focusRequester = searchFocusRequester)

    // Handle one-shot snackbar messages from ViewModel
    LaunchedEffect(viewModel.pendingSnackbarMessage) {
        val message = viewModel.pendingSnackbarMessage ?: return@LaunchedEffect
        viewModel.pendingSnackbarMessage = null
        snack.showSnackbar(message)
    }

    val visibleCategories = viewModel.getVisibleCategories()
    val posCategories = visibleCategories.map { category ->
        val color = when (category.colorType) {
            CategoryColorType.Orange -> AppColors.Orange
            CategoryColorType.Purple -> AppColors.Primary
            CategoryColorType.Green -> AppColors.Green
            CategoryColorType.Gray -> AppColors.Gray
        }
        val icon = when (category.colorType) {
            CategoryColorType.Orange -> Icons.Outlined.Restaurant
            CategoryColorType.Purple -> Icons.Outlined.Category
            CategoryColorType.Green -> Icons.Outlined.Spa
            CategoryColorType.Gray -> Icons.Outlined.Extension
        }
        POSCategory(category.name, icon, color)
    }
    val posCategoryNames = remember(posCategories) { posCategories.map { it.label } }
    val filteredProducts by remember(productState.products, state.selectedCategory, state.searchQuery, posCategoryNames) {
        derivedStateOf { viewModel.getFilteredProducts(posCategoryNames) }
    }

    Box(Modifier.fillMaxSize()) {
        var showQrModal by remember { mutableStateOf(false) }
        Row(Modifier.fillMaxSize().background(AppColors.Background)) {
            // Sidebar
            Column(
                Modifier.width(150.dp).fillMaxHeight().background(AppColors.Surface).padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Categorías", color = AppColors.TextSecondary, fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 2.dp)
                )
                SidebarCategoryButton(
                    icon = Icons.Outlined.Category,
                    label = "Todas",
                    iconColor = AppColors.Primary,
                    isSelected = state.selectedCategory == 0,
                    onClick = { onEvent(PosEvent.SelectCategory(0)) }
                )
                posCategories.forEachIndexed { index, cat ->
                    SidebarCategoryButton(
                        icon = cat.icon, label = cat.label, iconColor = cat.iconColor,
                        isSelected = state.selectedCategory == index + 1,
                        onClick = { onEvent(PosEvent.SelectCategory(index + 1)) }
                    )
                }
            }

            // Main content
            Column(Modifier.weight(1f).fillMaxHeight()) {
                PosToolbar(
                    isGridView = state.isGridView,
                    quickAddEnabled = state.quickAddEnabled,
                    onToggleQuickAdd = { onEvent(PosEvent.SetQuickAdd(it)) },
                    onFreeSaleClick = { onEvent(PosEvent.ShowFreeSaleModal) },
                    onMesasClick = onNavigateToMesas,
                    onClientsClick = { showClientSelector = true },
                    selectedClientCount = if (state.selectedClient != null) 1 else 0,
                    copies = settingsState.settings.print.copies,
                    onCopiesChange = { settingsState.updatePrintCopies(it) },
                    onToggleView = { onEvent(PosEvent.ToggleGridView) },
                    onInvoiceHistoryClick = { showInvoiceDrawer = true },
                    searchQuery = rawQuery,
                    onSearchChange = { rawQuery = it },
                    onSearchSubmit = {
                        onEvent(PosEvent.UpdateSearchQuery(rawQuery))
                        onEvent(PosEvent.SearchAndAddByCode)
                    },
                    searchFocusRequester = searchFocusRequester
                )
                if (state.isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductCardPOS(
                                product = product,
                                onClick = { onEvent(PosEvent.ProductClick(product)) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        lazyItems(filteredProducts, key = { it.id }) { product ->
                            ProductCardPOS(
                                product = product,
                                onClick = { onEvent(PosEvent.ProductClick(product)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Cart panel
            CartPanel(
                cartItems = state.cartItems,
                onRemoveProduct = { itemToRemove -> onEvent(PosEvent.RemoveProduct(itemToRemove.product.id)) },
                onQuantityChange = { item, newQty -> onEvent(PosEvent.ChangeQuantity(item.product.id, newQty)) },
                onClearCart = { onEvent(PosEvent.ClearCart) },
                onCheckout = { discountLabel, discountAmount ->
                    onEvent(PosEvent.ShowPaymentModal(discountLabel, discountAmount))
                },
                onSendToKitchen = {
                    if (state.cartItems.isNotEmpty()) {
                        ComandasManager.enviarACocina(state.cartItems, "General")
                        onEvent(PosEvent.ClearCart)
                    }
                },
                onAddTip = { label, pct -> onEvent(PosEvent.AddPropina(label, pct)) },
                onClearTip = { onEvent(PosEvent.ClearPropina) },
                selectedClient = state.selectedClient,
                onSelectClient = { showClientSelector = true },
                onClearClient = { onEvent(PosEvent.SelectClient(null)) },
                deliveryAddress = state.deliveryAddress,
                deliveryPhone = state.deliveryPhone,
                deliveryNotes = state.deliveryNotes,
                onSetDelivery = { address, phone, notes -> onEvent(PosEvent.SetDelivery(address, phone, notes)) },
                onClearDelivery = { onEvent(PosEvent.ClearDelivery) },
                onSplitBill = { onEvent(PosEvent.ShowSplitBillModal) },
                splitBillActive = state.splitBillDinerNames.isNotEmpty(),
                onOpenDrawer = {
                    scope.launch {
                        eloOpenCashDrawer(settingsState.settings.print.selectedPrinter)
                    }
                },
                onHoldOrder = {
                    if (state.cartItems.isNotEmpty()) {
                        val held = com.tmrestaurant.ui.data.HeldOrder(
                            id = "HOLD-${System.currentTimeMillis().toString().takeLast(8)}",
                            label = "${state.cartItems.size} items - RD\$ ${"%.0f".format(state.total)}",
                            items = state.cartItems,
                            discountLabel = state.discountLabel,
                            discountAmount = state.discountAmount,
                            clientId = state.selectedClient?.id ?: "",
                            clientName = state.selectedClient?.nombre ?: ""
                        )
                        com.tmrestaurant.ui.data.HoldOrderManager.hold(held)
                        onEvent(PosEvent.ClearCart)
                    }
                },
                onRecallOrders = { showRecallModal = true },
                modifier = Modifier.width(420.dp).fillMaxHeight()
            )
        }
        SnackbarHost(snack, modifier = Modifier.align(Alignment.BottomCenter))
        InvoiceDrawer(visible = showInvoiceDrawer, onDismiss = { showInvoiceDrawer = false }, onReprint = printInvoice)

        // QR Button
        Box(Modifier.align(Alignment.BottomEnd).padding(end = 440.dp, bottom = 20.dp).size(48.dp).clip(RoundedCornerShape(14.dp)).background(AppColors.Surface).clickable { showQrModal = true }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.QrCode, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
        }

        if (showQrModal) {
            QrEndpointsModal(onDismiss = { showQrModal = false })
        }
        if (showClientSelector) {
            ClientSelectorModal(
                selectedClientId = state.selectedClient?.id,
                onDismiss = { showClientSelector = false },
                onSelect = {
                    onEvent(PosEvent.SelectClient(it))
                    showClientSelector = false
                }
            )
        }
    }

    // Modals
    state.selectedProduct?.let { product ->
        ProductDetailModal(
            product = product,
            onDismiss = { onEvent(PosEvent.DismissProductDetail) },
            onAddToCart = { quantity, extrasCost, extrasNote, weightQuantity, courseType ->
                onEvent(PosEvent.AddProductToCart(product, quantity, extrasCost, extrasNote, weightQuantity, courseType))
                onEvent(PosEvent.DismissProductDetail)
            }
        )
    }

    if (state.showFreeSaleModal) {
        FreeSaleModal(
            onDismiss = { onEvent(PosEvent.DismissFreeSaleModal) },
            onAdd = { name, amount -> onEvent(PosEvent.AddFreeSaleItem(name, amount)) }
        )
    }

    if (state.showSplitBillModal) {
        SplitBillModal(
            cartItems = state.cartItems.filter { it.product.code != "PROPINA-LEY" },
            onApply = { items, dinerNames ->
                val withPropina = items + state.cartItems.filter { it.product.code == "PROPINA-LEY" }
                onEvent(PosEvent.ApplySplitBill(withPropina, dinerNames))
            },
            onDismiss = { onEvent(PosEvent.DismissSplitBillModal) }
        )
    }

    if (state.showPaymentModal) {
        val fiscalType = if (state.selectedClient?.rnc?.isNotBlank() == true) {
            com.tmrestaurant.ui.data.FiscalDocumentType.CREDITO_FISCAL
        } else {
            com.tmrestaurant.ui.data.FiscalDocumentType.CONSUMIDOR_FINAL
        }
        val ncf = remember(fiscalType, state.selectedClient?.id) {
            com.tmrestaurant.ui.data.NcfManager.getNextNcf(fiscalType)
        }
        PaymentModal(
            cartItems = state.cartItems,
            subtotalPreTax = state.subtotalPreTax,
            taxAmount = state.taxAmount,
            total = state.total,
            ncf = ncf,
            paymentMethods = settingsState.settings.paymentMethods.methods,
            discountLabel = state.discountLabel,
            discountAmount = state.discountAmount,
            dinerNames = state.splitBillDinerNames,
            onDismiss = { onEvent(PosEvent.DismissPaymentModal) },
            onPaymentComplete = { result ->
                val client = state.selectedClient
                onEvent(
                    PosEvent.PaymentComplete(
                        result.copy(
                            ncf = ncf,
                            turnoId = TurnoManager.currentTurno?.id ?: "",
                            customerId = client?.id.orEmpty(),
                            customerName = client?.nombre.orEmpty(),
                            customerRnc = client?.rnc.orEmpty(),
                            customerPhone = client?.telefono.orEmpty(),
                            deliveryAddress = state.deliveryAddress,
                            deliveryPhone = state.deliveryPhone,
                            deliveryNotes = state.deliveryNotes,
                            deliveryStatus = if (state.deliveryAddress.isNotBlank() || state.deliveryPhone.isNotBlank()) "PENDIENTE" else ""
                        )
                    )
                )
            }
        )
    }

    state.paymentResult?.let { result ->
        if (state.showPaymentSuccess) {
            PaymentSuccessModal(
                invoiceNumber = result.invoiceNumber,
                ncf = result.ncf,
                total = result.total,
                printerName = settingsState.settings.print.selectedPrinter,
                onPrint = { printInvoice(result) },
                onNewSale = { onEvent(PosEvent.StartNewSale) }
            )
        }
    }

    if (showRecallModal) {
        RecallOrdersModal(
            heldOrders = HoldOrderManager.heldOrders.toList(),
            onRecall = { order ->
                onEvent(PosEvent.RecallOrder(order))
                showRecallModal = false
            },
            onDelete = { idx ->
                HoldOrderManager.removeAt(idx)
            },
            onDismiss = { showRecallModal = false }
        )
    }
}

private fun itemDisplayName(item: CartItem): String =
    if (item.extrasNote.isBlank()) item.product.name else "${item.product.name} - ${item.extrasNote}"
