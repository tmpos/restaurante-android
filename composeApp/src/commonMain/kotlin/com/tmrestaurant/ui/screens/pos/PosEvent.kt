package com.tmrestaurant.ui.screens.pos

import com.tmrestaurant.ui.data.Cliente
import com.tmrestaurant.ui.data.HeldOrder
import com.tmrestaurant.ui.data.Product

sealed interface PosEvent {
    // Carrito
    data class AddProductToCart(val product: Product, val quantity: Int = 1, val extrasCost: Double = 0.0, val extrasNote: String = "", val weightQuantity: Double = 0.0, val courseType: String = "") : PosEvent
    data class RemoveProduct(val productId: Int) : PosEvent
    data class ChangeQuantity(val productId: Int, val newQuantity: Int) : PosEvent
    data object ClearCart : PosEvent
    data class SelectClient(val client: Cliente?) : PosEvent
    data class SetDelivery(val address: String, val phone: String, val notes: String) : PosEvent
    data object ClearDelivery : PosEvent

    // Categoria/Vista
    data class SelectCategory(val index: Int) : PosEvent
    data object ToggleGridView : PosEvent
    data class SetQuickAdd(val enabled: Boolean) : PosEvent

    // Busqueda
    data class UpdateSearchQuery(val query: String) : PosEvent
    data object SearchAndAddByCode : PosEvent
    data object AutoDetectBarcode : PosEvent

    // Scanner
    data class ProcessScannedCode(val code: String) : PosEvent

    // Producto detalle
    data class ProductClick(val product: Product) : PosEvent
    data object DismissProductDetail : PosEvent

    // Venta libre
    data object ShowFreeSaleModal : PosEvent
    data object DismissFreeSaleModal : PosEvent
    data class AddFreeSaleItem(val name: String, val amount: Double) : PosEvent

    // Pago
    data class ShowPaymentModal(
        val discountLabel: String = "",
        val discountAmount: Double = 0.0
    ) : PosEvent
    data object DismissPaymentModal : PosEvent
    data class PaymentComplete(val result: com.tmrestaurant.ui.components.PaymentResult) : PosEvent
    data object StartNewSale : PosEvent
    data class RecallOrder(val order: HeldOrder) : PosEvent

    // Split Bill
    data object ShowSplitBillModal : PosEvent
    data object DismissSplitBillModal : PosEvent
    data class ApplySplitBill(val items: List<com.tmrestaurant.ui.data.CartItem>, val dinerNames: List<String>) : PosEvent

    // Propina
    data class AddPropina(val pctLabel: String, val pctValue: Double) : PosEvent
    data object ClearPropina : PosEvent

}
