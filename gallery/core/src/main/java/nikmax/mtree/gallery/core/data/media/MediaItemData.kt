package nikmax.mtree.gallery.core.data.media

import android.webkit.MimeTypeMap
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.name

sealed interface MediaItemData {
    enum class Storage { DEVICE, PLUGGABLE }
    
    val path: String
    val dateCreated: Long
    val dateModified: Long
    val size: Long
    val name: String get() = Path(path).name
    val isHidden: Boolean get() = path.contains("/.")
    val storage: Storage
        get() {
            return if (path.startsWith("/storage/emulated/")) Storage.DEVICE
            else Storage.PLUGGABLE
        }
    
    data class File(
        override val path: String,
        override val size: Long = 0,
        override val dateCreated: Long = 0,
        override val dateModified: Long = 0,
        val uri: String = "file://$path",
        val duration: Long = 0,
    ) : MediaItemData {
        enum class Type { IMAGE, VIDEO, GIF }
        
        val extension = Path(path).extension
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
        val mediaType =
            if (mimeType == "image/gif") Type.GIF
            else if (mimeType.startsWith("video/")) Type.VIDEO
            else if (mimeType.startsWith("image/")) Type.IMAGE
            else "null"
    }
    
    data class Album(
        override val path: String,
        val files: List<File> = emptyList(),
        override val size: Long = files.sumOf { it.size },
        override val dateCreated: Long = files.minOfOrNull { it.dateCreated } ?: 0,
        override val dateModified: Long = files.maxOfOrNull { it.dateModified } ?: 0,
        val imagesCount: Int = 0,
        val videosCount: Int = 0,
        val gifsCount: Int = 0,
        val unhiddenCount: Int = 0,
        val hiddenCount: Int = 0,
        val nestedDirectoriesCount: Int = 0,
        val thumbnail: String = ""
    ) : MediaItemData
}
