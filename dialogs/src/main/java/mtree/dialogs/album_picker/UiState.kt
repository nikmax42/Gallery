package mtree.dialogs.album_picker

import mtree.core.ui.models.MediaItemUI

data class UiState(
    val currentAlbum: MediaItemUI.Album?,
    val isLoading: Boolean,
    val portraitGridColumns: Int,
    val landscapeGridColumns: Int,
    val content: Content
)
