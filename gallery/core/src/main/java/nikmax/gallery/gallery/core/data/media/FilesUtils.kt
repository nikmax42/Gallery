package nikmax.gallery.gallery.core.data.media

import androidx.annotation.VisibleForTesting
import nikmax.gallery.gallery.core.data.media.FilesUtils.copy
import nikmax.gallery.gallery.core.data.media.FilesUtils.delete
import nikmax.gallery.gallery.core.data.media.FilesUtils.move
import nikmax.gallery.gallery.core.data.media.FilesUtils.rename
import java.io.File
import java.nio.file.FileAlreadyExistsException
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

/**
 * A set of utility functions to execute file operations.
 *
 * Provides functions for [copy], [move], [delete], and [rename] files.
 */
internal object FilesUtils {

    fun checkExistence(filePath: String): Boolean {
        return Path(filePath).exists()
    }

    /**
     * Copy a file from one location to another.
     *
     * @param sourceFilePath The original location of the file.
     * @param destinationFilePath The new location where the file will be copied.
     * @param conflictResolution The conflict resolution strategy to use in case of collision.
     * @return A [Result] with the new file if the operation is successful, or an error if the operation fails.
     */
    fun copy(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File> {
        return try {
            val sourceFile = File(sourceFilePath)
            val destinationFile = File(destinationFilePath)
            when (conflictResolution) {
                ConflictResolution.SKIP -> sourceFile.copyRecursively(
                    target = destinationFile,
                    onError = { _, e ->
                        if (e is FileAlreadyExistsException) OnErrorAction.SKIP
                        else throw e
                    }
                )
                ConflictResolution.KEEP_BOTH -> {
                    when (destinationFile.exists()) {
                        true -> copy(
                            sourceFilePath = sourceFilePath,
                            destinationFilePath = addSuffixToFilePath(destinationFilePath),
                            conflictResolution = ConflictResolution.KEEP_BOTH
                        )
                        false -> sourceFile.copyRecursively(target = destinationFile)
                    }
                }
                ConflictResolution.OVERWRITE -> sourceFile.copyRecursively(
                    target = destinationFile,
                    overwrite = true
                )
            }
            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a file.
     *
     * @param filePath The path of the file to delete.
     * @return A [Result] with the deleted file if the operation is successful, or an error if the operation fails.
     */
    fun delete(filePath: String): Result<File> {
        return try {
            val file = File(filePath)
            file.deleteRecursively()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Move a file from one location to another.
     * Uses [copy]+[delete] "under the hood" to increase files safety:
     * source file will be deleted ONLY if copying was successful.
     *
     * @param sourceFilePath The original location of the file.
     * @param destinationFilePath The new location where the file will be moved.
     * @param conflictResolution The conflict resolution strategy to use in case of collision.
     * @return A [Result] with the new file if the operation is successful, or an error if the operation fails.
     */
    fun move(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File> {
        return try {
            val copyResult = copy(sourceFilePath, destinationFilePath, conflictResolution)
            when (copyResult.isSuccess) {
                true -> delete(sourceFilePath)
                false -> Result.failure(copyResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rename a file.
     * Uses [move] "under the hood" to increase files safety:
     * source file will be deleted ONLY if the renamed copy was already created successfully.
     *
     * @param sourceFilePath The original location of the file.
     * @param destinationFilePath The new location where the file will be renamed.
     * @param conflictResolution The conflict resolution strategy to use in case of collision.
     * @return A [Result] with the new file if the operation is successful, or an error if the operation fails.
     */
    fun rename(
        sourceFilePath: String,
        destinationFilePath: String,
        conflictResolution: ConflictResolution
    ): Result<File> {
        return move(sourceFilePath, destinationFilePath, conflictResolution)
    }

    /**
     * Appends a number to the file name to avoid collisions with existing files.
     * E.g. if the file name is `example.txt`, it will be renamed to `example(1).txt`,
     * and if there is already a file with that name, it will be renamed to `example(2).txt`, and so on.
     * @param path The path of the file to be renamed.
     * @return The new path with the added number.
     */
    @VisibleForTesting
    internal fun addSuffixToFilePath(path: String): String {
        val file = Path(path)
        val name = file.nameWithoutExtension
        val extension = file.extension
        val suffixRegex = Regex("\\((\\d+)\\)(?=\\.[^.]+\$)")
        val newFilename = when (suffixRegex.matches(name)) {
            true -> {
                val number = suffixRegex.find(name)!!
                    .groups[1]!!.value
                    .replace("(", "")
                    .replace(")", "")
                    .toInt()
                name.replace("($number)", "(${number + 1})")
            }
            false -> {
                "$name(1)"
            }
        }
        return "${file.parent.pathString}/$newFilename.$extension"
    }
}
