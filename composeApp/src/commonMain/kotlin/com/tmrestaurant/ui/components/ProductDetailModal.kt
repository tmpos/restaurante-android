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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
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
import com.tmrestaurant.platform.ImageFromBytes
import com.tmrestaurant.ui.data.Extra
import com.tmrestaurant.ui.data.ExtrasManager
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.ModifierManager
import com.tmrestaurant.ui.data.ModifierSelection
import com.tmrestaurant.ui.data.Product
import com.tmrestaurant.ui.theme.AppColors

@Composable
fun ProductDetailModal(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (quantity: Int, extrasCost: Double, extrasNote: String, weightQuantity: Double, courseType: String) -> Unit
) {
    val productState = LocalProductState.current
    val imageBytes = product.imagePath?.let { productState.getImageBytes(it) }
    var quantity by remember { mutableIntStateOf(1) }
    var weightText by remember { mutableStateOf("1.00") }
    val availableExtras = ExtrasManager.extrasFor(product.id)
    val isBeverage = product.category.trim().lowercase().contains("bebida")
    val availableGuarniciones = if (isBeverage) emptyList() else ExtrasManager.allGuarniciones()
    var selectedExtras by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedGuarnicion by remember { mutableStateOf<String?>(null) }
    var showGuarniciones by remember { mutableStateOf(false) }
    val assignedModifierGroups = product.modifierGroupIds.split(",").filter { it.isNotBlank() }.mapNotNull { ModifierManager.getById(it) }
    var selectedModifiers by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var selectedCourse by remember { mutableStateOf("") }
    val courseOptions = listOf("Entrada", "Fuerte", "Postre")

    val extrasTotal = availableExtras.filter { it.id in selectedExtras }.sumOf { it.price }
    val guarnicionSelected = availableGuarniciones.find { it.id == selectedGuarnicion }
    val guarnicionPrice = guarnicionSelected?.price ?: 0.0
    val extrasNames = availableExtras.filter { it.id in selectedExtras }.joinToString(", ") { it.name }
    val guarnicionName = guarnicionSelected?.name ?: ""
    val modifierCost = assignedModifierGroups.flatMap { group ->
        selectedModifiers[group.id].orEmpty().mapNotNull { optId ->
            group.options.find { it.id == optId }?.price
        }
    }.sum()
    val subtotal = (product.price * quantity) + (extrasTotal * quantity) + (guarnicionPrice * quantity) + modifierCost * quantity

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(480.dp).clip(RoundedCornerShape(24.dp)).background(AppColors.Surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(AppColors.PlaceholderBg),
                contentAlignment = Alignment.Center
            ) {
                if (product.imagePath != null && imageBytes != null) {
                    ImageFromBytes(bytes = imageBytes, modifier = Modifier.fillMaxWidth().height(240.dp))
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = AppColors.Gray,
                        modifier = Modifier.size(80.dp)
                    )
                }
                if (product.favorite) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Favorito",
                        tint = AppColors.StarYellow,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .clickable(onClick = onDismiss)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.Surface.copy(alpha = 0.9f))
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cerrar",
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = product.name,
                    color = AppColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(2.dp))
                }

                if (product.code.isNotEmpty()) {
                    Text(
                        text = "Código: ${product.code}",
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RD\$ ${"%.2f".format(product.price)}",
                        color = AppColors.Primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (product.sellByWeight) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.Background)
                                .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (weightText.isEmpty()) Text("0.00", color = AppColors.Gray, fontSize = 16.sp)
                            BasicTextField(
                                value = weightText,
                                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' } && it.count { c -> c == '.' } <= 1) weightText = it },
                                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxSize(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("lbs", color = AppColors.TextSecondary, fontSize = 14.sp)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.PrimaryLight)
                                    .clickable { if (quantity > 1) quantity-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Remove,
                                    contentDescription = "Menos",
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "$quantity",
                                color = AppColors.TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.PrimaryLight)
                                    .clickable { quantity++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "Más",
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppColors.DividerColor)
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Subtotal:",
                            color = AppColors.TextSecondary,
                            fontSize = 16.sp
                        )
                        if (extrasTotal > 0 || guarnicionPrice > 0) {
                            Text(
                                text = "Extras/Guarn: +RD\$ ${"%.2f".format((extrasTotal + guarnicionPrice) * quantity)}",
                                color = AppColors.Primary,
                                fontSize = 13.sp
                            )
                        }
                        if (modifierCost > 0) {
                            Text(
                                text = "Modificadores: +RD\$ ${"%.2f".format(modifierCost * quantity)}",
                                color = AppColors.Primary,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Text(
                        text = "RD\$ ${"%.2f".format(subtotal)}",
                        color = AppColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(14.dp))

                if (availableGuarniciones.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEF3C7)).clickable { showGuarniciones = true }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFDE68A)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.RestaurantMenu, null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Guarnicion", color = Color(0xFFD97706), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(guarnicionName.ifBlank { "Seleccionar guarnicion" }, color = AppColors.TextSecondary, fontSize = 12.sp)
                        }
                        Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (availableExtras.isNotEmpty()) {
                    Text("Extras disponibles:", color = AppColors.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    availableExtras.forEach { extra ->
                        val isSelected = extra.id in selectedExtras
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppColors.PrimaryLight else AppColors.Background)
                                .clickable {
                                    selectedExtras = if (isSelected) selectedExtras - extra.id else selectedExtras + extra.id
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(if (isSelected) AppColors.Primary else AppColors.Border), contentAlignment = Alignment.Center) {
                                if (isSelected) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(extra.name, color = AppColors.TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("+RD\$ ${"%.2f".format(extra.price)}", color = if (isSelected) AppColors.Primary else AppColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                }

                if (assignedModifierGroups.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Modificadores:", color = AppColors.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    assignedModifierGroups.forEach { group ->
                        Column(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(group.name, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                if (group.required) {
                                    Spacer(Modifier.width(6.dp))
                                    Text("(Obligatorio)", color = AppColors.Danger, fontSize = 11.sp)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            group.options.forEach { option ->
                                val isSelected = selectedModifiers[group.id]?.contains(option.id) == true
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) AppColors.PrimaryLight else AppColors.Background)
                                        .clickable {
                                            val current = selectedModifiers[group.id].orEmpty().toMutableList()
                                            if (group.maxSelections <= 1) {
                                                selectedModifiers = selectedModifiers + (group.id to listOf(option.id))
                                            } else {
                                                if (isSelected) current.remove(option.id) else if (current.size < group.maxSelections) current.add(option.id)
                                                selectedModifiers = selectedModifiers + (group.id to current)
                                            }
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (group.maxSelections <= 1) {
                                        Box(Modifier.size(22.dp).clip(RoundedCornerShape(11.dp)).background(if (isSelected) AppColors.Primary else AppColors.Border), contentAlignment = Alignment.Center) {
                                            if (isSelected) Box(Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color.White))
                                        }
                                    } else {
                                        Box(Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(if (isSelected) AppColors.Primary else AppColors.Border), contentAlignment = Alignment.Center) {
                                            if (isSelected) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(option.name, color = AppColors.TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    if (option.price > 0) {
                                        Text("+RD\$ ${"%.2f".format(option.price)}", color = if (isSelected) AppColors.Primary else AppColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.DividerColor))
                }

                Spacer(Modifier.height(16.dp))
                Text("Tiempo de comida:", color = AppColors.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    courseOptions.forEach { course ->
                        val isSelected = selectedCourse == course
                        val chipColor = when (course) {
                            "Entrada" -> Color(0xFF0891B2)
                            "Fuerte" -> Color(0xFFD97706)
                            "Postre" -> Color(0xFF7C3AED)
                            else -> AppColors.Primary
                        }
                        Box(
                            Modifier.clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) chipColor.copy(alpha = 0.15f) else AppColors.Background)
                                .clickable { selectedCourse = if (isSelected) "" else course }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(course, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) chipColor else AppColors.TextSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, AppColors.Border, RoundedCornerShape(14.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancelar",
                            color = AppColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.Primary)
                            .clickable {
                                val finalQty = if (product.sellByWeight) 1 else quantity
                                val finalWeight = if (product.sellByWeight) (weightText.toDoubleOrNull() ?: 1.0) else 0.0
                                val modifierNote = selectedModifiers.entries.joinToString(", ") { entry ->
                                    val g = ModifierManager.getById(entry.key)
                                    val groupName = g?.name ?: ""
                                    val optionNames = entry.value.mapNotNull { optId -> g?.options?.find { it.id == optId }?.name }
                                    "$groupName: ${optionNames.joinToString("/")}"
                                }
                                val extrasNote = buildString {
                                    if (extrasNames.isNotBlank()) append(extrasNames)
                                    if (guarnicionName.isNotBlank()) {
                                        if (isNotEmpty()) append(", ")
                                        append(guarnicionName)
                                    }
                                    if (modifierNote.isNotBlank()) {
                                        if (isNotEmpty()) append(" | ")
                                        append(modifierNote)
                                    }
                                }
                                onAddToCart(finalQty, (extrasTotal + guarnicionPrice) * finalQty + modifierCost * finalQty, extrasNote, finalWeight, selectedCourse) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Agregar  •  RD\$ ${"%.2f".format(subtotal)}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGuarniciones) {
        GuarnicionesModal(
            guarniciones = availableGuarniciones,
            selectedId = selectedGuarnicion,
            onSelect = { id -> selectedGuarnicion = id; showGuarniciones = false },
            onDismiss = { showGuarniciones = false }
        )
    }
}

@Composable
private fun GuarnicionesModal(
    guarniciones: List<Extra>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(400.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.RestaurantMenu, null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("Seleccionar Guarnicion", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.Background).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null, tint = AppColors.IconGray, modifier = Modifier.size(16.dp))
                }
            }

            if (guarniciones.isEmpty()) {
                Text("No hay guarniciones disponibles", color = AppColors.TextSecondary, fontSize = 13.sp)
            } else {
                guarniciones.forEach { guarn ->
                    val isSelected = guarn.id == selectedId
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFFFEF3C7) else AppColors.Background)
                            .clickable { onSelect(guarn.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(24.dp).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Color(0xFFD97706) else AppColors.Border), contentAlignment = Alignment.Center) {
                            if (isSelected) Box(Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color.White))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(guarn.name, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(if (guarn.price > 0) "+RD\$ ${"%.2f".format(guarn.price)}" else "Incluida", color = if (isSelected) Color(0xFFD97706) else AppColors.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { onSelect("") ; onDismiss() }, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(44.dp)) {
                        Text("Ninguna", color = AppColors.TextSecondary, fontSize = 13.sp)
                    }
                    Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)), modifier = Modifier.weight(1f).height(44.dp)) {
                        Text("Listo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
