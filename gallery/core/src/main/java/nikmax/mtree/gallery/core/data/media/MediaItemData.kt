package nikmax.mtree.gallery.core.data.media

import nikmax.mtree.gallery.core.domain.models.MediaItemDomain
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
        val files: List<File>,
    ) : MediaItemData
    
    
    fun mapToDomain(): MediaItemDomain {
        return when (this) {
            is File -> MediaItemDomain.File(
                path = path,
                creationDate = creationDate,
                modificationDate = modificationDate,
                size = size,
                thumbnail = thumbnail,
                mimetype = mimeType,
                duration = duration,
                uri = uri
            )
            is Album -> MediaItemDomain.Album(
                path = path,
                files = files.map { it.mapToDomain() as MediaItemDomain.File },
                size = files.sumOf { it.size },
                creationDate = files.minOfOrNull { it.creationDate } ?: 0,
                modificationDate = files.maxOfOrNull { it.modificationDate } ?: 0,
                thumbnail = files.firstOrNull()?.thumbnail,
                filesCount = files.size,
                imagesCount = files.count { it.mediaType == File.Type.IMAGE },
                videosCount = files.count { it.mediaType == File.Type.VIDEO },
                gifsCount = files.count { it.mediaType == File.Type.GIF },
                hiddenCount = files.count { it.isHidden },
                unhiddenCount = files.count { !it.isHidden },
                albumsCount = 0,
            )
        }
    }
}
