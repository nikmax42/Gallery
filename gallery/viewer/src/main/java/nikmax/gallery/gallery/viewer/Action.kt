package nikmax.gallery.gallery.viewer

import nikmax.gallery.gallery.core.ui.MediaItemUI

internal sealed interface Action {
    data class Launch(val filePath: String) : Action
    data object SwitchControls : Action
    data class Copy(val file: MediaItemUI.File) : Action
    data class Move(val file: MediaItemUI.File) : Action
    data class Rename(val file: MediaItemUI.File) : Action
    data class Delete(val file: MediaItemUI.File) : Action
}
