package nikmax.gallery.gallery.explorer.components.main_contents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import nikmax.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.gallery.core.ui.components.grid.ItemsGrid
import nikmax.gallery.gallery.explorer.ExplorerVm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainContent(
    state: ExplorerVm.UIState,
    onAction: (ExplorerVm.UserAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by GalleryPreferencesUtils
        .getPreferencesFlow(LocalContext.current)
        .collectAsState(GalleryPreferences())
    
    Box {
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onAction(ExplorerVm.UserAction.Refresh) },
        ) {
            ItemsGrid(
                items = state.items,
                selectedItems = state.selectedItems,
                onItemOpen = { onAction(ExplorerVm.UserAction.ItemOpen(it)) },
                onSelectionChange = { onAction(ExplorerVm.UserAction.ItemsSelectionChange(it)) },
                columnsAmountPortrait = preferences.appearance.gridAppearance.portraitColumns,
                columnsAmountLandscape = preferences.appearance.gridAppearance.landscapeColumns,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
