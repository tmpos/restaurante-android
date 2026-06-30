package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager

data class Employee(
    val id: String = "emp_${(10000..99999).random()}",
    val name: String = "",
    val position: String = "",
    val phone: String = "",
    val email: String = "",
    val hireDate: String = "",
    val active: Boolean = true,
    val hourlyRate: Double = 0.0,
    val commissionPercent: Double = 0.0,
    val clockedIn: Boolean = false,
    val lastClockIn: Long = 0L,
    val totalWorkedMinutes: Long = 0L,
    val uid: String = genUid("emp"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object EmployeeManager {
    private const val FILE = "employees.v1.tsv"

    var employees by mutableStateOf<List<Employee>>(emptyList())
        private set

    init {
        load()
    }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("employees")
    } catch (_: Exception) { false }

    fun add(employee: Employee) {
        if (!AccessControl.canManageEmployees(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_EMPLEADO_CREAR", "Acceso denegado para crear empleados", level = "WARN")
            return
        }
        employees = employees + employee.copy(updatedAt = System.currentTimeMillis())
        save()
        AuditLogManager.log("Empleados", "CREAR_EMPLEADO", employee.name)
    }

    fun update(employee: Employee) {
        if (!AccessControl.canManageEmployees(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_EMPLEADO_EDITAR", "Acceso denegado para editar empleados", level = "WARN")
            return
        }
        employees = employees.map { if (it.id == employee.id) employee.copy(updatedAt = System.currentTimeMillis()) else it }
        save()
        AuditLogManager.log("Empleados", "EDITAR_EMPLEADO", employee.name)
    }

    fun delete(id: String) {
        if (!AccessControl.canManageEmployees(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_EMPLEADO_ELIMINAR", "Acceso denegado para eliminar empleados", level = "WARN")
            return
        }
        val employee = employees.firstOrNull { it.id == id }
        employees = employees.filter { it.id != id }
        save()
        AuditLogManager.log("Empleados", "ELIMINAR_EMPLEADO", employee?.name ?: id, level = "WARN")
    }

    fun clockIn(id: String) {
        val employee = employees.firstOrNull { it.id == id } ?: return
        if (employee.clockedIn || !employee.active) return
        employees = employees.map {
            if (it.id == id) it.copy(clockedIn = true, lastClockIn = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()) else it
        }
        save()
        AuditLogManager.log("Empleados", "MARCAR_ENTRADA", employee.name)
    }

    fun clockOut(id: String) {
        val employee = employees.firstOrNull { it.id == id } ?: return
        if (!employee.clockedIn || employee.lastClockIn <= 0L) return
        val minutes = ((System.currentTimeMillis() - employee.lastClockIn) / 60000L).coerceAtLeast(0L)
        employees = employees.map {
            if (it.id == id) {
                it.copy(
                    clockedIn = false,
                    lastClockIn = 0L,
                    totalWorkedMinutes = it.totalWorkedMinutes + minutes,
                    updatedAt = System.currentTimeMillis()
                )
            } else it
        }
        save()
        AuditLogManager.log("Empleados", "MARCAR_SALIDA", "${employee.name} - $minutes min")
    }

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("employees")
                    employees.forEach { e ->
                        DatabaseManager.insert("employees", mapOf(
                            "id" to e.id,
                            "name" to e.name,
                            "position" to e.position,
                            "phone" to e.phone,
                            "email" to e.email,
                            "hire_date" to e.hireDate,
                            "active" to (if (e.active) 1 else 0),
                            "hourly_rate" to e.hourlyRate,
                            "commission_percent" to e.commissionPercent,
                            "clocked_in" to (if (e.clockedIn) 1 else 0),
                            "last_clock_in" to e.lastClockIn,
                            "total_worked_minutes" to e.totalWorkedMinutes,
                            "uid" to e.uid,
                            "created_at" to e.createdAt,
                            "updated_at" to e.updatedAt
                        ))
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = employees.map { e ->
            listOf(
                e.id, e.name, e.position, e.phone, e.email, e.hireDate, e.active.toString(),
                e.hourlyRate.toString(), e.commissionPercent.toString(), e.clockedIn.toString(),
                e.lastClockIn.toString(), e.totalWorkedMinutes.toString(), e.uid,
                e.createdAt.toString(), e.updatedAt.toString()
            ).joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText(FILE, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("employees") { it }
                if (rows.isNotEmpty()) {
                    employees = rows.map { row ->
                        Employee(
                            id = row["id"] as? String ?: "",
                            name = row["name"] as? String ?: "",
                            position = row["position"] as? String ?: "",
                            phone = row["phone"] as? String ?: "",
                            email = row["email"] as? String ?: "",
                            hireDate = row["hire_date"] as? String ?: "",
                            active = (row["active"] as? Long)?.let { it == 1L } ?: true,
                            hourlyRate = (row["hourly_rate"] as? Double) ?: ((row["hourly_rate"] as? Long)?.toDouble() ?: 0.0),
                            commissionPercent = (row["commission_percent"] as? Double) ?: ((row["commission_percent"] as? Long)?.toDouble() ?: 0.0),
                            clockedIn = (row["clocked_in"] as? Long)?.let { it == 1L } ?: false,
                            lastClockIn = (row["last_clock_in"] as? Long) ?: 0L,
                            totalWorkedMinutes = (row["total_worked_minutes"] as? Long) ?: 0L,
                            uid = row["uid"] as? String ?: genUid("emp"),
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis()
                        )
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE) ?: return
        employees = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val f = split(line)
            if (f.size < 6) null else Employee(
                id = f.getOrElse(0) { "" },
                name = f.getOrElse(1) { "" },
                position = f.getOrElse(2) { "" },
                phone = f.getOrElse(3) { "" },
                email = f.getOrElse(4) { "" },
                hireDate = f.getOrElse(5) { "" },
                active = f.getOrElse(6) { "true" }.toBooleanStrictOrNull() ?: true,
                hourlyRate = f.getOrElse(7) { "0" }.toDoubleOrNull() ?: 0.0,
                commissionPercent = f.getOrElse(8) { "0" }.toDoubleOrNull() ?: 0.0,
                clockedIn = f.getOrElse(9) { "false" }.toBooleanStrictOrNull() ?: false,
                lastClockIn = f.getOrElse(10) { "0" }.toLongOrNull() ?: 0L,
                totalWorkedMinutes = f.getOrElse(11) { "0" }.toLongOrNull() ?: 0L,
                uid = f.getOrElse(12) { genUid("emp") },
                createdAt = f.getOrElse(13) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = f.getOrElse(14) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
            )
        }
        if (employees.isNotEmpty()) save()
    }

    private fun esc(value: String): String =
        value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

    private fun split(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var escaping = false
        for (ch in line) {
            if (escaping) {
                current.append(when (ch) { 'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'; else -> ch })
                escaping = false
            } else when (ch) {
                '\\' -> escaping = true
                '\t' -> { result.add(current.toString()); current.clear() }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }
}
