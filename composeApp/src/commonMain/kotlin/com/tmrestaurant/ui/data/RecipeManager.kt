package com.tmrestaurant.ui.data

import androidx.compose.runtime.mutableStateListOf
import com.tmrestaurant.db.DatabaseManager

data class RecipeIngredient(
    val productId: Int,
    val productName: String,
    val quantity: Double,
    val unit: String = "unidad"
)

data class Recipe(
    val id: String = genUid("rec"),
    val productId: Int,
    val productName: String = "",
    val ingredients: List<RecipeIngredient> = emptyList(),
    val servings: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object RecipeManager {
    private const val FILE = "recipes.v1.tsv"
    val recipes = mutableStateListOf<Recipe>()

    init { load() }

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("recipes")
    } catch (_: Exception) { false }

    fun addOrUpdate(recipe: Recipe) {
        if (!AccessControl.canManageRecipes(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_RECETA_EDITAR", "Acceso denegado para crear o editar recetas", level = "WARN")
            return
        }
        val idx = recipes.indexOfFirst { it.productId == recipe.productId }
        if (idx >= 0) {
            recipes[idx] = recipe.copy(updatedAt = System.currentTimeMillis())
        } else {
            recipes.add(recipe)
        }
        save()
    }

    fun remove(productId: Int) {
        if (!AccessControl.canManageRecipes(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_RECETA_ELIMINAR", "Acceso denegado para eliminar recetas", level = "WARN")
            return
        }
        recipes.removeAll { it.productId == productId }
        save()
    }

    fun getForProduct(productId: Int): Recipe? = recipes.find { it.productId == productId }

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

    private fun save() {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("recipes")
                    DatabaseManager.deleteAll("recipe_ingredients")
                    recipes.forEach { r ->
                        DatabaseManager.insert("recipes", mapOf(
                            "id" to r.id,
                            "product_id" to r.productId,
                            "product_name" to r.productName,
                            "servings" to r.servings,
                            "created_at" to r.createdAt,
                            "updated_at" to r.updatedAt
                        ))
                        r.ingredients.forEach { ing ->
                            DatabaseManager.insert("recipe_ingredients", mapOf(
                                "recipe_id" to r.id,
                                "product_id" to ing.productId,
                                "product_name" to ing.productName,
                                "quantity" to ing.quantity,
                                "unit" to ing.unit
                            ))
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        val lines = recipes.map { r ->
            val ingStr = r.ingredients.joinToString("|") { i ->
                "${i.productId},${esc(i.productName)},${i.quantity},${esc(i.unit)}"
            }
            listOf(r.id, r.productId.toString(), esc(r.productName), ingStr, r.servings.toString(), r.createdAt.toString(), r.updatedAt.toString())
                .joinToString("\t") { esc(it) }
        }
        PersistentFiles.writeText(FILE, lines.joinToString("\n"))
    }

    private fun load() {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("recipes") { it }
                if (rows.isNotEmpty()) {
                    val ingMap = mutableMapOf<String, MutableList<RecipeIngredient>>()
                    try {
                        val ingRows = DatabaseManager.query("recipe_ingredients") { it }
                        ingRows.forEach { row ->
                            val recipeId = row["recipe_id"] as? String ?: return@forEach
                            ingMap.getOrPut(recipeId) { mutableListOf() }.add(
                                RecipeIngredient(
                                    productId = ((row["product_id"] as? Long) ?: (row["product_id"] as? Int)?.toLong() ?: 0L).toInt(),
                                    productName = row["product_name"] as? String ?: "",
                                    quantity = (row["quantity"] as? Double) ?: ((row["quantity"] as? Long)?.toDouble() ?: 0.0),
                                    unit = row["unit"] as? String ?: "unidad"
                                )
                            )
                        }
                    } catch (_: Exception) { }
                    recipes.clear()
                    rows.forEach { row ->
                        val id = row["id"] as? String ?: return@forEach
                        recipes.add(Recipe(
                            id = id,
                            productId = ((row["product_id"] as? Long) ?: (row["product_id"] as? Int)?.toLong() ?: 0L).toInt(),
                            productName = row["product_name"] as? String ?: "",
                            ingredients = ingMap[id].orEmpty(),
                            servings = ((row["servings"] as? Long) ?: 1L).toInt(),
                            createdAt = (row["created_at"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (row["updated_at"] as? Long) ?: System.currentTimeMillis()
                        ))
                    }
                    return
                }
            } catch (_: Exception) { }
        }
        val text = PersistentFiles.readText(FILE) ?: return
        if (text.isBlank()) return
        recipes.clear()
        text.lines().filter { it.isNotBlank() }.forEach { line ->
            val f = descline(line)
            if (f.size < 4) return@forEach
            val ingredients = f.getOrElse(3) { "" }.split("|").mapNotNull { part ->
                val p = part.split(",")
                if (p.size >= 3) RecipeIngredient(p[0].toIntOrNull() ?: return@mapNotNull null, p[1], p[2].toDoubleOrNull() ?: 0.0, p.getOrElse(3) { "unidad" }) else null
            }
            recipes.add(Recipe(
                id = f[0], productId = f[1].toIntOrNull() ?: return@forEach,
                productName = f[2], ingredients = ingredients,
                servings = f.getOrElse(4) { "1" }.toIntOrNull() ?: 1,
                createdAt = f.getOrElse(5) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                updatedAt = f.getOrElse(6) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
            ))
        }
        if (recipes.isNotEmpty()) save()
    }
}
