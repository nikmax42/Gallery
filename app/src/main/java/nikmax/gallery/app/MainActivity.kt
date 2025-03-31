package nikmax.gallery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import nikmax.gallery.core.preferences.GalleryPreferences
import nikmax.gallery.core.preferences.GalleryPreferencesUtils
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.gallery.GalleryNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        setContent {
            val context = LocalContext.current
            val appPrefs = GalleryPreferencesUtils
                .getPreferencesFlow(context)
                .collectAsState(GalleryPreferences())
            
            val useDarkTheme = when (appPrefs.value.appearance.theme) {
                GalleryPreferences.Appearance.Theme.SYSTEM -> isSystemInDarkTheme()
                GalleryPreferences.Appearance.Theme.LIGHT -> false
                GalleryPreferences.Appearance.Theme.DARK -> true
            }
            val useSystemDynamicColors = when (appPrefs.value.appearance.dynamicColors) {
                GalleryPreferences.Appearance.DynamicColors.SYSTEM -> true
                GalleryPreferences.Appearance.DynamicColors.DISABLED -> false
            }
            
            GalleryTheme(darkTheme = useDarkTheme, dynamicColor = useSystemDynamicColors) {
                GalleryNavHost()
            }
        }
    }
}
