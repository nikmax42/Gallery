package nikmax.gallery.explorer.ui.components.bottom_bars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nikmax.gallery.explorer.R

@Composable
fun SelectionBottomBar(
    onCopyClick: () -> Unit,
    onMoveClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = { onCopyClick() }) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copy_selected))
            }
            IconButton(onClick = { onMoveClick() }) {
                Icon(imageVector = Icons.Default.ContentCut, contentDescription = stringResource(R.string.move_selected))
            }
            IconButton(onClick = { onRenameClick() }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.rename_selected))
            }
            IconButton(onClick = { onDeleteClick() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete_selected))
            }
        }
    )
}
@Preview
@Composable
private fun SelectionBottomBarPreview() {
    SelectionBottomBar(
        onCopyClick = {},
        onMoveClick = {},
        onRenameClick = {},
        onDeleteClick = {}
    )
}
