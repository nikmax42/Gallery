package mtree.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import mtree.core.domain.models.GalleryPermission
import mtree.core.domain.models.PermissionStatus

object PermissionsUtils {
    fun checkPermission(permission: GalleryPermission): PermissionStatus {
        return when (permission) {
            GalleryPermission.MANAGE_EXTERNAL_STORAGE -> checkManageExternalStorage()
        }
    }
    
    fun requestPermission(permission: GalleryPermission, context: Context) {
        when (permission) {
            GalleryPermission.MANAGE_EXTERNAL_STORAGE -> requestManageExternalStorage(context)
        }
    }
    
    
    private fun checkManageExternalStorage(): PermissionStatus {
        return when (Environment.isExternalStorageManager()) {
            true -> PermissionStatus.GRANTED
            false -> PermissionStatus.DENIED
        }
    }
    
    private fun requestManageExternalStorage(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    this.data = Uri.parse("package:${context.packageName}")
                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }
        catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            )
        }
    }
}
