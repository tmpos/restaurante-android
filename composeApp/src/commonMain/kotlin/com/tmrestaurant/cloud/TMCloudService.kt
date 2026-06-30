package com.tmrestaurant.cloud

import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.data.*
import kotlinx.coroutines.*

object TMCloudService {
    private val client = CloudHttpClient()
    private var config = CloudConfig()
    private var baseUrl = ""
    private var syncJob: Job? = null
    private var lastSyncTimestamps = mutableMapOf<String, Long>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private const val SYNC_STATE_FILE = "tmcloud_sync_state.v1.props"

    fun init(cfg: CloudConfig) {
        config = cfg
        baseUrl = cfg.url.trim().trimEnd('/')
        loadSyncState()
    }

    fun saveConfig(cfg: CloudConfig) {
        init(cfg)
        CloudConfigStore.save(cfg)
        setAutoSync(cfg.autoSync, cfg.intervalSec.coerceAtLeast(10) * 1000L)
    }

    fun loadSavedConfig(): CloudConfig {
        val saved = CloudConfigStore.load()
        init(saved)
        if (saved.autoSync) setAutoSync(true, saved.intervalSec.coerceAtLeast(10) * 1000L)
        return saved
    }

    fun loadConfig(savedUrl: String, savedKey: String, savedSecret: String): CloudConfig {
        val cfg = CloudConfig(url = savedUrl, publicKey = savedKey, secretKey = savedSecret)
        init(cfg)
        return cfg
    }

    fun getConfig() = config

    suspend fun testConnection(): CloudStatus {
        if (baseUrl.isBlank() || config.publicKey.isBlank()) {
            return CloudStatus(false, "URL y Public Key requeridas")
        }
        return runCatching {
            val res = client.get("$baseUrl/health", config.publicKey)
            if (!res.ok) {
                val msg = when {
                    res.code == 404 -> "TMPBase no tiene /health. Verifica que el servidor tenga la version actualizada de ApiController.php"
                    res.code == 401 || res.code == 403 -> "Credenciales invalidas. Verifica tu Public Key"
                    else -> "Error ${res.code}: ${res.body.take(200)}"
                }
                return@runCatching CloudStatus(false, msg)
            }
            try {
                val status = extractJson(res.body, "status")
                if (status != "ok") return@runCatching CloudStatus(false, "Respuesta inesperada de TMPBase")
            } catch (_: Exception) {}
            CloudStatus(
                connected = true,
                projectName = extractJson(res.body, "project_name") ?: extractJson(res.body, "name"),
                projectUid = extractJson(res.body, "project_uid") ?: extractJson(res.body, "uid"),
                tableCount = extractJson(res.body, "tables")?.toIntOrNull() ?: extractJson(res.body, "table_count")?.toIntOrNull()
            )
        }.getOrElse { CloudStatus(false, it.message ?: "Error de red") }
    }

    suspend fun createTables(): SyncResult {
        if (baseUrl.isBlank()) return SyncResult(false, "URL API requerida")
        if (config.secretKey.isBlank()) return SyncResult(false, "Secret Key requerida")
        val schemas = getTableSchemas()
        val tablesArray = schemas.entries.joinToString(",") { (name, cols) ->
            val colsArray = cols.joinToString(",") { c ->
                buildJsonObject(
                    "name" to "\"${c.name}\"",
                    "type" to "\"${c.type}\"",
                    "nullable" to "${c.nullable}",
                    "primary" to "${c.primary}"
                )
            }
            buildJsonObject("name" to "\"$name\"", "columns" to "[$colsArray]")
        }
        val body = buildJsonObject("tables" to "[$tablesArray]")
        val path = "${baseUrl}/schema/tables/batch"
        val res = client.post(path, config.secretKey, body)
        if (!res.ok) return SyncResult(false, "Error ${res.code}: ${res.body}")
        return SyncResult(true, "Tablas creadas", inserts = schemas.size)
    }

    suspend fun pushTable(tableName: String): SyncResult {
        if (baseUrl.isBlank()) return SyncResult(false, "URL API requerida", errors = 1)
        val key = if (config.secretKey.isNotBlank()) config.secretKey else config.publicKey
        val data = getTableData(tableName).map { record ->
            "{" + record.entries.joinToString(",") { (k, v) ->
                if (v.startsWith("{") || v.startsWith("[")) "\"$k\":$v"
                else if (v.toDoubleOrNull() != null) "\"$k\":$v"
                else "\"$k\":\"${v.replace("\"", "\\\"")}\""
            } + "}"
        }
        if (data.isEmpty()) return SyncResult(true, "$tableName: sin datos", 0)
        val body = buildJsonObject("rows" to "[${data.joinToString(",")}]")
        val path = "${baseUrl}/${tableName}/upsert"
        val res = client.post(path, key, body)
        if (!res.ok) return SyncResult(false, "$tableName: ${res.body}", errors = 1)
        return SyncResult(true, "$tableName: ${data.size} enviados", inserts = data.size)
    }

    suspend fun pushAllTables(): List<SyncResult> =
        getTableSchemas().keys.map { pushTable(it) }

    suspend fun pullTable(tableName: String): SyncResult {
        if (baseUrl.isBlank()) return SyncResult(false, "URL API requerida", errors = 1)
        val since = lastSyncTimestamps[tableName]
        val path = if (since != null) "${baseUrl}/${tableName}/modified-since?since=$since"
        else "${baseUrl}/${tableName}?limit=1000"
        val res = client.get(path, config.publicKey)
        if (!res.ok) {
            if (res.code == 404) {
                val altPath = "${baseUrl}/${tableName}"
                val altRes = client.get(altPath, config.publicKey)
                if (!altRes.ok) return SyncResult(true, "$tableName: tabla remota no existe", 0, responseBody = altRes.body)
                return parseTableResponse(tableName, altRes.body)
            }
            return SyncResult(false, "$tableName: ${res.body}", errors = 1, responseBody = res.body)
        }
        return parseTableResponse(tableName, res.body)
    }

    private suspend fun parseTableResponse(tableName: String, body: String): SyncResult {
        val records = parseJsonArrayFromBody(body, tableName)
        if (records.isEmpty()) return SyncResult(true, "$tableName: sin cambios", 0, responseBody = body)
        val applied = applyTableData(tableName, records)
        lastSyncTimestamps[tableName] = System.currentTimeMillis()
        saveSyncState()
        return SyncResult(true, "$tableName: $applied aplicados", inserts = applied, responseBody = body)
    }

    private fun parseJsonArrayFromBody(body: String, tableName: String): List<Map<String, String>> {
        val trimmed = body.trim()
        if (trimmed.startsWith("[")) return parseJsonArray(trimmed)
        for (key in listOf("\"data\":", "\"${tableName}\":", "\"records\":", "\"rows\":")) {
            val idx = body.indexOf(key)
            if (idx >= 0) {
                val jsonData = body.substring(idx + key.length).trimStart()
                val records = parseJsonArray(jsonData)
                if (records.isNotEmpty()) return records
            }
        }
        val braceIdx = trimmed.indexOf('{')
        if (braceIdx >= 0) {
            val end = findMatchingBrace(trimmed, braceIdx)
            if (end > braceIdx) {
                val obj = trimmed.substring(braceIdx, end + 1)
                val map = parseJsonObject(obj)
                if (map.isNotEmpty()) return listOf(map)
            }
        }
        return emptyList()
    }

    suspend fun pullAllTables(): List<SyncResult> =
        getTableSchemas().keys.map { pullTable(it) }

    suspend fun syncChanges(): SyncResult {
        val results = mutableListOf<SyncResult>()
        for ((tableName, _) in getTableSchemas()) {
            val pushRes = pushTable(tableName)
            results.add(pushRes)
        }
        if (config.mode == SyncMode.AMBOS) {
            for ((tableName, _) in getTableSchemas()) {
                val pullRes = pullTable(tableName)
                results.add(pullRes)
            }
        }
        val total = results.sumOf { it.inserts }
        val errs = results.sumOf { it.errors }
        return SyncResult(errs == 0, if (errs == 0) "Sync completado: $total cambios" else "Sync con $errs errores", inserts = total, errors = errs)
    }

    suspend fun fullSync(): SyncResult {
        if (config.mode == SyncMode.OFFLINE) return SyncResult(false, "El modo Offline no sincroniza con la nube")
        lastSyncTimestamps.clear()
        saveSyncState()
        return syncChanges()
    }

    suspend fun downloadEssentialTables(): SyncResult {
        if (baseUrl.isBlank()) return SyncResult(false, "URL API requerida")
        val essential = listOf("usuarios", "productos", "categorias", "clientes", "proveedores", "facturas", "gastos")
        essential.forEach { lastSyncTimestamps.remove(it) }
        saveSyncState()
        val details = mutableListOf<String>()
        var totalErrors = 0
        var totalInserts = 0
        for (table in essential) {
            val res = pullTable(table)
            details.add(res.message)
            if (!res.success) totalErrors++
            totalInserts += res.inserts
        }
        val summary = details.joinToString("\n")
        return SyncResult(
            success = totalErrors == 0,
            message = summary,
            inserts = totalInserts,
            errors = totalErrors
        )
    }

    suspend fun downloadTables(tables: List<String>): List<SyncResult> {
        if (baseUrl.isBlank()) return listOf(SyncResult(false, "URL API requerida", errors = 1))
        tables.forEach { lastSyncTimestamps.remove(it) }
        saveSyncState()
        return tables.map { pullTable(it) }
    }

    suspend fun triggerSync() {
        if (config.mode == SyncMode.OFFLINE) return
        if (baseUrl.isBlank() || config.publicKey.isBlank()) return
        try { syncChanges() } catch (_: Exception) {}
    }

    fun setAutoSync(enable: Boolean, intervalMs: Long = 30000) {
        syncJob?.cancel()
        if (enable && config.mode != SyncMode.OFFLINE) {
            syncJob = scope.launch {
                while (isActive) {
                    try { syncChanges() } catch (_: Exception) {}
                    delay(intervalMs)
                }
            }
        }
    }

    fun stopAutoSync() { syncJob?.cancel() }

    fun notifyLocalChange() {
        scope.launch {
            delay(2000)
            try { triggerSync() } catch (_: Exception) {}
        }
    }

    private fun loadSyncState() {
        val text = PersistentFiles.readText(SYNC_STATE_FILE) ?: return
        if (text.isBlank()) return
        text.lines().filter { it.isNotBlank() }.forEach { line ->
            val parts = line.split("\t")
            if (parts.size >= 2) {
                lastSyncTimestamps[parts[0]] = parts[1].toLongOrNull() ?: 0L
            }
        }
    }

    private fun saveSyncState() {
        val lines = lastSyncTimestamps.entries.joinToString("\n") { "${it.key}\t${it.value}" }
        PersistentFiles.writeText(SYNC_STATE_FILE, lines)
    }

    private fun parseJsonArray(json: String): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()
        var depth = 0
        var i = 0
        while (i < json.length) {
            if (json[i] == '[') { depth++; i++; continue }
            if (json[i] == ']') break
            if (json[i] == '{') {
                val end = findMatchingBrace(json, i)
                if (end > i) {
                    val obj = json.substring(i, end + 1)
                    val map = parseJsonObject(obj)
                    if (map.isNotEmpty()) result.add(map)
                    i = end + 1
                } else i++
            } else i++
        }
        return result
    }

    private fun findMatchingBrace(json: String, start: Int): Int {
        var depth = 0
        var inStr = false
        for (i in start until json.length) {
            val ch = json[i]
            if (ch == '"' && (i == 0 || json[i-1] != '\\')) inStr = !inStr
            if (!inStr) {
                when (ch) { '{' -> depth++; '}' -> { depth--; if (depth == 0) return i } }
            }
        }
        return -1
    }

    private fun parseJsonObject(json: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val regex = "\"([^\"]+)\"\\s*:\\s*((\"([^\"]*)\")|([0-9]+(\\.[0-9]+)?)|(true|false|null))".toRegex()
        regex.findAll(json).forEach { m ->
            val key = m.groupValues[1]
            val value = m.groupValues[4].ifEmpty { m.groupValues[5].ifEmpty { m.groupValues[7] } }
            map[key] = value
        }
        return map
    }

    private fun applyTableData(tableName: String, records: List<Map<String, String>>): Int {
        var count = 0
        SystemActionContext.runPrivileged {
        when (tableName) {
            "productos" -> {
                val existingProducts = AppPersistence.loadProducts() ?: emptyList()
                var products = existingProducts.toMutableList()
                records.forEach { rec ->
                    val uid = rec["uid"] ?: return@forEach
                    val existing = products.find { it.uid == uid }
                    val product = Product(
                        id = existing?.id ?: (products.maxOfOrNull { it.id } ?: 0) + 1,
                        uid = uid, name = rec["nombre"] ?: "", code = rec["codigo"] ?: "",
                        barcode = rec["barcode"] ?: "", category = rec["categoria"] ?: "",
                        description = rec["descripcion"] ?: "", price = parseDouble(rec["precio"]),
                        cost = parseDouble(rec["costo"]), taxPercent = parseDouble(rec["impuesto"]),
                        stock = parseInt(rec["stock"]), stockAlert = parseInt(rec["alerta"]),
                        active = rec["activo"] != "0", sellInPos = true
                    )
                    if (existing != null) {
                        val idx = products.indexOfFirst { it.uid == uid }
                        if (idx >= 0) products[idx] = product
                    } else {
                        products.add(product)
                    }
                    count++
                }
                AppPersistence.saveProducts(products)
            }
            "clientes" -> {
                records.forEach { rec ->
                    val uid = rec["uid"] ?: return@forEach
                    val existing = ClientesManager.clientes.find { it.uid == uid }
                    val cliente = Cliente(
                        uid = uid, id = existing?.id ?: genUid("cli"),
                        nombre = rec["nombre"] ?: "", rnc = rec["rnc"] ?: "",
                        telefono = rec["telefono"] ?: "", email = rec["email"] ?: "",
                        direccion = rec["direccion"] ?: "", tipo = rec["tipo"] ?: "",
                        limiteCredito = parseDouble(rec["limite_credito"]),
                        loyaltyPoints = parseLong(rec["loyalty_points"]).toInt(),
                        totalSpent = parseDouble(rec["total_spent"]),
                        createdAt = parseLong(rec["created_at"])
                    )
                    if (existing != null) ClientesManager.update(cliente)
                    else ClientesManager.add(cliente)
                    count++
                }
            }
            "proveedores" -> {
                records.forEach { rec ->
                    val uid = rec["uid"] ?: return@forEach
                    val existing = ProveedoresManager.proveedores.find { it.uid == uid }
                    val prov = Proveedor(
                        uid = uid, id = existing?.id ?: genUid("prov"),
                        nombre = rec["nombre"] ?: "", rnc = rec["rnc"] ?: "",
                        telefono = rec["telefono"] ?: "", email = rec["email"] ?: "",
                        direccion = rec["direccion"] ?: "", contacto = rec["contacto"] ?: "",
                        rubro = rec["rubro"] ?: "", createdAt = parseLong(rec["created_at"])
                    )
                    if (existing != null) ProveedoresManager.update(prov)
                    else ProveedoresManager.add(prov)
                    count++
                }
            }
            "usuarios" -> {
                records.forEach { rec ->
                    val id = rec["usuario"] ?: rec["id"] ?: return@forEach
                    val existing = UsuariosManager.usuarios.find { it.id == id || it.uid == id }
                    val name = rec["nombre"] ?: rec["name"] ?: ""
                    val pin = rec["pin"] ?: rec["password"] ?: ""
                    val roleStr = rec["rol"] ?: rec["role"] ?: ""
                    val uid = rec["uid"] ?: genUid("usr")
                    if (existing != null) {
                        val updated = existing.copy(
                            name = name,
                            pin = pin
                        )
                        UsuariosManager.update(updated)
                    } else {
                        UsuariosManager.add(Usuario(
                            id = id,
                            name = name,
                            pin = pin,
                            uid = uid,
                            role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.CAJERO },
                            createdAt = parseLong(rec["created_at"])
                        ))
                    }
                    count++
                }
            }
            "categorias" -> {
                val existingCats = AppPersistence.loadCategories() ?: emptyList()
                var cats = existingCats.toMutableList()
                records.forEach { rec ->
                    val uid = rec["uid"] ?: return@forEach
                    val existing = cats.find { it.uid == uid }
                    val cat = Category(
                        uid = uid, id = existing?.id ?: (cats.maxOfOrNull { it.id } ?: 0) + 1,
                        name = rec["nombre"] ?: "", description = rec["descripcion"] ?: "",
                        colorType = try { CategoryColorType.valueOf(rec["color"] ?: "Gray") } catch (_: Exception) { CategoryColorType.Gray },
                        active = rec["activo"] != "0", order = parseInt(rec["orden"])
                    )
                    if (existing != null) {
                        val idx = cats.indexOfFirst { it.uid == uid }
                        if (idx >= 0) cats[idx] = cat
                    } else {
                        cats.add(cat)
                    }
                    count++
                }
                AppPersistence.saveCategories(cats)
            }
            "facturas" -> {
                records.forEach { rec ->
                    val invoiceNumber = rec["no_factura"] ?: rec["ncf"] ?: return@forEach
                    if (InvoiceHistory.invoices.none { it.invoiceNumber == invoiceNumber || it.ncf == invoiceNumber }) {
                        val total = parseDouble(rec["total"])
                        val invoice = PaymentResult(
                            invoiceNumber = invoiceNumber,
                            ncf = rec["ncf"] ?: "",
                            total = total,
                            subtotalPreTax = parseDouble(rec["subtotal"]),
                            taxAmount = parseDouble(rec["impuesto"]),
                            paymentMethod = rec["metodo_pago"] ?: "",
                            receivedAmount = total,
                            change = 0.0,
                            note = rec["nota"] ?: "",
                            items = emptyList(),
                            turnoId = rec["turno_id"] ?: "",
                            timestamp = parseLong(rec["created_at"]),
                            discountAmount = parseDouble(rec["descuento"]),
                            tipAmount = parseDouble(rec["propina"])
                        )
                        InvoiceHistory.add(invoice)
                        count++
                    }
                }
            }
            "extras" -> {
                records.forEach { rec ->
                    val uid = rec["uid"] ?: return@forEach
                    if (ExtrasManager.extras.none { it.uid == uid }) {
                        val extra = Extra(
                            uid = uid, id = genUid("ext"),
                            name = rec["nombre"] ?: "", price = parseDouble(rec["precio"]),
                            productId = parseInt(rec["producto_id"])
                        )
                        ExtrasManager.add(extra)
                        count++
                    }
                }
            }
            "gastos" -> {
                records.forEach { rec ->
                    val uid = rec["uid"] ?: return@forEach
                    val gasto = Gasto(
                        id = uid, description = rec["comentario"] ?: "",
                        amount = parseDouble(rec["cantidad"]),
                        userId = rec["turno_id"] ?: "", userName = "",
                        createdAt = parseLong(rec["created_at"])
                    )
                    val existing = TurnoManager.gastosMap[gasto.userId]?.toMutableList() ?: mutableListOf()
                    existing.add(gasto)
                    TurnoManager.gastosMap = TurnoManager.gastosMap + (gasto.userId to existing)
                    count++
                }
                TurnoPersistence.save()
            }
        }
        }
        return count
    }

    private fun parseDouble(value: String?): Double = value?.toDoubleOrNull() ?: 0.0
    private fun parseInt(value: String?): Int = value?.toIntOrNull() ?: 0
    private fun parseLong(value: String?): Long = value?.toLongOrNull() ?: System.currentTimeMillis()

    private fun extractJson(json: String, key: String): String? {
        val regex = "\"$key\"\\s*:\\s*\"?([^\",}]+)\"?".toRegex()
        return regex.find(json)?.groupValues?.getOrNull(1)?.trim('"')
    }

    private fun getTableSchemas(): Map<String, List<ColumnDef>> = mapOf(
        "productos" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("nombre", "TEXT"), ColumnDef("codigo", "TEXT"),
            ColumnDef("barcode", "TEXT"), ColumnDef("categoria", "TEXT"), ColumnDef("descripcion", "TEXT"),
            ColumnDef("precio", "REAL"), ColumnDef("costo", "REAL"), ColumnDef("impuesto", "REAL"),
            ColumnDef("stock", "INTEGER"), ColumnDef("alerta", "INTEGER"), ColumnDef("activo", "INTEGER"),
            ColumnDef("enable_pos", "INTEGER"), ColumnDef("send_bar", "INTEGER"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "categorias" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("nombre", "TEXT"), ColumnDef("descripcion", "TEXT"),
            ColumnDef("color", "TEXT"), ColumnDef("activo", "INTEGER"), ColumnDef("orden", "INTEGER"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "clientes" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("nombre", "TEXT"), ColumnDef("rnc", "TEXT"),
            ColumnDef("telefono", "TEXT"), ColumnDef("email", "TEXT"), ColumnDef("direccion", "TEXT"),
            ColumnDef("tipo", "TEXT"), ColumnDef("limite_credito", "REAL"),
            ColumnDef("loyalty_points", "INTEGER"), ColumnDef("total_spent", "REAL"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "proveedores" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("nombre", "TEXT"), ColumnDef("rnc", "TEXT"),
            ColumnDef("telefono", "TEXT"), ColumnDef("email", "TEXT"), ColumnDef("direccion", "TEXT"),
            ColumnDef("contacto", "TEXT"), ColumnDef("rubro", "TEXT"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "usuarios" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("usuario", "TEXT"), ColumnDef("nombre", "TEXT"),
            ColumnDef("pin", "TEXT"), ColumnDef("rol", "TEXT"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "facturas" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("no_factura", "TEXT"), ColumnDef("ncf", "TEXT"),
            ColumnDef("total", "REAL"), ColumnDef("subtotal", "REAL"), ColumnDef("impuesto", "REAL"),
            ColumnDef("metodo_pago", "TEXT"), ColumnDef("descuento", "REAL"),
            ColumnDef("propina", "REAL"), ColumnDef("nota", "TEXT"),
            ColumnDef("cajero", "TEXT"), ColumnDef("turno_id", "TEXT"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "gastos" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("cantidad", "REAL"), ColumnDef("comentario", "TEXT"),
            ColumnDef("categoria", "TEXT"), ColumnDef("turno_id", "TEXT"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "extras" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("nombre", "TEXT"), ColumnDef("precio", "REAL"),
            ColumnDef("producto_id", "INTEGER"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "mesas" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("nombre", "TEXT"), ColumnDef("x_pos", "REAL"),
            ColumnDef("y_pos", "REAL"), ColumnDef("forma", "TEXT"),
            ColumnDef("ancho", "INTEGER"), ColumnDef("alto", "INTEGER"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "reservaciones" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("cliente_nombre", "TEXT"), ColumnDef("cliente_telefono", "TEXT"),
            ColumnDef("mesa_id", "INTEGER"), ColumnDef("fecha", "TEXT"), ColumnDef("hora", "TEXT"),
            ColumnDef("personas", "INTEGER"), ColumnDef("estado", "TEXT"), ColumnDef("nota", "TEXT"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "comandas" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("mesa", "TEXT"), ColumnDef("items", "TEXT"),
            ColumnDef("estado", "TEXT"), ColumnDef("area", "TEXT"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "recetas" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("producto_id", "INTEGER"),
            ColumnDef("producto_nombre", "TEXT"), ColumnDef("ingredientes", "TEXT"),
            ColumnDef("porciones", "INTEGER"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
        "pedidos_compra" to listOf(
            ColumnDef("uid", "TEXT"), ColumnDef("proveedor", "TEXT"), ColumnDef("items", "TEXT"),
            ColumnDef("total", "REAL"), ColumnDef("estado", "TEXT"), ColumnDef("nota", "TEXT"),
            ColumnDef("created_at", "DATETIME"), ColumnDef("updated_at", "DATETIME"),
        ),
    )

    private fun getTableData(tableName: String): List<Map<String, String>> = when (tableName) {
        "productos" -> (AppPersistence.loadProducts() ?: emptyList()).map { mapOf(
            "uid" to it.uid, "nombre" to it.name, "codigo" to it.code, "barcode" to it.barcode,
            "categoria" to it.category, "descripcion" to it.description,
            "precio" to it.price.toString(), "costo" to it.cost.toString(),
            "impuesto" to it.taxPercent.toString(), "stock" to it.stock.toString(),
            "alerta" to it.stockAlert.toString(), "activo" to (if (it.active) 1 else 0).toString(),
            "enable_pos" to (if (it.sellInPos) 1 else 0).toString(),
            "send_bar" to (if (it.sendToBar) 1 else 0).toString(),
            "created_at" to it.createdAt.toString(), "updated_at" to it.updatedAt.toString(),
        ) }
        "categorias" -> (AppPersistence.loadCategories() ?: emptyList()).map { mapOf(
            "uid" to it.uid, "nombre" to it.name, "descripcion" to it.description,
            "color" to it.colorType.name, "activo" to (if (it.active) 1 else 0).toString(),
            "orden" to it.order.toString(),
            "created_at" to it.createdAt.toString(), "updated_at" to it.updatedAt.toString(),
        ) }
        "clientes" -> ClientesManager.clientes.map { mapOf(
            "uid" to it.uid, "nombre" to it.nombre, "rnc" to it.rnc, "telefono" to it.telefono,
            "email" to it.email, "direccion" to it.direccion, "tipo" to it.tipo,
            "limite_credito" to it.limiteCredito.toString(),
            "loyalty_points" to it.loyaltyPoints.toString(), "total_spent" to it.totalSpent.toString(),
            "created_at" to it.createdAt.toString(), "updated_at" to (it.createdAt).toString(),
        ) }
        "proveedores" -> ProveedoresManager.proveedores.map { mapOf(
            "uid" to it.uid, "nombre" to it.nombre, "rnc" to it.rnc, "telefono" to it.telefono,
            "email" to it.email, "direccion" to it.direccion, "contacto" to it.contacto,
            "rubro" to it.rubro,
            "created_at" to it.createdAt.toString(), "updated_at" to (it.createdAt).toString(),
        ) }
        "usuarios" -> UsuariosManager.usuarios.map { mapOf(
            "uid" to it.uid, "usuario" to it.id, "nombre" to it.name,
            "pin" to "", "rol" to it.role.name,
            "created_at" to it.createdAt.toString(), "updated_at" to (it.createdAt).toString(),
        ) }
        "facturas" -> InvoiceHistory.invoices.map { mapOf(
            "uid" to it.ncf.ifBlank { it.invoiceNumber }, "no_factura" to it.invoiceNumber,
            "ncf" to it.ncf, "total" to it.total.toString(), "subtotal" to it.subtotalPreTax.toString(),
            "impuesto" to it.taxAmount.toString(), "metodo_pago" to it.paymentMethod,
            "descuento" to it.discountAmount.toString(), "propina" to it.tipAmount.toString(),
            "nota" to it.note, "cajero" to (TurnoManager.currentUser?.name ?: ""),
            "turno_id" to it.turnoId,
            "created_at" to it.timestamp.toString(), "updated_at" to it.timestamp.toString(),
        ) }
        "gastos" -> TurnoManager.gastosMap.values.flatten().map { gasto -> mapOf(
            "uid" to gasto.id, "cantidad" to gasto.amount.toString(),
            "comentario" to gasto.description, "categoria" to "",
            "turno_id" to gasto.userId,
            "created_at" to gasto.createdAt.toString(), "updated_at" to gasto.createdAt.toString(),
        ) }
        "extras" -> ExtrasManager.extras.map { mapOf(
            "uid" to it.uid, "nombre" to it.name, "precio" to it.price.toString(),
            "producto_id" to it.productId.toString(),
            "created_at" to it.createdAt.toString(), "updated_at" to (it.createdAt).toString(),
        ) }
        "mesas" -> MesasManager.mesas.map { mapOf(
            "uid" to "mesa_${it.id}", "nombre" to it.name, "x_pos" to it.xPos.toString(),
            "y_pos" to it.yPos.toString(), "forma" to it.shape,
            "ancho" to it.tableWidth.toString(), "alto" to it.tableHeight.toString(),
            "created_at" to it.openedAt.toString(), "updated_at" to System.currentTimeMillis().toString(),
        ) }
        "reservaciones" -> ReservacionesManager.reservaciones.map { mapOf(
            "uid" to it.id, "cliente_nombre" to it.clienteNombre,
            "cliente_telefono" to it.clienteTelefono, "mesa_id" to (it.mesaId?.toString() ?: ""),
            "fecha" to it.fecha, "hora" to it.hora, "personas" to it.clientePersonas.toString(),
            "estado" to it.estado, "nota" to it.notas,
            "created_at" to it.createdAt.toString(), "updated_at" to System.currentTimeMillis().toString(),
        ) }
        "comandas" -> ComandasManager.activeComandas.map { mapOf(
            "uid" to it.id, "mesa" to it.mesaName,
            "items" to it.items.joinToString(";") { i -> "${i.productName}|${i.quantity}|${i.notes}|${i.courseType}" },
            "estado" to it.status.name, "area" to it.area,
            "created_at" to it.createdAt.toString(), "updated_at" to it.updatedAt.toString(),
        ) }
        "recetas" -> RecipeManager.recipes.map { mapOf(
            "uid" to it.id, "producto_id" to it.productId.toString(),
            "producto_nombre" to it.productName,
            "ingredientes" to it.ingredients.joinToString(";") { i -> "${i.productId},${i.productName},${i.quantity},${i.unit}" },
            "porciones" to it.servings.toString(),
            "created_at" to it.createdAt.toString(), "updated_at" to it.updatedAt.toString(),
        ) }
        "pedidos_compra" -> PurchaseOrderManager.orders.map { mapOf(
            "uid" to it.id, "proveedor" to it.providerName,
            "items" to it.items.joinToString(";") { i -> "${i.productId},${i.productName},${i.quantity},${i.unitPrice}" },
            "total" to it.total.toString(), "estado" to it.status, "nota" to it.notes,
            "created_at" to it.createdAt.toString(), "updated_at" to it.updatedAt.toString(),
        ) }
        else -> emptyList()
    }
}
