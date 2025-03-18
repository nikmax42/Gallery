package nikmax.gallery.gallery.explorer.components.sheets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.preferences.GalleryPreferencesUtils

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
        GalleryPreferencesMenu(
            nestedAlbumsEnabled = galleryPreferences.appearance.nestedAlbumsEnabled,
            onNestedAlbumsChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        appearance = galleryPreferences.appearance.copy(nestedAlbumsEnabled = it)
                    )
                )
            },
            onTop = galleryPreferences.appearance.onTop,
            onTopChange = {
                setNewPreferences(
                    galleryPreferences.copy(
                        appearance = galleryPreferences.appearance.copy(onTop = it)
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


@Composable
private fun GalleryPreferencesMenu(
    nestedAlbumsEnabled: Boolean,
    onNestedAlbumsChange: (Boolean) -> Unit,
    onTop: GalleryPreferences.Appearance.OnTop,
    onTopChange: (GalleryPreferences.Appearance.OnTop) -> Unit,
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
                0 -> AppearanceMenu(
                    nestedAlbumsEnabled = nestedAlbumsEnabled,
                    onAlbumsModeChange = { onNestedAlbumsChange(it) },
                    onTop = onTop,
                    onTopChange = { onTopChange(it) },
                    gridPortraitColumnsAmount = gridPortraitColumnsAmount,
                    onGridPortraitColumnsAmountChange = { onGridPortraitColumnsAmountChange(it) },
                    gridLandscapeColumnsAmount = gridLandscapeColumnsAmount,
                    onGridLandscapeColumnsAmountChange = { onGridLandscapeColumnsAmountChange(it) },
                )
                // sorting
                1 -> SortingMenu(
                    selectedSortOrder = selectedSortingOrder,
                    onSortingTypeChange = { onSortingOrderChange(it) },
                    descend = descendSortingEnabled,
                    onDescendChange = { onDescendChange(it) }
                )
                // filtering
                2 -> FilteringMenu(
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
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun GalleryAppearanceMenuPreview() {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    scope.launch { sheetState.show() }

    GalleryTheme {
        GalleryPreferencesMenu(
            nestedAlbumsEnabled = true,
            onNestedAlbumsChange = {},
            onTop = GalleryPreferences.Appearance.OnTop.ALBUMS_ON_TOP,
            onTopChange = {},
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
}


@Composable
private fun AppearanceMenu(
    nestedAlbumsEnabled: Boolean,
    onAlbumsModeChange: (Boolean) -> Unit,
    onTop: GalleryPreferences.Appearance.OnTop,
    onTopChange: (GalleryPreferences.Appearance.OnTop) -> Unit,
    gridPortraitColumnsAmount: Int,
    onGridPortraitColumnsAmountChange: (Int) -> Unit,
    gridLandscapeColumnsAmount: Int,
    onGridLandscapeColumnsAmountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        // display mode
        Column {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.display_mode),
                style = MaterialTheme.typography.titleSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = nestedAlbumsEnabled,
                    onClick = { onAlbumsModeChange(true) },
                    leadingIcon = { Icon(Icons.Default.AccountTree, null) },
                    label = { Text(stringResource(R.string.tree_view)) }
                )
                FilterChip(
                    selected = !nestedAlbumsEnabled,
                    onClick = { onAlbumsModeChange(false) },
                    leadingIcon = { Icon(Icons.Default.GridView, null) },
                    label = { Text(stringResource(R.string.plain_view)) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = onTop == GalleryPreferences.Appearance.OnTop.ALBUMS_ON_TOP,
                    onClick = {
                        val newValue = when (onTop == GalleryPreferences.Appearance.OnTop.ALBUMS_ON_TOP) {
                            true -> GalleryPreferences.Appearance.OnTop.NONE
                            false -> GalleryPreferences.Appearance.OnTop.ALBUMS_ON_TOP
                        }
                        onTopChange(newValue)
                    },
                    leadingIcon = { Icon(Icons.Default.Folder, null) },
                    label = { Text(stringResource(R.string.albums_on_top)) }
                )
                FilterChip(
                    selected = onTop == GalleryPreferences.Appearance.OnTop.FILES_ON_TOP,
                    onClick = {
                        val newValue = when (onTop == GalleryPreferences.Appearance.OnTop.FILES_ON_TOP) {
                            true -> GalleryPreferences.Appearance.OnTop.NONE
                            false -> GalleryPreferences.Appearance.OnTop.FILES_ON_TOP
                        }
                        onTopChange(newValue)
                    },
                    leadingIcon = { Icon(Icons.Default.Image, null) },
                    label = { Text(stringResource(R.string.files_on_top)) }
                )
            }
        }
        // grid appearance
        Column {
            Text(
                text = stringResource(R.string.grid_appearance),
                style = MaterialTheme.typography.titleSmall
            )
            Row(
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
            Row(
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

@Preview(showBackground = true)
@Composable
private fun AppearancePreview() {
    GalleryTheme {
        AppearanceMenu(
            nestedAlbumsEnabled = true,
            onAlbumsModeChange = {},
            onTop = GalleryPreferences.Appearance.OnTop.ALBUMS_ON_TOP,
            onTopChange = {},
            gridPortraitColumnsAmount = 3,
            onGridPortraitColumnsAmountChange = {},
            gridLandscapeColumnsAmount = 4,
            onGridLandscapeColumnsAmountChange = {},
        )
    }
}


@Composable
private fun SortingMenu(
    selectedSortOrder: GalleryPreferences.Sorting.Order,
    onSortingTypeChange: (GalleryPreferences.Sorting.Order) -> Unit,
    descend: Boolean,
    onDescendChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        GalleryPreferences.Sorting.Order.entries.forEach { order ->
            TextButton(
                onClick = {
                    if (order == selectedSortOrder) onDescendChange(descend.not())
                    else onSortingTypeChange(order)
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (order == selectedSortOrder) {
                        val icon = when (order == GalleryPreferences.Sorting.Order.RANDOM) {
                            true -> Icons.Default.Shuffle
                            false -> if (descend) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    } else Spacer(modifier = Modifier.width(24.dp))
                    val sortingName = when (order) {
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

@Preview(showBackground = true)
@Composable
private fun SortingPreview() {
    GalleryTheme {
        SortingMenu(
            selectedSortOrder = GalleryPreferences.Sorting.Order.RANDOM,
            onSortingTypeChange = {},
            descend = false,
            onDescendChange = {}
        )
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
    Column {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
        ) {
            item {
                FilterCheckBox(
                    checked = includeImages,
                    onCheckedChange = { onIncludeImagesChange(it) },
                    label = stringResource(R.string.images)
                )
            }
            item {
                FilterCheckBox(
                    checked = includeVideos,
                    onCheckedChange = { onIncludeVideosChange(it) },
                    label = stringResource(R.string.videos)
                )
            }
            item {
                FilterCheckBox(
                    checked = includeGifs,
                    onCheckedChange = { onIncludeGifsChange(it) },
                    label = stringResource(R.string.gifs)
                )
            }
            item {
                FilterCheckBox(
                    checked = includeHidden,
                    onCheckedChange = { onIncludeHiddenChange(it) },
                    label = stringResource(R.string.hidden)
                )
            }
        }
    }
}

@Composable
private fun FilterCheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
        Text(label)
    }
}

@Preview(showBackground = true)
@Composable
private fun FilteringPreview() {
    GalleryTheme {
        FilteringMenu(
            includeImages = true,
            onIncludeImagesChange = {},
            includeVideos = true,
            onIncludeVideosChange = {},
            includeGifs = true,
            onIncludeGifsChange = {},
            includeHidden = true,
            onIncludeHiddenChange = {}
        )
    }
}
