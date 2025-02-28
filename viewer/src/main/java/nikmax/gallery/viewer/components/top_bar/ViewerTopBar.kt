package nikmax.gallery.viewer.components.top_bar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import nikmax.gallery.viewer.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewerTopBar(
    itemPath: String,
    onBackCLick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = itemPath,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackCLick() }) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
            }
        }
    )
}
