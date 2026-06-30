package com.tmrestaurant.platform

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_DECODED_IMAGES = 64
private val decodedImageCache = object : LinkedHashMap<String, ImageBitmap>(
    MAX_DECODED_IMAGES,
    0.75f,
    true
) {
    override fun removeEldestEntry(
        eldest: MutableMap.MutableEntry<String, ImageBitmap>?
    ): Boolean = size > MAX_DECODED_IMAGES
}

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (name: String, bytes: ByteArray) -> Unit): () -> Unit {
    return {
        val dialog = FileDialog(null as Frame?, "Seleccionar imagen", FileDialog.LOAD)
        dialog.file = "*.jpg;*.jpeg;*.png;*.gif;*.webp"
        dialog.isVisible = true
        if (dialog.file != null) {
            val file = File(dialog.directory, dialog.file)
            try {
                val bytes = Files.readAllBytes(file.toPath())
                onImagePicked(file.name, bytes)
            } catch (_: Exception) {}
        }
    }
}

@Composable
actual fun ImageFromBytes(
    bytes: ByteArray?,
    modifier: Modifier,
    contentScale: ContentScale,
    cacheKey: String?
) {
    var bitmap by remember(bytes, cacheKey) {
        mutableStateOf(cacheKey?.let { synchronized(decodedImageCache) { decodedImageCache[it] } })
    }

    LaunchedEffect(bytes, cacheKey) {
        if (bytes == null || bitmap != null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.Default) {
            try {
                Image.makeFromEncoded(bytes).toComposeImageBitmap().also { decoded ->
                    cacheKey?.let { synchronized(decodedImageCache) { decodedImageCache[it] = decoded } }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
