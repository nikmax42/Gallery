package mtree.explorer

import mtree.core.domain.models.FileOperation
import mtree.core.ui.models.MediaItemUI

sealed interface Snackbar {
    data class ProtectedItems(
        val protectedItems: List<MediaItemUI>,
        val onConfirm: () -> Unit
    ) : Snackbar
    
    data class OperationStarted(val operations: List<FileOperation>) : Snackbar
    data class OperationFinished(val completeItems: Int, val failedItems: Int) : Snackbar
}
