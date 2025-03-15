package nikmax.gallery.core.ui

import org.apache.tika.Tika
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.isWritable
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

sealed interface MediaItemUI {
    enum class Volume { PRIMARY, SECONDARY }

    val path: String
    val name: String
    val thumbnail: String?
    val size: Long
    val dateCreated: Long
    val dateModified: Long
    val volume: Volume
    val hidden get() = path.contains("/.")
    val protected: Boolean
        get() {
            val volumesRegex = Regex("^/storage(/emulated)*(/[a-zA-Z0-9_-]+)?$")
            val isVolume = volumesRegex.containsMatchIn(path)
            val isNotWritable = Path(path).isWritable().not()
            return isVolume || isNotWritable
        }

    data class File(
        override val path: String,
        override val size: Long = 0,
        override val dateCreated: Long = 0,
        override val dateModified: Long = 0,
        override val volume: Volume = Volume.PRIMARY,
        override val name: String = Path(path).name,
        override val thumbnail: String? = null,
        val uri: String? = null,
        val mimetype: String = Tika().detect(path)
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
    }

    data class Album(
        override val path: String,
        override val name: String = Path(path).name,
        override val size: Long = 0,
        override val dateCreated: Long = 0,
        override val dateModified: Long = 0,
        override val volume: Volume = Volume.PRIMARY,
        override val thumbnail: String? = null,
        val filesCount: Int = 0,
        val imagesCount: Int = 0,
        val videosCount: Int = 0,
        val gifsCount: Int = 0
    ) : MediaItemUI
}
