package com.tmrestaurant.ui.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class CategoryState {
    private var initialCategories = AppPersistence.loadCategories()
    var categories by mutableStateOf(initialCategories ?: MockData.categories)
        private set

    init {
        if (initialCategories == null) AppPersistence.saveCategories(categories)
    }

    fun reload() {
        initialCategories = AppPersistence.loadCategories()
        categories = initialCategories ?: MockData.categories
    }

    val nextId: Int get() = (categories.maxOfOrNull { it.id } ?: 0) + 1

    fun add(cat: Category) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CATEGORIA_CREAR", "Acceso denegado para crear categorias", level = "WARN")
            return
        }
        categories = categories + cat
        AppPersistence.saveCategories(categories)
    }

    fun update(cat: Category) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CATEGORIA_EDITAR", "Acceso denegado para editar categorias", level = "WARN")
            return
        }
        categories = categories.map { if (it.id == cat.id) cat.copy(updatedAt = System.currentTimeMillis()) else it }
        AppPersistence.saveCategories(categories)
    }

    fun delete(id: Int) {
        if (!AccessControl.canManageCatalog(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CATEGORIA_ELIMINAR", "Acceso denegado para eliminar categorias", level = "WARN")
            return
        }
        categories = categories.filter { it.id != id }
        AppPersistence.saveCategories(categories)
    }
}

val LocalCategoryState = staticCompositionLocalOf { CategoryState() }
