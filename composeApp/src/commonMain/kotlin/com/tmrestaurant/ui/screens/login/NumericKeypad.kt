package com.tmrestaurant.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KeySize = 80.dp
private val KeySpacing = 12.dp

@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(KeySpacing)) {
        for (row in listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))) {
            Row(horizontalArrangement = Arrangement.spacedBy(KeySpacing)) {
                row.forEach { digit ->
                    KeyButton(digit = digit, onClick = { onDigit(digit) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(KeySpacing)) {
            KeyButton(digit = "C", onClick = onClear, isSpecial = true)
            KeyButton(digit = "0", onClick = { onDigit("0") })
            Box(
                Modifier.size(KeySize)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Backspace,
                    "Borrar",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun KeyButton(digit: String, onClick: () -> Unit, isSpecial: Boolean = false) {
    val bg = if (isSpecial) Color(0xFFFEF2F2) else Color(0xFFF1F5F9)
    val border = if (isSpecial) Color(0xFFFECACA) else Color(0xFFCBD5E1)
    val textColor = if (isSpecial) Color(0xFFEF4444) else Color(0xFF0F172A)
    Box(
        Modifier.size(KeySize)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            digit,
            color = textColor,
            fontSize = 26.sp,
            fontWeight = if (isSpecial) FontWeight.SemiBold else FontWeight.Bold
        )
    }
}
