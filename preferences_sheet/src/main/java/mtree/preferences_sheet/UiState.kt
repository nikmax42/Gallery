package mtree.preferences_sheet

data class UiState(
    val mode: GalleryMode,
    val theme: AppTheme,
    val dynamicColors: DynamicColors,
    val gridColumnsPortrait: Int,
    val gridColumnsLandscape: Int,
    
    val placeOnTop: PlaceOnTop,
    val sortOrder: SortOrder,
    val descendSorting: Boolean,
    
    val showImages: Boolean,
    val showVideos: Boolean,
    val showGifs: Boolean,
    val showHidden: Boolean,
    val showUnHidden: Boolean,
    val showFiles: Boolean,
    val showAlbums: Boolean
) {
    companion object {
        fun default() = UiState(
            mode = GalleryMode.TREE,
            theme = AppTheme.SYSTEM,
            dynamicColors = DynamicColors.SYSTEM,
            gridColumnsPortrait = 3,
            gridColumnsLandscape = 4,
            placeOnTop = PlaceOnTop.ALBUMS,
            sortOrder = SortOrder.CREATIOIN_DATE,
            descendSorting = false,
            showImages = true,
            showVideos = true,
            showGifs = true,
            showHidden = false,
            showUnHidden = true,
            showFiles = true,
            showAlbums = true
        )
    }
}
