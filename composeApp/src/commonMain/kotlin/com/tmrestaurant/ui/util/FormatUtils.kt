package com.tmrestaurant.ui.util

import kotlin.math.roundToLong

fun formatCurrency(amount: Double): String {
    val whole = amount.toLong()
    val cents = ((amount - whole) * 100).roundToLong()
    return "RD\$ $whole.${if (cents < 10) "0" else ""}$cents"
}
