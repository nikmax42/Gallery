package nikmax.gallery.gallery.explorer.components.bottom_bars

import android.annotation.SuppressLint
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.core.ui.MediaItemUI

@Composable
fun SelectionBottomBar(
    selectedItems: List<MediaItemUI>,
    onCopy: (List<MediaItemUI>) -> Unit,
    onMove: (List<MediaItemUI>) -> Unit,
    onRename: (List<MediaItemUI>) -> Unit,
    onDelete: (List<MediaItemUI>) -> Unit,
    onShare: (MediaItemUI.File) -> Unit,
    onUnavailableItemsUnselection: (selectionWithoutProtectedItems: List<MediaItemUI>) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val thereIsVolumesSelected = selectedItems.any { it is MediaItemUI.Album && it.isVolume }
    val buttonsColors = when (thereIsVolumesSelected) {
        true -> IconButtonDefaults.iconButtonColors().copy(
            containerColor = IconButtonDefaults.iconButtonColors().disabledContainerColor,
            contentColor = IconButtonDefaults.iconButtonColors().disabledContentColor
        )
        false -> IconButtonDefaults.iconButtonColors()
    }
    val strSnackbarText = stringResource(R.string.protected_items_warning)
    val strSnackbarActionText = stringResource(R.string.unselect)

    fun showErrorSnackbar() {
        scope.launch {
            when (
                snackbarHostState.showSnackbar(
                    message = strSnackbarText,
                    actionLabel = strSnackbarActionText
                )
            ) {
                SnackbarResult.Dismissed -> {}
                SnackbarResult.ActionPerformed -> {
                    val newSelection = selectedItems.minus(
                        selectedItems.filter { it is MediaItemUI.Album && it.isVolume }
                    )
                    onUnavailableItemsUnselection(newSelection)
                }
            }
        }
    }

    BottomAppBar(
        actions = {
            IconButton(
                colors = buttonsColors,
                onClick = {
                    if (thereIsVolumesSelected) showErrorSnackbar()
                    else onCopy(selectedItems)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_selected)
                )
            }
            IconButton(
                colors = buttonsColors,
                onClick = {
                    if (thereIsVolumesSelected) showErrorSnackbar()
                    else onMove(selectedItems)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = stringResource(R.string.move_selected)
                )
            }
            IconButton(
                colors = buttonsColors,
                onClick = {
                    if (thereIsVolumesSelected) showErrorSnackbar()
                    else onRename(selectedItems)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.rename_selected)
                )
            }
            IconButton(
                colors = buttonsColors,
                onClick = {
                    if (thereIsVolumesSelected) showErrorSnackbar()
                    else onDelete(selectedItems)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_selected)
                )
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
        onCopy = {},
        onMove = {},
        onRename = {},
        onDelete = {},
        onShare = {},
        onUnavailableItemsUnselection = {},
        snackbarHostState = SnackbarHostState()
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
private fun LockedSelectionBottomBarPreview() {
    val selectedItems = remember {
        listOf<MediaItemUI>(
            MediaItemUI.Album("/storage")
        )
    }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = {
            SelectionBottomBar(
                selectedItems = selectedItems,
                onCopy = {},
                onMove = {},
                onRename = {},
                onDelete = {},
                onShare = {},
                onUnavailableItemsUnselection = {},
                snackbarHostState = snackbarHostState
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddings -> }
}
