package com.tmrestaurant.ui.data

enum class ComandaStatus { Pendiente, EnPreparacion, Listo }

data class ComandaItem(
    val productName: String,
    val quantity: Int,
    val notes: String = "",
    val courseType: String = ""
)

data class Comanda(
    val id: String = "cmd_${(10000..99999).random()}",
    val mesaName: String = "General",
    val items: List<ComandaItem> = emptyList(),
    val status: ComandaStatus = ComandaStatus.Pendiente,
    val createdAt: Long = System.currentTimeMillis(),
    val turnoId: String = "",
    val area: String = "Cocina",
    val uid: String = genUid("cmd"),
    val updatedAt: Long = System.currentTimeMillis()
)
