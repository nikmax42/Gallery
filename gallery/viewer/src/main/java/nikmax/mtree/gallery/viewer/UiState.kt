package nikmax.mtree.gallery.viewer

import nikmax.material_tree.gallery.dialogs.Dialog
import nikmax.mtree.gallery.core.ui.MediaItemUI

internal data class UiState(
    val showControls: Boolean = true,
    val refreshing: Boolean = false,
    val content: Content = Content.Initiating,
    val dialog: Dialog = Dialog.None
) {
    sealed interface Content {
        data object Initiating : Content
        data class Main(val files: List<MediaItemUI.File>) : Content
    }
}
