package nikmax.gallery.viewer.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nikmax.gallery.viewer.R


@Composable
fun ViewerBottomBar(
    onCopyClick: () -> Unit,
    onMoveClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = { onCopyClick() }) {
                Icon(imageVector = Icons.Default.CopyAll, contentDescription = stringResource(R.string.copy))
            }
            IconButton(onClick = { onMoveClick() }) {
                Icon(imageVector = Icons.Default.ContentCut, contentDescription = stringResource(R.string.move))
            }
            IconButton(onClick = { onRenameClick() }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.rename))
            }
            IconButton(onClick = { onDeleteClick() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onShareClick() }) {
                IconButton(onClick = { onDeleteClick() }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share))
                }
            }
        },
    )
}
@Preview
@Composable
private fun ViewerBottomBarPreview() {
    ViewerBottomBar(
        onCopyClick = {},
        onMoveClick = {},
        onRenameClick = {},
        onDeleteClick = {},
        onShareClick = {}
    )
}
