package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.components.DashboardSectionCard
import com.tmrestaurant.ui.components.MainStatsRow
import com.tmrestaurant.ui.components.MenuDashboardModal
import com.tmrestaurant.ui.components.QuickAccessRow
import com.tmrestaurant.ui.components.RecentOrdersList
import com.tmrestaurant.ui.components.SecondaryStatsRow
import com.tmrestaurant.ui.components.TopSellingList
import com.tmrestaurant.ui.components.WelcomeBanner
import com.tmrestaurant.ui.components.Screen
import com.tmrestaurant.ui.data.*
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun DashboardScreen(
    currentScreen: Screen = Screen.Dashboard,
    onNavigate: (Screen) -> Unit = {}
) {
    val invoices = InvoiceHistory.invoices
    val todayInvoices = invoices.filter {
        val now = System.currentTimeMillis()
        now - it.timestamp < 86400000L
    }
    var showMenu by remember { mutableStateOf(false) }

    val totalVentasHoy = todayInvoices.sumOf { it.total }
    val totalEfectivoHoy = todayInvoices.filter { it.paymentMethod.uppercase().contains("EFECTIVO") }.sumOf { it.total }
    val totalTarjetaHoy = todayInvoices.filter { it.paymentMethod.uppercase().contains("TARJETA") }.sumOf { it.total }
    val totalTransferenciaHoy = todayInvoices.filter { it.paymentMethod.uppercase().contains("TRANSFERENCIA") }.sumOf { it.total }

    val comandasPendientes = ComandasManager.activeComandas.count { it.status == ComandaStatus.Pendiente || it.status == ComandaStatus.EnPreparacion }

    val mesas = MesasManager.mesasForTurno(TurnoManager.currentTurno?.id ?: "")
    val mesasOcupadas = mesas?.count { it.isOccupied } ?: 0
    val mesasTotal = mesas?.size ?: 0

    val ordenesActivas = comandasPendientes + mesasOcupadas

    val topProducts = todayInvoices.flatMap { it.items }
        .groupBy { it.product.name }
        .map { (name, items) -> TopSelling(name, items.sumOf { it.quantity }) }
        .sortedByDescending { it.qtySold }
        .take(8)

    val recentOrders = todayInvoices.sortedByDescending { it.timestamp }.take(6).map { inv ->
        RecentOrder(
            id = inv.invoiceNumber,
            time = com.tmrestaurant.platform.formatDateTime(inv.timestamp),
            status = "CERRADA",
            total = inv.total
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        WelcomeBanner()

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.height(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(AppColors.Primary).clickable { showMenu = true }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Menu, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Menú", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            QuickAccessRow(
                items = MockData.quickAccessList,
                onItemClick = { title ->
                    when (title) {
                        "Nueva Venta" -> onNavigate(Screen.POS)
                        "Comandas" -> onNavigate(Screen.Comandas)
                        "Mesas" -> onNavigate(Screen.Mesas)
                        "Productos" -> onNavigate(Screen.Products)
                        "Facturas" -> onNavigate(Screen.Facturas)
                    }
                }
            )

            MainStatsRow(
                ventasDia = "RD\$ ${"%,.2f".format(totalVentasHoy)}",
                ordenesAbiertas = "$ordenesActivas",
                enCocina = "$comandasPendientes",
                facturasEmitidas = "${todayInvoices.size}"
            )

            SecondaryStatsRow(
                efectivo = "RD\$ ${"%,.2f".format(totalEfectivoHoy)}",
                tarjeta = "RD\$ ${"%,.2f".format(totalTarjetaHoy)}",
                transferencia = "RD\$ ${"%,.2f".format(totalTransferenciaHoy)}",
                mesasOcupadas = "$mesasOcupadas / $mesasTotal"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DashboardSectionCard(
                    title = "Productos mas vendidos hoy",
                    icon = Icons.Outlined.TrendingUp,
                    iconColor = AppColors.Primary,
                    onViewMore = { },
                    modifier = Modifier.weight(1f)
                ) {
                    TopSellingList(items = topProducts)
                }

                DashboardSectionCard(
                    title = "Ordenes recientes",
                    icon = Icons.Outlined.AccessTime,
                    iconColor = AppColors.Primary,
                    onViewMore = { },
                    modifier = Modifier.weight(1f)
                ) {
                    RecentOrdersList(orders = recentOrders)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showMenu) {
        MenuDashboardModal(
            currentScreen = currentScreen,
            onNavigate = { screen ->
                onNavigate(screen)
                showMenu = false
            },
            onDismiss = { showMenu = false }
        )
    }
}
