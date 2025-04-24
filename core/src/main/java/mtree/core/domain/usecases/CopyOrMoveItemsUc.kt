package mtree.core.domain.usecases

import android.content.Context
import mtree.core.domain.models.ConflictResolutionDomain
import mtree.core.domain.models.FileOperation
import mtree.core.domain.models.MediaItemDomain
import mtree.core.workers.FileOperationWorker
import kotlin.io.path.Path
import kotlin.io.path.exists


interface CopyOrMoveItemsUc {
    suspend fun execute(
        items: List<MediaItemDomain>,
        move: Boolean = false,
        onDestinationDirectoryRequired: suspend () -> String,
        onConflictResolutionRequired: suspend (item: MediaItemDomain) -> ConflictResolutionDomain,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    )
}


internal class CopyOrMoveItemsUcImpl(private val context: Context) : CopyOrMoveItemsUc {
    override suspend fun execute(
        items: List<MediaItemDomain>,
        move: Boolean,
        onDestinationDirectoryRequired: suspend () -> String,
        onConflictResolutionRequired: suspend (MediaItemDomain) -> ConflictResolutionDomain,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    ) {
        val destinationDirectory = onDestinationDirectoryRequired()
        var conflictResolutionForAll: ConflictResolutionDomain? = null
        items.map { item ->
            val newPath = "$destinationDirectory/${item.name}"
            //if target file already exists:
            // if resolution with "apply to all" checkbox is not set yet - show dialog
            // else use "applied to all" resolution
            val conflictResolution = when (Path(newPath).exists()) {
                true -> when (conflictResolutionForAll == null) {
                    true -> onConflictResolutionRequired(item).let { resolution ->
                        if (resolution.applyToAll) conflictResolutionForAll = resolution
                        resolution
                    }
                    false -> conflictResolutionForAll!!
                }
                false -> ConflictResolutionDomain.keepBoth()
            }
            when (move) {
                true -> FileOperation.Move(
                    sourceFilePath = item.path,
                    destinationFilePath = newPath,
                    conflictResolution = conflictResolution
                )
                false -> FileOperation.Copy(
                    sourceFilePath = item.path,
                    destinationFilePath = newPath,
                    conflictResolution = conflictResolution
                )
            }
        }.let { fileOperations ->
            onFilesystemOperationsStarted()
            FileOperationWorker.performFileOperationsInBackground(
                operations = fileOperations,
                context = context,
                onProgressChanged = { succeeded, failed -> /* todo observe progress here */ },
                onFinished = { onFilesystemOperationsFinished() }
            )
        }
    }
}
