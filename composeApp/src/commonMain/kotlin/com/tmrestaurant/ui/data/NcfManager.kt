package com.tmrestaurant.ui.data

import com.tmrestaurant.db.DatabaseManager

private const val LEGACY_NCF_FILE = "ncf.counter"

enum class FiscalDocumentType(val label: String, val defaultPrefix: String) {
    CONSUMIDOR_FINAL("Consumidor Final", "E32"),
    CREDITO_FISCAL("Credito Fiscal", "E31"),
    GUBERNAMENTAL("Gubernamental", "E45"),
    REGIMEN_ESPECIAL("Regimen Especial", "E44"),
    NOTA_CREDITO("Nota de Credito", "E34"),
    NOTA_DEBITO("Nota de Debito", "E33")
}

data class FiscalSequence(
    val type: FiscalDocumentType,
    val enabled: Boolean = true,
    val prefix: String = type.defaultPrefix,
    val current: Int = 39,
    val rangeStart: Int = 1,
    val rangeEnd: Int = 99999999,
    val validUntil: String = "31/12/2027"
) {
    val remaining: Int get() = (rangeEnd - current).coerceAtLeast(0)
    val isExhausted: Boolean get() = current >= rangeEnd
}

object NcfManager {
    private var sequences: Map<FiscalDocumentType, FiscalSequence> = loadSequences()

    fun getNextNcf(type: FiscalDocumentType = FiscalDocumentType.CONSUMIDOR_FINAL): String {
        val seq = sequenceFor(type)
        if (!seq.enabled) return ""
        if (seq.isExhausted) {
            AuditLogManager.log("Fiscal", "NCF_AGOTADO", "${type.name} rango ${seq.rangeStart}-${seq.rangeEnd}", level = "WARN")
            return ""
        }
        val next = (seq.current + 1).coerceAtLeast(seq.rangeStart)
        val updated = seq.copy(current = next)
        sequences = sequences + (type to updated)
        saveSequences()
        return updated.prefix + next.toString().padStart(8, '0')
    }

    fun allSequences(): List<FiscalSequence> =
        FiscalDocumentType.entries.map { sequenceFor(it) }

    fun updateSequence(sequence: FiscalSequence) {
        sequences = sequences + (sequence.type to sequence)
        saveSequences()
        AuditLogManager.log("Fiscal", "ACTUALIZAR_SECUENCIA_NCF", "${sequence.type.name} ${sequence.prefix}${sequence.current}")
    }

    fun sequenceFor(type: FiscalDocumentType): FiscalSequence =
        sequences[type] ?: defaultSequence(type)

    fun resetSequence(value: Int) {
        updateSequence(sequenceFor(FiscalDocumentType.CONSUMIDOR_FINAL).copy(current = value))
    }

    private fun loadSequences(): Map<FiscalDocumentType, FiscalSequence> {
        val dbRows = try {
            DatabaseManager.query("fiscal_sequences") { it }
        } catch (_: Exception) { emptyList() }

        if (dbRows.isNotEmpty()) {
            return dbRows.mapNotNull { row ->
                val typeName = row["type"] as? String ?: return@mapNotNull null
                val type = FiscalDocumentType.entries.firstOrNull { it.name == typeName } ?: return@mapNotNull null
                type to FiscalSequence(
                    type = type,
                    enabled = (row["enabled"] as? Long)?.let { it == 1L } ?: true,
                    prefix = row["prefix"] as? String ?: type.defaultPrefix,
                    current = (row["current"] as? Long)?.toInt() ?: 39,
                    rangeStart = (row["range_start"] as? Long)?.toInt() ?: 1,
                    rangeEnd = (row["range_end"] as? Long)?.toInt() ?: 99999999,
                    validUntil = row["valid_until"] as? String ?: "31/12/2027"
                )
            }.toMap()
        }

        val legacy = PersistentFiles.readText(LEGACY_NCF_FILE)?.trim()?.toIntOrNull() ?: 39
        val result = mapOf(FiscalDocumentType.CONSUMIDOR_FINAL to defaultSequence(FiscalDocumentType.CONSUMIDOR_FINAL).copy(current = legacy))
        if (legacy != 39) {
            try {
                DatabaseManager.insert("fiscal_sequences", mapOf(
                    "type" to FiscalDocumentType.CONSUMIDOR_FINAL.name,
                    "enabled" to 1,
                    "prefix" to FiscalDocumentType.CONSUMIDOR_FINAL.defaultPrefix,
                    "current" to legacy,
                    "range_start" to 1,
                    "range_end" to 99999999,
                    "valid_until" to "31/12/2027",
                    "_sync_status" to "pending",
                    "_updated_at" to System.currentTimeMillis(),
                    "_server_id" to null
                ))
            } catch (_: Exception) { }
        }
        return result
    }

    private fun saveSequences() {
        val now = System.currentTimeMillis()
        allSequences().forEach { seq ->
            DatabaseManager.insert("fiscal_sequences", mapOf(
                "type" to seq.type.name,
                "enabled" to (if (seq.enabled) 1 else 0),
                "prefix" to seq.prefix,
                "current" to seq.current,
                "range_start" to seq.rangeStart,
                "range_end" to seq.rangeEnd,
                "valid_until" to seq.validUntil,
                "_sync_status" to "pending",
                "_updated_at" to now,
                "_server_id" to null
            ))
        }
    }

    private fun defaultSequence(type: FiscalDocumentType): FiscalSequence =
        FiscalSequence(
            type = type,
            enabled = type == FiscalDocumentType.CONSUMIDOR_FINAL || type == FiscalDocumentType.CREDITO_FISCAL,
            prefix = type.defaultPrefix
        )
}
