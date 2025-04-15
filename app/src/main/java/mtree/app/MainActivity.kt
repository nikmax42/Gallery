package mtree.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import mtree.core.preferences.MtreePreferences
import mtree.core.preferences.MtreePreferencesUtils
import mtree.core.ui.theme.GalleryTheme

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
                MainNavHost()
            }
        }
    }
}
