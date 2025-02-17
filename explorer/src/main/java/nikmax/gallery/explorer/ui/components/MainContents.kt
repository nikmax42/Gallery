package nikmax.gallery.explorer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.components.ItemsGrid
import nikmax.gallery.data.preferences.GalleryPreferences


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewingContent(
    items: List<MediaItemUI>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (MediaItemUI) -> Unit,
    onItemLongClick: (MediaItemUI) -> Unit,
    showSheet: Boolean,
    onShowSheetChange: (Boolean) -> Unit,
    preferences: GalleryPreferences,
    onPreferencesChange: (GalleryPreferences) -> Unit,
    modifier: Modifier = Modifier
) {
    Box {
        PullToRefreshBox(
            isRefreshing = loading,
            onRefresh = { onRefresh() },
        ) {
            ItemsGrid(
                items = items,
                selectedItems = emptyList(),
                onItemClick = { onItemClick(it) },
                onItemLongClick = { onItemLongClick(it) },
                columnsAmountPortrait = preferences.gridColumnsPortrait,
                columnsAmountLandscape = preferences.gridColumnsLandscape,
                modifier = modifier
            )
        }
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { onShowSheetChange(false) },
                dragHandle = null
            ) {
                GalleryAppearanceMenu(
                    selectedAlbumsMode = preferences.albumsMode,
                    onAlbumsModeChange = { onPreferencesChange(preferences.copy(albumsMode = it)) },
                    gridPortraitColumnsAmount = preferences.gridColumnsPortrait,
                    onGridPortraitColumnsAmountChange = { onPreferencesChange(preferences.copy(gridColumnsPortrait = it)) },
                    gridLandscapeColumnsAmount = preferences.gridColumnsLandscape,
                    onGridLandscapeColumnsAmountChange = { onPreferencesChange(preferences.copy(gridColumnsLandscape = it)) },
                    selectedSortingType = preferences.sortingOrder,
                    onSortingTypeChange = { onPreferencesChange(preferences.copy(sortingOrder = it)) },
                    descend = preferences.descendSorting,
                    onDescendChange = { onPreferencesChange(preferences.copy(descendSorting = it)) },
                    selectedFilters = preferences.enabledFilters,
                    onFilterSelectionChange = { filter ->
                        onPreferencesChange(
                            preferences.copy(
                                enabledFilters = when (preferences.enabledFilters.contains(filter)) {
                                    true -> preferences.enabledFilters - filter
                                    false -> preferences.enabledFilters + filter
                                }
                            )
                        )
                    },
                    showHidden = preferences.showHidden,
                    onHiddenChange = { onPreferencesChange(preferences.copy(showHidden = it)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchingContent(
    items: List<MediaItemUI>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (MediaItemUI) -> Unit,
    onItemLongClick: (MediaItemUI) -> Unit,
    preferences: GalleryPreferences,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = loading,
        onRefresh = { onRefresh() },
    ) {
        ItemsGrid(
            items = items,
            selectedItems = emptyList(),
            onItemClick = { onItemClick(it) },
            onItemLongClick = { onItemLongClick(it) },
            columnsAmountPortrait = preferences.gridColumnsPortrait,
            columnsAmountLandscape = preferences.gridColumnsLandscape,
            modifier = modifier
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionContent(
    items: List<MediaItemUI>,
    selectedItems: List<MediaItemUI>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (MediaItemUI) -> Unit,
    onItemLongClick: (MediaItemUI) -> Unit,
    preferences: GalleryPreferences,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = loading,
        onRefresh = { onRefresh() },
    ) {
        ItemsGrid(
            items = items,
            selectedItems = selectedItems,
            onItemClick = { onItemClick(it) },
            onItemLongClick = { onItemLongClick(it) },
            columnsAmountPortrait = preferences.gridColumnsPortrait,
            columnsAmountLandscape = preferences.gridColumnsLandscape,
            modifier = modifier
        )
    }
}
