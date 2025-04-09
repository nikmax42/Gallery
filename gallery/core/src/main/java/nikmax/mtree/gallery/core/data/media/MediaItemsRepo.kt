package nikmax.mtree.gallery.core.data.media

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nikmax.mtree.core.data.Resource
import nikmax.mtree.gallery.core.data.media.MediaItemsRepo.SortOrder
import timber.log.Timber
import kotlin.io.path.Path
import kotlin.io.path.pathString

interface MediaItemsRepo {
    
    enum class SortOrder {
        NAME,
        SIZE,
        EXTENSION,
        CREATION_DATE,
        MODIFICATION_DATE,
        RANDOM
    }
    
    enum class PlaceOnTop {
        ALBUMS_ON_TOP,
        FILES_ON_TOP
    }
    
    val galleryRootPath: String
        get() = "/storage"
    
    fun getMediaItemsFlow(
        path: String?,
        searchQuery: String?,
        treeMode: Boolean = true,
        includeAlbums: Boolean = true,
        includeFiles: Boolean = true,
        includeImages: Boolean = true,
        includeVideos: Boolean = true,
        includeGifs: Boolean = true,
        includeUnhidden: Boolean = true,
        includeHidden: Boolean = false,
        sortingOrder: SortOrder = SortOrder.MODIFICATION_DATE,
        descendSorting: Boolean = false,
        placeOnTop: PlaceOnTop = PlaceOnTop.ALBUMS_ON_TOP
    ): Flow<Resource<List<MediaItemData>>>
    
    fun getAlbumContentFlow(
        path: String?,
        searchQuery: String?,
        treeMode: Boolean = true,
        includeAlbums: Boolean = true,
        includeFiles: Boolean = true,
        includeImages: Boolean = true,
        includeVideos: Boolean = true,
        includeGifs: Boolean = true,
        includeUnhidden: Boolean = true,
        includeHidden: Boolean = false,
        sortingOrder: SortOrder = SortOrder.MODIFICATION_DATE,
        descendSorting: Boolean = false,
        filesFirst: Boolean = false,
        albumsFirst: Boolean = false
    ): Flow<Resource<List<MediaItemData>>>
    
    fun getSearchResultFlow(
        query: String,
        basePath: String? = null,
        includeAlbums: Boolean = true,
        includeFiles: Boolean = true,
        includeImages: Boolean = true,
        includeVideos: Boolean = true,
        includeGifs: Boolean = true,
        includeUnhidden: Boolean = true,
        includeHidden: Boolean = false,
        sortingOrder: SortOrder = SortOrder.MODIFICATION_DATE,
        descendSorting: Boolean = false,
        filesFirst: Boolean = false,
        albumsFirst: Boolean = false
    ): Flow<Resource<List<MediaItemData>>>
    
    /**
     * Use mediastore to update files flow with all available media files.
     */
    suspend fun rescan()
    
    suspend fun checkExistence(filePath: String): Boolean
    
    suspend fun checkWriteAccess(directoryPath: String): Boolean
    
    suspend fun executeFileOperations(operations: List<FileOperation>): LiveData<List<WorkInfo>>
}


internal class MediaItemsRepoImpl(
    private val mediastoreDs: MediastoreDs,
    private val context: Context
) : MediaItemsRepo {
    
    private val _loadingFlow = MutableStateFlow(false)
    private val _albumsFlow = MutableStateFlow<List<MediaItemData.Album>>(emptyList())
    
    override fun getMediaItemsFlow(
        path: String?,
        searchQuery: String?,
        treeMode: Boolean,
        includeAlbums: Boolean,
        includeFiles: Boolean,
        includeImages: Boolean,
        includeVideos: Boolean,
        includeGifs: Boolean,
        includeUnhidden: Boolean,
        includeHidden: Boolean,
        sortingOrder: SortOrder,
        descendSorting: Boolean,
        placeOnTop: MediaItemsRepo.PlaceOnTop
    ): Flow<Resource<List<MediaItemData>>> {
        return combine(_albumsFlow, _loadingFlow) { albums, loading ->
            //search mode
            val itemsList = if (searchQuery != null) {
                albums.getPlainSearchResult(
                    query = searchQuery,
                    baseDirectory = path
                )
            }
            //directory content mode
            else when (treeMode) {
                true -> albums.filterDirectoryContent(directoryPath = path ?: galleryRootPath)
                false -> when (path == null) {
                    true -> albums.filterFlatListOfAllGalleryNotEmptyAlbums()
                    false -> albums.filterAlbumOwnFilesList(albumPath = path)
                }
            }.applyItemTypeFilters(
                includeAlbums = includeAlbums,
                includeFiles = includeFiles
            ).applyVisibilityFilters(
                includeUnhidden = includeUnhidden,
                includeHidden = includeHidden
            ).applyMediaTypeFilters(
                includeImages = includeImages,
                includeVideos = includeVideos,
                includeGifs = includeGifs
            ).applySorting(
                order = sortingOrder,
                descend = descendSorting,
                albumsFirst = placeOnTop == MediaItemsRepo.PlaceOnTop.ALBUMS_ON_TOP,
                filesFirst = placeOnTop == MediaItemsRepo.PlaceOnTop.FILES_ON_TOP
            )
            
            when (loading) {
                true -> Resource.Loading(itemsList)
                false -> Resource.Success(itemsList)
            }
        }
    }
    
    
    override fun getAlbumContentFlow(
        path: String?,
        searchQuery: String?,
        treeMode: Boolean,
        includeAlbums: Boolean,
        includeFiles: Boolean,
        includeImages: Boolean,
        includeVideos: Boolean,
        includeGifs: Boolean,
        includeUnhidden: Boolean,
        includeHidden: Boolean,
        sortingOrder: SortOrder,
        descendSorting: Boolean,
        filesFirst: Boolean,
        albumsFirst: Boolean
    ): Flow<Resource<List<MediaItemData>>> {
        return combine(_albumsFlow, _loadingFlow) { albums, loading ->
            val rawData = when (treeMode) {
                true -> albums.filterDirectoryContent(
                    directoryPath = path ?: galleryRootPath
                )
                false -> when (path == null) {
                    true -> albums.filterFlatListOfAllGalleryNotEmptyAlbums()
                    false -> albums.filterAlbumOwnFilesList(albumPath = path)
                }
            }
            
            val filteredData = rawData
                .applyItemTypeFilters(
                    includeAlbums = includeAlbums,
                    includeFiles = includeFiles
                )
                .applyVisibilityFilters(
                    includeUnhidden = includeUnhidden,
                    includeHidden = includeHidden
                )
                .applyMediaTypeFilters(
                    includeImages = includeImages,
                    includeVideos = includeVideos,
                    includeGifs = includeGifs
                )
            val sortedData = filteredData.applySorting(
                order = sortingOrder,
                descend = descendSorting,
                albumsFirst = albumsFirst,
                filesFirst = filesFirst
            )
            
            when (loading) {
                true -> Resource.Loading(sortedData)
                false -> Resource.Success(sortedData)
            }
        }
    }
    
    override fun getSearchResultFlow(
        query: String,
        basePath: String?,
        includeAlbums: Boolean,
        includeFiles: Boolean,
        includeImages: Boolean,
        includeVideos: Boolean,
        includeGifs: Boolean,
        includeUnhidden: Boolean,
        includeHidden: Boolean,
        sortingOrder: SortOrder,
        descendSorting: Boolean,
        filesFirst: Boolean,
        albumsFirst: Boolean
    ): Flow<Resource<List<MediaItemData>>> {
        return combine(_albumsFlow, _loadingFlow) { galleryAlbums, loading ->
            val foundAlbums = galleryAlbums
                .filter { album -> album.path.contains(query) }
            val foundFiles = galleryAlbums
                .map { it.files }
                .flatten()
                .filter { file -> file.path.contains(query) }
                .filterNot { foundAlbums.map { it.files }.flatten().contains(it) }
            
            val rawData = when (basePath != null) {
                true -> (foundAlbums + foundFiles).filter { it.path.startsWith(basePath) }
                false -> foundAlbums + foundFiles
            }
            val filteredData = rawData
                .applyItemTypeFilters(
                    includeAlbums = includeAlbums,
                    includeFiles = includeFiles
                )
                .applyVisibilityFilters(
                    includeUnhidden = includeUnhidden,
                    includeHidden = includeHidden
                )
                .applyMediaTypeFilters(
                    includeImages = includeImages,
                    includeVideos = includeVideos,
                    includeGifs = includeGifs
                )
            val sortedData = filteredData.applySorting(
                order = sortingOrder,
                descend = descendSorting,
                albumsFirst = albumsFirst,
                filesFirst = filesFirst
            )
            
            when (loading) {
                true -> Resource.Loading(sortedData)
                false -> Resource.Success(sortedData)
            }
        }
    }
    
    override suspend fun rescan() {
        val startTime = System.currentTimeMillis()
        Timber.d("Rescan initiated")
        _loadingFlow.update { true }
        withContext(Dispatchers.IO) {
            val galleryData = mediastoreDs
                .getImagesAndVideos()
                .createGalleryAlbumsList()
            withContext(Dispatchers.Main) {
                _albumsFlow.update { galleryData }
                _loadingFlow.update { false }
            }
        }
        val endTime = System.currentTimeMillis()
        Timber.d("Rescan finished. Took ${endTime - startTime} ms")
    }
    
    override suspend fun checkExistence(filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            FilesystemUtils.checkExistence(filePath)
        }
    }
    
    override suspend fun checkWriteAccess(directoryPath: String): Boolean {
        return FilesystemUtils.checkWriteAccess(directoryPath)
    }
    
    override suspend fun executeFileOperations(operations: List<FileOperation>): LiveData<List<WorkInfo>> {
        return withContext(Dispatchers.IO) {
            val workManager = WorkManager.getInstance(context)
            val workTag = System.currentTimeMillis().toString()
            val requests = operations.map {
                OneTimeWorkRequestBuilder<FileOperationWorker>()
                    .setInputData(
                        workDataOf(
                            FileOperationWorker.Keys.FILE_OPERATION_JSON.name to Json.encodeToString(it)
                        )
                    ).addTag(workTag)
                    .build()
            }
            requests.forEach { workManager.enqueue(it) }
            workManager.getWorkInfosByTagLiveData(workTag)
        }
    }
    
    
    companion object {
        
        @VisibleForTesting
        internal fun List<MediaItemData.Album>.getPlainSearchResult(
            query: String,
            baseDirectory: String? = null
        ): List<MediaItemData> {
            val foundAlbums = this
                .filter { album -> album.path.contains(query, ignoreCase = true) }
            val foundFiles = this
                .map { it.files }
                .flatten()
                .filter { file -> file.path.contains(query) }
                .filterNot { foundAlbums.map { it.files }.flatten().contains(it) }
            
            return when (baseDirectory != null) {
                true -> (foundAlbums + foundFiles).filter { it.path.startsWith(baseDirectory) }
                false -> foundAlbums + foundFiles
            }
        }
        
        @VisibleForTesting
        internal fun List<MediaItemData.Album>.filterDirectoryContent(
            directoryPath: String
        ): List<MediaItemData> {
            val files = filterAlbumOwnFilesList(directoryPath)
            val albums = this.filter {
                Path(it.path).parent?.pathString == directoryPath
            }
            return files + albums
        }
        
        @VisibleForTesting
        internal fun List<MediaItemData.Album>.filterFlatListOfAllGalleryNotEmptyAlbums(): List<MediaItemData.Album> {
            return this.filter { it.files.isNotEmpty() }
        }
        
        @VisibleForTesting
        internal fun List<MediaItemData.Album>.filterAlbumOwnFilesList(
            albumPath: String
        ): List<MediaItemData.File> {
            return this.find { it.path == albumPath }?.files ?: emptyList()
        }
        
        //convert plain list of files to plain list of albums.
        // Result list contains empty intermediate albums for search and navigation purposes
        @VisibleForTesting
        internal fun List<MediaItemData.File>.createGalleryAlbumsList(): List<MediaItemData.Album> {
            val albums = mutableMapOf<String, MediaItemData.Album>()
            
            //create empty albums from path
            this
                .groupBy { Path(it.path).parent.pathString }
                .forEach { albumGroup ->
                    albums.put(
                        albumGroup.key,
                        MediaItemData.Album(
                            path = albumGroup.key,
                            files = albumGroup.value
                        )
                    )
                    val parentDirectories = mutableListOf<String>()
                    var node = Path(albumGroup.key).parent.pathString
                    while (node != "null") {
                        parentDirectories.add(node)
                        node = Path(node).parent?.toString() ?: "null"
                    }
                    parentDirectories.forEach { albumPath ->
                        albums.put(
                            albumPath,
                            MediaItemData.Album(path = albumPath)
                        )
                    }
                }
            
            //fill albums with metadata
            albums.values.forEach { album ->
                val albumOwnFiles = album.files
                //files placed in nested albums
                val albumDeepFiles = this.fastFilter { it.path.startsWith(album.path) } - albumOwnFiles
                //size of whole directory (including nested directories)
                val albumSize = (albumOwnFiles + albumDeepFiles).sumOf { it.size }
                //count of nested images (include placed nested albums)
                val imagesCount = (albumOwnFiles + albumDeepFiles).count { it.mediaType == MediaItemData.File.Type.IMAGE }
                val videosCount = (albumOwnFiles + albumDeepFiles).count { it.mediaType == MediaItemData.File.Type.VIDEO }
                val gifsCount = (albumOwnFiles + albumDeepFiles).count { it.mediaType == MediaItemData.File.Type.GIF }
                //count of child deep albums
                val nestedAlbumsCount = albums.values.count { it.path.startsWith(album.path) && it.path != album.path }
                val hiddenCount = (albumOwnFiles + albumDeepFiles).count { it.isHidden }
                val unhiddenCount = (albumOwnFiles + albumDeepFiles).count { !it.isHidden }
                val creationDate = (albumOwnFiles + albumDeepFiles).minOfOrNull { it.dateCreated } ?: 0
                val modificationDate = (albumOwnFiles + albumDeepFiles).maxOfOrNull { it.dateModified } ?: 0
                val thumbnail = albumOwnFiles.firstOrNull()?.path
                //hidden children excluded from thumbnail calculation for "safety reasons"
                    ?: albumDeepFiles.filterNot { it.isHidden }.firstOrNull()?.path
                    ?: ""
                
                albums.put(
                    album.path,
                    MediaItemData.Album(
                        path = album.path,
                        files = albumOwnFiles,
                        size = albumSize,
                        imagesCount = imagesCount,
                        videosCount = videosCount,
                        gifsCount = gifsCount,
                        hiddenCount = hiddenCount,
                        unhiddenCount = unhiddenCount,
                        dateCreated = creationDate,
                        dateModified = modificationDate,
                        nestedDirectoriesCount = nestedAlbumsCount,
                        thumbnail = thumbnail
                    )
                )
            }
            return albums.values.toList()
        }
        
        
        @VisibleForTesting
        internal fun List<MediaItemData>.applyMediaTypeFilters(
            includeImages: Boolean,
            includeVideos: Boolean,
            includeGifs: Boolean,
        ): List<MediaItemData> {
            val files = this.filterIsInstance<MediaItemData.File>()
            val albums = this.filterIsInstance<MediaItemData.Album>()
            
            val filteredFiles = files.filterFilesByMediaType(includeImages, includeVideos, includeGifs)
            val filteredAlbums = albums.filterAlbumsByMediaType(includeImages, includeVideos, includeGifs)
            
            return filteredFiles + filteredAlbums
        }
        
        @VisibleForTesting
        internal fun List<MediaItemData>.applyVisibilityFilters(
            includeUnhidden: Boolean,
            includeHidden: Boolean,
        ): List<MediaItemData> {
            val files = this.filterIsInstance<MediaItemData.File>()
            val albums = this.filterIsInstance<MediaItemData.Album>()
            
            val filteredFiles = files.filterFilesByVisibility(includeUnhidden, includeHidden)
            val filteredAlbums = albums.filterAlbumsByVisibility(includeUnhidden, includeHidden)
            
            return filteredFiles + filteredAlbums
        }
        
        @VisibleForTesting
        internal fun List<MediaItemData>.applyItemTypeFilters(
            includeAlbums: Boolean,
            includeFiles: Boolean
        ): List<MediaItemData> {
            val filesFiltered = if (includeFiles) this.filterIsInstance<MediaItemData.File>() else emptyList()
            val albumsFiltered = if (includeAlbums) this.filterIsInstance<MediaItemData.Album>() else emptyList()
            
            return (filesFiltered + albumsFiltered).distinct()
        }
        
        private fun List<MediaItemData.File>.filterFilesByMediaType(
            includeImages: Boolean,
            includeVideos: Boolean,
            includeGifs: Boolean,
        ): List<MediaItemData.File> {
            val imageFiltered = if (includeImages) this.filter { it.mediaType == MediaItemData.File.Type.IMAGE } else emptyList()
            val videoFiltered = if (includeVideos) this.filter { it.mediaType == MediaItemData.File.Type.VIDEO } else emptyList()
            val gifFiltered = if (includeGifs) this.filter { it.mediaType == MediaItemData.File.Type.GIF } else emptyList()
            
            return (imageFiltered + videoFiltered + gifFiltered).distinct()
        }
        
        private fun List<MediaItemData.Album>.filterAlbumsByMediaType(
            includeImages: Boolean,
            includeVideos: Boolean,
            includeGifs: Boolean,
        ): List<MediaItemData.Album> {
            val imageFiltered = if (includeImages) this.filter { it.imagesCount > 0 } else emptyList()
            val videoFiltered = if (includeVideos) this.filter { it.videosCount > 0 } else emptyList()
            val gifFiltered = if (includeGifs) this.filter { it.gifsCount > 0 } else emptyList()
            
            return (imageFiltered + videoFiltered + gifFiltered).distinct()
        }
        
        private fun List<MediaItemData.File>.filterFilesByVisibility(
            includeUnhidden: Boolean,
            includeHidden: Boolean
        ): List<MediaItemData.File> {
            val unhidden = if (includeUnhidden) this.filterNot { it.isHidden } else emptyList()
            val hidden = if (includeHidden) this.filter { it.isHidden } else emptyList()
            
            return (unhidden + hidden).distinct()
        }
        
        private fun List<MediaItemData.Album>.filterAlbumsByVisibility(
            includeUnhidden: Boolean,
            includeHidden: Boolean
        ): List<MediaItemData.Album> {
            val unhidden = if (includeUnhidden) this.filter { !it.isHidden && it.unhiddenCount > 0 } else emptyList()
            val hidden = if (includeHidden) this.filter { it.isHidden && it.hiddenCount > 0 } else emptyList()
            return (unhidden + hidden).distinct()
        }
        
        
        @VisibleForTesting
        internal fun List<MediaItemData>.applySorting(
            order: SortOrder,
            descend: Boolean,
            albumsFirst: Boolean,
            filesFirst: Boolean
        ): List<MediaItemData> {
            return when (order) {
                SortOrder.CREATION_DATE -> this.sortedBy { it.dateCreated }
                SortOrder.MODIFICATION_DATE -> this.sortedBy { it.dateModified }
                SortOrder.NAME -> this.sortedBy { it.name }
                SortOrder.SIZE -> this.sortedBy { it.size }
                SortOrder.EXTENSION -> this.sortByExtension()
                SortOrder.RANDOM -> this.shuffled()
            }.let {
                if (descend) it.reversed() else it
            }.let {
                if (albumsFirst) it.placeAlbumsFirst()
                else if (filesFirst) it.placeFilesFirst()
                else it
            }
        }
        
        private fun List<MediaItemData>.sortByExtension(): List<MediaItemData> {
            val files = filterIsInstance<MediaItemData.File>()
            return files.sortedBy { it.extension } + (this - files)
        }
        
        private fun List<MediaItemData>.placeAlbumsFirst(): List<MediaItemData> {
            return filterIsInstance<MediaItemData.Album>() + filterIsInstance<MediaItemData.File>()
        }
        
        private fun List<MediaItemData>.placeFilesFirst(): List<MediaItemData> {
            return filterIsInstance<MediaItemData.File>() + filterIsInstance<MediaItemData.Album>()
        }
    }
}
