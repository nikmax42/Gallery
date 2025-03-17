package nikmax.gallery.gallery.explorer.components.error_contents

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.explorer.components.drawables.UndrawLock

@Composable
internal fun PermissionNotGrantedContent(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxSize()
        ) {
            Image(imageVector = UndrawLock, contentDescription = null)
            Text(stringResource(R.string.not_storage_access))
            Text(stringResource(R.string.storage_permission_explanation))
            Button(onClick = { onGrantClick() }) {
                Text(stringResource(R.string.grant))
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PermissionNotGrantedContentPreview() {
    GalleryTheme {
        PermissionNotGrantedContent(onGrantClick = {})
    }
}
