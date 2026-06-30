package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.platform.formatDateKey
import com.tmrestaurant.platform.currentDateParts
import com.tmrestaurant.platform.EmailAttachment
import com.tmrestaurant.platform.isNetworkAvailable
import com.tmrestaurant.platform.sendEmail
import com.tmrestaurant.platform.SmtpConfig
import com.tmrestaurant.ui.components.PaymentResult
import com.tmrestaurant.ui.data.InvoiceHistory
import com.tmrestaurant.ui.data.PersistentFiles
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

private enum class ReportPeriod(val label: String, val durationMillis: Long?) {
    TODAY("Hoy", 24L * 60L * 60L * 1000L),
    DAYS_7("7 dias", 7L * 24L * 60L * 60L * 1000L),
    DAYS_30("30 dias", 30L * 24L * 60L * 60L * 1000L),
    ALL("Todo", null),
    CUSTOM("Personalizado", null)
}

private data class PaymentMetric(val name: String, val amount: Double, val color: Color)
private data class ProductMetric(val name: String, val quantity: Double, val amount: Double)
private data class CategoryMetric(val name: String, val quantity: Double, val amount: Double)
private data class HourMetric(val hour: Int, val invoices: Int, val amount: Double)

@Composable
fun ReportsScreen() {
    var period by remember { mutableStateOf(ReportPeriod.TODAY) }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var selectingDate by remember { mutableStateOf<DateTarget?>(null) }
    var sendingEmail by remember { mutableStateOf(false) }
    var exportingCsv by remember { mutableStateOf(false) }
    var emailStatus by remember { mutableStateOf("") }
    val settingsState = LocalSettingsState.current
    val scope = rememberCoroutineScope()
    val now = System.currentTimeMillis()
    val customRangeValid = isValidDateKey(fromDate) && isValidDateKey(toDate) && fromDate <= toDate
    val invoices = InvoiceHistory.invoices.filter { invoice ->
        val inPeriod = when (period) {
            ReportPeriod.CUSTOM -> customRangeValid && formatDateKey(invoice.timestamp) in fromDate..toDate
            else -> period.durationMillis?.let { invoice.timestamp >= now - it } ?: true
        }
        inPeriod && invoice.status != "ANULADA" && invoice.status != "DEVUELTA"
    }

    val netSales = invoices.sumOf(::netInvoiceTotal)
    val taxTotal = invoices.sumOf { invoice ->
        val ratio = if (invoice.total > 0.0) netInvoiceTotal(invoice) / invoice.total else 0.0
        invoice.taxAmount * ratio
    }
    val discountTotal = invoices.sumOf { it.discountAmount }
    val averageTicket = if (invoices.isNotEmpty()) netSales / invoices.size else 0.0
    val paymentMetrics = buildPaymentMetrics(invoices)
    val productMetrics = buildProductMetrics(invoices).take(8)
    val categoryMetrics = buildCategoryMetrics(invoices).take(8)
    val hourlyMetrics = buildHourlyMetrics(invoices)
    val recentInvoices = invoices.sortedByDescending { it.timestamp }.take(8)

    Column(
        Modifier.fillMaxSize().background(AppColors.Background)
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Reportes", color = AppColors.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Resumen de ventas y rendimiento del negocio", color = AppColors.TextSecondary, fontSize = 13.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportPeriod.entries.forEach { option ->
                    PeriodButton(option.label, option == period) { period = option }
                }
                ExportCsvButton(exportingCsv) {
                    when {
                        period == ReportPeriod.CUSTOM && !customRangeValid ->
                            emailStatus = "Seleccione un rango personalizado valido"
                        invoices.isEmpty() ->
                            emailStatus = "No hay datos para exportar en este periodo"
                        else -> {
                            exportingCsv = true
                            val fileName = buildReportCsvFileName(period, fromDate, toDate)
                            PersistentFiles.writeText(
                                fileName,
                                buildReportCsv(
                                    periodLabel = reportPeriodLabel(period, fromDate, toDate),
                                    invoices = invoices,
                                    paymentMetrics = paymentMetrics,
                                    productMetrics = productMetrics,
                                    categoryMetrics = categoryMetrics,
                                    hourlyMetrics = hourlyMetrics
                                )
                            )
                            exportingCsv = false
                            emailStatus = "CSV exportado: $fileName"
                        }
                    }
                }
                EmailReportButton(sendingEmail) {
                    val company = settingsState.settings.company
                    val notifications = settingsState.settings.notifications
                    val periodLabel = reportPeriodLabel(period, fromDate, toDate)
                    when {
                        period == ReportPeriod.CUSTOM && !customRangeValid ->
                            emailStatus = "Seleccione un rango personalizado valido"
                        invoices.isEmpty() ->
                            emailStatus = "No hay datos para enviar en este periodo"
                        company.email.isBlank() ->
                            emailStatus = "Configure el correo de la empresa"
                        !notifications.enabled ->
                            emailStatus = "Habilite las notificaciones por correo"
                        notifications.senderEmail.isBlank() || notifications.appPassword.isBlank() ->
                            emailStatus = "Configure el correo remitente y la contrasena de aplicacion"
                        !isNetworkAvailable() ->
                            emailStatus = "No hay conexion a internet"
                        else -> {
                            sendingEmail = true
                            emailStatus = "Enviando reporte a ${company.email}..."
                            val body = buildReportEmailBody(
                                companyName = company.businessName,
                                companyEmail = company.email,
                                periodLabel = periodLabel,
                                invoices = invoices,
                                netSales = netSales,
                                averageTicket = averageTicket,
                                taxTotal = taxTotal,
                                discountTotal = discountTotal,
                                paymentMetrics = paymentMetrics,
                                productMetrics = productMetrics,
                                categoryMetrics = categoryMetrics,
                                hourlyMetrics = hourlyMetrics
                            )
                            val csvFileName = buildReportCsvFileName(period, fromDate, toDate)
                            val csvContent = buildReportCsv(
                                periodLabel = periodLabel,
                                invoices = invoices,
                                paymentMetrics = paymentMetrics,
                                productMetrics = productMetrics,
                                categoryMetrics = categoryMetrics,
                                hourlyMetrics = hourlyMetrics
                            )
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    sendEmail(
                                        config = SmtpConfig(
                                            host = notifications.smtpServer,
                                            port = notifications.smtpPort.toIntOrNull() ?: 465,
                                            username = notifications.senderEmail,
                                            password = notifications.appPassword,
                                            useSsl = notifications.sslTls
                                        ),
                                        fromName = notifications.senderName,
                                        fromAddr = notifications.senderEmail,
                                        to = company.email,
                                        subject = "Reporte de ventas - $periodLabel - ${company.businessName}",
                                        body = body,
                                        attachments = listOf(
                                            EmailAttachment(
                                                fileName = csvFileName,
                                                mimeType = "text/csv",
                                                bytes = csvContent.encodeToByteArray()
                                            )
                                        )
                                    )
                                }
                                sendingEmail = false
                                emailStatus = if (result.success) {
                                    "Reporte enviado correctamente a ${company.email}"
                                } else {
                                    "Error al enviar: ${result.error}"
                                }
                            }
                        }
                    }
                }
            }
        }

        if (emailStatus.isNotBlank()) {
            Text(
                emailStatus,
                color = if (emailStatus.startsWith("Reporte enviado")) AppColors.Success
                else if (sendingEmail) AppColors.Info else AppColors.Danger,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (period == ReportPeriod.CUSTOM) {
            CustomDateRange(
                fromDate = fromDate,
                toDate = toDate,
                isValid = customRangeValid,
                onSelectFrom = { selectingDate = DateTarget.FROM },
                onSelectTo = { selectingDate = DateTarget.TO }
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ReportStatCard(
                "Ventas netas", money(netSales), "${invoices.size} facturas",
                Icons.Outlined.TrendingUp, AppColors.Success, Modifier.weight(1f)
            )
            ReportStatCard(
                "Ticket promedio", money(averageTicket), "Por factura",
                Icons.Outlined.Receipt, AppColors.Primary, Modifier.weight(1f)
            )
            ReportStatCard(
                "ITBIS", money(taxTotal), "Impuesto incluido",
                Icons.Outlined.Percent, AppColors.Info, Modifier.weight(1f)
            )
            ReportStatCard(
                "Descuentos", money(discountTotal), "Aplicados en ventas",
                Icons.Outlined.ShoppingCart, AppColors.Orange, Modifier.weight(1f)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ReportSection("Metodos de pago", Icons.Outlined.CreditCard, Modifier.weight(0.92f)) {
                if (paymentMetrics.isEmpty()) {
                    EmptyReportMessage("No hay pagos en este periodo")
                } else {
                    val maximum = max(paymentMetrics.maxOf { it.amount }, 1.0)
                    paymentMetrics.forEach { metric ->
                        PaymentBar(metric, maximum, netSales)
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }

            ReportSection("Productos mas vendidos", Icons.Outlined.Inventory2, Modifier.weight(1.08f)) {
                if (productMetrics.isEmpty()) {
                    EmptyReportMessage("No hay productos vendidos en este periodo")
                } else {
                    productMetrics.forEachIndexed { index, product ->
                        ProductReportRow(index + 1, product)
                        if (index < productMetrics.lastIndex) {
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ReportSection("Ventas por categoria", Icons.Outlined.Category, Modifier.weight(1f)) {
                if (categoryMetrics.isEmpty()) {
                    EmptyReportMessage("No hay categorias vendidas en este periodo")
                } else {
                    categoryMetrics.forEachIndexed { index, category ->
                        CategoryReportRow(index + 1, category)
                        if (index < categoryMetrics.lastIndex) {
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            ReportSection("Ventas por hora", Icons.Outlined.CalendarMonth, Modifier.weight(1f)) {
                if (hourlyMetrics.isEmpty()) {
                    EmptyReportMessage("No hay ventas por hora en este periodo")
                } else {
                    val maximum = max(hourlyMetrics.maxOf { it.amount }, 1.0)
                    hourlyMetrics.forEach { metric ->
                        HourReportRow(metric, maximum)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        ReportSection("Facturas recientes", Icons.Outlined.Receipt, Modifier.fillMaxWidth()) {
            if (recentInvoices.isEmpty()) {
                EmptyReportMessage("No hay facturas en este periodo")
            } else {
                InvoiceTableHeader()
                recentInvoices.forEach { invoice ->
                    InvoiceReportRow(invoice)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    selectingDate?.let { target ->
        CalendarDatePicker(
            title = if (target == DateTarget.FROM) "Seleccionar fecha inicial" else "Seleccionar fecha final",
            initialDate = if (target == DateTarget.FROM) fromDate else toDate,
            onDismiss = { selectingDate = null },
            onConfirm = { selected ->
                if (target == DateTarget.FROM) fromDate = selected else toDate = selected
                selectingDate = null
            }
        )
    }
}

@Composable
private fun EmailReportButton(sending: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.height(40.dp).clip(RoundedCornerShape(10.dp))
            .background(if (sending) AppColors.Border else AppColors.Success)
            .clickable(enabled = !sending, onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Email, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            if (sending) "Enviando..." else "Enviar por correo",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExportCsvButton(exporting: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.height(40.dp).clip(RoundedCornerShape(10.dp))
            .background(if (exporting) AppColors.Border else AppColors.Info)
            .clickable(enabled = !exporting, onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Receipt, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            if (exporting) "Exportando..." else "Exportar CSV",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private enum class DateTarget { FROM, TO }

@Composable
private fun CustomDateRange(
    fromDate: String,
    toDate: String,
    isValid: Boolean,
    onSelectFrom: () -> Unit,
    onSelectTo: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = AppColors.Primary, modifier = Modifier.size(21.dp))
        }
        DateRangeField("Desde", fromDate, onSelectFrom)
        DateRangeField("Hasta", toDate, onSelectTo)
        Column(Modifier.weight(1f)) {
            Text(
                if (isValid) "Rango aplicado: $fromDate al $toDate" else customRangeMessage(fromDate, toDate),
                color = if (isValid) AppColors.Success else AppColors.Danger,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("Las fechas inicial y final estan incluidas", color = AppColors.TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun DateRangeField(label: String, value: String, onClick: () -> Unit) {
    Column(Modifier.width(180.dp)) {
        Text(label, color = AppColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(10.dp))
                .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                .clickable(onClick = onClick).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                value.ifBlank { "Seleccionar fecha" },
                color = if (value.isBlank()) AppColors.Gray else AppColors.TextPrimary,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Outlined.CalendarMonth, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CalendarDatePicker(
    title: String,
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val today = currentDateParts()
    val initial = parseDateParts(initialDate) ?: today
    var year by remember { mutableStateOf(initial.first) }
    var month by remember { mutableStateOf(initial.second) }
    var selectedDay by remember { mutableStateOf(initial.third.coerceAtMost(daysInMonth(initial.first, initial.second))) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.widthIn(min = 390.dp, max = 440.dp).clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface).border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(title, color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CalendarNavButton(Icons.Outlined.ArrowBack) {
                    if (month == 1) { month = 12; year-- } else month--
                    selectedDay = selectedDay.coerceAtMost(daysInMonth(year, month))
                }
                Text(
                    "${monthNames[month - 1]} $year",
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                CalendarNavButton(Icons.Outlined.ArrowForward) {
                    if (month == 12) { month = 1; year++ } else month++
                    selectedDay = selectedDay.coerceAtMost(daysInMonth(year, month))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(
                        day,
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val firstOffset = dayOfWeek(year, month, 1)
            val totalDays = daysInMonth(year, month)
            repeat(6) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { column ->
                        val day = row * 7 + column - firstOffset + 1
                        if (day in 1..totalDays) {
                            val selected = day == selectedDay
                            val isToday = year == today.first && month == today.second && day == today.third
                            Box(
                                Modifier.weight(1f).height(42.dp).padding(3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) AppColors.Primary else Color.Transparent)
                                    .border(
                                        if (isToday && !selected) 1.dp else 0.dp,
                                        if (isToday && !selected) AppColors.Primary else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDay = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$day",
                                    color = if (selected) Color.White else AppColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        } else {
                            Spacer(Modifier.weight(1f).height(42.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    Modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                        .clickable(onClick = onDismiss).padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancelar", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Primary)
                        .clickable { onConfirm(dateKey(year, month, selectedDay)) }.padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aceptar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CalendarNavButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = AppColors.Primary, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun PeriodButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.height(40.dp).clip(RoundedCornerShape(10.dp))
            .background(if (selected) AppColors.Primary else AppColors.Surface)
            .border(1.dp, if (selected) AppColors.Primary else AppColors.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReportStatCard(
    title: String,
    value: String,
    detail: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.clip(RoundedCornerShape(16.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp)).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column {
            Text(title, color = AppColors.TextSecondary, fontSize = 12.sp)
            Text(value, color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ReportSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp)).padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AppColors.Primary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))
        content()
    }
}

@Composable
private fun PaymentBar(metric: PaymentMetric, maximum: Double, grandTotal: Double) {
    val ratio = (metric.amount / maximum).toFloat().coerceIn(0f, 1f)
    val percent = if (grandTotal > 0.0) metric.amount / grandTotal * 100.0 else 0.0
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(RoundedCornerShape(5.dp)).background(metric.color))
            Spacer(Modifier.width(8.dp))
            Text(metric.name, color = AppColors.TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("${"%.1f".format(percent)}%", color = AppColors.TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.width(12.dp))
            Text(money(metric.amount), color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(7.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(AppColors.Background)) {
            Box(Modifier.fillMaxWidth(ratio).height(8.dp).clip(RoundedCornerShape(4.dp)).background(metric.color))
        }
    }
}

@Composable
private fun ProductReportRow(position: Int, product: ProductMetric) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background),
            contentAlignment = Alignment.Center
        ) {
            Text("$position", color = AppColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(product.name, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${formatQuantity(product.quantity)} vendidos", color = AppColors.TextSecondary, fontSize = 10.sp)
        }
        Text(money(product.amount), color = AppColors.Success, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CategoryReportRow(position: Int, category: CategoryMetric) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Text("$position", color = AppColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(category.name, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${formatQuantity(category.quantity)} articulos", color = AppColors.TextSecondary, fontSize = 10.sp)
        }
        Text(money(category.amount), color = AppColors.Success, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HourReportRow(metric: HourMetric, maximum: Double) {
    val ratio = (metric.amount / maximum).toFloat().coerceIn(0f, 1f)
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${metric.hour.toString().padStart(2, '0')}:00", color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp))
            Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(AppColors.Background)) {
                Box(Modifier.fillMaxWidth(ratio).height(8.dp).clip(RoundedCornerShape(4.dp)).background(AppColors.Info))
            }
            Spacer(Modifier.width(10.dp))
            Text("${metric.invoices} fac.", color = AppColors.TextSecondary, fontSize = 10.sp, modifier = Modifier.width(44.dp))
            Text(money(metric.amount), color = AppColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(92.dp))
        }
    }
}

@Composable
private fun InvoiceTableHeader() {
    Row(Modifier.fillMaxWidth().background(AppColors.Background).padding(horizontal = 12.dp, vertical = 9.dp)) {
        TableText("Factura", 1.1f, true)
        TableText("Fecha", 1.4f, true)
        TableText("Metodo", 1.2f, true)
        TableText("Cliente", 1.4f, true)
        TableText("Estado", 0.9f, true)
        TableText("Total", 1f, true)
    }
}

@Composable
private fun InvoiceReportRow(invoice: PaymentResult) {
    val statusColor = if (invoice.status == "DEVOLUCION_PARCIAL") AppColors.Orange else AppColors.Success
    Row(
        Modifier.fillMaxWidth().border(0.5.dp, AppColors.DividerColor)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableText(invoice.invoiceNumber, 1.1f)
        TableText(formatDateTime(invoice.timestamp), 1.4f)
        TableText(invoice.paymentMethod, 1.2f)
        TableText(invoice.customerName.ifBlank { "Consumidor final" }, 1.4f)
        Text(invoice.status.replace("_", " "), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
        Text(money(netInvoiceTotal(invoice)), color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RowScope.TableText(text: String, weight: Float, header: Boolean = false) {
    Text(
        text,
        color = if (header) AppColors.TextSecondary else AppColors.TextPrimary,
        fontSize = if (header) 10.sp else 12.sp,
        fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(weight)
    )
}

@Composable
private fun EmptyReportMessage(text: String) {
    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Text(text, color = AppColors.TextSecondary, fontSize = 13.sp)
    }
}

private fun netInvoiceTotal(invoice: PaymentResult): Double =
    (invoice.total - invoice.returnedItems.sumOf { it.refundAmount }).coerceAtLeast(0.0)

private fun buildPaymentMetrics(invoices: List<PaymentResult>): List<PaymentMetric> {
    val values = linkedMapOf<String, Double>()
    invoices.forEach { invoice ->
        val net = netInvoiceTotal(invoice)
        if (invoice.paymentSplits.isNotEmpty() && invoice.total > 0.0) {
            val ratio = net / invoice.total
            invoice.paymentSplits.forEach { split ->
                values[split.method] = (values[split.method] ?: 0.0) + split.amount * ratio
            }
        } else {
            values[invoice.paymentMethod] = (values[invoice.paymentMethod] ?: 0.0) + net
        }
    }
    val colors = listOf(Color(0xFF22C55E), Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899))
    return values.entries.sortedByDescending { it.value }.mapIndexed { index, entry ->
        PaymentMetric(entry.key.ifBlank { "Sin especificar" }, entry.value, colors[index % colors.size])
    }
}

private fun buildProductMetrics(invoices: List<PaymentResult>): List<ProductMetric> {
    val quantities = linkedMapOf<String, Double>()
    val amounts = linkedMapOf<String, Double>()
    invoices.forEach { invoice ->
        val returnsByProduct = invoice.returnedItems.groupBy { it.productId }
        invoice.items.forEach { item ->
            val returnedQty = returnsByProduct[item.product.id]?.sumOf { it.quantity } ?: 0
            val quantity = (item.effectiveQuantity - returnedQty).coerceAtLeast(0.0)
            val returnedAmount = returnsByProduct[item.product.id]?.sumOf { it.refundAmount } ?: 0.0
            val amount = (item.product.price * item.effectiveQuantity + item.extrasCost - returnedAmount).coerceAtLeast(0.0)
            quantities[item.product.name] = (quantities[item.product.name] ?: 0.0) + quantity
            amounts[item.product.name] = (amounts[item.product.name] ?: 0.0) + amount
        }
    }
    return quantities.map { (name, quantity) ->
        ProductMetric(name, quantity, amounts[name] ?: 0.0)
    }.sortedByDescending { it.quantity }
}

private fun buildCategoryMetrics(invoices: List<PaymentResult>): List<CategoryMetric> {
    val quantities = linkedMapOf<String, Double>()
    val amounts = linkedMapOf<String, Double>()
    invoices.forEach { invoice ->
        invoice.items.forEach { item ->
            val category = item.product.category.ifBlank { "Sin categoria" }
            val amount = item.product.price * item.effectiveQuantity + item.extrasCost
            quantities[category] = (quantities[category] ?: 0.0) + item.effectiveQuantity
            amounts[category] = (amounts[category] ?: 0.0) + amount
        }
    }
    return quantities.map { (name, quantity) ->
        CategoryMetric(name, quantity, amounts[name] ?: 0.0)
    }.sortedByDescending { it.amount }
}

private fun buildHourlyMetrics(invoices: List<PaymentResult>): List<HourMetric> {
    if (invoices.isEmpty()) return emptyList()
    return invoices.groupBy { hourOfDay(it.timestamp) }
        .map { (hour, values) -> HourMetric(hour, values.size, values.sumOf(::netInvoiceTotal)) }
        .sortedBy { it.hour }
}

private fun hourOfDay(timestamp: Long): Int {
    val oneDay = 24L * 60L * 60L * 1000L
    val localMillis = timestamp - (4L * 60L * 60L * 1000L)
    return ((localMillis % oneDay + oneDay) % oneDay / (60L * 60L * 1000L)).toInt()
}

private fun money(value: Double): String = "RD$ ${"%,.2f".format(value)}"

private fun formatQuantity(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else "%.2f".format(value)

private fun isValidDateKey(value: String): Boolean {
    if (value.length != 10 || value[4] != '-' || value[7] != '-') return false
    val year = value.substring(0, 4).toIntOrNull() ?: return false
    val month = value.substring(5, 7).toIntOrNull() ?: return false
    val day = value.substring(8, 10).toIntOrNull() ?: return false
    if (year !in 2000..2100 || month !in 1..12) return false
    val maxDay = when (month) {
        2 -> if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return day in 1..maxDay
}

private fun customRangeMessage(fromDate: String, toDate: String): String = when {
    fromDate.isBlank() || toDate.isBlank() -> "Complete ambas fechas"
    !isValidDateKey(fromDate) || !isValidDateKey(toDate) -> "Use el formato AAAA-MM-DD"
    fromDate > toDate -> "La fecha inicial debe ser anterior a la final"
    else -> "Rango no valido"
}

private val monthNames = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

private val weekDays = listOf("Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab")

private fun parseDateParts(value: String): Triple<Int, Int, Int>? {
    if (!isValidDateKey(value)) return null
    return Triple(
        value.substring(0, 4).toInt(),
        value.substring(5, 7).toInt(),
        value.substring(8, 10).toInt()
    )
}

private fun dateKey(year: Int, month: Int, day: Int): String =
    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (isLeapYear(year)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

private fun isLeapYear(year: Int): Boolean =
    year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)

private fun dayOfWeek(year: Int, month: Int, day: Int): Int {
    var adjustedYear = year
    var adjustedMonth = month
    if (adjustedMonth < 3) {
        adjustedMonth += 12
        adjustedYear--
    }
    val zeroBased = (
        day + (13 * (adjustedMonth + 1)) / 5 + adjustedYear +
            adjustedYear / 4 - adjustedYear / 100 + adjustedYear / 400
        ) % 7
    return (zeroBased + 6) % 7
}

private fun reportPeriodLabel(period: ReportPeriod, fromDate: String, toDate: String): String = when (period) {
    ReportPeriod.TODAY -> "Hoy"
    ReportPeriod.DAYS_7 -> "Ultimos 7 dias"
    ReportPeriod.DAYS_30 -> "Ultimos 30 dias"
    ReportPeriod.ALL -> "Todo el historial"
    ReportPeriod.CUSTOM -> "$fromDate al $toDate"
}

private fun buildReportEmailBody(
    companyName: String,
    companyEmail: String,
    periodLabel: String,
    invoices: List<PaymentResult>,
    netSales: Double,
    averageTicket: Double,
    taxTotal: Double,
    discountTotal: Double,
    paymentMetrics: List<PaymentMetric>,
    productMetrics: List<ProductMetric>,
    categoryMetrics: List<CategoryMetric>,
    hourlyMetrics: List<HourMetric>
): String {
    val paymentsHtml = paymentMetrics.joinToString("") { metric ->
        """<tr><td>${html(metric.name)}</td><td class="amount">${money(metric.amount)}</td></tr>"""
    }
    val productsHtml = productMetrics.take(10).joinToString("") { product ->
        """<tr><td>${html(product.name)}</td><td class="center">${formatQuantity(product.quantity)}</td><td class="amount">${money(product.amount)}</td></tr>"""
    }
    val categoriesHtml = categoryMetrics.take(10).joinToString("") { category ->
        """<tr><td>${html(category.name)}</td><td class="center">${formatQuantity(category.quantity)}</td><td class="amount">${money(category.amount)}</td></tr>"""
    }
    val hoursHtml = hourlyMetrics.joinToString("") { metric ->
        """<tr><td>${metric.hour.toString().padStart(2, '0')}:00</td><td class="center">${metric.invoices}</td><td class="amount">${money(metric.amount)}</td></tr>"""
    }
    val invoicesHtml = invoices.sortedByDescending { it.timestamp }.take(20).joinToString("") { invoice ->
        """<tr><td>${html(invoice.invoiceNumber)}</td><td>${html(formatDateTime(invoice.timestamp))}</td><td>${html(invoice.paymentMethod)}</td><td class="amount">${money(netInvoiceTotal(invoice))}</td></tr>"""
    }

    return """<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
body{margin:0;background:#f1f5f9;font-family:Arial,Helvetica,sans-serif;color:#0f172a}
.container{max-width:760px;margin:20px auto;background:#fff;border-radius:14px;overflow:hidden}
.header{background:#111827;padding:26px 32px;color:#fff}
.header h1{margin:0;font-size:24px}.header p{margin:6px 0 0;color:#cbd5e1}
.content{padding:24px 32px}.grid{display:flex;flex-wrap:wrap;gap:12px}
.card{flex:1;min-width:145px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:10px;padding:14px}
.label{font-size:11px;color:#64748b;text-transform:uppercase}.value{font-size:20px;font-weight:bold;margin-top:5px}
h2{font-size:16px;margin:26px 0 10px;border-bottom:2px solid #22c55e;padding-bottom:8px}
table{width:100%;border-collapse:collapse}th{background:#f1f5f9;text-align:left;font-size:11px;color:#475569;padding:9px}
td{font-size:12px;padding:9px;border-bottom:1px solid #e5e7eb}.amount{text-align:right;font-weight:bold}.center{text-align:center}
.footer{text-align:center;background:#f8fafc;color:#64748b;font-size:11px;padding:16px}
</style>
</head>
<body>
<div class="container">
<div class="header">
<h1>${html(companyName.ifBlank { "TM-RESTAURANTE" })}</h1>
<p>Reporte de ventas | ${html(periodLabel)}</p>
</div>
<div class="content">
<div class="grid">
<div class="card"><div class="label">Ventas netas</div><div class="value">${money(netSales)}</div></div>
<div class="card"><div class="label">Facturas</div><div class="value">${invoices.size}</div></div>
<div class="card"><div class="label">Ticket promedio</div><div class="value">${money(averageTicket)}</div></div>
<div class="card"><div class="label">ITBIS</div><div class="value">${money(taxTotal)}</div></div>
<div class="card"><div class="label">Descuentos</div><div class="value">${money(discountTotal)}</div></div>
</div>
<h2>Metodos de pago</h2>
<table><thead><tr><th>Metodo</th><th class="amount">Monto</th></tr></thead><tbody>$paymentsHtml</tbody></table>
<h2>Productos mas vendidos</h2>
<table><thead><tr><th>Producto</th><th class="center">Cantidad</th><th class="amount">Ventas</th></tr></thead><tbody>$productsHtml</tbody></table>
<h2>Ventas por categoria</h2>
<table><thead><tr><th>Categoria</th><th class="center">Articulos</th><th class="amount">Ventas</th></tr></thead><tbody>$categoriesHtml</tbody></table>
<h2>Ventas por hora</h2>
<table><thead><tr><th>Hora</th><th class="center">Facturas</th><th class="amount">Ventas</th></tr></thead><tbody>$hoursHtml</tbody></table>
<h2>Facturas</h2>
<table><thead><tr><th>Factura</th><th>Fecha</th><th>Metodo</th><th class="amount">Total</th></tr></thead><tbody>$invoicesHtml</tbody></table>
</div>
<div class="footer">Enviado automaticamente por TM POS a ${html(companyEmail)}</div>
</div>
</body>
</html>"""
}

private fun html(value: String): String =
    value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;")

private fun buildReportCsvFileName(period: ReportPeriod, fromDate: String, toDate: String): String {
    val suffix = when (period) {
        ReportPeriod.CUSTOM -> "${fromDate}_$toDate"
        else -> period.label.lowercase().replace(" ", "_")
    }
    return "report_${suffix}_${System.currentTimeMillis()}.csv"
}

private fun buildReportCsv(
    periodLabel: String,
    invoices: List<PaymentResult>,
    paymentMetrics: List<PaymentMetric>,
    productMetrics: List<ProductMetric>,
    categoryMetrics: List<CategoryMetric>,
    hourlyMetrics: List<HourMetric>
): String = buildString {
    appendLine("Periodo,$periodLabel")
    appendLine()
    appendLine("Resumen")
    appendLine("Factura,Fecha,Metodo,Cliente,Estado,Total Neto")
    invoices.sortedByDescending { it.timestamp }.forEach { invoice ->
        appendCsvRow(
            invoice.invoiceNumber,
            formatDateTime(invoice.timestamp),
            invoice.paymentMethod,
            invoice.customerName.ifBlank { "Consumidor final" },
            invoice.status,
            "%.2f".format(netInvoiceTotal(invoice))
        )
    }
    appendLine()
    appendLine("Metodos de pago")
    appendLine("Metodo,Monto")
    paymentMetrics.forEach { metric ->
        appendCsvRow(metric.name, "%.2f".format(metric.amount))
    }
    appendLine()
    appendLine("Productos")
    appendLine("Producto,Cantidad,Ventas")
    productMetrics.forEach { product ->
        appendCsvRow(product.name, formatQuantity(product.quantity), "%.2f".format(product.amount))
    }
    appendLine()
    appendLine("Categorias")
    appendLine("Categoria,Articulos,Ventas")
    categoryMetrics.forEach { category ->
        appendCsvRow(category.name, formatQuantity(category.quantity), "%.2f".format(category.amount))
    }
    appendLine()
    appendLine("Ventas por hora")
    appendLine("Hora,Facturas,Ventas")
    hourlyMetrics.forEach { metric ->
        appendCsvRow("${metric.hour.toString().padStart(2, '0')}:00", metric.invoices.toString(), "%.2f".format(metric.amount))
    }
}

private fun StringBuilder.appendCsvRow(vararg values: String) {
    appendLine(values.joinToString(",") { value ->
        "\"${value.replace("\"", "\"\"")}\""
    })
}
