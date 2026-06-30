package com.tmrestaurant.ui.data

import com.tmrestaurant.db.DatabaseManager
import com.tmrestaurant.ui.data.settings.AdminCard
import com.tmrestaurant.ui.data.settings.AdminCardSettings
import com.tmrestaurant.ui.data.settings.AppSettings
import com.tmrestaurant.ui.data.settings.CompanySettings
import com.tmrestaurant.ui.data.settings.LicenseSettings
import com.tmrestaurant.ui.data.settings.NotificationSettings
import com.tmrestaurant.ui.data.settings.PaymentMethod
import com.tmrestaurant.ui.data.settings.PaymentMethodSettings
import com.tmrestaurant.ui.data.settings.PrintSettings
import com.tmrestaurant.ui.data.settings.SalesSettings
import com.tmrestaurant.ui.data.settings.ServerSettings
import com.tmrestaurant.ui.data.settings.SystemSettings
import com.tmrestaurant.ui.data.settings.VisualSettings

expect object PersistentFiles {
    fun readText(fileName: String): String?
    fun writeText(fileName: String, text: String)
    fun readBytes(fileName: String): ByteArray?
    fun writeBytes(fileName: String, bytes: ByteArray)
    fun listFiles(): List<String>
    fun deleteFile(fileName: String): Boolean
}

object AppPersistence {
    private const val PRODUCTS_FILE = "products.v1.tsv"
    private const val CATEGORIES_FILE = "categories.v1.tsv"
    private const val SETTINGS_FILE = "settings.v1.props"
    private const val COMPANY_LOGO_FILE = "company_logo.bin"

    private fun isDbReady(): Boolean = try {
        DatabaseManager.tableExists("company_settings")
    } catch (_: Exception) { false }

    fun loadCompanyLogo(): ByteArray? {
        if (isDbReady()) {
            try {
                val row = DatabaseManager.getSingleRow("company_settings")
                val bytes = row?.get("logo_bytes") as? ByteArray
                if (bytes != null && bytes.isNotEmpty()) return bytes
            } catch (_: Exception) { }
        }
        return PersistentFiles.readBytes(COMPANY_LOGO_FILE)?.takeIf { it.isNotEmpty() }
    }

    fun saveCompanyLogo(bytes: ByteArray) {
        if (isDbReady()) {
            try {
                DatabaseManager.update("company_settings", mapOf("logo_bytes" to bytes), "id = 1", emptyList())
            } catch (_: Exception) { }
        }
        PersistentFiles.writeBytes(COMPANY_LOGO_FILE, bytes)
    }

    fun loadProducts(): List<Product>? {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("products", whereClause = "1=1", orderBy = "id ASC") { it }
                if (rows.isNotEmpty()) {
                    return rows.map { rowToProduct(it) }
                }
            } catch (e: Exception) {
                println("loadProducts() DB error: $e")
            }
        }
        val fromFile = loadProductsFromFile()
        if (fromFile != null && fromFile.isNotEmpty() && isDbReady()) {
            saveProducts(fromFile)
        }
        return fromFile
    }

    fun saveProducts(products: List<Product>) {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("products")
                    products.forEach { p ->
                        DatabaseManager.insert("products", productToMap(p))
                    }
                }
            } catch (e: Exception) {
                println("saveProducts() DB error: $e")
            }
        }
        saveProductsToFile(products)
    }

    fun loadCategories(): List<Category>? {
        if (isDbReady()) {
            try {
                val rows = DatabaseManager.query("categories", whereClause = "1=1", orderBy = "id ASC") { it }
                if (rows.isNotEmpty()) {
                    return rows.map { rowToCategory(it) }
                }
            } catch (e: Exception) {
                println("loadCategories() DB error: $e")
            }
        }
        val fromFile = loadCategoriesFromFile()
        if (fromFile != null && fromFile.isNotEmpty() && isDbReady()) {
            saveCategories(fromFile)
        }
        return fromFile
    }

    fun saveCategories(categories: List<Category>) {
        if (isDbReady()) {
            try {
                DatabaseManager.transaction {
                    DatabaseManager.deleteAll("categories")
                    categories.forEach { c ->
                        DatabaseManager.insert("categories", categoryToMap(c))
                    }
                }
            } catch (e: Exception) {
                println("saveCategories() DB error: $e")
            }
        }
        saveCategoriesToFile(categories)
    }

    fun loadSettings(): AppSettings? {
        if (isDbReady()) {
            try {
                return loadSettingsFromDb()
            } catch (_: Exception) { }
        }
        return loadSettingsFromFile()
    }

    fun saveSettings(settings: AppSettings) {
        val now = System.currentTimeMillis()
        try {
            DatabaseManager.transaction {
                DatabaseManager.upsertSingleRow("company_settings", mapOf(
                    "business_name" to settings.company.businessName,
                    "rnc" to settings.company.rnc,
                    "phone" to settings.company.phone,
                    "email" to settings.company.email,
                    "currency" to settings.company.currency,
                    "address" to settings.company.address,
                    "tax_percent" to settings.company.taxPercent,
                    "suggested_tip_percent" to settings.company.suggestedTipPercent,
                    "logo_path" to settings.company.logoPath
                ))

                DatabaseManager.upsertSingleRow("visual_settings", mapOf(
                    "theme_mode" to settings.visual.themeMode,
                    "primary_color" to settings.visual.primaryColor
                ))

                DatabaseManager.upsertSingleRow("sales_settings", mapOf(
                    "tax_mode" to settings.sales.taxMode,
                    "tax_percent" to settings.sales.taxPercent,
                    "allow_discounts" to (if (settings.sales.allowDiscounts) 1 else 0),
                    "require_customer" to (if (settings.sales.requireCustomer) 1 else 0),
                    "allow_out_of_stock_sales" to (if (settings.sales.allowOutOfStockSales) 1 else 0),
                    "auto_send_to_kitchen" to (if (settings.sales.autoSendToKitchen) 1 else 0)
                ))

                DatabaseManager.hardDelete("payment_methods", "1=1", emptyList())
                settings.paymentMethods.methods.forEach { pm ->
                    DatabaseManager.insert("payment_methods", mapOf(
                        "id" to pm.id, "name" to pm.name,
                        "percentage" to pm.percentage,
                        "enabled" to (if (pm.enabled) 1 else 0)
                    ))
                }

                DatabaseManager.upsertSingleRow("print_settings", printToMap(settings.print))
                DatabaseManager.upsertSingleRow("notification_settings", notificationToMap(settings.notifications))
                DatabaseManager.upsertSingleRow("license_settings", licenseToMap(settings.license))
                DatabaseManager.upsertSingleRow("server_settings", serverToMap(settings.server))

                DatabaseManager.upsertSingleRow("system_settings", mapOf(
                    "local_server_ip" to settings.system.localServerIp,
                    "local_server_port" to settings.system.localServerPort,
                    "detected_ips" to settings.system.detectedIps.joinToString(","),
                    "available_url" to settings.system.availableUrl
                ))

                DatabaseManager.hardDelete("admin_cards", "1=1", emptyList())
                settings.adminCards.cards.forEach { card ->
                    DatabaseManager.insert("admin_cards", mapOf(
                        "user_id" to card.userId, "user_name" to card.userName,
                        "user_role" to card.userRole, "card_number" to card.cardNumber
                    ))
                }
            }
        } catch (_: Exception) { }

        saveSettingsToFile(settings)
    }

    // ─── DB LOADERS ─────────────────────────────────────────────────────────

    private fun loadSettingsFromDb(): AppSettings {
        val defaults = AppSettings()
        val company = mapRow("company_settings") ?: return loadSettingsFromFile() ?: defaults
        val visual = mapRow("visual_settings")
        val sales = mapRow("sales_settings")
        val paymentMethods = DatabaseManager.query("payment_methods") { it }
        val print = mapRow("print_settings")
        val notifications = mapRow("notification_settings")
        val license = mapRow("license_settings")
        val server = mapRow("server_settings")
        val system = mapRow("system_settings")
        val adminCards = DatabaseManager.query("admin_cards") { it }

        return defaults.copy(
            company = CompanySettings(
                businessName = stringVal(company, "business_name", defaults.company.businessName),
                rnc = stringVal(company, "rnc", defaults.company.rnc),
                phone = stringVal(company, "phone", defaults.company.phone),
                email = stringVal(company, "email", defaults.company.email),
                currency = stringVal(company, "currency", defaults.company.currency),
                address = stringVal(company, "address", defaults.company.address),
                taxPercent = stringVal(company, "tax_percent", defaults.company.taxPercent),
                suggestedTipPercent = stringVal(company, "suggested_tip_percent", defaults.company.suggestedTipPercent),
                logoPath = stringVal(company, "logo_path", null)
            ),
            visual = VisualSettings(
                themeMode = stringVal(visual, "theme_mode", defaults.visual.themeMode),
                primaryColor = stringVal(visual, "primary_color", defaults.visual.primaryColor)
            ),
            sales = SalesSettings(
                taxMode = stringVal(sales, "tax_mode", defaults.sales.taxMode),
                taxPercent = stringVal(sales, "tax_percent", defaults.sales.taxPercent),
                allowDiscounts = boolVal(sales, "allow_discounts", defaults.sales.allowDiscounts),
                requireCustomer = boolVal(sales, "require_customer", defaults.sales.requireCustomer),
                allowOutOfStockSales = boolVal(sales, "allow_out_of_stock_sales", defaults.sales.allowOutOfStockSales),
                autoSendToKitchen = boolVal(sales, "auto_send_to_kitchen", defaults.sales.autoSendToKitchen)
            ),
            paymentMethods = PaymentMethodSettings(
                methods = if (paymentMethods.isNotEmpty()) paymentMethods.map { row ->
                    PaymentMethod(
                        id = stringVal(row, "id", "pm_${kotlin.random.Random.nextInt()}"),
                        name = stringVal(row, "name", ""),
                        percentage = stringVal(row, "percentage", "0"),
                        enabled = boolVal(row, "enabled", true)
                    )
                } else defaults.paymentMethods.methods
            ),
            print = rowToPrint(print, defaults.print),
            notifications = rowToNotification(notifications, defaults.notifications),
            license = rowToLicense(license, defaults.license),
            server = rowToServer(server, defaults.server),
            system = SystemSettings(
                localServerIp = stringVal(system, "local_server_ip", defaults.system.localServerIp),
                localServerPort = stringVal(system, "local_server_port", defaults.system.localServerPort),
                detectedIps = stringVal(system, "detected_ips", "").split(",").filter { it.isNotBlank() }.ifEmpty { defaults.system.detectedIps },
                availableUrl = stringVal(system, "available_url", defaults.system.availableUrl),
                backups = defaults.system.backups
            ),
            adminCards = AdminCardSettings(
                cards = adminCards.map { row ->
                    AdminCard(
                        userId = stringVal(row, "user_id", ""),
                        userName = stringVal(row, "user_name", ""),
                        userRole = stringVal(row, "user_role", ""),
                        cardNumber = stringVal(row, "card_number", "")
                    )
                }
            )
        )
    }

    // ─── ROW HELPERS ────────────────────────────────────────────────────────

    private fun mapRow(table: String): Map<String, Any?>? = DatabaseManager.getSingleRow(table)

    private fun stringVal(row: Map<String, Any?>?, key: String, default: String?): String {
        if (row == null) return default ?: ""
        val v = row[key] as? String
        return if (v.isNullOrBlank()) (default ?: "") else v
    }

    private fun boolVal(row: Map<String, Any?>?, key: String, default: Boolean): Boolean {
        if (row == null) return default
        val v = row[key]
        return when (v) {
            is Long -> v == 1L
            is Boolean -> v
            is String -> v.toBooleanStrictOrNull() ?: default
            else -> default
        }
    }

    private fun intVal(row: Map<String, Any?>?, key: String, default: Int): Int {
        if (row == null) return default
        val v = row[key]
        return when (v) {
            is Long -> v.toInt()
            is Int -> v
            is String -> v.toIntOrNull() ?: default
            else -> default
        }
    }

    // ─── ROW → SETTINGS ─────────────────────────────────────────────────────

    private fun rowToPrint(row: Map<String, Any?>?, defaults: PrintSettings): PrintSettings {
        if (row == null) return defaults
        return defaults.copy(
            selectedPrinter = stringVal(row, "selected_printer", defaults.selectedPrinter),
            searchType = stringVal(row, "search_type", defaults.searchType),
            paperWidthMm = stringVal(row, "paper_width_mm", defaults.paperWidthMm),
            copies = intVal(row, "copies", defaults.copies).coerceIn(1, 5),
            textSize = stringVal(row, "text_size", defaults.textSize),
            marginLeft = stringVal(row, "margin_left", defaults.marginLeft),
            marginRight = stringVal(row, "margin_right", defaults.marginRight),
            marginTop = stringVal(row, "margin_top", defaults.marginTop),
            marginBottom = stringVal(row, "margin_bottom", defaults.marginBottom),
            usefulWidthMm = stringVal(row, "useful_width_mm", defaults.usefulWidthMm),
            logoWidthMm = stringVal(row, "logo_width_mm", defaults.logoWidthMm),
            logoHeightMm = stringVal(row, "logo_height_mm", defaults.logoHeightMm),
            showCompanyLogo = boolVal(row, "show_company_logo", defaults.showCompanyLogo),
            showCompanyName = boolVal(row, "show_company_name", defaults.showCompanyName),
            showCompanyRnc = boolVal(row, "show_company_rnc", defaults.showCompanyRnc),
            showCompanyAddress = boolVal(row, "show_company_address", defaults.showCompanyAddress),
            showCompanyPhone = boolVal(row, "show_company_phone", defaults.showCompanyPhone),
            showCompanyEmail = boolVal(row, "show_company_email", defaults.showCompanyEmail),
            showCustomerName = boolVal(row, "show_customer_name", defaults.showCustomerName),
            showCustomerRnc = boolVal(row, "show_customer_rnc", defaults.showCustomerRnc),
            showCustomerPhone = boolVal(row, "show_customer_phone", defaults.showCustomerPhone),
            showCustomerEmail = boolVal(row, "show_customer_email", defaults.showCustomerEmail),
            showDateTime = boolVal(row, "show_date_time", defaults.showDateTime),
            showCashierName = boolVal(row, "show_cashier_name", defaults.showCashierName),
            showTaxes = boolVal(row, "show_taxes", defaults.showTaxes),
            showDiscounts = boolVal(row, "show_discounts", defaults.showDiscounts),
            showTip = boolVal(row, "show_tip", defaults.showTip),
            showPaymentMethod = boolVal(row, "show_payment_method", defaults.showPaymentMethod),
            showCashChange = boolVal(row, "show_cash_change", defaults.showCashChange),
            showThankYouMessage = boolVal(row, "show_thank_you_message", defaults.showThankYouMessage),
            showQr = boolVal(row, "show_qr", defaults.showQr),
            showReceiptNumber = boolVal(row, "show_receipt_number", defaults.showReceiptNumber),
            showNcf = boolVal(row, "show_ncf", defaults.showNcf),
            showNcfExpiry = boolVal(row, "show_ncf_expiry", defaults.showNcfExpiry),
            showInvoiceTitle = boolVal(row, "show_invoice_title", defaults.showInvoiceTitle),
            showItems = boolVal(row, "show_items", defaults.showItems),
            showSubtotal = boolVal(row, "show_subtotal", defaults.showSubtotal),
            showTotal = boolVal(row, "show_total", defaults.showTotal),
            showItemCount = boolVal(row, "show_item_count", defaults.showItemCount),
            showCashRegister = boolVal(row, "show_cash_register", defaults.showCashRegister),
            showNote = boolVal(row, "show_note", defaults.showNote),
            showTaxSummary = boolVal(row, "show_tax_summary", defaults.showTaxSummary),
            showReturnPolicy = boolVal(row, "show_return_policy", defaults.showReturnPolicy),
            showBarcode = boolVal(row, "show_barcode", defaults.showBarcode),
            showFooterDate = boolVal(row, "show_footer_date", defaults.showFooterDate),
            thankYouMessage = stringVal(row, "thank_you_message", defaults.thankYouMessage)
        )
    }

    private fun rowToNotification(row: Map<String, Any?>?, defaults: NotificationSettings): NotificationSettings {
        if (row == null) return defaults
        return defaults.copy(
            enabled = boolVal(row, "enabled", defaults.enabled),
            smtpServer = stringVal(row, "smtp_server", defaults.smtpServer),
            smtpPort = stringVal(row, "smtp_port", defaults.smtpPort),
            senderEmail = stringVal(row, "sender_email", defaults.senderEmail),
            appPassword = stringVal(row, "app_password", defaults.appPassword),
            senderName = stringVal(row, "sender_name", defaults.senderName),
            destinationEmails = stringVal(row, "destination_emails", defaults.destinationEmails),
            sslTls = boolVal(row, "ssl_tls", defaults.sslTls),
            sendOnLogout = boolVal(row, "send_on_logout", defaults.sendOnLogout),
            sendOnCashClose = boolVal(row, "send_on_cash_close", defaults.sendOnCashClose),
            sendOnCancelInvoice = boolVal(row, "send_on_cancel_invoice", defaults.sendOnCancelInvoice),
            sendOnDeleteInvoice = boolVal(row, "send_on_delete_invoice", defaults.sendOnDeleteInvoice)
        )
    }

    private fun rowToLicense(row: Map<String, Any?>?, defaults: LicenseSettings): LicenseSettings {
        if (row == null) return defaults
        return defaults.copy(
            status = stringVal(row, "status", defaults.status),
            companyName = stringVal(row, "company_name", defaults.companyName),
            lastCheck = stringVal(row, "last_check", defaults.lastCheck),
            nextCheck = stringVal(row, "next_check", defaults.nextCheck),
            offlineDaysRemaining = stringVal(row, "offline_days_remaining", defaults.offlineDaysRemaining),
            deviceCode = stringVal(row, "device_code", defaults.deviceCode),
            checkIntervalMinutes = stringVal(row, "check_interval_minutes", defaults.checkIntervalMinutes)
        )
    }

    private fun rowToServer(row: Map<String, Any?>?, defaults: ServerSettings): ServerSettings {
        if (row == null) return defaults
        return defaults.copy(
            enabled = boolVal(row, "enabled", defaults.enabled),
            serverUrl = stringVal(row, "server_url", defaults.serverUrl),
            apiRoute = stringVal(row, "api_route", defaults.apiRoute),
            apiKey = stringVal(row, "api_key", defaults.apiKey),
            tokenConfigured = boolVal(row, "token_configured", defaults.tokenConfigured),
            syncInvoices = boolVal(row, "sync_invoices", defaults.syncInvoices),
            automaticSend = boolVal(row, "automatic_send", defaults.automaticSend),
            syncProducts = boolVal(row, "sync_products", defaults.syncProducts),
            syncCustomers = boolVal(row, "sync_customers", defaults.syncCustomers),
            syncCashClosings = boolVal(row, "sync_cash_closings", defaults.syncCashClosings),
            lastSync = stringVal(row, "last_sync", defaults.lastSync)
        )
    }

    // ─── SETTINGS → MAP ─────────────────────────────────────────────────────

    private fun printToMap(p: PrintSettings) = mapOf(
        "selected_printer" to p.selectedPrinter, "search_type" to p.searchType,
        "paper_width_mm" to p.paperWidthMm, "copies" to p.copies.coerceIn(1, 5),
        "text_size" to p.textSize, "margin_left" to p.marginLeft,
        "margin_right" to p.marginRight, "margin_top" to p.marginTop,
        "margin_bottom" to p.marginBottom, "useful_width_mm" to p.usefulWidthMm,
        "logo_width_mm" to p.logoWidthMm, "logo_height_mm" to p.logoHeightMm,
        "show_company_logo" to (if (p.showCompanyLogo) 1 else 0),
        "show_company_name" to (if (p.showCompanyName) 1 else 0),
        "show_company_rnc" to (if (p.showCompanyRnc) 1 else 0),
        "show_company_address" to (if (p.showCompanyAddress) 1 else 0),
        "show_company_phone" to (if (p.showCompanyPhone) 1 else 0),
        "show_company_email" to (if (p.showCompanyEmail) 1 else 0),
        "show_customer_name" to (if (p.showCustomerName) 1 else 0),
        "show_customer_rnc" to (if (p.showCustomerRnc) 1 else 0),
        "show_customer_phone" to (if (p.showCustomerPhone) 1 else 0),
        "show_customer_email" to (if (p.showCustomerEmail) 1 else 0),
        "show_date_time" to (if (p.showDateTime) 1 else 0),
        "show_cashier_name" to (if (p.showCashierName) 1 else 0),
        "show_taxes" to (if (p.showTaxes) 1 else 0),
        "show_discounts" to (if (p.showDiscounts) 1 else 0),
        "show_tip" to (if (p.showTip) 1 else 0),
        "show_payment_method" to (if (p.showPaymentMethod) 1 else 0),
        "show_cash_change" to (if (p.showCashChange) 1 else 0),
        "show_thank_you_message" to (if (p.showThankYouMessage) 1 else 0),
        "show_qr" to (if (p.showQr) 1 else 0),
        "show_receipt_number" to (if (p.showReceiptNumber) 1 else 0),
        "show_ncf" to (if (p.showNcf) 1 else 0),
        "show_ncf_expiry" to (if (p.showNcfExpiry) 1 else 0),
        "show_invoice_title" to (if (p.showInvoiceTitle) 1 else 0),
        "show_items" to (if (p.showItems) 1 else 0),
        "show_subtotal" to (if (p.showSubtotal) 1 else 0),
        "show_total" to (if (p.showTotal) 1 else 0),
        "show_item_count" to (if (p.showItemCount) 1 else 0),
        "show_cash_register" to (if (p.showCashRegister) 1 else 0),
        "show_note" to (if (p.showNote) 1 else 0),
        "show_tax_summary" to (if (p.showTaxSummary) 1 else 0),
        "show_return_policy" to (if (p.showReturnPolicy) 1 else 0),
        "show_barcode" to (if (p.showBarcode) 1 else 0),
        "show_footer_date" to (if (p.showFooterDate) 1 else 0),
        "thank_you_message" to p.thankYouMessage
    )

    private fun notificationToMap(n: NotificationSettings) = mapOf(
        "enabled" to (if (n.enabled) 1 else 0), "smtp_server" to n.smtpServer,
        "smtp_port" to n.smtpPort, "sender_email" to n.senderEmail,
        "app_password" to n.appPassword, "sender_name" to n.senderName,
        "destination_emails" to n.destinationEmails, "ssl_tls" to (if (n.sslTls) 1 else 0),
        "send_on_logout" to (if (n.sendOnLogout) 1 else 0),
        "send_on_cash_close" to (if (n.sendOnCashClose) 1 else 0),
        "send_on_cancel_invoice" to (if (n.sendOnCancelInvoice) 1 else 0),
        "send_on_delete_invoice" to (if (n.sendOnDeleteInvoice) 1 else 0)
    )

    private fun licenseToMap(l: LicenseSettings) = mapOf(
        "status" to l.status, "company_name" to l.companyName,
        "last_check" to l.lastCheck, "next_check" to l.nextCheck,
        "offline_days_remaining" to l.offlineDaysRemaining,
        "device_code" to l.deviceCode, "check_interval_minutes" to l.checkIntervalMinutes
    )

    private fun serverToMap(s: ServerSettings) = mapOf(
        "enabled" to (if (s.enabled) 1 else 0), "server_url" to s.serverUrl,
        "api_route" to s.apiRoute, "api_key" to s.apiKey,
        "token_configured" to (if (s.tokenConfigured) 1 else 0),
        "sync_invoices" to (if (s.syncInvoices) 1 else 0),
        "automatic_send" to (if (s.automaticSend) 1 else 0),
        "sync_products" to (if (s.syncProducts) 1 else 0),
        "sync_customers" to (if (s.syncCustomers) 1 else 0),
        "sync_cash_closings" to (if (s.syncCashClosings) 1 else 0),
        "last_sync" to s.lastSync
    )

    // ─── PRODUCT ROW MAPPERS ────────────────────────────────────────────────

    private fun rowToProduct(row: Map<String, Any?>): Product = Product(
        id = (row["id"] as? Long)?.toInt() ?: 0,
        name = row["name"] as? String ?: "",
        code = row["code"] as? String ?: "",
        barcode = row["barcode"] as? String ?: "",
        category = row["category"] as? String ?: "",
        description = row["description"] as? String ?: "",
        price = (row["price"] as? Double) ?: ((row["price"] as? Long)?.toDouble() ?: 0.0),
        cost = (row["cost"] as? Double) ?: ((row["cost"] as? Long)?.toDouble() ?: 0.0),
        taxPercent = (row["tax_percent"] as? Double) ?: ((row["tax_percent"] as? Long)?.toDouble() ?: 18.0),
        stock = (row["stock"] as? Long)?.toInt() ?: 0,
        stockAlert = (row["stock_alert"] as? Long)?.toInt() ?: 1,
        imagePath = row["image_path"] as? String,
        active = boolValRow(row, "active", true),
        sellInPos = boolValRow(row, "sell_in_pos", true),
        sendToKitchen = boolValRow(row, "send_to_kitchen", true),
        sendToBar = boolValRow(row, "send_to_bar", false),
        favorite = boolValRow(row, "favorite", false),
        controlInventory = boolValRow(row, "control_inventory", true),
        sellByWeight = boolValRow(row, "sell_by_weight", false),
        modifierGroupIds = row["modifier_group_ids"] as? String ?: "",
        uid = row["uid"] as? String ?: "",
        createdAt = (row["created_at"] as? Long) ?: 0L,
        updatedAt = (row["updated_at"] as? Long) ?: 0L
    )

    private fun productToMap(p: Product) = mapOf<String, Any?>(
        "id" to p.id, "name" to p.name, "code" to p.code,
        "barcode" to p.barcode, "category" to p.category,
        "description" to p.description, "price" to p.price,
        "cost" to p.cost, "tax_percent" to p.taxPercent,
        "stock" to p.stock, "stock_alert" to p.stockAlert,
        "image_path" to p.imagePath, "active" to (if (p.active) 1 else 0),
        "sell_in_pos" to (if (p.sellInPos) 1 else 0),
        "send_to_kitchen" to (if (p.sendToKitchen) 1 else 0),
        "send_to_bar" to (if (p.sendToBar) 1 else 0),
        "favorite" to (if (p.favorite) 1 else 0),
        "control_inventory" to (if (p.controlInventory) 1 else 0),
        "sell_by_weight" to (if (p.sellByWeight) 1 else 0),
        "modifier_group_ids" to p.modifierGroupIds,
        "uid" to p.uid, "created_at" to p.createdAt,
        "updated_at" to p.updatedAt
    )

    private fun rowToCategory(row: Map<String, Any?>): Category = Category(
        id = (row["id"] as? Long)?.toInt() ?: 0,
        name = row["name"] as? String ?: "",
        description = row["description"] as? String ?: "",
        colorType = CategoryColorType.entries.firstOrNull { it.name == row["color_type"] as? String } ?: CategoryColorType.Gray,
        active = boolValRow(row, "active", true),
        visiblePos = boolValRow(row, "visible_pos", true),
        order = (row["ord"] as? Long)?.toInt() ?: 0,
        uid = row["uid"] as? String ?: "",
        createdAt = (row["created_at"] as? Long) ?: 0L,
        updatedAt = (row["updated_at"] as? Long) ?: 0L
    )

    private fun categoryToMap(c: Category) = mapOf<String, Any?>(
        "id" to c.id, "name" to c.name, "description" to c.description,
        "color_type" to c.colorType.name, "active" to (if (c.active) 1 else 0),
        "visible_pos" to (if (c.visiblePos) 1 else 0), "ord" to c.order,
        "uid" to c.uid, "created_at" to c.createdAt, "updated_at" to c.updatedAt
    )

    private fun boolValRow(row: Map<String, Any?>, key: String, default: Boolean): Boolean {
        val v = row[key]
        return when (v) {
            is Long -> v == 1L
            is Boolean -> v
            is String -> v.toBooleanStrictOrNull() ?: default
            else -> default
        }
    }

    // ─── FALLBACK FILE LOADERS ──────────────────────────────────────────────

    private fun loadSettingsFromFile(): AppSettings? {
        val map = PersistentFiles.readText(SETTINGS_FILE)
            ?.lineSequence()
            ?.filter { it.contains("=") }
            ?.associate { line ->
                val idx = line.indexOf("=")
                line.substring(0, idx) to unescape(line.substring(idx + 1))
            }
            ?: return null
        val defaults = AppSettings()
        return defaults.copy(
            company = CompanySettings(
                businessName = map["company.businessName"] ?: defaults.company.businessName,
                rnc = map["company.rnc"] ?: defaults.company.rnc,
                phone = map["company.phone"] ?: defaults.company.phone,
                email = map["company.email"] ?: defaults.company.email,
                currency = map["company.currency"] ?: defaults.company.currency,
                address = map["company.address"] ?: defaults.company.address,
                taxPercent = map["company.taxPercent"] ?: defaults.company.taxPercent,
                suggestedTipPercent = map["company.suggestedTipPercent"] ?: defaults.company.suggestedTipPercent,
                logoPath = map["company.logoPath"]?.ifBlank { null }
            ),
            visual = VisualSettings(
                themeMode = map["visual.themeMode"] ?: defaults.visual.themeMode,
                primaryColor = map["visual.primaryColor"] ?: defaults.visual.primaryColor
            ),
            sales = SalesSettings(
                taxMode = map["sales.taxMode"] ?: defaults.sales.taxMode,
                taxPercent = map["sales.taxPercent"] ?: defaults.sales.taxPercent,
                allowDiscounts = map["sales.allowDiscounts"]?.toBooleanStrictOrNull() ?: defaults.sales.allowDiscounts,
                requireCustomer = map["sales.requireCustomer"]?.toBooleanStrictOrNull() ?: defaults.sales.requireCustomer,
                allowOutOfStockSales = map["sales.allowOutOfStockSales"]?.toBooleanStrictOrNull() ?: defaults.sales.allowOutOfStockSales,
                autoSendToKitchen = map["sales.autoSendToKitchen"]?.toBooleanStrictOrNull() ?: defaults.sales.autoSendToKitchen
            ),
            paymentMethods = PaymentMethodSettings(
                methods = parsePaymentMethods(map["paymentMethods.methods"].orEmpty(), defaults.paymentMethods.methods)
            ),
            print = PrintSettings(
                selectedPrinter = map["print.selectedPrinter"] ?: defaults.print.selectedPrinter,
                searchType = map["print.searchType"] ?: defaults.print.searchType,
                paperWidthMm = map["print.paperWidthMm"] ?: defaults.print.paperWidthMm,
                copies = (map["print.copies"]?.toIntOrNull() ?: defaults.print.copies).coerceIn(1, 5),
                textSize = map["print.textSize"] ?: defaults.print.textSize,
                marginLeft = map["print.marginLeft"] ?: defaults.print.marginLeft,
                marginRight = map["print.marginRight"] ?: defaults.print.marginRight,
                marginTop = map["print.marginTop"] ?: defaults.print.marginTop,
                marginBottom = map["print.marginBottom"] ?: defaults.print.marginBottom,
                usefulWidthMm = map["print.usefulWidthMm"] ?: defaults.print.usefulWidthMm,
                logoWidthMm = map["print.logoWidthMm"] ?: defaults.print.logoWidthMm,
                logoHeightMm = map["print.logoHeightMm"] ?: defaults.print.logoHeightMm,
                showCompanyLogo = map["print.showCompanyLogo"]?.toBooleanStrictOrNull() ?: defaults.print.showCompanyLogo,
                showCompanyName = map["print.showCompanyName"]?.toBooleanStrictOrNull() ?: defaults.print.showCompanyName,
                showCompanyRnc = map["print.showCompanyRnc"]?.toBooleanStrictOrNull() ?: defaults.print.showCompanyRnc,
                showCompanyAddress = map["print.showCompanyAddress"]?.toBooleanStrictOrNull() ?: defaults.print.showCompanyAddress,
                showCompanyPhone = map["print.showCompanyPhone"]?.toBooleanStrictOrNull() ?: defaults.print.showCompanyPhone,
                showCompanyEmail = map["print.showCompanyEmail"]?.toBooleanStrictOrNull() ?: defaults.print.showCompanyEmail,
                showCustomerName = map["print.showCustomerName"]?.toBooleanStrictOrNull() ?: defaults.print.showCustomerName,
                showCustomerRnc = map["print.showCustomerRnc"]?.toBooleanStrictOrNull() ?: defaults.print.showCustomerRnc,
                showCustomerPhone = map["print.showCustomerPhone"]?.toBooleanStrictOrNull() ?: defaults.print.showCustomerPhone,
                showCustomerEmail = map["print.showCustomerEmail"]?.toBooleanStrictOrNull() ?: defaults.print.showCustomerEmail,
                showDateTime = map["print.showDateTime"]?.toBooleanStrictOrNull() ?: defaults.print.showDateTime,
                showCashierName = map["print.showCashierName"]?.toBooleanStrictOrNull() ?: defaults.print.showCashierName,
                showTaxes = map["print.showTaxes"]?.toBooleanStrictOrNull() ?: defaults.print.showTaxes,
                showDiscounts = map["print.showDiscounts"]?.toBooleanStrictOrNull() ?: defaults.print.showDiscounts,
                showTip = map["print.showTip"]?.toBooleanStrictOrNull() ?: defaults.print.showTip,
                showPaymentMethod = map["print.showPaymentMethod"]?.toBooleanStrictOrNull() ?: defaults.print.showPaymentMethod,
                showCashChange = map["print.showCashChange"]?.toBooleanStrictOrNull() ?: defaults.print.showCashChange,
                showThankYouMessage = map["print.showThankYouMessage"]?.toBooleanStrictOrNull() ?: defaults.print.showThankYouMessage,
                showQr = map["print.showQr"]?.toBooleanStrictOrNull() ?: defaults.print.showQr,
                showReceiptNumber = map["print.showReceiptNumber"]?.toBooleanStrictOrNull() ?: defaults.print.showReceiptNumber,
                showNcf = map["print.showNcf"]?.toBooleanStrictOrNull() ?: defaults.print.showNcf,
                showNcfExpiry = map["print.showNcfExpiry"]?.toBooleanStrictOrNull() ?: defaults.print.showNcfExpiry,
                showInvoiceTitle = map["print.showInvoiceTitle"]?.toBooleanStrictOrNull() ?: defaults.print.showInvoiceTitle,
                showItems = map["print.showItems"]?.toBooleanStrictOrNull() ?: defaults.print.showItems,
                showSubtotal = map["print.showSubtotal"]?.toBooleanStrictOrNull() ?: defaults.print.showSubtotal,
                showTotal = map["print.showTotal"]?.toBooleanStrictOrNull() ?: defaults.print.showTotal,
                showItemCount = map["print.showItemCount"]?.toBooleanStrictOrNull() ?: defaults.print.showItemCount,
                showCashRegister = map["print.showCashRegister"]?.toBooleanStrictOrNull() ?: defaults.print.showCashRegister,
                showNote = map["print.showNote"]?.toBooleanStrictOrNull() ?: defaults.print.showNote,
                showTaxSummary = map["print.showTaxSummary"]?.toBooleanStrictOrNull() ?: defaults.print.showTaxSummary,
                showReturnPolicy = map["print.showReturnPolicy"]?.toBooleanStrictOrNull() ?: defaults.print.showReturnPolicy,
                showBarcode = map["print.showBarcode"]?.toBooleanStrictOrNull() ?: defaults.print.showBarcode,
                showFooterDate = map["print.showFooterDate"]?.toBooleanStrictOrNull() ?: defaults.print.showFooterDate,
                thankYouMessage = map["print.thankYouMessage"] ?: defaults.print.thankYouMessage
            ),
            notifications = NotificationSettings(
                enabled = map["notifications.enabled"]?.toBooleanStrictOrNull() ?: defaults.notifications.enabled,
                smtpServer = map["notifications.smtpServer"] ?: defaults.notifications.smtpServer,
                smtpPort = map["notifications.smtpPort"] ?: defaults.notifications.smtpPort,
                senderEmail = map["notifications.senderEmail"] ?: defaults.notifications.senderEmail,
                appPassword = map["notifications.appPassword"] ?: defaults.notifications.appPassword,
                senderName = map["notifications.senderName"] ?: defaults.notifications.senderName,
                destinationEmails = map["notifications.destinationEmails"] ?: defaults.notifications.destinationEmails,
                sslTls = map["notifications.sslTls"]?.toBooleanStrictOrNull() ?: defaults.notifications.sslTls,
                sendOnLogout = map["notifications.sendOnLogout"]?.toBooleanStrictOrNull() ?: defaults.notifications.sendOnLogout,
                sendOnCashClose = map["notifications.sendOnCashClose"]?.toBooleanStrictOrNull() ?: defaults.notifications.sendOnCashClose,
                sendOnCancelInvoice = map["notifications.sendOnCancelInvoice"]?.toBooleanStrictOrNull() ?: defaults.notifications.sendOnCancelInvoice,
                sendOnDeleteInvoice = map["notifications.sendOnDeleteInvoice"]?.toBooleanStrictOrNull() ?: defaults.notifications.sendOnDeleteInvoice
            ),
            license = LicenseSettings(
                status = map["license.status"] ?: defaults.license.status,
                companyName = map["license.companyName"] ?: defaults.license.companyName,
                lastCheck = map["license.lastCheck"] ?: defaults.license.lastCheck,
                nextCheck = map["license.nextCheck"] ?: defaults.license.nextCheck,
                offlineDaysRemaining = map["license.offlineDaysRemaining"] ?: defaults.license.offlineDaysRemaining,
                deviceCode = map["license.deviceCode"] ?: defaults.license.deviceCode,
                checkIntervalMinutes = map["license.checkIntervalMinutes"] ?: defaults.license.checkIntervalMinutes
            ),
            server = ServerSettings(
                enabled = map["server.enabled"]?.toBooleanStrictOrNull() ?: defaults.server.enabled,
                serverUrl = map["server.serverUrl"] ?: defaults.server.serverUrl,
                apiRoute = map["server.apiRoute"] ?: defaults.server.apiRoute,
                apiKey = map["server.apiKey"] ?: defaults.server.apiKey,
                tokenConfigured = map["server.tokenConfigured"]?.toBooleanStrictOrNull() ?: defaults.server.tokenConfigured,
                syncInvoices = map["server.syncInvoices"]?.toBooleanStrictOrNull() ?: defaults.server.syncInvoices,
                automaticSend = map["server.automaticSend"]?.toBooleanStrictOrNull() ?: defaults.server.automaticSend,
                syncProducts = map["server.syncProducts"]?.toBooleanStrictOrNull() ?: defaults.server.syncProducts,
                syncCustomers = map["server.syncCustomers"]?.toBooleanStrictOrNull() ?: defaults.server.syncCustomers,
                syncCashClosings = map["server.syncCashClosings"]?.toBooleanStrictOrNull() ?: defaults.server.syncCashClosings,
                lastSync = map["server.lastSync"] ?: defaults.server.lastSync
            ),
            system = SystemSettings(
                localServerIp = map["system.localServerIp"] ?: defaults.system.localServerIp,
                localServerPort = map["system.localServerPort"] ?: defaults.system.localServerPort,
                detectedIps = defaults.system.detectedIps,
                availableUrl = map["system.availableUrl"] ?: defaults.system.availableUrl,
                backups = defaults.system.backups
            ),
            adminCards = AdminCardSettings(
                cards = parseAdminCards(map["adminCards.cards"].orEmpty())
            )
        )
    }

    private fun saveSettingsToFile(settings: AppSettings) {
        PersistentFiles.writeText(
            SETTINGS_FILE,
            buildList {
                addProp("company.businessName", settings.company.businessName)
                addProp("company.rnc", settings.company.rnc)
                addProp("company.phone", settings.company.phone)
                addProp("company.email", settings.company.email)
                addProp("company.currency", settings.company.currency)
                addProp("company.address", settings.company.address)
                addProp("company.taxPercent", settings.company.taxPercent)
                addProp("company.suggestedTipPercent", settings.company.suggestedTipPercent)
                addProp("company.logoPath", settings.company.logoPath.orEmpty())
                addProp("visual.themeMode", settings.visual.themeMode)
                addProp("visual.primaryColor", settings.visual.primaryColor)
                addProp("sales.taxMode", settings.sales.taxMode)
                addProp("sales.taxPercent", settings.sales.taxPercent)
                addProp("sales.allowDiscounts", settings.sales.allowDiscounts.toString())
                addProp("sales.requireCustomer", settings.sales.requireCustomer.toString())
                addProp("sales.allowOutOfStockSales", settings.sales.allowOutOfStockSales.toString())
                addProp("sales.autoSendToKitchen", settings.sales.autoSendToKitchen.toString())
                addProp("paymentMethods.methods", serializePaymentMethods(settings.paymentMethods.methods))
                settings.print.toProps().forEach { add("print.${it.first}=${escape(it.second)}") }
                settings.notifications.toProps().forEach { add("notifications.${it.first}=${escape(it.second)}") }
                settings.license.toProps().forEach { add("license.${it.first}=${escape(it.second)}") }
                settings.server.toProps().forEach { add("server.${it.first}=${escape(it.second)}") }
                addProp("system.localServerIp", settings.system.localServerIp)
                addProp("system.localServerPort", settings.system.localServerPort)
                addProp("system.availableUrl", settings.system.availableUrl)
                addProp("adminCards.cards", serializeAdminCards(settings.adminCards.cards))
            }.joinToString("\n")
        )
    }

    private fun loadProductsFromFile(): List<Product>? {
        return PersistentFiles.readText(PRODUCTS_FILE)
            ?.lineSequence()
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { line ->
                val f = splitEscaped(line)
                if (f.size < 19) return@mapNotNull null
                Product(
                    id = f[0].toIntOrNull() ?: return@mapNotNull null,
                    name = f[1], code = f[2], barcode = f[3], category = f[4],
                    description = f[5], price = f[6].toDoubleOrNull() ?: 0.0,
                    cost = f[7].toDoubleOrNull() ?: 0.0,
                    taxPercent = f[8].toDoubleOrNull() ?: 18.0,
                    stock = f[9].toIntOrNull() ?: 0, stockAlert = f[10].toIntOrNull() ?: 1,
                    imagePath = f[11].ifBlank { null },
                    active = f[12].toBooleanStrictOrNull() ?: true,
                    sellInPos = f[13].toBooleanStrictOrNull() ?: true,
                    sendToKitchen = f[14].toBooleanStrictOrNull() ?: true,
                    sendToBar = f[15].toBooleanStrictOrNull() ?: false,
                    favorite = f[16].toBooleanStrictOrNull() ?: false,
                    controlInventory = f[17].toBooleanStrictOrNull() ?: true,
                    uid = f.getOrElse(18) { genUid("prod") },
                    createdAt = f.getOrElse(19) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                    updatedAt = f.getOrElse(20) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                    modifierGroupIds = f.getOrElse(21) { "" }
                )
            }
            ?.toList()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun saveProductsToFile(products: List<Product>) {
        PersistentFiles.writeText(
            PRODUCTS_FILE,
            products.joinToString("\n") { p ->
                joinEscaped(
                    p.id.toString(), p.name, p.code, p.barcode, p.category, p.description,
                    p.price.toString(), p.cost.toString(), p.taxPercent.toString(),
                    p.stock.toString(), p.stockAlert.toString(), p.imagePath.orEmpty(),
                    p.active.toString(), p.sellInPos.toString(), p.sendToKitchen.toString(),
                    p.sendToBar.toString(), p.favorite.toString(), p.controlInventory.toString(),
                    p.uid, p.createdAt.toString(), p.updatedAt.toString(),
                    p.modifierGroupIds
                )
            }
        )
    }

    private fun loadCategoriesFromFile(): List<Category>? {
        return PersistentFiles.readText(CATEGORIES_FILE)
            ?.lineSequence()
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { line ->
                val f = splitEscaped(line)
                if (f.size < 7) return@mapNotNull null
                Category(
                    id = f[0].toIntOrNull() ?: return@mapNotNull null,
                    name = f[1], description = f[2],
                    colorType = CategoryColorType.entries.firstOrNull { it.name == f[3] } ?: CategoryColorType.Gray,
                    active = f[4].toBooleanStrictOrNull() ?: true,
                    visiblePos = f[5].toBooleanStrictOrNull() ?: true,
                    order = f[6].toIntOrNull() ?: 0,
                    uid = f.getOrElse(7) { genUid("cat") },
                    createdAt = f.getOrElse(8) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                    updatedAt = f.getOrElse(9) { "0" }.toLongOrNull() ?: System.currentTimeMillis()
                )
            }
            ?.toList()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun saveCategoriesToFile(categories: List<Category>) {
        PersistentFiles.writeText(
            CATEGORIES_FILE,
            categories.joinToString("\n") { c ->
                joinEscaped(
                    c.id.toString(), c.name, c.description, c.colorType.name,
                    c.active.toString(), c.visiblePos.toString(), c.order.toString(),
                    c.uid, c.createdAt.toString(), c.updatedAt.toString()
                )
            }
        )
    }

    private fun serializeAdminCards(cards: List<AdminCard>): String =
        cards.joinToString("\n") { card ->
            listOf(card.userId, card.userName, card.userRole, card.cardNumber)
                .joinToString("\t") { escape(it) }
        }

    private fun parseAdminCards(data: String): List<AdminCard> {
        if (data.isBlank()) return emptyList()
        return data.split("\n").mapNotNull { line ->
            val f = splitEscaped(line)
            if (f.size < 4) null
            else AdminCard(userId = f[0], userName = f[1], userRole = f[2], cardNumber = f[3])
        }
    }

    private fun serializePaymentMethods(methods: List<PaymentMethod>): String =
        methods.joinToString("\n") { method ->
            joinEscaped(method.id, method.name, method.percentage, method.enabled.toString())
        }

    private fun parsePaymentMethods(data: String, fallback: List<PaymentMethod>): List<PaymentMethod> {
        if (data.isBlank()) return fallback
        return data.split("\n").mapNotNull { line ->
            val f = splitEscaped(line)
            if (f.size < 4) null
            else PaymentMethod(id = f[0], name = f[1], percentage = f[2], enabled = f[3].toBooleanStrictOrNull() ?: true)
        }.takeIf { it.isNotEmpty() } ?: fallback
    }

    private fun MutableList<String>.addProp(key: String, value: String) {
        add("$key=${escape(value)}")
    }

    private fun PrintSettings.toProps() = listOf(
        "selectedPrinter" to selectedPrinter, "searchType" to searchType, "paperWidthMm" to paperWidthMm,
        "copies" to copies.toString(), "textSize" to textSize, "marginLeft" to marginLeft,
        "marginRight" to marginRight, "marginTop" to marginTop, "marginBottom" to marginBottom,
        "usefulWidthMm" to usefulWidthMm, "logoWidthMm" to logoWidthMm, "logoHeightMm" to logoHeightMm,
        "showCompanyLogo" to showCompanyLogo.toString(), "showCompanyName" to showCompanyName.toString(),
        "showCompanyRnc" to showCompanyRnc.toString(), "showCompanyAddress" to showCompanyAddress.toString(),
        "showCompanyPhone" to showCompanyPhone.toString(), "showCompanyEmail" to showCompanyEmail.toString(),
        "showCustomerName" to showCustomerName.toString(), "showCustomerRnc" to showCustomerRnc.toString(),
        "showCustomerPhone" to showCustomerPhone.toString(), "showCustomerEmail" to showCustomerEmail.toString(),
        "showDateTime" to showDateTime.toString(), "showCashierName" to showCashierName.toString(),
        "showTaxes" to showTaxes.toString(), "showDiscounts" to showDiscounts.toString(),
        "showTip" to showTip.toString(), "showPaymentMethod" to showPaymentMethod.toString(),
        "showCashChange" to showCashChange.toString(), "showThankYouMessage" to showThankYouMessage.toString(),
        "showQr" to showQr.toString(), "showReceiptNumber" to showReceiptNumber.toString(),
        "showNcf" to showNcf.toString(), "showNcfExpiry" to showNcfExpiry.toString(),
        "showInvoiceTitle" to showInvoiceTitle.toString(), "showItems" to showItems.toString(),
        "showSubtotal" to showSubtotal.toString(), "showTotal" to showTotal.toString(),
        "showItemCount" to showItemCount.toString(), "showCashRegister" to showCashRegister.toString(),
        "showNote" to showNote.toString(), "showTaxSummary" to showTaxSummary.toString(),
        "showReturnPolicy" to showReturnPolicy.toString(), "showBarcode" to showBarcode.toString(),
        "showFooterDate" to showFooterDate.toString(), "thankYouMessage" to thankYouMessage
    )

    private fun NotificationSettings.toProps() = listOf(
        "enabled" to enabled.toString(), "smtpServer" to smtpServer, "smtpPort" to smtpPort,
        "senderEmail" to senderEmail, "appPassword" to appPassword, "senderName" to senderName,
        "destinationEmails" to destinationEmails, "sslTls" to sslTls.toString(),
        "sendOnLogout" to sendOnLogout.toString(), "sendOnCashClose" to sendOnCashClose.toString(),
        "sendOnCancelInvoice" to sendOnCancelInvoice.toString(), "sendOnDeleteInvoice" to sendOnDeleteInvoice.toString()
    )

    private fun LicenseSettings.toProps() = listOf(
        "status" to status, "companyName" to companyName, "lastCheck" to lastCheck,
        "nextCheck" to nextCheck, "offlineDaysRemaining" to offlineDaysRemaining,
        "deviceCode" to deviceCode, "checkIntervalMinutes" to checkIntervalMinutes
    )

    private fun ServerSettings.toProps() = listOf(
        "enabled" to enabled.toString(), "serverUrl" to serverUrl, "apiRoute" to apiRoute,
        "apiKey" to apiKey, "tokenConfigured" to tokenConfigured.toString(),
        "syncInvoices" to syncInvoices.toString(), "automaticSend" to automaticSend.toString(),
        "syncProducts" to syncProducts.toString(), "syncCustomers" to syncCustomers.toString(),
        "syncCashClosings" to syncCashClosings.toString(), "lastSync" to lastSync
    )

    private fun joinEscaped(vararg values: String): String = values.joinToString("\t") { escape(it) }

    private fun splitEscaped(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var escaping = false
        for (ch in line) {
            if (escaping) {
                current.append(when (ch) { 'n' -> '\n'; 't' -> '\t'; 'r' -> '\r'; else -> ch })
                escaping = false
            } else {
                when (ch) {
                    '\\' -> escaping = true
                    '\t' -> { result.add(current.toString()); current.clear() }
                    else -> current.append(ch)
                }
            }
        }
        result.add(current.toString())
        return result
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

    private fun unescape(value: String): String = splitEscaped(value).firstOrNull() ?: ""
}
