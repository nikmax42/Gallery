package mtree.viewer

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import mtree.core.ui.models.MediaItemUI
import mtree.core.ui.theme.GalleryTheme
import mtree.core.utils.SharingUtils
import mtree.dialogs.album_picker.AlbumPickerFullScreenDialog
import mtree.dialogs.conflict_resolver.ConflictResolverDialog
import mtree.dialogs.deletion.DeletionDialog
import mtree.dialogs.renaming.RenamerDialog
import mtree.viewer.components.contents.InitializationContent
import mtree.viewer.components.contents.MainContent

@Composable
fun ViewerScreen(
    filePath: String,
    onClose: () -> Unit,
    vm: ViewerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    
    ViewerContent(
        state = state,
        onAction = { vm.onAction(it) },
        onClose = { onClose() }
    )
    
    LaunchedEffect(filePath) {
        vm.onAction(Action.Launch(filePath))
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun ViewerContent(
    state: UiState,
    onAction: (Action) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    
    AnimatedContent(targetState = state.content) { content ->
        when (content) {
            Content.Initiating -> InitializationContent(
                onClose = { onClose() }
            )
            is Content.Main -> MainContent(
                files = content.files,
                initialFile = content.initialFile,
                showUi = state.showControls,
                onSwitchUi = { onAction(Action.SwitchControls) },
                onClose = { onClose() },
                onCopy = { file -> onAction(Action.Copy(file)) },
                onMove = { file -> onAction(Action.Move(file)) },
                onRename = { file -> onAction(Action.Rename(file)) },
                onDelete = { file -> onAction(Action.Delete(file)) },
                onShare = { file -> SharingUtils.shareSingleFile(file, context) }
            )
            Content.NoFiles -> onClose()
        }
    }
    
    // dialogs block
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


@Preview
@Composable
private fun ViewerContentPreview() {
    var files by remember {
        mutableStateOf(
            listOf(
                MediaItemUI.File.emptyFromPath(
                    path = "/storage/emulated/0/DCIM/Camera/IMG1234567890.jpg"
                ),
                MediaItemUI.File.emptyFromPath(
                    path = "/storage/emulated/0/Images/wallpaper.jpg"
                ),
                MediaItemUI.File.emptyFromPath(
                    path = "/storage/emulated/0/Movies/VID1234567890.mp4"
                )
            )
        )
    }
    val showUi = remember { true }
    var state by remember {
        mutableStateOf(
            UiState(
                content = Content.Main(
                    files = files,
                    initialFile = files.first()
                ),
                showControls = showUi
            )
        )
    }
    
    fun onAction(action: Action) {
        when (action) {
            is Action.Launch -> {}
            is Action.Copy -> {}
            is Action.Delete -> {
                files -= action.file
            }
            is Action.Move -> {}
            is Action.Rename -> {}
            Action.SwitchControls -> {
                state = state.copy(showControls = !state.showControls)
            }
        }
    }
    
    val context = LocalContext.current
    GalleryTheme {
        ViewerContent(
            state = state,
            onAction = { onAction(it) },
            onClose = { Toast.makeText(context, "onClose", Toast.LENGTH_SHORT).show() }
        )
    }
}
