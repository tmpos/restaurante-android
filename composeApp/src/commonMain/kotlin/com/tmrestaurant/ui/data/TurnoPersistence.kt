package com.tmrestaurant.ui.data

import com.tmrestaurant.db.DatabaseManager

private const val FILE_NAME = "turno_state.v1.props"

private fun esc(value: String): String =
    value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

private fun descline(line: String): List<String> {
    val result = mutableListOf<String>()
    val cur = StringBuilder()
    var escaping = false
    for (ch in line) {
        if (escaping) {
            cur.append(when (ch) { 'n' -> '\n'; 't' -> '\t'; 'r' -> '\r'; else -> ch })
            escaping = false
        } else when (ch) {
            '\\' -> escaping = true
            '\t' -> { result.add(cur.toString()); cur.clear() }
            else -> cur.append(ch)
        }
    }
    result.add(cur.toString())
    return result
}

object TurnoPersistence {

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("turnos")
    } catch (_: Exception) { false }

    fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("turnos")
                    DatabaseManager.deleteAll("gastos")
                    DatabaseManager.deleteAll("caja_movimientos")
                    DatabaseManager.deleteAll("mesas")
                    TurnoManager.activeTurnos.forEach { (userId, t) ->
                        DatabaseManager.insert("turnos", mapOf(
                            "id" to t.id,
                            "user_id" to t.userId,
                            "user_name" to t.userName,
                            "initial_amount" to t.initialAmount,
                            "start_time" to t.startTime,
                            "end_time" to null,
                            "is_closed" to 0
                        ))
                        val turnoGastos = TurnoManager.gastosMap[userId] ?: emptyList()
                        turnoGastos.forEach { g ->
                            DatabaseManager.insert("gastos", mapOf(
                                "id" to g.id,
                                "turno_id" to t.id,
                                "description" to g.description,
                                "amount" to g.amount,
                                "user_id" to g.userId,
                                "user_name" to g.userName,
                                "created_at" to g.createdAt
                            ))
                        }
                        val turnoMovimientos = TurnoManager.movimientosMap[userId] ?: emptyList()
                        turnoMovimientos.forEach { m ->
                            DatabaseManager.insert("caja_movimientos", mapOf(
                                "id" to m.id,
                                "turno_id" to t.id,
                                "tipo" to m.tipo.name,
                                "description" to m.description,
                                "amount" to m.amount,
                                "user_id" to m.userId,
                                "user_name" to m.userName,
                                "created_at" to m.createdAt
                            ))
                        }
                        val turnoMesas = MesasManager.mesasForTurno(t.id)
                        if (turnoMesas != null) {
                            turnoMesas.forEach { mesa ->
                                DatabaseManager.insert("mesas", mapOf(
                                    "id" to mesa.id,
                                    "turno_id" to t.id,
                                    "name" to mesa.name,
                                    "is_occupied" to (if (mesa.isOccupied) 1 else 0),
                                    "opened_at" to mesa.openedAt,
                                    "x_pos" to mesa.xPos.toDouble(),
                                    "y_pos" to mesa.yPos.toDouble(),
                                    "shape" to mesa.shape,
                                    "table_width" to mesa.tableWidth,
                                    "table_height" to mesa.tableHeight,
                                    "waiter_name" to mesa.waiterName
                                ))
                            }
                        }
                    }
                    TurnoManager.closedTurnos.forEach { t ->
                        DatabaseManager.insert("turnos", mapOf(
                            "id" to t.id,
                            "user_id" to t.userId,
                            "user_name" to t.userName,
                            "initial_amount" to t.initialAmount,
                            "start_time" to t.startTime,
                            "end_time" to t.endTime,
                            "is_closed" to (if (t.isClosed) 1 else 0)
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = mutableListOf<String>()

        TurnoManager.activeTurnos.forEach { (userId, t) ->
            lines.add(listOf("TURNO", t.id, t.userId, t.userName, t.initialAmount.toString(), t.startTime.toString()).joinToString("\t") { esc(it) })
            val turnoGastos = TurnoManager.gastosMap[userId] ?: emptyList()
            turnoGastos.forEach { g ->
                lines.add(listOf("GASTO", g.id, g.description, g.amount.toString(), g.userId, g.userName, g.createdAt.toString()).joinToString("\t") { esc(it) })
            }
            val turnoMovimientos = TurnoManager.movimientosMap[userId] ?: emptyList()
            turnoMovimientos.forEach { m ->
                lines.add(listOf("MOVIMIENTO", m.id, m.tipo.name, m.description, m.amount.toString(), m.userId, m.userName, m.createdAt.toString()).joinToString("\t") { esc(it) })
            }
            val turnoMesas = MesasManager.mesasForTurno(t.id)
            if (turnoMesas != null) {
                turnoMesas.forEach { mesa ->
                    lines.add(listOf("MESA", t.id, mesa.id.toString(), mesa.name, mesa.isOccupied.toString(), mesa.openedAt.toString(), mesa.shape, mesa.xPos.toString(), mesa.yPos.toString(), mesa.tableWidth.toString(), mesa.tableHeight.toString(), mesa.waiterName).joinToString("\t") { esc(it) })
                    mesa.items.forEach { item ->
                        val p = item.product
                        lines.add(listOf("ITEM", t.id, mesa.id.toString(), p.id.toString(), p.name, p.price.toString(), p.category, p.taxPercent.toString(), item.quantity.toString(), item.extrasCost.toString(), item.extrasNote, item.courseType).joinToString("\t") { esc(it) })
                    }
                }
            }
        }

        TurnoManager.closedTurnos.forEach { t ->
            lines.add(listOf("CLOSED", t.id, t.userId, t.userName, t.initialAmount.toString(), t.startTime.toString(), t.endTime?.toString() ?: "", t.isClosed.toString()).joinToString("\t") { esc(it) })
        }

        PersistentFiles.writeText(FILE_NAME, lines.joinToString("\n"))
    }

    fun load() {
        if (isDbReady()) {
            try {
                val turnoRows = DatabaseManager.query("turnos") { it }
                val gastoRows = DatabaseManager.query("gastos") { it }
                val movRows = DatabaseManager.query("caja_movimientos") { it }
                val mesaRows = DatabaseManager.query("mesas") { it }
                if (turnoRows.isNotEmpty() || gastoRows.isNotEmpty() || movRows.isNotEmpty() || mesaRows.isNotEmpty()) {
                    val loadedGastosByUser = mutableMapOf<String, MutableList<Gasto>>()
                    val loadedMovimientosByUser = mutableMapOf<String, MutableList<CajaMovimiento>>()
                    val loadedClosed = mutableListOf<Turno>()
                    val pendingMesas = mutableMapOf<String, MutableMap<Int, Mesa>>()

                    gastoRows.forEach { row ->
                        val gasto = Gasto(
                            id = row["id"] as? String ?: "",
                            description = row["description"] as? String ?: "",
                            amount = (row["amount"] as? Double) ?: ((row["amount"] as? Long)?.toDouble() ?: 0.0),
                            userId = row["user_id"] as? String ?: "",
                            userName = row["user_name"] as? String ?: "",
                            createdAt = (row["created_at"] as? Long) ?: 0L
                        )
                        loadedGastosByUser.getOrPut(gasto.userId) { mutableListOf() }.add(gasto)
                    }
                    movRows.forEach { row ->
                        val movimiento = CajaMovimiento(
                            id = row["id"] as? String ?: "",
                            tipo = CajaMovimientoTipo.entries.firstOrNull { it.name == (row["tipo"] as? String) } ?: CajaMovimientoTipo.ENTRADA,
                            description = row["description"] as? String ?: "",
                            amount = (row["amount"] as? Double) ?: ((row["amount"] as? Long)?.toDouble() ?: 0.0),
                            userId = row["user_id"] as? String ?: "",
                            userName = row["user_name"] as? String ?: "",
                            createdAt = (row["created_at"] as? Long) ?: 0L
                        )
                        loadedMovimientosByUser.getOrPut(movimiento.userId) { mutableListOf() }.add(movimiento)
                    }
                    mesaRows.forEach { row ->
                        val turnoId = row["turno_id"] as? String ?: return@forEach
                        val mesaId = ((row["id"] as? Long) ?: return@forEach).toInt()
                        val mesa = Mesa(
                            id = mesaId,
                            name = row["name"] as? String ?: "",
                            isOccupied = ((row["is_occupied"] as? Long) ?: 0L) == 1L,
                            openedAt = (row["opened_at"] as? Long) ?: 0L,
                            shape = row["shape"] as? String ?: "rectangle",
                            xPos = ((row["x_pos"] as? Double) ?: -1.0).toFloat(),
                            yPos = ((row["y_pos"] as? Double) ?: -1.0).toFloat(),
                            tableWidth = ((row["table_width"] as? Long) ?: 120L).toInt(),
                            tableHeight = ((row["table_height"] as? Long) ?: 80L).toInt(),
                            waiterName = row["waiter_name"] as? String ?: ""
                        )
                        pendingMesas.getOrPut(turnoId) { mutableMapOf() }[mesaId] = mesa
                    }

                    turnoRows.forEach { row ->
                        val turno = Turno(
                            id = row["id"] as? String ?: "",
                            userId = row["user_id"] as? String ?: "",
                            userName = row["user_name"] as? String ?: "",
                            initialAmount = (row["initial_amount"] as? Double) ?: ((row["initial_amount"] as? Long)?.toDouble() ?: 0.0),
                            startTime = (row["start_time"] as? Long) ?: 0L,
                            endTime = row["end_time"] as? Long,
                            isClosed = ((row["is_closed"] as? Long) ?: 0L) == 1L
                        )
                        if (turno.isClosed) {
                            loadedClosed.add(turno)
                        } else {
                            TurnoManager.restoreActiveTurno(turno)
                        }
                    }

                    pendingMesas.forEach { (turnoId, mesasMap) ->
                        MesasManager.restoreMesasForTurno(turnoId, mesasMap.values.toList())
                    }
                    loadedGastosByUser.forEach { (userId, gastos) ->
                        TurnoManager.restoreGastosForUser(userId, gastos)
                    }
                    loadedMovimientosByUser.forEach { (userId, movimientos) ->
                        TurnoManager.restoreMovimientosForUser(userId, movimientos)
                    }
                    TurnoManager.restoreClosedTurnos(loadedClosed)
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE_NAME) ?: return
        if (text.isBlank()) return

        val loadedGastosByUser = mutableMapOf<String, MutableList<Gasto>>()
        val loadedMovimientosByUser = mutableMapOf<String, MutableList<CajaMovimiento>>()
        val loadedClosed = mutableListOf<Turno>()
        val pendingMesas = mutableMapOf<String, MutableMap<Int, Mesa>>()

        for (line in text.lines()) {
            if (line.isBlank()) continue
            val f = descline(line)
            if (f.isEmpty()) continue
            when (f[0]) {
                "TURNO" -> {
                    if (f.size >= 6) {
                        val turno = Turno(
                            id = f[1],
                            userId = f[2],
                            userName = f[3],
                            initialAmount = f[4].toDoubleOrNull() ?: 0.0,
                            startTime = f[5].toLongOrNull() ?: 0L
                        )
                        TurnoManager.restoreActiveTurno(turno)
                    }
                }
                "GASTO" -> {
                    if (f.size >= 7) {
                        val gasto = Gasto(
                            id = f[1],
                            description = f[2],
                            amount = f[3].toDoubleOrNull() ?: 0.0,
                            userId = f[4],
                            userName = f[5],
                            createdAt = f[6].toLongOrNull() ?: 0L
                        )
                        loadedGastosByUser.getOrPut(gasto.userId) { mutableListOf() }.add(gasto)
                    }
                }
                "MOVIMIENTO" -> {
                    if (f.size >= 8) {
                        val movimiento = CajaMovimiento(
                            id = f[1],
                            tipo = CajaMovimientoTipo.entries.firstOrNull { it.name == f[2] } ?: CajaMovimientoTipo.ENTRADA,
                            description = f[3],
                            amount = f[4].toDoubleOrNull() ?: 0.0,
                            userId = f[5],
                            userName = f[6],
                            createdAt = f[7].toLongOrNull() ?: 0L
                        )
                        loadedMovimientosByUser.getOrPut(movimiento.userId) { mutableListOf() }.add(movimiento)
                    }
                }
                "CLOSED" -> {
                    if (f.size >= 8) {
                        loadedClosed.add(Turno(
                            id = f[1],
                            userId = f[2],
                            userName = f[3],
                            initialAmount = f[4].toDoubleOrNull() ?: 0.0,
                            startTime = f[5].toLongOrNull() ?: 0L,
                            endTime = f[6].toLongOrNull(),
                            isClosed = f[7].toBooleanStrictOrNull() ?: true
                        ))
                    }
                }
                "MESA" -> {
                    if (f.size >= 6) {
                        val turnoId = f[1]
                        val mesaId = f[2].toIntOrNull() ?: continue
                        val mesa = Mesa(
                            id = mesaId,
                            name = f[3],
                            isOccupied = f[4].toBooleanStrictOrNull() ?: false,
                            openedAt = f[5].toLongOrNull() ?: 0L,
                            shape = f.getOrElse(6) { "rectangle" },
                            xPos = f.getOrElse(7) { "-1" }.toFloatOrNull() ?: -1f,
                            yPos = f.getOrElse(8) { "-1" }.toFloatOrNull() ?: -1f,
                            tableWidth = f.getOrElse(9) { "120" }.toIntOrNull() ?: 120,
                            tableHeight = f.getOrElse(10) { "80" }.toIntOrNull() ?: 80,
                            waiterName = f.getOrElse(11) { "" }
                        )
                        pendingMesas.getOrPut(turnoId) { mutableMapOf() }[mesaId] = mesa
                    }
                }
                "ITEM" -> {
                    if (f.size >= 9) {
                        val turnoId = f[1]
                        val mesaId = f[2].toIntOrNull() ?: continue
                        val product = Product(
                            id = f[3].toIntOrNull() ?: continue,
                            name = f[4],
                            price = f[5].toDoubleOrNull() ?: 0.0,
                            category = f[6],
                            taxPercent = f[7].toDoubleOrNull() ?: 18.0
                        )
                        val quantity = f[8].toIntOrNull() ?: 1
                        val extrasCost = f.getOrElse(9) { "0" }.toDoubleOrNull() ?: 0.0
                        val extrasNote = f.getOrElse(10) { "" }
                        val courseType = f.getOrElse(11) { "" }
                        val mesa = pendingMesas[turnoId]?.get(mesaId) ?: continue
                        val updatedItems = mesa.items + CartItem(product, quantity, extrasCost, extrasNote, courseType = courseType)
                        pendingMesas[turnoId]?.put(mesaId, mesa.copy(items = updatedItems))
                    }
                }
            }
        }

        pendingMesas.forEach { (turnoId, mesasMap) ->
            MesasManager.restoreMesasForTurno(turnoId, mesasMap.values.toList())
        }
        loadedGastosByUser.forEach { (userId, gastos) ->
            TurnoManager.restoreGastosForUser(userId, gastos)
        }
        loadedMovimientosByUser.forEach { (userId, movimientos) ->
            TurnoManager.restoreMovimientosForUser(userId, movimientos)
        }
        TurnoManager.restoreClosedTurnos(loadedClosed)
        save()
    }
}
