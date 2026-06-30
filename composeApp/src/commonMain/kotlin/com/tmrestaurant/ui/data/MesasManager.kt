package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Mesa(
    val id: Int,
    val name: String,
    val items: List<CartItem> = emptyList(),
    val isOccupied: Boolean = false,
    val openedAt: Long = 0L,
    val xPos: Float = -1f,
    val yPos: Float = -1f,
    val shape: String = "rectangle",
    val tableWidth: Int = 120,
    val tableHeight: Int = 80,
    val waiterName: String = ""
)

object MesasManager {
    private val defaultMesas = listOf(
        Mesa(1, "Mesa 1"), Mesa(2, "Mesa 2"), Mesa(3, "Mesa 3"),
        Mesa(4, "Mesa 4"), Mesa(5, "Mesa 5"), Mesa(6, "Mesa 6"),
        Mesa(7, "Mesa 7"), Mesa(8, "Mesa 8"), Mesa(9, "Mesa 9"),
        Mesa(10, "Mesa 10")
    )

    private var mesasPorTurno by mutableStateOf<Map<String, List<Mesa>>>(emptyMap())

    private var _mesas by mutableStateOf(defaultMesas)

    val mesas: List<Mesa> get() {
        val turnoId = TurnoManager.currentTurno?.id
        if (turnoId != null) {
            val turnoMesas = mesasPorTurno[turnoId]
            return turnoMesas?.ifEmpty { null } ?: _mesas.ifEmpty { defaultMesas }
        }
        return _mesas.ifEmpty { defaultMesas }
    }

    fun mesasForTurno(turnoId: String): List<Mesa>? = mesasPorTurno[turnoId]

    val nextId: Int get() = (mesas.maxOfOrNull { it.id } ?: 0) + 1

    private fun updateMesas(transform: (List<Mesa>) -> List<Mesa>) {
        val turnoId = TurnoManager.currentTurno?.id
        if (turnoId != null) {
            val current = mesasPorTurno[turnoId] ?: _mesas
            mesasPorTurno = mesasPorTurno + (turnoId to transform(current))
        } else {
            _mesas = transform(_mesas)
        }
        TurnoPersistence.save()
    }

    fun addMesa(name: String, shape: String = "rectangle") {
        updateMesas { mesas ->
            val existingCount = mesas.size
            val cols = 4
            val col = existingCount % cols
            val row = existingCount / cols
            val xPos = 50f + col * 160f
            val yPos = 50f + row * 120f
            mesas + Mesa(nextId, name, shape = shape, xPos = xPos, yPos = yPos)
        }
    }

    fun updateMesaPosition(id: Int, xPos: Float, yPos: Float) {
        updateMesas { list ->
            list.map {
                if (it.id == id) it.copy(xPos = xPos, yPos = yPos)
                else it
            }
        }
    }

    fun removeMesa(id: Int) {
        updateMesas { it.filter { m -> m.id != id } }
    }

    fun occupyMesa(id: Int) {
        updateMesas { list ->
            list.map {
                if (it.id == id) it.copy(isOccupied = true, items = emptyList(), openedAt = System.currentTimeMillis())
                else it
            }
        }
    }

    fun addProductToMesa(mesaId: Int, product: Product, quantity: Int = 1) {
        updateMesas { list ->
            list.map { mesa ->
                if (mesa.id != mesaId) return@map mesa
                val newItems = mesa.items.toMutableList()
                val existing = newItems.indexOfFirst { it.product.id == product.id }
                if (existing >= 0) {
                    newItems[existing] = newItems[existing].copy(quantity = newItems[existing].quantity + quantity)
                } else {
                    newItems.add(CartItem(product, quantity))
                }
                mesa.copy(items = newItems, isOccupied = true)
            }
        }
    }

    fun removeProductFromMesa(mesaId: Int, productId: Int) {
        updateMesas { list ->
            list.map { mesa ->
                if (mesa.id != mesaId) return@map mesa
                val remainingItems = mesa.items.filter { it.product.id != productId }
                mesa.copy(
                    items = remainingItems,
                    isOccupied = remainingItems.isNotEmpty(),
                    openedAt = if (remainingItems.isEmpty()) 0L else mesa.openedAt
                )
            }
        }
    }

    fun updateProductQuantity(mesaId: Int, productId: Int, quantity: Int) {
        updateMesas { list ->
            list.map { mesa ->
                if (mesa.id != mesaId) return@map mesa
                val newItems = mesa.items.toMutableList()
                val idx = newItems.indexOfFirst { it.product.id == productId }
                if (idx >= 0) {
                    newItems[idx] = newItems[idx].copy(quantity = quantity)
                }
                mesa.copy(items = newItems)
            }
        }
    }

    fun updateProductNote(mesaId: Int, productId: Int, note: String) {
        updateMesas { list ->
            list.map { mesa ->
                if (mesa.id != mesaId) return@map mesa
                mesa.copy(
                    items = mesa.items.map { item ->
                        if (item.product.id == productId) item.copy(extrasNote = note.trim()) else item
                    }
                )
            }
        }
    }

    fun clearMesa(id: Int) {
        updateMesas { list ->
            list.map {
                if (it.id == id) it.copy(items = emptyList(), isOccupied = false)
                else it
            }
        }
    }

    fun clearAllForTurno(turnoId: String) {
        mesasPorTurno = mesasPorTurno - turnoId
    }

    fun restoreMesasForTurno(turnoId: String, mesas: List<Mesa>) {
        mesasPorTurno = mesasPorTurno + (turnoId to mesas)
    }

    fun assignWaiter(id: Int, waiterName: String) {
        updateMesas { list ->
            list.map { if (it.id == id) it.copy(waiterName = waiterName) else it }
        }
    }

    fun mergeTables(sourceIds: List<Int>, targetId: Int) {
        updateMesas { list ->
            val target = list.find { it.id == targetId } ?: return@updateMesas list
            val sources = list.filter { it.id in sourceIds && it.id != targetId }
            val combinedItems = (target.items + sources.flatMap { it.items })
                .groupBy { it.product.id }
                .map { (_, items) ->
                    items.reduce { acc, item ->
                        acc.copy(quantity = acc.quantity + item.quantity)
                    }
                }
            list.map { mesa ->
                when {
                    mesa.id == targetId -> mesa.copy(
                        items = combinedItems,
                        isOccupied = combinedItems.isNotEmpty(),
                        openedAt = if (combinedItems.isNotEmpty() && !mesa.isOccupied) System.currentTimeMillis() else mesa.openedAt
                    )
                    mesa.id in sourceIds -> mesa.copy(items = emptyList(), isOccupied = false, openedAt = 0L)
                    else -> mesa
                }
            }
        }
    }

    fun moveItemsToMesa(sourceId: Int, targetId: Int) {
        updateMesas { list ->
            val source = list.find { it.id == sourceId } ?: return@updateMesas list
            list.map { mesa ->
                when (mesa.id) {
                    sourceId -> mesa.copy(items = emptyList(), isOccupied = false, openedAt = 0L)
                    targetId -> mesa.copy(
                        items = mesa.items + source.items,
                        isOccupied = mesa.items.isNotEmpty() || source.items.isNotEmpty(),
                        openedAt = if (source.items.isNotEmpty() && !mesa.isOccupied) System.currentTimeMillis() else mesa.openedAt
                    )
                    else -> mesa
                }
            }
        }
    }
}
