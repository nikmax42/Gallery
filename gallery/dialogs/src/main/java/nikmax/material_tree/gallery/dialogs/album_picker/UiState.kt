package nikmax.material_tree.gallery.dialogs.album_picker

import nikmax.gallery.gallery.core.ui.MediaItemUI

internal data class UiState(
    val items: List<MediaItemUI> = listOf(),
    val loading: Boolean = false,
    val pickedAlbum: MediaItemUI.Album? = null,
    val pickedAlbumIsNotWritable: Boolean = true,
    val portraitGridColumns: Int = 3,
    val landscapeGridColumns: Int = 4
)
