package com.tmrestaurant.platform

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun rememberImagePickerLauncher(onImagePicked: (name: String, bytes: ByteArray) -> Unit): () -> Unit

@Composable
expect fun ImageFromBytes(
    bytes: ByteArray?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cacheKey: String? = null
)
