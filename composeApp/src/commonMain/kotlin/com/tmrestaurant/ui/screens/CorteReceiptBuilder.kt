package com.tmrestaurant.ui.screens

import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.Corte
import com.tmrestaurant.ui.data.settings.CompanySettings

fun buildCorteReceiptText(
    corte: Corte,
    company: CompanySettings,
    paperWidthMm: String = "80"
): String {
    val width = if (paperWidthMm.trim() == "80") 48 else 32

    fun money(value: Double) = "RD$ ${"%,.2f".format(value)}"
    fun line(char: Char = '-') = char.toString().repeat(width)
    fun center(value: String): String {
        val text = value.take(width)
        val left = ((width - text.length) / 2).coerceAtLeast(0)
        return " ".repeat(left) + text
    }
    fun row(label: String, value: String): String {
        val cleanValue = value.take(width)
        val spaces = (width - label.length - cleanValue.length).coerceAtLeast(1)
        return label + " ".repeat(spaces) + cleanValue
    }

    return buildString {
        appendLine(center(company.businessName.ifBlank { "TM-RESTAURANTE" }))
        appendLine(center("CORTE DE CAJA"))
        appendLine(line())
        appendLine(row("Cajero:", corte.turno.userName))
        appendLine(row("Inicio:", formatDateTime(corte.turno.startTime)))
        appendLine(row("Efectivo Inicial:", money(corte.turno.initialAmount)))
        appendLine(line())
        appendLine(center("RESUMEN DE VENTAS"))
        appendLine(line())
        appendLine(row("Facturas:", "${corte.invoiceCount}"))
        appendLine(row("Articulos:", "${corte.totalArticulos}"))
        appendLine(row("Efectivo:", money(corte.totalEfectivo)))
        appendLine(row("Tarjeta:", money(corte.totalTarjeta)))
        appendLine(row("Transferencia:", money(corte.totalTransferencia)))
        appendLine(row("Credito:", money(corte.totalCredito)))
        appendLine(line())
        appendLine(row("TOTAL VENTAS:", money(corte.totalVentas)))
        appendLine(line())
        if (corte.gastos.isNotEmpty()) {
            appendLine(center("GASTOS"))
            appendLine(line())
            corte.gastos.forEach { gasto ->
                appendLine("  ${gasto.description.take(width - 16)}")
                appendLine(row("  ${gasto.userName}", money(gasto.amount)))
            }
            appendLine(line())
            appendLine(row("TOTAL GASTOS:", money(corte.totalGastos)))
            appendLine(line())
        }
        if (corte.movimientos.isNotEmpty()) {
            appendLine(center("MOVIMIENTOS DE CAJA"))
            appendLine(line())
            corte.movimientos.forEach { mov ->
                val sign = if (mov.tipo.name == "ENTRADA") "+" else "-"
                appendLine("  ${mov.description.take(width - 18)}")
                appendLine(row("  $sign ${mov.userName}", money(mov.amount)))
            }
            appendLine(line())
            if (corte.totalEntradas > 0) appendLine(row("TOTAL ENTRADAS:", money(corte.totalEntradas)))
            if (corte.totalRetiros > 0) appendLine(row("TOTAL RETIROS:", money(corte.totalRetiros)))
            appendLine(line())
        }
        appendLine(center("EFECTIVO EN CAJA"))
        appendLine(line())
        appendLine(row("Inicial:", money(corte.turno.initialAmount)))
        appendLine(row("+ Ventas Efectivo:", money(corte.totalEfectivo)))
        if (corte.totalEntradas > 0) {
            appendLine(row("+ Entradas:", money(corte.totalEntradas)))
        }
        if (corte.totalGastos > 0) {
            appendLine(row("- Gastos:", money(corte.totalGastos)))
        }
        if (corte.totalRetiros > 0) {
            appendLine(row("- Retiros:", money(corte.totalRetiros)))
        }
        appendLine(line())
        appendLine(row("ESPERADO EN CAJA:", money(corte.expectedCash)))
        appendLine(line())
        appendLine(center("--- FIN DEL CORTE ---"))
    }
}
