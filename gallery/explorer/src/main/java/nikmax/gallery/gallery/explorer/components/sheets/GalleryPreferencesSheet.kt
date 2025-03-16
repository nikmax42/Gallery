package nikmax.gallery.gallery.explorer.components.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nikmax.gallery.core.data.preferences.GalleryPreferences
import nikmax.gallery.core.data.preferences.GalleryPreferencesUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GalleryPreferencesSheet(
    onShowSheetChange: (showSheet: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val galleryPreferences by GalleryPreferencesUtils
        .getPreferencesFlow(context)
        .collectAsState(GalleryPreferences())

    fun setNewPreferences(prefs: GalleryPreferences) {
        scope.launch {
            GalleryPreferencesUtils.savePreferences(prefs, context)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onShowSheetChange(false) },
        dragHandle = null,
        modifier = modifier
    ) {
        GalleryAppearanceMenu(
            nestedAlbumsEnabled = galleryPreferences.appearance.nestedAlbumsEnabled,
            onNestedAlbumsChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        appearance = galleryPreferences.appearance.copy(nestedAlbumsEnabled = it)
                    )
                )
            },
            gridPortraitColumnsAmount = galleryPreferences.appearance.grid.portraitColumns,
            onGridPortraitColumnsAmountChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        appearance = galleryPreferences.appearance.copy(
                            grid = galleryPreferences.appearance.grid.copy(portraitColumns = it)
                        )
                    )
                )
            },
            gridLandscapeColumnsAmount = galleryPreferences.appearance.grid.landscapeColumns,
            onGridLandscapeColumnsAmountChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        appearance = galleryPreferences.appearance.copy(
                            grid = galleryPreferences.appearance.grid.copy(landscapeColumns = it)
                        )
                    )
                )
            },
            selectedSortingOrder = galleryPreferences.sorting.order,
            onSortingOrderChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        sorting = galleryPreferences.sorting.copy(
                            order = it
                        )
                    )
                )
            },
            descendSortingEnabled = galleryPreferences.sorting.descend,
            onDescendChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        sorting = galleryPreferences.sorting.copy(descend = it)
                    )
                )
            },
            includeImages = galleryPreferences.filtering.includeImages,
            onIncludeImagesChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        filtering = galleryPreferences.filtering.copy(includeImages = it)
                    )
                )
            },
            includeVideos = galleryPreferences.filtering.includeVideos,
            onIncludeVideosChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        filtering = galleryPreferences.filtering.copy(includeVideos = it)
                    )
                )
            },
            includeGifs = galleryPreferences.filtering.includeGifs,
            onIncludeGifsChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        filtering = galleryPreferences.filtering.copy(includeGifs = it)
                    )
                )
            },
            showHidden = galleryPreferences.filtering.includeHidden,
            onHiddenChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        filtering = galleryPreferences.filtering.copy(includeHidden = it)
                    )
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
