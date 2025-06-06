package mtree.core.ui.components.grid

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
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
import mtree.core.R
import mtree.core.ui.theme.GalleryTheme
import mtree.core.utils.MeasurementUnitsUtils.millisToDurationString
import mtree.core.utils.MeasurementUnitsUtils.sizeToString

@Composable
internal fun GridItem(
    image: String?,
    name: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isVideo: Boolean = false,
    videoDuration: Long = 0,
    isAlbum: Boolean = false,
    isVolume: Boolean = false,
    albumSize: Long = 0,
    isPlacedOnPluggableVolume: Boolean = false,
    albumFilesCount: Int = 0,
    albumNestedAlbumsCount: Int = 0,
) {
    val borderWidth by animateDpAsState(
        if (isSelected) 2.dp
        else CardDefaults.outlinedCardBorder().width
    )
    val selectionColor by animateColorAsState(
        if (isSelected) when (isVolume) {
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
                if (isVolume) IconCorner(
                    icon = Icons.Default.Storage,
                    color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    contentDescription = stringResource(R.string.its_system_directory),
                    bottomEndRadius = 30F,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                if (isSelected) {
                    when (isVolume) {
                        true -> IconCorner(
                            icon = Icons.Default.Lock,
                            color = MaterialTheme.colorScheme.error,
                            contentDescription = stringResource(R.string.item_selected),
                            bottomStartRadius = 30F,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                        false -> IconCorner(
                            icon = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.item_selected),
                            bottomStartRadius = 30F,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }
                if (isVideo) IconCorner(
                    icon = Icons.Default.PlayCircle,
                    label = videoDuration.millisToDurationString(),
                    contentDescription = videoDuration.millisToDurationString(),
                    topEndRadius = 30F,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
            if (isAlbum || isVolume) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (albumFilesCount > 0)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = stringResource(R.string.files_count),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = albumFilesCount.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                        if (albumNestedAlbumsCount > 0)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PermMedia,
                                    contentDescription = stringResource(R.string.directories_count),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = albumNestedAlbumsCount.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // switch between phone and sdcard icon depends on album volume
                        val icon = if (isPlacedOnPluggableVolume) Icons.Default.SdStorage else Icons.Default.Smartphone
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(R.string.directory_size),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = albumSize.sizeToString(),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
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
            videoDuration = 20_000_442
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedSdcardVideoItemPreview() {
    GalleryTheme {
        GridItem(
            image = "",
            name = "video.mp4",
            isVideo = true,
            isSelected = true,
            isPlacedOnPluggableVolume = true,
            videoDuration = 20_000_442
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
            isAlbum = true,
            albumSize = 35326346,
            isSelected = false,
            albumFilesCount = 2
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedAlbumItemPreview() {
    GalleryTheme {
        GridItem(
            image = "",
            name = "album",
            isAlbum = true,
            albumSize = 35326346,
            isSelected = true,
            albumFilesCount = 2
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VolumePreview() {
    GalleryTheme {
        GridItem(
            image = "",
            name = "system-directory",
            isAlbum = true,
            isVolume = true,
            albumSize = 35326346,
            isSelected = false,
            albumFilesCount = 2
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedVolumePreview() {
    GalleryTheme {
        GridItem(
            image = "",
            name = "system-directory",
            isAlbum = true,
            isVolume = true,
            isSelected = true,
            albumFilesCount = 2
        )
    }
}


@Composable
private fun IconCorner(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    label: String? = null,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                
                )
            if (label != null) Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
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
