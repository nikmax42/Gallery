package nikmax.mtree.gallery.core.ui

import android.webkit.MimeTypeMap
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
        override val size: Long = 0,
        override val creationDate: Long = 0,
        override val modificationDate: Long = 0,
        override val name: String = Path(path).name,
        override val thumbnail: String? = null,
        val duration: Long = 0,
        val uri: String? = null
    ) : MediaItemUI {
        enum class MediaType { IMAGE, VIDEO, GIF }
        
        val mimetype: String = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
            .toString()
        val mediaType: MediaType
            get() = if (mimetype.startsWith("video/")) MediaType.VIDEO
            else if (mimetype == "image/gif") MediaType.GIF
            else MediaType.IMAGE
        val isVideoOrGif = mediaType == MediaType.VIDEO || mediaType == MediaType.GIF
        val extension: String
            get() = Path(path).extension
        val nameWithoutExtension: String
            get() = Path(path).nameWithoutExtension
    }
    
    
    data class Album(
        override val path: String,
        override val name: String = Path(path).name,
        override val size: Long = 0,
        override val creationDate: Long = 0,
        override val modificationDate: Long = 0,
        override val thumbnail: String? = null,
        val filesCount: Int = 0,
        val imagesCount: Int = 0,
        val videosCount: Int = 0,
        val gifsCount: Int = 0,
        val nestedAlbumsCount: Int = 0
    ) : MediaItemUI {
        val isVolume: Boolean
            get() {
                val volumesRegex = Regex("^/storage(/emulated)*(/[a-zA-Z0-9_-]+)?$")
                val isVolume = volumesRegex.containsMatchIn(path)
                val isNotWritable = Path(path).isWritable().not()
                return isVolume || isNotWritable
            }
    }
}
