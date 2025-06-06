package mtree.dialogs.album_picker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import mtree.core.ui.models.MediaItemUI
import mtree.dialogs.album_picker.components.contents.InitializationShimmer
import mtree.dialogs.album_picker.components.contents.MainContent


@Composable
fun AlbumPickerFullScreenDialog(
    onConfirm: (pickedPath: String) -> Unit,
    onDismiss: () -> Unit,
    vm: AlbumPickerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    
    LaunchedEffect(null) {
        vm.onAction(Action.Launch)
    }
    
    BackHandler(state.currentAlbum != null) {
        vm.onAction(Action.NavigateBack)
    }
    
    BackHandler(state.currentAlbum == null) {
        onDismiss()
    }
    
    PickerContent(
        state = state,
        onAction = { vm.onAction(it) },
        onConfirm = { pickedAlbum -> onConfirm(pickedAlbum.path) },
        onDismiss = { onDismiss() }
    )
}


@Composable
private fun PickerContent(
    state: UiState,
    onAction: (Action) -> Unit,
    onConfirm: (MediaItemUI.Album) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state.content,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class.simpleName }
        ) { content ->
            when (content) {
                is Content.Main -> MainContent(
                    items = content.items,
                    currentAlbum = content.pickedAlbum,
                    loading = state.isLoading,
                    onRefresh = { onAction(Action.Refresh) },
                    onAlbumClick = { album -> onAction(Action.NavigateInsideAlbum(album)) },
                    onConfirm = { onConfirm(it) },
                    onDismiss = { onDismiss() },
                    gridColumnsPortrait = state.portraitGridColumns,
                    gridColumnsLandscape = state.landscapeGridColumns
                )
                Content.Initialization -> InitializationShimmer(
                    portraitGridColumns = state.portraitGridColumns,
                    landscapeGridColumns = state.landscapeGridColumns,
                    onDismiss = { onDismiss() }
                )
            }
        }
    }
}
