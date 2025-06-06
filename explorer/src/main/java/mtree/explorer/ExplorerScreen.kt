package mtree.explorer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import mtree.core.ui.models.MediaItemUI
import mtree.core.utils.SharingUtils
import mtree.dialogs.album_picker.AlbumPickerFullScreenDialog
import mtree.dialogs.conflict_resolver.ConflictResolverDialog
import mtree.dialogs.deletion.DeletionDialog
import mtree.dialogs.renaming.RenamerDialog
import mtree.explorer.components.bottom_bars.SelectionBottomBar
import mtree.explorer.components.main_contents.InitializationContent
import mtree.explorer.components.main_contents.MainContent
import mtree.explorer.components.main_contents.NothingToShowContent
import mtree.explorer.components.top_bars.SearchTopBar
import mtree.explorer.components.top_bars.SelectionTopBar
import mtree.preferences_sheet.GalleryPreferencesSheet

@Composable
fun ExplorerScreen(
    albumPath: String?,
    searchQuery: String?,
    onAlbumOpen: (albumPath: String, searchQuery: String?) -> Unit,
    onFileOpen: (filePath: String, searchQuery: String?) -> Unit,
    vm: ExplorerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val strCopying = stringResource(R.string.copying)
    val strCopied = stringResource(R.string.copied)
    val strMoving = stringResource(R.string.moving)
    val strMoved = stringResource(R.string.moved)
    val strRenaming = stringResource(R.string.renaming)
    val strRenamed = stringResource(R.string.renamed)
    val strDeleting = stringResource(R.string.deleting)
    val strDeleted = stringResource(R.string.deleted)
    
    LaunchedEffect(albumPath) {
        vm.onAction(
            Action.Launch(
                albumPath = albumPath,
                searchQuery = searchQuery
            )
        )
        if (albumPath == null && state.items.isEmpty()) {
            vm.onAction(Action.Refresh)
        }
        
        vm.snackbar.collectLatest { snackbar ->
            when (snackbar) {
                is Snackbar.RenamingStarted -> snackbarHostState.showSnackbar(strRenaming)
                Snackbar.RenamingFinished -> snackbarHostState.showSnackbar(strRenamed)
                is Snackbar.CopyingStarted -> snackbarHostState.showSnackbar(strCopying)
                Snackbar.CopyingFinished -> snackbarHostState.showSnackbar(strCopied)
                is Snackbar.MovingStarted -> snackbarHostState.showSnackbar(strMoving)
                Snackbar.MovingFinished -> snackbarHostState.showSnackbar(strMoved)
                is Snackbar.DeletionStarted -> snackbarHostState.showSnackbar(strDeleting)
                Snackbar.DeletionFinished -> snackbarHostState.showSnackbar(strDeleted)
            }
        }
    }
    
    ExplorerScreenContent(
        state = state,
        onAction = { vm.onAction(it) },
        onFileOpen = { file -> onFileOpen(file.path, state.searchQuery) },
        onAlbumOpen = { album -> onAlbumOpen(album.path, state.searchQuery) },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplorerScreenContent(
    state: UiState,
    onAction: (Action) -> Unit,
    onFileOpen: (MediaItemUI.File) -> Unit,
    onAlbumOpen: (MediaItemUI.Album) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    var showAppearanceSheet by remember { mutableStateOf(false) }
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val focusManager = LocalFocusManager.current
    
    // if it's gallery root - cancel search mode
    BackHandler(state.searchQuery != null && state.albumPath == null) {
        onAction(Action.SearchQueryChange(null))
        focusManager.clearFocus()
    }
    // if there are some selected items - clear selection
    BackHandler(state.selectedItems.isNotEmpty()) {
        onAction(Action.ItemsSelectionChange(emptyList()))
    }
    
    Scaffold(
        topBar = {
            AnimatedContent(state.selectedItems.isNotEmpty()) { hasSelectedItems ->
                when (hasSelectedItems) {
                    // when there is a selected items - show selection topbar
                    true -> SelectionTopBar(
                        items = state.items,
                        selectedItems = state.selectedItems,
                        onClearSelectionClick = {
                            onAction(
                                Action.ItemsSelectionChange(emptyList())
                            )
                        },
                        onSelectAllClick = {
                            onAction(
                                Action.ItemsSelectionChange(state.items)
                            )
                        }
                    )
                    // when selected items is empty - show searchbar
                    false -> SearchTopBar(
                        searchQuery = state.searchQuery,
                        onQueryChange = { onAction(Action.SearchQueryChange(it)) },
                        onFilterButtonClick = { showAppearanceSheet = !showAppearanceSheet },
                        albumName = state.albumPath?.substringAfterLast('/'),
                        scrollBehavior = topBarScrollBehavior,
                        focusManager = focusManager
                    )
                }
            }
        },
        bottomBar = {
            // when selected items is not empty - show selection bar
            AnimatedVisibility(
                visible = state.selectedItems.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                SelectionBottomBar(
                    selectedItems = state.selectedItems,
                    onCopy = { items -> onAction(Action.ItemsCopy(items)) },
                    onMove = { items -> onAction(Action.ItemsMove(items)) },
                    onRename = { items -> onAction(Action.ItemsRename(items)) },
                    onDelete = { items -> onAction(Action.ItemsDelete(items)) },
                    onShare = { file -> SharingUtils.shareSingleFile(file, context) },
                    onUnavailableItemsUnselection = { newSelection -> onAction(Action.ItemsSelectionChange(newSelection)) },
                    snackbarHostState = snackbarHostState
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddings ->
        Box {
            AnimatedContent(
                targetState = state.content,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { content ->
                when (content) {
                    Content.Shimmer -> InitializationContent(
                        portraitGridColumns = state.portraitGridColumns,
                        landscapeGridColumns = state.landscapeGridColumns,
                        modifier = Modifier.padding(
                            top = paddings.calculateTopPadding(),
                            bottom = paddings.calculateBottomPadding(),
                            start = 16.dp,
                            end = 16.dp
                        )
                    )
                    Content.Main -> MainContent(
                        items = state.items,
                        selectedItems = state.selectedItems,
                        isLoading = state.isLoading,
                        onFileOpen = { file -> onFileOpen(file) },
                        onAlbumOpen = { album -> onAlbumOpen(album) },
                        onSelectionChange = { newSelection -> onAction(Action.ItemsSelectionChange(newSelection)) },
                        portraitGridColumns = state.portraitGridColumns,
                        landscapeGridColumns = state.landscapeGridColumns,
                        onRefresh = { onAction(Action.Refresh) },
                        modifier = Modifier
                            .padding(
                                top = paddings.calculateTopPadding(),
                                bottom = paddings.calculateBottomPadding(),
                                start = 8.dp,
                                end = 8.dp
                            )
                            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                    )
                    Content.NothingToDisplay -> NothingToShowContent(
                        onRescan = { onAction(Action.Refresh) },
                        onReset = { onAction(Action.ResetFiltersAndSearch) },
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(
                            top = paddings.calculateTopPadding(),
                            bottom = paddings.calculateBottomPadding(),
                            start = 16.dp,
                            end = 16.dp
                        )
                    )
                }
            }
            // model sheet with ui preferences
            if (showAppearanceSheet) GalleryPreferencesSheet(
                onShowSheetChange = { showAppearanceSheet = it }
            )
        }
    }
    
    // dialogs section
    when (val dialog = state.dialog) {
        Dialog.None -> {}
        is Dialog.AlbumPicker -> AlbumPickerFullScreenDialog(
            onConfirm = { dialog.onConfirm(it) },
            onDismiss = { dialog.onDismiss() }
        )
        is Dialog.ConflictResolver -> ConflictResolverDialog(
            conflictItem = dialog.conflictItem,
            onConfirm = { resolution -> dialog.onConfirm(resolution) },
            onDismiss = { dialog.onDismiss() }
        )
        is Dialog.DeletionConfirmation -> DeletionDialog(
            items = dialog.items,
            onConfirm = { dialog.onConfirm() },
            onDismiss = { dialog.onDismiss() }
        )
        is Dialog.Renaming -> RenamerDialog(
            mediaItem = dialog.item,
            onConfirm = { dialog.onConfirm(it) },
            onDismiss = { dialog.onDismiss() }
        )
    }
}
