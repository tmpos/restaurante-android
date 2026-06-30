package com.tmrestaurant.db

import com.tmrestaurant.ui.data.PersistentFiles

object DataMigrator {

    private val MIGRATION_LOG = "migration_v1.done"

    fun isMigrationDone(): Boolean {
        return PersistentFiles.readText(MIGRATION_LOG) == "1"
    }

    fun markMigrationDone() {
        PersistentFiles.writeText(MIGRATION_LOG, "1")
    }

    fun migrateAll() {
        if (isMigrationDone()) return
        val now = System.currentTimeMillis()

        migrateSettings(now)
        migrateFiscalSequences(now)
        // Data migration happens lazily on first access via each manager
        // The flat files remain as fallback until all managers are migrated

        markMigrationDone()
    }

    private fun migrateSettings(now: Long) {
        val props = PersistentFiles.readText("settings.v1.props") ?: return
        val map = props.lines()
            .filter { it.contains("=") }
            .associate { line ->
                val idx = line.indexOf("=")
                line.substring(0, idx) to line.substring(idx + 1)
            }
        if (map.isEmpty()) return

        DatabaseManager.transaction {
            // Company
            DatabaseManager.upsertSingleRow("company_settings", mapOf(
                "business_name" to (map["company.businessName"] ?: "CRIS POLLO"),
                "rnc" to (map["company.rnc"] ?: "123-456789-0"),
                "phone" to (map["company.phone"] ?: "809-555-1234"),
                "email" to (map["company.email"] ?: "info@tm-restaurante.com"),
                "currency" to (map["company.currency"] ?: "RD\$ - Peso Dominicano"),
                "address" to (map["company.address"] ?: "Av. Principal #123, Santo Domingo"),
                "tax_percent" to (map["company.taxPercent"] ?: "18"),
                "suggested_tip_percent" to (map["company.suggestedTipPercent"] ?: "10"),
                "logo_path" to (map["company.logoPath"]?.ifBlank { null })
            ), sync = false)

            // Visual
            DatabaseManager.upsertSingleRow("visual_settings", mapOf(
                "theme_mode" to (map["visual.themeMode"] ?: "light"),
                "primary_color" to (map["visual.primaryColor"] ?: "#8758F2")
            ), sync = false)

            // Sales
            DatabaseManager.upsertSingleRow("sales_settings", mapOf(
                "tax_mode" to (map["sales.taxMode"] ?: "included"),
                "tax_percent" to (map["sales.taxPercent"] ?: "18"),
                "allow_discounts" to ((map["sales.allowDiscounts"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "require_customer" to ((map["sales.requireCustomer"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "allow_out_of_stock_sales" to ((map["sales.allowOutOfStockSales"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "auto_send_to_kitchen" to ((map["sales.autoSendToKitchen"]?.toBooleanStrictOrNull() ?: true).toInt())
            ), sync = false)

            // Payment methods
            val pmData = map["paymentMethods.methods"] ?: ""
            if (pmData.isNotBlank()) {
                pmData.split("\n").forEach { line ->
                    val f = line.split("\t")
                    if (f.size >= 4) {
                        DatabaseManager.insert("payment_methods", mapOf(
                            "id" to f[0], "name" to f[1],
                            "percentage" to f[2],
                            "enabled" to ((f[3].toBooleanStrictOrNull() ?: true).toInt())
                        ), sync = false)
                    }
                }
            }

            // Print
            DatabaseManager.upsertSingleRow("print_settings", mapOf(
                "selected_printer" to (map["print.selectedPrinter"] ?: "POS80 Printer"),
                "search_type" to (map["print.searchType"] ?: "USB"),
                "paper_width_mm" to (map["print.paperWidthMm"] ?: "80"),
                "copies" to ((map["print.copies"]?.toIntOrNull() ?: 2).coerceIn(1, 5)),
                "text_size" to (map["print.textSize"] ?: "large"),
                "margin_left" to (map["print.marginLeft"] ?: "2"),
                "margin_right" to (map["print.marginRight"] ?: "2"),
                "margin_top" to (map["print.marginTop"] ?: "2"),
                "margin_bottom" to (map["print.marginBottom"] ?: "2"),
                "useful_width_mm" to (map["print.usefulWidthMm"] ?: "70"),
                "logo_width_mm" to (map["print.logoWidthMm"] ?: "51"),
                "logo_height_mm" to (map["print.logoHeightMm"] ?: "20"),
                "show_company_logo" to ((map["print.showCompanyLogo"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_company_name" to ((map["print.showCompanyName"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_company_rnc" to ((map["print.showCompanyRnc"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_company_address" to ((map["print.showCompanyAddress"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_company_phone" to ((map["print.showCompanyPhone"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_company_email" to ((map["print.showCompanyEmail"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "show_customer_name" to ((map["print.showCustomerName"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_customer_rnc" to ((map["print.showCustomerRnc"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_customer_phone" to ((map["print.showCustomerPhone"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "show_customer_email" to ((map["print.showCustomerEmail"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "show_date_time" to ((map["print.showDateTime"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_cashier_name" to ((map["print.showCashierName"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_taxes" to ((map["print.showTaxes"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_discounts" to ((map["print.showDiscounts"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_tip" to ((map["print.showTip"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_payment_method" to ((map["print.showPaymentMethod"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_cash_change" to ((map["print.showCashChange"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_thank_you_message" to ((map["print.showThankYouMessage"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_qr" to ((map["print.showQr"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_receipt_number" to ((map["print.showReceiptNumber"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_ncf" to ((map["print.showNcf"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_ncf_expiry" to ((map["print.showNcfExpiry"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_invoice_title" to ((map["print.showInvoiceTitle"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_items" to ((map["print.showItems"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_subtotal" to ((map["print.showSubtotal"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_total" to ((map["print.showTotal"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_item_count" to ((map["print.showItemCount"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_cash_register" to ((map["print.showCashRegister"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_note" to ((map["print.showNote"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_tax_summary" to ((map["print.showTaxSummary"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_return_policy" to ((map["print.showReturnPolicy"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_barcode" to ((map["print.showBarcode"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "show_footer_date" to ((map["print.showFooterDate"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "thank_you_message" to (map["print.thankYouMessage"] ?: "Gracias por su compra!")
            ), sync = false)

            // Notifications
            DatabaseManager.upsertSingleRow("notification_settings", mapOf(
                "enabled" to ((map["notifications.enabled"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "smtp_server" to (map["notifications.smtpServer"] ?: "smtp.gmail.com"),
                "smtp_port" to (map["notifications.smtpPort"] ?: "465"),
                "sender_email" to (map["notifications.senderEmail"] ?: "tmposrd@gmail.com"),
                "app_password" to (map["notifications.appPassword"] ?: ""),
                "sender_name" to (map["notifications.senderName"] ?: "TM-RESTAURANTE"),
                "destination_emails" to (map["notifications.destinationEmails"] ?: "wilsontomas1986@gmail.com"),
                "ssl_tls" to ((map["notifications.sslTls"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "send_on_logout" to ((map["notifications.sendOnLogout"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "send_on_cash_close" to ((map["notifications.sendOnCashClose"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "send_on_cancel_invoice" to ((map["notifications.sendOnCancelInvoice"]?.toBooleanStrictOrNull() ?: true).toInt()),
                "send_on_delete_invoice" to ((map["notifications.sendOnDeleteInvoice"]?.toBooleanStrictOrNull() ?: true).toInt())
            ), sync = false)

            // License
            DatabaseManager.upsertSingleRow("license_settings", mapOf(
                "status" to (map["license.status"] ?: "Prueba (7 dias)"),
                "company_name" to (map["license.companyName"] ?: "TM-RESTAURANTE"),
                "last_check" to (map["license.lastCheck"] ?: ""),
                "next_check" to (map["license.nextCheck"] ?: ""),
                "offline_days_remaining" to (map["license.offlineDaysRemaining"] ?: "5 de 5"),
                "device_code" to (map["license.deviceCode"] ?: ""),
                "check_interval_minutes" to (map["license.checkIntervalMinutes"] ?: "360")
            ), sync = false)

            // Server
            DatabaseManager.upsertSingleRow("server_settings", mapOf(
                "enabled" to ((map["server.enabled"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "server_url" to (map["server.serverUrl"] ?: ""),
                "api_route" to (map["server.apiRoute"] ?: "/api/elo"),
                "api_key" to (map["server.apiKey"] ?: ""),
                "token_configured" to ((map["server.tokenConfigured"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "sync_invoices" to ((map["server.syncInvoices"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "automatic_send" to ((map["server.automaticSend"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "sync_products" to ((map["server.syncProducts"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "sync_customers" to ((map["server.syncCustomers"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "sync_cash_closings" to ((map["server.syncCashClosings"]?.toBooleanStrictOrNull() ?: false).toInt()),
                "last_sync" to (map["server.lastSync"] ?: "")
            ), sync = false)

            // System
            DatabaseManager.upsertSingleRow("system_settings", mapOf(
                "local_server_ip" to (map["system.localServerIp"] ?: "192.168.1.7"),
                "local_server_port" to (map["system.localServerPort"] ?: "8787"),
                "detected_ips" to "",
                "available_url" to (map["system.availableUrl"] ?: "")
            ), sync = false)
        }
    }

    private fun migrateFiscalSequences(now: Long) {
        val text = PersistentFiles.readText("fiscal_sequences.v1.tsv") ?: return
        text.lines().filter { it.isNotBlank() }.forEach { line ->
            val f = line.split("\t")
            val type = f.getOrNull(0) ?: return@forEach
            DatabaseManager.insert("fiscal_sequences", mapOf(
                "type" to type,
                "enabled" to ((f.getOrNull(1)?.toBooleanStrictOrNull() ?: true).toInt()),
                "prefix" to (f.getOrNull(2) ?: ""),
                "current" to (f.getOrNull(3)?.toIntOrNull() ?: 39),
                "range_start" to (f.getOrNull(4)?.toIntOrNull() ?: 1),
                "range_end" to (f.getOrNull(5)?.toIntOrNull() ?: 99999999),
                "valid_until" to (f.getOrNull(6) ?: "31/12/2027")
            ), sync = false)
        }
    }

    private fun Boolean.toInt() = if (this) 1 else 0
}
