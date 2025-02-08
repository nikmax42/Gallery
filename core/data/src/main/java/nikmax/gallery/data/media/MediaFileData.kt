package nikmax.gallery.data.media

import android.webkit.MimeTypeMap
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.name

data class MediaFileData(
    val path: String,
    val size: Long,
    val dateCreated: Long,
    val dateModified: Long,
    val volume: Volume,
) {
    enum class Volume { PRIMARY, SECONDARY }
    enum class MediaType { IMAGE, VIDEO, GIF, UNDEFINED }

    val name: String = Path(path).name
    val extension = Path(path).extension
    val mimetype: String? = calculateMime()
    val mediaType: MediaType
        get() = if (mimetype.toString().startsWith("video/")) MediaType.VIDEO
        else if (mimetype == "image/gif") MediaType.GIF
        else if (mimetype.toString().startsWith("image/")) MediaType.IMAGE
        else MediaType.UNDEFINED

    private fun calculateMime(): String? {
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
    }
}
