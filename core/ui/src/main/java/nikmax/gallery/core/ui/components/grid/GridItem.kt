package nikmax.gallery.core.ui.components.grid

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import nikmax.gallery.core.ui.R
import nikmax.gallery.core.ui.theme.GalleryTheme

@Composable
internal fun GridItem(
    image: String?,
    name: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isVideo: Boolean = false,
    isFolder: Boolean = false,
    isSecondaryVolume: Boolean = false,
    folderFilesCount: Int = 0,
    isNotWritable: Boolean = false
) {
    val borderWidth by animateDpAsState(
        if (isSelected) 2.dp
        else CardDefaults.outlinedCardBorder().width
    )
    val selectionColor by animateColorAsState(
        if (isSelected) when (isNotWritable) {
            true -> MaterialTheme.colorScheme.error
            false -> MaterialTheme.colorScheme.secondary
        }
        else MaterialTheme.colorScheme.outlineVariant
    )

    OutlinedCard(
        border = BorderStroke(borderWidth, selectionColor),
        modifier = modifier
    ) {
        Column {
            val imageRequest = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .dispatcher(Dispatchers.IO)
                .memoryCacheKey(image)
                .diskCacheKey(image)
                .placeholder(R.drawable.gallery_image_placeholder)
                .error(R.drawable.gallery_image_placeholder)
                .fallback(R.drawable.gallery_image_placeholder)
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
            Box {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                )
                if (isSelected) {
                    when (isNotWritable) {
                        true -> IconCorner(
                            icon = Icons.Default.Lock,
                            color = selectionColor,
                            contentDescription = stringResource(R.string.item_selected),
                            bottomEndRadius = 30F,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        false -> IconCorner(
                            icon = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.item_selected),
                            bottomEndRadius = 30F,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }
                }
                if (isVideo) IconCorner(
                    icon = Icons.Default.PlayCircle,
                    contentDescription = null,
                    bottomStartRadius = 30F,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            if (isFolder) {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isSecondaryVolume) Icon(
                            imageVector = Icons.Default.SdCard,
                            contentDescription = stringResource(R.string.pluggable_storage),
                            modifier = Modifier.height(MaterialTheme.typography.titleMedium.fontSize.value.dp),
                        )
                    }
                    Text(
                        text = folderFilesCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageItemPreview() {
    GalleryTheme {
        GridItem(
            image = "",
            name = "image.png",
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun VideoItemPreview() {
    GalleryTheme {
        GridItem(
            image = "",
            name = "video.mp4",
            isVideo = true,
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun AlbumItemPreview() {
    GalleryTheme {
        GridItem(
            image = "",
            name = "album",
            isFolder = true,
            isSelected = true,
            folderFilesCount = 2
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun ProtectedItemPreview() {
    GalleryTheme {
        GridItem(
            image = "",
            name = "system-directory",
            isFolder = true,
            isSelected = true,
            isNotWritable = true,
            folderFilesCount = 2
        )
    }
}


@Composable
private fun IconCorner(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    topStartRadius: Float = 0F,
    topEndRadius: Float = 0F,
    bottomStartRadius: Float = 0F,
    bottomEndRadius: Float = 0F
) {
    val shape = RoundedCornerShape(
        topStart = topStartRadius,
        topEnd = topEndRadius,
        bottomStart = bottomStartRadius,
        bottomEnd = bottomEndRadius
    )

    Surface(
        color = color,
        modifier = modifier.clip(shape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Preview
@Composable
private fun IconCornerPreview() {
    GalleryTheme {
        IconCorner(
            icon = Icons.Default.Videocam,
            contentDescription = "",
            bottomEndRadius = 25F
        )
    }
}
