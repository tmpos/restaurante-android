package com.tmrestaurant.ui.screens.pos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.InvoiceHistory

class PosViewModel(
    private val productState: ProductState,
    private val categoryState: CategoryState
) {
    var state by mutableStateOf(PosState())
        private set

    var pendingSnackbarMessage by mutableStateOf<String?>(null)

    private var lastProcessedBarcode = ""
    private var lastProcessedBarcodeAt = 0L
    private var filteredProductsCache: List<Product> = emptyList()
    private var filteredProductsRef: List<Product>? = null
    private var filteredCategoryNames: List<String> = emptyList()
    private var filteredCategoryIndex = -1
    private var filteredQuery = ""
    private var lookupProductsRef: List<Product>? = null
    private var codeLookup: Map<String, Product> = emptyMap()

    fun loadWebCheckout(request: WebCheckoutRequest) {
        state = state.copy(
            cartItems = request.items.map { it.copy() },
            showPaymentModal = false,
            showPaymentSuccess = false,
            paymentResult = null,
            discountLabel = "",
            discountAmount = 0.0
        )
        pendingSnackbarMessage = "${request.mesaName} cargada: ${request.itemCount} productos"
    }

    fun onEvent(event: PosEvent) {
        when (event) {
            // Carrito
            is PosEvent.AddProductToCart -> addProductToCart(event.product, event.quantity, event.extrasCost, event.extrasNote, event.weightQuantity, event.courseType)
            is PosEvent.RemoveProduct -> {
                state = state.copy(cartItems = state.cartItems.filter { it.product.id != event.productId })
            }
            is PosEvent.ChangeQuantity -> {
                state = if (event.newQuantity <= 0) {
                    state.copy(cartItems = state.cartItems.filter { it.product.id != event.productId })
                } else {
                    state.copy(cartItems = state.cartItems.map {
                        if (it.product.id == event.productId) it.copy(quantity = event.newQuantity) else it
                    })
                }
            }
            is PosEvent.ClearCart -> {
                state = state.copy(cartItems = emptyList())
            }
            is PosEvent.SelectClient -> {
                state = state.copy(selectedClient = event.client)
            }
            is PosEvent.SetDelivery -> {
                state = state.copy(deliveryAddress = event.address, deliveryPhone = event.phone, deliveryNotes = event.notes)
            }
            PosEvent.ClearDelivery -> {
                state = state.copy(deliveryAddress = "", deliveryPhone = "", deliveryNotes = "")
            }

            // Categoria/Vista
            is PosEvent.SelectCategory -> {
                state = state.copy(selectedCategory = event.index)
            }
            is PosEvent.ToggleGridView -> {
                state = state.copy(isGridView = !state.isGridView)
            }
            is PosEvent.SetQuickAdd -> {
                state = state.copy(quickAddEnabled = event.enabled)
            }

            // Busqueda
            is PosEvent.UpdateSearchQuery -> {
                state = state.copy(searchQuery = event.query)
            }
            is PosEvent.SearchAndAddByCode -> searchAndAddByCode()
            is PosEvent.AutoDetectBarcode -> autoDetectBarcode()

            // Scanner
            is PosEvent.ProcessScannedCode -> processScannedCode(event.code)

            // Producto detalle
            is PosEvent.ProductClick -> handleProductClick(event.product)
            is PosEvent.DismissProductDetail -> {
                state = state.copy(selectedProduct = null)
            }

            // Venta libre
            is PosEvent.ShowFreeSaleModal -> {
                state = state.copy(showFreeSaleModal = true)
            }
            is PosEvent.DismissFreeSaleModal -> {
                state = state.copy(showFreeSaleModal = false)
            }
            is PosEvent.AddFreeSaleItem -> addFreeSaleItem(event.name, event.amount)

            // Pago
            is PosEvent.ShowPaymentModal -> {
                if (state.cartItems.isNotEmpty()) {
                    state = state.copy(
                        showPaymentModal = true,
                        discountLabel = event.discountLabel,
                        discountAmount = event.discountAmount
                    )
                }
            }
            is PosEvent.DismissPaymentModal -> {
                state = state.copy(showPaymentModal = false)
            }
            is PosEvent.PaymentComplete -> {
                deductStockForSale(event.result.items)
                val saved = InvoiceHistory.add(event.result)
                ClientesManager.registerPurchase(event.result.customerId, event.result.total)
                WebCheckoutManager.selectedRequest?.let { WebCheckoutManager.complete(it.id) }
                state = state.copy(
                    paymentResult = saved,
                    showPaymentModal = false,
                    showPaymentSuccess = true
                )
            }
            is PosEvent.StartNewSale -> {
                state = state.copy(
                    cartItems = emptyList(),
                    showPaymentSuccess = false,
                    paymentResult = null,
                    discountLabel = "",
                    discountAmount = 0.0,
                    selectedClient = null,
                    deliveryAddress = "",
                    deliveryPhone = "",
                    deliveryNotes = "",
                    splitBillDinerNames = emptyList()
                )
            }

            is PosEvent.RecallOrder -> {
                state = state.copy(
                    cartItems = event.order.items,
                    discountLabel = event.order.discountLabel,
                    discountAmount = event.order.discountAmount,
                    selectedClient = if (event.order.clientId.isNotBlank())
                        Cliente(id = event.order.clientId, nombre = event.order.clientName, rnc = "", telefono = "", email = "", direccion = "", tipo = "", limiteCredito = 0.0, uid = "", createdAt = 0, updatedAt = 0)
                    else null
                )
            }

            // Split Bill
            is PosEvent.ShowSplitBillModal -> {
                state = state.copy(showSplitBillModal = true)
            }
            is PosEvent.DismissSplitBillModal -> {
                state = state.copy(showSplitBillModal = false)
            }
            is PosEvent.ApplySplitBill -> {
                state = state.copy(
                    cartItems = event.items,
                    splitBillDinerNames = event.dinerNames,
                    showSplitBillModal = false
                )
            }

            // Propina
            is PosEvent.AddPropina -> addPropina(event.pctLabel, event.pctValue)
            is PosEvent.ClearPropina -> clearPropina()

        }
    }

    fun getVisibleCategories(): List<Category> =
        categoryState.categories.filter { it.visiblePos }

    fun getPosProducts(): List<Product> = productState.getPosProducts()

    fun getFilteredProducts(posCategoryNames: List<String>): List<Product> {
        val posProducts = getPosProducts()
        val query = state.searchQuery.trim()
        if (
            filteredProductsRef === posProducts &&
            filteredCategoryIndex == state.selectedCategory &&
            filteredQuery == query &&
            filteredCategoryNames == posCategoryNames
        ) {
            return filteredProductsCache
        }
        filteredProductsRef = posProducts
        filteredCategoryIndex = state.selectedCategory
        filteredQuery = query
        filteredCategoryNames = posCategoryNames.toList()
        filteredProductsCache = posProducts.filter { product ->
            val matchesCategory = state.selectedCategory < 1 ||
                    state.selectedCategory > posCategoryNames.size ||
                    product.category == posCategoryNames[state.selectedCategory - 1]
            val matchesSearch = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.code.contains(query, ignoreCase = true) ||
                    product.barcode.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        return filteredProductsCache
    }

    private fun addProductToCart(product: Product, quantity: Int = 1, extrasCost: Double = 0.0, extrasNote: String = "", weightQuantity: Double = 0.0, courseType: String = "") {
        if (extrasCost > 0 || extrasNote.isNotBlank() || weightQuantity > 0 || courseType.isNotBlank()) {
            state = state.copy(cartItems = state.cartItems + CartItem(product = product, quantity = quantity, extrasCost = extrasCost, extrasNote = extrasNote, weightQuantity = weightQuantity, courseType = courseType))
            return
        }
        val idx = state.cartItems.indexOfFirst { it.product.id == product.id }
        val newCart = if (idx >= 0) {
            state.cartItems.mapIndexed { i, item ->
                if (i == idx) item.copy(quantity = item.quantity + quantity) else item
            }
        } else {
            state.cartItems + CartItem(product = product, quantity = quantity)
        }
        state = state.copy(cartItems = newCart)
    }

    private fun searchAndAddByCode() {
        val query = state.searchQuery.trim()
        if (query.isEmpty()) return
        val product = findByCodeOrBarcode(query)
        if (product != null) {
            state = state.copy(searchQuery = "")
            if (acceptBarcode(query)) {
                addProductToCart(product)
                pendingSnackbarMessage = "Agregado: ${product.name}"
            }
        } else {
            state = state.copy(searchQuery = "")
            pendingSnackbarMessage = "Producto no encontrado: $query"
        }
    }

    private fun processScannedCode(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return

        val product = findByCodeOrBarcode(trimmed)
        if (product != null) {
            if (!acceptBarcode(trimmed)) return
            addProductToCart(product)
            pendingSnackbarMessage = "Codigo escaneado: $trimmed → ${product.name}"
        } else {
            pendingSnackbarMessage = "Codigo escaneado: $trimmed (producto no encontrado)"
        }
    }

    private fun autoDetectBarcode() {
        val query = state.searchQuery.trim()
        if (query.isBlank()) return
        val product = findByCodeOrBarcode(query)
        if (product != null) {
            state = state.copy(searchQuery = "")
            if (acceptBarcode(query)) {
                addProductToCart(product)
                pendingSnackbarMessage = "Escaneado: ${product.name}"
            }
        }
    }

    private fun acceptBarcode(rawCode: String): Boolean {
        val code = rawCode.trim().uppercase()
        val now = System.currentTimeMillis()
        if (code == lastProcessedBarcode && now - lastProcessedBarcodeAt < BARCODE_DEDUP_WINDOW_MS) {
            return false
        }
        lastProcessedBarcode = code
        lastProcessedBarcodeAt = now
        return true
    }

    private fun findByCodeOrBarcode(raw: String): Product? {
        val products = getPosProducts()
        if (lookupProductsRef !== products) {
            lookupProductsRef = products
            codeLookup = buildMap {
                products.forEach { product ->
                    product.code.trim().uppercase().takeIf { it.isNotBlank() }?.let { put(it, product) }
                    product.barcode.trim().uppercase().takeIf { it.isNotBlank() }?.let { put(it, product) }
                }
            }
        }
        return codeLookup[raw.trim().uppercase()]
    }

    private fun handleProductClick(product: Product) {
        if (state.quickAddEnabled) {
            addProductToCart(product)
        } else {
            state = state.copy(selectedProduct = product)
        }
    }

    private fun addFreeSaleItem(name: String, amount: Double) {
        val product = Product(
            id = state.freeSaleSequence,
            name = name.ifBlank { "Articulo no Registrado" },
            code = "VENTA-LIBRE-${state.freeSaleSequence * -1}",
            category = "Venta libre",
            description = "Articulo agregado por venta libre",
            price = amount,
            stock = 0,
            active = true,
            sellInPos = true,
            sendToKitchen = false,
            controlInventory = false
        )
        state = state.copy(freeSaleSequence = state.freeSaleSequence - 1, showFreeSaleModal = false)
        addProductToCart(product)
    }

    private fun deductStockForSale(items: List<CartItem>) {
        RecipeInventoryManager.applySale(items, productState)
    }

    companion object {
        private const val PROPINA_PRODUCT_ID = -9999
        private const val BARCODE_DEDUP_WINDOW_MS = 1_000L
    }

    private fun addPropina(pctLabel: String, pctValue: Double) {
        clearPropina()
        val baseSubtotal = state.cartItems.sumOf { it.product.price * it.effectiveQuantity }
        val tipAmount = (baseSubtotal * pctValue).coerceAtLeast(0.0)
        val product = Product(
            id = PROPINA_PRODUCT_ID,
            name = "PROPINA DE LEY $pctLabel",
            code = "PROPINA-LEY",
            category = "Propina",
            description = "$pctLabel aplicado sobre RD\$ ${"%.2f".format(baseSubtotal)}",
            price = tipAmount,
            stock = 0,
            active = true,
            sellInPos = true,
            sendToKitchen = false,
            controlInventory = false
        )
        addProductToCart(product)
    }

    private fun clearPropina() {
        state = state.copy(cartItems = state.cartItems.filter { it.product.code != "PROPINA-LEY" })
    }
}
