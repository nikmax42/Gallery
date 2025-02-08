package nikmax.gallery.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

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
