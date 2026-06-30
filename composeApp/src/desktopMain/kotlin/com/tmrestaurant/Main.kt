package com.tmrestaurant

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(
        width = 1440.dp,
        height = 900.dp,
        position = WindowPosition(Alignment.Center)
    )
    val isFullscreen = windowState.placement == WindowPlacement.Fullscreen

    Window(
        onCloseRequest = ::exitApplication,
        title = "TM-RESTAURANTE - Sistema POS",
        state = windowState
    ) {
        App(
            isFullscreen = isFullscreen,
            onToggleFullscreen = {
                windowState.placement = if (isFullscreen) {
                    WindowPlacement.Floating
                } else {
                    WindowPlacement.Fullscreen
                }
            }
        )
    }
}
