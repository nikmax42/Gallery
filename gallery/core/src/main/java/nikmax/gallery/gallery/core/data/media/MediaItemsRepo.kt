package nikmax.gallery.gallery.core.data.media

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
import nikmax.gallery.core.data.Resource
import timber.log.Timber
import kotlin.io.path.Path
import kotlin.io.path.pathString

interface MediaItemsRepo {
    
    fun getAlbumContentFlow(
        path: String?,
        searchQuery: String?,
        treeMode: Boolean = true
    ): Flow<Resource<List<MediaItemData>>>
    
    fun getSearchResultFlow(
        query: String,
        basePath: String? = null
    ): Flow<Resource<List<MediaItemData>>>
    
    /**
     * Use mediastore to update files flow with all available media files.
     */
    suspend fun rescan()
    
    suspend fun checkExistence(filePath: String): Boolean
    
    suspend fun checkWriteAccess(directoryPath: String): Boolean
    
    suspend fun executeFileOperations(operations: List<FileOperation>): LiveData<List<WorkInfo>>
}


internal class MediaItemRepoImpl(
    private val context: Context
) : MediaItemsRepo {
    
    private val galleryRootPath = "/storage"
    
    private val _loadingFlow = MutableStateFlow(false)
    private val _albumsFlow = MutableStateFlow<List<MediaItemData.Album>>(emptyList())
    
    override fun getAlbumContentFlow(
        path: String?,
        searchQuery: String?,
        treeMode: Boolean
    ): Flow<Resource<List<MediaItemData>>> {
        return combine(_albumsFlow, _loadingFlow) { albums, loading ->
            val data = when (treeMode) {
                true -> getDirectoryContent(
                    directoryPath = path ?: galleryRootPath,
                    galleryAlbums = albums
                )
                false -> when (path == null) {
                    true -> getFlatListOfAllGalleryNotEmptyAlbums(galleryAlbums = albums)
                    false -> getAlbumOwnFilesList(
                        albumPath = path,
                        galleryAlbums = albums
                    )
                }
            }
            when (loading) {
                true -> Resource.Loading(data)
                false -> Resource.Success(data)
            }
        }
    }
    
    override fun getSearchResultFlow(
        query: String,
        basePath: String?
    ): Flow<Resource<List<MediaItemData>>> {
        return combine(_albumsFlow, _loadingFlow) { galleryAlbums, loading ->
            val foundAlbums = galleryAlbums
                .filter { album -> album.path.contains(query) }
            val foundFiles = galleryAlbums
                .map { it.files }
                .flatten()
                .filter { file -> file.path.contains(query) }
                .filterNot { foundAlbums.map { it.files }.flatten().contains(it) }
            
            val data = when (basePath != null) {
                true -> (foundAlbums + foundFiles).filter { it.path.startsWith(basePath) }
                false -> foundAlbums + foundFiles
            }
            when (loading) {
                true -> Resource.Loading(data)
                false -> Resource.Success(data)
            }
        }
    }
    
    override suspend fun rescan() {
        val startTime = System.currentTimeMillis()
        Timber.d("Rescan initiated")
        _loadingFlow.update { true }
        withContext(Dispatchers.IO) {
            val galleryData = MediastoreUtils
                .getAllImagesAndVideos(context)
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
        internal fun getDirectoryContent(
            directoryPath: String,
            galleryAlbums: List<MediaItemData.Album>
        ): List<MediaItemData> {
            val files = getAlbumOwnFilesList(directoryPath, galleryAlbums)
            val albums = galleryAlbums.filter {
                Path(it.path).parent?.pathString == directoryPath
            }
            return files + albums
        }
        
        @VisibleForTesting
        internal fun getFlatListOfAllGalleryNotEmptyAlbums(
            galleryAlbums: List<MediaItemData.Album>
        ): List<MediaItemData.Album> {
            return galleryAlbums.filter { it.files.isNotEmpty() }
        }
        
        @VisibleForTesting
        internal fun getAlbumOwnFilesList(
            albumPath: String,
            galleryAlbums: List<MediaItemData.Album>
        ): List<MediaItemData.File> {
            return galleryAlbums.find { it.path == albumPath }?.files ?: emptyList()
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
                val creationDate = (albumOwnFiles + albumDeepFiles).minOfOrNull { it.dateCreated } ?: 0
                val modificationDate = (albumOwnFiles + albumDeepFiles).maxOfOrNull { it.dateModified } ?: 0
                val thumbnail = albumOwnFiles.firstOrNull()?.path
                    ?: albumDeepFiles.filterNot { it.isHidden }.firstOrNull()?.path //hidden excluded for "safety"
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
                        nestedDirectoriesCount = nestedAlbumsCount,
                        dateCreated = creationDate,
                        dateModified = modificationDate,
                        thumbnail = thumbnail
                    )
                )
            }
            return albums.values.toList()
        }
    }
}
