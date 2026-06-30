package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.*

class ProductState {
    private var initialProducts = AppPersistence.loadProducts()
    var products by mutableStateOf(initialProducts ?: MockData.products)
        private set

    init {
        if (initialProducts == null) AppPersistence.saveProducts(products)
    }

    fun reload() {
        initialProducts = AppPersistence.loadProducts()
        products = initialProducts ?: MockData.products
        invalidatePosCache()
    }

    private val imageCache = mutableMapOf<String, ByteArray>()
    private var cachedPosProducts: List<Product>? = null
    private var cachedProductsRef: List<Product>? = null

    val nextId: Int get() = (products.maxOfOrNull { it.id } ?: 0) + 1

    private var saveJob: Job? = null

    fun add(product: Product) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_PRODUCTO_CREAR", "Acceso denegado para crear productos", level = "WARN")
            return
        }
        products = products + product
        invalidatePosCache()
        scheduleSave()
    }

    fun update(product: Product) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_PRODUCTO_EDITAR", "Acceso denegado para editar productos", level = "WARN")
            return
        }
        products = products.map { if (it.id == product.id) product.copy(updatedAt = System.currentTimeMillis()) else it }
        invalidatePosCache()
        scheduleSave()
    }

    fun delete(id: Int) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_PRODUCTO_ELIMINAR", "Acceso denegado para eliminar productos", level = "WARN")
            return
        }
        products = products.filter { it.id != id }
        invalidatePosCache()
        scheduleSave()
    }

    fun clearAll() {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_PRODUCTO_VACIAR", "Acceso denegado para vaciar productos", level = "WARN")
            return
        }
        products = emptyList()
        invalidatePosCache()
        scheduleSave()
    }

    fun getPosProducts(): List<Product> {
        val current = products
        if (cachedPosProducts == null || cachedProductsRef !== current) {
            cachedPosProducts = current.filter { it.active && it.sellInPos }
            cachedProductsRef = current
        }
        return cachedPosProducts!!
    }

    fun getById(id: Int): Product? = products.find { it.id == id }

    fun adjustStock(productId: Int, delta: Int, reason: String = "") {
        val product = getById(productId) ?: return
        if (!product.controlInventory) return
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser) && reason.contains("manual", ignoreCase = true)) {
            AuditLogManager.log("Seguridad", "DENEGAR_AJUSTE_STOCK", "Acceso denegado para ajustar inventario manualmente", level = "WARN")
            return
        }
        val previousStock = product.stock
        val newStock = (previousStock + delta).coerceAtLeast(0)
        update(product.copy(stock = newStock))
        if (delta != 0) {
            val r = if (reason.isNotBlank()) reason else if (delta > 0) "Compra/Ajuste manual" else "Venta/Ajuste manual"
            InventoryAdjustmentManager.log(productId, product.name, previousStock, newStock, r)
        }
    }

    fun cacheImage(name: String, bytes: ByteArray) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_PRODUCTO_IMAGEN", "Acceso denegado para actualizar imagen de producto", level = "WARN")
            return
        }
        imageCache[name] = bytes
        PersistentFiles.writeBytes("img_$name", bytes)
        saveImageList()
    }

    fun getImageBytes(name: String?): ByteArray? {
        if (name == null) return null
        val cached = imageCache[name]
        if (cached != null) return cached
        val bytes = PersistentFiles.readBytes("img_$name")
        if (bytes != null) {
            if (imageCache.size >= 100) {
                val oldest = imageCache.keys.first()
                imageCache.remove(oldest)
            }
            imageCache[name] = bytes
        }
        return bytes
    }

    private fun invalidatePosCache() {
        cachedPosProducts = null
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(500)
            AppPersistence.saveProducts(products)
        }
    }

    private fun saveImageList() {
        PersistentFiles.writeText("image_list.v1.tsv", imageCache.keys.joinToString("\n"))
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }
}

val categoryNames = listOf("Sin categoría", "COMBOS", "GUARNICIONES", "ENTRADA", "Bebidas", "Extras")

val LocalProductState = staticCompositionLocalOf { ProductState() }
