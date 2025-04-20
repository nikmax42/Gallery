package mtree.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface NewFileOperation {
    
    @Serializable
    data class Copy(
        val sourceFilePath: String,
        val destinationFilePath: String,
        val conflictResolution: NewConflictResolutionDomain
    ) : NewFileOperation
    
    @Serializable
    data class Move(
        val sourceFilePath: String,
        val destinationFilePath: String,
        val conflictResolution: NewConflictResolutionDomain
    ) : NewFileOperation
    
    @Serializable
    data class Rename(
        val originalFilePath: String,
        val newFilePath: String,
        val conflictResolution: NewConflictResolutionDomain
    ) : NewFileOperation
    
    @Serializable
    data class Delete(val filePath: String) : NewFileOperation
}
