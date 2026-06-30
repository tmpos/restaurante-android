package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object TurnoManager {
    var currentUser by mutableStateOf<User?>(null)
        private set
    var activeTurnos by mutableStateOf<Map<String, Turno>>(emptyMap())
        private set
    var closedTurnos = mutableListOf<Turno>()
    internal var gastosMap by mutableStateOf<Map<String, MutableList<Gasto>>>(emptyMap())
    internal var movimientosMap by mutableStateOf<Map<String, MutableList<CajaMovimiento>>>(emptyMap())

    val currentTurno: Turno? get() = currentUser?.let { activeTurnos[it.id] }
    val gastos: List<Gasto> get() = currentUser?.let { gastosMap[it.id]?.toList() } ?: emptyList()
    val movimientos: List<CajaMovimiento> get() = currentUser?.let { movimientosMap[it.id]?.toList() } ?: emptyList()

    init {
        TurnoPersistence.load()
    }

    fun activeTurnoForUser(userId: String): Turno? = activeTurnos[userId]

    fun login(user: User) {
        currentUser = user
        AuditLogManager.log("Sesion", "LOGIN", "Inicio de sesion de ${user.id}")
    }

    fun updateCurrentUser(user: User) {
        if (currentUser?.id == user.id) {
            currentUser = user
        }
    }

    fun openTurno(initialAmount: Double) {
        val user = currentUser ?: return
        if (!AccessControl.canOperateCash(user)) {
            AuditLogManager.log("Seguridad", "DENEGAR_APERTURA_TURNO", "Acceso denegado para abrir turno", level = "WARN")
            return
        }
        val turno = Turno(
            userId = user.id,
            userName = user.name,
            initialAmount = initialAmount
        )
        activeTurnos = activeTurnos + (user.id to turno)
        gastosMap = gastosMap + (user.id to mutableListOf())
        movimientosMap = movimientosMap + (user.id to mutableListOf())
        TurnoPersistence.save()
        AuditLogManager.log("Caja", "APERTURA_TURNO", "Turno ${turno.id} abierto con RD\$ ${"%,.2f".format(initialAmount)}")
    }

    fun addGasto(description: String, amount: Double) {
        val user = currentUser ?: return
        if (!AccessControl.canOperateCash(user)) {
            AuditLogManager.log("Seguridad", "DENEGAR_GASTO_CAJA", "Acceso denegado para registrar gasto", level = "WARN")
            return
        }
        val gasto = Gasto(
            description = description,
            amount = amount,
            userId = user.id,
            userName = user.name
        )
        val list = gastosMap[user.id] ?: mutableListOf()
        list.add(gasto)
        gastosMap = gastosMap + (user.id to list)
        TurnoPersistence.save()
        AuditLogManager.log("Caja", "REGISTRAR_GASTO", "${description.ifBlank { "Sin descripcion" }} - RD\$ ${"%,.2f".format(amount)}", level = "WARN")
    }

    fun addMovimientoCaja(tipo: CajaMovimientoTipo, description: String, amount: Double) {
        val user = currentUser ?: return
        if (!AccessControl.canOperateCash(user)) {
            AuditLogManager.log("Seguridad", "DENEGAR_MOVIMIENTO_CAJA", "Acceso denegado para registrar movimiento de caja", level = "WARN")
            return
        }
        val movimiento = CajaMovimiento(
            tipo = tipo,
            description = description,
            amount = amount,
            userId = user.id,
            userName = user.name
        )
        val list = movimientosMap[user.id] ?: mutableListOf()
        list.add(movimiento)
        movimientosMap = movimientosMap + (user.id to list)
        TurnoPersistence.save()
        AuditLogManager.log("Caja", "MOVIMIENTO_CAJA", "${tipo.name} - ${description.ifBlank { "Sin descripcion" }} - RD\$ ${"%,.2f".format(amount)}")
    }

    fun closeTurno(): Turno? {
        val user = currentUser ?: return null
        if (!AccessControl.canOperateCash(user)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CIERRE_TURNO", "Acceso denegado para cerrar turno", level = "WARN")
            return null
        }
        val turno = activeTurnos[user.id] ?: return null
        val closed = turno.copy(endTime = System.currentTimeMillis(), isClosed = true)
        closedTurnos.add(closed)
        activeTurnos = activeTurnos - user.id
        gastosMap = gastosMap - user.id
        movimientosMap = movimientosMap - user.id
        MesasManager.clearAllForTurno(turno.id)
        ComandasManager.clearTurnoComandas(turno.id)
        TurnoPersistence.save()
        BackupManager.createAutomaticBackup("cierre-turno")
        AuditLogManager.log("Caja", "CIERRE_TURNO", "Turno ${turno.id} cerrado")
        return closed
    }

    fun closeTurnoForUser(userId: String): Turno? {
        if (!isAdmin()) {
            AuditLogManager.log("Seguridad", "DENEGAR_CIERRE_TURNO_FORZADO", "Acceso denegado para cerrar turno de otro usuario", level = "WARN")
            return null
        }
        val turno = activeTurnos[userId] ?: return null
        val closed = turno.copy(endTime = System.currentTimeMillis(), isClosed = true)
        closedTurnos.add(closed)
        activeTurnos = activeTurnos - userId
        gastosMap = gastosMap - userId
        movimientosMap = movimientosMap - userId
        MesasManager.clearAllForTurno(turno.id)
        ComandasManager.clearTurnoComandas(turno.id)
        TurnoPersistence.save()
        BackupManager.createAutomaticBackup("cierre-turno-forzado")
        AuditLogManager.log("Caja", "CIERRE_TURNO_FORZADO", "Turno ${turno.id} cerrado para usuario $userId", level = "WARN")
        return closed
    }

    fun isLoggedIn(): Boolean = currentUser != null
    fun isAdmin(): Boolean = currentUser?.role == UserRole.ADMIN
    fun hasActiveTurno(): Boolean {
        val user = currentUser ?: return false
        val turno = activeTurnos[user.id] ?: return false
        return !turno.isClosed
    }

    fun getCorte(invoices: List<com.tmrestaurant.ui.components.PaymentResult>, turno: Turno): Corte {
        val turnoInvoices = invoices.filter { it.turnoId == turno.id }
        val totalVentas = turnoInvoices.sumOf { it.total }
        val totalEfectivo = turnoInvoices.filter { it.paymentMethod.uppercase().contains("EFECTIVO") }.sumOf { it.total }
        val totalTarjeta = turnoInvoices.filter { it.paymentMethod.uppercase().contains("TARJETA") }.sumOf { it.total }
        val totalTransferencia = turnoInvoices.filter { it.paymentMethod.uppercase().contains("TRANSFERENCIA") }.sumOf { it.total }
        val totalCredito = turnoInvoices.filter { it.paymentMethod.uppercase().contains("CREDITO") }.sumOf { it.total }
        val totalArticulos = turnoInvoices.sumOf { it.items.size }
        val turnoGastos = gastosMap[turno.userId]?.toList() ?: emptyList()
        val totalGastos = turnoGastos.sumOf { it.amount }
        val turnoMovimientos = movimientosMap[turno.userId]?.toList() ?: emptyList()
        val totalEntradas = turnoMovimientos.filter { it.tipo == CajaMovimientoTipo.ENTRADA }.sumOf { it.amount }
        val totalRetiros = turnoMovimientos.filter { it.tipo == CajaMovimientoTipo.RETIRO }.sumOf { it.amount }
        return Corte(
            turno = turno,
            totalVentas = totalVentas,
            totalEfectivo = totalEfectivo,
            totalTarjeta = totalTarjeta,
            totalTransferencia = totalTransferencia,
            totalCredito = totalCredito,
            totalArticulos = totalArticulos,
            invoiceCount = turnoInvoices.size,
            expectedCash = turno.initialAmount + totalEfectivo + totalEntradas - totalGastos - totalRetiros,
            invoices = turnoInvoices,
            gastos = turnoGastos,
            totalGastos = totalGastos,
            movimientos = turnoMovimientos,
            totalEntradas = totalEntradas,
            totalRetiros = totalRetiros
        )
    }

    internal fun restoreActiveTurno(turno: Turno) {
        activeTurnos = activeTurnos + (turno.userId to turno)
    }

    internal fun restoreGastosForUser(userId: String, list: List<Gasto>) {
        gastosMap = gastosMap + (userId to list.toMutableList())
    }

    internal fun restoreMovimientosForUser(userId: String, list: List<CajaMovimiento>) {
        movimientosMap = movimientosMap + (userId to list.toMutableList())
    }

    internal fun restoreClosedTurnos(list: List<Turno>) {
        closedTurnos.clear()
        closedTurnos.addAll(list)
    }
}
