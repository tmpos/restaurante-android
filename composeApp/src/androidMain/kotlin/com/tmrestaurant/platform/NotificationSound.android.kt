package com.tmrestaurant.platform

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.util.Log

actual fun playCheckoutNotificationSound() {
    Log.i("CheckoutNotification", "Playing checkout notification sound")
    val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
    tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 450)
    Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 600)
}
