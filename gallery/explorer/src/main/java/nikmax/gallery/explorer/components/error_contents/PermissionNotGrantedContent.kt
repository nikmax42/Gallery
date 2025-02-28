package nikmax.gallery.explorer.components.error_contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.gallery.R

@Composable
internal fun PermissionNotGrantedContent(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
        ) {
            Text(stringResource(R.string.storage_permission_not_granted))
            Text(stringResource(R.string.storage_permission_explanation))
            Button(onClick = { onGrantClick() }) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

@Preview
@Composable
private fun PermissionNotGrantedContentPreview() {
    GalleryTheme {
        PermissionNotGrantedContent(onGrantClick = {})
    }
}
