package com.tmrestaurant.ui.data

import com.tmrestaurant.db.DatabaseManager
import com.tmrestaurant.ui.components.Screen

object AccessControl {
    private const val PERMISSIONS_FILE = "role_permissions.v1.tsv"

    enum class Permission(val title: String) {
        MANAGE_USERS("Usuarios"),
        MANAGE_EMPLOYEES("Empleados"),
        MANAGE_INVOICES("Facturas"),
        MANAGE_CATALOG("Catalogo"),
        MANAGE_SETTINGS("Configuracion"),
        OPERATE_CASH("Caja"),
        MANAGE_SUPPLIERS("Proveedores"),
        MANAGE_PURCHASES("Compras"),
        MANAGE_RECIPES("Recetas"),
        MANAGE_BACKUPS("Backups"),
        ACCESS_CLOUD("TM Cloud"),
        MANAGE_CUSTOMERS("Clientes"),
        DELETE_CUSTOMERS("Eliminar clientes"),
        MANAGE_RESERVATIONS("Reservaciones"),
        DELETE_RESERVATIONS("Eliminar reservaciones"),
        MANAGE_CREDIT_ACCOUNTS("Cuentas por cobrar"),
        DELETE_CREDIT_ENTRIES("Eliminar creditos"),
        MANAGE_QUOTES("Cotizaciones"),
        DELETE_QUOTES("Eliminar cotizaciones"),
        FINALIZE_QUOTES("Finalizar cotizaciones")
    }

    private val cashierScreens = setOf(
        Screen.POS,
        Screen.Comandas,
        Screen.Mesas,
        Screen.Clientes,
        Screen.Reservaciones,
        Screen.CuentasCobrar,
        Screen.Cotizaciones,
        Screen.Delivery,
        Screen.Caja,
        Screen.ControlCaja,
        Screen.Facturas
    )

    private val supervisorScreens = cashierScreens + setOf(
        Screen.Dashboard,
        Screen.Reportes,
        Screen.Turnos,
        Screen.Inventario
    )

    private val waiterScreens = setOf(
        Screen.POS,
        Screen.Comandas,
        Screen.Mesas,
        Screen.Clientes,
        Screen.Reservaciones,
        Screen.Cotizaciones,
        Screen.Delivery
    )

    private val kitchenScreens = setOf(Screen.Comandas)

    private val defaultRolePermissions = mapOf(
        UserRole.ADMIN to Permission.entries.toSet(),
        UserRole.SUPERVISOR to setOf(
            Permission.MANAGE_INVOICES,
            Permission.OPERATE_CASH,
            Permission.MANAGE_CUSTOMERS,
            Permission.DELETE_CUSTOMERS,
            Permission.MANAGE_RESERVATIONS,
            Permission.DELETE_RESERVATIONS,
            Permission.MANAGE_CREDIT_ACCOUNTS,
            Permission.DELETE_CREDIT_ENTRIES,
            Permission.MANAGE_QUOTES,
            Permission.DELETE_QUOTES,
            Permission.FINALIZE_QUOTES
        ),
        UserRole.CAJERO to setOf(
            Permission.OPERATE_CASH,
            Permission.MANAGE_CUSTOMERS,
            Permission.MANAGE_RESERVATIONS,
            Permission.MANAGE_CREDIT_ACCOUNTS,
            Permission.MANAGE_QUOTES
        ),
        UserRole.CAMARERO to setOf(
            Permission.MANAGE_CUSTOMERS,
            Permission.MANAGE_RESERVATIONS,
            Permission.MANAGE_QUOTES
        ),
        UserRole.COCINA to emptySet()
    )

    private var customRolePermissions: Map<UserRole, Set<Permission>> = loadRolePermissions()

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("role_permissions")
    } catch (_: Exception) { false }

    fun canAccess(screen: Screen, user: User?): Boolean {
        if (user == null) return screen == Screen.POS
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.SUPERVISOR -> screen in supervisorScreens
            UserRole.CAJERO -> screen in cashierScreens
            UserRole.CAMARERO -> screen in waiterScreens
            UserRole.COCINA -> screen in kitchenScreens
        }
    }

    fun landingScreenFor(user: User?): Screen =
        when (user?.role) {
            UserRole.ADMIN, UserRole.SUPERVISOR -> Screen.Dashboard
            UserRole.COCINA -> Screen.Comandas
            else -> Screen.POS
        }

    fun hasPermission(user: User?, permission: Permission): Boolean =
        user?.let { permissionsForRole(it.role).contains(permission) } == true

    fun permissionsForRole(role: UserRole): Set<Permission> =
        if (role == UserRole.ADMIN) Permission.entries.toSet()
        else customRolePermissions[role] ?: defaultRolePermissions[role].orEmpty()

    fun setPermissionsForRole(role: UserRole, permissions: Set<Permission>) {
        if (role == UserRole.ADMIN) return
        customRolePermissions = customRolePermissions + (role to permissions)
        saveRolePermissions()
        AuditLogManager.log("Seguridad", "ACTUALIZAR_PERMISOS_ROL", "${role.name}: ${permissions.joinToString(",") { it.name }}")
    }

    fun resetPermissionsForRole(role: UserRole) {
        if (role == UserRole.ADMIN) return
        customRolePermissions = customRolePermissions - role
        saveRolePermissions()
        AuditLogManager.log("Seguridad", "RESTAURAR_PERMISOS_ROL", role.name)
    }

    private fun loadRolePermissions(): Map<UserRole, Set<Permission>> {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("role_permissions") { it }
                if (rows.isNotEmpty()) {
                    val result = mutableMapOf<UserRole, MutableSet<Permission>>()
                    rows.forEach { row ->
                        val roleName = row["role_name"] as? String ?: return@forEach
                        val permName = row["permission_name"] as? String ?: return@forEach
                        val role = UserRole.entries.firstOrNull { it.name == roleName } ?: return@forEach
                        val perm = Permission.entries.firstOrNull { it.name == permName } ?: return@forEach
                        result.getOrPut(role) { mutableSetOf() }.add(perm)
                    }
                    result.remove(UserRole.ADMIN)
                    return result
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(PERMISSIONS_FILE) ?: return emptyMap()
        val result = text.lines()
            .filter { it.isNotBlank() && it.contains('\t') }
            .mapNotNull { line ->
                val parts = line.split('\t')
                val role = UserRole.entries.firstOrNull { it.name == parts.getOrNull(0) } ?: return@mapNotNull null
                if (role == UserRole.ADMIN) return@mapNotNull null
                val permissions = parts.getOrNull(1).orEmpty()
                    .split(',')
                    .mapNotNull { name -> Permission.entries.firstOrNull { it.name == name } }
                    .toSet()
                role to permissions
            }
            .toMap()
        if (result.isNotEmpty() && isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("role_permissions")
                    result.forEach { (role, perms) ->
                        perms.forEach { perm ->
                            DatabaseManager.insert("role_permissions", mapOf(
                                "role_name" to role.name,
                                "permission_name" to perm.name
                            ))
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        return result
    }

    private fun saveRolePermissions() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("role_permissions")
                    customRolePermissions
                        .filterKeys { it != UserRole.ADMIN }
                        .forEach { (role, perms) ->
                            perms.forEach { perm ->
                                DatabaseManager.insert("role_permissions", mapOf(
                                    "role_name" to role.name,
                                    "permission_name" to perm.name
                                ))
                            }
                        }
                }
            } catch (_: Exception) { }
        }
        val lines = customRolePermissions
            .filterKeys { it != UserRole.ADMIN }
            .entries
            .sortedBy { it.key.name }
            .map { (role, permissions) ->
                "${role.name}\t${permissions.sortedBy { it.name }.joinToString(",") { it.name }}"
            }
        PersistentFiles.writeText(PERMISSIONS_FILE, lines.joinToString("\n"))
    }

    fun canManageUsers(user: User?): Boolean = hasPermission(user, Permission.MANAGE_USERS)

    fun canManageEmployees(user: User?): Boolean = hasPermission(user, Permission.MANAGE_EMPLOYEES)

    fun canManageInvoices(user: User?): Boolean = hasPermission(user, Permission.MANAGE_INVOICES)

    fun canManageCatalog(user: User?): Boolean = hasPermission(user, Permission.MANAGE_CATALOG)

    fun canManageSettings(user: User?): Boolean = hasPermission(user, Permission.MANAGE_SETTINGS)

    fun canOperateCash(user: User?): Boolean = hasPermission(user, Permission.OPERATE_CASH)

    fun canManageSuppliers(user: User?): Boolean = hasPermission(user, Permission.MANAGE_SUPPLIERS)

    fun canManagePurchases(user: User?): Boolean = hasPermission(user, Permission.MANAGE_PURCHASES)

    fun canManageRecipes(user: User?): Boolean = hasPermission(user, Permission.MANAGE_RECIPES)

    fun canManageBackups(user: User?): Boolean = hasPermission(user, Permission.MANAGE_BACKUPS)

    fun canAccessCloud(user: User?): Boolean = hasPermission(user, Permission.ACCESS_CLOUD)

    fun canManageCustomers(user: User?): Boolean = hasPermission(user, Permission.MANAGE_CUSTOMERS)

    fun canDeleteCustomers(user: User?): Boolean = hasPermission(user, Permission.DELETE_CUSTOMERS)

    fun canManageReservations(user: User?): Boolean = hasPermission(user, Permission.MANAGE_RESERVATIONS)

    fun canDeleteReservations(user: User?): Boolean = hasPermission(user, Permission.DELETE_RESERVATIONS)

    fun canManageCreditAccounts(user: User?): Boolean = hasPermission(user, Permission.MANAGE_CREDIT_ACCOUNTS)

    fun canDeleteCreditEntries(user: User?): Boolean = hasPermission(user, Permission.DELETE_CREDIT_ENTRIES)

    fun canManageQuotes(user: User?): Boolean = hasPermission(user, Permission.MANAGE_QUOTES)

    fun canDeleteQuotes(user: User?): Boolean = hasPermission(user, Permission.DELETE_QUOTES)

    fun canFinalizeQuotes(user: User?): Boolean = hasPermission(user, Permission.FINALIZE_QUOTES)
}
