package mtree.viewer.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers

@Composable
internal fun Image(
    imagePath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(imagePath) {
        ImageRequest.Builder(context)
            .data(imagePath)
            .dispatcher(Dispatchers.IO)
            .memoryCacheKey(imagePath)
            .diskCacheKey(imagePath)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    AsyncImage(
        model = imageRequest,
        /*  error = painterResource(R.drawable.gallery_image_placeholder),
         // todo replace with animated placeholder
         placeholder = painterResource(R.drawable.gallery_image_placeholder),
         // todo replace with animated placeholder */
        contentDescription = imagePath,
        modifier = modifier
    )
}
