package nikmax.gallery.gallery.explorer.components.preferences_sheet


internal sealed interface Action {
    data object Launch : Action
    data class ChangeTab(val tab: Tab) : Action
    data class ChangeMode(val galleryMode: GalleryMode) : Action
    data class ChangeTheme(val theme: AppTheme) : Action
    data class ChangeDynamicColors(val colors: DynamicColors) : Action
    data class ChangeGridColumnsPortrait(val columns: Int) : Action
    data class ChangeGridColumnsLandscape(val columns: Int) : Action
    data class ChangePlaceOnTop(val placeOnTop: PlaceOnTop) : Action
    data class ChangeSortOrder(val sortOrder: SortOrder) : Action
    data class ChangeDescendSorting(val enabled: Boolean) : Action
    data class ChangeIncludeImages(val enabled: Boolean) : Action
    data class ChangeIncludeVideos(val enabled: Boolean) : Action
    data class ChangeIncludeGifs(val enabled: Boolean) : Action
    data class ChangeIncludeHidden(val enabled: Boolean) : Action
    data class ChangeIncludeUnHidden(val enabled: Boolean) : Action
    data class ChangeIncludeFiles(val enabled: Boolean) : Action
    data class ChangeIncludeAlbums(val enabled: Boolean) : Action
}
