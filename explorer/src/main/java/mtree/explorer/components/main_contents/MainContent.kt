package mtree.explorer.components.main_contents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mtree.core.ui.components.grid.ItemsGrid
import mtree.core.ui.models.MediaItemUI
import mtree.core.ui.theme.GalleryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainContent(
    items: List<MediaItemUI>,
    selectedItems: List<MediaItemUI>,
    isLoading: Boolean,
    onFileOpen: (MediaItemUI.File) -> Unit,
    onAlbumOpen: (MediaItemUI.Album) -> Unit,
    onSelectionChange: (newSelection: List<MediaItemUI>) -> Unit,
    portraitGridColumns: Int,
    landscapeGridColumns: Int,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { onRefresh() },
            modifier = modifier
        ) {
            ItemsGrid(
                items = items,
                selectedItems = selectedItems,
                onItemOpen = { item ->
                    when (item) {
                        is MediaItemUI.Album -> onAlbumOpen(item)
                        is MediaItemUI.File -> onFileOpen(item)
                    }
                },
                onSelectionChange = { onSelectionChange(it) },
                columnsAmountPortrait = portraitGridColumns,
                columnsAmountLandscape = landscapeGridColumns,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview
@Composable
private fun MainContentPreview() {
    
    val items = listOf(
        MediaItemUI.File.emptyFromPath("/test.jpg"),
        MediaItemUI.Album.emptyFromPath("/test"),
    )
    val selectedItems = listOf(items.first())
    val isLoading = false
    
    GalleryTheme {
        MainContent(
            items = items,
            selectedItems = selectedItems,
            isLoading = isLoading,
            onFileOpen = {},
            onAlbumOpen = {},
            onSelectionChange = {},
            portraitGridColumns = 3,
            landscapeGridColumns = 4,
            onRefresh = {},
        )
    }
}
