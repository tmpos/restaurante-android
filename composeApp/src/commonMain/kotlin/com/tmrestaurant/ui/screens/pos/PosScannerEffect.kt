package com.tmrestaurant.ui.screens.pos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.tmrestaurant.platform.eloClearLastScanCode
import com.tmrestaurant.platform.eloGetLastScanCode
import com.tmrestaurant.platform.eloKeepScannerAlive
import com.tmrestaurant.platform.eloTestBarcodeScanner
import kotlinx.coroutines.delay

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun PosScannerEffect(
    state: PosState,
    onEvent: (PosEvent) -> Unit,
    focusRequester: FocusRequester
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Activate ELO SDK scanner listeners (broadcast + DeviceManager callbacks)
    LaunchedEffect(Unit) {
        eloClearLastScanCode()
        eloTestBarcodeScanner()
    }

    // Keep scanner ready without hammering the device service on every frame.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            eloKeepScannerAlive()
        }
    }

    // Poll for scanned codes from SDK/broadcast path every 300ms
    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            val code = eloGetLastScanCode()
            if (code != null && code.isNotBlank()) {
                eloClearLastScanCode()
                onEvent(PosEvent.ProcessScannedCode(code))
                eloKeepScannerAlive()
            }
        }
    }

    // Reactivate scanner immediately after a product is added to cart
    LaunchedEffect(state.cartItems) {
        if (state.cartItems.isNotEmpty()) {
            delay(100)
            eloKeepScannerAlive()
        }
    }

    // Auto-focus search field on entry so HID scanner can type into it
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        keyboardController?.hide()
        delay(100)
        keyboardController?.hide()
    }

    // Re-focus search field after product is added (searchQuery cleared) to keep scanner ready
    LaunchedEffect(state.searchQuery) {
        if (state.searchQuery.isEmpty()) {
            delay(150)
            try {
                focusRequester.requestFocus()
                keyboardController?.hide()
            } catch (_: Exception) {}
        }
    }

    // Auto-detect barcode after 500ms of no typing in the search field
    LaunchedEffect(state.searchQuery) {
        if (state.searchQuery.isBlank() || state.searchQuery.length < 3) return@LaunchedEffect
        delay(500)
        onEvent(PosEvent.AutoDetectBarcode)
    }
}
