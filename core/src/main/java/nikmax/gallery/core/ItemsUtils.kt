package nikmax.gallery.core

import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.data.media.MediaFileData
import nikmax.gallery.data.preferences.GalleryPreferences
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.pathString

/**
 * Contains utility functions for working with [MediaItemUI].
 */
object ItemsUtils {

    /**
     * Contains functions to convert one model to another.
     */
    object Mapping {
        /**
         * Maps [MediaFileData] to [MediaItemUI.File].
         */
        fun MediaFileData.mapDataFileToUiFile(): MediaItemUI.File {
            return MediaItemUI.File(
                path = this.path,
                name = this.name,
                size = this.size,
                dateCreated = this.dateCreated,
                dateModified = this.dateModified,
                volume = when (this.volume) {
                    MediaFileData.Volume.PRIMARY -> MediaItemUI.Volume.PRIMARY
                    MediaFileData.Volume.SECONDARY -> MediaItemUI.Volume.SECONDARY
                },
                mimetype = this.mimetype ?: "null",
                thumbnail = this.path
            )
        }

        fun List<MediaFileData>.mapDataFilesToUiFiles(): List<MediaItemUI.File> {
            return this.map { it.mapDataFileToUiFile() }
        }

        /**
         * Maps list of [MediaFileData] to [MediaItemUI.Album].
         *
         * WARNING: all provided files must be the direct children of the desired album. This method does not check it by itself
         */
        fun List<MediaFileData>.mapDataFilesToUiAlbum(): MediaItemUI.Album {
            val albumPath = Path(this.first().path).parent
            val albumSize = this.sumOf { it.size }
            val albumCreated = this.minOf { it.dateCreated }
            val albumModified = this.maxOf { it.dateModified }
            val albumThumbnail = this.minBy { it.dateCreated }.path
            return MediaItemUI.Album(
                path = albumPath.pathString,
                name = albumPath.name,
                size = albumSize,
                dateCreated = albumCreated,
                dateModified = albumModified,
                volume = when (this.first().volume) {
                    MediaFileData.Volume.PRIMARY -> MediaItemUI.Volume.PRIMARY
                    MediaFileData.Volume.SECONDARY -> MediaItemUI.Volume.SECONDARY
                },
                thumbnail = albumThumbnail,
                filesCount = this.size,
                imagesCount = this.count { (it.mimetype?.startsWith("image/") == true && it.mimetype != "image/gif") },
                videosCount = this.count { it.mimetype?.startsWith("video/") == true },
                gifsCount = this.count { it.mimetype?.startsWith("image/gif") == true }
            )
        }
    }

    /**
     * Contains functions to sort lists of [MediaItemUI] by provided [GalleryPreferences.SortingOrder].
     */
    object Sorting {
        /**
         * Sorts the list of [MediaItemUI] by [GalleryPreferences.SortingOrder].
         */
        fun List<MediaItemUI>.applySorting(
            sortingOrder: GalleryPreferences.SortingOrder,
            descend: Boolean
        ): List<MediaItemUI> {
            return when (sortingOrder) {
                GalleryPreferences.SortingOrder.CREATION_DATE -> this.sortedBy { it.dateCreated }
                GalleryPreferences.SortingOrder.MODIFICATION_DATE -> this.sortedBy { it.dateModified }
                GalleryPreferences.SortingOrder.NAME -> this.sortedBy { it.name }
                GalleryPreferences.SortingOrder.SIZE -> this.sortedBy { it.size }
            }.let {
                when (descend) {
                    true -> it.reversed()
                    false -> it
                }
            }
        }
    }

    /**
     * Contains functions to apply filters and search to lists of [MediaItemUI].
     */
    object SearchingAndFiltering {

        /**
         * Filters the list of [MediaItemUI] to only include files in the specified album path.
         *
         * @param albumPath The path of the album whose files are to be included.
         * @return A list of [MediaItemUI] that belong to the specified album path.
         */
        fun List<MediaItemUI>.createAlbumOwnFilesList(albumPath: String): List<MediaItemUI> {
            return this.filter { Path(it.path).parent.pathString == albumPath }
        }

        /**
         * Groups a list of [MediaItemUI.File] into albums based on their parent directory path.
         *
         * @return A list of [MediaItemUI.Album] created from the grouped files.
         */
        fun List<MediaItemUI.File>.createFlatAlbumsList(): List<MediaItemUI.Album> {
            return this.groupBy { file -> Path(file.path).parent.pathString }
                .map {
                    MediaItemUI.Album(
                        path = it.key,
                        name = Path(it.key).name,
                        size = it.value.sumOf { file -> file.size },
                        dateCreated = it.value.minOf { file -> file.dateCreated },
                        dateModified = it.value.maxOf { file -> file.dateModified },
                        volume = it.value.first().volume,
                        thumbnail = it.value.first().path,
                        filesCount = it.value.size,
                        imagesCount = it.value.count { file -> file.mediaType == MediaItemUI.File.MediaType.IMAGE },
                        videosCount = it.value.count { file -> file.mediaType == MediaItemUI.File.MediaType.VIDEO },
                        gifsCount = it.value.count { file -> file.mediaType == MediaItemUI.File.MediaType.GIF }
                    )
                }
        }

        /**
         * include target album's direct children
         * include target album's deep children that doesn't have any media albums between them and the target album
         * NOT include album if it's a target album's deep child, but has intermediate media album between.
         *
         *
         * For Example, we have the next directories structure:
         * ```
         * DCIM [3 images in directory itself]
         * DCIM/Pictures [2]
         * DCIM/Photos [0]
         * DCIM/Photos/Birthday [10]
         * DCIM/Photos/Cats [50]
         * DCIM/Movies/FolderWithoutOwnMedia/DeepFolderWithMedia [1]
         * ```
         * If the target albums is `/DCIM/`, then the next albums should be returned:
         * ```
         * DCIM [3] // Because have 3 own files
         * Pictures [2]
         * Photos [50] // Photos doesn't contain any files by itself, but has two child albums with media
         * DeepFolderWithMedia[1] // intermediate folder doesn't contain media files by itself, so `FolderWithoutOwnMedia` will be skipped
         * ```
         */
        fun List<MediaItemUI.File>.createNestedAlbumsList(albumPath: String?): List<MediaItemUI> {
            // filter all files which placed under target directory (include both direct and deep children)
            val targetAlbumNestedFiles = this.filter { it.path.startsWith(albumPath.toString()) }
            val subAlbums = targetAlbumNestedFiles
                .groupBy { Path(it.path).parent.pathString }
                .map {
                    MediaItemUI.Album(
                        path = it.key,
                        name = Path(it.key).name,
                        size = it.value.sumOf { file -> file.size },
                        dateCreated = it.value.minOf { file -> file.dateCreated },
                        dateModified = it.value.maxOf { file -> file.dateModified },
                        volume = it.value.first().volume,
                        thumbnail = it.value.first().path,
                        filesCount = it.value.size,
                        imagesCount = it.value.count { file -> file.mediaType == MediaItemUI.File.MediaType.IMAGE },
                        videosCount = it.value.count { file -> file.mediaType == MediaItemUI.File.MediaType.VIDEO },
                        gifsCount = it.value.count { file -> file.mediaType == MediaItemUI.File.MediaType.GIF }
                    )
                }
                .filterNot { it.path == albumPath }
            // 1. filter albums that not include other album full path
            val filteredNestedAlbums = subAlbums.filterNot { album ->
                subAlbums
                    .filterNot { it == album }
                    .any { otherAlbum -> album.path.startsWith(otherAlbum.path) }
            }
            // 2. filter album own files
            val ownFiles = this.filter { file ->
                Path(file.path).parent.pathString == albumPath
            }
            return filteredNestedAlbums + ownFiles
        }

        /**
         * Filter list of [MediaItemUI] depends on provided [GalleryPreferences.Filter] filters .
         */
        fun List<MediaItemUI>.applyFilters(
            selectedFilters: Set<GalleryPreferences.Filter>
        ): List<MediaItemUI> {
            val imagesFiltered = when (selectedFilters.contains(GalleryPreferences.Filter.IMAGES)) {
                true -> this.filter { item ->
                    when (item) {
                        is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.IMAGE
                        is MediaItemUI.Album -> item.imagesCount > 0
                    }
                }
                false -> emptyList()
            }
            val videosFiltered = when (selectedFilters.contains(GalleryPreferences.Filter.VIDEOS)) {
                true -> this.filter { item ->
                    when (item) {
                        is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.VIDEO
                        is MediaItemUI.Album -> item.videosCount > 0
                    }
                }
                false -> emptyList()
            }
            val gifsFiltered = when (selectedFilters.contains(GalleryPreferences.Filter.GIFS)) {
                true -> this.filter { item ->
                    when (item) {
                        is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.GIF
                        is MediaItemUI.Album -> item.gifsCount > 0
                    }
                }
                false -> emptyList()
            }
            return imagesFiltered + videosFiltered + gifsFiltered
        }

        /**
         * Filter list of [MediaItemUI] to exclude hidden items.
         */
        fun List<MediaItemUI>.excludeHidden(): List<MediaItemUI> {
            return this.filterNot { itemUi -> itemUi.hidden }
        }
    }
}
