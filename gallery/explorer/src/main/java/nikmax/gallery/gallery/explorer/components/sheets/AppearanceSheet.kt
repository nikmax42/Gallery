package nikmax.gallery.gallery.explorer.components.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.data.preferences.OLDGalleryPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppearanceSheet(
    appPreferences: OLDGalleryPreferences,
    onPreferencesChange: (newPreferences: OLDGalleryPreferences) -> Unit,
    onShowSheetChange: (showSheet: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = { onShowSheetChange(false) },
        dragHandle = null
    ) {
        GalleryAppearanceMenu(
            selectedAlbumsMode = appPreferences.albumsMode,
            onAlbumsModeChange = { onPreferencesChange(appPreferences.copy(albumsMode = it)) },
            gridPortraitColumnsAmount = appPreferences.gridColumnsPortrait,
            onGridPortraitColumnsAmountChange = { onPreferencesChange(appPreferences.copy(gridColumnsPortrait = it)) },
            gridLandscapeColumnsAmount = appPreferences.gridColumnsLandscape,
            onGridLandscapeColumnsAmountChange = { onPreferencesChange(appPreferences.copy(gridColumnsLandscape = it)) },
            selectedSortingType = appPreferences.sortingOrder,
            onSortingTypeChange = { onPreferencesChange(appPreferences.copy(sortingOrder = it)) },
            descend = appPreferences.descendSorting,
            onDescendChange = { onPreferencesChange(appPreferences.copy(descendSorting = it)) },
            selectedFilters = appPreferences.enabledFilters,
            onFilterSelectionChange = { filter ->
                onPreferencesChange(
                    appPreferences.copy(
                        enabledFilters = when (appPreferences.enabledFilters.contains(filter)) {
                            true -> appPreferences.enabledFilters - filter
                            false -> appPreferences.enabledFilters + filter
                        }
                    )
                )
            },
            showHidden = appPreferences.showHidden,
            onHiddenChange = { onPreferencesChange(appPreferences.copy(showHidden = it)) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
