package mtree.explorer.components.top_bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mtree.explorer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchTopBar(
    albumName: String?,
    searchQuery: String?,
    onQueryChange: (String?) -> Unit,
    onFilterButtonClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    focusManager: FocusManager
) {
    fun cancelSearch() {
        focusManager.clearFocus()
        onQueryChange(null)
    }
    
    TopAppBar(
        title = {
            Surface(
                shape = RoundedCornerShape(100F),
                color = SearchBarDefaults.colors().containerColor,
                modifier = Modifier
                    .padding( // to compensate hardcoded inner paddings
                        start = 0.dp,
                        end = 8.dp
                    )
            ) {
                SearchBarDefaults.InputField(
                    query = searchQuery ?: "",
                    onQueryChange = { onQueryChange(it) },
                    onSearch = { },
                    expanded = false,
                    onExpandedChange = { },
                    placeholder = {
                        Text(
                            text = if (albumName != null) stringResource(R.string.search_in_directory, albumName)
                            else stringResource(R.string.search_in_gallery),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = if (albumName != null) stringResource(R.string.search_in_directory, albumName)
                            else stringResource(R.string.search_in_gallery)
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQuery != null,
                            enter = fadeIn() + slideInHorizontally { it },
                            exit = fadeOut() + slideOutHorizontally { it }
                        ) {
                            IconButton(onClick = { cancelSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.cancel_search),
                                )
                            }
                        }
                    },
                )
            }
        },
        actions = {
            IconButton(onClick = { onFilterButtonClick() }) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    stringResource(R.string.gallery_preferences)
                )
            }
        },
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
        onFilterButtonClick = {},
        albumName = "Album",
        focusManager = LocalFocusManager.current,
        scrollBehavior = null
    )
}
