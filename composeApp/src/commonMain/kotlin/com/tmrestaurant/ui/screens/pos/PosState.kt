package com.tmrestaurant.ui.screens.pos

import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.data.CartItem
import com.tmrestaurant.ui.data.Cliente
import com.tmrestaurant.ui.data.Product

data class PosState(
    val cartItems: List<CartItem> = emptyList(),
    val selectedCategory: Int = 0,
    val isGridView: Boolean = true,
    val quickAddEnabled: Boolean = false,
    val selectedProduct: Product? = null,
    val showFreeSaleModal: Boolean = false,
    val freeSaleSequence: Int = -1,
    val showPaymentModal: Boolean = false,
    val showPaymentSuccess: Boolean = false,
    val paymentResult: PaymentResult? = null,
    val searchQuery: String = "",
    val discountLabel: String = "",
    val discountAmount: Double = 0.0,
    val selectedClient: Cliente? = null,
    val deliveryAddress: String = "",
    val deliveryPhone: String = "",
    val deliveryNotes: String = "",
    val splitBillDinerNames: List<String> = emptyList(),
    val showSplitBillModal: Boolean = false
) {
    private val rawSubtotal get() = cartItems.sumOf { it.product.price * it.effectiveQuantity + it.extrasCost }
    val adjustedSubtotal get() = (rawSubtotal - discountAmount).coerceAtLeast(0.0)
    val subtotalPreTax get() = adjustedSubtotal / 1.18
    val taxAmount get() = adjustedSubtotal - subtotalPreTax
    val total get() = adjustedSubtotal
}
