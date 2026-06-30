package com.tmrestaurant.wifi

import com.tmrestaurant.ui.data.*

class MenuApiHandler {
    fun route(req: HttpRequest): HttpResponse {
        return when {
            req.path == "/" || req.path == "/index.html" -> serveWebApp()
            req.path == "/api/menu" && req.method == "GET" -> getMenu()
            req.path == "/api/mesas" && req.method == "GET" -> getMesas()
            req.path == "/api/comandas" && req.method == "GET" -> getComandas()
            req.path.startsWith("/api/mesa/") && req.path.endsWith("/items") && req.method == "GET" -> {
                val mesaId = req.path.removePrefix("/api/mesa/").removeSuffix("/items").toIntOrNull() ?: return error("ID invalido")
                getMesaItems(mesaId)
            }
            req.path.startsWith("/api/mesa/") && req.path.endsWith("/add") && req.method == "POST" -> {
                val mesaId = req.path.removePrefix("/api/mesa/").removeSuffix("/add").toIntOrNull() ?: return error("ID invalido")
                addToMesa(mesaId, req.body)
            }
            req.path.startsWith("/api/mesa/") && req.path.endsWith("/remove") && req.method == "POST" -> {
                val mesaId = req.path.removePrefix("/api/mesa/").removeSuffix("/remove").toIntOrNull() ?: return error("ID invalido")
                removeFromMesa(mesaId, req.body)
            }
            req.path.startsWith("/api/mesa/") && req.path.endsWith("/note") && req.method == "POST" -> {
                val mesaId = req.path.removePrefix("/api/mesa/").removeSuffix("/note").toIntOrNull() ?: return error("ID invalido")
                updateMesaNote(mesaId, req.body)
            }
            req.path.startsWith("/api/mesa/") && req.path.endsWith("/cobrar") && req.method == "POST" -> {
                val mesaId = req.path.removePrefix("/api/mesa/").removeSuffix("/cobrar").toIntOrNull() ?: return error("ID invalido")
                cobrarMesa(mesaId)
            }
            req.path.startsWith("/api/mesa/") && req.path.endsWith("/cocina") && req.method == "POST" -> {
                val mesaId = req.path.removePrefix("/api/mesa/").removeSuffix("/cocina").toIntOrNull() ?: return error("ID invalido")
                enviarMesaACocina(mesaId)
            }
            req.path.startsWith("/api/mesa/") && req.method == "GET" -> {
                val mesaId = req.path.removePrefix("/api/mesa/").toIntOrNull() ?: return error("ID invalido")
                getMesaItems(mesaId)
            }
            else -> HttpResponse(404, """{"error":"Not found"}""")
        }
    }

    private fun serveWebApp(): HttpResponse {
        val html = WebAppContent.generate()
        return HttpResponse(200, html, "text/html; charset=utf-8")
    }

    private fun getMenu(): HttpResponse {
        val products = loadProducts().filter { it.active && it.sellInPos }
        val cats = loadCategories().filter { it.active && it.visiblePos && it.name.isNotBlank() }
        println("[API] GET /api/menu -> ${products.size} products, ${cats.size} categories")
        cats.forEachIndexed { i, c -> println("[API]   cat[$i]: id=${c.id} name='${c.name}' active=${c.active} visible=${c.visiblePos}") }
        val json = buildString {
            append("{\"categorias\":[")
            if (cats.isNotEmpty()) {
                append(cats.joinToString(",") { """{"id":${it.id},"nombre":"${esc(it.name)}"}""" })
            }
            append("],\"productos\":[")
            if (products.isNotEmpty()) {
                append(products.joinToString(",") { p ->
                    val img = if (p.imagePath != null) """"/api/img/${esc(p.imagePath)}"""" else "\"\""
                    val desc = esc(p.description)
                    """{"id":${p.id},"nombre":"${esc(p.name)}","precio":${p.price},"categoria":"${esc(p.category)}","descripcion":"$desc","imagen":$img}"""
                })
            }
            append("]}")
        }
        println("[API] menu response (${json.length} chars)")
        return HttpResponse(200, json)
    }

    private fun getMesas(): HttpResponse {
        val mesas = MesasManager.mesas
        println("[API] GET /api/mesas -> ${mesas.size} mesas, turnoActivo=${TurnoManager.currentTurno?.id}")
        mesas.forEachIndexed { i, m ->
            println("[API]   mesa[$i]: id=${m.id} nombre=${m.name} ocupada=${m.isOccupied} items=${m.items.size}")
        }
        val json = buildString {
            append("[")
            append(mesas.joinToString(",") { mesa ->
                val comanda = latestComandaForMesa(mesa.name)
                val commandFields = if (comanda == null) {
                    ""","estadoComanda":null,"comandaId":null,"areaComanda":null"""
                } else {
                    ""","estadoComanda":"${statusLabel(comanda.status)}","comandaId":"${esc(comanda.id)}","areaComanda":"${esc(comanda.area)}""""
                }
                """{"id":${mesa.id},"nombre":"${esc(mesa.name)}","ocupada":${mesa.isOccupied},"items":${mesa.items.size}$commandFields}"""
            })
            append("]")
        }
        println("[API] response body (${json.length} chars): ${if (json.length > 200) json.take(200) + "..." else json}")
        return HttpResponse(200, json)
    }

    private fun getMesaItems(mesaId: Int): HttpResponse {
        val mesa = MesasManager.mesas.find { it.id == mesaId } ?: return error("Mesa no encontrada")
        val comanda = latestComandaForMesa(mesa.name)
        val json = buildString {
            append("{\"id\":${mesa.id},\"nombre\":\"${esc(mesa.name)}\",\"items\":[")
            append(mesa.items.joinToString(",") { item ->
                val total = (item.product.price + item.extrasCost / item.quantity.coerceAtLeast(1)) * item.quantity
                """{"productoId":${item.product.id},"producto":"${esc(item.product.name)}","nota":"${esc(item.extrasNote)}","cantidad":${item.quantity},"precio":${item.product.price},"extras":${item.extrasCost},"total":$total}"""
            })
            append("],\"total\":${mesa.items.sumOf { (it.product.price * it.quantity) + it.extrasCost }}")
            if (comanda == null) {
                append(""","estadoComanda":null,"comandaId":null,"areaComanda":null}""")
            } else {
                append(""","estadoComanda":"${statusLabel(comanda.status)}","comandaId":"${esc(comanda.id)}","areaComanda":"${esc(comanda.area)}"}""")
            }
        }
        return HttpResponse(200, json)
    }

    private fun getComandas(): HttpResponse {
        val turnoId = TurnoManager.currentTurno?.id
        val comandas = ComandasManager.activeComandas
            .filter { turnoId == null || it.turnoId == turnoId }
            .sortedByDescending { maxOf(it.createdAt, it.updatedAt) }
        val json = buildString {
            append("[")
            append(comandas.joinToString(",") { comanda ->
                val items = comanda.items.joinToString(",") { item ->
                    """{"producto":"${esc(item.productName)}","cantidad":${item.quantity},"nota":"${esc(item.notes)}"}"""
                }
                """{"id":"${esc(comanda.id)}","mesa":"${esc(comanda.mesaName)}","estado":"${statusLabel(comanda.status)}","area":"${esc(comanda.area)}","creadaEn":${comanda.createdAt},"actualizadaEn":${comanda.updatedAt},"productos":[$items]}"""
            })
            append("]")
        }
        return HttpResponse(200, json)
    }

    private fun addToMesa(mesaId: Int, body: String): HttpResponse {
        val productId = extractJson(body, "productoId")?.toIntOrNull() ?: return error("productoId requerido")
        val quantity = extractJson(body, "cantidad")?.toIntOrNull() ?: 1
        val products = loadProducts()
        val product = products.find { it.id == productId } ?: return error("Producto no encontrado")
        WebCheckoutManager.cancelForMesa(mesaId)
        MesasManager.addProductToMesa(mesaId, product, quantity)
        return HttpResponse(200, """{"ok":true,"mensaje":"Agregado a mesa"}""")
    }

    private fun removeFromMesa(mesaId: Int, body: String): HttpResponse {
        val productId = extractJson(body, "productoId")?.toIntOrNull() ?: return error("productoId requerido")
        val mesa = MesasManager.mesas.find { it.id == mesaId } ?: return error("Mesa no encontrada")
        if (mesa.items.none { it.product.id == productId }) return error("Producto no encontrado en la mesa")
        WebCheckoutManager.cancelForMesa(mesaId)
        MesasManager.removeProductFromMesa(mesaId, productId)
        return HttpResponse(200, """{"ok":true,"mensaje":"Producto eliminado"}""")
    }

    private fun updateMesaNote(mesaId: Int, body: String): HttpResponse {
        val productId = extractJson(body, "productoId")?.toIntOrNull() ?: return error("productoId requerido")
        val note = extractJsonString(body, "nota")?.trim()?.take(200) ?: return error("nota requerida")
        val mesa = MesasManager.mesas.find { it.id == mesaId } ?: return error("Mesa no encontrada")
        if (mesa.items.none { it.product.id == productId }) return error("Producto no encontrado en la mesa")
        WebCheckoutManager.cancelForMesa(mesaId)
        MesasManager.updateProductNote(mesaId, productId, note)
        return HttpResponse(200, """{"ok":true,"mensaje":"Nota actualizada"}""")
    }

    private fun cobrarMesa(mesaId: Int): HttpResponse {
        val mesa = MesasManager.mesas.find { it.id == mesaId } ?: return error("Mesa no encontrada")
        if (mesa.items.isEmpty()) return error("Mesa vacia")
        val request = WebCheckoutManager.requestCheckout(mesa)
        return HttpResponse(
            200,
            """{"ok":true,"mensaje":"Solicitud enviada a caja","solicitudId":${request.id},"total":${request.total}}"""
        )
    }

    private fun enviarMesaACocina(mesaId: Int): HttpResponse {
        val mesa = MesasManager.mesas.find { it.id == mesaId } ?: return error("Mesa no encontrada")
        if (mesa.items.isEmpty()) return error("Mesa vacia")
        val comanda = ComandasManager.enviarACocina(mesa.items, mesa.name)
        return HttpResponse(
            200,
            """{"ok":true,"mensaje":"Comanda enviada a ${esc(comanda.area)}","comandaId":"${esc(comanda.id)}","productos":${comanda.items.sumOf { it.quantity }}}"""
        )
    }

    private fun latestComandaForMesa(mesaName: String): Comanda? {
        val turnoId = TurnoManager.currentTurno?.id
        return ComandasManager.activeComandas
            .asSequence()
            .filter { it.mesaName == mesaName && (turnoId == null || it.turnoId == turnoId) }
            .maxByOrNull { maxOf(it.createdAt, it.updatedAt) }
    }

    private fun statusLabel(status: ComandaStatus): String = when (status) {
        ComandaStatus.Pendiente -> "Pendiente"
        ComandaStatus.EnPreparacion -> "En preparacion"
        ComandaStatus.Listo -> "Listo"
    }

    private fun error(msg: String) = HttpResponse(400, """{"error":"$msg"}""")

    private fun loadProducts(): List<Product> =
        AppPersistence.loadProducts() ?: MockData.products

    private fun loadCategories(): List<Category> =
        AppPersistence.loadCategories() ?: MockData.categories

    private fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

    private fun extractJson(json: String, key: String): String? {
        val regex = "\"$key\"\\s*:\\s*\"?([^\",}]+)\"?".toRegex()
        return regex.find(json)?.groupValues?.getOrNull(1)?.trim('"')
    }

    private fun extractJsonString(json: String, key: String): String? {
        val value = "\"$key\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"".toRegex()
            .find(json)?.groupValues?.getOrNull(1) ?: return null
        return value.replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }
}
