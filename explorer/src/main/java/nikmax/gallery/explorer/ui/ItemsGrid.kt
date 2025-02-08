package nikmax.gallery.explorer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.ui.MediaItemUI

@Composable
fun ItemsGrid(
    items: List<MediaItemUI>,
    onItemClick: (MediaItemUI) -> Unit,
    onItemLongClick: (MediaItemUI) -> Unit,
    modifier: Modifier = Modifier,
    columnsAmountPortrait: Int = 3,
    columnsAmountLandscape: Int = 4,
    selectedItems: List<MediaItemUI> = emptyList(),
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
                MediaItem(
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
