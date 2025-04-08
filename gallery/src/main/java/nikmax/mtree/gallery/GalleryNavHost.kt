package nikmax.mtree.gallery

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import nikmax.mtree.gallery.explorer.ExplorerScreen
import nikmax.mtree.gallery.viewer.ViewerScreen

@Composable
fun GalleryNavHost() {
    val navController = rememberNavController()
    Surface {
        NavHost(
            navController = navController,
            startDestination = GalleryRoutes.Explorer()
        ) {
            composable<GalleryRoutes.Explorer> { entry ->
                val args = entry.toRoute<GalleryRoutes.Explorer>()
                ExplorerScreen(
                    albumPath = args.albumPath,
                    onFileOpen = { file -> navController.navigate(GalleryRoutes.Viewer(file.path)) }
                )
            }
            composable<GalleryRoutes.Viewer>(
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
                val args = entry.toRoute<GalleryRoutes.Viewer>()
                ViewerScreen(
                    filePath = args.filePath,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}

object GalleryRoutes {
    @Serializable
    data class Explorer(val albumPath: String? = null)
    
    @Serializable
    data class Viewer(val filePath: String)
}
