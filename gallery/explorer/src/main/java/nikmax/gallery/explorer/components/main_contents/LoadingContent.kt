package nikmax.gallery.explorer.components.main_contents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.ui.components.shimmerBackground
import nikmax.gallery.core.ui.theme.GalleryTheme

@Composable
internal fun LoadingContent(
    modifier: Modifier = Modifier,
    portraitColumnsAmount: Int = 3,
    landscapeColumnsAmount: Int = 4
) {
    val orientation = LocalConfiguration.current.orientation
    val columnsAmount = when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> landscapeColumnsAmount
        else -> portraitColumnsAmount
    }

    LazyVerticalGrid(
        GridCells.Fixed(columnsAmount),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(30) {
            Box(
                Modifier
                    .fillMaxSize()
                    .size(96.dp)
                    .clip(CardDefaults.outlinedShape)
                    .shimmerBackground()
            )
        }
    }
}

@Preview
@Composable
private fun ExplorerLoadingContentPreview() {
    GalleryTheme {
        LoadingContent()
    }
}
