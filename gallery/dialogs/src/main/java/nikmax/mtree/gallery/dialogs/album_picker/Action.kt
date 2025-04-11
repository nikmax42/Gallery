package nikmax.mtree.gallery.dialogs.album_picker

import nikmax.mtree.gallery.core.ui.MediaItemUI

internal sealed interface Action {
    data object Launch : Action
    data class NavigateIn(val album: MediaItemUI.Album) : Action
    data object NavigateBack : Action
    data object Refresh : Action
}
