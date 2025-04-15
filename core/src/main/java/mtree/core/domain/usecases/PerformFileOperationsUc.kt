package mtree.core.domain.usecases

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mtree.core.domain.models.FileOperation
import mtree.core.workers.FileOperationWorker

interface PerformFileOperationsUc {
    fun execute(operations: List<FileOperation>)
}


internal class PerformFileOperationsUcImpl(private val context: Context) : PerformFileOperationsUc {
    override fun execute(operations: List<FileOperation>) {
        val workManager = WorkManager.getInstance(context)
        val workTag = operations.toString()
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
        //todo add progress observation
    }
}
