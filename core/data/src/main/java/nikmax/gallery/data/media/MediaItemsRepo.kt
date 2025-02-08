package nikmax.gallery.data.media

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import nikmax.gallery.data.Resource
import kotlin.io.path.Path
import kotlin.io.path.pathString

interface MediaItemsRepo {
    /**
     * Returns a [Resource] [Flow] with new list of [MediaFileData] filtered by the given [path]:
     *  - if [path] is null, returns all files
     *  - if [path] is not null, returns:
     *    - the file at [path] if it exists
     *    - the direct children of [path] if the file at [path] does not exist
     */
    fun getFilesPlacedOnPath(
        path: String? = null
    ): Flow<Resource<List<MediaFileData>>>

    /**
     * Returns a [Resource] [Flow] with list of [MediaFileData] which paths contains given path
     * or empty list if nothing found
     */
    fun getFilesContainsPath(path: String): Flow<Resource<List<MediaFileData>>>

    fun getFilesFlow(): Flow<Resource<List<MediaFileData>>>

    suspend fun rescan()
}


internal class MediaItemRepoImpl(private val mediaStoreDs: MediaStoreDs) : MediaItemsRepo {

    private val _filesFlow = MutableStateFlow<List<MediaFileData>>(emptyList())
    private val _loadingFlow = MutableStateFlow(false)

    override fun getFilesFlow(): Flow<Resource<List<MediaFileData>>> {
        return combine(_filesFlow, _loadingFlow) { files, loading ->
            when (loading) {
                true -> Resource.Loading(files)
                false -> Resource.Success(files)
            }
        }
    }

    override suspend fun rescan() {
        _loadingFlow.update { true }
        withContext(Dispatchers.IO) {
            mediaStoreDs.rescanFiles()
        }
        _filesFlow.update { mediaStoreDs.getFiles() }
        _loadingFlow.update { false }
    }

    override fun getFilesPlacedOnPath(path: String?): Flow<Resource<List<MediaFileData>>> {
        return combine(_filesFlow, _loadingFlow) { files, loading ->
            val filteredFiles = when (path == null) {
                true -> files
                false -> {
                    val fileOnPath = files.find { it.path == path }
                    val pathDirectChildren =
                        files.filter { Path(it.path).parent.pathString == path }
                    when (fileOnPath != null) {
                        true -> listOf(fileOnPath)
                        false -> pathDirectChildren
                    }
                }
            }
            when (loading) {
                true -> Resource.Loading(filteredFiles)
                false -> Resource.Success(filteredFiles)
            }
        }
    }

    override fun getFilesContainsPath(path: String): Flow<Resource<List<MediaFileData>>> {
        return combine(_filesFlow, _loadingFlow) { files, loading ->
            val filteredFiles = files.filter { it.path.contains(path) }
            when (loading) {
                true -> Resource.Loading(filteredFiles)
                false -> Resource.Success(filteredFiles)
            }
        }
    }
}
