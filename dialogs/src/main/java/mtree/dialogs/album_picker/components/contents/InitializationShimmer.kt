package mtree.dialogs.album_picker.components.contents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import mtree.core.ui.components.shimmerBackground
import mtree.dialogs.album_picker.components.topbar.PickerTopBar

@Composable
fun InitializationShimmer(
    portraitGridColumns: Int,
    landscapeGridColumns: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columnsAmount = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> landscapeGridColumns
        else -> portraitGridColumns
    }
    
    Scaffold(
        topBar = {
            PickerTopBar(
                currentAlbum = null,
                onConfirm = {},
                onDismiss = { onDismiss() },
                snackbarHostState = SnackbarHostState()
            )
        },
    ) { paddings ->
        LazyVerticalGrid(
            GridCells.Fixed(columnsAmount),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.padding(paddings)
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
}
