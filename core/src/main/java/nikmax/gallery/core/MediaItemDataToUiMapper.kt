package nikmax.gallery.core

import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.media.MediaFileData
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.pathString

object MediaItemDataToUiMapper {
    fun mapToFile(mediaItemData: MediaFileData): MediaItemUI.File {
        return MediaItemUI.File(
            path = mediaItemData.path,
            name = mediaItemData.name,
            size = mediaItemData.size,
            dateCreated = mediaItemData.dateCreated,
            dateModified = mediaItemData.dateModified,
            volume = when (mediaItemData.volume) {
                MediaFileData.Volume.PRIMARY -> MediaItemUI.Volume.PRIMARY
                MediaFileData.Volume.SECONDARY -> MediaItemUI.Volume.SECONDARY
            },
            mimetype = mediaItemData.mimetype ?: "null/null",
            thumbnail = mediaItemData.path
        )
    }

    fun mapToAlbum(albumFiles: List<MediaFileData>): MediaItemUI.Album {
        val albumPath = Path(albumFiles.first().path).parent
        val albumSize = albumFiles.sumOf { it.size }
        val albumCreated = albumFiles.minOf { it.dateCreated }
        val albumModified = albumFiles.maxOf { it.dateModified }
        val albumThumbnail = albumFiles.minBy { it.dateCreated }.path
        return MediaItemUI.Album(
            path = albumPath.pathString,
            name = albumPath.name,
            size = albumSize,
            dateCreated = albumCreated,
            dateModified = albumModified,
            volume = when (albumFiles.first().volume) {
                MediaFileData.Volume.PRIMARY -> MediaItemUI.Volume.PRIMARY
                MediaFileData.Volume.SECONDARY -> MediaItemUI.Volume.SECONDARY
            },
            thumbnail = albumThumbnail,
            filesCount = albumFiles.size,
            imagesCount = albumFiles.count { (it.mimetype?.startsWith("image/") == true && it.mimetype != "image/gif") },
            videosCount = albumFiles.count { it.mimetype?.startsWith("video/") == true },
            gifsCount = albumFiles.count { it.mimetype?.startsWith("image/gif") == true }
        )
    }
}
