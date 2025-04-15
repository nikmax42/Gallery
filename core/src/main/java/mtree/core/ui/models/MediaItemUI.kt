package mtree.core.ui.models

import mtree.core.utils.MimetypeUtils
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isWritable
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

sealed interface MediaItemUI {
    enum class Volume { DEVICE, PLUGGABLE }
    
    val path: String
    
    val name: String
    
    val thumbnail: String?
    
    val size: Long
    
    val creationDate: Long
    
    val modificationDate: Long
    
    val hidden
        get() = path.contains("/.")
    
    val belongsToVolume: Volume
        get() = if (path.startsWith("/storage/emulated")) Volume.DEVICE
        else Volume.PLUGGABLE
    
    
    data class File(
        override val path: String,
        override val size: Long,
        override val creationDate: Long,
        override val modificationDate: Long,
        override val name: String,
        override val thumbnail: String?,
        val mimetype: String,
        val duration: Long,
        val uri: String?
    ) : MediaItemUI {
        enum class MediaType { IMAGE, VIDEO, GIF }
        
        val mediaType: MediaType
            get() = if (mimetype.startsWith("video/")) MediaType.VIDEO
            else if (mimetype == "image/gif") MediaType.GIF
            else MediaType.IMAGE
        
        val isVideoOrGif = mediaType == MediaType.VIDEO || mediaType == MediaType.GIF
        
        val extension: String
            get() = Path(path).extension
        
        val nameWithoutExtension: String
            get() = Path(path).nameWithoutExtension
        
        companion object {
            fun emptyFromPath(path: String) = File(
                path = path,
                size = 0,
                creationDate = 0,
                modificationDate = 0,
                name = Path(path).name,
                thumbnail = null,
                mimetype = MimetypeUtils.getFromPath(path).toString(),
                duration = 0,
                uri = "file://$path"
            )
        }
    }
    
    
    data class Album(
        override val path: String,
        override val name: String,
        override val size: Long,
        override val creationDate: Long,
        override val modificationDate: Long,
        override val thumbnail: String?,
        //in tree mode should also include files in nested albums
        val filesCount: Int,
        val imagesCount: Int,
        val videosCount: Int,
        val gifsCount: Int,
        val albumsCount: Int
    ) : MediaItemUI {
        val isVolume: Boolean
            get() {
                val volumesRegex = Regex("^/storage(/emulated)*(/[a-zA-Z0-9_-]+)?$")
                val isVolume = volumesRegex.containsMatchIn(path)
                val isNotWritable = Path(path).isWritable().not()
                return isVolume || isNotWritable
            }
        
        companion object {
            fun emptyFromPath(path: String) = Album(
                path = path,
                size = 0,
                creationDate = 0,
                modificationDate = 0,
                name = Path(path).name,
                thumbnail = null,
                filesCount = 0,
                imagesCount = 0,
                videosCount = 0,
                gifsCount = 0,
                albumsCount = 0,
            )
        }
    }
}
