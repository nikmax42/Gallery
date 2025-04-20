package mtree.viewer

import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.NewConflictResolutionDomain
import mtree.core.domain.usecases.CopyOrMoveItemsUc

internal class FakeCopyOrMoveItemsUc : CopyOrMoveItemsUc {
    override suspend fun execute(
        items: List<MediaItemDomain>,
        move: Boolean,
        onDestinationDirectoryRequired: suspend () -> String,
        onConflictResolutionRequired: suspend (MediaItemDomain) -> NewConflictResolutionDomain,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    ) {
        onDestinationDirectoryRequired()
        items.forEach { onConflictResolutionRequired(it) }
        onFilesystemOperationsStarted()
        onFilesystemOperationsFinished()
    }
}
