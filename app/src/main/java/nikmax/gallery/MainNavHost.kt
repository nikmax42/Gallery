package nikmax.gallery

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import nikmax.gallery.explorer.ui.ExplorerScreen
import nikmax.gallery.viewer.ViewerScreen

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Explorer()
    ) {
        composable<NavRoutes.Explorer> { entry ->
            val args = entry.toRoute<NavRoutes.Explorer>()
            ExplorerScreen(
                albumPath = args.albumPath,
                onFileOpen = { file ->
                    // navigate to viewer
                    navController.navigate(NavRoutes.Viewer(file.path))
                },
                onAlbumOpen = { album ->
                    // navigate to another explorer instance
                    navController.navigate(NavRoutes.Explorer(album.path))
                }
            )
        }
        composable<NavRoutes.Viewer> { entry ->
            val args = entry.toRoute<NavRoutes.Viewer>()
            ViewerScreen(
                filePath = args.filePath,
                onBackCLick = { navController.popBackStack() }
            )
        }
    }
}
