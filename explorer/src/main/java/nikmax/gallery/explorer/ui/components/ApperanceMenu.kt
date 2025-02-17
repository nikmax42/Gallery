package nikmax.gallery.explorer.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import nikmax.gallery.data.preferences.GalleryPreferences


@Composable
fun GalleryAppearanceMenu(
    selectedAlbumsMode: GalleryPreferences.AlbumsMode,
    onAlbumsModeChange: (GalleryPreferences.AlbumsMode) -> Unit,
    gridPortraitColumnsAmount: Int,
    onGridPortraitColumnsAmountChange: (Int) -> Unit,
    gridLandscapeColumnsAmount: Int,
    onGridLandscapeColumnsAmountChange: (Int) -> Unit,
    selectedSortingType: GalleryPreferences.SortingOrder,
    onSortingTypeChange: (GalleryPreferences.SortingOrder) -> Unit,
    descend: Boolean,
    onDescendChange: (Boolean) -> Unit,
    selectedFilters: Set<GalleryPreferences.Filter>,
    onFilterSelectionChange: (GalleryPreferences.Filter) -> Unit,
    showHidden: Boolean,
    onHiddenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        stringResource(nikmax.gallery.core.R.string.appearance),
        stringResource(nikmax.gallery.core.R.string.sorting),
        stringResource(nikmax.gallery.core.R.string.filtering),
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
                        selectedAlbumsMode = selectedAlbumsMode,
                        onAlbumsModeChange = { onAlbumsModeChange(it) },
                        gridPortraitColumnsAmount = gridPortraitColumnsAmount,
                        onGridPortraitColumnsAmountChange = { onGridPortraitColumnsAmountChange(it) },
                        gridLandscapeColumnsAmount = gridLandscapeColumnsAmount,
                        onGridLandscapeColumnsAmountChange = { onGridLandscapeColumnsAmountChange(it) }
                    )
                }
                // sorting
                1 -> AnimatedVisibility(visible = selectedTab == 1) {
                    SortingMenu(
                        selectedType = selectedSortingType,
                        onSortingTypeChange = { onSortingTypeChange(it) },
                        descend = descend,
                        onDescendChange = { onDescendChange(it) }
                    )
                }
                2 -> AnimatedVisibility(visible = selectedTab == 2) {
                    FilteringMenu(
                        selectedFilters = selectedFilters,
                        onSelectionChange = { onFilterSelectionChange(it) },
                        hiddenEnabled = showHidden,
                        onHiddenChange = { onHiddenChange(it) }
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
        selectedAlbumsMode = GalleryPreferences.AlbumsMode.entries.first(),
        onAlbumsModeChange = {},
        gridPortraitColumnsAmount = 3,
        gridLandscapeColumnsAmount = 4,
        onGridPortraitColumnsAmountChange = {},
        onGridLandscapeColumnsAmountChange = {},
        selectedSortingType = GalleryPreferences.SortingOrder.entries.first(),
        onSortingTypeChange = {},
        descend = false,
        onDescendChange = {},
        selectedFilters = emptySet(),
        onFilterSelectionChange = {},
        showHidden = false,
        onHiddenChange = {}
    )
}


@Composable
private fun AppearanceMenu(
    selectedAlbumsMode: GalleryPreferences.AlbumsMode,
    onAlbumsModeChange: (GalleryPreferences.AlbumsMode) -> Unit,
    gridPortraitColumnsAmount: Int,
    onGridPortraitColumnsAmountChange: (Int) -> Unit,
    gridLandscapeColumnsAmount: Int,
    onGridLandscapeColumnsAmountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val orientation = LocalConfiguration.current.orientation

    Column(modifier = modifier) {
        // displaying mode
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GalleryPreferences.AlbumsMode.entries.forEachIndexed { index, mode ->
                FilterChip(
                    selected = mode == selectedAlbumsMode,
                    onClick = { onAlbumsModeChange(mode) },
                    label = {
                        val modeName = when (mode) {
                            GalleryPreferences.AlbumsMode.PLAIN -> stringResource(nikmax.gallery.core.R.string.plain_albums)
                            GalleryPreferences.AlbumsMode.NESTED -> stringResource(nikmax.gallery.core.R.string.nested_albums)
                        }
                        Text(modeName)
                    }
                )
            }
        }

        // grid appearance
        when (orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val minColumns = remember { 2F }
                val maxColumns = remember { 6F }
                var value by remember { mutableFloatStateOf(gridPortraitColumnsAmount.toFloat()) }
                Text(
                    text = stringResource(nikmax.gallery.core.R.string.grid_cols_portrait) + ": ${value.toInt()}",
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
                    text = stringResource(nikmax.gallery.core.R.string.grid_cols_landscape) + ": ${value.toInt()}",
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
private fun AppearanceMenuPreview() {
    var selectedAlbumsMode by remember { mutableStateOf(GalleryPreferences.AlbumsMode.entries.first()) }
    var gridPortraitColumnsAmount by remember { mutableIntStateOf(3) }
    var gridLandscapeColumnsAmount by remember { mutableIntStateOf(4) }
    AppearanceMenu(
        selectedAlbumsMode = selectedAlbumsMode,
        onAlbumsModeChange = { selectedAlbumsMode = it },
        gridPortraitColumnsAmount = gridPortraitColumnsAmount,
        onGridPortraitColumnsAmountChange = { gridPortraitColumnsAmount = it },
        gridLandscapeColumnsAmount = gridLandscapeColumnsAmount,
        onGridLandscapeColumnsAmountChange = { gridLandscapeColumnsAmount = it }
    )
}


@Composable
private fun SortingMenu(
    selectedType: GalleryPreferences.SortingOrder,
    onSortingTypeChange: (GalleryPreferences.SortingOrder) -> Unit,
    descend: Boolean,
    onDescendChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        GalleryPreferences.SortingOrder.entries.forEach { sorting ->
            TextButton(
                onClick = {
                    if (sorting == selectedType) onDescendChange(descend.not())
                    else onSortingTypeChange(sorting)
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (sorting == selectedType) {
                        Icon(
                            imageVector = if (descend) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = if (descend) stringResource(nikmax.gallery.core.R.string.descend)
                            else stringResource(nikmax.gallery.core.R.string.ascend)
                        )
                    } else Spacer(modifier = Modifier.width(24.dp))
                    val sortingName = when (sorting) {
                        GalleryPreferences.SortingOrder.CREATION_DATE -> stringResource(nikmax.gallery.core.R.string.creation_date)
                        GalleryPreferences.SortingOrder.MODIFICATION_DATE -> stringResource(nikmax.gallery.core.R.string.modification_date)
                        GalleryPreferences.SortingOrder.NAME -> stringResource(nikmax.gallery.core.R.string.name)
                        GalleryPreferences.SortingOrder.SIZE -> stringResource(nikmax.gallery.core.R.string.size)
                    }
                    Text(sortingName)
                }
            }
        }
    }
}
@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun SortingMenuPreview() {
    var selectedType by remember { mutableStateOf(GalleryPreferences.SortingOrder.entries.first()) }
    var descend by remember { mutableStateOf(false) }
    SortingMenu(
        selectedType = selectedType,
        onSortingTypeChange = { selectedType = it },
        descend = descend,
        onDescendChange = { descend = it }
    )
}


@Composable
private fun FilteringMenu(
    selectedFilters: Set<GalleryPreferences.Filter>,
    onSelectionChange: (GalleryPreferences.Filter) -> Unit,
    hiddenEnabled: Boolean,
    onHiddenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        GalleryPreferences.Filter.entries.forEach { filter ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectionChange(filter) }
                ) {
                    Checkbox(
                        checked = selectedFilters.contains(filter),
                        onCheckedChange = { onSelectionChange(filter) }
                    )
                    val text = when (filter) {
                        GalleryPreferences.Filter.IMAGES -> stringResource(nikmax.gallery.core.R.string.images)
                        GalleryPreferences.Filter.VIDEOS -> stringResource(nikmax.gallery.core.R.string.videos)
                        GalleryPreferences.Filter.GIFS -> stringResource(nikmax.gallery.core.R.string.gifs)
                    }
                    Text(text)
                }
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHiddenChange(hiddenEnabled.not()) }
            ) {
                Checkbox(
                    checked = hiddenEnabled,
                    onCheckedChange = { onHiddenChange(hiddenEnabled.not()) },
                )
                Text(stringResource(nikmax.gallery.core.R.string.hidden))
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun FilteringMenuPreview() {
    var selectedFilters = remember { mutableStateListOf(GalleryPreferences.Filter.entries.first()) }
    var hiddenEnabled by remember { mutableStateOf(false) }
    FilteringMenu(
        selectedFilters = selectedFilters.toSet(),
        onSelectionChange = {
            when (selectedFilters.contains(it)) {
                true -> selectedFilters -= it
                false -> selectedFilters += it
            }
        },
        hiddenEnabled = hiddenEnabled,
        onHiddenChange = { hiddenEnabled = it }
    )
}
