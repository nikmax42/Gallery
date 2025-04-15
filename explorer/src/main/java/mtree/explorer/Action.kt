package mtree.explorer

import mtree.core.ui.models.MediaItemUI

sealed interface Action {
    data class Launch(
        val albumPath: String?,
        val searchQuery: String?
    ) : Action
    
    data object Refresh : Action
    data class SearchQueryChange(val newQuery: String?) : Action
    data class ItemsSelectionChange(val newSelection: List<MediaItemUI>) : Action
    data class ItemsCopy(val itemsToCopy: List<MediaItemUI>) : Action
    data class ItemsMove(val itemsToMove: List<MediaItemUI>) : Action
    data class ItemsRename(val itemsToRename: List<MediaItemUI>) : Action
    data class ItemsDelete(val itemsToDelete: List<MediaItemUI>) : Action
}
