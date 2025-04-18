package mtree.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dagger.hilt.android.AndroidEntryPoint
import mtree.core.domain.models.GalleryPermission
import mtree.core.domain.models.PermissionStatus
import mtree.core.preferences.MtreePreferences
import mtree.core.preferences.MtreePreferencesUtils
import mtree.core.ui.theme.GalleryTheme
import mtree.core.utils.PermissionsUtils
import mtree.permission_request.StoragePermissionRequestScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appPrefs = MtreePreferencesUtils
                .getPreferencesFlow(LocalContext.current)
                .collectAsState(MtreePreferences.default())
                .value
            
            val useDarkTheme = when (appPrefs.theme) {
                MtreePreferences.Theme.SYSTEM -> isSystemInDarkTheme()
                MtreePreferences.Theme.LIGHT -> false
                MtreePreferences.Theme.DARK -> true
            }
            val useSystemDynamicColors = when (appPrefs.dynamicColors) {
                MtreePreferences.DynamicColors.SYSTEM -> true
                MtreePreferences.DynamicColors.DISABLED -> false
            }
            
            GalleryTheme(
                darkTheme = useDarkTheme,
                dynamicColor = useSystemDynamicColors
            ) {
                var storagePermissionStatus by remember {
                    mutableStateOf(
                        PermissionsUtils.checkPermission(
                            GalleryPermission.MANAGE_EXTERNAL_STORAGE
                        )
                    )
                }
                
                LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
                    storagePermissionStatus = PermissionsUtils.checkPermission(
                        GalleryPermission.MANAGE_EXTERNAL_STORAGE
                    )
                }
                
                AnimatedContent(targetState = storagePermissionStatus) { storage ->
                    when (storage) {
                        PermissionStatus.GRANTED -> MainNavHost()
                        PermissionStatus.DENIED -> {
                            val context = LocalContext.current
                            StoragePermissionRequestScreen(
                                onGrantClick = {
                                    PermissionsUtils.requestPermission(
                                        permission = GalleryPermission.MANAGE_EXTERNAL_STORAGE,
                                        context = context
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
