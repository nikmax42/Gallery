package nikmax.mtree.gallery.explorer

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

class FakeMediaItemsRepo : MediaItemsRepo {
    
    companion object {
        val picture1 = MediaItemData.File(
            path = "/storage/emulated/0/Pictures/1.jpg",
        )
        val picture2 = MediaItemData.File(
            path = "/storage/emulated/0/Pictures/2.jpg",
        )
    }
    
    
    private val _loading = MutableStateFlow(false)
    private val _data = MutableStateFlow<List<MediaItemData>>(
        listOf(
            picture1,
            picture2
        )
    )
    
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
        sortingOrder: MediaItemsRepo.SortOrder,
        descendSorting: Boolean,
        placeOnTop: MediaItemsRepo.PlaceOnTop
    ): Flow<Resource<List<MediaItemData>>> {
        return combine(_data, _loading) { data, loading ->
            when (loading) {
                true -> Resource.Loading(data)
                false -> Resource.Success(data)
            }
        }
    }
    
    override suspend fun rescan() {
        _loading.update { true }
        delay(1000)
        _loading.update { false }
    }
    
    override suspend fun checkExistence(filePath: String): Boolean {
        TODO("Not yet implemented")
    }
    
    override suspend fun checkWriteAccess(directoryPath: String): Boolean {
        TODO("Not yet implemented")
    }
    
    override suspend fun executeFileOperations(operations: List<FileOperation>): LiveData<List<WorkInfo>> {
        TODO("Not yet implemented")
    }
}
