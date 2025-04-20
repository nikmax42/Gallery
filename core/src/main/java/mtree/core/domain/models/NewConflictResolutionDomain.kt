package mtree.core.domain.models

import kotlinx.serialization.Serializable
import mtree.core.ui.models.ConflictResolutionUi

@Serializable
data class NewConflictResolutionDomain(
    val type: Type,
    val applyToAll: Boolean
) {
    enum class Type {
        KEEP_BOTH,
        SKIP,
        OVERWRITE
    }
    
    companion object {
        fun default(): NewConflictResolutionDomain {
            return NewConflictResolutionDomain(
                type = Type.KEEP_BOTH,
                applyToAll = false
            )
        }
    }
    
    fun mapToUi(): ConflictResolutionUi {
        return ConflictResolutionUi(
            type = when (type) {
                Type.KEEP_BOTH -> ConflictResolutionUi.Type.ADD_SUFFIX_TO_NEW_FILE_NAME
                Type.SKIP -> ConflictResolutionUi.Type.SKIP_FILE
                Type.OVERWRITE -> ConflictResolutionUi.Type.OVERWRITE_OLD_FILE
            },
            applyToAll = applyToAll
        )
    }
}
