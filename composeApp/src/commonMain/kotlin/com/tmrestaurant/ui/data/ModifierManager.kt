package com.tmrestaurant.ui.data

import androidx.compose.runtime.mutableStateListOf
import com.tmrestaurant.db.DatabaseManager

data class ModifierOption(
    val id: String = "mo_${(10000..99999).random()}",
    val name: String,
    val price: Double = 0.0
)

data class ModifierGroup(
    val id: String = "mg_${(10000..99999).random()}",
    val name: String,
    val options: List<ModifierOption> = emptyList(),
    val required: Boolean = false,
    val maxSelections: Int = 1
)

data class ModifierSelection(
    val groupId: String,
    val groupName: String,
    val optionId: String,
    val optionName: String,
    val price: Double = 0.0
)

object ModifierManager {
    private const val FILE = "modifiers.v1.tsv"
    val groups = mutableStateListOf<ModifierGroup>()

    init { load() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("modifier_groups")
    } catch (_: Exception) { false }

    fun add(group: ModifierGroup) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_MODIFICADOR_CREAR", "Acceso denegado para crear modificadores", level = "WARN")
            return
        }
        groups.add(group); save()
    }
    fun update(group: ModifierGroup) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_MODIFICADOR_EDITAR", "Acceso denegado para editar modificadores", level = "WARN")
            return
        }
        val idx = groups.indexOfFirst { it.id == group.id }
        if (idx >= 0) { groups[idx] = group; save() }
    }
    fun delete(id: String) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_MODIFICADOR_ELIMINAR", "Acceso denegado para eliminar modificadores", level = "WARN")
            return
        }
        groups.removeAll { it.id == id }; save()
    }
    fun getById(id: String): ModifierGroup? = groups.find { it.id == id }

    private fun esc(v: String) = v.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
    private fun descline(line: String): List<String> {
        val result = mutableListOf<String>()
        val cur = StringBuilder()
        var escaping = false
        for (ch in line) {
            if (escaping) { cur.append(when (ch) { 'n' -> '\n'; 't' -> '\t'; 'r' -> '\r'; else -> ch }); escaping = false }
            else when (ch) { '\\' -> escaping = true; '\t' -> { result.add(cur.toString()); cur.clear() }; else -> cur.append(ch) }
        }
        result.add(cur.toString())
        return result
    }
    private fun serializeOptions(opts: List<ModifierOption>): String =
        opts.joinToString("|") { listOf(esc(it.id), esc(it.name), it.price.toString()).joinToString(",") { esc(it) } }
    private fun deserializeOptions(data: String): List<ModifierOption> {
        if (data.isBlank()) return emptyList()
        return data.split("|").map { part ->
            val f = part.split(",").map { v -> v.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\") }
            ModifierOption(id = f.getOrElse(0) { "mo_0" }, name = f.getOrElse(1) { "" }, price = f.getOrElse(2) { "0" }.toDoubleOrNull() ?: 0.0)
        }
    }
    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("modifier_groups")
                    DatabaseManager.deleteAll("modifier_options")
                    groups.forEach { g ->
                        DatabaseManager.insert("modifier_groups", mapOf(
                            "id" to g.id,
                            "name" to g.name,
                            "required" to (if (g.required) 1 else 0),
                            "max_selections" to g.maxSelections,
                            "created_at" to System.currentTimeMillis(),
                            "updated_at" to System.currentTimeMillis()
                        ))
                        g.options.forEach { opt ->
                            DatabaseManager.insert("modifier_options", mapOf(
                                "id" to opt.id,
                                "group_id" to g.id,
                                "name" to opt.name,
                                "price" to opt.price
                            ))
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = groups.map { g ->
            listOf(esc(g.id), esc(g.name), serializeOptions(g.options), g.required.toString(), g.maxSelections.toString()).joinToString("\t")
        }
        PersistentFiles.writeText(FILE, lines.joinToString("\n"))
    }
    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("modifier_groups") { it }
                if (rows.isNotEmpty()) {
                    val optionsMap = mutableMapOf<String, MutableList<ModifierOption>>()
                    try {
                        val optRows = DatabaseManager.query("modifier_options") { it }
                        optRows.forEach { row ->
                            val groupId = row["group_id"] as? String ?: return@forEach
                            optionsMap.getOrPut(groupId) { mutableListOf() }.add(
                                ModifierOption(
                                    id = row["id"] as? String ?: "",
                                    name = row["name"] as? String ?: "",
                                    price = (row["price"] as? Double) ?: ((row["price"] as? Long)?.toDouble() ?: 0.0)
                                )
                            )
                        }
                    } catch (_: Exception) { }
                    groups.clear()
                    rows.forEach { row ->
                        val id = row["id"] as? String ?: return@forEach
                        groups.add(ModifierGroup(
                            id = id,
                            name = row["name"] as? String ?: "",
                            options = optionsMap[id].orEmpty(),
                            required = ((row["required"] as? Long) ?: 0L) == 1L,
                            maxSelections = ((row["max_selections"] as? Long) ?: 1L).toInt()
                        ))
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE) ?: return
        if (text.isBlank()) return
        groups.clear()
        for (line in text.lines()) {
            if (line.isBlank()) continue
            val f = descline(line)
            if (f.size < 3) continue
            groups.add(ModifierGroup(id = f[0], name = f[1], options = deserializeOptions(f.getOrElse(2) { "" }), required = f.getOrElse(3) { "false" }.toBooleanStrictOrNull() ?: false, maxSelections = f.getOrElse(4) { "1" }.toIntOrNull() ?: 1))
        }
        if (groups.isNotEmpty()) save()
    }
}
