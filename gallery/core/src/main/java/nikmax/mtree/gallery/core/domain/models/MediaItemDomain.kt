package nikmax.mtree.gallery.core.domain.models

import nikmax.mtree.gallery.core.data.media.MediaItemData
import nikmax.mtree.gallery.core.ui.models.MediaItemUI
import nikmax.mtree.gallery.core.utils.MimetypeUtils
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.name

sealed interface MediaItemDomain {
    enum class Volume { DEVICE, PLUGGABLE }
    
    val path: String
    
    val name: String
        get() = Path(path).name
    
    val thumbnail: String?
    
    val size: Long
    
    val creationDate: Long
    
    val modificationDate: Long
    
    val isHidden
        get() = path.contains("/.")
    
    val belongsToVolume: Volume
        get() = if (path.startsWith("/storage/emulated")) Volume.DEVICE
        else Volume.PLUGGABLE
    
    
    data class File(
        override val path: String,
        override val size: Long,
        override val creationDate: Long,
        override val modificationDate: Long,
        override val thumbnail: String?,
        val mimetype: String,
        val duration: Long,
        val uri: String
    ) : MediaItemDomain {
        enum class MediaType { IMAGE, VIDEO, GIF }
        
        val mediaType: MediaType
            get() = if (mimetype.startsWith("video/")) MediaType.VIDEO
            else if (mimetype == "image/gif") MediaType.GIF
            else MediaType.IMAGE
        
        val extension: String
            get() = Path(path).extension
        
        companion object {
            fun createEmptyFromPath(path: String) {
                File(
                    path = path,
                    size = 0,
                    creationDate = 0,
                    modificationDate = 0,
                    thumbnail = null,
                    mimetype = MimetypeUtils.getFromPath(path).toString(),
                    duration = 0,
                    uri = "file//:$path"
                )
            }
        }
    }
    
    
    data class Album(
        override val path: String,
        override val size: Long,
        override val creationDate: Long,
        override val modificationDate: Long,
        override val thumbnail: String?,
        //in tree mode should also include files in nested albums
        val files: List<File>,
        val filesCount: Int,
        val imagesCount: Int,
        val videosCount: Int,
        val gifsCount: Int,
        val hiddenCount: Int,
        val unhiddenCount: Int,
        val albumsCount: Int
    ) : MediaItemDomain
    
    
    fun mapToUi(): MediaItemUI {
        return when (this) {
            is File -> MediaItemUI.File(
                path = path,
                name = name,
                creationDate = creationDate,
                modificationDate = modificationDate,
                size = size,
                thumbnail = thumbnail,
                mimetype = mimetype,
                duration = duration,
                uri = uri
            )
            is Album -> MediaItemUI.Album(
                path = path,
                name = name,
                creationDate = creationDate,
                modificationDate = modificationDate,
                size = size,
                thumbnail = thumbnail,
                filesCount = filesCount,
                imagesCount = imagesCount,
                videosCount = videosCount,
                gifsCount = gifsCount,
                albumsCount = albumsCount
            )
        }
    }
    
    
    fun mapToData(): MediaItemData {
        return when (this) {
            is File -> MediaItemData.File(
                path = path,
                creationDate = creationDate,
                modificationDate = modificationDate,
                size = size,
                thumbnail = thumbnail,
                mimeType = mimetype,
                duration = duration,
                uri = uri
            )
            is Album -> MediaItemData.Album(
                path = path,
                files = files.map { it.mapToData() as MediaItemData.File },
            )
        }
    }
}
