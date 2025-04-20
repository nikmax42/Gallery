package mtree.viewer

import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.usecases.DeleteItemsUc

internal class FakeDeleteItemsUc : DeleteItemsUc {
    override suspend fun execute(
        items: List<MediaItemDomain>,
        onConfirmationRequired: suspend () -> Boolean,
        onFilesystemOperationsStarted: () -> Unit,
        onFilesystemOperationsFinished: () -> Unit
    ) {
        onConfirmationRequired()
        onFilesystemOperationsStarted()
        onFilesystemOperationsFinished()
    }
}
