package com.tmrestaurant.ui.screens.caja

import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.Corte
import com.tmrestaurant.ui.data.Gasto
import com.tmrestaurant.ui.data.settings.CompanySettings

fun buildCorteEmailBody(
    corte: Corte,
    company: CompanySettings,
    billetCounts: List<Int>,
    monedaCounts: List<Int>,
    totalBilletes: Int,
    totalMonedas: Int,
    totalFisico: Int,
    diferencia: Int
): String {
    val turno = corte.turno
    val isAdmin = turno.userId == "admin"
    val denomBills = listOf("RD\$ 2000" to 2000, "RD\$ 1000" to 1000, "RD\$ 500" to 500, "RD\$ 200" to 200, "RD\$ 100" to 100, "RD\$ 50" to 50, "RD\$ 25" to 25)
    val denomCoins = listOf("RD\$ 25" to 25, "RD\$ 10" to 10, "RD\$ 5" to 5, "RD\$ 1" to 1)

    val billetesHtml = denomBills.mapIndexed { i, (label, _) ->
        val count = billetCounts.getOrElse(i) { 0 }
        val subtotal = count * denomBills[i].second
        """<tr><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;">$label</td><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;text-align:center;">x$count</td><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;text-align:right;">RD$ ${"%,d".format(subtotal)}</td></tr>"""
    }.joinToString("\n")

    val monedasHtml = denomCoins.mapIndexed { i, (label, _) ->
        val count = monedaCounts.getOrElse(i) { 0 }
        val subtotal = count * denomCoins[i].second
        """<tr><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;">$label</td><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;text-align:center;">x$count</td><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;text-align:right;">RD$ ${"%,d".format(subtotal)}</td></tr>"""
    }.joinToString("\n")

    val facturasHtml = corte.invoices.joinToString("\n") { inv ->
        val items = inv.items.joinToString("<br>") { it.product.name + " x" + it.quantity + " = RD$ " + "%,.2f".format(it.product.price * it.quantity) }
        """<tr><td style="padding:6px 8px;border-bottom:1px solid #e5e7eb;">${inv.invoiceNumber}</td><td style="padding:6px 8px;border-bottom:1px solid #e5e7eb;">${inv.ncf}</td><td style="padding:6px 8px;border-bottom:1px solid #e5e7eb;">${inv.paymentMethod}</td><td style="padding:6px 8px;border-bottom:1px solid #e5e7eb;text-align:right;">RD$ ${"%,.2f".format(inv.total)}</td></tr>"""
    }

    val gastosHtml = if (corte.gastos.isNotEmpty()) {
        corte.gastos.joinToString("\n") { g ->
            """<tr><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;">${g.description}</td><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;text-align:right;">RD$ ${"%,.2f".format(g.amount)}</td></tr>"""
        }
    } else ""

    val movimientosHtml = if (corte.movimientos.isNotEmpty()) {
        corte.movimientos.joinToString("\n") { m ->
            val tipo = if (m.tipo.name == "ENTRADA") "Entrada" else "Retiro"
            val color = if (m.tipo.name == "ENTRADA") "#16a34a" else "#ef4444"
            """<tr><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;color:$color;font-weight:bold;">$tipo</td><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;">${m.description}</td><td style="padding:4px 8px;border-bottom:1px solid #e5e7eb;text-align:right;color:$color;">RD$ ${"%,.2f".format(m.amount)}</td></tr>"""
        }
    } else ""

    val diffClass = if (diferencia >= 0) "color: #16a34a;" else "color: #ef4444;"
    val diffSign = if (diferencia >= 0) "" else "-"

    return """<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><style>
body{font-family:Arial,Helvetica,sans-serif;margin:0;padding:0;background:#f3f4f6;}
.container{max-width:700px;margin:20px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);}
.header{background:linear-gradient(135deg,#1e293b,#0f172a);padding:24px 32px;text-align:center;}
.header h1{color:#f97316;margin:0;font-size:24px;}
.header p{color:#94a3b8;margin:4px 0 0;font-size:13px;}
.section{padding:20px 32px 0;}
.section h2{color:#0f172a;font-size:16px;margin:0 0 12px;padding-bottom:8px;border-bottom:2px solid #f97316;}
.summary-grid{display:flex;flex-wrap:wrap;gap:12px;margin-bottom:16px;}
.summary-card{flex:1;min-width:130px;background:#f8fafc;border-radius:10px;padding:14px;text-align:center;}
.summary-card .label{color:#64748b;font-size:11px;text-transform:uppercase;}
.summary-card .value{color:#0f172a;font-size:20px;font-weight:bold;margin-top:4px;}
.summary-card .value.orange{color:#f97316;}
.summary-card .value.green{color:#16a34a;}
.summary-card .value.red{color:#ef4444;}
table{width:100%;border-collapse:collapse;margin-bottom:16px;}
table th{background:#f1f5f9;color:#475569;font-size:12px;text-transform:uppercase;padding:8px;text-align:left;border-bottom:2px solid #e2e8f0;}
table td{font-size:13px;color:#1e293b;}
.footer{background:#f8fafc;padding:16px 32px;text-align:center;color:#94a3b8;font-size:11px;border-top:1px solid #e5e7eb;}
</style></head>
<body>
<div class="container">
<div class="header">
<h1>${company.businessName.ifBlank { "TM-RESTAURANTE" }}</h1>
<p>Reporte de Cierre de Caja</p>
</div>

<div class="section">
<h2>Informacion del Turno</h2>
<table>
<tr><td style="padding:4px 8px;color:#64748b;width:140px;">Turno:</td><td style="padding:4px 8px;font-weight:bold;">${turno.id}</td></tr>
<tr><td style="padding:4px 8px;color:#64748b;">Cajero:</td><td style="padding:4px 8px;font-weight:bold;">${turno.userName}</td></tr>
<tr><td style="padding:4px 8px;color:#64748b;">Fecha Apertura:</td><td style="padding:4px 8px;">${formatDateTime(turno.startTime)}</td></tr>
<tr><td style="padding:4px 8px;color:#64748b;">Monto Inicial:</td><td style="padding:4px 8px;">RD$ ${"%,.2f".format(turno.initialAmount)}</td></tr>
</table>
</div>

<div class="section">
<h2>Resumen de Ventas</h2>
<div class="summary-grid">
<div class="summary-card"><div class="label">Total Ventas</div><div class="value orange">RD$ ${"%,.2f".format(corte.totalVentas)}</div></div>
<div class="summary-card"><div class="label">Efectivo</div><div class="value">RD$ ${"%,.2f".format(corte.totalEfectivo)}</div></div>
<div class="summary-card"><div class="label">Tarjeta</div><div class="value">RD$ ${"%,.2f".format(corte.totalTarjeta)}</div></div>
<div class="summary-card"><div class="label">Transferencia</div><div class="value">RD$ ${"%,.2f".format(corte.totalTransferencia)}</div></div>
<div class="summary-card"><div class="label">Credito</div><div class="value">RD$ ${"%,.2f".format(corte.totalCredito)}</div></div>
<div class="summary-card"><div class="label">Articulos</div><div class="value">${corte.totalArticulos}</div></div>
<div class="summary-card"><div class="label">Facturas</div><div class="value">${corte.invoiceCount}</div></div>
</div>
</div>

${if (gastosHtml.isNotEmpty()) """
<div class="section">
<h2>Gastos del Turno</h2>
<table>
<thead><tr><th>Descripcion</th><th style="text-align:right;">Monto</th></tr></thead>
<tbody>
$gastosHtml
<tr><td style="padding:6px 8px;font-weight:bold;border-top:2px solid #e2e8f0;">Total Gastos</td><td style="padding:6px 8px;font-weight:bold;text-align:right;border-top:2px solid #e2e8f0;">RD$ ${"%,.2f".format(corte.totalGastos)}</td></tr>
</tbody>
</table>
</div>
""" else ""}

${if (movimientosHtml.isNotEmpty()) """
<div class="section">
<h2>Movimientos de Caja</h2>
<table>
<thead><tr><th>Tipo</th><th>Descripcion</th><th style="text-align:right;">Monto</th></tr></thead>
<tbody>
$movimientosHtml
<tr><td colspan="2" style="padding:6px 8px;font-weight:bold;border-top:2px solid #e2e8f0;">Total Entradas</td><td style="padding:6px 8px;font-weight:bold;text-align:right;border-top:2px solid #e2e8f0;color:#16a34a;">RD$ ${"%,.2f".format(corte.totalEntradas)}</td></tr>
<tr><td colspan="2" style="padding:6px 8px;font-weight:bold;">Total Retiros</td><td style="padding:6px 8px;font-weight:bold;text-align:right;color:#ef4444;">RD$ ${"%,.2f".format(corte.totalRetiros)}</td></tr>
</tbody>
</table>
</div>
""" else ""}

<div class="section">
<h2>Conteo Fisico</h2>
<h3 style="font-size:13px;color:#475569;margin:8px 0;">Billetes</h3>
<table>
<thead><tr><th>Denominacion</th><th style="text-align:center;">Cant.</th><th style="text-align:right;">Subtotal</th></tr></thead>
<tbody>$billetesHtml</tbody>
</table>
<h3 style="font-size:13px;color:#475569;margin:8px 0;">Monedas</h3>
<table>
<thead><tr><th>Denominacion</th><th style="text-align:center;">Cant.</th><th style="text-align:right;">Subtotal</th></tr></thead>
<tbody>$monedasHtml</tbody>
</table>
<div class="summary-grid">
<div class="summary-card"><div class="label">Total Billetes</div><div class="value">RD$ ${"%,d".format(totalBilletes)}</div></div>
<div class="summary-card"><div class="label">Total Monedas</div><div class="value">RD$ ${"%,d".format(totalMonedas)}</div></div>
<div class="summary-card"><div class="label">Total Fisico</div><div class="value orange">RD$ ${"%,d".format(totalFisico)}</div></div>
</div>
</div>

<div class="section">
<h2>Comparacion de Efectivo</h2>
<div class="summary-grid">
<div class="summary-card"><div class="label">Efectivo Esperado</div><div class="value green">RD$ ${"%,.2f".format(corte.expectedCash)}</div></div>
<div class="summary-card"><div class="label">Entradas</div><div class="value green">RD$ ${"%,.2f".format(corte.totalEntradas)}</div></div>
<div class="summary-card"><div class="label">Retiros</div><div class="value red">RD$ ${"%,.2f".format(corte.totalRetiros)}</div></div>
<div class="summary-card"><div class="label">Diferencia</div><div class="value" style="$diffClass">${diffSign}RD$ ${"%,d".format(kotlin.math.abs(diferencia))}</div></div>
</div>
</div>

${if (facturasHtml.isNotEmpty()) """
<div class="section">
<h2>Detalle de Facturas</h2>
<table>
<thead><tr><th>Factura</th><th>NCF</th><th>Metodo</th><th style="text-align:right;">Total</th></tr></thead>
<tbody>$facturasHtml</tbody>
</table>
</div>
""" else ""}

<div class="footer">
<p>Reporte generado automaticamente por TM-Restaurante POS</p>
<p>${company.businessName.ifBlank { "TM-RESTAURANTE" }} | ${company.rnc.ifBlank { "" }} | ${company.phone.ifBlank { "" }} | ${company.email.ifBlank { "" }}</p>
</div>
</div>
</body>
</html>"""
}
