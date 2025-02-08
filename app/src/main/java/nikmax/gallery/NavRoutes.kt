package nikmax.gallery

import kotlinx.serialization.Serializable

object NavRoutes {
    @Serializable
    data class Explorer(val albumPath: String? = null)

    @Serializable
    data class Viewer(val filePath: String)
}
