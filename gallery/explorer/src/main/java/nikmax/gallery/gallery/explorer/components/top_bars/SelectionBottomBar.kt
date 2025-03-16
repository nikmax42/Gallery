package nikmax.gallery.gallery.explorer.components.top_bars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nikmax.gallery.gallery.core.ui.MediaItemUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    items: List<MediaItemUI>,
    selectedItems: List<MediaItemUI>,
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
        items = items,
        selectedItems = selectedItems,
        onClearSelectionClick = { selectedItems.clear() },
        onSelectAllClick = { selectedItems.addAll(listOf(file1, file2, album1)) }
    )
}
