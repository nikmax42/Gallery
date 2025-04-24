package mtree.viewer

import mtree.core.ui.models.ConflictResolutionUi
import mtree.core.ui.models.MediaItemUI

sealed interface Dialog {
    data object None : Dialog
    
    data class DeletionConfirmation(
        val items: List<MediaItemUI>,
        val onConfirm: () -> Unit,
        val onDismiss: () -> Unit
    ) : Dialog
    
    data class Renaming(
        val item: MediaItemUI,
        val onConfirm: (newPath: String) -> Unit,
        val onDismiss: () -> Unit
    ) : Dialog
    
    data class AlbumPicker(
        val onConfirm: (selectedPath: String) -> Unit,
        val onDismiss: () -> Unit
    ) : Dialog
    
    data class ConflictResolver(
        val conflictItem: MediaItemUI,
        val onConfirm: (resolution: ConflictResolutionUi) -> Unit,
        val onDismiss: () -> Unit
    ) : Dialog
}
