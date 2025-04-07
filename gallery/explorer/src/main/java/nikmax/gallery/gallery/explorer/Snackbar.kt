package nikmax.gallery.gallery.explorer

import nikmax.gallery.gallery.core.data.media.FileOperation
import nikmax.gallery.gallery.core.ui.MediaItemUI

internal sealed interface SnackBar {
    data class ProtectedItems(
        val protectedItems: List<MediaItemUI>,
        val onConfirm: () -> Unit
    ) : SnackBar
    
    data class OperationStarted(val operations: List<FileOperation>) : SnackBar
    data class OperationFinished(val completeItems: Int, val failedItems: Int) : SnackBar
}
