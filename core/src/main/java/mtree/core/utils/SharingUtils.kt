package mtree.core.utils

import android.content.Context
import android.content.Intent
import mtree.core.ui.models.MediaItemUI

object SharingUtils {
    fun shareSingleFile(file: MediaItemUI.File, context: Context) {
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = file.mimetype
                putExtra(Intent.EXTRA_STREAM, file.uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            },
            null
        ).apply {
            context.startActivity(this)
        }
    }
}
