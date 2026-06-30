package com.tmrestaurant.platform

import java.awt.Toolkit

actual fun playCheckoutNotificationSound() {
    Toolkit.getDefaultToolkit().beep()
}
