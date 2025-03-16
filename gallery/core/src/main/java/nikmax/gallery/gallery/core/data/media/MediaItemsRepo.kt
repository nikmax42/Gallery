package nikmax.gallery.gallery.core.data.media

import android.content.Context
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

interface MediaItemsRepo {

    /**
     * @return flow of [Resource] of [MediaFileData].
     */
    fun getFilesResourceFlow(): Flow<Resource<List<MediaFileData>>>

    /**
     * Use mediastore to update files flow with all available media files.
     */
    suspend fun rescan()

    suspend fun checkExistence(filePath: String): Boolean

    suspend fun executeFileOperations(operations: List<FileOperation>): LiveData<List<WorkInfo>>
}


internal class MediaItemRepoImpl(
    private val context: Context
) : MediaItemsRepo {

    private val _filesFlow = MutableStateFlow<List<MediaFileData>>(emptyList())
    private val _loadingFlow = MutableStateFlow(false)

    override fun getFilesResourceFlow(): Flow<Resource<List<MediaFileData>>> {
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

    override suspend fun checkExistence(filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            FilesUtils.checkExistence(filePath)
        }
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
}
