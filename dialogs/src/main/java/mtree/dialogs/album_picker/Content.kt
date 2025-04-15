package mtree.dialogs.album_picker

import mtree.core.ui.models.MediaItemUI

sealed interface Content {
    data object Initialization : Content
    data class Main(
        val items: List<MediaItemUI>,
        val pickedAlbum: MediaItemUI.Album?,
        val pickedAlbumIsNotWritable: Boolean
    ) : Content
}
