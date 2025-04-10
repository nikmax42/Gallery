package nikmax.mtree.gallery.explorer

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow
import nikmax.mtree.core.data.Resource
import nikmax.mtree.gallery.core.data.media.FileOperation
import nikmax.mtree.gallery.core.data.media.MediaItemData
import nikmax.mtree.gallery.core.data.media.MediaItemsRepo

class FakeMediaItemsRepo : MediaItemsRepo {
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
        TODO("Not yet implemented")
    }
    
    override suspend fun rescan() {
        TODO("Not yet implemented")
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
