package nikmax.gallery.gallery.viewer

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
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.SharingUtils
import nikmax.gallery.gallery.viewer.ViewerVm.UIState.Content
import nikmax.gallery.gallery.viewer.ViewerVm.UserAction.Copy
import nikmax.gallery.gallery.viewer.ViewerVm.UserAction.Delete
import nikmax.gallery.gallery.viewer.ViewerVm.UserAction.Launch
import nikmax.gallery.gallery.viewer.ViewerVm.UserAction.Move
import nikmax.gallery.gallery.viewer.ViewerVm.UserAction.Rename
import nikmax.gallery.gallery.viewer.ViewerVm.UserAction.SwitchControls
import nikmax.gallery.gallery.viewer.components.contents.InitializationContent
import nikmax.gallery.gallery.viewer.components.contents.MainContent
import nikmax.material_tree.gallery.dialogs.Dialog
import nikmax.material_tree.gallery.dialogs.album_picker.AlbumPickerFullScreenDialog
import nikmax.material_tree.gallery.dialogs.conflict_resolver.ConflictResolverDialog
import nikmax.material_tree.gallery.dialogs.deletion.DeletionDialog
import nikmax.material_tree.gallery.dialogs.renaming.RenamingDialog

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
        vm.onAction(Launch(filePath))
        event.collectLatest { event ->
            when (event) {
                ViewerVm.Event.CloseViewer -> onClose()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun ViewerContent(
    state: ViewerVm.UIState,
    onAction: (ViewerVm.UserAction) -> Unit,
    initialFilePath: String,
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
                initialFilePath = initialFilePath,
                showUi = state.showControls,
                onSwitchUi = { onAction(SwitchControls) },
                onClose = { onClose() },
                onCopy = { onAction(Copy(it)) },
                onMove = { onAction(Move(it)) },
                onRename = { onAction(Rename(it)) },
                onDelete = { onAction(Delete(it)) },
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
            ViewerVm.UIState(
                content = Content.Main(files),
                showControls = showUi
            )
        )
    }
    
    fun onAction(action: ViewerVm.UserAction) {
        when (action) {
            is Launch -> {}
            is Copy -> {}
            is Delete -> {
                files -= action.file
            }
            is Move -> {}
            is Rename -> {}
            SwitchControls -> {
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
