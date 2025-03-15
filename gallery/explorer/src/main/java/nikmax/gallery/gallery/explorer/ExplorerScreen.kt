package nikmax.gallery.gallery.explorer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.dialogs.Dialog
import nikmax.gallery.dialogs.album_picker.AlbumPickerFullScreenDialog
import nikmax.gallery.dialogs.conflict_resolver.ConflictResolverDialog
import nikmax.gallery.dialogs.deletion.DeletionDialog
import nikmax.gallery.dialogs.renaming.RenamingDialog
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.core.SharingUtils
import nikmax.gallery.gallery.explorer.components.bottom_bars.SelectionBottomBar
import nikmax.gallery.gallery.explorer.components.main_contents.LoadingContent
import nikmax.gallery.gallery.explorer.components.main_contents.MainContent
import nikmax.gallery.gallery.explorer.components.sheets.AppearanceSheet
import nikmax.gallery.gallery.explorer.components.top_bars.SearchTopBar
import nikmax.gallery.gallery.explorer.components.top_bars.SelectionTopBar

@Composable
fun ExplorerScreen(
    albumPath: String?,
    onFileOpen: (MediaItemUI.File) -> Unit,
    vm: ExplorerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val strProtectedItemsWarning = stringResource(R.string.protected_items_warning)
    val strUnselect = stringResource(R.string.unselect)

    LaunchedEffect(Unit) {
        vm.onAction(ExplorerVm.UserAction.ScreenLaunch(albumPath))
        vm.event.collectLatest { event ->
            when (event) {
                is ExplorerVm.Event.OpenViewer -> onFileOpen(event.file)
                is ExplorerVm.Event.ShowSnackbar -> when (val snack = event.snackbar) {
                    is ExplorerVm.SnackBar.ProtectedItems -> {
                        snackbarHostState.showSnackbar(
                            message = strProtectedItemsWarning,
                            actionLabel = strUnselect,
                            duration = SnackbarDuration.Long
                        ).let {
                            when (it) {
                                SnackbarResult.ActionPerformed -> snack.onConfirm()
                                SnackbarResult.Dismissed -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    ExplorerScreenContent(
        state = state,
        onAction = { vm.onAction(it) },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplorerScreenContent(
    state: ExplorerVm.UIState,
    onAction: (ExplorerVm.UserAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    var showAppearanceSheet by remember { mutableStateOf(false) }
    val topBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val focusManager = LocalFocusManager.current

    // if it's not a gallery root - display parent album content
    BackHandler(state.albumPath != null) {
        onAction(ExplorerVm.UserAction.NavigateOutOfAlbum)
    }
    // if it's gallery root - cancel search mode
    BackHandler(state.searchQuery != null) {
        onAction(ExplorerVm.UserAction.SearchQueryChange(null))
        focusManager.clearFocus()
    }
    // if there are some selected items - clear selection
    BackHandler(state.selectedItems.isNotEmpty()) {
        onAction(ExplorerVm.UserAction.ItemsSelectionChange(emptyList()))
    }

    Scaffold(
        topBar = {
            AnimatedContent(state.selectedItems.isNotEmpty()) { hasSelectedItems ->
                when (hasSelectedItems) {
                    // when there is a selected items - show selection topbar
                    true -> SelectionTopBar(
                        items = state.items,
                        selectedItems = state.selectedItems,
                        onClearSelectionClick = { onAction(ExplorerVm.UserAction.ItemsSelectionChange(emptyList())) },
                        onSelectAllClick = { onAction(ExplorerVm.UserAction.ItemsSelectionChange(state.items)) }
                    )
                    false -> {
                        // when selected items is empty - show searchbar
                        SearchTopBar(
                            searchQuery = state.searchQuery,
                            onQueryChange = { onAction(ExplorerVm.UserAction.SearchQueryChange(it)) },
                            onSearch = { /* search performs on query change */ },
                            actions = {
                                Row {
                                    IconButton(onClick = { showAppearanceSheet = !showAppearanceSheet }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Sort,
                                            stringResource(R.string.appearance)
                                        )
                                    }
                                }
                            },
                            scrollBehavior = topBarScrollBehavior,
                            focusManager = focusManager
                        )
                    }
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
                    onCopyClick = { items -> onAction(ExplorerVm.UserAction.ItemsCopy(items)) },
                    onMoveClick = { items -> onAction(ExplorerVm.UserAction.ItemsMove(items)) },
                    onRenameClick = { items -> onAction(ExplorerVm.UserAction.ItemsRename(items)) },
                    onDeleteClick = { items -> onAction(ExplorerVm.UserAction.ItemsDelete(items)) },
                    onShare = { file -> SharingUtils.shareSingleFile(file, context) }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddings ->
        Box {
            AnimatedContent(targetState = state.isLoading && state.items.isEmpty()) { screenIsNotInitialized ->
                when (screenIsNotInitialized) {
                    true -> LoadingContent(
                        portraitColumnsAmount = state.appPreferences.gridColumnsPortrait,
                        landscapeColumnsAmount = state.appPreferences.gridColumnsLandscape,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddings.calculateTopPadding(),
                                bottom = paddings.calculateBottomPadding(),
                                start = 16.dp,
                                end = 16.dp
                            )
                    )
                    false -> MainContent(
                        state = state,
                        onAction = onAction,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddings.calculateTopPadding(),
                                bottom = paddings.calculateBottomPadding(),
                                start = 8.dp,
                                end = 8.dp
                            )
                            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                    )
                }
            }
            // model sheet with ui preferences
            if (showAppearanceSheet) AppearanceSheet(
                appPreferences = state.appPreferences,
                onPreferencesChange = { onAction(ExplorerVm.UserAction.PreferencesChange(it)) },
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
            onResolve = { resolution, applyToAll -> dialog.onConfirm(resolution) }, // todo enable "apply to all functionality"
            onDismiss = { dialog.onDismiss() }
        )
        is Dialog.DeletionConfirmation -> DeletionDialog(
            items = dialog.items,
            onConfirm = { dialog.onConfirm() },
            onDismiss = { dialog.onDismiss() }
        )
        is Dialog.Renaming -> RenamingDialog(
            mediaItem = dialog.item,
            onConfirm = { dialog.onConfirm(it) },
            onDismiss = { dialog.onDismiss() }
        )
    }
}

@Preview
@Composable
private fun ExplorerContentPreview() {
    var state by remember { mutableStateOf(ExplorerVm.UIState(isLoading = true)) }
    val snackbarHostState = remember { SnackbarHostState() }
    fun onAction(action: ExplorerVm.UserAction) {}

    LaunchedEffect(Unit) {
        delay(5000)
        state = state.copy(
            isLoading = false,
            items = listOf(
                MediaItemUI.File(
                    path = "path/to/file",
                    name = "file name",
                    size = 1024
                )
            )
        )
    }

    GalleryTheme {
        ExplorerScreenContent(
            state = state,
            onAction = { onAction(it) },
            snackbarHostState = snackbarHostState
        )
    }
}
