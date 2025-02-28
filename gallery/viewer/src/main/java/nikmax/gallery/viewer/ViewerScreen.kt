package nikmax.gallery.viewer

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nikmax.gallery.core.ui.MediaItemUI
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.dialogs.Dialog
import nikmax.gallery.dialogs.album_picker.AlbumPickerFullScreenDialog
import nikmax.gallery.dialogs.conflict_resolver.ConflictResolverDialog
import nikmax.gallery.dialogs.deletion.DeletionDialog
import nikmax.gallery.dialogs.renaming.RenamingDialog
import nikmax.gallery.viewer.components.FramePreview
import nikmax.gallery.viewer.components.Image
import nikmax.gallery.viewer.components.PlayPauseButton
import nikmax.gallery.viewer.components.Seekbar
import nikmax.gallery.viewer.components.Video
import nikmax.gallery.viewer.components.bottom_bar.ViewerBottomBar
import nikmax.gallery.viewer.components.top_bar.ViewerTopBar

@Composable
fun ViewerScreen(
    filePath: String,
    onClose: () -> Unit,
    vm: ViewerVm = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    ViewerContent(
        state = state,
        onAction = { vm.onAction(it) },
        initialFilePath = filePath,
        onClose = { onClose() }
    )

    LaunchedEffect(filePath) {
        vm.onAction(ViewerVm.UserAction.Launch(filePath))
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun ViewerContent(
    state: ViewerVm.UIState,
    onAction: (ViewerVm.UserAction) -> Unit,
    initialFilePath: String,
    onClose: () -> Unit,
) {
    AnimatedContent(targetState = state.files.isEmpty()) { filesIsEmpty ->
        when (filesIsEmpty) {
            true -> {
                /* todo add loading placeholder content here */
            }
            false -> ContentReady(
                files = state.files,
                initialFilePath = initialFilePath,
                showUi = state.showControls,
                onSwitchUi = { onAction(ViewerVm.UserAction.SwitchControls) },
                onClose = { onClose() },
                onCopy = { onAction(ViewerVm.UserAction.Copy(it)) },
                onMove = { onAction(ViewerVm.UserAction.Move(it)) },
                onRename = { onAction(ViewerVm.UserAction.Rename(it)) },
                onDelete = { onAction(ViewerVm.UserAction.Delete(it)) },
                onShare = { onAction(ViewerVm.UserAction.Share(it)) }
            )
        }
    }

    // dialogs block
    when (val dialog = state.dialog) {
        Dialog.None -> {}
        is Dialog.AlbumPicker -> AlbumPickerFullScreenDialog(
            onConfirm = { dialog.onConfirm(it) },
            onDismiss = { dialog.onDismiss() }
        )
        is Dialog.ConflictResolver -> ConflictResolverDialog(
            conflictItem = dialog.conflictItem,
            onResolve = { resolution, _ -> dialog.onConfirm(resolution) },
            onDismiss = { dialog.onDismiss() }
        )
        is Dialog.DeletionConfirmation -> DeletionDialog(
            items = dialog.items,
            onConfirm = { dialog.onConfirm() },
            onDismiss = { dialog.onDismiss() }
        )
        is Dialog.Renaming -> RenamingDialog(
            mediaItem = dialog.item,
            onConfirm = { dialog.onConfirm(it) },
            onDismiss = { dialog.onDismiss() }
        )
    }
}


@Preview
@Composable
private fun ViewerContentPreview() {
    val files = remember {
        listOf(
            MediaItemUI.File(
                path = "/storage/emulated/0/DCIM/Camera/IMG1234567890.jpg",
                size = 0,
                dateCreated = 0,
                dateModified = 0,
                volume = MediaItemUI.Volume.PRIMARY
            ),
            MediaItemUI.File(
                path = "/storage/emulated/0/Images/wallpaper.jpg",
                size = 0,
                dateCreated = 0,
                dateModified = 0,
                volume = MediaItemUI.Volume.PRIMARY
            ),
            MediaItemUI.File(
                path = "/storage/emulated/0/Movies/VID1234567890.mp4",
                size = 0,
                dateCreated = 0,
                dateModified = 0,
                volume = MediaItemUI.Volume.PRIMARY
            )
        )
    }
    val showUi = remember { true }

    var state by remember {
        mutableStateOf(
            ViewerVm.UIState(
                files = files,
                showControls = showUi
            )
        )
    }

    fun onAction(action: ViewerVm.UserAction) {
        when (action) {
            is ViewerVm.UserAction.Launch -> {}
            is ViewerVm.UserAction.Copy -> {}
            is ViewerVm.UserAction.Delete -> {}
            is ViewerVm.UserAction.Move -> {}
            is ViewerVm.UserAction.Rename -> {}
            is ViewerVm.UserAction.Share -> {}
            ViewerVm.UserAction.SwitchControls -> {
                state = state.copy(showControls = !state.showControls)
            }
        }
    }

    GalleryTheme {
        ViewerContent(
            state = state,
            onAction = { onAction(it) },
            initialFilePath = files[1].path,
            onClose = {}
        )
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContentReady(
    files: List<MediaItemUI.File>, // must not be empty!
    initialFilePath: String,
    showUi: Boolean,
    onSwitchUi: () -> Unit,
    onClose: () -> Unit,
    onCopy: (MediaItemUI.File) -> Unit,
    onMove: (MediaItemUI.File) -> Unit,
    onRename: (MediaItemUI.File) -> Unit,
    onDelete: (MediaItemUI.File) -> Unit,
    onShare: (MediaItemUI.File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialPage = remember(initialFilePath, files) {
        files
            .indexOfFirst { it.path == initialFilePath }
            .coerceAtLeast(0)
    }
    val pagerState = remember(files) { PagerState(initialPage) { files.size } }
    val currentFile = files[pagerState.settledPage]

    // for videos and gifs only
    val player = remember(currentFile) {
        ExoPlayer.Builder(context).build().apply {
            this.setMediaItem(MediaItem.fromUri(currentFile.uri))
            prepare()
            playWhenReady = false
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    var videoDuration by remember(player) { mutableLongStateOf(player.duration) }
    var videoPosition by remember(player) { mutableLongStateOf(player.currentPosition) }
    val videoBufferedPosition by remember(player) { mutableLongStateOf(player.bufferedPosition) }
    var videoIsPlaying by remember(player) { mutableStateOf(player.isPlaying) }
    var videoSeekInProgress by remember(player) { mutableStateOf(false) }
    // Update video playback stats (if user not using seekbar at this moment)
    LaunchedEffect(player) {
        while (true) {
            if (!videoSeekInProgress) {
                videoDuration = player.duration
                videoPosition = player.currentPosition
                videoIsPlaying = player.isPlaying
            }
            delay(100)
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = showUi,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            ) {
                ViewerTopBar(
                    itemPath = currentFile.path,
                    onBackCLick = { onClose() }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showUi,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                Column {
                    // video seekbar bound to scaffold's bottom bar
                    if (currentFile.isVideoOrGif) {
                        Seekbar(
                            position = videoPosition,
                            bufferedPosition = videoBufferedPosition,
                            duration = videoDuration,
                            onSeek = {
                                videoSeekInProgress = true
                                player.seekTo(it)
                                videoPosition = player.currentPosition
                            },
                            onSeekFinished = {
                                videoSeekInProgress = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ViewerBottomBar(
                        onCopyClick = { onCopy(currentFile) },
                        onMoveClick = { onMove(currentFile) },
                        onRenameClick = { onRename(currentFile) },
                        onDeleteClick = { onDelete(currentFile) },
                        onShareClick = { onShare(currentFile) }
                    )
                }
            }
        }
    ) { _ ->
        Box {
            BoxWithConstraints(
                contentAlignment = Alignment.Center,
                modifier = modifier
            ) {
                val scope = rememberCoroutineScope()

                val animatableZoom = remember { Animatable(1f) }
                val animatableOffsetX = remember { Animatable(Offset.Zero.x) }
                val animatableOffsetY = remember { Animatable(Offset.Zero.y) }

                val transformationState = rememberTransformableState { zoomChange, panChange, rotationChange ->
                    val zoom = (animatableZoom.value * zoomChange).coerceIn(1f, 5f)
                    val extraWidth = (zoom - 1) * constraints.maxWidth
                    val extraHeight = (zoom - 1) * constraints.maxHeight
                    val maxX = extraWidth / 2
                    val maxY = extraHeight / 2
                    val offset = Offset(
                        x = (animatableOffsetX.value + zoom * panChange.x).coerceIn(-maxX, maxX),
                        y = (animatableOffsetY.value + zoom * panChange.y).coerceIn(-maxY, maxY),
                    )
                    scope.launch {
                        animatableZoom.snapTo(zoom)
                        animatableOffsetX.snapTo(offset.x)
                        animatableOffsetY.snapTo(offset.y)
                    }
                }

                suspend fun resetZoomAndOffset() {
                    animatableZoom.animateTo(1f)
                    animatableOffsetX.animateTo(Offset.Zero.x)
                    animatableOffsetY.animateTo(Offset.Zero.y)
                }

                // reset transformation on file switch
                LaunchedEffect(currentFile) { resetZoomAndOffset() }
                // reset transformation on back press
                BackHandler(animatableZoom.value != 1f) {
                    scope.launch { resetZoomAndOffset() }
                }

                // pager to swipe files
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = animatableZoom.value == 1f,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = animatableZoom.value
                            scaleY = animatableZoom.value
                            translationX = animatableOffsetX.value
                            translationY = animatableOffsetY.value
                        }
                        .transformable(state = transformationState)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onSwitchUi() },
                                onDoubleTap = {
                                    scope.launch {
                                        if (animatableZoom.value == 1f) animatableZoom.animateTo(2.5f)
                                        else resetZoomAndOffset()
                                    }
                                }
                            )
                        }
                ) { page ->
                    val file = files[page]
                    when (file.isVideoOrGif) {
                        true -> {
                            Box {
                                Video(
                                    player = player,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center),
                                )
                                // if video is not prepared yet - display preview frame above the player
                                val notReadyYet = player.playbackState == Player.STATE_IDLE || videoDuration == C.TIME_UNSET
                                val awaitsForStart = player.playbackState == Player.STATE_READY && player.currentPosition == 0L
                                if (notReadyYet || awaitsForStart) {
                                    val framePosition = remember(videoPosition) {
                                        (videoPosition.toDouble() / videoDuration.toDouble())
                                            .coerceIn(0.01, 1.0)
                                    }
                                    FramePreview(
                                        videoUri = file.uri,
                                        videoFramePercent = framePosition,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }
                        false -> Image(
                            imagePath = file.path,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // if not in transformation mode - enable screen borders gestures
                if (animatableZoom.value == 1f) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.25F)
                            .align(Alignment.CenterStart)
                            .clickable(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.settledPage - 1)
                                    }
                                },
                                interactionSource = null,
                                indication = null
                            )
                            .draggable(
                                state = rememberDraggableState { /* TODO("call onDrag and pass float value here") */ },
                                orientation = Orientation.Vertical
                            )
                    )
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.25F)
                            .align(Alignment.CenterEnd)
                            .clickable(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.settledPage + 1)
                                    }
                                },
                                interactionSource = null,
                                indication = null
                            )
                            .draggable(
                                state = rememberDraggableState { /* TODO("call onDrag and pass float value here") */ },
                                orientation = Orientation.Vertical
                            )
                    )
                }
            }

            // show play/pause button if file is video and user not seeking it
            AnimatedVisibility(
                visible = currentFile.isVideoOrGif && showUi && !videoSeekInProgress,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                PlayPauseButton(
                    isPlaying = videoIsPlaying,
                    onClick = { if (videoIsPlaying) player.pause() else player.play() },
                )
            }
        }
    }
}
