package com.tmrestaurant.ui.data

data class User(
    val id: String,
    val name: String,
    val role: UserRole,
    val nivelSeguridad: Int = 1,
    val clave: String = "0000"
)

enum class UserRole(val displayName: String) {
    ADMIN("Administrador"),
    SUPERVISOR("Supervisor"),
    CAJERO("Cajero"),
    CAMARERO("Camarero"),
    COCINA("Cocina")
}

data class Turno(
    val id: String = "T${System.currentTimeMillis()}",
    val userId: String,
    val userName: String,
    val initialAmount: Double,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isClosed: Boolean = false
)

data class Gasto(
    val id: String = "G${System.currentTimeMillis()}",
    val description: String,
    val amount: Double,
    val userId: String,
    val userName: String,
    val createdAt: Long = System.currentTimeMillis()
)

enum class CajaMovimientoTipo { ENTRADA, RETIRO }

data class CajaMovimiento(
    val id: String = "M${System.currentTimeMillis()}",
    val tipo: CajaMovimientoTipo,
    val description: String,
    val amount: Double,
    val userId: String,
    val userName: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Corte(
    val turno: Turno,
    val totalVentas: Double,
    val totalEfectivo: Double,
    val totalTarjeta: Double,
    val totalTransferencia: Double,
    val totalCredito: Double,
    val totalArticulos: Int,
    val invoiceCount: Int,
    val expectedCash: Double,
    val invoices: List<com.tmrestaurant.ui.components.PaymentResult>,
    val gastos: List<Gasto> = emptyList(),
    val totalGastos: Double = 0.0,
    val movimientos: List<CajaMovimiento> = emptyList(),
    val totalEntradas: Double = 0.0,
    val totalRetiros: Double = 0.0
)

val defaultUsers = listOf(
    User(id = "admin", name = "ADMINISTRADOR", role = UserRole.ADMIN, nivelSeguridad = 3, clave = "1234"),
    User(id = "cajero1", name = "CAJERO 1", role = UserRole.CAJERO, nivelSeguridad = 1, clave = "1111"),
    User(id = "cajero2", name = "CAJERO 2", role = UserRole.CAJERO, nivelSeguridad = 2, clave = "2222")
)
