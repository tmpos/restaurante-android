package com.tmrestaurant.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Orange = Color(0xFFF97316)

@Composable
fun LoginTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String> = listOf("Credenciales", "PIN Rapido")
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9))
    ) {
        Row(Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTab
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color.White
                            else Color.Transparent
                        )
                        .clickable(onClick = { onTabSelected(index) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (isSelected) Orange else Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
