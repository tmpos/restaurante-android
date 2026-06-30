package com.tmrestaurant.ui.data

data class Cliente(
    val id: String = "cli_${(10000..99999).random()}",
    val nombre: String = "",
    val rnc: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val tipo: String = "Consumidor Final",
    val limiteCredito: Double = 0.0,
    val loyaltyPoints: Int = 0,
    val totalSpent: Double = 0.0,
    val uid: String = genUid("cli"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
