package nikmax.gallery.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers

@Composable
fun ImageViewer(
    imagePath: String,
    modifier: Modifier = Modifier
) {
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(imagePath)
        .dispatcher(Dispatchers.IO)
        .memoryCacheKey(imagePath)
        .diskCacheKey(imagePath)
        .diskCachePolicy(CachePolicy.DISABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .build()
    AsyncImage(
        model = imageRequest,
        contentDescription = imagePath,
        modifier = modifier
    )
}
