package nikmax.material_tree.gallery.dialogs

import nikmax.mtree.gallery.core.data.media.ConflictResolution
import nikmax.mtree.gallery.core.ui.MediaItemUI

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
        val onConfirm: (resolution: ConflictResolution) -> Unit,
        val onDismiss: () -> Unit
    ) : Dialog
}
