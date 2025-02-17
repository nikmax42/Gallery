package nikmax.gallery.data.media

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import nikmax.gallery.data.Resource
import java.io.File

interface MediaItemsRepo {

    /**
     * @return flow of [Resource] of [MediaFileData].
     */
    fun getFilesFlow(): Flow<Resource<List<MediaFileData>>>

    /**
     * Use mediastore to update files flow with all available media files.
     * @return nothing
     */
    suspend fun rescan()

    suspend fun copyFile(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File>

    suspend fun moveFile(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File>

    suspend fun renameFile(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File>

    suspend fun deleteFile(filePath: String): Result<File>
}


internal class MediaItemRepoImpl(
    private val context: Context
) : MediaItemsRepo {

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
            val media = MediastoreUtils.getAllImagesAndVideos(context)
            withContext(Dispatchers.Main) {
                _filesFlow.update { media }
                _loadingFlow.update { false }
            }
        }
    }

    override suspend fun copyFile(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File> {
        val result = FilesUtils.copy(sourceFilePath, destinationFilePath, conflictResolution)
        rescan()
        return result
    }

    override suspend fun moveFile(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File> {
        val result = FilesUtils.move(sourceFilePath, destinationFilePath, conflictResolution)
        rescan()
        return result
    }

    override suspend fun renameFile(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File> {
        val result = FilesUtils.rename(sourceFilePath, destinationFilePath, conflictResolution)
        rescan()
        return result
    }

    override suspend fun deleteFile(filePath: String): Result<File> {
        val result = FilesUtils.delete(filePath)
        rescan()
        return result
    }
}
