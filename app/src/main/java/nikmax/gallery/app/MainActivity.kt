package nikmax.gallery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import nikmax.gallery.core.preferences.AppPreferences
import nikmax.gallery.core.preferences.AppPreferencesUtils
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.gallery.GalleryNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appPreferences by AppPreferencesUtils
                .getPreferencesFlow(this)
                .collectAsState(AppPreferences())

            GalleryTheme(dynamicColor = appPreferences.useDynamicColors) {
                GalleryNavHost()
            }
        }
    }
}
