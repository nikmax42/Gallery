package mtree.core.domain.usecases

import mtree.core.domain.models.Filters
import mtree.core.domain.models.GalleryMode
import mtree.core.domain.models.MediaItemDomain
import mtree.core.domain.models.Sort
import mtree.core.domain.usecases.ItemsUtils.Filtering.applyFilters
import mtree.core.domain.usecases.ItemsUtils.PlainMode.getAlbumOwnFiles
import mtree.core.domain.usecases.ItemsUtils.PlainMode.getAllNotEmptyAlbums
import mtree.core.domain.usecases.ItemsUtils.PlainMode.plainSearchByPath
import mtree.core.domain.usecases.ItemsUtils.Sorting.applySorting
import mtree.core.domain.usecases.ItemsUtils.TreeMode.getItemsForPath
import mtree.core.domain.usecases.ItemsUtils.TreeMode.getTreeSearchResult
import kotlin.io.path.Path
import kotlin.io.path.pathString

interface CreateItemsListToDisplayUc {
    fun execute(
        galleryAlbums: List<MediaItemDomain.Album>,
        basePath: String?,
        searchQuery: String?,
        galleryMode: GalleryMode,
        filters: Filters,
        sort: Sort
    ): List<MediaItemDomain>
}



internal class CreateItemsListToDisplayUcImpl() : CreateItemsListToDisplayUc {
    override fun execute(
        galleryAlbums: List<MediaItemDomain.Album>,
        basePath: String?,
        searchQuery: String?,
        galleryMode: GalleryMode,
        filters: Filters,
        sort: Sort
    ): List<MediaItemDomain> {
        val itemsToDisplay = when (galleryMode) {
            GalleryMode.PLAIN -> {
                when (basePath == null) {
                    true -> galleryAlbums.getAllNotEmptyAlbums()
                    false -> galleryAlbums.getAlbumOwnFiles(albumPath = basePath)
                }.let { plainItems ->
                    when (searchQuery == null) {
                        true -> plainItems
                        false -> plainItems.plainSearchByPath(query = searchQuery)
                    }
                }
            }
            GalleryMode.TREE -> {
                when (searchQuery == null) {
                    true -> galleryAlbums.getItemsForPath(basePath)
                    false -> galleryAlbums.getTreeSearchResult(
                        basePath = basePath,
                        query = searchQuery
                    )
                }
            }
        }.applyFilters(filters).applySorting(sort)
        return itemsToDisplay
    }
    
}

internal object ItemsUtils {
    
    object PlainMode {
        fun List<MediaItemDomain.Album>.getAllNotEmptyAlbums(): List<MediaItemDomain.Album> {
            return this.filterNot { it.ownFiles.isEmpty() }
        }
        
        fun List<MediaItemDomain.Album>.getAlbumOwnFiles(
            albumPath: String
        ): List<MediaItemDomain.File> {
            return this.find { it.path == albumPath }?.ownFiles ?: emptyList()
        }
        
        fun List<MediaItemDomain>.plainSearchByPath(
            query: String
        ): List<MediaItemDomain> {
            return this.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    
    object TreeMode {
        private const val GALLERY_ROOT_PATH = "/storage"
        
        fun List<MediaItemDomain.Album>.getItemsForPath(path: String?): List<MediaItemDomain> {
            val targetPath = when (path == null) {
                true -> GALLERY_ROOT_PATH
                false -> path
            }
            val files = getAlbumOwnFiles(targetPath)
            val albums = this.filter {
                Path(it.path).parent?.pathString == targetPath
            }
            return albums + files
        }
        
        fun List<MediaItemDomain.Album>.getTreeSearchResult(
            basePath: String?,
            query: String
        ): List<MediaItemDomain> {
            val directoryForSearch = when (basePath == null) {
                true -> GALLERY_ROOT_PATH
                false -> basePath
            }
            val baseDirectoryFilesContainsQuery = this
                .find { it.path == directoryForSearch }
                ?.let { baseDirectory ->
                    baseDirectory.ownFiles.filter { it.name.contains(query, ignoreCase = true) }
                } ?: emptyList()
            val nestedAlbumsContainsQuery = this
                .filter { it.path.startsWith(directoryForSearch) }
                .filterNot { it.path == directoryForSearch }
                .let { albumsToSearch ->
                    val foundAlbumsMap = mutableMapOf<String, MediaItemDomain.Album>()
                    albumsToSearch.forEach { album ->
                        //if album contains files with query:
                        // 1. get this album
                        // 2. check if it has parents in search directory
                        // 3. if parents found - return parent nearest to the base path
                        // 4. else return album itself
                        if (album.ownFiles.any { it.name.contains(query, ignoreCase = true) }) {
                            val albumOldestParent = albumsToSearch
                                .filter { it.path != album.path && album.path.startsWith(it.path) }
                                .minByOrNull { it.path.count { it == '/' } }
                            when (albumOldestParent != null) {
                                true -> foundAlbumsMap.put(albumOldestParent.path, albumOldestParent)
                                false -> foundAlbumsMap.put(album.path, album)
                            }
                        }
                    }
                    foundAlbumsMap.values.toList()
                }
            return baseDirectoryFilesContainsQuery + nestedAlbumsContainsQuery
        }
    }
    
    object Filtering {
        
        fun List<MediaItemDomain>.applyFilters(
            filters: Filters
        ): List<MediaItemDomain> {
            return this
                .applyMediaTypeFilters(
                    includeImages = filters.includeImages,
                    includeVideos = filters.includeVideos,
                    includeGifs = filters.includeGifs
                ).applyVisibilityFilters(
                    includeUnhidden = filters.includeUnhidden,
                    includeHidden = filters.includeHidden
                ).applyItemTypeFilters(
                    includeAlbums = filters.includeAlbums,
                    includeFiles = filters.includeFiles
                )
        }
        
        private fun List<MediaItemDomain>.applyMediaTypeFilters(
            includeImages: Boolean,
            includeVideos: Boolean,
            includeGifs: Boolean,
        ): List<MediaItemDomain> {
            val files = this.filterIsInstance<MediaItemDomain.File>()
            val albums = this.filterIsInstance<MediaItemDomain.Album>()
            
            val filteredFiles = files.filterFilesByMediaType(includeImages, includeVideos, includeGifs)
            val filteredAlbums = albums.filterAlbumsByMediaType(includeImages, includeVideos, includeGifs)
            
            return filteredFiles + filteredAlbums
        }
        
        private fun List<MediaItemDomain>.applyVisibilityFilters(
            includeUnhidden: Boolean,
            includeHidden: Boolean,
        ): List<MediaItemDomain> {
            val files = this.filterIsInstance<MediaItemDomain.File>()
            val albums = this.filterIsInstance<MediaItemDomain.Album>()
            
            val filteredFiles = files.filterFilesByVisibility(includeUnhidden, includeHidden)
            val filteredAlbums = albums.filterAlbumsByVisibility(includeUnhidden, includeHidden)
            
            return filteredFiles + filteredAlbums
        }
        
        private fun List<MediaItemDomain>.applyItemTypeFilters(
            includeAlbums: Boolean,
            includeFiles: Boolean
        ): List<MediaItemDomain> {
            val filesFiltered = if (includeFiles) this.filterIsInstance<MediaItemDomain.File>() else emptyList()
            val albumsFiltered = if (includeAlbums) this.filterIsInstance<MediaItemDomain.Album>() else emptyList()
            
            return (filesFiltered + albumsFiltered).distinct()
        }
        
        private fun List<MediaItemDomain.File>.filterFilesByMediaType(
            includeImages: Boolean,
            includeVideos: Boolean,
            includeGifs: Boolean,
        ): List<MediaItemDomain.File> {
            val imageFiltered =
                if (includeImages) this.filter { it.mediaType == MediaItemDomain.File.MediaType.IMAGE }
                else emptyList()
            
            val videoFiltered =
                if (includeVideos) this.filter { it.mediaType == MediaItemDomain.File.MediaType.VIDEO }
                else emptyList()
            
            val gifFiltered =
                if (includeGifs) this.filter { it.mediaType == MediaItemDomain.File.MediaType.GIF }
                else emptyList()
            
            return (imageFiltered + videoFiltered + gifFiltered).distinct()
        }
        
        private fun List<MediaItemDomain.Album>.filterAlbumsByMediaType(
            includeImages: Boolean,
            includeVideos: Boolean,
            includeGifs: Boolean,
        ): List<MediaItemDomain.Album> {
            val imageFiltered = if (includeImages) this.filter { it.nestedImagesCount > 0 } else emptyList()
            val videoFiltered = if (includeVideos) this.filter { it.nestedVideosCount > 0 } else emptyList()
            val gifFiltered = if (includeGifs) this.filter { it.nestedGifsCount > 0 } else emptyList()
            
            return (imageFiltered + videoFiltered + gifFiltered).distinct()
        }
        
        private fun List<MediaItemDomain.File>.filterFilesByVisibility(
            includeUnhidden: Boolean,
            includeHidden: Boolean
        ): List<MediaItemDomain.File> {
            val unhidden = if (includeUnhidden) this.filterNot { it.isHidden } else emptyList()
            val hidden = if (includeHidden) this.filter { it.isHidden } else emptyList()
            
            return (unhidden + hidden).distinct()
        }
        
        private fun List<MediaItemDomain.Album>.filterAlbumsByVisibility(
            includeUnhidden: Boolean,
            includeHidden: Boolean
        ): List<MediaItemDomain.Album> {
            val unhidden = if (includeUnhidden) this.filter { !it.isHidden && it.nestedUnhiddenMediaCount > 0 } else emptyList()
            val hidden = if (includeHidden) this.filter { it.isHidden && it.nestedHiddenMediaCount > 0 } else emptyList()
            return (unhidden + hidden).distinct()
        }
    }
    
    object Sorting {
        fun List<MediaItemDomain>.applySorting(
            sort: Sort
        ): List<MediaItemDomain> {
            return when (sort.order) {
                Sort.Order.NAME -> this.sortedBy { it.name }
                Sort.Order.EXTENSION -> this.sortedByExtension()
                Sort.Order.SIZE -> this.sortedBy { it.size }
                Sort.Order.CREATION_DATE -> this.sortedBy { it.creationDate }
                Sort.Order.MODIFICATION_DATE -> this.sortedBy { it.modificationDate }
                Sort.Order.RANDOM -> this.shuffled()
            }.let {
                when (sort.descend) {
                    true -> it.reversed()
                    false -> it
                }
            }.let {
                when (sort.placeFirst) {
                    Sort.PlaceFirst.ALBUMS -> it.albumsFirstPlaced()
                    Sort.PlaceFirst.FILES -> it.filesFirstPlaced()
                    Sort.PlaceFirst.NONE -> it
                }
            }
        }
        
        private fun List<MediaItemDomain>.sortedByExtension(): List<MediaItemDomain> {
            return filterIsInstance<MediaItemDomain.File>().sortedBy { it.extension } +
                    filterIsInstance<MediaItemDomain.Album>()
        }
        
        private fun List<MediaItemDomain>.albumsFirstPlaced(): List<MediaItemDomain> {
            return filterIsInstance<MediaItemDomain.Album>() + filterIsInstance<MediaItemDomain.File>()
        }
        
        private fun List<MediaItemDomain>.filesFirstPlaced(): List<MediaItemDomain> {
            return filterIsInstance<MediaItemDomain.File>() + filterIsInstance<MediaItemDomain.Album>()
        }
    }
}
