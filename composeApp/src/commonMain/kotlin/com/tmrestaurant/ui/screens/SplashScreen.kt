package com.tmrestaurant.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.ImageFromBytes
import com.tmrestaurant.ui.data.settings.LocalSettingsState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val businessName = LocalSettingsState.current.settings.company.businessName

    val scaleAnim = remember { Animatable(0.3f) }
    val alphaAnim = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        alphaAnim.animateTo(1f, animationSpec = tween(600))
        textAlpha.animateTo(1f, animationSpec = tween(500, delayMillis = 300))
        delay(1200)
        alphaAnim.animateTo(0f, animationSpec = tween(400))
        onFinished()
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF7C3AED), Color(0xFF5B21B6)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(120.dp).scale(scaleAnim.value).alpha(alphaAnim.value)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "TM",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                businessName.ifBlank { "TM-RESTAURANTE" },
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Sistema POS",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(Modifier.height(40.dp))

            // Pulsing dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                repeat(3) { i ->
                    val dotAlpha = remember { Animatable(0.3f) }
                    LaunchedEffect(i) {
                        while (true) {
                            dotAlpha.animateTo(1f, animationSpec = tween(400, delayMillis = i * 200))
                            dotAlpha.animateTo(0.3f, animationSpec = tween(400))
                        }
                    }
                    Box(
                        Modifier.size(8.dp).clip(RoundedCornerShape(4.dp))
                            .background(Color.White).alpha(dotAlpha.value)
                    )
                }
            }
        }
    }
}
