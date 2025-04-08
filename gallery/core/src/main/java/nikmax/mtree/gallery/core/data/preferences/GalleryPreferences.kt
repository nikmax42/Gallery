package nikmax.mtree.gallery.core.data.preferences

import kotlinx.serialization.Serializable

@Serializable
data class GalleryPreferences(
    val galleryMode: GalleryMode = GalleryMode.TREE,
    val portraitGridColumns: Int = 3,
    val landscapeGridColumns: Int = 4,
    
    val sortOrder: SortOrder = SortOrder.CREATION_DATE,
    val descendSortOrder: Boolean = false,
    val placeOnTop: PlaceOnTop = PlaceOnTop.ALBUMS_ON_TOP,
    
    val showImages: Boolean = true,
    val showVideos: Boolean = true,
    val showGifs: Boolean = true,
    val showFiles: Boolean = true,
    val showAlbums: Boolean = true,
    val showUnHidden: Boolean = true,
    val showHidden: Boolean = false
) {
    enum class GalleryMode { TREE, PLAIN }
    enum class SortOrder { CREATION_DATE, MODIFICATION_DATE, NAME, EXTENSION, SIZE, RANDOM }
    enum class PlaceOnTop { NONE, ALBUMS_ON_TOP, FILES_ON_TOP }
}
