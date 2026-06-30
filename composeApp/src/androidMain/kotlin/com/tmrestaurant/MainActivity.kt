package com.tmrestaurant

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import com.tmrestaurant.platform.onHidKeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tmrestaurant.platform.appContext
import com.tmrestaurant.platform.currentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        currentActivity = this
        HomeShortcut.ensureCreated(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            var isFullscreen by remember { mutableStateOf(true) }

            LaunchedEffect(isFullscreen) {
                applyFullscreen(isFullscreen)
            }

            Box(
                modifier = if (isFullscreen) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.fillMaxSize().systemBarsPadding()
                }
            ) {
                App(
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = { isFullscreen = !isFullscreen }
                )
            }
        }
    }

    private fun applyFullscreen(enabled: Boolean) {
        if (enabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Only intercept events from physical/external HID devices (scanner),
        // not from the soft keyboard, so text fields work normally.
        val isHardwareKey = event.device?.let { !it.isVirtual } ?: false
        if (isHardwareKey) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                android.util.Log.d("SCANNER_ACTIVITY", "HW key: keyCode=${event.keyCode} char=${event.unicodeChar} device=${event.device?.name}")
            }
            val consumed = onHidKeyEvent(event.keyCode, event.action, event.unicodeChar)
            if (consumed) return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0) {
            applyFullscreen(true)
        }
    }
}
