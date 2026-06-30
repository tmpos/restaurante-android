package com.tmrestaurant.wifi

import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.settings.AppSettings

class AdminApiHandler {
    fun route(req: HttpRequest): HttpResponse {
        val path = req.path
        val cleanPath = path.substringBefore("?")
        val method = req.method
        return when {
            cleanPath == "/" || cleanPath == "/index.html" || cleanPath == "/admin" -> serveWebApp()
            cleanPath == "/api/admin/dashboard" && method == "GET" -> dashboard()
            cleanPath == "/api/admin/products" && method == "GET" -> listProducts(req)
            cleanPath == "/api/admin/products" && method == "POST" -> createProduct(req.body)
            cleanPath.startsWith("/api/admin/products/") && method == "PUT" -> updateProduct(req)
            cleanPath.startsWith("/api/admin/products/") && method == "DELETE" -> deleteProduct(req)
            cleanPath == "/api/admin/categories" && method == "GET" -> listCategories()
            cleanPath == "/api/admin/categories" && method == "POST" -> createCategory(req.body)
            cleanPath.startsWith("/api/admin/categories/") && method == "PUT" -> updateCategory(req)
            cleanPath.startsWith("/api/admin/categories/") && method == "DELETE" -> deleteCategory(req)
            cleanPath == "/api/admin/invoices" && method == "GET" -> listInvoices(req)
            cleanPath.startsWith("/api/admin/invoices/") && method == "DELETE" -> deleteInvoice(req)
            cleanPath == "/api/admin/inventory" && method == "GET" -> getInventory()
            cleanPath == "/api/admin/inventory/adjust" && method == "POST" -> adjustInventory(req.body)
            cleanPath == "/api/admin/comandas" && method == "GET" -> getComandas()
            cleanPath.startsWith("/api/admin/comandas/") && method == "PUT" -> updateComandaStatus(req)
            cleanPath == "/api/admin/clientes" && method == "GET" -> getClientes()
            cleanPath == "/api/admin/clientes" && method == "POST" -> createCliente(req.body)
            cleanPath.startsWith("/api/admin/clientes/") && method == "DELETE" -> deleteCliente(req)
            cleanPath == "/api/admin/turnos" && method == "GET" -> getTurnos()
            cleanPath == "/api/admin/turnos" && method == "POST" -> crearTurno(req.body)
            cleanPath.startsWith("/api/admin/settings") && method == "GET" -> getSettings()
            cleanPath.startsWith("/api/admin/settings") && method == "POST" -> updateSettings(req.body)
            cleanPath.startsWith("/api/img/") && method == "GET" -> serveImage(cleanPath.removePrefix("/api/img/"))
            cleanPath == "/api/admin/users" && method == "GET" -> getUsers()
            cleanPath == "/api/admin/users" && method == "POST" -> createUser(req.body)
            cleanPath.startsWith("/api/admin/users/") && method == "PUT" -> updateUser(req)
            cleanPath.startsWith("/api/admin/users/") && method == "DELETE" -> deleteUser(req)
            else -> HttpResponse(404, """{"error":"Not found"}""")
        }
    }

    private fun serveWebApp(): HttpResponse {
        return HttpResponse(200, WebAdminContent.generate(), "text/html; charset=utf-8")
    }

    private fun dashboard(): HttpResponse {
        val products = AppPersistence.loadProducts() ?: MockData.products
        val invoices = InvoiceHistory.invoices.toList()
        val totalVendido = invoices.sumOf { it.total }
        val totalFacturas = invoices.size
        val totalProductos = products.size
        val totalActivos = products.count { it.active }
        val bajosStock = products.count { it.controlInventory && it.stock <= it.stockAlert }
        val totalClientes = ClientesManager.clientes.size
        val turnoActivo = TurnoManager.currentTurno != null
        val turnoName = TurnoManager.currentTurno?.userName ?: ""
        val turnoId = TurnoManager.currentTurno?.userId ?: ""
        return HttpResponse(200, """{
            "ok":true,
            "totalProductos":$totalProductos,
            "totalActivos":$totalActivos,
            "bajosStock":$bajosStock,
            "totalFacturas":$totalFacturas,
            "totalVendido":$totalVendido,
            "totalClientes":$totalClientes,
            "turnoActivo":$turnoActivo,
            "turnoNombre":"${esc(turnoName)}",
            "turnoCajero":"${esc(turnoId)}",
            "turnoInicio":${TurnoManager.currentTurno?.startTime ?: 0}
        }""")
    }

    private fun listProducts(req: HttpRequest): HttpResponse {
        val params = parseQueryParams(req.path)
        val search = params["q"]?.lowercase()
        val cat = params["cat"]
        val active = params["active"]
        val products = AppPersistence.loadProducts() ?: MockData.products
        val filtered = products.filter { p ->
            val matchesSearch = search == null || p.name.lowercase().contains(search) || p.code.lowercase().contains(search) || p.barcode.lowercase().contains(search)
            val matchesCat = cat == null || cat.isBlank() || p.category == cat
            val matchesActive = when (active) { "true" -> p.active; "false" -> !p.active; else -> true }
            matchesSearch && matchesCat && matchesActive
        }
        return HttpResponse(200, buildString {
            append("[")
            append(filtered.joinToString(",") { p ->
                """{"id":${p.id},"nombre":"${esc(p.name)}","codigo":"${esc(p.code)}","codigoBarra":"${esc(p.barcode)}","categoria":"${esc(p.category)}","descripcion":"${esc(p.description)}","precio":${p.price},"costo":${p.cost},"itbis":${p.taxPercent},"stock":${p.stock},"alertaStock":${p.stockAlert},"activo":${p.active},"venderPos":${p.sellInPos},"cocina":${p.sendToKitchen},"bar":${p.sendToBar},"favorito":${p.favorite},"controlInventario":${p.controlInventory},"venderPorPeso":${p.sellByWeight},"imagen":"${esc(p.imagePath ?: "")}","uid":"${esc(p.uid)}","creado":${p.createdAt},"actualizado":${p.updatedAt}}"""
            })
            append("]")
        })
    }

    private fun createProduct(body: String): HttpResponse {
        val products = AppPersistence.loadProducts() ?: MockData.products
        val maxId = (products.maxOfOrNull { it.id } ?: 0) + 1
        val now = System.currentTimeMillis()
        val p = Product(
            id = maxId,
            name = extractJsonString(body, "nombre") ?: "",
            code = extractJsonString(body, "codigo") ?: "",
            barcode = extractJsonString(body, "codigoBarra") ?: "",
            category = extractJsonString(body, "categoria") ?: "",
            description = extractJsonString(body, "descripcion") ?: "",
            price = extractJson(body, "precio")?.toDoubleOrNull() ?: 0.0,
            cost = extractJson(body, "costo")?.toDoubleOrNull() ?: 0.0,
            taxPercent = extractJson(body, "itbis")?.toDoubleOrNull() ?: 18.0,
            stock = extractJson(body, "stock")?.toIntOrNull() ?: 0,
            stockAlert = extractJson(body, "alertaStock")?.toIntOrNull() ?: 1,
            imagePath = null,
            active = extractJson(body, "activo")?.toBooleanStrictOrNull() ?: true,
            sellInPos = extractJson(body, "venderPos")?.toBooleanStrictOrNull() ?: true,
            sendToKitchen = extractJson(body, "cocina")?.toBooleanStrictOrNull() ?: true,
            sendToBar = extractJson(body, "bar")?.toBooleanStrictOrNull() ?: false,
            favorite = extractJson(body, "favorito")?.toBooleanStrictOrNull() ?: false,
            controlInventory = extractJson(body, "controlInventario")?.toBooleanStrictOrNull() ?: true,
            sellByWeight = extractJson(body, "venderPorPeso")?.toBooleanStrictOrNull() ?: false,
            uid = genUid("prod"),
            createdAt = now,
            updatedAt = now
        )
        AppPersistence.saveProducts(products + p)
        return HttpResponse(200, """{"ok":true,"id":${p.id}}""")
    }

    private fun updateProduct(req: HttpRequest): HttpResponse {
        val id = req.path.removePrefix("/api/admin/products/").substringBefore("/").toIntOrNull() ?: return HttpResponse(400, """{"error":"ID invalido"}""")
        val products = AppPersistence.loadProducts() ?: MockData.products
        val idx = products.indexOfFirst { it.id == id }
        if (idx < 0) return HttpResponse(404, """{"error":"Producto no encontrado"}""")
        val existing = products[idx]
        val now = System.currentTimeMillis()
        val body = req.body
        val updated = existing.copy(
            name = extractJsonString(body, "nombre") ?: existing.name,
            code = extractJsonString(body, "codigo") ?: existing.code,
            barcode = extractJsonString(body, "codigoBarra") ?: existing.barcode,
            category = extractJsonString(body, "categoria") ?: existing.category,
            description = extractJsonString(body, "descripcion") ?: existing.description,
            price = extractJson(body, "precio")?.toDoubleOrNull() ?: existing.price,
            cost = extractJson(body, "costo")?.toDoubleOrNull() ?: existing.cost,
            taxPercent = extractJson(body, "itbis")?.toDoubleOrNull() ?: existing.taxPercent,
            stock = extractJson(body, "stock")?.toIntOrNull() ?: existing.stock,
            stockAlert = extractJson(body, "alertaStock")?.toIntOrNull() ?: existing.stockAlert,
            active = extractJson(body, "activo")?.toBooleanStrictOrNull() ?: existing.active,
            sellInPos = extractJson(body, "venderPos")?.toBooleanStrictOrNull() ?: existing.sellInPos,
            sendToKitchen = extractJson(body, "cocina")?.toBooleanStrictOrNull() ?: existing.sendToKitchen,
            sendToBar = extractJson(body, "bar")?.toBooleanStrictOrNull() ?: existing.sendToBar,
            favorite = extractJson(body, "favorito")?.toBooleanStrictOrNull() ?: existing.favorite,
            controlInventory = extractJson(body, "controlInventario")?.toBooleanStrictOrNull() ?: existing.controlInventory,
            sellByWeight = extractJson(body, "venderPorPeso")?.toBooleanStrictOrNull() ?: existing.sellByWeight,
            updatedAt = now
        )
        AppPersistence.saveProducts(products.mapIndexed { i, p -> if (i == idx) updated else p })
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun deleteProduct(req: HttpRequest): HttpResponse {
        val id = req.path.removePrefix("/api/admin/products/").toIntOrNull() ?: return HttpResponse(400, """{"error":"ID invalido"}""")
        val products = AppPersistence.loadProducts() ?: return HttpResponse(404, """{"error":"No products"}""")
        AppPersistence.saveProducts(products.filter { it.id != id })
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun listCategories(): HttpResponse {
        val cats = AppPersistence.loadCategories() ?: MockData.categories
        return HttpResponse(200, buildString {
            append("[")
            append(cats.joinToString(",") { c ->
                """{"id":${c.id},"nombre":"${esc(c.name)}","descripcion":"${esc(c.description)}","color":"${c.colorType.name}","activo":${c.active},"visiblePos":${c.visiblePos},"orden":${c.order},"uid":"${esc(c.uid)}"}"""
            })
            append("]")
        })
    }

    private fun createCategory(body: String): HttpResponse {
        val cats = AppPersistence.loadCategories() ?: MockData.categories
        val maxId = (cats.maxOfOrNull { it.id } ?: 0) + 1
        val now = System.currentTimeMillis()
        val c = Category(
            id = maxId,
            name = extractJsonString(body, "nombre") ?: "",
            description = extractJsonString(body, "descripcion") ?: "",
            colorType = try { CategoryColorType.valueOf(extractJsonString(body, "color") ?: "Gray") } catch (_: Exception) { CategoryColorType.Gray },
            active = extractJson(body, "activo")?.toBooleanStrictOrNull() ?: true,
            visiblePos = extractJson(body, "visiblePos")?.toBooleanStrictOrNull() ?: true,
            order = extractJson(body, "orden")?.toIntOrNull() ?: 0,
            uid = genUid("cat"),
            createdAt = now,
            updatedAt = now
        )
        AppPersistence.saveCategories(cats + c)
        return HttpResponse(200, """{"ok":true,"id":${c.id}}""")
    }

    private fun updateCategory(req: HttpRequest): HttpResponse {
        val id = req.path.removePrefix("/api/admin/categories/").substringBefore("/").toIntOrNull() ?: return HttpResponse(400, """{"error":"ID invalido"}""")
        val cats = AppPersistence.loadCategories() ?: MockData.categories
        val idx = cats.indexOfFirst { it.id == id }
        if (idx < 0) return HttpResponse(404, """{"error":"Categoria no encontrada"}""")
        val existing = cats[idx]
        val now = System.currentTimeMillis()
        val body = req.body
        val updated = existing.copy(
            name = extractJsonString(body, "nombre") ?: existing.name,
            description = extractJsonString(body, "descripcion") ?: existing.description,
            colorType = try { CategoryColorType.valueOf(extractJsonString(body, "color") ?: existing.colorType.name) } catch (_: Exception) { existing.colorType },
            active = extractJson(body, "activo")?.toBooleanStrictOrNull() ?: existing.active,
            visiblePos = extractJson(body, "visiblePos")?.toBooleanStrictOrNull() ?: existing.visiblePos,
            order = extractJson(body, "orden")?.toIntOrNull() ?: existing.order,
            updatedAt = now
        )
        AppPersistence.saveCategories(cats.mapIndexed { i, c -> if (i == idx) updated else c })
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun deleteCategory(req: HttpRequest): HttpResponse {
        val id = req.path.removePrefix("/api/admin/categories/").toIntOrNull() ?: return HttpResponse(400, """{"error":"ID invalido"}""")
        val cats = AppPersistence.loadCategories() ?: return HttpResponse(404, """{"error":"No categories"}""")
        AppPersistence.saveCategories(cats.filter { it.id != id })
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun listInvoices(req: HttpRequest): HttpResponse {
        val params = parseQueryParams(req.path)
        val search = params["q"]?.lowercase()
        val all = InvoiceHistory.invoices.toList()
        val filtered = if (search != null) all.filter {
            it.invoiceNumber.lowercase().contains(search) || it.customerName.lowercase().contains(search) || it.ncf.lowercase().contains(search)
        } else all
        val page = params["page"]?.toIntOrNull() ?: 1
        val limit = params["limit"]?.toIntOrNull() ?: 50
        val start = (page - 1) * limit
        val pageItems = filtered.drop(start).take(limit)
        return HttpResponse(200, buildString {
            append("""{"total":${filtered.size},"pagina":$page,"facturas":[""")
            append(pageItems.joinToString(",") { inv ->
                """{"numero":"${esc(inv.invoiceNumber)}","ncf":"${esc(inv.ncf)}","total":${inv.total},"pago":"${esc(inv.paymentMethod)}","cliente":"${esc(inv.customerName)}","items":${inv.items.size},"fecha":${inv.timestamp}}"""
            })
            append("]}")
        })
    }

    private fun deleteInvoice(req: HttpRequest): HttpResponse {
        val num = req.path.removePrefix("/api/admin/invoices/").trimEnd('/')
        val idx = InvoiceHistory.invoices.indexOfFirst { it.invoiceNumber == num }
        if (idx < 0) return HttpResponse(404, """{"error":"Factura no encontrada"}""")
        InvoiceHistory.removeAt(idx)
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun getInventory(): HttpResponse {
        val products = AppPersistence.loadProducts()?.filter { it.controlInventory } ?: emptyList()
        return HttpResponse(200, buildString {
            append("[")
            append(products.joinToString(",") { p ->
                """{"id":${p.id},"nombre":"${esc(p.name)}","codigo":"${esc(p.code)}","stock":${p.stock},"alerta":${p.stockAlert},"bajo":${p.stock <= p.stockAlert},"precio":${p.price},"costo":${p.cost}}"""
            })
            append("]")
        })
    }

    private fun adjustInventory(body: String): HttpResponse {
        val id = extractJson(body, "id")?.toIntOrNull() ?: return HttpResponse(400, """{"error":"ID requerido"}""")
        val delta = extractJson(body, "delta")?.toIntOrNull() ?: return HttpResponse(400, """{"error":"Delta requerido"}""")
        val reason = extractJsonString(body, "razon") ?: "Ajuste web"
        val products = AppPersistence.loadProducts() ?: return HttpResponse(404, """{"error":"No products"}""")
        val idx = products.indexOfFirst { it.id == id }
        if (idx < 0) return HttpResponse(404, """{"error":"Producto no encontrado"}""")
        val p = products[idx]
        val newStock = (p.stock + delta).coerceAtLeast(0)
        val now = System.currentTimeMillis()
        AppPersistence.saveProducts(products.mapIndexed { i, prod -> if (i == idx) prod.copy(stock = newStock, updatedAt = now) else prod })
        InventoryAdjustmentManager.log(id, p.name, p.stock, newStock, reason)
        return HttpResponse(200, """{"ok":true,"nuevoStock":$newStock}""")
    }

    private fun getComandas(): HttpResponse {
        val comandas = ComandasManager.activeComandas
        return HttpResponse(200, buildString {
            append("[")
            append(comandas.joinToString(",") { c ->
                """{"id":"${esc(c.id)}","mesa":"${esc(c.mesaName)}","area":"${esc(c.area)}","estado":"${esc(c.status.name)}","creado":${c.createdAt},"actualizado":${c.updatedAt},"turnoId":"${esc(c.turnoId)}","productos":[${c.items.joinToString(",") { i -> """{"nombre":"${esc(i.productName)}","cantidad":${i.quantity},"nota":"${esc(i.notes)}"}""" }}]}"""
            })
            append("]")
        })
    }

    private fun updateComandaStatus(req: HttpRequest): HttpResponse {
        val id = req.path.removePrefix("/api/admin/comandas/").substringBefore("/")
        val status = extractJsonString(req.body, "estado") ?: return HttpResponse(400, """{"error":"Estado requerido"}""")
        val newStatus = try { ComandaStatus.valueOf(status) } catch (_: Exception) { return HttpResponse(400, """{"error":"Estado invalido: Pendiente, EnPreparacion, Listo"}""") }
        ComandasManager.updateStatus(id, newStatus)
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun getClientes(): HttpResponse {
        val clientes = ClientesManager.clientes
        return HttpResponse(200, buildString {
            append("[")
            append(clientes.joinToString(",") { c ->
                """{"id":"${esc(c.id)}","nombre":"${esc(c.nombre)}","rnc":"${esc(c.rnc)}","telefono":"${esc(c.telefono)}","email":"${esc(c.email)}","direccion":"${esc(c.direccion)}","tipo":"${esc(c.tipo)}","limiteCredito":${c.limiteCredito}}"""
            })
            append("]")
        })
    }

    private fun createCliente(body: String): HttpResponse {
        val now = System.currentTimeMillis()
        val c = Cliente(
            id = "C${now.toString().takeLast(8)}",
            nombre = extractJsonString(body, "nombre") ?: "",
            rnc = extractJsonString(body, "rnc") ?: "",
            telefono = extractJsonString(body, "telefono") ?: "",
            email = extractJsonString(body, "email") ?: "",
            direccion = extractJsonString(body, "direccion") ?: "",
            tipo = extractJsonString(body, "tipo") ?: "Consumidor Final",
            limiteCredito = extractJson(body, "limiteCredito")?.toDoubleOrNull() ?: 0.0,
            uid = genUid("cli"),
            createdAt = now,
            updatedAt = now
        )
        SystemActionContext.runPrivileged { ClientesManager.add(c) }
        return HttpResponse(200, """{"ok":true,"id":"${esc(c.id)}"}""")
    }

    private fun deleteCliente(req: HttpRequest): HttpResponse {
        val id = req.path.removePrefix("/api/admin/clientes/").trimEnd('/')
        SystemActionContext.runPrivileged { ClientesManager.delete(id) }
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun getTurnos(): HttpResponse {
        val active = TurnoManager.activeTurnos.values.toList()
        val closed = TurnoManager.closedTurnos.toList()
        val all = active.map { t ->
            """{"id":"${esc(t.id)}","nombre":"${esc(t.userName)}","cajero":"${esc(t.userId)}","inicio":${t.startTime},"cierre":${null},"activo":true,"totalVentas":0,"totalEfectivo":0,"totalTarjeta":0}"""
        } + closed.map { t ->
            """{"id":"${esc(t.id)}","nombre":"${esc(t.userName)}","cajero":"${esc(t.userId)}","inicio":${t.startTime},"cierre":${t.endTime ?: 0},"activo":false,"totalVentas":0,"totalEfectivo":0,"totalTarjeta":0}"""
        }
        return HttpResponse(200, buildString {
            append("[")
            append(all.joinToString(","))
            append("]")
        })
    }

    private fun crearTurno(body: String): HttpResponse {
        TurnoManager.openTurno(0.0)
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun getSettings(): HttpResponse {
        val settings = AppPersistence.loadSettings() ?: AppSettings()
        return HttpResponse(200, """{"ok":true,"nombreEmpresa":"${esc(settings.company.businessName)}","rnc":"${esc(settings.company.rnc)}","telefono":"${esc(settings.company.phone)}","direccion":"${esc(settings.company.address)}","email":"${esc(settings.company.email)}","moneda":"${esc(settings.company.currency)}","itbis":"${esc(settings.company.taxPercent)}","propinaSugerida":"${esc(settings.company.suggestedTipPercent)}","modoOscuro":${settings.visual.themeMode == "dark"},"colorPrimario":"${esc(settings.visual.primaryColor)}"}""")
    }

    private fun updateSettings(body: String): HttpResponse {
        val s = AppPersistence.loadSettings() ?: AppSettings()
        val company = s.company.copy(
            businessName = extractJsonString(body, "nombreEmpresa") ?: s.company.businessName,
            rnc = extractJsonString(body, "rnc") ?: s.company.rnc,
            phone = extractJsonString(body, "telefono") ?: s.company.phone,
            address = extractJsonString(body, "direccion") ?: s.company.address,
            email = extractJsonString(body, "email") ?: s.company.email,
            currency = extractJsonString(body, "moneda") ?: s.company.currency,
            taxPercent = extractJsonString(body, "itbis") ?: s.company.taxPercent,
            suggestedTipPercent = extractJsonString(body, "propinaSugerida") ?: s.company.suggestedTipPercent
        )
        val darkStr = extractJson(body, "modoOscuro")
        val theme = if (darkStr != null) if (darkStr.toBooleanStrictOrNull() == true) "dark" else "light" else s.visual.themeMode
        val colorStr = extractJsonString(body, "colorPrimario")
        val visual = s.visual.copy(themeMode = theme, primaryColor = colorStr ?: s.visual.primaryColor)
        AppPersistence.saveSettings(s.copy(company = company, visual = visual))
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun getUsers(): HttpResponse {
        return HttpResponse(200, buildString {
            append("[")
            append(UsuariosManager.usuarios.joinToString(",") { u ->
                """{"id":"${esc(u.id)}","nombre":"${esc(u.name)}","rol":"${esc(u.role.name)}"}"""
            })
            append("]")
        })
    }

    private fun createUser(body: String): HttpResponse {
        val name = extractJsonString(body, "nombre") ?: return HttpResponse(400, """{"error":"Nombre requerido"}""")
        val role = extractJsonString(body, "rol") ?: "CAJERO"
        val pin = extractJsonString(body, "pin") ?: "0000"
        val roleEnum = try { UserRole.valueOf(role.uppercase()) } catch (_: Exception) { UserRole.CAJERO }
        val id = "U${System.currentTimeMillis().toString().takeLast(8)}"
        val usuario = Usuario(id = id, name = name, pin = pin, password = "", role = roleEnum, mustChangeCredentials = false)
        SystemActionContext.runPrivileged { UsuariosManager.add(usuario) }
        return HttpResponse(200, """{"ok":true,"id":"${esc(id)}"}""")
    }

    private fun updateUser(req: HttpRequest): HttpResponse {
        val id = req.path.removePrefix("/api/admin/users/").substringBefore("/")
        val existing = UsuariosManager.usuarios.firstOrNull { it.id == id }
            ?: return HttpResponse(404, """{"error":"Usuario no encontrado"}""")
        val updated = existing.copy(
            name = extractJsonString(req.body, "nombre") ?: existing.name,
            role = try { UserRole.valueOf((extractJsonString(req.body, "rol") ?: existing.role.name).uppercase()) } catch (_: Exception) { existing.role },
            pin = extractJsonString(req.body, "pin") ?: existing.pin,
            password = existing.password
        )
        SystemActionContext.runPrivileged { UsuariosManager.update(updated) }
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun deleteUser(req: HttpRequest): HttpResponse {
        val id = req.path.removePrefix("/api/admin/users/").trimEnd('/')
        SystemActionContext.runPrivileged { UsuariosManager.delete(id) }
        return HttpResponse(200, """{"ok":true}""")
    }

    private fun serveImage(name: String): HttpResponse {
        val bytes = PersistentFiles.readBytes("img_$name")
        if (bytes == null) return HttpResponse(404, "Not Found", "text/plain")
        val ext = name.substringAfterLast(".", "").lowercase()
        val mime = when (ext) { "png" -> "image/png"; "jpg", "jpeg" -> "image/jpeg"; "gif" -> "image/gif"; "webp" -> "image/webp"; else -> "application/octet-stream" }
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
        val value = "\"$key\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"".toRegex().find(json)?.groupValues?.getOrNull(1) ?: return null
        return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\"", "\"").replace("\\\\", "\\")
    }
}
