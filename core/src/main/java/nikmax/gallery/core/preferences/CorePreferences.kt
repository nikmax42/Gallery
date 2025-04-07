package nikmax.gallery.core.preferences

import kotlinx.serialization.Serializable

@Serializable
data class CorePreferences(
    val theme: Theme = Theme.SYSTEM,
    val dynamicColors: DynamicColors = DynamicColors.SYSTEM,
) {
    enum class Theme { SYSTEM, LIGHT, DARK }
    enum class DynamicColors { SYSTEM, DISABLED }
}
