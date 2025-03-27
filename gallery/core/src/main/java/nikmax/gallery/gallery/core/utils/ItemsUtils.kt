package nikmax.gallery.gallery.core.utils

import nikmax.gallery.gallery.core.data.media.MediaFileData
import nikmax.gallery.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.ui.MediaItemUI
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.random.Random

/**
 * Contains utility functions for working with [MediaItemUI].
 */
object ItemsUtils {
    /**
     * Default album path that will be used when target album path is not specified
     */
    private const val ROOT_ALBUM_PATH = "/storage/"
    
    fun List<MediaFileData>.createItemsListToDisplay(
        targetAlbumPath: String?,
        searchQuery: String? = null,
        treeModeEnabled: Boolean = true,
        includeImages: Boolean = true,
        includeVideos: Boolean = true,
        includeGifs: Boolean = true,
        includeUnHidden: Boolean = true,
        includeHidden: Boolean = false,
        includeFiles: Boolean = true,
        includeAlbums: Boolean = true,
        sortingOrder: GalleryPreferences.Sorting.Order = GalleryPreferences.Sorting.Order.MODIFICATION_DATE,
        descendSorting: Boolean = false,
        showAlbumsFirst: Boolean = false,
        showFilesFirst: Boolean = false
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
            val albumsGrouped = when (treeModeEnabled) {
                true -> when (targetAlbumPath.isNullOrEmpty()) {
                    true -> searchFiltered.createNestedAlbumsList(ROOT_ALBUM_PATH)
                    false -> searchFiltered.createNestedAlbumsList(targetAlbumPath)
                }
                false -> when (targetAlbumPath.isNullOrEmpty()) {
                    true -> searchFiltered.createFlatAlbumsList()
                    false -> searchFiltered.createAlbumOwnFilesList(targetAlbumPath)
                }
            }
            val filtered = albumsGrouped.applyFilters(
                includeImages = includeImages,
                includeVideos = includeVideos,
                includeGifs = includeGifs,
                includeUnHidden = includeUnHidden,
                includeHidden = includeHidden,
                includeFiles = includeFiles,
                includeAlbums = includeAlbums
            )
            val sorted = filtered.applySorting(
                sortingOrder = sortingOrder,
                descend = descendSorting,
                albumsFirst = showAlbumsFirst,
                filesFirst = showFilesFirst
            )
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
                    creationDate = it.value.minOf { file -> file.creationDate },
                    modificationDate = it.value.maxOf { file -> file.modificationDate },
                    belongsToVolume = it.value.first().belongsToVolume,
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
            .map { albumGroup ->
                val albumOwnFiles = albumGroup.value
                val nestedAlbumsFiles =
                    this.filter { file -> file.path.startsWith(albumGroup.key) }.minus(albumOwnFiles)
                
                val albumName = Path(albumGroup.key).name
                // size of album own files and all nested album's files
                val albumSize = (albumOwnFiles + nestedAlbumsFiles).sumOf { it.size }
                val albumCreationDate = (albumOwnFiles + nestedAlbumsFiles).minOf { it.creationDate }
                val albumModificationDate = (albumOwnFiles + nestedAlbumsFiles).minOf { it.modificationDate }
                val albumVolume = albumOwnFiles.first().belongsToVolume
                val albumThumbnail = (albumOwnFiles + nestedAlbumsFiles).first().path
                val filesCount = (albumOwnFiles + nestedAlbumsFiles).size
                val imagesCount =
                    (albumOwnFiles + nestedAlbumsFiles).count { it.mediaType == MediaItemUI.File.MediaType.IMAGE }
                val videosCount =
                    (albumOwnFiles + nestedAlbumsFiles).count { it.mediaType == MediaItemUI.File.MediaType.VIDEO }
                val gifsCount =
                    (albumOwnFiles + nestedAlbumsFiles).count { it.mediaType == MediaItemUI.File.MediaType.GIF }
                
                MediaItemUI.Album(
                    path = albumGroup.key,
                    name = albumName,
                    size = albumSize,
                    creationDate = albumCreationDate,
                    modificationDate = albumModificationDate,
                    belongsToVolume = albumVolume,
                    thumbnail = albumThumbnail,
                    filesCount = filesCount,
                    imagesCount = imagesCount,
                    videosCount = videosCount,
                    gifsCount = gifsCount
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
    
    
    private fun List<MediaItemUI>.applySorting(
        sortingOrder: GalleryPreferences.Sorting.Order,
        descend: Boolean,
        albumsFirst: Boolean,
        filesFirst: Boolean
    ): List<MediaItemUI> {
        return when (sortingOrder) {
            GalleryPreferences.Sorting.Order.CREATION_DATE -> this.sortedBy { it.creationDate }
            GalleryPreferences.Sorting.Order.MODIFICATION_DATE -> this.sortedBy { it.modificationDate }
            GalleryPreferences.Sorting.Order.NAME -> this.sortedBy { it.name }
            GalleryPreferences.Sorting.Order.SIZE -> this.sortedBy { it.size }
            GalleryPreferences.Sorting.Order.RANDOM -> this.sortedBy { Random.nextInt() }
            GalleryPreferences.Sorting.Order.EXTENSION -> this.filterIsInstance<MediaItemUI.Album>() +
                    this.filterIsInstance<MediaItemUI.File>().sortedBy { it.extension }
        }.let {
            when (descend) {
                true -> it.reversed()
                false -> it
            }.let {
                if (albumsFirst) it.filterIsInstance<MediaItemUI.Album>() + it.filterIsInstance<MediaItemUI.File>()
                else if (filesFirst) it.filterIsInstance<MediaItemUI.File>() + it.filterIsInstance<MediaItemUI.Album>()
                else it
            }
        }
    }
    
    private fun List<MediaItemUI>.applyFilters(
        includeImages: Boolean,
        includeVideos: Boolean,
        includeGifs: Boolean,
        includeFiles: Boolean,
        includeAlbums: Boolean,
        includeUnHidden: Boolean,
        includeHidden: Boolean,
    ): List<MediaItemUI> {
        val images = if (includeImages) this.filter { item ->
            when (item) {
                is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.IMAGE
                is MediaItemUI.Album -> item.imagesCount > 0
            }
        }
        else emptyList()
        
        val videos = if (includeVideos) this.filter { item ->
            when (item) {
                is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.VIDEO
                is MediaItemUI.Album -> item.videosCount > 0
            }
        }
        else emptyList()
        
        val gifs = if (includeGifs) this.filter { item ->
            when (item) {
                is MediaItemUI.File -> item.mediaType == MediaItemUI.File.MediaType.GIF
                is MediaItemUI.Album -> item.gifsCount > 0
            }
        }
        else emptyList()
        
        val mediaTypeFiltered = (images + videos + gifs).distinct()
        
        val unhidden = if (includeUnHidden) mediaTypeFiltered.filter { !it.hidden } else emptyList()
        val hidden = if (includeHidden) mediaTypeFiltered.filter { it.hidden } else emptyList()
        
        val visibilityFiltered = unhidden + hidden
        
        val filesFiltered =
            if (includeFiles) visibilityFiltered.filterIsInstance<MediaItemUI.File>() else emptyList()
        val albumsFiltered =
            if (includeAlbums) visibilityFiltered.filterIsInstance<MediaItemUI.Album>() else emptyList()
        
        val itemTypeFiltered = filesFiltered + albumsFiltered
        
        return itemTypeFiltered
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
            duration = this.duration,
            creationDate = this.dateCreated,
            modificationDate = this.dateModified,
            belongsToVolume = when (this.volume) {
                MediaFileData.Volume.PRIMARY -> MediaItemUI.Volume.DEVICE
                MediaFileData.Volume.SECONDARY -> MediaItemUI.Volume.PLUGGABLE
            },
            thumbnail = this.path
        )
    }
}
