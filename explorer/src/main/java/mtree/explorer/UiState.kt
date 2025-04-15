package mtree.explorer

import mtree.core.ui.models.MediaItemUI

data class UiState(
    val albumPath: String?,
    val items: List<MediaItemUI>,
    val selectedItems: List<MediaItemUI>,
    val searchQuery: String?,
    val isLoading: Boolean,
    val portraitGridColumns: Int,
    val landscapeGridColumns: Int,
    val content: Content,
    val dialog: Dialog,
)
