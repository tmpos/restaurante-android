package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tmrestaurant.db.DatabaseManager
import com.tmrestaurant.platform.sha256Hex

object UsuariosManager {
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCK_WINDOW_MS = 5 * 60 * 1000L
    private const val FILE_NAME = "usuarios.v1.tsv"

    var usuarios by mutableStateOf<List<Usuario>>(emptyList())
        private set

    init {
        load()
        if (usuarios.isEmpty()) {
            usuarios = listOf(
                Usuario(id = "admin", name = "Administrador", pin = "1234", password = "admin123", role = UserRole.ADMIN, mustChangeCredentials = false),
                Usuario(id = "cajero1", name = "Cajero 1", pin = "1111", password = "cajero1", role = UserRole.CAJERO, mustChangeCredentials = false),
                Usuario(id = "cajero2", name = "Cajero 2", pin = "2222", password = "cajero2", role = UserRole.CAJERO, mustChangeCredentials = false)
            )
            save()
        }
    }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("usuarios")
    } catch (_: Exception) { false }

    fun add(usuario: Usuario) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageUsers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_USUARIO_CREAR", "Acceso denegado para crear usuarios", level = "WARN")
            return
        }
        usuarios = usuarios + normalizeForPersist(usuario)
        save()
        AuditLogManager.log("Usuarios", "CREAR_USUARIO", "Usuario ${usuario.id} (${usuario.role.name}) creado")
    }
    fun update(usuario: Usuario) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageUsers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_USUARIO_EDITAR", "Acceso denegado para editar usuarios", level = "WARN")
            return
        }
        val existing = usuarios.firstOrNull { it.id == usuario.id }
        val normalized = normalizeForPersist(usuario, existing)
        usuarios = usuarios.map { if (it.id == usuario.id) normalized else it }
        save()
        AuditLogManager.log("Usuarios", "EDITAR_USUARIO", "Usuario ${usuario.id} actualizado")
    }
    fun delete(id: String) {
        if (!SystemActionContext.isPrivileged() && !AccessControl.canManageUsers(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_USUARIO_ELIMINAR", "Acceso denegado para eliminar usuarios", level = "WARN")
            return
        }
        usuarios = usuarios.filter { it.id != id }
        save()
        AuditLogManager.log("Usuarios", "ELIMINAR_USUARIO", "Usuario $id eliminado", level = "WARN")
    }

    fun isLocked(usuario: Usuario, now: Long = System.currentTimeMillis()): Boolean =
        (usuario.lockedUntil ?: 0L) > now

    fun lockRemainingMs(usuario: Usuario, now: Long = System.currentTimeMillis()): Long =
        ((usuario.lockedUntil ?: 0L) - now).coerceAtLeast(0L)

    fun findByLogin(login: String): Usuario? =
        usuarios.firstOrNull { it.id.equals(login.trim(), ignoreCase = true) || it.name.equals(login.trim(), ignoreCase = true) }

    fun findByPin(pin: String): Usuario? =
        usuarios.firstOrNull { matchesPin(it, pin) }

    fun isPinInUse(pin: String, exceptUserId: String? = null): Boolean =
        usuarios.any { it.id != exceptUserId && matchesPin(it, pin) }

    fun validateCredentials(id: String, password: String): Usuario? {
        val user = findByLogin(id) ?: return null
        if (isLocked(user)) return null
        return if (matchesPassword(user, password)) {
            clearFailures(user.id)
            user
        } else {
            registerFailedAttempt(user.id, "LOGIN_PASSWORD_INVALIDO")
            null
        }
    }

    fun validatePin(pin: String): Usuario? {
        val user = findByPin(pin) ?: return null
        if (isLocked(user)) return null
        clearFailures(user.id)
        return user
    }

    fun registerFailedPin(pin: String) {
        val user = findByPin(pin) ?: return
        registerFailedAttempt(user.id, "LOGIN_PIN_INVALIDO")
    }

    fun clearFailures(userId: String) {
        usuarios = usuarios.map {
            if (it.id == userId && (it.failedAttempts != 0 || it.lockedUntil != null)) {
                it.copy(failedAttempts = 0, lockedUntil = null, updatedAt = System.currentTimeMillis())
            } else it
        }
        save()
    }

    fun forceCredentialsUpdated(userId: String, name: String, pin: String, password: String) {
        val current = usuarios.firstOrNull { it.id == userId } ?: return
        update(
            current.copy(
                name = name.trim(),
                pin = pin,
                password = password,
                mustChangeCredentials = false,
                failedAttempts = 0,
                lockedUntil = null
            )
        )
        AuditLogManager.log("Usuarios", "CAMBIO_CREDENCIALES_OBLIGATORIO", "Credenciales actualizadas para $userId")
    }

    private fun registerFailedAttempt(userId: String, action: String) {
        val user = usuarios.firstOrNull { it.id == userId } ?: return
        val attempts = user.failedAttempts + 1
        val lockedUntil = if (attempts >= MAX_FAILED_ATTEMPTS) System.currentTimeMillis() + LOCK_WINDOW_MS else null
        usuarios = usuarios.map {
            if (it.id == userId) {
                it.copy(
                    failedAttempts = attempts,
                    lockedUntil = lockedUntil,
                    updatedAt = System.currentTimeMillis()
                )
            } else it
        }
        save()
        AuditLogManager.log(
            "Seguridad",
            action,
            if (lockedUntil != null) "Usuario $userId bloqueado por intentos fallidos" else "Intento fallido para usuario $userId ($attempts/$MAX_FAILED_ATTEMPTS)",
            level = "WARN"
        )
    }

    private fun esc(v: String) = v.replace("\\","\\\\").replace("\n","\\n").replace("\t","\\t")

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("usuarios")
                    usuarios.forEach { u ->
                        DatabaseManager.insert("usuarios", mapOf(
                            "id" to u.id,
                            "name" to u.name,
                            "pin" to u.pin,
                            "password" to u.password,
                            "role" to u.role.name,
                            "uid" to u.uid,
                            "created_at" to u.createdAt,
                            "updated_at" to u.updatedAt,
                            "failed_attempts" to u.failedAttempts,
                            "locked_until" to u.lockedUntil,
                            "must_change_credentials" to (if (u.mustChangeCredentials) 1 else 0),
                            "pin_hash" to u.pinHash,
                            "password_hash" to u.passwordHash
                        ))
                    }
                }
            } catch (e: Exception) {
                println("UsuariosManager.save() DB error: $e")
            }
        }
        val lines = usuarios.map { u ->
            listOf(
                u.id, u.name, u.pin, u.password, u.role.name, u.uid,
                u.createdAt.toString(), u.updatedAt.toString(),
                u.failedAttempts.toString(), (u.lockedUntil ?: 0L).toString(),
                u.mustChangeCredentials.toString(), u.pinHash, u.passwordHash
            ).joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText(FILE_NAME, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("usuarios") { it }
                if (rows.isNotEmpty()) {
                    var migrated = false
                    usuarios = rows.map { row ->
                        val user = Usuario(
                            id = row["id"] as? String ?: "",
                            name = row["name"] as? String ?: "",
                            pin = row["pin"] as? String ?: "",
                            password = row["password"] as? String ?: "",
                            role = UserRole.entries.firstOrNull { it.name == row["role"] as? String } ?: UserRole.CAJERO,
                            uid = row["uid"] as? String ?: genUid("usr"),
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis(),
                            failedAttempts = ((row["failed_attempts"] as? Long)?.toInt()) ?: 0,
                            lockedUntil = row["locked_until"] as? Long,
                            mustChangeCredentials = (row["must_change_credentials"] as? Long)?.let { it == 1L } ?: false,
                            pinHash = row["pin_hash"] as? String ?: "",
                            passwordHash = row["password_hash"] as? String ?: ""
                        )
                        val normalized = normalizeForPersist(user, user, preserveUpdatedAt = true)
                        migrated = migrated || normalized != user
                        normalized
                    }
                    val fixed = usuarios.map { u ->
                        if (u.mustChangeCredentials) {
                            if (u.pinHash.isNotBlank() && u.passwordHash.isNotBlank()) {
                                migrated = true; u.copy(mustChangeCredentials = false, updatedAt = System.currentTimeMillis())
                            } else if (u.pin.isBlank() && u.password.isBlank()) {
                                migrated = true; u.copy(mustChangeCredentials = false, updatedAt = System.currentTimeMillis())
                            } else u
                        } else u
                    }
                    usuarios = fixed
                    if (migrated) save()
                    return
                }
            } catch (e: Exception) {
                println("UsuariosManager.load() DB query error: $e")
            }
        }
        val text = PersistentFiles.readText(FILE_NAME) ?: return
        usuarios = text.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val f = line.split("\t")
            if (f.size < 5) null
            else {
                val user = Usuario(
                    id = f[0], name = f[1], pin = f[2], password = f[3],
                    role = UserRole.entries.firstOrNull { it.name == f[4] } ?: UserRole.CAJERO,
                    uid = f.getOrElse(5) { genUid("usr") },
                    createdAt = f.getOrElse(6) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                    updatedAt = f.getOrElse(7) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                    failedAttempts = f.getOrElse(8) { "0" }.toIntOrNull() ?: 0,
                    lockedUntil = f.getOrElse(9) { "0" }.toLongOrNull()?.takeIf { it > 0L },
                    mustChangeCredentials = f.getOrElse(10) { "false" }.toBooleanStrictOrNull() ?: false,
                    pinHash = f.getOrElse(11) { "" },
                    passwordHash = f.getOrElse(12) { "" }
                )
                normalizeForPersist(user, user, preserveUpdatedAt = true)
            }
        }
        if (usuarios.isNotEmpty()) save()
    }

    private fun normalizeForPersist(
        usuario: Usuario,
        existing: Usuario? = null,
        preserveUpdatedAt: Boolean = false
    ): Usuario {
        val base = existing ?: usuario
        val pinHash = when {
            usuario.pinHash.isNotBlank() -> usuario.pinHash
            usuario.pin.isNotBlank() -> hashCredential(base.uid, "pin", usuario.pin)
            else -> existing?.pinHash.orEmpty()
        }
        val passwordHash = when {
            usuario.passwordHash.isNotBlank() -> usuario.passwordHash
            usuario.password.isNotBlank() -> hashCredential(base.uid, "password", usuario.password)
            else -> existing?.passwordHash.orEmpty()
        }
        return usuario.copy(
            id = usuario.id.trim(),
            name = usuario.name.trim(),
            pin = "",
            password = "",
            uid = base.uid,
            createdAt = existing?.createdAt ?: usuario.createdAt,
            updatedAt = if (preserveUpdatedAt) usuario.updatedAt else System.currentTimeMillis(),
            pinHash = pinHash,
            passwordHash = passwordHash
        )
    }

    private fun matchesPin(usuario: Usuario, pin: String): Boolean =
        when {
            usuario.pinHash.isNotBlank() -> usuario.pinHash == hashCredential(usuario.uid, "pin", pin)
            else -> usuario.pin == pin
        }

    private fun matchesPassword(usuario: Usuario, password: String): Boolean =
        when {
            usuario.passwordHash.isNotBlank() -> usuario.passwordHash == hashCredential(usuario.uid, "password", password)
            else -> usuario.password == password
        }

    private fun hashCredential(uid: String, type: String, secret: String): String =
        sha256Hex("tmrestaurant:$type:$uid:${secret.trim()}")
}

data class Usuario(
    val id: String = "",
    val name: String = "",
    val pin: String = "",
    val password: String = "",
    val role: UserRole = UserRole.CAJERO,
    val uid: String = genUid("usr"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val failedAttempts: Int = 0,
    val lockedUntil: Long? = null,
    val mustChangeCredentials: Boolean = false,
    val pinHash: String = "",
    val passwordHash: String = ""
)
