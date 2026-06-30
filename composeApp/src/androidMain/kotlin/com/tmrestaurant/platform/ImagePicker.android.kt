package com.tmrestaurant.platform

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val decodedImageCache = object : LruCache<String, Bitmap>(32 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
}

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (name: String, bytes: ByteArray) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { u ->
            val name = getFileName(context, u)
            val bytes = readBytes(context, u)
            if (bytes != null) onImagePicked(name, bytes)
        }
    }
    return { launcher.launch("image/*") }
}

@Composable
actual fun ImageFromBytes(
    bytes: ByteArray?,
    modifier: Modifier,
    contentScale: ContentScale,
    cacheKey: String?
) {
    var bitmap by remember(bytes, cacheKey) {
        mutableStateOf(cacheKey?.let(decodedImageCache::get))
    }

    LaunchedEffect(bytes, cacheKey) {
        if (bytes == null || bitmap != null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.Default) {
            decodeThumbnail(bytes)?.also { decoded ->
                cacheKey?.let { decodedImageCache.put(it, decoded) }
            }
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

private fun decodeThumbnail(bytes: ByteArray): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 512 || bounds.outHeight / sampleSize > 512) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
} catch (_: Exception) {
    null
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "image.jpg"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) name = cursor.getString(nameIndex) ?: "image.jpg"
    }
    return name
}

private fun readBytes(context: Context, uri: Uri): ByteArray? {
    return context.contentResolver.openInputStream(uri)?.use { input ->
        val baos = ByteArrayOutputStream()
        input.copyTo(baos)
        baos.toByteArray()
    }
}
