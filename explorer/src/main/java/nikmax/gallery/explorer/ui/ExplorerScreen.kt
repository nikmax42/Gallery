package nikmax.gallery.explorer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.explorer.ui.components.SearchTopBar
import nikmax.gallery.explorer.ui.components.SearchingContent
import nikmax.gallery.explorer.ui.components.SelectionContent
import nikmax.gallery.explorer.ui.components.SelectionTopBar
import nikmax.gallery.explorer.ui.components.ViewingContent


@Composable
fun ExplorerScreen(
    albumPath: String?,
    onFileOpen: (MediaItemUI.File) -> Unit,
    onAlbumOpen: (MediaItemUI.Album) -> Unit,
    vm: ExplorerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(albumPath) {
        vm.onAction(ExplorerVm.UserAction.Launch(albumPath))
        vm.event.collectLatest { event ->
            when (event) {
                is ExplorerVm.Event.OpenFile -> onFileOpen(event.file)
                is ExplorerVm.Event.OpenAlbum -> onAlbumOpen(event.album)
            }
        }
    }

    ExplorerContent(
        state = state,
        onAction = { vm.onAction(it) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplorerContent(
    state: ExplorerVm.UIState,
    onAction: (ExplorerVm.UserAction) -> Unit
) {
    val topbarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            when (val mode = state.mode) {
                // show searchbar in viewing and searching modes
                ExplorerVm.UIState.Mode.Viewing,
                is ExplorerVm.UIState.Mode.Searching -> {
                    SearchTopBar(
                        searchQuery = if (mode is ExplorerVm.UIState.Mode.Searching) mode.searchQuery else "",
                        onQueryChange = { onAction(ExplorerVm.UserAction.SearchQueryChange(it)) },
                        onSearch = { onAction(ExplorerVm.UserAction.Search(it)) },
                        trailingIcon = {
                            IconButton(onClick = { showSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "",
                                )
                            }
                        },
                        scrollBehavior = topbarScrollBehavior,
                    )
                }
                // show selection topbar in selection mode
                is ExplorerVm.UIState.Mode.Selection -> SelectionTopBar(
                    items = state.items.toSet(),
                    selectedItems = mode.selectedItems.toSet(),
                    onClearSelectionClick = { onAction(ExplorerVm.UserAction.ClearSelection) },
                    onSelectAllClick = { onAction(ExplorerVm.UserAction.SelectAllItems) }
                )
            }
        }
    ) { paddings ->
        when (val mode = state.mode) {
            ExplorerVm.UIState.Mode.Viewing -> ViewingContent(
                items = state.items,
                loading = state.loading,
                onRefresh = { onAction(ExplorerVm.UserAction.Refresh) },
                onItemClick = { onAction(ExplorerVm.UserAction.OpenItem(it)) },
                onItemLongClick = { onAction(ExplorerVm.UserAction.ChangeItemSelection(it)) },
                showSheet = showSheet,
                onShowSheetChange = { showSheet = it },
                preferences = state.preferences,
                onPreferencesChange = { onAction(ExplorerVm.UserAction.UpdatePreferences(it)) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddings.calculateTopPadding(),
                        bottom = paddings.calculateBottomPadding(),
                        start = 8.dp,
                        end = 8.dp
                    )
                    .nestedScroll(topbarScrollBehavior.nestedScrollConnection)
            )
            is ExplorerVm.UIState.Mode.Searching -> SearchingContent(
                items = mode.foundedItems,
                loading = state.loading,
                onRefresh = { onAction(ExplorerVm.UserAction.Refresh) },
                onItemClick = { onAction(ExplorerVm.UserAction.OpenItem(it)) },
                onItemLongClick = { onAction(ExplorerVm.UserAction.ChangeItemSelection(it)) },
                preferences = state.preferences,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddings.calculateTopPadding(),
                        bottom = paddings.calculateBottomPadding(),
                        start = 8.dp,
                        end = 8.dp
                    )
            )
            is ExplorerVm.UIState.Mode.Selection -> SelectionContent(
                items = mode.items,
                selectedItems = mode.selectedItems,
                loading = state.loading,
                onRefresh = { onAction(ExplorerVm.UserAction.Refresh) },
                onItemClick = { onAction(ExplorerVm.UserAction.ChangeItemSelection(it)) },
                onItemLongClick = { onAction(ExplorerVm.UserAction.ChangeItemSelection(it)) },
                preferences = state.preferences,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddings.calculateTopPadding(),
                        bottom = paddings.calculateBottomPadding(),
                        start = 8.dp,
                        end = 8.dp
                    )
            )
        }
    }
}
@Preview(device = "spec:parent=pixel_5,orientation=portrait")
// @Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun ExplorerContentPreview() {
    val scope = rememberCoroutineScope()
    var state by remember {
        mutableStateOf(
            ExplorerVm.UIState(items = PreviewsData.items)
        )
    }

    ExplorerContent(
        state = state,
        onAction = {}
    )
}


private object PreviewsData {
    val image = MediaItemUI.File(
        path = "album1/image.png",
        name = "image.png",
        volume = MediaItemUI.Volume.PRIMARY,
        dateCreated = 0,
        dateModified = 0,
        size = 0,
        mimetype = "image/png"
    )
    val video = MediaItemUI.File(
        path = "album1/video.mp4",
        name = "video.mp4",
        volume = MediaItemUI.Volume.PRIMARY,
        dateCreated = 0,
        dateModified = 0,
        size = 0,
        mimetype = "video/mp4"
    )
    val gif = MediaItemUI.File(
        path = "album1/gif.gif",
        name = "gif.gif",
        volume = MediaItemUI.Volume.PRIMARY,
        dateCreated = 0,
        dateModified = 0,
        size = 0,
        mimetype = "image/gif"
    )
    val album = MediaItemUI.Album(
        path = "album1/nested_album",
        name = "nested_album",
        volume = MediaItemUI.Volume.PRIMARY,
        dateCreated = 0,
        dateModified = 0,
        size = 0,
        filesCount = 3
    )

    val items = listOf(image, video, gif, album)
}
