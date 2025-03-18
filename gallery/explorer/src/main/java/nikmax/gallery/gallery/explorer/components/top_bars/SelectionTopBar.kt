package nikmax.gallery.gallery.explorer.components.top_bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.MeasurementUnitsUtils.sizeToString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    items: List<MediaItemUI>,
    selectedItems: List<MediaItemUI>,
    onClearSelectionClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val affectedAlbumsCount = selectedItems.count { it is MediaItemUI.Album }
    // including files in selected albums
    val affectedFilesCount = selectedItems.count { it is MediaItemUI.File } +
            selectedItems.filterIsInstance<MediaItemUI.Album>().sumOf { it.filesCount }
    val affectedItemsSize: Long = selectedItems.sumOf { it.size }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${selectedItems.size}/${items.size}",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.albums, affectedAlbumsCount),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.files, affectedFilesCount),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = affectedItemsSize.sizeToString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
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
            creationDate = 0,
            modificationDate = 0,
            belongsToVolume = MediaItemUI.Volume.DEVICE,
            mimetype = "image/png"
        )
    }
    val file2 = remember {
        MediaItemUI.File(
            path = "/test.file2.gif",
            name = "file2.gif",
            size = 123456,
            creationDate = 0,
            modificationDate = 0,
            belongsToVolume = MediaItemUI.Volume.DEVICE,
            mimetype = "image/gif"
        )
    }
    val album1 = remember {
        MediaItemUI.Album(
            path = "/test.album/",
            name = "album",
            size = 0,
            creationDate = 0,
            modificationDate = 0,
            belongsToVolume = MediaItemUI.Volume.DEVICE,
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
