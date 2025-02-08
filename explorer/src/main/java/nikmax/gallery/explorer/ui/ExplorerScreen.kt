package nikmax.gallery.explorer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import nikmax.gallery.core.R
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.Searchbar


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
                    TopAppBar(
                        navigationIcon = {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( // to compensate hardcoded inner paddings
                                        start = 4.dp,
                                        end = 8.dp
                                    )
                            )
                        },
                        title = { /* title has incorrect hardcoded inner paddings */ },
                        scrollBehavior = topbarScrollBehavior,
                    )
                }
                // show selection topbar in selection mode
                is ExplorerVm.UIState.Mode.Selection -> TODO()
            }
        }
    ) { paddings ->
        Box {
            PullToRefreshBox(
                isRefreshing = state.loading,
                onRefresh = { onAction(ExplorerVm.UserAction.Refresh) },
                modifier = Modifier.padding(
                    top = paddings.calculateTopPadding(),
                    bottom = paddings.calculateBottomPadding(),
                    start = 8.dp,
                    end = 8.dp
                )
            ) {
                ItemsGrid(
                    items = when (val mode = state.mode) {
                        is ExplorerVm.UIState.Mode.Searching -> mode.foundedItems
                        ExplorerVm.UIState.Mode.Viewing -> state.items
                        is ExplorerVm.UIState.Mode.Selection -> TODO()
                    },
                    selectedItems = emptyList(), // todo get from selection mode when it will be implemented
                    onItemClick = { onAction(ExplorerVm.UserAction.ItemClick(it)) },
                    onItemLongClick = { onAction(ExplorerVm.UserAction.ItemLongClick(it)) },
                    columnsAmountPortrait = state.preferences.gridColumnsPortrait,
                    columnsAmountLandscape = state.preferences.gridColumnsLandscape,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(topbarScrollBehavior.nestedScrollConnection)
                )
            }
            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    dragHandle = null
                ) {
                    GalleryAppearanceMenu(
                        selectedAlbumsMode = state.preferences.albumsMode,
                        onAlbumsModeChange = {
                            onAction(
                                ExplorerVm.UserAction.UpdatePreferences(
                                    state.preferences.copy(albumsMode = it)
                                )
                            )
                        },
                        gridPortraitColumnsAmount = state.preferences.gridColumnsPortrait,
                        onGridPortraitColumnsAmountChange = {
                            onAction(
                                ExplorerVm.UserAction.UpdatePreferences(
                                    state.preferences.copy(gridColumnsPortrait = it)
                                )
                            )
                        },
                        gridLandscapeColumnsAmount = state.preferences.gridColumnsLandscape,
                        onGridLandscapeColumnsAmountChange = {
                            onAction(
                                ExplorerVm.UserAction.UpdatePreferences(
                                    state.preferences.copy(gridColumnsLandscape = it)
                                )
                            )
                        },
                        selectedSortingType = state.preferences.sortingOrder,
                        onSortingTypeChange = {
                            onAction(
                                ExplorerVm.UserAction.UpdatePreferences(
                                    state.preferences.copy(sortingOrder = it)
                                )
                            )
                        },
                        descend = state.preferences.descendSorting,
                        onDescendChange = {
                            onAction(
                                ExplorerVm.UserAction.UpdatePreferences(
                                    state.preferences.copy(descendSorting = it)
                                )
                            )
                        },
                        selectedFilters = state.preferences.enabledFilters,
                        onFilterSelectionChange = { filter ->
                            onAction(
                                ExplorerVm.UserAction.UpdatePreferences(
                                    state.preferences.copy(
                                        enabledFilters = when (state.preferences.enabledFilters.contains(filter)) {
                                            true -> state.preferences.enabledFilters - filter
                                            false -> state.preferences.enabledFilters + filter
                                        }
                                    )
                                )
                            )
                        },
                        showHidden = state.preferences.showHidden,
                        onHiddenChange = {
                            onAction(
                                ExplorerVm.UserAction.UpdatePreferences(
                                    state.preferences.copy(showHidden = it)
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
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


@Composable
fun SearchTopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: (query: String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit) = {}
) {
    val focusManager = LocalFocusManager.current // to clear focus on search cancel

    fun clearQueryAndFocus() {
        focusManager.clearFocus()
        onQueryChange("")
    }

    // Cancel search with back press
    BackHandler(searchQuery.isNotEmpty()) { clearQueryAndFocus() }

    Searchbar(
        query = searchQuery,
        onQueryChange = { onQueryChange(it) },
        onSearch = { onSearch(it) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_placeholder)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = searchQuery.isNotEmpty(),
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "",
                    modifier = Modifier.clickable { clearQueryAndFocus() }
                )
            }
            AnimatedVisibility(
                visible = searchQuery.isEmpty(),
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                trailingIcon()
            }
        },
        placeholder = { Text(text = stringResource(R.string.search_placeholder)) },
        modifier = modifier
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
