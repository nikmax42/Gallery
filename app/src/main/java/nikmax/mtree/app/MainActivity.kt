package nikmax.mtree.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import nikmax.mtree.core.preferences.CorePreferences
import nikmax.mtree.core.preferences.CorePreferencesRepo
import nikmax.mtree.core.ui.theme.GalleryTheme
import nikmax.mtree.gallery.GalleryNavHost
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var prefsRepo: CorePreferencesRepo
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appPrefs = prefsRepo
                .getPreferencesFlow()
                .collectAsState(CorePreferences())
                .value
            
            val useDarkTheme = when (appPrefs.theme) {
                CorePreferences.Theme.SYSTEM -> isSystemInDarkTheme()
                CorePreferences.Theme.LIGHT -> false
                CorePreferences.Theme.DARK -> true
            }
            val useSystemDynamicColors = when (appPrefs.dynamicColors) {
                CorePreferences.DynamicColors.SYSTEM -> true
                CorePreferences.DynamicColors.DISABLED -> false
            }
            
            GalleryTheme(darkTheme = useDarkTheme, dynamicColor = useSystemDynamicColors) {
                GalleryNavHost()
            }
        }
    }
}
