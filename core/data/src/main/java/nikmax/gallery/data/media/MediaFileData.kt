package nikmax.gallery.data.media

import kotlin.io.path.Path
import kotlin.io.path.name

data class MediaFileData(
    val path: String,
    val name: String = Path(path).name,
    val size: Long = 0,
    val dateCreated: Long = 0,
    val dateModified: Long = 0,
    val volume: Volume = Volume.PRIMARY,
) {
    enum class Volume { PRIMARY, SECONDARY }
}
