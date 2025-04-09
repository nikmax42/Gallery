package nikmax.mtree.gallery.viewer

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import nikmax.mtree.core.data.Resource
import nikmax.mtree.gallery.core.data.media.FileOperation
import nikmax.mtree.gallery.core.data.media.MediaItemData
import nikmax.mtree.gallery.core.data.media.MediaItemsRepo

class FakeGalleryItemsRepo : MediaItemsRepo {
    
    private val picture1 = MediaItemData.File(path = "/storage/emulated/0/Pictures/1.jpg")
    private val movie1 = MediaItemData.File(path = "/storage/emulated/0/Movies/1.jpg")
    
    private val pictures = MediaItemData.Album(path = "/storage/emulated/0/Pictures", files = listOf(picture1))
    private val movies = MediaItemData.Album(path = "/storage/emulated/0/Movies", files = listOf(movie1))
    
    private val _loadingFlow = MutableStateFlow(false)
    private val _albumsFlow = MutableStateFlow<List<MediaItemData.Album>>(
        listOf(
            pictures,
            movies
        )
    )
    
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
        sortingOrder: MediaItemsRepo.SortOrder,
        descendSorting: Boolean,
        filesFirst: Boolean,
        albumsFirst: Boolean
    ): Flow<Resource<List<MediaItemData>>> {
        return combine(_albumsFlow, _loadingFlow) { albums, loading ->
            val baseList = when (path == null) {
                true -> albums
                false -> albums.filter { it.path.startsWith(basePath) }
            }
            val result = baseList.filter { it.path.contains(query) }
            when (loading) {
                false -> Resource.Success(result)
                true -> Resource.Loading(result)
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
        sortingOrder: MediaItemsRepo.SortOrder,
        descendSorting: Boolean,
        filesFirst: Boolean,
        albumsFirst: Boolean
    ): Flow<Resource<List<MediaItemData>>> {
        return combine(_albumsFlow, _loadingFlow) { albums, loading ->
            val baseList = when (basePath == null) {
                true -> albums
                false -> albums.filter { it.path.startsWith(basePath) }
            }
            val result = baseList.filter { it.path.contains(query) }
            when (loading) {
                false -> Resource.Success(result)
                true -> Resource.Loading(result)
            }
        }
    }
    
    override suspend fun rescan() {
        _loadingFlow.update { true }
        delay(3000)
        _loadingFlow.update { false }
    }
    
    override suspend fun checkExistence(filePath: String): Boolean {
        val items = _albumsFlow.value + _albumsFlow.value.map { it.files }.flatten()
        return items.any { it.path == filePath }
    }
    
    override suspend fun checkWriteAccess(directoryPath: String): Boolean {
        TODO("Not yet implemented")
    }
    
    override suspend fun executeFileOperations(operations: List<FileOperation>): LiveData<List<WorkInfo>> {
        TODO("Not yet implemented")
    }
}
