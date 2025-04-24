package mtree.viewer

import mtree.core.ui.models.MediaItemUI

sealed interface Content {
    data object Initiating : Content
    data class Main(
        val files: List<MediaItemUI.File>,
        val initialFile: MediaItemUI.File
    ) : Content
    
    data object NoFiles : Content
}
