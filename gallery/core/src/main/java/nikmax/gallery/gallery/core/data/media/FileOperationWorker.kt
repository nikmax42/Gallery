package nikmax.gallery.gallery.core.data.media

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json


class FileOperationWorker(
    appContext: Context, params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    enum class Keys { FILE_OPERATION_JSON }

    override suspend fun doWork(): Result {
        val operationJson = inputData.getString(
            Keys.FILE_OPERATION_JSON.name
        ) ?: return Result.failure()
        val operationResult = when (val operation = Json.decodeFromString<FileOperation>(operationJson)) {
            is FileOperation.Copy -> FilesUtils.copy(
                operation.sourceFilePath,
                operation.destinationFilePath,
                operation.conflictResolution
            )
            is FileOperation.Move -> FilesUtils.move(
                operation.sourceFilePath,
                operation.destinationFilePath,
                operation.conflictResolution
            )
            is FileOperation.Rename -> FilesUtils.rename(
                operation.originalFilePath,
                operation.newFilePath,
                operation.conflictResolution
            )
            is FileOperation.Delete -> FilesUtils.delete(operation.filePath)
        }
        val outputData = Data.Builder()
            .putString(Keys.FILE_OPERATION_JSON.name, operationJson)
            .build()
        return when (operationResult.isSuccess) {
            true -> return Result.success(outputData)
            false -> return Result.failure(outputData)
        }
    }
}
