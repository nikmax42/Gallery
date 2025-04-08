package nikmax.mtree.gallery.core.utils

import android.content.Context
import android.content.Intent
import nikmax.mtree.gallery.core.ui.MediaItemUI

object SharingUtils {
    fun shareSingleFile(file: MediaItemUI.File, context: Context) {
        /*  val shareLauncher = rememberLauncherForActivityResult(
             contract = ActivityResultContracts.StartActivityForResult()
         ) { result -> } */
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = file.mimetype
                putExtra(Intent.EXTRA_STREAM, file.uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            },
            null
        ).apply {
            // shareLauncher.launch(this)
            context.startActivity(this)
        }
    }
}
