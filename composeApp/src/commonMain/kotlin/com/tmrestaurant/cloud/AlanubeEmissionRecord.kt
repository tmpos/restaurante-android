package com.tmrestaurant.cloud

import com.tmrestaurant.ui.data.PersistentFiles

data class AlanubeEmissionRecord(
    val timestamp: String = "",
    val docType: String = "",
    val encf: String = "",
    val total: String = "",
    val documentId: String = "",
    val legalStatus: String = "",
    val success: Boolean = false,
    val responseBody: String = ""
)

object AlanubeEmissionStore {
    private const val FILE_NAME = "alanube_emissions.v1.tsv"

    fun load(): List<AlanubeEmissionRecord> {
        return PersistentFiles.readText(FILE_NAME)
            ?.lineSequence()
            ?.filter { it.isNotBlank() }
            ?.map { line ->
                val parts = line.split('\t')
                AlanubeEmissionRecord(
                    timestamp = decode(parts.getOrElse(0) { "" }),
                    docType = decode(parts.getOrElse(1) { "" }),
                    encf = decode(parts.getOrElse(2) { "" }),
                    total = decode(parts.getOrElse(3) { "" }),
                    documentId = decode(parts.getOrElse(4) { "" }),
                    legalStatus = decode(parts.getOrElse(5) { "" }),
                    success = parts.getOrElse(6) { "" } == "1",
                    responseBody = decode(parts.getOrElse(7) { "" })
                )
            }
            ?.toList()
            .orEmpty()
    }

    fun add(record: AlanubeEmissionRecord) {
        val records = load().toMutableList()
        records.add(0, record)
        val maxLines = 200
        val trimmed = if (records.size > maxLines) records.take(maxLines) else records
        saveAll(trimmed)
    }

    fun saveAll(records: List<AlanubeEmissionRecord>) {
        val text = records.joinToString("\n") { r ->
            listOf(
                encode(r.timestamp),
                encode(r.docType),
                encode(r.encf),
                encode(r.total),
                encode(r.documentId),
                encode(r.legalStatus),
                if (r.success) "1" else "0",
                encode(r.responseBody.take(500))
            ).joinToString("\t")
        }
        PersistentFiles.writeText(FILE_NAME, text)
    }

    private fun encode(v: String) = v.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
    private fun decode(v: String): String {
        val sb = StringBuilder()
        var esc = false
        for (c in v) {
            if (esc) { sb.append(when (c) { 'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'; else -> c }); esc = false }
            else if (c == '\\') esc = true else sb.append(c)
        }
        if (esc) sb.append('\\')
        return sb.toString()
    }
}
