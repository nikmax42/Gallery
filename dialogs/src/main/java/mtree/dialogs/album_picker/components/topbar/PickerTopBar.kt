package mtree.dialogs.album_picker.components.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import mtree.core.ui.models.MediaItemUI
import mtree.dialogs.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PickerTopBar(
    currentAlbum: MediaItemUI.Album?,
    onConfirm: (MediaItemUI.Album) -> Unit,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { onDismiss() }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.cancel)
                )
            }
        },
        title = { Text(stringResource(R.string.pick_destination)) },
        actions = {
            val scope = rememberCoroutineScope()
            val albumIsNotSelectedYet = currentAlbum == null
            val albumIsNotWritable = currentAlbum?.isVolume == true
            val strDirectoryIsNotWritable = stringResource(R.string.directory_is_not_writable)
            val strPickDestination = stringResource(R.string.pick_destination_first)
            TextButton(
                onClick = {
                    scope.launch {
                        if (albumIsNotSelectedYet)
                            snackbarHostState.showSnackbar(strPickDestination)
                        else if (albumIsNotWritable)
                            snackbarHostState.showSnackbar(strDirectoryIsNotWritable)
                        else
                            onConfirm(currentAlbum)
                    }
                },
                colors = when (albumIsNotSelectedYet || albumIsNotWritable) {
                    true -> ButtonDefaults.textButtonColors().copy(
                        contentColor = ButtonDefaults.textButtonColors().disabledContentColor,
                        containerColor = ButtonDefaults.textButtonColors().disabledContainerColor,
                    )
                    false -> ButtonDefaults.textButtonColors()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
