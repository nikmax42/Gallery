package nikmax.gallery.viewer.components.viewers

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import nikmax.gallery.core.ui.R
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.viewer.components.GesturesContainer

@Composable
fun ImageViewer(
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
            .diskCachePolicy(CachePolicy.DISABLED) // todo get from app preferences?
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    GesturesContainer(modifier = modifier) {
        AsyncImage(
            model = imageRequest,
            error = painterResource(R.drawable.gallery_image_placeholder),
            // todo replace with animated placeholder
            placeholder = painterResource(R.drawable.gallery_image_placeholder),
            // todo replace with animated placeholder
            contentDescription = imagePath,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ImageViewerPreview() {
    GalleryTheme {
        ImageViewer(
            imagePath = "",
            modifier = Modifier.fillMaxSize()
        )
    }
}
