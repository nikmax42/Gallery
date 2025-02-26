package nikmax.gallery.core

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionsUtils {

    enum class PermissionStatus {
        GRANTED, DENIED
    }


    fun checkPermission(permission: String, context: Context): PermissionStatus {
        return when (permission) {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> checkManageExternalStorage()
            else -> {
                when (val code = ContextCompat.checkSelfPermission(context, permission)) {
                    PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
                    else -> PermissionStatus.DENIED
                }
            }
        }
    }

    fun requestPermission(permission: String, context: Context) {
        when (permission) {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> requestManageExternalStorage(context)
            else -> {}
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
                }
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            )
        }
    }
}
