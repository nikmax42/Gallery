package mtree.dialogs.album_picker

import mtree.core.ui.models.MediaItemUI


sealed interface Action {
    data object Launch : Action
    data class NavigateInsideAlbum(val album: MediaItemUI.Album) : Action
    data object NavigateBack : Action
    data object Refresh : Action
}
