package nikmax.gallery.explorer.components.main_contents

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.components.grid.ItemsGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploringContent(
    items: List<MediaItemUI>,
    selectedItems: List<MediaItemUI>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onItemOpen: (MediaItemUI) -> Unit,
    onSelectionChange: (List<MediaItemUI>) -> Unit,
    portraitColumnsAmount: Int,
    landscapeColumnsAmount: Int,
    modifier: Modifier = Modifier
) {
    Surface {
        // to show bottom sheet above the content
        Box {
            PullToRefreshBox(
                isRefreshing = loading,
                onRefresh = { onRefresh() },
            ) {
                ItemsGrid(
                    items = items,
                    selectedItems = selectedItems,
                    onItemOpen = { onItemOpen(it) },
                    onSelectionChange = { onSelectionChange(it) },
                    columnsAmountPortrait = portraitColumnsAmount,
                    columnsAmountLandscape = landscapeColumnsAmount,
                    modifier = modifier
                )
            }
        }
    }
}
