package nikmax.gallery.viewer

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import nikmax.gallery.core.ui.MediaItemUI

@Composable
fun ViewerScreen(
    filePath: String,
    onBackCLick: () -> Unit,
    vm: ViewerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(filePath) {
        vm.onAction(ViewerVm.UserAction.Launch(filePath))
    }

    when (val content = state.content) {
        ViewerVm.UIState.Content.Preparing -> {
            /* todo implement animated loading placeholder */
        }
        is ViewerVm.UIState.Content.Ready -> {
            val initialPage = remember(content.files) {
                content.files
                    .indexOfFirst { it.path == filePath }
                    .coerceAtLeast(0)
            }
            ViewerScreenContent(
                files = content.files,
                showControls = state.showControls,
                onSwitchControls = { vm.onAction(ViewerVm.UserAction.SwitchControls) },
                onBackCLick = { onBackCLick() },
                initialPage = initialPage
            )
        }
    }
}

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // to show appbars above the image
fun ViewerScreenContent(
    files: List<MediaItemUI.File>,
    showControls: Boolean,
    onSwitchControls: () -> Unit,
    onBackCLick: () -> Unit,
    initialPage: Int,
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { files.size }
    )
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = showControls,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            ) {
                ViewerTopBar(
                    itemPath = files.getOrNull(pagerState.settledPage)?.path ?: "",
                    onBackCLick = { onBackCLick() }
                )
            }
        },
    ) { paddings ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { onSwitchControls() },
                    interactionSource = null,
                    indication = null
                )
        ) { page ->
            val file = files.getOrNull(page)
            if (file != null) {
                if (file.mimetype.startsWith("video/") || file.mimetype == "image/gif") {
                    VideoViewer(
                        videoUri = file.path,
                        showPlayerControls = showControls,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (file.mimetype.startsWith("image/")) {
                    ImageViewer(
                        imagePath = file.path,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
@Preview
@Composable
private fun ViewerContentPreview() {
    val file1 = MediaItemUI.File(
        path = "/storage/emulated/0/DCIM/image1.jpg",
        name = "image1.jpg",
        size = 42L,
        dateCreated = 0,
        dateModified = 0,
        mimetype = "image/jpg",
        volume = MediaItemUI.Volume.PRIMARY
    )
    val file2 = MediaItemUI.File(
        path = "/storage/emulated/0/DCIM/video1.mp4",
        name = "video1.mp4",
        size = 42L,
        dateCreated = 0,
        dateModified = 0,
        mimetype = "video/mp4",
        volume = MediaItemUI.Volume.PRIMARY
    )
    val file3 = MediaItemUI.File(
        path = "/storage/emulated/0/DCIM/gif1.gif",
        name = "gif1.gif",
        size = 42L,
        dateCreated = 0,
        dateModified = 0,
        mimetype = "image/gif",
        volume = MediaItemUI.Volume.PRIMARY
    )
    var showControls by remember { mutableStateOf(true) }
    ViewerScreenContent(
        files = listOf(file1, file2, file3),
        showControls = showControls,
        onSwitchControls = { showControls = showControls.not() },
        onBackCLick = {},
        initialPage = 0
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerTopBar(
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
