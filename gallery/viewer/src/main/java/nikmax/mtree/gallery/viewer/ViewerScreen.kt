package nikmax.mtree.gallery.viewer

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
import kotlinx.coroutines.flow.collectLatest
import nikmax.material_tree.gallery.dialogs.Dialog
import nikmax.material_tree.gallery.dialogs.album_picker.AlbumPickerFullScreenDialog
import nikmax.material_tree.gallery.dialogs.conflict_resolver.ConflictResolverDialog
import nikmax.material_tree.gallery.dialogs.deletion.DeletionDialog
import nikmax.material_tree.gallery.dialogs.renaming.RenamingDialog
import nikmax.mtree.core.ui.theme.GalleryTheme
import nikmax.mtree.gallery.core.ui.MediaItemUI
import nikmax.mtree.gallery.core.utils.SharingUtils
import nikmax.mtree.gallery.viewer.components.contents.InitializationContent
import nikmax.mtree.gallery.viewer.components.contents.MainContent

@Composable
fun ViewerScreen(
    filePath: String,
    onClose: () -> Unit,
    vm: ViewerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val event = vm.event
    
    ViewerContent(
        state = state,
        onAction = { vm.onAction(it) },
        initialFilePath = filePath,
        onClose = { onClose() }
    )
    
    LaunchedEffect(filePath) {
        vm.onAction(Action.Launch(filePath))
        event.collectLatest { event ->
            when (event) {
                Event.CloseViewer -> onClose()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun ViewerContent(
    state: UiState,
    onAction: (Action) -> Unit,
    initialFilePath: String,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    
    AnimatedContent(targetState = state.content) { content ->
        when (content) {
            UiState.Content.Initiating -> InitializationContent(
                onClose = { onClose() }
            )
            is UiState.Content.Main -> MainContent(
                files = content.files,
                initialFilePath = initialFilePath,
                showUi = state.showControls,
                onSwitchUi = { onAction(Action.SwitchControls) },
                onClose = { onClose() },
                onCopy = { onAction(Action.Copy(it)) },
                onMove = { onAction(Action.Move(it)) },
                onRename = { onAction(Action.Rename(it)) },
                onDelete = { onAction(Action.Delete(it)) },
                onShare = { SharingUtils.shareSingleFile(it, context) }
            )
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
            onResolve = { resolution, _ -> dialog.onConfirm(resolution) },
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
private fun ViewerContentPreview() {
    var files by remember {
        mutableStateOf(
            listOf(
                MediaItemUI.File(
                    path = "/storage/emulated/0/DCIM/Camera/IMG1234567890.jpg",
                    size = 0,
                    creationDate = 0,
                    modificationDate = 0,
                ),
                MediaItemUI.File(
                    path = "/storage/emulated/0/Images/wallpaper.jpg",
                    size = 0,
                    creationDate = 0,
                    modificationDate = 0,
                ),
                MediaItemUI.File(
                    path = "/storage/emulated/0/Movies/VID1234567890.mp4",
                    size = 0,
                    creationDate = 0,
                    modificationDate = 0,
                )
            )
        )
    }
    val showUi = remember { true }
    var state by remember {
        mutableStateOf(
            UiState(
                content = UiState.Content.Main(files),
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
            initialFilePath = files[1].path,
            onClose = { Toast.makeText(context, "onClose", Toast.LENGTH_SHORT).show() }
        )
    }
}
