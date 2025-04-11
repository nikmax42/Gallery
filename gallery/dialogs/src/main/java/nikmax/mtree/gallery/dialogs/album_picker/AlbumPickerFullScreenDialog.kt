package nikmax.mtree.gallery.dialogs.album_picker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import nikmax.mtree.core.ui.theme.GalleryTheme
import nikmax.mtree.gallery.core.ui.MediaItemUI
import nikmax.mtree.gallery.core.ui.components.grid.ItemsGrid
import nikmax.mtree.gallery.dialogs.R


@Composable
fun AlbumPickerFullScreenDialog(
    onConfirm: (pickedPath: String) -> Unit,
    onDismiss: () -> Unit,
    vm: AlbumPickerVm = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    
    LaunchedEffect(null) {
        vm.onAction(Action.Launch)
        vm.event.collectLatest { event ->
            when (event) {
                Event.DismissDialog -> onDismiss()
            }
        }
    }
    
    BackHandler { vm.onAction(Action.NavigateBack) }
    
    AnimatedVisibility(true) { // to show dialog with crossfade animation
        AlbumPickerContent(
            items = state.items,
            selectedAlbum = state.pickedAlbum,
            albumIsNotWritable = state.pickedAlbumIsNotWritable,
            loading = state.loading,
            onRefresh = { vm.onAction(Action.Refresh) },
            onItemClick = { if (it is MediaItemUI.Album) vm.onAction(Action.NavigateIn(it)) },
            onConfirm = { onConfirm(it) },
            onDismiss = { onDismiss() },
            gridColumnsPortrait = state.portraitGridColumns,
            gridColumnsLandscape = state.landscapeGridColumns
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumPickerContent(
    items: List<MediaItemUI>,
    selectedAlbum: MediaItemUI.Album?,
    albumIsNotWritable: Boolean,
    loading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (MediaItemUI) -> Unit,
    onConfirm: (albumPath: String) -> Unit,
    onDismiss: () -> Unit,
    gridColumnsPortrait: Int,
    gridColumnsLandscape: Int,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val albumIsNotSelectedYet = selectedAlbum == null
    val strDirectoryIsNotWritable = stringResource(R.string.directory_is_not_writable)
    val strPickDestination = stringResource(R.string.pick_destination_first)
    
    Scaffold(
        topBar = {
            AlbumPickerTopBar(
                onConfirmClick = {
                    scope.launch {
                        if (albumIsNotSelectedYet)
                            snackbarHostState.showSnackbar(strPickDestination)
                        else if (albumIsNotWritable)
                            snackbarHostState.showSnackbar(strDirectoryIsNotWritable)
                        else onConfirm(selectedAlbum.path)
                    }
                },
                onDismissClick = { onDismiss() },
                albumIsNotSelectedYet = albumIsNotSelectedYet,
                albumIsNotWritable = albumIsNotWritable
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
        MediaItemUI.File(
            path = "",
            name = "image.png",
            creationDate = 0,
            modificationDate = 0,
            size = 0,
        )
    )
    var loading by remember { mutableStateOf(false) }
    GalleryTheme {
        AlbumPickerContent(
            items = items,
            selectedAlbum = null,
            albumIsNotWritable = true,
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
    onDismissClick: () -> Unit,
    albumIsNotSelectedYet: Boolean,
    albumIsNotWritable: Boolean
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
            TextButton(
                onClick = { onConfirmClick() },
                colors = when (albumIsNotSelectedYet || albumIsNotWritable) {
                    true -> ButtonDefaults.textButtonColors().copy(
                        contentColor = ButtonDefaults.textButtonColors().disabledContentColor,
                        containerColor = ButtonDefaults.textButtonColors().disabledContainerColor,
                    )
                    false -> ButtonDefaults.textButtonColors()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
