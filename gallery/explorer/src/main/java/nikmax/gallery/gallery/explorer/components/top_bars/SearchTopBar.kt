package nikmax.gallery.gallery.explorer.components.top_bars

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.explorer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchTopBar(
    searchQuery: String?,
    onQueryChange: (String?) -> Unit,
    onSearch: (query: String) -> Unit,
    actions: @Composable (() -> Unit) = {},
    albumName: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    focusManager: FocusManager = LocalFocusManager.current
) {
    fun cancelSearch() {
        focusManager.clearFocus()
        onQueryChange(null)
    }
    
    TopAppBar(
        navigationIcon = {
            Surface(
                shape = RoundedCornerShape(100F),
                color = SearchBarDefaults.colors().containerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( // to compensate hardcoded inner paddings
                        start = 4.dp,
                        end = 8.dp
                    )
            ) {
                SearchBarDefaults.InputField(
                    query = searchQuery ?: "",
                    onQueryChange = { onQueryChange(it) },
                    onSearch = { onSearch(it) },
                    expanded = false,
                    onExpandedChange = { },
                    placeholder = {
                        Text(
                            text = if (albumName != null) stringResource(R.string.search_in, albumName)
                            else stringResource(R.string.search_in_gallery),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = if (albumName != null) stringResource(R.string.search_in, albumName)
                            else stringResource(R.string.search_in_gallery)
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQuery == null,
                            enter = fadeIn() + slideInHorizontally { it },
                            exit = fadeOut() + slideOutHorizontally { it }
                        ) {
                            actions()
                        }
                        AnimatedVisibility(
                            visible = searchQuery != null,
                            enter = fadeIn() + slideInHorizontally { it },
                            exit = fadeOut() + slideOutHorizontally { it }
                        ) {
                            IconButton(onClick = { cancelSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_search_query),
                                )
                            }
                        }
                    },
                )
            }
        },
        title = { /* has inner hardcoded paddings incompatible with searchbar */ },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors().copy(
            containerColor = Color.Transparent
        ),
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SearchbarPreview() {
    var query by remember { mutableStateOf<String?>(null) }
    
    SearchTopBar(
        searchQuery = query,
        onQueryChange = { query = it },
        onSearch = {},
        albumName = "Album",
        actions = {
            AnimatedVisibility(
                visible = !query.isNullOrBlank(),
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
