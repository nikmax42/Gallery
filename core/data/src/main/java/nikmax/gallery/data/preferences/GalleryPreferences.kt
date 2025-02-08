package nikmax.gallery.data.preferences

data class GalleryPreferences(
    val albumsMode: AlbumsMode = AlbumsMode.PLAIN,
    val sortingOrder: SortingOrder = SortingOrder.CREATION_DATE,
    val descendSorting: Boolean = false,
    val gridColumnsPortrait: Int = 3,
    val gridColumnsLandscape: Int = 4,
    val enabledFilters: Set<Filter> = setOf(Filter.IMAGES, Filter.VIDEOS, Filter.GIFS),
    val showHidden: Boolean = false
) {
    enum class AlbumsMode { PLAIN, NESTED }
    enum class SortingOrder { NAME, SIZE, CREATION_DATE, MODIFICATION_DATE }
    enum class Filter { IMAGES, VIDEOS, GIFS }
}
