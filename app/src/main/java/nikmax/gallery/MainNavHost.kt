package nikmax.gallery

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import nikmax.gallery.explorer.ExplorerScreen
import nikmax.gallery.viewer.ViewerScreen

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    Surface {
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
            composable<NavRoutes.Viewer>(
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Up,
                        animationSpec = tween(500)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Companion.Down,
                        animationSpec = tween(500)
                    )
                }
            ) { entry ->
                val args = entry.toRoute<NavRoutes.Viewer>()
                ViewerScreen(
                    filePath = args.filePath,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}

private object NavRoutes {
    @Serializable
    data class Explorer(val albumPath: String? = null)

    @Serializable
    data class Viewer(val filePath: String)
}
