package nikmax.gallery.gallery.explorer.components.error_contents

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.explorer.components.drawables.UndrawTreeSwing5010

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NothingFoundContent(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = { onRefresh() },
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(imageVector = UndrawTreeSwing5010, contentDescription = null)
                Text(
                    text = stringResource(R.string.no_media_found),
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    Button(onClick = { onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                        Text(stringResource(R.string.refresh))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun NoMediaFoundContentPreview() {
    GalleryTheme {
        NothingFoundContent(
            onRefresh = { },
        )
    }
}
