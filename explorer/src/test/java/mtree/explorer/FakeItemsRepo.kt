package mtree.explorer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import mtree.core.data.MediaItemData
import mtree.core.data.MediaItemsRepo
import mtree.core.data.Resource

internal class FakeItemsRepo : MediaItemsRepo {
    
    private val _isLoading = MutableStateFlow(false)
    private val _albums = MutableStateFlow(FakeItems.fakeAlbumsData)
    
    override fun getMediaAlbumsFlow(): Flow<Resource<List<MediaItemData>>> {
        return combine(_albums, _isLoading) { albums, loading ->
            when (loading) {
                true -> Resource.Loading(albums)
                false -> Resource.Success(albums)
            }
        }
    }
    
    override suspend fun rescan() {
        _isLoading.update { true }
        delay(1000)
        _isLoading.update { false }
    }
}
