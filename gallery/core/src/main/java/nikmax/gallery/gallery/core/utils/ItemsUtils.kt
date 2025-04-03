package nikmax.gallery.gallery.core.utils

import nikmax.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.gallery.core.data.media.MediaItemData
import nikmax.gallery.gallery.core.ui.MediaItemUI
import kotlin.random.Random

/**
 * Contains utility functions for working with [MediaItemUI].
 */
object ItemsUtils {
    
    fun List<MediaItemData>.mapToUi(): List<MediaItemUI> {
        return this.map { it.mapToUi() }
    }
    
    fun MediaItemData.mapToUi(): MediaItemUI {
        return when (this) {
            is MediaItemData.File -> MediaItemUI.File(
                path = this.path,
                size = this.size,
                duration = this.duration,
                creationDate = this.dateCreated,
                modificationDate = this.dateModified,
                belongsToVolume = when (this.storage) {
                    MediaItemData.Storage.DEVICE -> MediaItemUI.Volume.DEVICE
                    MediaItemData.Storage.PLUGGABLE -> MediaItemUI.Volume.PLUGGABLE
                },
                thumbnail = this.path
            )
            is MediaItemData.Album -> MediaItemUI.Album(
                path = this.path,
                size = this.size,
                creationDate = this.dateCreated,
                modificationDate = this.dateModified,
                belongsToVolume = when (this.storage) {
                    MediaItemData.Storage.DEVICE -> MediaItemUI.Volume.DEVICE
                    MediaItemData.Storage.PLUGGABLE -> MediaItemUI.Volume.PLUGGABLE
                },
                thumbnail = this.thumbnail,
                filesCount = this.imagesCount + this.videosCount + this.gifsCount,
                imagesCount = this.imagesCount,
                videosCount = this.videosCount,
                gifsCount = this.gifsCount,
                nestedAlbumsCountLInt = this.nestedDirectoriesCount
            )
        }
    }
    
    
    //todo cover with tests
    fun List<MediaItemUI>.applySorting(
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
    
    //todo cover with tests
    fun List<MediaItemUI>.applyFilters(
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
}
