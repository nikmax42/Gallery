package mtree.viewer

import mtree.core.ui.models.MediaItemUI

sealed interface Action {
    data class Launch(
        val initialFilePath: String,
        val searchQuery: String?
    ) : Action
    
    data object SwitchControls : Action
    data class Copy(val file: MediaItemUI.File) : Action
    data class Move(val file: MediaItemUI.File) : Action
    data class Rename(val file: MediaItemUI.File) : Action
    data class Delete(val file: MediaItemUI.File) : Action
}
