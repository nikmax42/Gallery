package nikmax.gallery.gallery.explorer.components.main_contents

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nikmax.gallery.core.ui.components.grid.ItemsGrid
import nikmax.gallery.gallery.explorer.ExplorerVm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainContent(
    state: ExplorerVm.UIState,
    onAction: (ExplorerVm.UserAction) -> Unit,
    modifier: Modifier = Modifier
) {
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
                columnsAmountPortrait = state.appPreferences.gridColumnsPortrait,
                columnsAmountLandscape = state.appPreferences.gridColumnsLandscape,
                modifier = modifier
            )
        }
    }
}
