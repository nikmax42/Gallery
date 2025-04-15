package mtree.viewer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
internal fun Video(
    player: ExoPlayer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    this.player = player
                    this.useController = false
                }
            },
            update = { it.player = player },
            modifier = modifier
        )
    }
}
