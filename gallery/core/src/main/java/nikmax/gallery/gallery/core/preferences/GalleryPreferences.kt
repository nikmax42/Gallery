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
        val nestedAlbumsEnabled: Boolean = true,
        val theme: Theme = Theme.SYSTEM,
        val dynamicColors: DynamicColors = DynamicColors.SYSTEM,
        val gridAppearance: GridAppearance = GridAppearance()
    ) {
        enum class Theme { SYSTEM, LIGHT, DARK }
        enum class DynamicColors { SYSTEM, DISABLED }
        @Serializable
        data class GridAppearance(
            val portraitColumns: Int = 3,
            val landscapeColumns: Int = 4
        )
    }

    @Serializable
    data class Sorting(
        val order: Order = Order.CREATION_DATE,
        val descend: Boolean = false,
        val onTop: OnTop = OnTop.ALBUMS_ON_TOP
    ) {
        enum class Order { CREATION_DATE, MODIFICATION_DATE, NAME, EXTENSION, SIZE, RANDOM }
        enum class OnTop { NONE, ALBUMS_ON_TOP, FILES_ON_TOP }
    }

    @Serializable
    data class Filtering(
        val includeImages: Boolean = true,
        val includeVideos: Boolean = true,
        val includeGifs: Boolean = true,
        val includeFiles: Boolean = true,
        val includeAlbums: Boolean = true,
        val includeUnHidden: Boolean = true,
        val includeHidden: Boolean = false
    )
}
