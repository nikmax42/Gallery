package mtree.core.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import mtree.core.ui.theme.GalleryTheme

/*
* Thanks to Philipp Lackner and valentinilk for the references:
* 1. https://github.com/philipplackner/ShimmerEffectCompose
* 2. https://github.com/valentinilk/compose-shimmer
*/

fun Modifier.shimmerBackground(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing,
                delayMillis = 1500
            )
        )
    )
    
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.surfaceDim.copy(alpha = 1.0f),
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
            ),
            start = Offset(x = startOffsetX, y = 0f),
            end = Offset(x = startOffsetX + size.width.toFloat(), y = size.height.toFloat() / 5f)
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
private fun ShimmerPreview() {
    GalleryTheme {
        Surface {
            Box(
                Modifier
                    .size(64.dp)
                    .shimmerBackground()
            )
        }
    }
}
