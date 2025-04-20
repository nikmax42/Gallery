package mtree.viewer

import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.NewConflictResolutionDomain
import mtree.core.domain.usecases.RenameItemsUc

internal class FakeRenameItemsUc : RenameItemsUc {
    override suspend fun execute(
        items: List<MediaItemDomain>,
        onNewNameRequired: suspend (MediaItemDomain) -> String,
        onConflictResolutionRequired: suspend (MediaItemDomain) -> NewConflictResolutionDomain,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    ) {
        items.forEach { onNewNameRequired(it) }
        items.forEach { onConflictResolutionRequired(it) }
        onFilesystemOperationsStarted()
        onFilesystemOperationsFinished()
    }
}
