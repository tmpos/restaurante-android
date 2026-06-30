package com.tmrestaurant.ui.data.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.AuditLogManager
import com.tmrestaurant.ui.data.AppPersistence
import com.tmrestaurant.ui.data.TurnoManager

class SettingsState {
    var settings by mutableStateOf(AppPersistence.loadSettings() ?: AppSettings())
        private set

    var selectedSection by mutableStateOf(SettingsSection.EMPRESA)

    private var logoBytes: ByteArray? = null

    init {
        logoBytes = AppPersistence.loadCompanyLogo()
    }

    fun update(block: (AppSettings) -> AppSettings) {
        if (!AccessControl.canManageSettings(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CONFIGURACION_EDITAR", "Acceso denegado para actualizar configuracion", level = "WARN")
            return
        }
        settings = block(settings)
        AppPersistence.saveSettings(settings)
        AuditLogManager.log("Configuracion", "ACTUALIZAR_CONFIGURACION", "Cambios guardados en configuracion")
    }

    fun updatePrintCopies(copies: Int) {
        val safeCopies = copies.coerceIn(1, 5)
        settings = settings.copy(print = settings.print.copy(copies = safeCopies))
        AppPersistence.saveSettings(settings)
        AuditLogManager.log("Configuracion", "ACTUALIZAR_COPIAS_TICKET", "Copias de ticket: $safeCopies")
    }

    fun save() {
        if (!AccessControl.canManageSettings(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_CONFIGURACION_GUARDAR", "Acceso denegado para guardar configuracion", level = "WARN")
            return
        }
        AppPersistence.saveSettings(settings)
        AuditLogManager.log("Configuracion", "GUARDAR_CONFIGURACION", "Configuracion persistida manualmente")
    }

    fun cacheLogo(bytes: ByteArray) {
        if (!AccessControl.canManageSettings(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_LOGO_EMPRESA", "Acceso denegado para cambiar logo de empresa", level = "WARN")
            return
        }
        logoBytes = bytes
        AppPersistence.saveCompanyLogo(bytes)
    }

    fun getLogoBytes(): ByteArray? = logoBytes

    fun clearLogo() {
        if (!AccessControl.canManageSettings(TurnoManager.currentUser)) {
            AuditLogManager.log("Seguridad", "DENEGAR_LOGO_EMPRESA", "Acceso denegado para borrar logo de empresa", level = "WARN")
            return
        }
        logoBytes = null
        AppPersistence.saveCompanyLogo(byteArrayOf())
    }
}

val LocalSettingsState = staticCompositionLocalOf { SettingsState() }
