package nikmax.gallery.gallery.explorer

import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.material_tree.gallery.dialogs.Dialog

internal data class UiState(
    val albumPath: String? = null,
    val items: List<MediaItemUI> = emptyList(),
    val selectedItems: List<MediaItemUI> = emptyList(),
    val searchQuery: String? = null,
    val isLoading: Boolean = true,
    val portraitGridColumns: Int = 3,
    val landscapeGridColumns: Int = 4,
    val content: Content = Content.Initialization,
    val dialog: Dialog = Dialog.None,
) {
    sealed interface Content {
        data object Initialization : Content
        data object Normal : Content
        sealed interface Error : Content {
            data class PermissionNotGranted(val onGrantClick: () -> Unit) : Error
            data object NothingFound : Error
        }
    }
}
