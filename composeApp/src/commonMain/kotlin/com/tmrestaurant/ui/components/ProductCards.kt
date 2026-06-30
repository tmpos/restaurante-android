package com.tmrestaurant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.ImageFromBytes
import com.tmrestaurant.ui.data.LocalProductState
import com.tmrestaurant.ui.data.Product
import com.tmrestaurant.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProductCardPOS(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val productState = LocalProductState.current
    var imageBytes by remember(product.imagePath) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(product.imagePath) {
        imageBytes = withContext(Dispatchers.IO) {
            product.imagePath?.let { productState.getImageBytes(it) }
        }
    }

    Column(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.PlaceholderBg),
            contentAlignment = Alignment.Center
        ) {
            if (product.imagePath != null && imageBytes != null) {
                ImageFromBytes(
                    bytes = imageBytes,
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    cacheKey = product.imagePath
                )
            } else if (product.imagePath != null) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = AppColors.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
            if (product.favorite) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Favorito",
                    tint = AppColors.StarYellow,
                    modifier = Modifier.size(20.dp).align(Alignment.TopEnd).padding(8.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = product.name,
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "RD\$ ${"%.2f".format(product.price)}",
            color = AppColors.Primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        if (product.code.isNotEmpty()) {
            Text(
                text = "Código: ${product.code}",
                color = AppColors.TextSecondary,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(2.dp))
        }

        Text(
            text = "Disponible: ${product.stock} unidad",
            color = AppColors.Success,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProductCardAdmin(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val productState = LocalProductState.current
    var imageBytes by remember(product.imagePath) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(product.imagePath) {
        imageBytes = withContext(Dispatchers.IO) {
            product.imagePath?.let { productState.getImageBytes(it) }
        }
    }

    Column(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(AppColors.PlaceholderBg),
            contentAlignment = Alignment.Center
        ) {
            if (product.imagePath != null && imageBytes != null) {
                ImageFromBytes(
                    bytes = imageBytes,
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    cacheKey = product.imagePath
                )
            } else if (product.imagePath != null) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(44.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = AppColors.Gray,
                    modifier = Modifier.size(44.dp)
                )
            }
            if (product.favorite) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Favorito",
                    tint = AppColors.StarYellow,
                    modifier = Modifier.size(18.dp).align(Alignment.TopEnd).padding(10.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = product.name,
                color = AppColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (product.code.isNotEmpty()) {
                Text(
                    text = "Código: ${product.code}",
                    color = AppColors.TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(6.dp))

            CategoryBadge(text = product.category)

            Spacer(Modifier.height(6.dp))

            Text(
                text = "RD\$ ${"%.2f".format(product.price)}",
                color = AppColors.Primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Stock: ${product.stock}",
                color = AppColors.TextSecondary,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.PrimaryLight)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = AppColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.DangerLight)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = AppColors.Danger,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
