package mtree.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import mtree.explorer.ExplorerScreen
import mtree.viewer.ViewerScreen

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    Surface {
        NavHost(
            navController = navController,
            startDestination = MainRoutes.Explorer(
                albumPath = null,
                searchQuery = null
            )
        ) {
            composable<MainRoutes.Explorer> { entry ->
                val args = entry.toRoute<MainRoutes.Explorer>()
                ExplorerScreen(
                    albumPath = args.albumPath,
                    searchQuery = args.searchQuery,
                    onAlbumOpen = { albumPath, searchQuery ->
                        navController.navigate(
                            MainRoutes.Explorer(
                                albumPath = args.albumPath,
                                searchQuery = args.searchQuery,
                            )
                        )
                    },
                    onFileOpen = { filePath, searchQuery ->
                        navController.navigate(
                            MainRoutes.Viewer(
                                filePath = filePath,
                                searchQuery = searchQuery
                            )
                        )
                    }
                )
            }
            composable<MainRoutes.Viewer>(
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
                val args = entry.toRoute<MainRoutes.Viewer>()
                ViewerScreen(
                    filePath = args.filePath,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}

internal object MainRoutes {
    @Serializable
    data class Explorer(
        val albumPath: String?,
        val searchQuery: String?
    )
    
    @Serializable
    data class Viewer(
        val filePath: String,
        val searchQuery: String?
    )
}
