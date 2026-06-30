package com.tmrestaurant.wifi

import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.settings.AppSettings

class PosApiHandler {
    fun route(req: HttpRequest): HttpResponse {
        return when {
            req.path == "/" || req.path == "/index.html" -> serveWebApp()
            req.path == "/api/pos/products" && req.method == "GET" -> getProducts(req)
            req.path == "/api/pos/categories" && req.method == "GET" -> getCategories()
            req.path == "/api/pos/checkout" && req.method == "POST" -> checkout(req.body)
            req.path == "/api/pos/invoice/last" && req.method == "GET" -> getLastInvoice()
            req.path.startsWith("/api/img/") && req.method == "GET" -> serveImage(req.path.removePrefix("/api/img/"))
            else -> HttpResponse(404, """{"error":"Not found"}""")
        }
    }

    private fun serveWebApp(): HttpResponse {
        val html = WebPosContent.generate()
        return HttpResponse(200, html, "text/html; charset=utf-8")
    }

    private fun getCategories(): HttpResponse {
        val cats = AppPersistence.loadCategories()
            ?.filter { it.active && it.visiblePos && it.name.isNotBlank() }
            ?: emptyList()
        val json = buildString {
            append("[")
            append(cats.joinToString(",") { """{"id":${it.id},"nombre":"${esc(it.name)}"}""" })
            append("]")
        }
        return HttpResponse(200, json)
    }

    private fun getProducts(req: HttpRequest): HttpResponse {
        val queryParams = parseQueryParams(req.path)
        val catFilter = queryParams["cat"]
        val searchFilter = queryParams["q"]?.lowercase()

        val products = AppPersistence.loadProducts()
            ?.filter { it.active && it.sellInPos }
            ?: emptyList()

        val filtered = products.filter { p ->
            val matchesCat = catFilter == null || catFilter.isBlank() || p.category == catFilter
            val matchesSearch = searchFilter == null || searchFilter.isBlank() ||
                    p.name.lowercase().contains(searchFilter) ||
                    p.code.lowercase().contains(searchFilter) ||
                    p.barcode.lowercase().contains(searchFilter)
            matchesCat && matchesSearch
        }

        val json = buildString {
            append("[")
            append(filtered.joinToString(",") { p ->
                val img = if (p.imagePath != null) """"/api/img/${esc(p.imagePath)}"""" else "null"
                """{"id":${p.id},"nombre":"${esc(p.name)}","precio":${p.price},"categoria":"${esc(p.category)}","codigo":"${esc(p.code)}","imagen":$img,"stock":${p.stock}}"""
            })
            append("]")
        }
        return HttpResponse(200, json)
    }

    private fun checkout(body: String): HttpResponse {
        val itemsJson = extractJsonArray(body, "items")
        if (itemsJson == null || itemsJson.isEmpty()) return HttpResponse(400, """{"error":"Carrito vacio"}""")

        val paymentMethod = extractJsonString(body, "pago") ?: "EFECTIVO"
        val customerName = extractJsonString(body, "cliente") ?: ""

        val allProducts = AppPersistence.loadProducts() ?: MockData.products
        val cartItems = itemsJson.mapNotNull { itemJson ->
            val id = extractJson(itemJson, "id")?.toIntOrNull() ?: return@mapNotNull null
            val qty = extractJson(itemJson, "cantidad")?.toIntOrNull() ?: 1
            val product = allProducts.find { it.id == id } ?: return@mapNotNull null
            CartItem(product = product, quantity = qty)
        }

        if (cartItems.isEmpty()) return HttpResponse(400, """{"error":"Productos no encontrados"}""")

        val total = cartItems.sumOf { it.product.price * it.quantity }
        val subtotalPreTax = total / 1.18
        val taxAmount = total - subtotalPreTax
        val ncf = NcfManager.getNextNcf()
        val turnoId = TurnoManager.currentTurno?.id ?: ""

        val invoice = PaymentResult(
            invoiceNumber = "W${System.currentTimeMillis().toString().takeLast(8)}",
            ncf = ncf,
            total = total,
            subtotalPreTax = subtotalPreTax,
            taxAmount = taxAmount,
            paymentMethod = paymentMethod,
            receivedAmount = total,
            change = 0.0,
            note = "",
            items = cartItems,
            turnoId = turnoId,
            customerName = customerName
        )

        val loaded = AppPersistence.loadProducts() ?: MockData.products
        val updatedProducts = loaded.toMutableList()
        for (item in cartItems) {
            if (item.product.controlInventory) {
                val idx = updatedProducts.indexOfFirst { it.id == item.product.id }
                if (idx >= 0) {
                    val p = updatedProducts[idx]
                    updatedProducts[idx] = p.copy(stock = (p.stock - item.quantity).coerceAtLeast(0))
                }
            }
        }
        AppPersistence.saveProducts(updatedProducts)

        InvoiceHistory.add(invoice)
        return HttpResponse(200, """{"ok":true,"factura":"${invoice.invoiceNumber}","total":$total,"ncf":"$ncf"}""")
    }

    private fun getLastInvoice(): HttpResponse {
        val invoices = InvoiceHistory.invoices.toList()
        if (invoices.isEmpty()) return HttpResponse(200, """{"ok":true,"facturas":[]}""")
        val json = buildString {
            append("""{"ok":true,"facturas":[""")
            append(invoices.takeLast(10).joinToString(",") { inv ->
                """{"numero":"${esc(inv.invoiceNumber)}","ncf":"${esc(inv.ncf)}","total":${inv.total},"pago":"${esc(inv.paymentMethod)}","items":${inv.items.size},"fecha":${inv.timestamp}}"""
            })
            append("]}")
        }
        return HttpResponse(200, json)
    }

    private fun serveImage(name: String): HttpResponse {
        val bytes = PersistentFiles.readBytes("img_$name")
        if (bytes == null) return HttpResponse(404, "Not Found", "text/plain")
        val ext = name.substringAfterLast(".", "").lowercase()
        val mime = when (ext) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
        return HttpResponse(200, "", mime)
    }

    private fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

    private fun parseQueryParams(path: String): Map<String, String> {
        val idx = path.indexOf("?")
        if (idx < 0) return emptyMap()
        return path.substring(idx + 1).split("&").mapNotNull { pair ->
            val eq = pair.indexOf("=")
            if (eq < 0) null else pair.substring(0, eq) to pair.substring(eq + 1).replace("%20", " ").replace("%22", "\"").replace("%27", "'").replace("%2C", ",").replace("%3D", "=")
        }.toMap()
    }

    private fun extractJson(json: String, key: String): String? {
        val regex = "\"$key\"\\s*:\\s*\"?([^\",}]+)\"?".toRegex()
        return regex.find(json)?.groupValues?.getOrNull(1)?.trim('"')
    }

    private fun extractJsonString(json: String, key: String): String? {
        val value = "\"$key\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"".toRegex()
            .find(json)?.groupValues?.getOrNull(1) ?: return null
        return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\"", "\"").replace("\\\\", "\\")
    }

    private fun extractJsonArray(json: String, key: String): List<String>? {
        val regex = "\"$key\"\\s*:\\s*\\[([^]]*)]".toRegex()
        val match = regex.find(json) ?: return null
        val content = match.groupValues.getOrNull(1) ?: return emptyList()
        if (content.isBlank()) return emptyList()
        val items = mutableListOf<String>()
        var depth = 0
        val current = StringBuilder()
        for (ch in content) {
            when {
                ch == '{' || ch == '[' -> { depth++; current.append(ch) }
                ch == '}' || ch == ']' -> { depth--; current.append(ch); if (depth == 0) { items.add(current.toString()); current.clear() } }
                ch == ',' && depth == 0 -> { items.add(current.toString()); current.clear() }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) items.add(current.toString())
        return items
    }
}
