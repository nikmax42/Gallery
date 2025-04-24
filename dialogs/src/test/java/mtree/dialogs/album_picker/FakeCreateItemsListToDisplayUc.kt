package mtree.dialogs.album_picker

import mtree.core.domain.models.Filters
import mtree.core.domain.models.GalleryMode
import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.Sort
import mtree.core.domain.usecases.CreateItemsListToDisplayUc

internal class FakeCreateItemsListToDisplayUc : CreateItemsListToDisplayUc {
    override fun execute(
        galleryAlbums: List<MediaItemDomain.Album>,
        basePath: String?,
        searchQuery: String?,
        galleryMode: GalleryMode,
        filters: Filters,
        sort: Sort
    ): List<MediaItemDomain> {
        return galleryAlbums
    }
}
