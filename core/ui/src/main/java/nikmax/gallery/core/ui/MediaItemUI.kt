package nikmax.gallery.core.ui

sealed interface MediaItemUI {
    enum class Volume { PRIMARY, SECONDARY }

    val path: String
    val name: String
    val thumbnail: String?
    val size: Long
    val dateCreated: Long
    val dateModified: Long
    val volume: Volume
    val hidden
        get() = path.contains("/.")

    data class File(
        override val path: String,
        override val name: String,
        override val size: Long,
        override val dateCreated: Long,
        override val dateModified: Long,
        override val volume: Volume,
        val mimetype: String,
        override val thumbnail: String? = null
    ) : MediaItemUI {
        enum class MediaType { IMAGE, VIDEO, GIF }

        val mediaType: MediaType
            get() = if (mimetype.startsWith("video/")) MediaType.VIDEO
            else if (mimetype == "image/gif") MediaType.GIF
            else MediaType.IMAGE
    }

    data class Album(
        override val path: String,
        override val name: String,
        override val size: Long,
        override val dateCreated: Long,
        override val dateModified: Long,
        override val volume: Volume,
        override val thumbnail: String? = null,
        val filesCount: Int = 0,
        val imagesCount: Int = 0,
        val videosCount: Int = 0,
        val gifsCount: Int = 0
    ) : MediaItemUI
}
