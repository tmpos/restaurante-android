package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.CartItem
import com.tmrestaurant.ui.theme.AppColors

data class PaymentSplit(
    val method: String,
    val amount: Double,
    val percentage: String = "0"
)

data class ReturnedItem(
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val refundAmount: Double,
    val reason: String = ""
)

data class PaymentResult(
    val invoiceNumber: String,
    val ncf: String,
    val total: Double,
    val subtotalPreTax: Double,
    val taxAmount: Double,
    val paymentMethod: String,
    val receivedAmount: Double,
    val change: Double,
    val note: String,
    val items: List<CartItem>,
    val surchargeAmount: Double = 0.0,
    val surchargePercent: Double = 0.0,
    val turnoId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val discountLabel: String = "",
    val discountAmount: Double = 0.0,
    val tipLabel: String = "",
    val tipAmount: Double = 0.0,
    val customerId: String = "",
    val customerName: String = "",
    val customerRnc: String = "",
    val customerPhone: String = "",
    val status: String = "ACTIVA",
    val paymentSplits: List<PaymentSplit> = emptyList(),
    val dinerNames: List<String> = emptyList(),
    val returnedItems: List<ReturnedItem> = emptyList(),
    val deliveryAddress: String = "",
    val deliveryPhone: String = "",
    val deliveryNotes: String = "",
    val deliveryStatus: String = "",
    val orderNumber: Int = 0
)

private val GreenPayment = Color(0xFF22C55E)

@Composable
fun PaymentModal(
    cartItems: List<CartItem>,
    subtotalPreTax: Double,
    taxAmount: Double,
    total: Double,
    invoiceNumber: String = "FAC-${System.currentTimeMillis().toString().takeLast(8)}",
    ncf: String,
    paymentMethods: List<com.tmrestaurant.ui.data.settings.PaymentMethod> = emptyList(),
    discountLabel: String = "",
    discountAmount: Double = 0.0,
    dinerNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onPaymentComplete: (PaymentResult) -> Unit
) {
    val enabledMethods = paymentMethods.filter { it.enabled }
    val defaultMethod = enabledMethods.firstOrNull()?.name ?: "EFECTIVO"
    var selectedMethod by remember { mutableStateOf(defaultMethod) }
    var receivedText by remember { mutableStateOf("") }
    var splitMode by remember { mutableStateOf(false) }
    var splitAmounts by remember { mutableStateOf(emptyMap<String, String>()) }
    val itemNotes = remember(cartItems) {
        cartItems
            .filter { it.extrasNote.isNotBlank() }
            .joinToString("\n") { "${it.product.name}: ${it.extrasNote.trim()}" }
            .take(300)
    }
    var noteText by remember(cartItems) { mutableStateOf(itemNotes) }

    val selectedPct = enabledMethods.find { it.name == selectedMethod }?.percentage?.toDoubleOrNull() ?: 0.0
    val surchargeFactor = 1.0 + selectedPct / 100.0
    val surchargeAmount = if (selectedPct > 0) total * (surchargeFactor - 1.0) else 0.0
    val grandTotal = total + surchargeAmount

    val receivedAmount = receivedText.toDoubleOrNull() ?: 0.0
    val change = if (receivedAmount >= grandTotal) receivedAmount - grandTotal else 0.0
    val pending = if (receivedAmount < grandTotal) grandTotal - receivedAmount else 0.0

    val isCardOrTransfer = selectedMethod.uppercase().contains("TARJETA") || selectedMethod.uppercase().contains("TRANSFERENCIA")
    val isCredit = selectedMethod.uppercase().contains("CREDITO")
    val isNonCash = isCardOrTransfer || isCredit
    val effectiveAmount = when {
        isNonCash -> grandTotal
        else -> receivedAmount
    }

    var splitTotal = 0.0
    for (m in enabledMethods) {
        splitTotal += splitAmounts[m.name]?.toDoubleOrNull() ?: 0.0
    }
    val splitRemaining = grandTotal - splitTotal
    val splitBalanced = kotlin.math.abs(splitRemaining) < 0.01

    fun onDigit(d: String) {
        if (receivedText.contains(".")) {
            val parts = receivedText.split(".")
            if (parts.size == 2 && parts[1].length >= 2) return
        }
        receivedText += d
    }

    fun onDecimal() {
        if (!receivedText.contains(".")) {
            receivedText = if (receivedText.isEmpty()) "0." else "$receivedText."
        }
    }

    fun onBackspace() {
        if (receivedText.isNotEmpty()) receivedText = receivedText.dropLast(1)
    }

    fun onClear() { receivedText = "" }

    fun addQuick(amount: Int) {
        val current = receivedAmount
        val newVal = current + amount
        receivedText = formatNum(newVal)
    }

    val canComplete = when {
        splitMode -> splitBalanced && splitTotal > 0
        isNonCash -> true
        else -> receivedAmount >= grandTotal
    }

    fun buildSplits(): List<PaymentSplit> {
        if (!splitMode) return emptyList()
        val result = mutableListOf<PaymentSplit>()
        for (method in enabledMethods) {
            val amt = splitAmounts[method.name]?.toDoubleOrNull() ?: 0.0
            if (amt > 0) result.add(PaymentSplit(method = method.name, amount = amt, percentage = method.percentage))
        }
        return result
    }

    fun buildResult(): PaymentResult {
        val splits = buildSplits()
        val hasSplits = splits.isNotEmpty()
        return PaymentResult(
            invoiceNumber = invoiceNumber,
            ncf = ncf,
            total = grandTotal,
            subtotalPreTax = subtotalPreTax * surchargeFactor,
            taxAmount = taxAmount * surchargeFactor,
            paymentMethod = if (hasSplits) splits.joinToString(" + ") { it.method } else selectedMethod,
            receivedAmount = if (hasSplits) grandTotal else effectiveAmount,
            change = if (hasSplits) 0.0 else change,
            note = noteText,
            items = cartItems,
            surchargeAmount = surchargeAmount,
            surchargePercent = selectedPct,
            discountLabel = discountLabel,
            discountAmount = discountAmount,
            paymentSplits = splits,
            dinerNames = dinerNames
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(1020.dp).heightIn(max = 740.dp).clip(RoundedCornerShape(22.dp))
                .background(AppColors.Surface)
        ) {
            PaymentModalHeader(
                itemCount = cartItems.size,
                onClose = onDismiss,
                splitMode = splitMode,
                onToggleSplit = {
                    splitMode = !splitMode
                    if (!splitMode) splitAmounts = emptyMap()
                }
            )

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (splitMode) {
                    SplitPaymentPanel(
                        modifier = Modifier.weight(1f),
                        methods = enabledMethods,
                        splitAmounts = splitAmounts,
                        onAmountChange = { method, value -> splitAmounts = splitAmounts + (method to value) },
                        grandTotal = grandTotal,
                        splitTotal = splitTotal,
                        splitRemaining = splitRemaining,
                        splitBalanced = splitBalanced
                    )
                } else {
                    Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                        PaymentMethodsRow(
                            methods = enabledMethods,
                            selected = selectedMethod,
                            onSelect = { method ->
                                selectedMethod = method
                                val methodUp = method.uppercase()
                                if (methodUp.contains("TARJETA") || methodUp.contains("TRANSFERENCIA") || methodUp.contains("CREDITO")) {
                                    val pct = enabledMethods.find { it.name == method }?.percentage?.toDoubleOrNull() ?: 0.0
                                    val f = 1.0 + pct / 100.0
                                    receivedText = formatNum(total * f)
                                }
                            }
                        )
                        Spacer(Modifier.height(14.dp))
                        AmountReceivedDisplay(amount = effectiveAmount, rawText = receivedText)
                        Spacer(Modifier.height(12.dp))
                        QuickAmountRow(onClick = ::addQuick)
                        Spacer(Modifier.height(14.dp))
                        NumericKeypad(
                            onDigit = ::onDigit,
                            onDecimal = ::onDecimal,
                            onBackspace = ::onBackspace,
                            onClear = ::onClear,
                            autoFill = { receivedText = formatNum(grandTotal) }
                        )
                    }
                }

                Column(
                    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FiscalReceiptSection(ncf = ncf)
                    InvoiceNoteSection(text = noteText, onTextChange = { noteText = it })
                    if (dinerNames.isNotEmpty() && !splitMode) {
                        DinerBreakdownSection(
                            cartItems = cartItems,
                            dinerNames = dinerNames
                        )
                    }
                    if (splitMode) {
                        SplitOrderSummary(
                            methods = enabledMethods,
                            splitAmounts = splitAmounts,
                            grandTotal = grandTotal,
                            splitRemaining = splitRemaining,
                            splitBalanced = splitBalanced
                        )
                    } else {
                        OrderSummarySection(
                            cartItems = cartItems,
                            subtotalPreTax = subtotalPreTax * surchargeFactor,
                            taxAmount = taxAmount * surchargeFactor,
                            total = grandTotal,
                            receivedAmount = effectiveAmount,
                            change = change,
                            pending = pending,
                            surchargeFactor = surchargeFactor,
                            discountLabel = discountLabel,
                            discountAmount = discountAmount
                        )
                    }
                }
            }

            PaymentBottomBar(
                canComplete = canComplete,
                onCancel = onDismiss,
                onComplete = { onPaymentComplete(buildResult()) }
            )
        }
    }
}

private fun formatNum(v: Double): String {
    if (v == v.toLong().toDouble()) return v.toLong().toString()
    val whole = v.toLong()
    val cents = ((v - whole) * 100 + 0.5).toLong()
    return "$whole.${if (cents < 10) "0" else ""}$cents"
}

@Composable
private fun PaymentModalHeader(itemCount: Int, onClose: () -> Unit, splitMode: Boolean, onToggleSplit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(AppColors.Background).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Receipt, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if (splitMode) "Dividir Pago" else "Cobrar Orden", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("$itemCount productos", color = AppColors.TextSecondary, fontSize = 12.sp)
        }
        Box(
            Modifier.height(34.dp).clip(RoundedCornerShape(8.dp))
                .background(if (splitMode) AppColors.PrimaryLight else AppColors.Background)
                .clickable(onClick = onToggleSplit)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CallSplit, null, tint = if (splitMode) AppColors.Primary else AppColors.IconGray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (splitMode) "Simple" else "Dividir", color = if (splitMode) AppColors.Primary else AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Surface)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Close, null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun PaymentMethodsRow(
    methods: List<com.tmrestaurant.ui.data.settings.PaymentMethod>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val icons = mapOf(
        "EFECTIVO" to Icons.Outlined.Money,
        "TARJETA" to Icons.Outlined.CreditCard,
        "TRANSFERENCIA" to Icons.Outlined.AccountBalance,
        "CREDITO" to Icons.Outlined.Assignment
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        methods.forEach { method ->
            val label = method.name.replaceFirstChar { it.uppercase() }
            val icon = icons.entries.firstOrNull { method.name.uppercase().contains(it.key) }?.value
                ?: Icons.Outlined.Money
            Column(
                modifier = Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (selected == method.name) GreenPayment else AppColors.Surface)
                    .border(1.5.dp, if (selected == method.name) GreenPayment else AppColors.Border, RoundedCornerShape(14.dp))
                    .clickable { onSelect(method.name) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, null, tint = if (selected == method.name) Color.White else AppColors.TextPrimary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(label, color = if (selected == method.name) Color.White else AppColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                if (method.percentage.toDoubleOrNull() ?: 0.0 > 0) {
                    Text("+${method.percentage}%", color = if (selected == method.name) Color.White else Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SplitPaymentPanel(
    methods: List<com.tmrestaurant.ui.data.settings.PaymentMethod>,
    splitAmounts: Map<String, String>,
    onAmountChange: (String, String) -> Unit,
    grandTotal: Double,
    splitTotal: Double,
    splitRemaining: Double,
    splitBalanced: Boolean,
    modifier: Modifier = Modifier
) {
    val icons = mapOf(
        "EFECTIVO" to Icons.Outlined.Money,
        "TARJETA" to Icons.Outlined.CreditCard,
        "TRANSFERENCIA" to Icons.Outlined.AccountBalance,
        "CREDITO" to Icons.Outlined.Assignment
    )
    Column(modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
        Text("Distribuir pago", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
        Text("Asigne un monto a cada metodo", fontSize = 11.sp, color = AppColors.TextSecondary)
        Spacer(Modifier.height(12.dp))

        methods.forEach { method ->
            val label = method.name.replaceFirstChar { it.uppercase() }
            val icon = icons.entries.firstOrNull { method.name.uppercase().contains(it.key) }?.value
                ?: Icons.Outlined.Money
            val currentText = splitAmounts[method.name] ?: ""
            val currentAmount = currentText.toDoubleOrNull() ?: 0.0
            val pct = method.percentage.toDoubleOrNull() ?: 0.0
            val effectiveAmount = if (pct > 0) currentAmount * (1.0 + pct / 100.0) else currentAmount

            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Surface)
                    .border(1.dp, if (currentAmount > 0) GreenPayment else AppColors.Border, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = if (currentAmount > 0) GreenPayment else AppColors.IconGray, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                        if (pct > 0) Text("+$pct% recargo", fontSize = 10.sp, color = Color(0xFFD97706))
                    }
                    Box(
                        Modifier.width(120.dp).height(40.dp).clip(RoundedCornerShape(8.dp))
                            .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (currentText.isEmpty()) {
                            Text("0", color = AppColors.Gray, fontSize = 14.sp)
                        }
                        BasicTextField(
                            value = currentText,
                            onValueChange = { v ->
                                if (v.all { it.isDigit() || it == '.' } && v.count { it == '.' } <= 1) {
                                    onAmountChange(method.name, v)
                                }
                            },
                            textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End),
                            modifier = Modifier.fillMaxSize(),
                            singleLine = true
                        )
                    }
                    if (currentAmount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFFEE2E2))
                                .clickable { onAmountChange(method.name, "") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Clear, null, tint = AppColors.Danger, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                if (currentAmount > 0 && pct > 0) {
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text("Total con recargo: RD\$ ${"%,.2f".format(effectiveAmount)}", fontSize = 10.sp, color = AppColors.TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("Total distribuido:", fontSize = 13.sp, color = AppColors.TextSecondary)
            Spacer(Modifier.weight(1f))
            Text("RD\$ ${"%,.2f".format(splitTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("Total factura:", fontSize = 13.sp, color = AppColors.TextSecondary)
            Spacer(Modifier.weight(1f))
            Text("RD\$ ${"%,.2f".format(grandTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        }
        if (kotlin.math.abs(splitRemaining) > 0.01) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Restante:", fontSize = 13.sp, color = AppColors.Danger)
                Spacer(Modifier.weight(1f))
                Text("RD\$ ${"%,.2f".format(splitRemaining)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Danger)
            }
        }
    }
}

@Composable
private fun SplitOrderSummary(
    methods: List<com.tmrestaurant.ui.data.settings.PaymentMethod>,
    splitAmounts: Map<String, String>,
    grandTotal: Double,
    splitRemaining: Double,
    splitBalanced: Boolean
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Receipt, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("RESUMEN DE PAGOS", color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        methods.forEach { method ->
            val amt = splitAmounts[method.name]?.toDoubleOrNull() ?: 0.0
            if (amt > 0) {
                val pct = method.percentage.toDoubleOrNull() ?: 0.0
                val effective = if (pct > 0) amt * (1.0 + pct / 100.0) else amt
                Row(Modifier.fillMaxWidth()) {
                    Text(method.name, fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text("RD\$ ${"%,.2f".format(amt)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                }
                if (pct > 0) {
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        Text("+$pct% recargo: RD\$ ${"%,.2f".format(effective - amt)}", fontSize = 11.sp, color = Color(0xFFD97706))
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(if (splitBalanced) AppColors.Primary else AppColors.Danger).padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (splitBalanced) "TOTAL DISTRIBUIDO" else "RESTANTE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(
                    if (splitBalanced) "RD\$ ${"%,.2f".format(grandTotal)}" else "RD\$ ${"%,.2f".format(splitRemaining)}",
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AmountReceivedDisplay(amount: Double, rawText: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(AppColors.SuccessLight).border(1.dp, AppColors.Success.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("MONTO RECIBIDO", color = AppColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Text(
                    text = "RD$ ${"%,.2f".format(amount)}".replace(",", ","),
                    color = AppColors.Success,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (amount > 0) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.DangerLight)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.Danger, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickAmountRow(onClick: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(50, 100, 200, 500, 1000, 2000).forEach { amount ->
            Box(
                modifier = Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).clickable { onClick(amount) },
                contentAlignment = Alignment.Center
            ) {
                Text("+$amount", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    autoFill: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KeypadRow {
            KeypadBtn("7", Modifier.weight(1f)) { onDigit("7") }
            KeypadBtn("8", Modifier.weight(1f)) { onDigit("8") }
            KeypadBtn("9", Modifier.weight(1f)) { onDigit("9") }
            SpecialKeypadBtn("C", AppColors.DangerLight, AppColors.Danger, Modifier.weight(1f), onClick = onClear)
        }
        KeypadRow {
            KeypadBtn("4", Modifier.weight(1f)) { onDigit("4") }
            KeypadBtn("5", Modifier.weight(1f)) { onDigit("5") }
            KeypadBtn("6", Modifier.weight(1f)) { onDigit("6") }
            Box(
                modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                    .clickable(onClick = onBackspace),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Backspace, null, tint = AppColors.TextPrimary, modifier = Modifier.size(24.dp))
            }
        }
        KeypadRow {
            KeypadBtn("1", Modifier.weight(1f)) { onDigit("1") }
            KeypadBtn("2", Modifier.weight(1f)) { onDigit("2") }
            KeypadBtn("3", Modifier.weight(1f)) { onDigit("3") }
            Box(
                modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(GreenPayment).clickable(onClick = autoFill),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        KeypadRow {
            KeypadBtn("0", Modifier.weight(1f)) { onDigit("0") }
            KeypadBtn("00", Modifier.weight(1f)) { onDigit("0"); onDigit("0") }
            KeypadBtn(".", Modifier.weight(1f)) { onDecimal() }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun KeypadRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun KeypadBtn(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(56.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
            .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SpecialKeypadBtn(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(56.dp).clip(RoundedCornerShape(12.dp)).background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FiscalReceiptSection(ncf: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.InfoLight)
            .border(1.dp, AppColors.Info.copy(alpha = 0.45f), RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Description, null, tint = AppColors.Info, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("COMPROBANTE FISCAL", color = AppColors.Info, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp))
                .background(AppColors.Surface).border(1.dp, AppColors.Info.copy(alpha = 0.45f), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("E32 - Factura de Consumo", modifier = Modifier.weight(1f), color = AppColors.TextPrimary, fontSize = 13.sp)
            Icon(Icons.Outlined.KeyboardArrowDown, null, tint = AppColors.IconGray, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("NCF:", color = AppColors.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.width(10.dp))
            Badge(text = ncf, backgroundColor = AppColors.InfoLight, textColor = AppColors.Info)
        }
    }
}

@Composable
private fun InvoiceNoteSection(text: String, onTextChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Description, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("NOTA DE FACTURA", color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("${text.length}/300", color = AppColors.TextSecondary, fontSize = 11.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(85.dp).clip(RoundedCornerShape(10.dp))
                .background(AppColors.Background).padding(12.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    "Nota opcional para esta factura",
                    color = AppColors.Gray,
                    fontSize = 13.sp
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 300) onTextChange(it) },
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 13.sp),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun OrderSummarySection(
    cartItems: List<CartItem>,
    subtotalPreTax: Double,
    taxAmount: Double,
    total: Double,
    receivedAmount: Double,
    change: Double,
    pending: Double,
    surchargeFactor: Double = 1.0,
    discountLabel: String = "",
    discountAmount: Double = 0.0
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Receipt, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("RESUMEN DE LA ORDEN", color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        cartItems.forEach { item ->
            val itemTotal = item.product.price * item.effectiveQuantity * surchargeFactor
            val isPropina = item.product.code == "PROPINA-LEY"
            val itemColor = if (isPropina) Color(0xFF16A34A) else AppColors.TextPrimary
            val labelColor = if (isPropina) Color(0xFF16A34A) else AppColors.TextSecondary
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val qtyLabel = if (item.weightQuantity > 0) "${item.weightQuantity} lbs" else "${item.quantity}x"
                Text(qtyLabel, color = labelColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(6.dp))
                Text(item.product.name, color = itemColor, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("RD\$ ${"%,.2f".format(itemTotal)}", color = itemColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal:", color = AppColors.TextSecondary, fontSize = 12.sp)
            Text("RD\$ ${"%,.2f".format(subtotalPreTax)}", color = AppColors.TextPrimary, fontSize = 12.sp)
        }
        if (discountAmount > 0 && discountLabel.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Descuento:", color = Color(0xFF16A34A), fontSize = 12.sp)
                Text("-RD\$ ${"%,.2f".format(discountAmount)}", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ITBIS (18%):", color = AppColors.TextSecondary, fontSize = 12.sp)
            Text("RD\$ ${"%,.2f".format(taxAmount)}", color = AppColors.TextPrimary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Primary).padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("TOTAL A PAGAR", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("RD\$ ${"%,.2f".format(total)}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (change > 0) {
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Cambio: ", color = AppColors.TextSecondary, fontSize = 13.sp)
                Text("RD\$ ${"%,.2f".format(change)}", color = GreenPayment, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (pending > 0) {
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Faltan: ", color = AppColors.TextSecondary, fontSize = 13.sp)
                Text("RD\$ ${"%,.2f".format(pending)}", color = AppColors.Danger, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DinerBreakdownSection(
    cartItems: List<CartItem>,
    dinerNames: List<String>
) {
    val dinerColors = listOf(Color(0xFF3B82F6), Color(0xFF22C55E), Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6))
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Group, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("COMENSALES", color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        dinerNames.forEachIndexed { dIdx, name ->
            val dinerItems = cartItems.filter { it.dinerIndex == dIdx + 1 }
            val dinerTotal = dinerItems.sumOf { it.product.price * it.effectiveQuantity + it.extrasCost }
            val dc = dinerColors[dIdx % dinerColors.size]
            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(dc))
                Spacer(Modifier.width(8.dp))
                Text(name, fontSize = 13.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Text("RD\$ ${"%,.2f".format(dinerTotal)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun PaymentBottomBar(
    canComplete: Boolean,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp))
                .background(AppColors.Background).border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Text("Cancelar", color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier.weight(3f).height(52.dp).clip(RoundedCornerShape(14.dp))
                .background(if (canComplete) Color(0xFF16A34A) else Color(0xFF86EFAC))
                .clickable(enabled = canComplete, onClick = onComplete),
            contentAlignment = Alignment.Center
        ) {
            Text("Completar Pago", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
