package com.tmrestaurant.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DotSize = 56.dp
private val DotSpacing = 16.dp

@Composable
fun PinDots(
    pin: String,
    pinLength: Int = 4,
    isError: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(DotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pinLength) {
            val filled = i < pin.length
            val bgColor = when {
                isError -> Color(0xFFFEE2E2)
                filled -> Color(0x1AF97316)
                else -> Color(0xFFF1F5F9)
            }
            val borderColor = when {
                isError -> Color(0xFFEF4444)
                filled -> Color(0xFFF97316)
                else -> Color(0xFFCBD5E1)
            }
            Box(
                Modifier
                    .size(DotSize)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (filled) {
                    Box(
                        Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isError) Color(0xFFEF4444) else Color(0xFFF97316))
                    )
                }
            }
        }
    }
}
