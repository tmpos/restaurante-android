package com.tmrestaurant.ui.data.settings

data class AppSettings(
    val company: CompanySettings = CompanySettings(),
    val visual: VisualSettings = VisualSettings(),
    val sales: SalesSettings = SalesSettings(),
    val paymentMethods: PaymentMethodSettings = PaymentMethodSettings(),
    val print: PrintSettings = PrintSettings(),
    val notifications: NotificationSettings = NotificationSettings(),
    val license: LicenseSettings = LicenseSettings(),
    val server: ServerSettings = ServerSettings(),
    val system: SystemSettings = SystemSettings(),
    val adminCards: AdminCardSettings = AdminCardSettings()
)

data class CompanySettings(
    val businessName: String = "CRIS POLLO",
    val rnc: String = "123-456789-0",
    val phone: String = "809-555-1234",
    val email: String = "info@tm-restaurante.com",
    val currency: String = "RD\$ - Peso Dominicano",
    val address: String = "Av. Principal #123, Santo Domingo",
    val taxPercent: String = "18",
    val suggestedTipPercent: String = "10",
    val logoPath: String? = null
)

data class VisualSettings(
    val themeMode: String = "light",
    val primaryColor: String = "#8758F2"
)

data class SalesSettings(
    val taxMode: String = "included",
    val taxPercent: String = "18",
    val allowDiscounts: Boolean = false,
    val requireCustomer: Boolean = false,
    val allowOutOfStockSales: Boolean = false,
    val autoSendToKitchen: Boolean = true
)

data class PaymentMethod(
    val id: String = "pm_${kotlin.random.Random.nextInt()}",
    val name: String,
    val percentage: String = "0",
    val enabled: Boolean = true
)

data class PaymentMethodSettings(
    val methods: List<PaymentMethod> = listOf(
        PaymentMethod(id = "pm_efectivo", name = "EFECTIVO", percentage = "0", enabled = true),
        PaymentMethod(id = "pm_tarjeta", name = "TARJETA DEBITO/CREDITO", percentage = "2.5", enabled = true),
        PaymentMethod(id = "pm_transferencia", name = "TRANSFERENCIA", percentage = "0", enabled = false)
    )
)

data class PrintSettings(
    val selectedPrinter: String = "POS80 Printer",
    val searchType: String = "USB",
    val paperWidthMm: String = "80",
    val copies: Int = 2,
    val textSize: String = "large",
    val marginLeft: String = "2",
    val marginRight: String = "2",
    val marginTop: String = "2",
    val marginBottom: String = "2",
    val usefulWidthMm: String = "70",
    val logoWidthMm: String = "51",
    val logoHeightMm: String = "20",
    val showCompanyLogo: Boolean = true,
    val showCompanyName: Boolean = true,
    val showCompanyRnc: Boolean = true,
    val showCompanyAddress: Boolean = true,
    val showCompanyPhone: Boolean = true,
    val showCompanyEmail: Boolean = false,
    val showCustomerName: Boolean = true,
    val showCustomerRnc: Boolean = true,
    val showCustomerPhone: Boolean = false,
    val showCustomerEmail: Boolean = false,
    val showDateTime: Boolean = true,
    val showCashierName: Boolean = true,
    val showTaxes: Boolean = true,
    val showDiscounts: Boolean = true,
    val showTip: Boolean = true,
    val showPaymentMethod: Boolean = true,
    val showCashChange: Boolean = true,
    val showThankYouMessage: Boolean = true,
    val showQr: Boolean = true,
    val showReceiptNumber: Boolean = true,
    val showNcf: Boolean = true,
    val showNcfExpiry: Boolean = true,
    val showInvoiceTitle: Boolean = true,
    val showItems: Boolean = true,
    val showSubtotal: Boolean = true,
    val showTotal: Boolean = true,
    val showItemCount: Boolean = true,
    val showCashRegister: Boolean = true,
    val showNote: Boolean = true,
    val showTaxSummary: Boolean = true,
    val showReturnPolicy: Boolean = true,
    val showBarcode: Boolean = true,
    val showFooterDate: Boolean = true,
    val thankYouMessage: String = "Gracias por su compra!"
)

data class NotificationSettings(
    val enabled: Boolean = false,
    val smtpServer: String = "smtp.gmail.com",
    val smtpPort: String = "465",
    val senderEmail: String = "tmposrd@gmail.com",
    val appPassword: String = "",
    val senderName: String = "TM-RESTAURANTE",
    val destinationEmails: String = "wilsontomas1986@gmail.com",
    val sslTls: Boolean = true,
    val sendOnLogout: Boolean = false,
    val sendOnCashClose: Boolean = true,
    val sendOnCancelInvoice: Boolean = true,
    val sendOnDeleteInvoice: Boolean = true
)

data class LicenseSettings(
    val status: String = "Prueba (7 días)",
    val companyName: String = "TM-RESTAURANTE",
    val lastCheck: String = "14/05/2026, 09:46 p. m.",
    val nextCheck: String = "15/05/2026, 03:46 a. m.",
    val offlineDaysRemaining: String = "5 de 5",
    val deviceCode: String = "NTQ2QVCMjczNEND",
    val checkIntervalMinutes: String = "360"
)

data class ServerSettings(
    val enabled: Boolean = true,
    val serverUrl: String = "http://127.0.0.1:8080",
    val apiRoute: String = "/api/elo",
    val apiKey: String = "CAMBIAR_ESTA_CLAVE_SEGURA",
    val tokenConfigured: Boolean = true,
    val syncInvoices: Boolean = true,
    val automaticSend: Boolean = true,
    val syncProducts: Boolean = false,
    val syncCustomers: Boolean = false,
    val syncCashClosings: Boolean = false,
    val lastSync: String = "15/05/2026, 12:57 a. m."
)

data class SystemSettings(
    val localServerIp: String = "192.168.1.7",
    val localServerPort: String = "8787",
    val detectedIps: List<String> = listOf("192.168.0.106", "0.0.0.0"),
    val availableUrl: String = "http://192.168.1.7:8787",
    val backups: List<BackupItem> = mockBackups
)

data class BackupItem(
    val id: Int,
    val fileName: String,
    val date: String,
    val sizeKb: Double
)

data class SyncQueueItem(
    val id: Int,
    val type: String,
    val reference: String,
    val status: String,
    val attempts: Int,
    val lastAttempt: String,
    val error: String = "-"
)

val mockBackups = listOf(
    BackupItem(1, "tm-restaurante-logout-2026-05-14T21-46-07-073Z.db", "14/05/2026, 05:46 p. m.", 272.0),
    BackupItem(2, "tm-restaurante-logout-2026-05-14T19-09-40-891Z.db", "14/05/2026, 03:09 p. m.", 264.0),
    BackupItem(3, "tm-restaurante-logout-2026-05-14T18-48-01-55Z.db", "14/05/2026, 02:48 p. m.", 264.0),
    BackupItem(4, "tm-restaurante-logout-2026-05-14T18-14-9-093Z.db", "14/05/2026, 02:14 p. m.", 260.0),
    BackupItem(5, "tm-restaurante-logout-2026-05-14T15-39-50-052Z.db", "14/05/2026, 11:39 a. m.", 248.0)
)

val mockSyncQueue = listOf(
    SyncQueueItem(14, "factura", "FAC-20260514-639596", "Enviado", 1, "14/05/2026, 08:57 p. m."),
    SyncQueueItem(13, "factura", "FAC-20260514-596563", "Enviado", 1, "14/05/2026, 08:56 p. m.")
)

val availableColors = listOf(
    "#EF4444" to "Rojo", "#E68A00" to "Naranja", "#FBBF24" to "Amarillo",
    "#22C55E" to "Verde", "#14B8A6" to "Turquesa", "#3B82F6" to "Azul",
    "#6366F1" to "Azul/Morado", "#8758F2" to "Morado", "#EC4899" to "Rosa", "#DC2626" to "Rojo fuerte"
)

data class AdminCard(
    val userId: String,
    val userName: String,
    val userRole: String,
    val cardNumber: String
)

data class AdminCardSettings(
    val cards: List<AdminCard> = emptyList()
)

enum class SettingsSection(val title: String, val iconDesc: String) {
    EMPRESA("Empresa", "edificio"),
    VISUAL("Visual", "paleta"),
    VENTAS("Ventas", "carrito"),
    PAGOS("Metodos de Pago", "pago"),
    IMPRESION("Impresion", "impresora"),
    NOTIFICACIONES("Notificaciones", "campana"),
    LICENCIA("Licencia", "escudo"),
    FISCAL("Fiscal", "comprobante"),
    SERVIDOR("Servidor", "servidor"),
    TM_CLOUD("TM Cloud", "cloud"),
    ALANUBE("Alanube DGII", "alanube"),
    SISTEMA("Sistema", "base de datos"),
    SOPORTE("Soporte", "herramientas"),
    ELO("ELO", "elo"),
    TARJETAS_ADMIN("Tarjetas Admin", "tarjetas")
}
