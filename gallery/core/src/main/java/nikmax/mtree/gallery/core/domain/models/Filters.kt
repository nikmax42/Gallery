package nikmax.mtree.gallery.core.domain.models

data class Filters(
    val includeAlbums: Boolean,
    val includeFiles: Boolean,
    val includeImages: Boolean,
    val includeVideos: Boolean,
    val includeGifs: Boolean,
    val includeUnhidden: Boolean,
    val includeHidden: Boolean
) {
    companion object {
        fun includeAll() = Filters(
            includeAlbums = true,
            includeFiles = true,
            includeImages = true,
            includeVideos = true,
            includeGifs = true,
            includeUnhidden = true,
            includeHidden = true
        )
    }
}
