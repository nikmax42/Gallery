package mtree.core.data

import mtree.core.domain.models.MediaItemDomain
import kotlin.io.path.Path
import kotlin.io.path.extension

sealed interface MediaItemData {
    val path: String
    
    
    data class File(
        override val path: String,
        val size: Long,
        val creationDate: Long,
        val modificationDate: Long,
        val thumbnail: String?,
        val mimeType: String,
        val duration: Long,
        val uri: String,
    ) : MediaItemData {
        enum class Type { IMAGE, VIDEO, GIF }
        
        val extension = Path(path).extension
        val mediaType =
            if (mimeType == "image/gif") Type.GIF
            else if (mimeType.startsWith("video/")) Type.VIDEO
            else Type.IMAGE
        val isHidden = path.contains("/.")
    }
    
    
    data class Album(
        override val path: String,
        val ownFiles: List<File>,
        val nestedMediaSize: Long,
        val nestedImagesCount: Int,
        val nestedVideosCount: Int,
        val nestedGifsCount: Int,
        val nestedHiddenMediaCount: Int,
        val nestedUnhiddenMediaCount: Int,
        val nestedAlbumsCount: Int,
        val creationDate: Long,
        val modificationDate: Long,
        val thumbnailPath: String?
    ) : MediaItemData {
        companion object {
            fun createEmpty(path: String, files: List<File>) = Album(
                path = path,
                ownFiles = files,
                nestedMediaSize = 0,
                nestedImagesCount = 0,
                nestedVideosCount = 0,
                nestedGifsCount = 0,
                nestedHiddenMediaCount = 0,
                nestedUnhiddenMediaCount = 0,
                nestedAlbumsCount = 0,
                creationDate = 0,
                modificationDate = 0,
                thumbnailPath = null
            )
        }
    }
    
    
    fun mapToDomain(): MediaItemDomain {
        return when (this) {
            is File -> MediaItemDomain.File(
                path = path,
                creationDate = creationDate,
                modificationDate = modificationDate,
                size = size,
                thumbnailPath = thumbnail,
                mimetype = mimeType,
                duration = duration,
                uri = uri
            )
            is Album -> MediaItemDomain.Album(
                path = path,
                ownFiles = ownFiles.map { it.mapToDomain() as MediaItemDomain.File },
                size = nestedMediaSize,
                creationDate = creationDate,
                modificationDate = modificationDate,
                thumbnailPath = thumbnailPath,
                nestedFilesCount = nestedImagesCount + nestedVideosCount + nestedGifsCount,
                nestedImagesCount = nestedImagesCount,
                nestedVideosCount = nestedVideosCount,
                nestedGifsCount = nestedGifsCount,
                nestedHiddenMediaCount = nestedHiddenMediaCount,
                nestedUnhiddenMediaCount = nestedUnhiddenMediaCount,
                nestedAlbumsCount = nestedAlbumsCount,
            )
        }
    }
}
