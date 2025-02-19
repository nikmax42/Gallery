package nikmax.gallery.dialogs.conflict_resolver

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.media.ConflictResolution
import nikmax.gallery.dialogs.R
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.pathString


@Composable
fun ConflictResolverDialog(
    conflictItem: MediaItemUI,
    onResolve: (resolution: ConflictResolution, applyToAll: Boolean) -> Unit,
    onDismiss: () -> Unit,
    conflictsCount: Int = 1
) {
    var selectedResolution by remember { mutableStateOf(ConflictResolution.entries.first()) }
    var applyToAll by remember { mutableStateOf(false) }

    ContentWithInfo(
        conflictsCount = conflictsCount,
        conflictItemPath = conflictItem.path,
        conflictItemThumbnail = conflictItem.thumbnail,
        conflictItemCreationDate = conflictItem.dateCreated,
        conflictItemSize = conflictItem.size,
        selectedResolution = selectedResolution,
        onResolutionChange = { selectedResolution = it },
        applyToAll = applyToAll,
        onApplyToAllChanges = { applyToAll = applyToAll.not() },
        onConfirm = { onResolve(selectedResolution, applyToAll) },
        onDismiss = { onDismiss() }
    )
}


@Composable
private fun ContentWithInfo(
    conflictsCount: Int,
    conflictItemPath: String,
    conflictItemThumbnail: Any?,
    conflictItemCreationDate: Long,
    conflictItemSize: Long,
    selectedResolution: ConflictResolution,
    onResolutionChange: (ConflictResolution) -> Unit,
    applyToAll: Boolean,
    onApplyToAllChanges: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val itemName = Path(conflictItemPath).name
    val parentPath = Path(conflictItemPath).parent.pathString

    val supportingText = when (conflictsCount > 1) {
        true -> stringResource(R.string.x_files_already_exist, conflictsCount)
        false -> stringResource(R.string.file_already_exist)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null)

                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.filename_conflict),
                    style = MaterialTheme.typography.headlineSmall,
                )

                Spacer(Modifier.height(16.dp))
                Text(supportingText)

                Spacer(Modifier.height(16.dp))
                ItemInfo(
                    itemThumbnail = conflictItemThumbnail,
                    itemName = itemName,
                    parentAlbumPath = parentPath,
                    created = conflictItemCreationDate,
                    size = conflictItemSize
                )

                Spacer(Modifier.height(16.dp))
                RadioButtonsGroup(
                    selectedOption = selectedResolution,
                    onSelectionChange = { onResolutionChange(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                //"apply to all" checkbox
                // todo enable checkbox and add support to screens viewmodels
                /* Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onApplyToAllChanges(applyToAll.not()) }
                ) {
                    Checkbox(
                        checked = applyToAll,
                        onCheckedChange = { onApplyToAllChanges(it) },
                        modifier = Modifier.size(20.dp) // to remove default padding
                    )
                    Text(stringResource(R.string.apply_to_all))
                } */

                Spacer(Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = { onConfirm() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}
@Preview
@Composable
private fun ConflictResolverPreview() {
    var selectedResolution by remember { mutableStateOf(ConflictResolution.entries.first()) }
    var applyToAll by remember { mutableStateOf(false) }

    ContentWithInfo(
        conflictsCount = 2,
        conflictItemThumbnail = null,
        conflictItemPath = "/storage/emulated/0/album/filename.png",
        conflictItemCreationDate = 0,
        conflictItemSize = 2244,
        selectedResolution = selectedResolution,
        onResolutionChange = { selectedResolution = it },
        onDismiss = {},
        applyToAll = applyToAll,
        onApplyToAllChanges = { applyToAll = applyToAll.not() },
        onConfirm = {}
    )
}


@Composable
private fun RadioButtonsGroup(
    selectedOption: ConflictResolution,
    onSelectionChange: (ConflictResolution) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ConflictResolution.entries.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectionChange(option) }
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = { onSelectionChange(option) })
                val text = when (option) {
                    ConflictResolution.KEEP_BOTH -> stringResource(R.string.keep_both)
                    ConflictResolution.SKIP -> stringResource(R.string.skip)
                    ConflictResolution.OVERWRITE -> stringResource(R.string.overwrite)
                }
                Text(text = text)
            }
        }
    }
}


@Composable
private fun ItemInfo(
    itemThumbnail: Any?,
    itemName: String,
    parentAlbumPath: String,
    created: Long,
    size: Long,
    context: Context = LocalContext.current
) {
    val thumbnailRequest = ImageRequest.Builder(context)
        .data(itemThumbnail)
        .size(64, 64)
        .build()
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = thumbnailRequest,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(nikmax.gallery.core.ui.R.drawable.gallery_image_placeholder),
            error = painterResource(nikmax.gallery.core.ui.R.drawable.gallery_image_placeholder),
            contentDescription = itemName,
            clipToBounds = true,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        )
        Column {
            Text(
                text = itemName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = parentAlbumPath,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // todo converted to the human format
            // Text(created.toString())
            // Text(size.toString())
        }
    }
}
