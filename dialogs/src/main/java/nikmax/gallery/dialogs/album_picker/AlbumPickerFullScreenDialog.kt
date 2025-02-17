package nikmax.gallery.dialogs.album_picker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.components.ItemsGrid
import nikmax.gallery.data.preferences.GalleryPreferences
import nikmax.gallery.dialogs.R


@Composable
fun AlbumPickerFullScreenDialog(
    onConfirm: (pickedPath: String) -> Unit,
    onDismiss: () -> Unit,
    initialPath: String? = null,
    vm: AlbumPickerVm = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(null) {
        vm.onAction(AlbumPickerVm.UserAction.Launch(initialPath))
        vm.event.collectLatest { event ->
            when (event) {
                is AlbumPickerVm.Event.ConfirmDialog -> onConfirm(event.selectedPath)
                AlbumPickerVm.Event.DismissDialog -> onDismiss()
            }
        }
    }

    BackHandler { vm.onAction(AlbumPickerVm.UserAction.NavigateBack) }

    Content(
        items = state.items,
        loading = state.loading,
        onRefresh = { vm.onAction(AlbumPickerVm.UserAction.Refresh) },
        onItemClick = { if (it is MediaItemUI.Album) vm.onAction(AlbumPickerVm.UserAction.NavigateIn(it.path)) },
        onItemLongClick = { /* not in use */ },
        onConfirm = { vm.onAction(AlbumPickerVm.UserAction.Confirm) },
        onDismiss = { onDismiss() },
        preferences = state.preferences
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    items: List<MediaItemUI>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (MediaItemUI) -> Unit,
    onItemLongClick: (MediaItemUI) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    preferences: GalleryPreferences,
) {
    Scaffold(
        topBar = {
            AlbumPickerTopBar(
                onConfirmClick = { onConfirm() },
                onDismissClick = { onDismiss() }
            )
        }
    ) { paddings ->
        PullToRefreshBox(
            isRefreshing = loading,
            onRefresh = { onRefresh() },
        ) {
            ItemsGrid(
                items = items,
                selectedItems = emptyList(),
                onItemClick = { onItemClick(it) },
                onItemLongClick = { onItemLongClick(it) },
                columnsAmountPortrait = preferences.gridColumnsPortrait,
                columnsAmountLandscape = preferences.gridColumnsLandscape,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings)
            )
        }
    }
}
@Preview
@Composable
private fun Preview() {
    val scope = rememberCoroutineScope()
    val items = listOf(
        MediaItemUI.File(
            path = "",
            name = "image.png",
            volume = MediaItemUI.Volume.PRIMARY,
            dateCreated = 0,
            dateModified = 0,
            size = 0,
            mimetype = "image/png"
        )
    )
    var loading by remember { mutableStateOf(false) }
    Content(
        items = items,
        loading = loading,
        onRefresh = {
            scope.launch {
                loading = true
                delay(3000)
                loading = false
            }
        },
        onItemClick = {},
        onItemLongClick = {},
        onDismiss = {},
        onConfirm = {},
        preferences = GalleryPreferences()
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumPickerTopBar(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { onDismissClick() }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.cancel)
                )
            }
        },
        title = { Text(stringResource(R.string.pick_destination)) },
        actions = {
            TextButton(onClick = { onConfirmClick() }) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
