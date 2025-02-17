package nikmax.gallery.explorer.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.R
import nikmax.gallery.core.ui.MediaItemUI


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchTopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: (query: String) -> Unit,
    trailingIcon: @Composable (() -> Unit) = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val focusManager = LocalFocusManager.current // to clear focus on search cancel

    fun clearQueryAndFocus() {
        focusManager.clearFocus()
        onQueryChange("")
    }

    // Cancel search with back press
    BackHandler(searchQuery.isNotEmpty()) { clearQueryAndFocus() }

    TopAppBar(
        navigationIcon = {
            Searchbar(
                query = searchQuery,
                onQueryChange = { onQueryChange(it) },
                onSearch = { onSearch(it) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_placeholder)
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = slideInHorizontally { it } + fadeIn(),
                        exit = slideOutHorizontally { it } + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "",
                            modifier = Modifier.clickable { clearQueryAndFocus() }
                        )
                    }
                    AnimatedVisibility(
                        visible = searchQuery.isEmpty(),
                        enter = slideInHorizontally { it } + fadeIn(),
                        exit = slideOutHorizontally { it } + fadeOut()
                    ) {
                        trailingIcon()
                    }
                },
                placeholder = { Text(text = stringResource(R.string.search_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( // to compensate hardcoded inner paddings
                        start = 4.dp,
                        end = 8.dp
                    )
            )
        },
        title = { /* has inner hardcoded paddings incompatible with searchbar */ },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Searchbar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (query: String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
) {
    Surface(
        shape = RoundedCornerShape(100F),
        color = SearchBarDefaults.colors().containerColor,
        modifier = modifier
    ) {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = { onQueryChange(it) },
            onSearch = { onSearch(it) },
            expanded = expanded,
            onExpandedChange = { onExpandedChange(it) },
            leadingIcon = leadingIcon,
            placeholder = placeholder,
            trailingIcon = trailingIcon,
        )
    }
}
@Preview
@Composable
private fun SearchbarPreview() {
    var query by remember { mutableStateOf("") }

    Searchbar(
        query = query,
        onQueryChange = { query = it },
        onSearch = {},
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = ""
            )
        },
        placeholder = {
            AnimatedVisibility(
                visible = query.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) { Text(text = "Placeholder text") }
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "",
                    modifier = Modifier.clickable { query = "" }
                )
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    items: Set<MediaItemUI>,
    selectedItems: Set<MediaItemUI>,
    onClearSelectionClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text("${selectedItems.size}/${items.size}")
        },
        navigationIcon = {
            IconButton(onClick = { onClearSelectionClick() }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = { onSelectAllClick() }) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = null
                )
            }
        },
        modifier = modifier
    )
}
@Preview
@Composable
private fun SelectionTopBarPreview() {
    val file1 = remember {
        MediaItemUI.File(
            path = "/test.file.png",
            name = "file.png",
            size = 3253636,
            dateCreated = 0,
            dateModified = 0,
            volume = MediaItemUI.Volume.PRIMARY,
            mimetype = "image/png"
        )
    }
    val file2 = remember {
        MediaItemUI.File(
            path = "/test.file2.gif",
            name = "file2.gif",
            size = 123456,
            dateCreated = 0,
            dateModified = 0,
            volume = MediaItemUI.Volume.PRIMARY,
            mimetype = "image/gif"
        )
    }
    val album1 = remember {
        MediaItemUI.Album(
            path = "/test.album/",
            name = "album",
            size = 0,
            dateCreated = 0,
            dateModified = 0,
            volume = MediaItemUI.Volume.PRIMARY,
            filesCount = 3
        )
    }
    val items = remember {
        mutableStateListOf(file1, file2, album1)
    }
    val selectedItems = remember {
        mutableStateListOf(file1, album1)
    }

    SelectionTopBar(
        items = items.toSet(),
        selectedItems = selectedItems.toSet(),
        onClearSelectionClick = { selectedItems.clear() },
        onSelectAllClick = { selectedItems.addAll(listOf(file1, file2, album1)) }
    )
}
