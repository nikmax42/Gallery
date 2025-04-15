package mtree.core.domain.models

import android.Manifest

enum class GalleryPermission(val key: String) {
    MANAGE_EXTERNAL_STORAGE(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
    // NOTIFICATIONS(Manifest.permission.POST_NOTIFICATIONS)
}
