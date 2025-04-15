package mtree.core.preferences

import kotlinx.serialization.Serializable

@Serializable
data class MtreePreferences(
    val theme: Theme,
    val dynamicColors: DynamicColors,
    
    val galleryMode: GalleryMode,
    val portraitGridColumns: Int,
    val landscapeGridColumns: Int,
    
    val sortOrder: SortOrder,
    val descendSortOrder: Boolean,
    val placeOnTop: PlaceOnTop,
    
    val showImages: Boolean,
    val showVideos: Boolean,
    val showGifs: Boolean,
    val showFiles: Boolean,
    val showAlbums: Boolean,
    val showUnHidden: Boolean,
    val showHidden: Boolean
) {
    enum class Theme { SYSTEM, LIGHT, DARK }
    enum class DynamicColors { SYSTEM, DISABLED }
    
    enum class GalleryMode { TREE, PLAIN }
    
    enum class SortOrder { CREATION_DATE, MODIFICATION_DATE, NAME, EXTENSION, SIZE, RANDOM }
    enum class PlaceOnTop { NONE, ALBUMS_ON_TOP, FILES_ON_TOP }
    
    
    companion object {
        fun default() = MtreePreferences(
            theme = Theme.SYSTEM,
            dynamicColors = DynamicColors.SYSTEM,
            
            galleryMode = GalleryMode.TREE,
            portraitGridColumns = 3,
            landscapeGridColumns = 4,
            
            sortOrder = SortOrder.CREATION_DATE,
            descendSortOrder = false,
            placeOnTop = PlaceOnTop.ALBUMS_ON_TOP,
            
            showImages = true,
            showVideos = true,
            showGifs = true,
            showFiles = true,
            showAlbums = true,
            showUnHidden = true,
            showHidden = false
        )
    }
}
