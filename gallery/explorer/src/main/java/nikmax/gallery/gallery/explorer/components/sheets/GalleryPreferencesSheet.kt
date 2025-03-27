package nikmax.gallery.gallery.explorer.components.sheets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.GifBox
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.gallery.explorer.components.drawables.PaletteDisabled

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
            galleryPreferences = galleryPreferences,
            setNewPreferences = { setNewPreferences(it) }
        )
    }
}


@Composable
private fun GalleryPreferencesMenu(
    galleryPreferences: GalleryPreferences,
    setNewPreferences: (GalleryPreferences) -> Unit,
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
        Column(modifier = modifier.padding(16.dp)) {
            when (selectedTab) {
                // appearance
                0 -> AppearanceMenu(
                    treeAlbumsEnabled = galleryPreferences.appearance.nestedAlbumsEnabled,
                    onTreeAlbumsChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                appearance = galleryPreferences.appearance.copy(nestedAlbumsEnabled = it)
                            )
                        )
                    },
                    theme = galleryPreferences.appearance.theme,
                    onThemeChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                appearance = galleryPreferences.appearance.copy(theme = it)
                            )
                        )
                    },
                    dynamicColors = galleryPreferences.appearance.dynamicColors,
                    onDynamicColorsChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                appearance = galleryPreferences.appearance.copy(dynamicColors = it)
                            )
                        )
                    },
                    gridPortraitColumnsAmount = galleryPreferences.appearance.gridAppearance.portraitColumns,
                    onGridPortraitColumnsAmountChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                appearance = galleryPreferences.appearance.copy(
                                    gridAppearance = galleryPreferences.appearance.gridAppearance.copy(
                                        portraitColumns = it
                                    )
                                )
                            )
                        )
                    },
                    gridLandscapeColumnsAmount = galleryPreferences.appearance.gridAppearance.landscapeColumns,
                    onGridLandscapeColumnsAmountChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                appearance = galleryPreferences.appearance.copy(
                                    gridAppearance = galleryPreferences.appearance.gridAppearance.copy(
                                        landscapeColumns = it
                                    )
                                )
                            )
                        )
                    },
                )
                // sorting
                1 -> SortingMenu(
                    onTop = galleryPreferences.sorting.onTop,
                    onOnTopChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                sorting = galleryPreferences.sorting.copy(
                                    onTop = it
                                )
                            )
                        )
                    },
                    sortOrder = galleryPreferences.sorting.order,
                    onSortOrderChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                sorting = galleryPreferences.sorting.copy(
                                    order = it
                                )
                            )
                        )
                    },
                    descendSorting = galleryPreferences.sorting.descend,
                    onDescendChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                sorting = galleryPreferences.sorting.copy(descend = it)
                            )
                        )
                    },
                )
                // filtering
                2 -> FilteringMenu(
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
                    includeFiles = galleryPreferences.filtering.includeFiles,
                    onIncludeFilesChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                filtering = galleryPreferences.filtering.copy(includeFiles = it)
                            )
                        )
                    },
                    includeAlbums = galleryPreferences.filtering.includeAlbums,
                    onIncludeAlbumsChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                filtering = galleryPreferences.filtering.copy(includeAlbums = it)
                            )
                        )
                    },
                    includeHidden = galleryPreferences.filtering.includeHidden,
                    onIncludeHiddenChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                filtering = galleryPreferences.filtering.copy(includeHidden = it)
                            )
                        )
                    },
                    includeUnhidden = galleryPreferences.filtering.includeUnHidden,
                    onIncludeUnhiddenChange = {
                        setNewPreferences(
                            galleryPreferences.copy(
                                filtering = galleryPreferences.filtering.copy(includeUnHidden = it)
                            )
                        )
                    },
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
            galleryPreferences = GalleryPreferences(),
            setNewPreferences = {}
        )
    }
}


@Composable
private fun AppearanceMenu(
    treeAlbumsEnabled: Boolean,
    onTreeAlbumsChange: (Boolean) -> Unit,
    theme: GalleryPreferences.Appearance.Theme,
    onThemeChange: (GalleryPreferences.Appearance.Theme) -> Unit,
    dynamicColors: GalleryPreferences.Appearance.DynamicColors,
    onDynamicColorsChange: (GalleryPreferences.Appearance.DynamicColors) -> Unit,
    gridPortraitColumnsAmount: Int,
    onGridPortraitColumnsAmountChange: (Int) -> Unit,
    gridLandscapeColumnsAmount: Int,
    onGridLandscapeColumnsAmountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        // verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        // albums mode
        Column {
            Text(
                text = stringResource(R.string.albums_mode),
                style = MaterialTheme.typography.titleSmall
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val selected = treeAlbumsEnabled
                    FilterChip(
                        selected = treeAlbumsEnabled,
                        onClick = { onTreeAlbumsChange(true) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selected) Icons.Default.Check else Icons.Default.AccountTree,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text(text = stringResource(R.string.tree_view)) }
                    )
                }
                item {
                    val selected = !treeAlbumsEnabled
                    FilterChip(
                        selected = selected,
                        onClick = { onTreeAlbumsChange(false) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selected) Icons.Default.Check else Icons.Default.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text(text = stringResource(R.string.plain_view)) }
                    )
                }
            }
        }
        
        // theme
        Column {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleSmall
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GalleryPreferences.Appearance.Theme.entries.forEach {
                    item {
                        val selected = theme == it
                        FilterChip(
                            selected = selected,
                            onClick = { onThemeChange(it) },
                            leadingIcon = {
                                if (selected) Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                else when (it) {
                                    GalleryPreferences.Appearance.Theme.SYSTEM -> Icon(
                                        painter = painterResource(
                                            R.drawable.routine_24dp_e3e3e3_fill0_wght400_grad0_opsz24
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    GalleryPreferences.Appearance.Theme.LIGHT -> Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    GalleryPreferences.Appearance.Theme.DARK -> Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            label = {
                                val text = when (it) {
                                    GalleryPreferences.Appearance.Theme.SYSTEM -> stringResource(
                                        R.string.system
                                    )
                                    GalleryPreferences.Appearance.Theme.LIGHT -> stringResource(
                                        R.string.light
                                    )
                                    GalleryPreferences.Appearance.Theme.DARK -> stringResource(R.string.dark)
                                }
                                Text(text)
                            }
                        )
                    }
                }
            }
        }
        
        // dynamic color scheme
        Column {
            Text(
                text = stringResource(R.string.dynamic_color_scheme),
                style = MaterialTheme.typography.titleSmall
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GalleryPreferences.Appearance.DynamicColors.entries.forEach {
                    item {
                        val selected = dynamicColors == it
                        FilterChip(
                            selected = selected,
                            onClick = { onDynamicColorsChange(it) },
                            leadingIcon = {
                                if (selected) Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                else {
                                    val icon = when (it) {
                                        GalleryPreferences.Appearance.DynamicColors.SYSTEM -> Icons.Default.Palette
                                        GalleryPreferences.Appearance.DynamicColors.DISABLED -> PaletteDisabled
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            label = {
                                val text = when (it) {
                                    GalleryPreferences.Appearance.DynamicColors.SYSTEM -> stringResource(
                                        R.string.system
                                    )
                                    GalleryPreferences.Appearance.DynamicColors.DISABLED -> stringResource(
                                        R.string.disabled
                                    )
                                }
                                Text(text)
                            }
                        )
                    }
                }
            }
        }
        
        // grid appearance
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.grid_appearance),
                style = MaterialTheme.typography.titleSmall
            )
            Column {
                val minColumns = remember { 2F }
                val maxColumns = remember { 7F }
                var value by remember { mutableFloatStateOf(gridPortraitColumnsAmount.toFloat()) }
                Text(
                    text = stringResource(R.string.grid_cols_portrait, value.toInt()),
                    style = MaterialTheme.typography.labelMedium
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
            Column {
                val minColumns = remember { 3F }
                val maxColumns = remember { 10F }
                var value by remember { mutableFloatStateOf(gridLandscapeColumnsAmount.toFloat()) }
                Text(
                    text = stringResource(R.string.grid_cols_portrait, value.toInt()),
                    style = MaterialTheme.typography.labelMedium
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
            treeAlbumsEnabled = true,
            onTreeAlbumsChange = {},
            theme = GalleryPreferences.Appearance.Theme.SYSTEM,
            onThemeChange = {},
            dynamicColors = GalleryPreferences.Appearance.DynamicColors.SYSTEM,
            onDynamicColorsChange = {},
            gridPortraitColumnsAmount = 3,
            onGridPortraitColumnsAmountChange = {},
            gridLandscapeColumnsAmount = 4,
            onGridLandscapeColumnsAmountChange = {},
        )
    }
}


@Composable
private fun SortingMenu(
    onTop: GalleryPreferences.Sorting.OnTop,
    onOnTopChange: (GalleryPreferences.Sorting.OnTop) -> Unit,
    sortOrder: GalleryPreferences.Sorting.Order,
    onSortOrderChange: (GalleryPreferences.Sorting.Order) -> Unit,
    descendSorting: Boolean,
    onDescendChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        LazyColumn {
            item {
                Text(
                    text = stringResource(R.string.place_on_top),
                    style = MaterialTheme.typography.titleSmall
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GalleryPreferences.Sorting.OnTop.entries.forEach {
                        item {
                            FilterChip(
                                selected = it == onTop,
                                onClick = { onOnTopChange(it) },
                                leadingIcon = {
                                    val icon = when (it) {
                                        GalleryPreferences.Sorting.OnTop.NONE -> Icons.Default.FilterNone
                                        GalleryPreferences.Sorting.OnTop.ALBUMS_ON_TOP -> Icons.Default.PermMedia
                                        GalleryPreferences.Sorting.OnTop.FILES_ON_TOP -> Icons.Default.Filter
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = {
                                    val text = when (it) {
                                        GalleryPreferences.Sorting.OnTop.NONE -> stringResource(
                                            R.string.disabled
                                        )
                                        GalleryPreferences.Sorting.OnTop.ALBUMS_ON_TOP -> stringResource(
                                            R.string.albums_on_top
                                        )
                                        GalleryPreferences.Sorting.OnTop.FILES_ON_TOP -> stringResource(
                                            R.string.files_on_top
                                        )
                                    }
                                    Text(text = text)
                                }
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.order),
                    style = MaterialTheme.typography.titleSmall
                )
                GalleryPreferences.Sorting.Order.entries.forEach { order ->
                    TextButton(
                        onClick = {
                            if (order == sortOrder) onDescendChange(descendSorting.not())
                            else onSortOrderChange(order)
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (order == sortOrder) {
                                val icon = when (order == GalleryPreferences.Sorting.Order.RANDOM) {
                                    true -> Icons.Default.Shuffle
                                    false -> if (descendSorting) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = ButtonDefaults.textButtonColors().contentColor
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            else Spacer(Modifier.width(24.dp))
                            val text = when (order) {
                                GalleryPreferences.Sorting.Order.CREATION_DATE -> stringResource(
                                    R.string.creation_date
                                )
                                GalleryPreferences.Sorting.Order.MODIFICATION_DATE -> stringResource(
                                    R.string.modification_date
                                )
                                GalleryPreferences.Sorting.Order.NAME -> stringResource(R.string.name)
                                GalleryPreferences.Sorting.Order.EXTENSION -> stringResource(
                                    R.string.extension
                                )
                                GalleryPreferences.Sorting.Order.SIZE -> stringResource(R.string.size)
                                GalleryPreferences.Sorting.Order.RANDOM -> stringResource(R.string.random)
                            }
                            Text(text)
                        }
                    }
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
            onTop = GalleryPreferences.Sorting.OnTop.ALBUMS_ON_TOP,
            onOnTopChange = {},
            onDescendChange = {},
            sortOrder = GalleryPreferences.Sorting.Order.MODIFICATION_DATE,
            onSortOrderChange = {},
            descendSorting = true,
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
    includeFiles: Boolean,
    onIncludeFilesChange: (Boolean) -> Unit,
    includeAlbums: Boolean,
    onIncludeAlbumsChange: (Boolean) -> Unit,
    includeHidden: Boolean,
    onIncludeHiddenChange: (Boolean) -> Unit,
    includeUnhidden: Boolean,
    onIncludeUnhiddenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Column {
            Text(
                text = stringResource(R.string.media_type),
                style = MaterialTheme.typography.titleSmall
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = includeImages,
                        onClick = { onIncludeImagesChange(!includeImages) },
                        label = { Text(stringResource(R.string.images)) },
                        leadingIcon = {
                            val icon = if (includeImages) Icons.Default.Check else Icons.Default.Image
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = includeVideos,
                        onClick = { onIncludeVideosChange(!includeVideos) },
                        label = { Text(stringResource(R.string.videos)) },
                        leadingIcon = {
                            val icon =
                                if (includeVideos) Icons.Default.Check else Icons.Default.VideoCameraBack
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = includeGifs,
                        onClick = { onIncludeGifsChange(!includeGifs) },
                        label = { Text(stringResource(R.string.gifs)) },
                        leadingIcon = {
                            val icon = if (includeGifs) Icons.Default.Check else Icons.Default.GifBox
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
        
        Column {
            Text(
                text = stringResource(R.string.item_type),
                style = MaterialTheme.typography.titleSmall
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = includeFiles,
                        onClick = { onIncludeFilesChange(!includeFiles) },
                        label = { Text(stringResource(R.string.file)) },
                        leadingIcon = {
                            val icon = if (includeFiles) Icons.Default.Check else Icons.Default.Filter
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = includeAlbums,
                        onClick = { onIncludeAlbumsChange(!includeAlbums) },
                        label = { Text(stringResource(R.string.album)) },
                        leadingIcon = {
                            val icon = if (includeAlbums) Icons.Default.Check else Icons.Default.PermMedia
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
        
        Column {
            Text(
                text = stringResource(R.string.item_type),
                style = MaterialTheme.typography.titleSmall
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = includeUnhidden,
                        onClick = { onIncludeUnhiddenChange(!includeUnhidden) },
                        label = { Text(stringResource(R.string.not_hidden)) },
                        leadingIcon = {
                            val icon = if (includeUnhidden) Icons.Default.Check else Icons.Default.Visibility
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = includeHidden,
                        onClick = { onIncludeHiddenChange(!includeHidden) },
                        label = { Text(stringResource(R.string.hidden)) },
                        leadingIcon = {
                            val icon = if (includeHidden) Icons.Default.Check else Icons.Default.VisibilityOff
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
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
            includeFiles = true,
            onIncludeFilesChange = {},
            includeAlbums = false,
            onIncludeAlbumsChange = {},
            includeHidden = false,
            onIncludeHiddenChange = {},
            includeUnhidden = true,
            onIncludeUnhiddenChange = {}
        )
    }
}
