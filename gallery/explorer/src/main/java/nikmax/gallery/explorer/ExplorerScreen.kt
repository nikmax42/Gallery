package nikmax.gallery.explorer

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.dialogs.Dialog
import nikmax.gallery.dialogs.album_picker.AlbumPickerFullScreenDialog
import nikmax.gallery.dialogs.conflict_resolver.ConflictResolverDialog
import nikmax.gallery.dialogs.deletion.DeletionDialog
import nikmax.gallery.dialogs.renaming.RenamingDialog
import nikmax.gallery.explorer.components.bottom_bars.SelectionBottomBar
import nikmax.gallery.explorer.components.main_contents.ExploringContent
import nikmax.gallery.explorer.components.main_contents.SearchingContent
import nikmax.gallery.explorer.components.sheets.AppearanceSheet
import nikmax.gallery.explorer.components.top_bars.SearchTopBar
import nikmax.gallery.explorer.components.top_bars.SelectionTopBar

@Composable
fun ExplorerScreen(
    albumPath: String?,
    onFileOpen: (MediaItemUI.File) -> Unit,
    vm: ExplorerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.onAction(ExplorerVm.UserAction.ScreenLaunch(albumPath))
        vm.event.collectLatest { event ->
            when (event) {
                is ExplorerVm.Event.OpenViewer -> onFileOpen(event.file)
            }
        }
    }

    ExplorerScreenContent(
        state = state,
        onAction = { vm.onAction(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplorerScreenContent(
    state: ExplorerVm.UIState,
    onAction: (ExplorerVm.UserAction) -> Unit,
) {
    var showAppearanceSheet by remember { mutableStateOf(false) }

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
                        val searchQuery = when (val content = state.content) {
                            ExplorerVm.UIState.Content.Exploring -> null
                            is ExplorerVm.UIState.Content.Searching -> content.searchQuery
                        }
                        SearchTopBar(
                            searchQuery = searchQuery,
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
                            }
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
                    onCopyClick = { onAction(ExplorerVm.UserAction.ItemsCopy(state.selectedItems)) },
                    onMoveClick = { onAction(ExplorerVm.UserAction.ItemsMove(state.selectedItems)) },
                    onRenameClick = { onAction(ExplorerVm.UserAction.ItemsRename(state.selectedItems)) },
                    onDeleteClick = { onAction(ExplorerVm.UserAction.ItemsDelete(state.selectedItems)) }
                )
            }
        }
    ) { paddings ->
        // box to display sheet above the main content
        Box {
            when (val content = state.content) {
                is ExplorerVm.UIState.Content.Exploring -> {
                    ExploringContent(
                        items = state.items,
                        selectedItems = state.selectedItems,
                        loading = state.isLoading,
                        onRefresh = { onAction(ExplorerVm.UserAction.Refresh) },
                        onItemOpen = { onAction(ExplorerVm.UserAction.ItemOpen(it)) },
                        onSelectionChange = { onAction(ExplorerVm.UserAction.ItemsSelectionChange(it)) },
                        portraitColumnsAmount = state.appPreferences.gridColumnsPortrait,
                        landscapeColumnsAmount = state.appPreferences.gridColumnsLandscape,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddings)
                    )
                    // if it's not a gallery root - display parent album content
                    BackHandler(state.albumPath != null) {
                        onAction(ExplorerVm.UserAction.NavigateOutOfAlbum)
                    }
                }
                is ExplorerVm.UIState.Content.Searching -> {
                    SearchingContent(
                        items = content.foundItems,
                        loading = state.isLoading,
                        onRefresh = { onAction(ExplorerVm.UserAction.Refresh) },
                        onItemOpen = { onAction(ExplorerVm.UserAction.ItemOpen(it)) },
                        onSelectionChange = { onAction(ExplorerVm.UserAction.ItemsSelectionChange(it)) },
                        preferences = state.appPreferences,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddings)
                    )
                    // if it's not a gallery root - display parent album content
                    // todo cancel search if query not empty and there is a gallery root
                    BackHandler(state.albumPath != null) {
                        onAction(ExplorerVm.UserAction.NavigateOutOfAlbum)
                    }
                }
            }
            // model sheet with ui preferences
            if (showAppearanceSheet)
                AppearanceSheet(
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

    // clear selection with back press
    BackHandler(state.selectedItems.isNotEmpty()) {
        onAction(ExplorerVm.UserAction.ItemsSelectionChange(emptyList()))
    }
}
