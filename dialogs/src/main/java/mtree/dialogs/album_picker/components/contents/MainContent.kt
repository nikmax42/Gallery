package mtree.dialogs.album_picker.components.contents

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mtree.core.ui.components.grid.ItemsGrid
import mtree.core.ui.models.MediaItemUI
import mtree.dialogs.album_picker.components.topbar.PickerTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainContent(
    items: List<MediaItemUI>,
    currentAlbum: MediaItemUI.Album?,
    loading: Boolean,
    onRefresh: () -> Unit,
    onAlbumClick: (MediaItemUI.Album) -> Unit,
    onConfirm: (MediaItemUI.Album) -> Unit,
    onDismiss: () -> Unit,
    gridColumnsPortrait: Int,
    gridColumnsLandscape: Int,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        topBar = {
            PickerTopBar(
                currentAlbum = currentAlbum,
                onConfirm = { onConfirm(it) },
                onDismiss = { onDismiss() },
                snackbarHostState = snackbarHostState
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddings ->
        PullToRefreshBox(
            isRefreshing = loading,
            onRefresh = { onRefresh() },
        ) {
            ItemsGrid(
                items = items,
                selectedItems = emptyList(),
                onItemOpen = { if (it is MediaItemUI.Album) onAlbumClick(it) },
                columnsAmountPortrait = gridColumnsPortrait,
                columnsAmountLandscape = gridColumnsLandscape,
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(paddings)
            )
        }
    }
}


@Preview
@Composable
private fun MainContentPreview() {
    val scope = rememberCoroutineScope()
    val items = remember {
        listOf(
            MediaItemUI.File.emptyFromPath(
                path = "preview/image.png"
            )
        )
    }
    var loading by remember { mutableStateOf(false) }
    
    MainContent(
        items = items,
        currentAlbum = null,
        loading = loading,
        onRefresh = {
            scope.launch {
                loading = true
                delay(3000)
                loading = false
            }
        },
        onAlbumClick = {},
        onDismiss = {},
        onConfirm = {},
        gridColumnsPortrait = 3,
        gridColumnsLandscape = 4,
    )
}
