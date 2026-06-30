package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class WebCheckoutRequest(
    val id: Long,
    val mesaId: Int,
    val mesaName: String,
    val items: List<CartItem>,
    val createdAt: Long = System.currentTimeMillis()
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
    val total: Double get() = items.sumOf { it.product.price * it.effectiveQuantity + it.extrasCost }
}

object WebCheckoutManager {
    var requests by mutableStateOf<List<WebCheckoutRequest>>(emptyList())
        private set

    var selectedRequest by mutableStateOf<WebCheckoutRequest?>(null)
        private set

    @Synchronized
    fun requestCheckout(mesa: Mesa): WebCheckoutRequest {
        val request = WebCheckoutRequest(
            id = System.currentTimeMillis(),
            mesaId = mesa.id,
            mesaName = mesa.name,
            items = mesa.items.map { it.copy() }
        )
        requests = requests.filterNot { it.mesaId == mesa.id } + request
        if (selectedRequest?.mesaId == mesa.id) selectedRequest = request
        return request
    }

    fun select(requestId: Long) {
        selectedRequest = requests.find { it.id == requestId }
    }

    fun cancelForMesa(mesaId: Int) {
        requests = requests.filterNot { it.mesaId == mesaId }
        if (selectedRequest?.mesaId == mesaId) selectedRequest = null
    }

    fun complete(requestId: Long) {
        val request = requests.find { it.id == requestId }
        if (request != null) MesasManager.clearMesa(request.mesaId)
        requests = requests.filterNot { it.id == requestId }
        if (selectedRequest?.id == requestId) selectedRequest = null
    }
}
