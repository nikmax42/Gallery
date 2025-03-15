package nikmax.gallery.gallery.explorer.components.bottom_bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.explorer.R

@Composable
fun SelectionBottomBar(
    selectedItems: List<MediaItemUI>,
    onCopyClick: (List<MediaItemUI>) -> Unit,
    onMoveClick: (List<MediaItemUI>) -> Unit,
    onRenameClick: (List<MediaItemUI>) -> Unit,
    onDeleteClick: (List<MediaItemUI>) -> Unit,
    onShare: (MediaItemUI.File) -> Unit
) {
    BottomAppBar(
        actions = {
            IconButton(
                enabled = selectedItems.none { it.protected },
                onClick = { onCopyClick(selectedItems) }) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copy_selected))
            }
            IconButton(
                enabled = selectedItems.none { it.protected },
                onClick = { onMoveClick(selectedItems) }) {
                Icon(imageVector = Icons.Default.ContentCut, contentDescription = stringResource(R.string.move_selected))
            }
            IconButton(
                enabled = selectedItems.none { it.protected },
                onClick = { onRenameClick(selectedItems) }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.rename_selected))
            }
            IconButton(
                enabled = selectedItems.none { it.protected },
                onClick = { onDeleteClick(selectedItems) }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete_selected))
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedItems.size == 1 && selectedItems.first() is MediaItemUI.File,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(onClick = { onShare(selectedItems.first() as MediaItemUI.File) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share)
                    )
                }
            }
        }
    )
}
@Preview
@Composable
private fun SelectionBottomBarPreview() {
    val selectedItems = remember {
        listOf<MediaItemUI>(
            MediaItemUI.File("/test.jpg")
        )
    }

    SelectionBottomBar(
        selectedItems = selectedItems,
        onCopyClick = {},
        onMoveClick = {},
        onRenameClick = {},
        onDeleteClick = {},
        onShare = {}
    )
}
