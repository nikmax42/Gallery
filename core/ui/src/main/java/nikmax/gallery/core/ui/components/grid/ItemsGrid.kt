package nikmax.gallery.core.ui.components.grid

import android.content.res.Configuration
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.theme.GalleryTheme

/**
 * [LazyVerticalGrid] of [MediaItemUI] with drag-selection gesture support
 *
 * @param items list of [MediaItemUI] to display
 * @param selectedItems list of selected [MediaItemUI]
 * @param onItemOpen callback fires when user clicks on [MediaItemUI] and no items selected
 * @param onSelectionChange callback fires when user selects/deselects [MediaItemUI] (including drag selection)
 * @param columnsAmountPortrait grid columns in portrait mode
 * @param columnsAmountLandscape grid columns in landscape mode
 * @param gridState
 */
@Composable
fun ItemsGrid(
    items: List<MediaItemUI>,
    modifier: Modifier = Modifier,
    selectedItems: List<MediaItemUI> = emptyList(),
    onItemOpen: ((MediaItemUI) -> Unit)? = null,
    onSelectionChange: ((selectedItems: List<MediaItemUI>) -> Unit)? = null,
    columnsAmountPortrait: Int = 3,
    columnsAmountLandscape: Int = 4,
    gridState: LazyGridState = rememberLazyGridState()
) {
    val orientation = LocalConfiguration.current.orientation
    val columnsAmount = when (orientation) {
        Configuration.ORIENTATION_PORTRAIT -> columnsAmountPortrait
        Configuration.ORIENTATION_LANDSCAPE -> columnsAmountLandscape
        else -> columnsAmountPortrait
    }
    // workaround to fix drag and click modifiers conflict
    // when true - grid items clickable modifier not set and unable to trigger drag cancellation
    val dragInProcess = remember { mutableStateOf(false) }
    // How fast the grid should be scrolling at any given time. The closer the
    // user moves their pointer to the bottom of the screen, the faster the scroll.
    val autoScrollSpeed = remember { mutableFloatStateOf(0f) }
    val autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() }
    // Executing the scroll
    LaunchedEffect(autoScrollSpeed.value) {
        if (autoScrollSpeed.value != 0f) {
            while (isActive) {
                // gridState.animateScrollBy(autoScrollSpeed.value)
                gridState.scrollBy(autoScrollSpeed.value)
                delay(10)
            }
        }
    }

    Surface {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsAmount),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            state = gridState,
            modifier = modifier.dragSelectable(
                lazyGridState = gridState,
                selectedIndexes = selectedItems.map { items.indexOf(it) },
                onSelectedIndexesChange = { draggedIndexes ->
                    if (onSelectionChange != null)
                        onSelectionChange(draggedIndexes.distinct().map { items[it] })
                },
                autoScrollSpeed = autoScrollSpeed,
                autoScrollThreshold = autoScrollThreshold,
                dragInProgress = dragInProcess
            )
        ) {
            items(items.size) { index ->
                val item = items[index]
                GridItem(
                    image = item.thumbnail,
                    name = item.name,
                    isVideo = item is MediaItemUI.File && item.mediaType == MediaItemUI.File.MediaType.VIDEO,
                    isFolder = item is MediaItemUI.Album,
                    folderFilesCount = if (item is MediaItemUI.Album) item.filesCount else 0,
                    isSelected = selectedItems.contains(item),
                    isSecondaryVolume = item.volume == MediaItemUI.Volume.SECONDARY,
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(),
                            fadeOutSpec = tween()
                        )
                        .then(when (dragInProcess.value) {
                            // should disable click handling when drag gesture in progress
                            // otherwise clickable modifier will be fired simultaneously with drag gesture and cancel it
                            true -> Modifier
                            false -> Modifier.clickable {
                                when (selectedItems.isEmpty()) {
                                    true -> if (onItemOpen != null) onItemOpen(item)
                                    false -> if (onSelectionChange != null)
                                        when (selectedItems.contains(item)) {
                                            true -> selectedItems.minus(item)
                                            false -> selectedItems.plus(item)
                                        }.let { onSelectionChange(it) }
                                }
                            }
                        })
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
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
            name = "album",
            volume = MediaItemUI.Volume.PRIMARY,
            dateCreated = 0,
            dateModified = 0,
            size = 0,
            filesCount = 3
        )
    }
    val items = remember { mutableStateListOf(image, video, gif, album) }
    val selectedItems1 = remember { mutableStateListOf<MediaItemUI>() }

    GalleryTheme {
        ItemsGrid(
            items = items,
            selectedItems = selectedItems1,
            onSelectionChange = {
                selectedItems1.apply { clear(); addAll(it) }
            },
        )
    }
}

private fun Modifier.dragSelectable(
    lazyGridState: LazyGridState,
    selectedIndexes: List<Int>,
    onSelectedIndexesChange: (List<Int>) -> Unit,
    autoScrollSpeed: MutableState<Float>,
    autoScrollThreshold: Float,
    dragInProgress: MutableState<Boolean>
) = pointerInput(Unit) {
    var initialIndex: Int? = null
    var lastIndex: Int? = null

    detectDragGesturesAfterLongPress(
        onDragStart = { offset ->
            // add gesture initial item to selection list
            dragInProgress.value = true
            lazyGridState.getItemIndexByOffset(offset)?.let { startItemIndex ->
                initialIndex = startItemIndex
                lastIndex = startItemIndex
                onSelectedIndexesChange(selectedIndexes.plus(startItemIndex))
            }
        },
        onDrag = { change, _ ->
            if (initialIndex != null) {
                // set autoscroll speed based on distance from grid edge
                val distFromBottom = lazyGridState.layoutInfo.viewportSize.height - change.position.y
                val distFromTop = change.position.y
                autoScrollSpeed.value = when {
                    distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                    distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                    else -> 0f
                }
                // deselect items between gesture initial item and previous selected item
                // select items between gesture initial item and item under the user's finger
                val currentIndex = lazyGridState.getItemIndexByOffset(change.position)
                currentIndex?.let {
                    if (lastIndex != currentIndex) {
                        val newSelection = selectedIndexes
                            .minus(initialIndex!!..lastIndex!!)
                            .minus(lastIndex!!..initialIndex!!)
                            .plus(initialIndex!!..currentIndex)
                            .plus(currentIndex..initialIndex!!)
                        onSelectedIndexesChange(newSelection)
                        lastIndex = currentIndex
                    }
                }
            }
        },
        // reset variables on gesture end
        onDragEnd = {
            dragInProgress.value = false
            initialIndex = null
            lastIndex = null
            autoScrollSpeed.value = 0F
        },
        onDragCancel = {
            dragInProgress.value = false
            initialIndex = null
            lastIndex = null
            autoScrollSpeed.value = 0F
        }
    )
}

/**
 * Find grid item index under the user's finger
 *
 * @param hitPoint [Offset] between grid top left corner and user's finger
 * @return [Int] index or null if not found
 */
private fun LazyGridState.getItemIndexByOffset(hitPoint: Offset): Int? {
    val foundItemInfo = layoutInfo.visibleItemsInfo
        .find { itemInfo ->
            val itemSizeRect = itemInfo.size.toIntRect()
            val hitpointOffsetMinusItemOffset = hitPoint.round() - itemInfo.offset
            val itemOffsetContainsHitpointOffset = itemSizeRect.contains(hitpointOffsetMinusItemOffset)
            itemOffsetContainsHitpointOffset
        }
    val foundItemKey = foundItemInfo?.index
    return foundItemKey
}
