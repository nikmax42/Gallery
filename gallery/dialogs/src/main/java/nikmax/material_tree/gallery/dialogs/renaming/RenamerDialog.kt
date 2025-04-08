package nikmax.material_tree.gallery.dialogs.renaming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import nikmax.mtree.core.ui.theme.GalleryTheme
import nikmax.mtree.gallery.core.ui.MediaItemUI
import nikmax.mtree.gallery.dialogs.R
import kotlin.io.path.Path
import kotlin.io.path.pathString


@Composable
fun RenamerDialog(
    mediaItem: MediaItemUI,
    onConfirm: (newPath: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember {
        val initialValue = when (mediaItem) {
            is MediaItemUI.File -> mediaItem.nameWithoutExtension
            is MediaItemUI.Album -> mediaItem.name
        }
        mutableStateOf(initialValue)
    }
    var extension by remember {
        val initialValue = when (mediaItem) {
            is MediaItemUI.File -> mediaItem.extension
            is MediaItemUI.Album -> null
        }
        mutableStateOf(initialValue)
    }
    val nameIsValid by remember(name) {
        mutableStateOf(RenamingUtils.fileNameIsValid(name))
    }
    val extensionIsValid by remember(extension) {
        val value = if (extension != null) RenamingUtils.fileExtensionIsValid(extension!!) else true
        mutableStateOf(value)
    }
    
    val focusRequester: FocusRequester = remember { FocusRequester() }
    
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties()
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.rename),
                    style = MaterialTheme.typography.headlineSmall,
                )
                
                Spacer(modifier = Modifier.size(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(id = R.string.new_name)) },
                    isError = nameIsValid.not(),
                    supportingText = {
                        if (nameIsValid.not()) {
                            Text(stringResource(R.string.invalid_filename))
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                if (extension != null) {
                    OutlinedTextField(
                        value = extension!!,
                        onValueChange = { extension = it },
                        label = { Text(text = stringResource(id = R.string.extension)) },
                        isError = extensionIsValid.not(),
                        supportingText = {
                            if (extensionIsValid.not()) {
                                Text(stringResource(R.string.invalid_extension))
                            }
                        },
                        singleLine = true,
                    )
                }
                
                Spacer(modifier = Modifier.size(24.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            val newPath =
                                "${
                                    Path(
                                        mediaItem.path
                                    ).parent.pathString
                                }/$name${if (extension != null) ".$extension" else ""}"
                            onConfirm(newPath)
                        },
                        enabled = nameIsValid && extensionIsValid
                    ) {
                        Text(text = stringResource(id = R.string.rename))
                    }
                }
            }
        }
    }
}


@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DialogPreview() {
    val item = MediaItemUI.File(
        path = "test/image.png",
        name = "image.png",
        size = 0,
        creationDate = 0,
        modificationDate = 0,
    )
    
    GalleryTheme {
        RenamerDialog(
            mediaItem = item,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
