package com.tmrestaurant

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build

object HomeShortcut {
    private const val PREFS = "tm_restaurant_launcher"
    private const val SHORTCUT_CREATED = "home_shortcut_created"
    private const val SHORTCUT_ID = "tm_restaurant_home"

    fun ensureCreated(context: Context) {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (preferences.getBoolean(SHORTCUT_CREATED, false)) return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val requested = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(ShortcutManager::class.java)
            if (manager?.isRequestPinShortcutSupported == true) {
                val shortcut = ShortcutInfo.Builder(context, SHORTCUT_ID)
                    .setShortLabel("TM-RESTAURANTE")
                    .setLongLabel("TM-RESTAURANTE")
                    .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(launchIntent)
                    .build()
                manager.requestPinShortcut(shortcut, null)
            } else {
                false
            }
        } else {
            @Suppress("DEPRECATION")
            context.sendBroadcast(
                Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, "TM-RESTAURANTE")
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                    putExtra(
                        Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher)
                    )
                    putExtra("duplicate", false)
                }
            )
            true
        }

        if (requested) {
            preferences.edit().putBoolean(SHORTCUT_CREATED, true).apply()
        }
    }
}
