package nikmax.material_tree.gallery.dialogs.album_picker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.gallery.core.ui.components.grid.ItemsGrid
import nikmax.material_tree.gallery.dialogs.R


@Composable
fun AlbumPickerFullScreenDialog(
    onConfirm: (pickedPath: String) -> Unit,
    onDismiss: () -> Unit,
    initialPath: String? = null,
    vm: AlbumPickerVm = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val preferences by GalleryPreferencesUtils
        .getPreferencesFlow(LocalContext.current)
        .collectAsState(GalleryPreferences())

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

    AnimatedVisibility(true) { // to show dialog with crossfade animation
        AlbumPickerContent(
            items = state.items,
            loading = state.loading,
            onRefresh = { vm.onAction(AlbumPickerVm.UserAction.Refresh) },
            onItemClick = {
                if (it is nikmax.gallery.gallery.core.ui.MediaItemUI.Album) vm.onAction(
                    AlbumPickerVm.UserAction.NavigateIn(
                        it.path
                    )
                )
            },
            onConfirm = { vm.onAction(AlbumPickerVm.UserAction.Confirm) },
            onDismiss = { onDismiss() },
            gridColumnsPortrait = preferences.appearance.grid.portraitColumns,
            gridColumnsLandscape = preferences.appearance.grid.landscapeColumns
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumPickerContent(
    items: List<nikmax.gallery.gallery.core.ui.MediaItemUI>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (nikmax.gallery.gallery.core.ui.MediaItemUI) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    gridColumnsPortrait: Int,
    gridColumnsLandscape: Int
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
                onItemOpen = { onItemClick(it) },
                columnsAmountPortrait = gridColumnsPortrait,
                columnsAmountLandscape = gridColumnsLandscape,
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
        nikmax.gallery.gallery.core.ui.MediaItemUI.File(
            path = "",
            name = "image.png",
            belongsToVolume = nikmax.gallery.gallery.core.ui.MediaItemUI.Volume.DEVICE,
            creationDate = 0,
            modificationDate = 0,
            size = 0,
            mimetype = "image/png"
        )
    )
    var loading by remember { mutableStateOf(false) }
    GalleryTheme {
        AlbumPickerContent(
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
            onDismiss = {},
            onConfirm = {},
            gridColumnsPortrait = 3,
            gridColumnsLandscape = 4,
        )
    }
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
