package com.tmrestaurant.cloud

import com.tmrestaurant.ui.data.PersistentFiles

enum class AlanubeEnvironment(val label: String, val baseUrl: String) {
    SANDBOX("Sandbox", "https://sandbox.alanube.co/dom/v1"),
    PRODUCTION("Produccion", "https://api.alanube.co/dom/v1")
}

enum class FiscalDocType(val label: String, val code: String) {
    CONSUMIDOR_FINAL("Consumo (E32)", "32"),
    CREDITO_FISCAL("Credito Fiscal (E31)", "31"),
    GUBERNAMENTAL("Gubernamental (E45)", "45"),
    REGIMEN_ESPECIAL("Regimen Especial (E44)", "44"),
    NOTA_CREDITO("Nota Credito (E34)", "34"),
    NOTA_DEBITO("Nota Debito (E33)", "33")
}

data class AlanubeConfig(
    val environment: AlanubeEnvironment = AlanubeEnvironment.SANDBOX,
    val jwtToken: String = "",
    val companyId: String = "",
    val rnc: String = "",
    val companyName: String = "",
    val address: String = "",
    val stampDate: String = ""
)

data class AlanubeStatus(
    val connected: Boolean = false,
    val companyName: String? = null,
    val error: String? = null
)

object AlanubeConfigStore {
    private const val FILE_NAME = "alanube.v1.props"

    fun load(): AlanubeConfig {
        val values = PersistentFiles.readText(FILE_NAME)
            ?.lineSequence()
            ?.mapNotNull { line ->
                val sep = line.indexOf('=')
                if (sep <= 0) null else line.substring(0, sep) to unescape(line.substring(sep + 1))
            }
            ?.toMap()
            .orEmpty()

        return AlanubeConfig(
            environment = AlanubeEnvironment.entries.firstOrNull { it.name == values["env"] } ?: AlanubeEnvironment.SANDBOX,
            jwtToken = values["jwt"] ?: "",
            companyId = values["companyId"] ?: "",
            rnc = values["rnc"] ?: "",
            companyName = values["companyName"] ?: "",
            address = values["address"] ?: "",
            stampDate = values["stampDate"] ?: ""
        )
    }

    fun save(config: AlanubeConfig) {
        val entries = listOf(
            "env" to config.environment.name,
            "jwt" to config.jwtToken.trim(),
            "companyId" to config.companyId.trim(),
            "rnc" to config.rnc.trim(),
            "companyName" to config.companyName.trim(),
            "address" to config.address.trim(),
            "stampDate" to config.stampDate.trim()
        )
        PersistentFiles.writeText(FILE_NAME, entries.joinToString("\n") { "${it.first}=${escape(it.second)}" })
    }

    private fun escape(v: String) = v.replace("\\", "\\\\").replace("\n", "\\n").replace("=", "\\e")
    private fun unescape(v: String): String {
        val sb = StringBuilder()
        var esc = false
        v.forEach { c ->
            if (esc) { sb.append(if (c == 'n') '\n' else if (c == 'e') '=' else c); esc = false }
            else if (c == '\\') esc = true
            else sb.append(c)
        }
        if (esc) sb.append('\\')
        return sb.toString()
    }
}
