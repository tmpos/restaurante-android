package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TableBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.data.CartItem
import com.tmrestaurant.ui.data.Cliente
import com.tmrestaurant.ui.data.Mesa
import com.tmrestaurant.ui.data.MesasManager
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun CartPanel(
    cartItems: List<CartItem>,
    onRemoveProduct: (CartItem) -> Unit,
    onQuantityChange: (CartItem, Int) -> Unit,
    onClearCart: () -> Unit = {},
    onCheckout: (discountLabel: String, discountAmount: Double) -> Unit = { _, _ -> },
    onSendToKitchen: () -> Unit = {},
    onAddToMesa: () -> Unit = {},
    onAddTip: (label: String, pct: Double) -> Unit = { _, _ -> },
    onClearTip: () -> Unit = {},
    selectedClient: Cliente? = null,
    onSelectClient: () -> Unit = {},
    onClearClient: () -> Unit = {},
    deliveryAddress: String = "",
    deliveryPhone: String = "",
    deliveryNotes: String = "",
    onSetDelivery: (String, String, String) -> Unit = { _, _, _ -> },
    onClearDelivery: () -> Unit = {},
    onSplitBill: () -> Unit = {},
    splitBillActive: Boolean = false,
    onHoldOrder: () -> Unit = {},
    onRecallOrders: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val taxRate = 0.18
    val subtotal = cartItems.sumOf { it.product.price * it.effectiveQuantity + it.extrasCost }
    val subtotalPreTax = subtotal / (1.0 + taxRate)
    val taxAmount = subtotal - subtotalPreTax

    var discountAmount by remember { mutableStateOf(0.0) }
    var discountLabel by remember { mutableStateOf("") }
    var showDescuento by remember { mutableStateOf(false) }
    var showDelivery by remember { mutableStateOf(false) }
    var showMesasModal by remember { mutableStateOf(false) }
    val adjustedSubtotal = (subtotal - discountAmount).coerceAtLeast(0.0)
    val total = adjustedSubtotal

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Surface)
            .border(0.5.dp, AppColors.Border)
    ) {
        CartHeader(
            itemCount = cartItems.size,
            onClearCart = onClearCart,
            onHoldOrder = onHoldOrder,
            onRecallOrders = onRecallOrders
        )
        SelectedClientRow(selectedClient, onSelectClient, onClearClient)
        DeliveryInfoRow(deliveryAddress, deliveryPhone, deliveryNotes, onClearDelivery)

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ShoppingCart, null, tint = AppColors.Border, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Carrito vacio", color = AppColors.TextSecondary, fontSize = 15.sp)
                    Text("Seleccione productos para agregar", color = AppColors.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        splitBillActive = splitBillActive,
                        onRemove = { onRemoveProduct(item) },
                        onQuantityChange = { newQty -> onQuantityChange(item, newQty) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(AppColors.Surface).padding(16.dp)
        ) {
            TotalsBox(
                subtotalPreTax = subtotalPreTax,
                taxAmount = taxAmount,
                total = total,
                discountLabel = discountLabel,
                discountAmount = discountAmount
            )

            Spacer(Modifier.height(10.dp))

            SecondaryButton(
                text = "  Aplicar Descuento",
                icon = Icons.Outlined.Percent,
                onClick = { showDescuento = true },
                height = 48.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth().height(40.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Propina:", color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
                listOf("10%" to 0.10, "15%" to 0.15, "20%" to 0.20).forEach { (label, pct) ->
                    Box(
                        Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(8.dp))
                            .background(AppColors.Background).clickable { onAddTip(label, pct) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Primary)
                    }
                }
                Box(Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.Background).clickable { onClearTip(); discountAmount = 0.0; discountLabel = "" }, contentAlignment = Alignment.Center) {
                    Text("Quitar", fontSize = 11.sp, color = AppColors.Danger)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = "Cocina",
                    icon = Icons.Outlined.RestaurantMenu,
                    onClick = onSendToKitchen,
                    height = 52.dp,
                    modifier = Modifier.weight(1f)
                )
                OrangeButton(
                    text = "Mesa",
                    icon = Icons.Outlined.TableBar,
                    onClick = { showMesasModal = true },
                    height = 52.dp,
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "Cajón",
                    icon = Icons.Outlined.Print,
                    onClick = onOpenDrawer,
                    height = 52.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(if (splitBillActive) Color(0xFFF0FDF4) else AppColors.Background)
                        .border(1.dp, if (splitBillActive) Color(0xFFBBF7D0) else AppColors.Border, RoundedCornerShape(10.dp))
                        .clickable(onClick = onSplitBill),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Group, null, tint = if (splitBillActive) Color(0xFF16A34A) else AppColors.Primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (splitBillActive) "Cuenta Dividida" else "Dividir Cuenta",
                            color = if (splitBillActive) Color(0xFF16A34A) else AppColors.Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                OutlinedButtonDelivery(
                    text = "Delivery",
                    icon = Icons.Outlined.LocalShipping,
                    onClick = { showDelivery = true },
                    height = 40.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            FullWidthButton(
                text = "Cobrar RD\$ ${"%.2f".format(total)}",
                icon = Icons.Outlined.CreditCard,
                onClick = { onCheckout(discountLabel, discountAmount) },
                height = 56.dp
            )
        }
    }

    if (showDescuento) {
        DescuentoModal(
            subtotal = subtotal,
            onApply = { label, amount ->
                discountLabel = label
                discountAmount = amount
                showDescuento = false
            },
            onDismiss = { showDescuento = false }
        )
    }

    if (showDelivery) {
        DeliveryModal(
            onApply = { dir, tel, notas ->
                onSetDelivery(dir, tel, notas)
                showDelivery = false
            },
            onDismiss = { showDelivery = false }
        )
    }

    if (showMesasModal) {
        MesasSelectorModal(
            mesas = MesasManager.mesas,
            onSelect = { mesa ->
                cartItems.forEach { item ->
                    MesasManager.addProductToMesa(mesa.id, item.product, item.quantity)
                }
                showMesasModal = false
                onClearCart()
            },
            onDismiss = { showMesasModal = false }
        )
    }
}

@Composable
private fun SelectedClientRow(client: Cliente?, onSelect: () -> Unit, onClear: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(if (client != null) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
            .clickable(onClick = onSelect).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(if (client != null) Color(0xFFDBEAFE) else Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Person, null, tint = if (client != null) Color(0xFF2563EB) else AppColors.Gray, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(client?.nombre ?: "Seleccionar cliente", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.TextPrimary)
            Text(
                client?.let { listOf(it.telefono, it.rnc).filter(String::isNotBlank).joinToString(" | ").ifBlank { it.tipo } }
                    ?: "Asigne esta venta a un cliente",
                color = AppColors.TextSecondary,
                fontSize = 9.sp
            )
        }
        if (client != null) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Color.White).clickable(onClick = onClear), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Close, "Quitar cliente", tint = AppColors.Danger, modifier = Modifier.size(15.dp))
            }
        } else {
            Icon(Icons.Outlined.Add, null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
}

@Composable
private fun OutlinedButtonDelivery(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    height: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(height).clip(RoundedCornerShape(10.dp))
            .background(AppColors.Background).border(1.dp, AppColors.Orange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = AppColors.Orange, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = AppColors.Orange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DeliveryInfoRow(address: String, phone: String, notes: String, onClear: () -> Unit) {
    if (address.isBlank() && phone.isBlank()) return
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF7ED))
            .border(1.dp, Color(0xFFFED7AA), RoundedCornerShape(12.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.LocalShipping, null, tint = AppColors.Orange, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(address.ifBlank { "Delivery sin direccion" }, color = AppColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(listOf(phone, notes).filter { it.isNotBlank() }.joinToString(" | ").ifBlank { "Sin telefono" }, color = AppColors.TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).clickable(onClick = onClear), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Close, null, tint = AppColors.Danger, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun CartHeader(itemCount: Int, onClearCart: () -> Unit = {}, onHoldOrder: () -> Unit = {}, onRecallOrders: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.PrimaryLight), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.ShoppingCart, null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Orden Actual", color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("$itemCount productos", color = AppColors.TextSecondary, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                .clickable(onClick = onHoldOrder), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Pause, "Pausar", tint = AppColors.IconGray, modifier = Modifier.size(20.dp))
            }
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.DangerLight).clickable(onClick = onClearCart), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Delete, "Vaciar carrito", tint = AppColors.Danger, modifier = Modifier.size(20.dp))
            }
            CounterBadge(count = itemCount, backgroundColor = AppColors.BadgeOrange)
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
}

@Composable
private fun CartItemRow(item: CartItem, splitBillActive: Boolean = false, onRemove: () -> Unit, onQuantityChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.CartItemBg).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val displayName = if (item.extrasNote.isNotBlank()) "${item.product.name} + ${item.extrasNote}" else item.product.name
                Text(displayName, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (splitBillActive && item.dinerIndex > 0) {
                    Spacer(Modifier.width(4.dp))
                    val dc = listOf(Color(0xFF3B82F6), Color(0xFF22C55E), Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6))
                    Box(
                        Modifier.size(20.dp).clip(androidx.compose.foundation.shape.CircleShape)
                            .background(dc[(item.dinerIndex - 1) % dc.size]),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${item.dinerIndex}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (item.product.description.isNotEmpty()) {
                Text(item.product.description, color = AppColors.TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (item.selectedModifiers.isNotEmpty()) {
                val modText = item.selectedModifiers.map { it.optionName }.joinToString(", ")
                Text(modText, color = AppColors.TextSecondary, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(4.dp))
            val unitPrice = item.product.price + item.extrasCost / item.quantity.coerceAtLeast(1)
            val lineTotal = item.product.price * item.effectiveQuantity + item.extrasCost
            val unitLabel = if (item.product.sellByWeight) "/ lb" else ""
            Text("RD\$ ${"%.2f".format(unitPrice)}$unitLabel", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            val qtyDisplay = if (item.weightQuantity > 0) "${item.weightQuantity} lbs" else "x ${item.quantity}"
            Text("$qtyDisplay  |  Total: RD\$ ${"%.2f".format(lineTotal)}", color = AppColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(AppColors.PrimaryLight).clickable { onQuantityChange(item.quantity - 1) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Remove, "Menos", tint = AppColors.Primary, modifier = Modifier.size(26.dp))
            }
            val qtyText = if (item.weightQuantity > 0) "${item.weightQuantity} lbs" else "${item.quantity}"
            Text(qtyText, color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(AppColors.PrimaryLight).clickable { onQuantityChange(item.quantity + 1) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Add, "Mas", tint = AppColors.Primary, modifier = Modifier.size(26.dp))
            }
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(AppColors.DangerLight).clickable(onClick = onRemove), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Delete, "Eliminar", tint = AppColors.Danger, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
fun TotalsBox(
    subtotalPreTax: Double,
    taxAmount: Double,
    total: Double,
    discountLabel: String = "",
    discountAmount: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Subtotal:", color = AppColors.TextSecondary, fontSize = 13.sp)
            Text("RD\$ ${"%.2f".format(subtotalPreTax)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(6.dp))
        if (discountAmount > 0 && discountLabel.isNotBlank()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Descuento:", color = Color(0xFF16A34A), fontSize = 13.sp)
                Text("-RD\$ ${"%.2f".format(discountAmount)}", color = Color(0xFF16A34A), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Text(discountLabel, color = AppColors.TextSecondary, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ITBIS (18%):", color = AppColors.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                Badge(text = "Incluido", backgroundColor = AppColors.InfoLight, textColor = AppColors.Info, fontSize = 9)
            }
            Text("RD\$ ${"%.2f".format(taxAmount)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total:", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("RD\$ ${"%.2f".format(total)}", color = AppColors.Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MesasSelectorModal(
    mesas: List<Mesa>,
    onSelect: (Mesa) -> Unit,
    onDismiss: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(500.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.TableBar, null, tint = AppColors.Orange, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Agregar a Mesa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
                }
            }

            if (mesas.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No hay mesas disponibles", color = AppColors.TextSecondary, fontSize = 14.sp)
                }
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    modifier = Modifier.heightIn(max = 300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mesas.size) { idx ->
                        val mesa = mesas[idx]
                        val bg = if (mesa.isOccupied) Color(0xFFFEF3C7) else Color(0xFFF0FDF4)
                        val fg = if (mesa.isOccupied) Color(0xFFD97706) else Color(0xFF16A34A)
                        Column(
                            Modifier.height(80.dp).clip(RoundedCornerShape(12.dp)).background(bg).clickable { onSelect(mesa) }.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.TableBar, null, tint = fg, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(mesa.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg, textAlign = TextAlign.Center)
                            Text(if (mesa.isOccupied) "${mesa.items.size} items" else "Libre", fontSize = 9.sp, color = fg.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(Modifier.height(38.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background).clickable(onClick = onDismiss).padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun Double.ifZero(block: () -> Double) = if (this == 0.0) block() else this
