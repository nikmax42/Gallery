package nikmax.mtree.gallery.explorer.components.preferences_sheet

internal data class UiState(
    val tab: Tab = Tab.APPEARANCE,
    
    val mode: GalleryMode = GalleryMode.TREE,
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColors: DynamicColors = DynamicColors.SYSTEM,
    val gridColumnsPortrait: Int = 3,
    val gridColumnsLandscape: Int = 4,
    
    val placeOnTop: PlaceOnTop = PlaceOnTop.ALBUMS,
    val sortOrder: SortOrder = SortOrder.DATE_MODIFIED,
    val descendSorting: Boolean = false,
    
    val showImages: Boolean = true,
    val showVideos: Boolean = true,
    val showGifs: Boolean = true,
    val showHidden: Boolean = true,
    val showUnHidden: Boolean = true,
    val showFiles: Boolean = true,
    val showAlbums: Boolean = true
)
