package mtree.core.domain.usecases

import android.content.Context
import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.NewFileOperation
import mtree.core.workers.FileOperationWorker

interface DeleteItemsUc {
    suspend fun execute(
        items: List<MediaItemDomain>,
        onConfirmationRequired: suspend () -> Boolean,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    )
}


class DeleteItemsUcImpl(private val context: Context) : DeleteItemsUc {
    override suspend fun execute(
        items: List<MediaItemDomain>,
        onConfirmationRequired: suspend () -> Boolean,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    ) {
        onConfirmationRequired().let { deletionConfirmed ->
            if (deletionConfirmed) {
                val operations = items.map {
                    NewFileOperation.Delete(it.path)
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
    }
}
