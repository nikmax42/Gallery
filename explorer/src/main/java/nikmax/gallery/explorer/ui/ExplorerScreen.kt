package nikmax.gallery.explorer.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import nikmax.gallery.core.PermissionsUtils
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.dialogs.album_picker.AlbumPickerFullScreenDialog
import nikmax.gallery.dialogs.conflict_resolver.ConflictResolverDialog
import nikmax.gallery.dialogs.deletion.DeletionDialog
import nikmax.gallery.dialogs.renaming.RenamingDialog
import nikmax.gallery.explorer.ui.components.PermissionNotGrantedContent
import nikmax.gallery.explorer.ui.components.SearchTopBar
import nikmax.gallery.explorer.ui.components.SearchingContent
import nikmax.gallery.explorer.ui.components.SelectionBottomBar
import nikmax.gallery.explorer.ui.components.SelectionContent
import nikmax.gallery.explorer.ui.components.SelectionTopBar
import nikmax.gallery.explorer.ui.components.ViewingContent
import kotlin.concurrent.timer


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
    val context = LocalContext.current
    var storagePermissionStatus by remember(state) {
        mutableStateOf(
            PermissionsUtils.checkPermission(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                context
            )
        )
    }
    val topbarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    val focusManager = LocalFocusManager.current
    var showSheet by remember { mutableStateOf(false) }

    BackHandler(state.mode is ExplorerVm.UIState.Mode.Searching) {
        onAction(ExplorerVm.UserAction.SearchQueryChange(""))
        focusManager.clearFocus()
    }

    BackHandler(state.mode is ExplorerVm.UIState.Mode.Selection) {
        onAction(ExplorerVm.UserAction.ClearSelection)
    }

    Scaffold(
        // todo add animated visibility to bars
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
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "",
                                )
                            }
                        },
                        scrollBehavior = topbarScrollBehavior,
                        focusManager = focusManager
                    )
                }
                // show selection topbar in selection mode
                is ExplorerVm.UIState.Mode.Selection -> SelectionTopBar(
                    items = state.screenItems.toSet(),
                    selectedItems = mode.selectedItems.toSet(),
                    onClearSelectionClick = { onAction(ExplorerVm.UserAction.ClearSelection) },
                    onSelectAllClick = { onAction(ExplorerVm.UserAction.SelectAllItems) }
                )
            }
        },
        bottomBar = {
            val mode = state.mode
            if (mode is ExplorerVm.UIState.Mode.Selection) {
                SelectionBottomBar(
                    onCopyClick = { onAction(ExplorerVm.UserAction.Copy(mode.selectedItems)) },
                    onMoveClick = { onAction(ExplorerVm.UserAction.Move(mode.selectedItems)) },
                    onRenameClick = { onAction(ExplorerVm.UserAction.Rename(mode.selectedItems)) },
                    onDeleteClick = { onAction(ExplorerVm.UserAction.Delete(mode.selectedItems)) }
                )
            }
        }
    ) { paddings ->
        if (storagePermissionStatus == PermissionsUtils.PermissionStatus.DENIED) {
            timer(period = 1000) {
                storagePermissionStatus = PermissionsUtils.checkPermission(
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    context
                )
                if (storagePermissionStatus == PermissionsUtils.PermissionStatus.GRANTED) {
                    onAction(ExplorerVm.UserAction.Refresh)
                    cancel()
                }
            }
            PermissionNotGrantedContent(
                onGrantClick = {
                    PermissionsUtils.requestPermission(
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        context
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        } else when (val mode = state.mode) {
            ExplorerVm.UIState.Mode.Viewing -> ViewingContent(
                items = state.screenItems,
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
                items = state.screenItems,
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

    when (val dialog = state.dialog) {
        ExplorerVm.UIState.Dialog.None -> {}
        is ExplorerVm.UIState.Dialog.AlbumPicker -> AlbumPickerFullScreenDialog(
            onConfirm = { dialog.onConfirm(it) },
            onDismiss = { dialog.onDismiss() }
        )
        is ExplorerVm.UIState.Dialog.ConflictResolver -> ConflictResolverDialog(
            conflictItem = dialog.conflictItem,
            onResolve = { resolution, applyToAll -> dialog.onConfirm(resolution) },
            onDismiss = { dialog.onDismiss }
        )
        is ExplorerVm.UIState.Dialog.DeletionConfirmation -> DeletionDialog(
            items = dialog.items,
            onConfirm = { dialog.onConfirm() },
            onDismiss = { dialog.onDismiss() }
        )
        is ExplorerVm.UIState.Dialog.Renaming -> RenamingDialog(
            mediaItem = dialog.item,
            onConfirm = { dialog.onConfirm(it) },
            onDismiss = { dialog.onDismiss() }
        )
    }
}
@Preview
@Composable
private fun ExplorerContentPreview() {
    val state by remember {
        mutableStateOf(
            ExplorerVm.UIState(screenItems = PreviewsData.items)
        )
    }
    GalleryTheme {
        ExplorerContent(
            state = state,
            onAction = {}
        )
    }
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
