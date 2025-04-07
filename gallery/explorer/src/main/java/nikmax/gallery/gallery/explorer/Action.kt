package nikmax.gallery.gallery.explorer

import nikmax.gallery.gallery.core.ui.MediaItemUI

internal sealed interface Action {
    data object Launch : Action
    data object Refresh : Action
    data class ItemOpen(val item: MediaItemUI) : Action
    data object NavigateOutOfAlbum : Action
    data class SearchQueryChange(val newQuery: String?) : Action
    data class ItemsSelectionChange(val newSelection: List<MediaItemUI>) : Action
    data class ItemsCopy(val itemsToCopy: List<MediaItemUI>) : Action
    data class ItemsMove(val itemsToMove: List<MediaItemUI>) : Action
    data class ItemsRename(val itemsToRename: List<MediaItemUI>) : Action
    data class ItemsDelete(val itemsToDelete: List<MediaItemUI>) : Action
}
