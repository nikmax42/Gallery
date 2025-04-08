package nikmax.mtree.core.utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Environment
import android.provider.Settings

object PermissionsUtils {

    enum class AppPermissions(val key: String) {
        MANAGE_EXTERNAL_STORAGE(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
        // NOTIFICATIONS(Manifest.permission.POST_NOTIFICATIONS)
    }

    enum class PermissionStatus {
        GRANTED, DENIED
    }


    fun checkPermission(permission: AppPermissions, context: Context): PermissionStatus {
        return when (permission) {
            AppPermissions.MANAGE_EXTERNAL_STORAGE -> checkManageExternalStorage()
            /* else -> {
                when (val code = ContextCompat.checkSelfPermission(context, permission.key)) {
                    PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
                    else -> PermissionStatus.DENIED
                }
            } */
        }
    }

    fun requestPermission(permission: AppPermissions, context: Context) {
        when (permission) {
            AppPermissions.MANAGE_EXTERNAL_STORAGE -> requestManageExternalStorage(context)
            // else -> {}
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
                    data = Uri.parse("package:${context.packageName}")
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            )
        }
    }
}
