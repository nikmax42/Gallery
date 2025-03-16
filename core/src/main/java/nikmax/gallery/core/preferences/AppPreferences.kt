package nikmax.gallery.core.preferences

import kotlinx.serialization.Serializable

@Serializable
data class AppPreferences(
    val useDynamicColors: Boolean = true
)
