package nikmax.gallery.gallery.viewer

import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.material_tree.gallery.dialogs.Dialog

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
