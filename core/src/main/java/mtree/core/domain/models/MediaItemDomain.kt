package mtree.core.domain.models

import mtree.core.data.MediaItemData
import mtree.core.ui.models.MediaItemUI
import mtree.core.utils.MimetypeUtils
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.name

sealed interface MediaItemDomain {
    enum class Volume { DEVICE, PLUGGABLE }
    
    val path: String
    
    val name: String
        get() = Path(path).name
    
    val thumbnailPath: String?
    
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
        override val thumbnailPath: String?,
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
                    thumbnailPath = null,
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
        override val thumbnailPath: String?,
        //in tree mode should also include files in nested albums
        val ownFiles: List<File>,
        val nestedFilesCount: Int,
        val nestedImagesCount: Int,
        val nestedVideosCount: Int,
        val nestedGifsCount: Int,
        val nestedHiddenMediaCount: Int,
        val nestedUnhiddenMediaCount: Int,
        val nestedAlbumsCount: Int
    ) : MediaItemDomain
    
    
    fun mapToUi(): MediaItemUI {
        return when (this) {
            is File -> MediaItemUI.File(
                path = path,
                creationDate = creationDate,
                modificationDate = modificationDate,
                size = size,
                thumbnail = thumbnailPath,
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
                thumbnail = thumbnailPath,
                filesCount = nestedFilesCount,
                imagesCount = nestedImagesCount,
                videosCount = nestedVideosCount,
                gifsCount = nestedGifsCount,
                albumsCount = nestedAlbumsCount,
                files = ownFiles.map { it.mapToUi() as MediaItemUI.File },
                unHiddenCount = nestedUnhiddenMediaCount,
                hiddenCount = nestedHiddenMediaCount,
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
                thumbnail = thumbnailPath,
                mimeType = mimetype,
                duration = duration,
                uri = uri
            )
            is Album -> MediaItemData.Album(
                path = path,
                ownFiles = ownFiles.map { it.mapToData() as MediaItemData.File },
                nestedMediaSize = size,
                nestedImagesCount = nestedImagesCount,
                nestedVideosCount = nestedVideosCount,
                nestedGifsCount = nestedGifsCount,
                nestedHiddenMediaCount = nestedHiddenMediaCount,
                nestedUnhiddenMediaCount = nestedUnhiddenMediaCount,
                nestedAlbumsCount = nestedAlbumsCount,
                creationDate = creationDate,
                modificationDate = modificationDate,
                thumbnailPath = thumbnailPath,
            )
        }
    }
}
