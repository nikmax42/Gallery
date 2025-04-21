package mtree.core.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mtree.core.domain.models.FileOperation

class FileOperationWorker(
    appContext: Context, params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    
    companion object {
        enum class Keys { FILE_OPERATION_JSON }
        
        suspend fun performFileOperationsInBackground(
            operations: List<FileOperation>,
            context: Context,
            onProgressChanged: (succeeded: Int, failed: Int) -> Unit,
            onFinished: () -> Unit
        ) {
            WorkManager.getInstance(context).let { workManager ->
                val workTag = operations.toString()
                val requests = operations.map {
                    OneTimeWorkRequestBuilder<FileOperationWorker>()
                        .setInputData(
                            workDataOf(
                                Keys.FILE_OPERATION_JSON.name to Json.encodeToString(it)
                            )
                        ).addTag(workTag)
                        .build()
                }
                requests.forEach { workManager.enqueue(it) }
                
                workManager
                    .getWorkInfosByTagFlow(workTag)
                    .collect { workInfos ->
                        onProgressChanged(
                            workInfos.count { it.state == WorkInfo.State.SUCCEEDED },
                            workInfos.count { it.state == WorkInfo.State.FAILED }
                        )
                        if (workInfos.all { it.state.isFinished }) {
                            onFinished()
                        }
                    }
            }
        }
    }
    
    
    override suspend fun doWork(): Result {
        val operationJson = inputData.getString(
            Keys.FILE_OPERATION_JSON.name
        ) ?: return Result.failure()
        
        when (val operation = Json.Default.decodeFromString<FileOperation>(operationJson)) {
            is FileOperation.Copy -> FilesystemUtils.copy(
                sourceFilePath = operation.sourceFilePath,
                destinationFilePath = operation.destinationFilePath,
                conflictResolution = operation.conflictResolution
            )
            is FileOperation.Move -> FilesystemUtils.move(
                sourceFilePath = operation.sourceFilePath,
                destinationFilePath = operation.destinationFilePath,
                conflictResolution = operation.conflictResolution
            )
            is FileOperation.Rename -> FilesystemUtils.rename(
                sourceFilePath = operation.originalFilePath,
                destinationFilePath = operation.newFilePath,
                conflictResolution = operation.conflictResolution
            )
            is FileOperation.Delete -> FilesystemUtils.delete(
                filePath = operation.filePath
            )
        }.let { operationResult ->
            val outputData = Data.Builder()
                .putString(Keys.FILE_OPERATION_JSON.name, operationJson)
                .build()
            return when (operationResult.isSuccess) {
                true -> return Result.success(outputData)
                false -> return Result.failure(outputData)
            }
        }
    }
}
