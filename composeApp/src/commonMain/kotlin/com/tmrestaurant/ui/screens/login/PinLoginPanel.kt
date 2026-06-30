package com.tmrestaurant.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinLoginPanel(
    pin: String,
    pinError: Boolean,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Ingresa tu PIN de 4 digitos",
            color = Color(0xFF64748B),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        PinDots(pin = pin, isError = pinError)
        if (pinError) {
            Spacer(Modifier.height(10.dp))
            Text(
                "PIN incorrecto",
                color = Color(0xFFEF4444),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(28.dp))
        NumericKeypad(
            onDigit = onDigit,
            onDelete = onDelete,
            onClear = onClear
        )
    }
}
