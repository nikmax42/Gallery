package mtree.permission_request

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mtree.core.ui.theme.GalleryTheme
import mtree.permission_request.illustration.StorageIllustration

@Composable
fun PermissionNotGrantedContent(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(0.7F)
            ) {
                Image(
                    imageVector = StorageIllustration,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.storage_not_granted),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.storage_not_granted_explanation),
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { onGrantClick() }) {
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
        PermissionNotGrantedContent(
            onGrantClick = {}
        )
    }
}
