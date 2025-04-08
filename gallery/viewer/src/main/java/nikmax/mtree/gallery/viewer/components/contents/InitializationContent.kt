package nikmax.mtree.gallery.viewer.components.contents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nikmax.mtree.core.ui.theme.GalleryTheme
import nikmax.mtree.gallery.core.ui.components.shimmerBackground
import nikmax.mtree.gallery.viewer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InitializationContent(
    onClose: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onClose() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(
                        text = "",
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .shimmerBackground()
                    )
                },
                modifier = Modifier.Companion.shimmerBackground()
            )
        },
    ) { paddings ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = paddings.calculateTopPadding(),
                    bottom = paddings.calculateBottomPadding()
                )
                .shimmerBackground()
        )
    }
}

@Preview
@Composable
private fun InitializationContentPreview() {
    GalleryTheme {
        InitializationContent(
            onClose = { /*TODO*/ },
            modifier = Modifier.Companion.fillMaxSize()
        )
    }
}
