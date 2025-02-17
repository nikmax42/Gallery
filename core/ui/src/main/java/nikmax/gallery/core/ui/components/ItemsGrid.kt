package nikmax.gallery.core.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.R

@Composable
fun ItemsGrid(
    items: List<MediaItemUI>,
    selectedItems: List<MediaItemUI>,
    onItemClick: (MediaItemUI) -> Unit,
    onItemLongClick: (MediaItemUI) -> Unit,
    modifier: Modifier = Modifier,
    columnsAmountPortrait: Int = 3,
    columnsAmountLandscape: Int = 4,
    gridState: LazyGridState = rememberLazyGridState()
) {
    val orientation = LocalConfiguration.current.orientation
    val columnsAmount = when (orientation) {
        android.content.res.Configuration.ORIENTATION_PORTRAIT -> columnsAmountPortrait
        android.content.res.Configuration.ORIENTATION_LANDSCAPE -> columnsAmountLandscape
        else -> columnsAmountPortrait
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnsAmount),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        state = gridState,
        modifier = modifier,
    ) {
        items.forEach {
            item {
                GridItem(
                    image = it.thumbnail,
                    name = it.name,
                    isVideo = it is MediaItemUI.File && it.mediaType == MediaItemUI.File.MediaType.VIDEO,
                    isFolder = it is MediaItemUI.Album,
                    folderFilesCount = if (it is MediaItemUI.Album) it.filesCount else 0,
                    isSelected = selectedItems.contains(it),
                    isSecondaryVolume = it.volume == MediaItemUI.Volume.SECONDARY,
                    onClick = { onItemClick(it) },
                    onLongClick = { onItemLongClick(it) }
                )
            }
        }
    }
}
@Preview
@Composable
private fun ItemsGridPreview() {
    val image = remember {
        MediaItemUI.File(
            path = "",
            name = "image.png",
            volume = MediaItemUI.Volume.PRIMARY,
            dateCreated = 0,
            dateModified = 0,
            size = 0,
            mimetype = "image/png"
        )
    }
    val video = remember {
        MediaItemUI.File(
            path = "",
            name = "video.mp4",
            volume = MediaItemUI.Volume.PRIMARY,
            dateCreated = 0,
            dateModified = 0,
            size = 0,
            mimetype = "video/mp4"
        )
    }
    val gif = remember {
        MediaItemUI.File(
            path = "",
            name = "gif.gif",
            volume = MediaItemUI.Volume.PRIMARY,
            dateCreated = 0,
            dateModified = 0,
            size = 0,
            mimetype = "image/gif"
        )
    }
    val album = remember {
        MediaItemUI.Album(
            path = "",
            name = "gif.gif",
            volume = MediaItemUI.Volume.PRIMARY,
            dateCreated = 0,
            dateModified = 0,
            size = 0,
            filesCount = 3
        )
    }
    val items = remember { listOf(image, video, gif, album) }
    val selectedItems = remember { listOf(album) }

    ItemsGrid(
        items = items,
        selectedItems = selectedItems,
        onItemClick = {},
        onItemLongClick = {}
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridItem(
    image: String?,
    name: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean = false,
    isVideo: Boolean = false,
    isFolder: Boolean = false,
    isSecondaryVolume: Boolean = false,
    folderFilesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.combinedClickable(
            onClick = { onClick() },
            onLongClick = { onLongClick() }
        )
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
                if (isSelected) IconCorner(
                    icon = Icons.Default.CheckCircle,
                    contentDescription = null, // todo add content description
                    bottomEndRadius = 30F,
                    modifier = Modifier.align(Alignment.TopStart)
                )
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
@Preview
@Composable
private fun MediaItemPreview() {
    GridItem(
        image = "",
        name = "name",
        isFolder = true,
        folderFilesCount = 2,
        onClick = {},
        onLongClick = {},
    )
}


@Composable
private fun IconCorner(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
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
        modifier = modifier.clip(shape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(2.dp)
        )
    }
}
@Preview
@Composable
private fun IconCornerPreview() {
    IconCorner(
        icon = Icons.Default.Videocam,
        contentDescription = "",
        bottomEndRadius = 25F
    )
}
