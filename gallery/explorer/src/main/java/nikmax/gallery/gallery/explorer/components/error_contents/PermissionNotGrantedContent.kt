package nikmax.gallery.gallery.explorer.components.error_contents

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.explorer.R
import nikmax.gallery.gallery.explorer.components.drawables.UndrawLock

@Composable
internal fun PermissionNotGrantedContent(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(imageVector = UndrawLock, contentDescription = null)
                //  Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.not_storage_access),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(R.string.storage_permission_explanation),
                    style = MaterialTheme.typography.bodyLarge
                )
                // Spacer(Modifier.height(8.dp))
                Button(onClick = { onGrantClick() }) {
                    Icon(Icons.Default.Check, stringResource(R.string.grant))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.grant))
                }
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
