package com.tmrestaurant.ui.data

import com.tmrestaurant.ui.components.ReturnedItem
import kotlin.math.roundToInt

object RecipeInventoryManager {
    fun applySale(items: List<CartItem>, productState: ProductState) {
        val deltas = mutableMapOf<Int, Int>()

        items.forEach { item ->
            if (item.product.controlInventory) {
                accumulate(deltas, item.product.id, -item.quantity)
            }

            val recipe = RecipeManager.getForProduct(item.product.id) ?: return@forEach
            val saleMultiplier = item.effectiveQuantity / recipe.servings.coerceAtLeast(1).toDouble()
            recipe.ingredients.forEach { ingredient ->
                val ingredientUnits = normalizeUnits(ingredient.quantity * saleMultiplier)
                if (ingredientUnits > 0) {
                    accumulate(deltas, ingredient.productId, -ingredientUnits)
                }
            }
        }

        deltas.forEach { (productId, delta) ->
            if (delta != 0) {
                productState.adjustStock(productId, delta, "Venta con receta")
            }
        }
    }

    fun revertReturn(invoiceItems: List<CartItem>, returnedItems: List<ReturnedItem>, productState: ProductState) {
        val returnedByProduct = returnedItems.groupBy { it.productId }.mapValues { entry ->
            entry.value.sumOf { it.quantity }
        }
        if (returnedByProduct.isEmpty()) return

        val deltas = mutableMapOf<Int, Int>()

        invoiceItems.forEach { soldItem ->
            val returnedQty = returnedByProduct[soldItem.product.id] ?: return@forEach
            if (returnedQty <= 0) return@forEach

            if (soldItem.product.controlInventory) {
                accumulate(deltas, soldItem.product.id, returnedQty)
            }

            val recipe = RecipeManager.getForProduct(soldItem.product.id) ?: return@forEach
            val effectiveReturnQty = if (soldItem.quantity > 0) {
                soldItem.effectiveQuantity * (returnedQty.toDouble() / soldItem.quantity.toDouble())
            } else {
                returnedQty.toDouble()
            }
            val returnMultiplier = effectiveReturnQty / recipe.servings.coerceAtLeast(1).toDouble()
            recipe.ingredients.forEach { ingredient ->
                val ingredientUnits = normalizeUnits(ingredient.quantity * returnMultiplier)
                if (ingredientUnits > 0) {
                    accumulate(deltas, ingredient.productId, ingredientUnits)
                }
            }
        }

        deltas.forEach { (productId, delta) ->
            if (delta != 0) {
                productState.adjustStock(productId, delta, "Devolucion con receta")
            }
        }
    }

    private fun accumulate(deltas: MutableMap<Int, Int>, productId: Int, delta: Int) {
        deltas[productId] = (deltas[productId] ?: 0) + delta
    }

    private fun normalizeUnits(rawUnits: Double): Int {
        if (rawUnits <= 0.0) return 0
        val rounded = rawUnits.roundToInt()
        return if (rounded == 0) 1 else rounded
    }
}
