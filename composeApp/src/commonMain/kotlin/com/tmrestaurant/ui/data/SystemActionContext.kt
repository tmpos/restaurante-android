package com.tmrestaurant.ui.data

object SystemActionContext {
    private var privilegedDepth = 0

    fun isPrivileged(): Boolean = privilegedDepth > 0

    fun <T> runPrivileged(block: () -> T): T {
        privilegedDepth++
        return try {
            block()
        } finally {
            privilegedDepth--
        }
    }
}
