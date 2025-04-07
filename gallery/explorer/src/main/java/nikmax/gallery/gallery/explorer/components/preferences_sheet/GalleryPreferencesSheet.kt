package nikmax.gallery.gallery.explorer.components.preferences_sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.explorer.components.drawables.PaletteDisabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GalleryPreferencesSheet(
    onShowSheetChange: (showSheet: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    vm: GalleryPreferencesSheetVm = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    
    LaunchedEffect(Unit) {
        vm.onAction(Action.Launch)
    }
    
    ModalBottomSheet(
        onDismissRequest = { onShowSheetChange(false) },
        dragHandle = null,
        modifier = modifier
    ) {
        GalleryPreferencesMenu(
            state = state,
            onAction = { vm.onAction(it) }
        )
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun GalleryPreferencesMenu(
    state: UiState,
    onAction: (Action) -> Unit
) {
    val tabs = Tab.entries
    val selectedTab = state.tab
    val selectedTabIndex = Tab.entries.indexOf(selectedTab)
    
    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = index == selectedTabIndex,
                    onClick = { onAction(Action.ChangeTab(tab)) },
                    text = {
                        val tabName = when (tab) {
                            Tab.SORTING -> stringResource(R.string.sorting)
                            Tab.APPEARANCE -> stringResource(R.string.appearance)
                            Tab.FILTERING -> stringResource(R.string.filtering)
                        }
                        Text(text = tabName)
                    }
                )
            }
        }
        AnimatedContent(
            targetState = state.tab,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(220, delayMillis = 90)
                ) togetherWith fadeOut(
                    animationSpec = tween(90)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) { currentTab ->
            when (currentTab) {
                Tab.APPEARANCE -> AppearanceMenu(
                    galleryMode = state.mode,
                    onGalleryModeChange = { treeEnabled -> onAction(Action.ChangeMode(treeEnabled)) },
                    theme = state.theme,
                    onThemeChange = { newTheme -> onAction(Action.ChangeTheme(newTheme)) },
                    dynamicColors = state.dynamicColors,
                    onDynamicColorsChange = { colors -> onAction(Action.ChangeDynamicColors(colors)) },
                    gridPortraitColumnsAmount = state.gridColumnsPortrait,
                    onGridPortraitColumnsAmountChange = { columns -> onAction(Action.ChangeGridColumnsPortrait(columns)) },
                    gridLandscapeColumnsAmount = state.gridColumnsLandscape,
                    onGridLandscapeColumnsAmountChange = { columns -> onAction(Action.ChangeGridColumnsLandscape(columns)) },
                )
                Tab.SORTING -> SortingMenu(
                    onTop = state.placeOnTop,
                    onOnTopChange = { placeOnTop -> onAction(Action.ChangePlaceOnTop(placeOnTop)) },
                    sortOrder = state.sortOrder,
                    onSortOrderChange = { sortOrder -> onAction(Action.ChangeSortOrder(sortOrder)) },
                    descendSorting = state.descendSorting,
                    onDescendChange = { descend -> onAction(Action.ChangeDescendSorting(descend)) },
                )
                Tab.FILTERING -> FilteringMenu(
                    includeImages = state.showImages,
                    onIncludeImagesChange = { enabled -> onAction(Action.ChangeIncludeImages(enabled)) },
                    includeVideos = state.showVideos,
                    onIncludeVideosChange = { enabled -> onAction(Action.ChangeIncludeVideos(enabled)) },
                    includeGifs = state.showGifs,
                    onIncludeGifsChange = { enabled -> onAction(Action.ChangeIncludeGifs(enabled)) },
                    includeFiles = state.showFiles,
                    onIncludeFilesChange = { enabled -> onAction(Action.ChangeIncludeFiles(enabled)) },
                    includeAlbums = state.showAlbums,
                    onIncludeAlbumsChange = { enabled -> onAction(Action.ChangeIncludeAlbums(enabled)) },
                    includeHidden = state.showHidden,
                    onIncludeHiddenChange = { enabled -> onAction(Action.ChangeIncludeHidden(enabled)) },
                    includeUnhidden = state.showUnHidden,
                    onIncludeUnhiddenChange = { enabled -> onAction(Action.ChangeIncludeUnHidden(enabled)) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SheetPreview() {
    GalleryTheme {
        GalleryPreferencesMenu(
            state = UiState(),
            onAction = {}
        )
    }
}


@Composable
private fun AppearanceMenu(
    galleryMode: GalleryMode,
    onGalleryModeChange: (GalleryMode) -> Unit,
    theme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    dynamicColors: DynamicColors,
    onDynamicColorsChange: (DynamicColors) -> Unit,
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
                GalleryMode.entries.forEach { mode ->
                    val selected = galleryMode == mode
                    item {
                        FilterChip(
                            selected = selected,
                            onClick = { onGalleryModeChange(mode) },
                            leadingIcon = {
                                val modeIcon = when (mode) {
                                    GalleryMode.TREE -> Icons.Default.AccountTree
                                    GalleryMode.PLAIN -> Icons.Default.GridView
                                }
                                Icon(
                                    imageVector = if (selected) Icons.Default.Check else modeIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = {
                                val modeName = when (mode) {
                                    GalleryMode.TREE -> stringResource(R.string.tree_view)
                                    GalleryMode.PLAIN -> stringResource(R.string.plain_view)
                                }
                                Text(text = modeName)
                            }
                        )
                    }
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
                AppTheme.entries.forEach {
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
                                    AppTheme.SYSTEM -> Icon(
                                        painter = painterResource(
                                            R.drawable.routine_24dp_e3e3e3_fill0_wght400_grad0_opsz24
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    AppTheme.LIGHT -> Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    AppTheme.DARK -> Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            label = {
                                val text = when (it) {
                                    AppTheme.SYSTEM -> stringResource(R.string.system)
                                    AppTheme.LIGHT -> stringResource(R.string.light)
                                    AppTheme.DARK -> stringResource(R.string.dark)
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
                DynamicColors.entries.forEach {
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
                                        DynamicColors.SYSTEM -> Icons.Default.Palette
                                        DynamicColors.DISABLED -> PaletteDisabled
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
                                    DynamicColors.SYSTEM -> stringResource(R.string.system)
                                    DynamicColors.DISABLED -> stringResource(R.string.disabled)
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
                    text = stringResource(R.string.grid_cols_landscape, value.toInt()),
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
            galleryMode = GalleryMode.TREE,
            onGalleryModeChange = {},
            theme = AppTheme.SYSTEM,
            onThemeChange = {},
            dynamicColors = DynamicColors.SYSTEM,
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
    onTop: PlaceOnTop,
    onOnTopChange: (PlaceOnTop) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
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
                    PlaceOnTop.entries.forEach {
                        item {
                            FilterChip(
                                selected = it == onTop,
                                onClick = { onOnTopChange(it) },
                                leadingIcon = {
                                    val icon = when (it) {
                                        PlaceOnTop.NONE -> Icons.Default.FilterNone
                                        PlaceOnTop.ALBUMS -> Icons.Default.PermMedia
                                        PlaceOnTop.FILES -> Icons.Default.Filter
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = {
                                    val text = when (it) {
                                        PlaceOnTop.NONE -> stringResource(R.string.disabled)
                                        PlaceOnTop.ALBUMS -> stringResource(R.string.albums_on_top)
                                        PlaceOnTop.FILES -> stringResource(R.string.files_on_top)
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
                SortOrder.entries.forEach { order ->
                    TextButton(
                        onClick = {
                            if (order == sortOrder) onDescendChange(descendSorting.not())
                            else onSortOrderChange(order)
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (order == sortOrder) {
                                val icon = when (order == SortOrder.RANDOM) {
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
                                SortOrder.DATE_CREATED -> stringResource(R.string.creation_date)
                                SortOrder.DATE_MODIFIED -> stringResource(R.string.modification_date)
                                SortOrder.NAME -> stringResource(R.string.name)
                                SortOrder.EXTENSION -> stringResource(R.string.extension)
                                SortOrder.SIZE -> stringResource(R.string.size)
                                SortOrder.RANDOM -> stringResource(R.string.random)
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
            onTop = PlaceOnTop.ALBUMS,
            onOnTopChange = {},
            onDescendChange = {},
            sortOrder = SortOrder.DATE_MODIFIED,
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
