package mtree.core.ui.models

import mtree.core.domain.models.ConflictResolutionDomain
import mtree.core.domain.models.NewConflictResolutionDomain


data class ConflictResolutionUi(
    val type: Type,
    val applyToAll: Boolean = false
) {
    companion object {
        val default = ConflictResolutionUi(
            type = Type.ADD_SUFFIX_TO_NEW_FILE_NAME,
            applyToAll = false
        )
    }
    
    enum class Type {
        SKIP_FILE,
        ADD_SUFFIX_TO_NEW_FILE_NAME,
        OVERWRITE_OLD_FILE
    }
    
    fun mapToDomain(): ConflictResolutionDomain {
        return when (type) {
            Type.SKIP_FILE -> ConflictResolutionDomain.SKIP
            Type.ADD_SUFFIX_TO_NEW_FILE_NAME -> ConflictResolutionDomain.KEEP_BOTH
            Type.OVERWRITE_OLD_FILE -> ConflictResolutionDomain.OVERWRITE
        }
    }
    
    fun mapToNewDomain(): NewConflictResolutionDomain {
        return NewConflictResolutionDomain(
            type = when (type) {
                Type.SKIP_FILE -> NewConflictResolutionDomain.Type.SKIP
                Type.ADD_SUFFIX_TO_NEW_FILE_NAME -> NewConflictResolutionDomain.Type.KEEP_BOTH
                Type.OVERWRITE_OLD_FILE -> NewConflictResolutionDomain.Type.OVERWRITE
            },
            applyToAll = applyToAll
        )
    }
}
