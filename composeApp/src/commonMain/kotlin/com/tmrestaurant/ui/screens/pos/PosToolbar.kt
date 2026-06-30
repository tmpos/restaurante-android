package com.tmrestaurant.ui.screens.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.ui.components.*
import com.tmrestaurant.ui.theme.AppColors

@Composable
internal fun PosToolbar(
    isGridView: Boolean,
    quickAddEnabled: Boolean,
    onToggleQuickAdd: (Boolean) -> Unit,
    onFreeSaleClick: () -> Unit,
    onMesasClick: () -> Unit,
    onClientsClick: () -> Unit,
    selectedClientCount: Int,
    copies: Int,
    onCopiesChange: (Int) -> Unit,
    onToggleView: () -> Unit,
    onInvoiceHistoryClick: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    searchFocusRequester: FocusRequester? = null
) {
    Column(Modifier.fillMaxWidth().background(AppColors.Surface).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SearchInput(value = searchQuery, onValueChange = onSearchChange,
                placeholder = "Buscar producto por nombre, código o código de barra...",
                onSearch = onSearchSubmit, modifier = Modifier.weight(1f),
                focusRequester = searchFocusRequester,
                showSoftKeyboard = false)
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (quickAddEnabled) AppColors.SuccessLight else AppColors.Background)
                    .padding(start = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Bolt,
                    null,
                    tint = if (quickAddEnabled) AppColors.Success else AppColors.IconGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Rapido",
                    color = if (quickAddEnabled) AppColors.Success else AppColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(6.dp))
                Switch(
                    checked = quickAddEnabled,
                    onCheckedChange = onToggleQuickAdd,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.Success,
                        checkedTrackColor = AppColors.SuccessLight,
                        uncheckedThumbColor = AppColors.Gray,
                        uncheckedTrackColor = AppColors.Border
                    )
                )
            }
            PrimaryButton(text = "$  Venta libre", onClick = onFreeSaleClick, height = 48.dp)
            IconButtonBox(icon = Icons.Outlined.Restaurant, onClick = onMesasClick, size = 48.dp)
            Box {
                IconButtonBox(icon = Icons.Outlined.Person, onClick = onClientsClick, size = 48.dp, isActive = selectedClientCount > 0)
                if (selectedClientCount > 0) {
                    CounterBadge(count = selectedClientCount, modifier = Modifier.align(Alignment.TopEnd).offset(4.dp, (-4).dp))
                }
            }
            IconButtonBox(icon = Icons.Outlined.Receipt, onClick = onInvoiceHistoryClick, size = 48.dp)
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CopyStepButton(
                    text = "-",
                    onClick = { onCopiesChange((copies - 1).coerceAtLeast(1)) }
                )
                Box(
                    Modifier
                        .height(34.dp)
                        .widthIn(min = 74.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(AppColors.Surface)
                        .clickable { onCopiesChange(if (copies >= 5) 1 else copies + 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$copies copias",
                        color = AppColors.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                CopyStepButton(
                    text = "+",
                    onClick = { onCopiesChange((copies + 1).coerceAtMost(5)) }
                )
            }
            IconButtonBox(icon = Icons.Outlined.GridView, onClick = { if (!isGridView) onToggleView() },
                isActive = isGridView, size = 48.dp)
            IconButtonBox(icon = Icons.Outlined.ViewList, onClick = { if (isGridView) onToggleView() },
                isActive = !isGridView, size = 48.dp)
        }
    }
}

@Composable
private fun CopyStepButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(width = 34.dp, height = 34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = AppColors.Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
