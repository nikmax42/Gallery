package mtree.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface FileOperation {
    
    @Serializable
    data class Copy(
        val sourceFilePath: String,
        val destinationFilePath: String,
        val conflictResolution: ConflictResolutionDomain
    ) : FileOperation
    
    @Serializable
    data class Move(
        val sourceFilePath: String,
        val destinationFilePath: String,
        val conflictResolution: ConflictResolutionDomain
    ) : FileOperation
    
    @Serializable
    data class Rename(
        val originalFilePath: String,
        val newFilePath: String,
        val conflictResolution: ConflictResolutionDomain
    ) : FileOperation
    
    @Serializable
    data class Delete(val filePath: String) : FileOperation
}
