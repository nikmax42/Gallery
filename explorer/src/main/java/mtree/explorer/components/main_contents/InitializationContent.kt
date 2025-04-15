package mtree.explorer.components.main_contents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mtree.core.ui.components.shimmerBackground
import mtree.core.ui.theme.GalleryTheme

@Composable
internal fun InitializationContent(
    portraitGridColumns: Int,
    landscapeGridColumns: Int,
    modifier: Modifier = Modifier,
) {
    val columnsAmount = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> landscapeGridColumns
        else -> portraitGridColumns
    }
    
    LazyVerticalGrid(
        GridCells.Fixed(columnsAmount),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(60) {
            Box(
                Modifier
                    .fillMaxSize()
                    .aspectRatio(0.9f)
                    .clip(CardDefaults.outlinedShape)
                    .shimmerBackground()
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun ExplorerLoadingContentPreview() {
    GalleryTheme {
        Surface {
            InitializationContent(
                portraitGridColumns = 3,
                landscapeGridColumns = 4
            )
        }
    }
}
