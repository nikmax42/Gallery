package nikmax.mtree.gallery.explorer.components.main_contents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nikmax.mtree.core.ui.theme.GalleryTheme
import nikmax.mtree.gallery.core.ui.MediaItemUI
import nikmax.mtree.gallery.core.ui.components.grid.ItemsGrid
import nikmax.mtree.gallery.explorer.Action
import nikmax.mtree.gallery.explorer.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainContent(
    state: UiState,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) {
    Box {
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onAction(Action.Refresh) },
            modifier = modifier
        ) {
            ItemsGrid(
                items = state.items,
                selectedItems = state.selectedItems,
                onItemOpen = { onAction(Action.ItemOpen(it)) },
                onSelectionChange = { onAction(Action.ItemsSelectionChange(it)) },
                columnsAmountPortrait = state.portraitGridColumns,
                columnsAmountLandscape = state.landscapeGridColumns,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview
@Composable
private fun MainContentPreview() {
    val state = UiState(
        items = listOf(
            MediaItemUI.File("/test.jpg"),
            MediaItemUI.Album("/test"),
        ),
        selectedItems = emptyList(),
        isLoading = false
    )
    GalleryTheme {
        MainContent(state, onAction = {})
    }
}
