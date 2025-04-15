package nikmax.mtree.gallery.dialogs.deletion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import nikmax.mtree.core.ui.theme.GalleryTheme
import nikmax.mtree.gallery.core.ui.models.MediaItemUI
import nikmax.mtree.gallery.dialogs.R


@Composable
fun DeletionDialog(
    items: List<MediaItemUI>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties()
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.delete),
                    style = MaterialTheme.typography.headlineSmall,
                )
                
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.delete_x_items, items.size),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // todo add "move to trash bin" and "dont ask again" checkboxes
                /* Spacer(modifier = Modifier.size(16.dp))
                CheckBoxWithText(
                    checked = false,
                    onCheckedChange = {},
                    text = "Dont ask again"
                ) */
                
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    TextButton(onClick = { onConfirm() }) {
                        Text(text = stringResource(id = R.string.delete))
                    }
                }
            }
        }
    }
}
@Preview
@Composable
private fun DeletionDialogPreview() {
    val item = MediaItemUI.File(
        path = "test/image.png",
        size = 0,
        creationDate = 0,
        modificationDate = 0,
    )
    
    GalleryTheme {
        DeletionDialog(
            items = listOf(item),
            onConfirm = {},
            onDismiss = {}
        )
    }
}


@Composable
private fun CheckBoxWithText(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
        Text(text)
    }
}
@Preview
@Composable
private fun CheckBoxWithTextPreview() {
    CheckBoxWithText(
        checked = true,
        onCheckedChange = {},
        text = "Text"
    )
}
