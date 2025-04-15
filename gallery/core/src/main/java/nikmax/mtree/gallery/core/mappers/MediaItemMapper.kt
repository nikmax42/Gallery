package nikmax.mtree.gallery.core.mappers

import nikmax.mtree.gallery.core.data.media.MediaItemData
import nikmax.mtree.gallery.core.ui.models.MediaItemUI

object MediaItemMapper {
    
    fun List<MediaItemData>.mapToUi(): List<MediaItemUI> {
        return this.map { it.mapToUi() }
    }
    
    fun MediaItemData.mapToUi(): MediaItemUI {
        TODO("move to data model and remove from this")
        /* return when (this) {
            is MediaItemData.File -> MediaItemUI.File(
                path = this.path,
                size = this.size,
                duration = this.duration,
                creationDate = this.creationDate,
                modificationDate = this.modificationDate,
                thumbnail = this.path
            )
            is MediaItemData.Album -> MediaItemUI.Album(
                path = this.path,
                size = this.size,
                creationDate = this.creationDate,
                modificationDate = this.modificationDate,
                thumbnail = this.thumbnail,
                filesCount = this.imagesCount + this.videosCount + this.gifsCount,
                imagesCount = this.imagesCount,
                videosCount = this.videosCount,
                gifsCount = this.gifsCount,
                albumsCount = this.nestedAlbumsCount
            )
        } */
    }
    
}
