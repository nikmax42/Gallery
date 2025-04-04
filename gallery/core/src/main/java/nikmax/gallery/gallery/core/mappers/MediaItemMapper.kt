package nikmax.gallery.gallery.core.mappers

import nikmax.gallery.gallery.core.data.media.MediaItemData
import nikmax.gallery.gallery.core.ui.MediaItemUI

object MediaItemMapper {
    
    fun List<MediaItemData>.mapToUi(): List<MediaItemUI> {
        return this.map { it.mapToUi() }
    }
    
    fun MediaItemData.mapToUi(): MediaItemUI {
        return when (this) {
            is MediaItemData.File -> MediaItemUI.File(
                path = this.path,
                size = this.size,
                duration = this.duration,
                creationDate = this.dateCreated,
                modificationDate = this.dateModified,
                belongsToVolume = when (this.storage) {
                    MediaItemData.Storage.DEVICE -> MediaItemUI.Volume.DEVICE
                    MediaItemData.Storage.PLUGGABLE -> MediaItemUI.Volume.PLUGGABLE
                },
                thumbnail = this.path
            )
            is MediaItemData.Album -> MediaItemUI.Album(
                path = this.path,
                size = this.size,
                creationDate = this.dateCreated,
                modificationDate = this.dateModified,
                belongsToVolume = when (this.storage) {
                    MediaItemData.Storage.DEVICE -> MediaItemUI.Volume.DEVICE
                    MediaItemData.Storage.PLUGGABLE -> MediaItemUI.Volume.PLUGGABLE
                },
                thumbnail = this.thumbnail,
                filesCount = this.imagesCount + this.videosCount + this.gifsCount,
                imagesCount = this.imagesCount,
                videosCount = this.videosCount,
                gifsCount = this.gifsCount,
                nestedAlbumsCountLInt = this.nestedDirectoriesCount
            )
        }
    }
    
}
