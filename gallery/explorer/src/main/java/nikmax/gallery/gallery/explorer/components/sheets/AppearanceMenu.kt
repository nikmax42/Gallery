package nikmax.gallery.gallery.explorer.components.sheets

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.core.preferences.GalleryPreferences


@Composable
fun GalleryAppearanceMenu(
    nestedAlbumsEnabled: Boolean,
    onNestedAlbumsChange: (Boolean) -> Unit,
    gridPortraitColumnsAmount: Int,
    onGridPortraitColumnsAmountChange: (Int) -> Unit,
    gridLandscapeColumnsAmount: Int,
    onGridLandscapeColumnsAmountChange: (Int) -> Unit,
    selectedSortingOrder: GalleryPreferences.Sorting.Order,
    onSortingOrderChange: (GalleryPreferences.Sorting.Order) -> Unit,
    descendSortingEnabled: Boolean,
    onDescendChange: (Boolean) -> Unit,
    includeImages: Boolean,
    onIncludeImagesChange: (Boolean) -> Unit,
    includeVideos: Boolean,
    onIncludeVideosChange: (Boolean) -> Unit,
    includeGifs: Boolean,
    onIncludeGifsChange: (Boolean) -> Unit,
    showHidden: Boolean,
    onHiddenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        stringResource(R.string.appearance),
        stringResource(R.string.sorting),
        stringResource(R.string.filtering),
    )
    var selectedTab by remember { mutableIntStateOf(0) }
    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = index == selectedTab,
                    onClick = { selectedTab = index },
                    text = { Text(text = tab) }
                )
            }
        }
        Column(modifier = modifier) {
            when (selectedTab) {
                // appearance
                0 -> AnimatedVisibility(visible = selectedTab == 0) {
                    AppearanceMenu(
                        nestedAlbumsEnabled = nestedAlbumsEnabled,
                        onAlbumsModeChange = { onNestedAlbumsChange(it) },
                        gridPortraitColumnsAmount = gridPortraitColumnsAmount,
                        onGridPortraitColumnsAmountChange = { onGridPortraitColumnsAmountChange(it) },
                        gridLandscapeColumnsAmount = gridLandscapeColumnsAmount,
                        onGridLandscapeColumnsAmountChange = { onGridLandscapeColumnsAmountChange(it) }
                    )
                }
                // sorting
                1 -> AnimatedVisibility(visible = selectedTab == 1) {
                    SortingMenu(
                        sortingOrder = selectedSortingOrder,
                        onSortingTypeChange = { onSortingOrderChange(it) },
                        descend = descendSortingEnabled,
                        onDescendChange = { onDescendChange(it) }
                    )
                }
                2 -> AnimatedVisibility(visible = selectedTab == 2) {
                    FilteringMenu(
                        includeImages = includeImages,
                        onIncludeImagesChange = { onIncludeImagesChange(it) },
                        includeVideos = includeVideos,
                        onIncludeVideosChange = { onIncludeVideosChange(it) },
                        includeGifs = includeGifs,
                        onIncludeGifsChange = { onIncludeGifsChange(it) },
                        includeHidden = showHidden,
                        onIncludeHiddenChange = { onHiddenChange(it) }
                    )
                }
            }
        }
    }
}
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun GalleryAppearanceMenuPreview() {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    scope.launch { sheetState.show() }
    GalleryAppearanceMenu(
        nestedAlbumsEnabled = true,
        onNestedAlbumsChange = {},
        gridPortraitColumnsAmount = 3,
        gridLandscapeColumnsAmount = 4,
        onGridPortraitColumnsAmountChange = {},
        onGridLandscapeColumnsAmountChange = {},
        selectedSortingOrder = GalleryPreferences.Sorting.Order.NAME,
        onSortingOrderChange = {},
        descendSortingEnabled = false,
        onDescendChange = {},
        includeImages = true,
        onIncludeImagesChange = {},
        includeVideos = true,
        onIncludeVideosChange = {},
        includeGifs = true,
        onIncludeGifsChange = {},
        showHidden = false,
        onHiddenChange = {}
    )
}


@Composable
private fun AppearanceMenu(
    nestedAlbumsEnabled: Boolean,
    onAlbumsModeChange: (Boolean) -> Unit,
    gridPortraitColumnsAmount: Int,
    onGridPortraitColumnsAmountChange: (Int) -> Unit,
    gridLandscapeColumnsAmount: Int,
    onGridLandscapeColumnsAmountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val orientation = LocalConfiguration.current.orientation

    Column(modifier = modifier) {
        // displaying mode
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = nestedAlbumsEnabled,
                    onClick = { onAlbumsModeChange(true) },
                    label = { Text(stringResource(R.string.nested_albums)) }
                )
            }
            item {
                FilterChip(
                    selected = !nestedAlbumsEnabled,
                    onClick = { onAlbumsModeChange(false) },
                    label = { Text(stringResource(R.string.plain_albums)) }
                )
            }
        }

        // grid appearance
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val minColumns = remember { 2F }
                val maxColumns = remember { 6F }
                var value by remember { mutableFloatStateOf(gridPortraitColumnsAmount.toFloat()) }
                Text(
                    text = stringResource(R.string.grid_cols_portrait) + ": ${value.toInt()}",
                    textAlign = TextAlign.Center
                )
                Slider(
                    value = value,
                    onValueChange = {
                        value = it
                        onGridPortraitColumnsAmountChange(value.toInt())
                    },
                    valueRange = minColumns..maxColumns,
                    steps = (maxColumns - minColumns).toInt() - 1
                )
            }
            else -> Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val minColumns = remember { 3F }
                val maxColumns = remember { 10F }
                var value by remember { mutableFloatStateOf(gridLandscapeColumnsAmount.toFloat()) }
                Text(
                    text = stringResource(R.string.grid_cols_landscape) + ": ${value.toInt()}",
                    textAlign = TextAlign.Center
                )
                Slider(
                    value = value,
                    onValueChange = {
                        value = it
                        onGridLandscapeColumnsAmountChange(value.toInt())
                    },
                    valueRange = minColumns..maxColumns,
                    steps = (maxColumns - minColumns).toInt() - 1
                )
            }
        }
    }
}


@Composable
private fun SortingMenu(
    sortingOrder: GalleryPreferences.Sorting.Order,
    onSortingTypeChange: (GalleryPreferences.Sorting.Order) -> Unit,
    descend: Boolean,
    onDescendChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        GalleryPreferences.Sorting.Order.entries.forEach { sorting ->
            TextButton(
                onClick = {
                    if (sorting == sortingOrder) onDescendChange(descend.not())
                    else onSortingTypeChange(sorting)
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (sorting == sortingOrder) {
                        Icon(
                            imageVector = if (descend) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = if (descend) stringResource(R.string.descend)
                            else stringResource(R.string.ascend)
                        )
                    } else Spacer(modifier = Modifier.width(24.dp))
                    val sortingName = when (sorting) {
                        GalleryPreferences.Sorting.Order.CREATION_DATE -> stringResource(R.string.creation_date)
                        GalleryPreferences.Sorting.Order.MODIFICATION_DATE -> stringResource(R.string.modification_date)
                        GalleryPreferences.Sorting.Order.NAME -> stringResource(R.string.name)
                        GalleryPreferences.Sorting.Order.SIZE -> stringResource(R.string.size)
                        GalleryPreferences.Sorting.Order.RANDOM -> stringResource(R.string.random)
                    }
                    Text(sortingName)
                }
            }
        }
    }
}


@Composable
private fun FilteringMenu(
    includeImages: Boolean,
    onIncludeImagesChange: (Boolean) -> Unit,
    includeVideos: Boolean,
    onIncludeVideosChange: (Boolean) -> Unit,
    includeGifs: Boolean,
    onIncludeGifsChange: (Boolean) -> Unit,
    includeHidden: Boolean,
    onIncludeHiddenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
    ) {
        item {
            FilterChip(
                selected = includeImages,
                onClick = { onIncludeImagesChange(includeImages.not()) },
                label = { Text(stringResource(R.string.images)) }
            )
        }
        item {
            FilterChip(
                selected = includeVideos,
                onClick = { onIncludeVideosChange(includeVideos.not()) },
                label = { Text(stringResource(R.string.videos)) }
            )
        }
        item {
            FilterChip(
                selected = includeGifs,
                onClick = { onIncludeGifsChange(includeGifs.not()) },
                label = { Text(stringResource(R.string.gifs)) }
            )
        }
        item {
            FilterChip(
                selected = includeHidden,
                onClick = { onIncludeHiddenChange(includeHidden.not()) },
                label = { Text(stringResource(R.string.hidden)) }
            )
        }
    }
}
