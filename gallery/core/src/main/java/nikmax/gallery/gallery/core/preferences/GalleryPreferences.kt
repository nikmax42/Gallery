package nikmax.gallery.gallery.core.preferences

import kotlinx.serialization.Serializable

@Serializable
data class GalleryPreferences(
    val appearance: Appearance = Appearance(),
    val sorting: Sorting = Sorting(),
    val filtering: Filtering = Filtering()
) {
    @Serializable
    data class Appearance(
        val dynamicColorsEnabled: Boolean = true,
        val grid: Grid = Grid(),
        val nestedAlbumsEnabled: Boolean = true
    ) {
        @Serializable
        data class Grid(
            val portraitColumns: Int = 3,
            val landscapeColumns: Int = 4
        )
    }

    @Serializable
    data class Sorting(
        val order: Order = Order.CREATION_DATE,
        val descend: Boolean = false,
        val albumsFirst: Boolean = true
    ) {
        enum class Order { CREATION_DATE, MODIFICATION_DATE, NAME, SIZE, RANDOM }
    }

    @Serializable
    data class Filtering(
        val includeImages: Boolean = true,
        val includeVideos: Boolean = true,
        val includeGifs: Boolean = true,
        val includeHidden: Boolean = false,
        val includeFilesOnly: Boolean = false
    )
}
