package mtree.core.ui.models

import mtree.core.domain.models.ConflictResolutionDomain


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
        return ConflictResolutionDomain(
            type = when (type) {
                Type.SKIP_FILE -> ConflictResolutionDomain.Type.SKIP
                Type.ADD_SUFFIX_TO_NEW_FILE_NAME -> ConflictResolutionDomain.Type.KEEP_BOTH
                Type.OVERWRITE_OLD_FILE -> ConflictResolutionDomain.Type.OVERWRITE
            },
            applyToAll = applyToAll
        )
    }
    
    fun mapToNewDomain(): ConflictResolutionDomain {
        return ConflictResolutionDomain(
            type = when (type) {
                Type.SKIP_FILE -> ConflictResolutionDomain.Type.SKIP
                Type.ADD_SUFFIX_TO_NEW_FILE_NAME -> ConflictResolutionDomain.Type.KEEP_BOTH
                Type.OVERWRITE_OLD_FILE -> ConflictResolutionDomain.Type.OVERWRITE
            },
            applyToAll = applyToAll
        )
    }
}
