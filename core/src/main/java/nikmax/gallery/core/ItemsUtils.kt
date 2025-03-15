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
     * Default album path that will be used when target album path is not specified
     */
    private const val ROOT_ALBUM_PATH = "/storage/"

    /**
     * Create list of [MediaItemUI] based on target album path, albums mode (plain/nested), applied filters, sorting order
     *
     * @param targetAlbumPath include only files and albums inside this album (if null [ROOT_ALBUM_PATH] will be used)
     * @param albumsMode create "classic" plain list of all albums found on device or nested list based on [targetAlbumPath]
     * @param appliedFilters set of [GalleryPreferences.Filter]
     * @param sortingOrder sort created list by [GalleryPreferences.SortingOrder]
     * @param useDescendSorting reverse sorting or not
     * @return list of [MediaItemUI] ready to display
     */
    fun List<MediaFileData>.createItemsListToDisplay(
        targetAlbumPath: String?,
        albumsMode: GalleryPreferences.AlbumsMode,
        appliedFilters: Set<GalleryPreferences.Filter>,
        sortingOrder: GalleryPreferences.SortingOrder,
        useDescendSorting: Boolean,
        includeHidden: Boolean,
        searchQuery: String? = null,
        includeFilesOnly: Boolean = false
    ): List<MediaItemUI> {
        val itemsListToDisplay = this.mapDataFilesToUiFiles().let { uiFiles ->
            val targetPathChildren = when (targetAlbumPath != null) {
                true -> uiFiles.filter { it.path.startsWith(targetAlbumPath) }
                false -> uiFiles
            }
            val searchFiltered = when (searchQuery != null) {
                true -> targetPathChildren.filter { it.path.contains(searchQuery, ignoreCase = true) }
                false -> targetPathChildren
            }
            val albumsGrouped = when (albumsMode) {
                GalleryPreferences.AlbumsMode.PLAIN -> when (targetAlbumPath.isNullOrEmpty()) {
                    true -> searchFiltered.createFlatAlbumsList()
                    false -> searchFiltered.createAlbumOwnFilesList(targetAlbumPath)
                }
                GalleryPreferences.AlbumsMode.NESTED -> when (targetAlbumPath.isNullOrEmpty()) {
                    true -> searchFiltered.createNestedAlbumsList(ROOT_ALBUM_PATH)
                    false -> searchFiltered.createNestedAlbumsList(targetAlbumPath)
                }
            }
            val withFiltersApplied = albumsGrouped.applyFilters(appliedFilters)
            val hiddenChecked = when (includeHidden) {
                true -> withFiltersApplied
                false -> withFiltersApplied.filterNot { it.hidden }
            }
            val filesOnlyChecked = when (includeFilesOnly) {
                true -> hiddenChecked.filterIsInstance<MediaItemUI.File>()
                false -> hiddenChecked
            }
            val sorted = filesOnlyChecked.applySorting(sortingOrder, useDescendSorting)
            sorted
        }
        return itemsListToDisplay
    }


    /**
     * Filters the list of [MediaItemUI] to only include files in the specified album path.
     *
     * @param albumPath The path of the album whose files are to be included.
     * @return A list of [MediaItemUI] that belong to the specified album path.
     */
    private fun List<MediaItemUI>.createAlbumOwnFilesList(albumPath: String): List<MediaItemUI> {
        return this.filter { Path(it.path).parent.pathString == albumPath }
    }

    /**
     * Groups a list of [MediaItemUI.File] into albums based on their parent directory path.
     *
     * @return A list of [MediaItemUI.Album] created from the grouped files.
     */
    private fun List<MediaItemUI.File>.createFlatAlbumsList(): List<MediaItemUI.Album> {
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
    private fun List<MediaItemUI.File>.createNestedAlbumsList(albumPath: String?): List<MediaItemUI> {
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
     * Sorts the list of [MediaItemUI] by [GalleryPreferences.SortingOrder].
     */
    private fun List<MediaItemUI>.applySorting(
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

    /**
     * Filter list of [MediaItemUI] depends on provided [GalleryPreferences.Filter] filters .
     */
    private fun List<MediaItemUI>.applyFilters(
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


    private fun List<MediaFileData>.mapDataFilesToUiFiles(): List<MediaItemUI.File> {
        return this.map { it.mapDataFileToUiFile() }
    }

    /**
     * Maps [MediaFileData] to [MediaItemUI.File].
     */
    private fun MediaFileData.mapDataFileToUiFile(): MediaItemUI.File {
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
            thumbnail = this.path
        )
    }
}
