package com.tmrestaurant.ui.data

data class SupportTable(
    val fileName: String,
    val displayName: String,
    val columns: List<String>,
    val rows: List<List<String>>,
    val sizeBytes: Int
)

object SupportStorage {
    private val schemas = mapOf(
        "products.v1.tsv" to listOf(
            "id", "nombre", "codigo", "codigo_barras", "categoria", "descripcion", "precio",
            "costo", "itbis", "stock", "alerta_stock", "imagen", "activo", "visible_pos",
            "control_inventario", "unidad", "uid", "creado", "actualizado"
        ),
        "categories.v1.tsv" to listOf(
            "id", "nombre", "descripcion", "color", "activo", "visible_pos", "orden",
            "uid", "creado", "actualizado"
        ),
        "clientes.v1.tsv" to listOf(
            "id", "nombre", "rnc", "telefono", "email", "direccion", "tipo",
            "limite_credito", "uid", "creado", "actualizado"
        ),
        "usuarios.v1.tsv" to listOf(
            "usuario", "nombre", "pin", "contrasena", "rol", "uid", "creado", "actualizado"
        ),
        "extras.v1.tsv" to listOf(
            "id", "nombre", "precio", "producto_id", "tipo", "uid", "creado", "actualizado"
        ),
        "proveedores.v1.tsv" to listOf(
            "id", "nombre", "rnc", "contacto", "telefono", "email", "direccion", "rubro",
            "uid", "creado", "actualizado"
        ),
        "comandas.v1.tsv" to listOf(
            "id", "mesa", "estado", "creado", "turno_id", "area", "productos", "uid", "actualizado"
        ),
        "invoices.v1.tsv" to listOf(
            "factura", "ncf", "total", "subtotal", "itbis", "metodo_pago", "recibido",
            "cambio", "nota", "recargo", "porcentaje_recargo", "turno_id", "productos",
            "fecha", "descuento_nombre", "descuento", "propina_nombre", "propina",
            "cliente_id", "cliente", "rnc", "telefono"
        ),
        "credit_accounts.v1.tsv" to listOf(
            "tipo", "id", "referencia", "valor_1", "valor_2", "valor_3", "valor_4"
        ),
        "turnos.v1.tsv" to listOf(
            "tipo", "id", "referencia", "nombre", "valor_1", "valor_2", "valor_3",
            "valor_4", "valor_5", "valor_6", "valor_7"
        )
    )

    fun tableFiles(): List<String> =
        PersistentFiles.listFiles().filter { it.endsWith(".tsv", ignoreCase = true) }.sorted()

    fun load(fileName: String): SupportTable {
        val raw = PersistentFiles.readText(fileName).orEmpty()
        val rows = raw.lineSequence().filter { it.isNotBlank() }.map(::splitEscaped).toList()
        val maxColumns = rows.maxOfOrNull { it.size } ?: schemas[fileName]?.size ?: 0
        val known = schemas[fileName].orEmpty()
        val columns = List(maxColumns) { index -> known.getOrNull(index) ?: "campo_${index + 1}" }
        return SupportTable(
            fileName = fileName,
            displayName = fileName.substringBeforeLast('.').replace(".v1", ""),
            columns = columns,
            rows = rows.map { it + List(maxColumns - it.size) { "" } },
            sizeBytes = raw.encodeToByteArray().size
        )
    }

    fun saveRows(fileName: String, rows: List<List<String>>) {
        PersistentFiles.writeText(
            fileName,
            rows.joinToString("\n") { row -> row.joinToString("\t") { escape(it) } }
        )
    }

    fun backup(fileName: String): String = PersistentFiles.readText(fileName).orEmpty()

    fun restore(fileName: String, content: String) {
        PersistentFiles.writeText(fileName, content.trimEnd())
    }

    fun clear(fileName: String) {
        PersistentFiles.writeText(fileName, "")
    }

    private fun splitEscaped(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var escaped = false
        line.forEach { char ->
            if (escaped) {
                current.append(
                    when (char) {
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        else -> char
                    }
                )
                escaped = false
            } else {
                when (char) {
                    '\\' -> escaped = true
                    '\t' -> {
                        values += current.toString()
                        current.clear()
                    }
                    else -> current.append(char)
                }
            }
        }
        if (escaped) current.append('\\')
        values += current.toString()
        return values
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
}
