package mtree.core.domain.usecases

import android.content.Context
import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.NewConflictResolutionDomain
import mtree.core.domain.models.NewFileOperation
import mtree.core.workers.FileOperationWorker
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

interface RenameItemsUc {
    suspend fun execute(
        items: List<MediaItemDomain>,
        onNewNameRequired: suspend (MediaItemDomain) -> String,
        onConflictResolutionRequired: suspend (item: MediaItemDomain) -> NewConflictResolutionDomain,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    )
}


internal class RenameItemsUcImpl(private val context: Context) : RenameItemsUc {
    override suspend fun execute(
        items: List<MediaItemDomain>,
        onNewNameRequired: suspend (MediaItemDomain) -> String,
        onConflictResolutionRequired: suspend (item: MediaItemDomain) -> NewConflictResolutionDomain,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    ) {
        var conflictResolutionForAll: NewConflictResolutionDomain? = null
        val operations = items.map { item ->
            val newPath = onNewNameRequired(item).let { newName ->
                val directoryPath = Path(item.path).parent.pathString
                "$directoryPath/$newName"
            }
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
                false -> NewConflictResolutionDomain.default()
            }
            NewFileOperation.Rename(
                originalFilePath = item.path,
                newFilePath = newPath,
                conflictResolution = conflictResolution
            )
        }
        onFilesystemOperationsStarted()
        FileOperationWorker.performFileOperationsInBackground(
            operations = operations,
            context = context,
            onProgressChanged = { succeeded, failed ->
                /* todo observe progress here */
            },
            onFinished = { onFilesystemOperationsFinished() }
        )
    }
}
