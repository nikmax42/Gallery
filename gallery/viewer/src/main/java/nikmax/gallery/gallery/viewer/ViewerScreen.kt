package nikmax.gallery.gallery.viewer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nikmax.gallery.core.ui.theme.GalleryTheme
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.core.utils.SharingUtils
import nikmax.gallery.gallery.viewer.components.FramePreview
import nikmax.gallery.gallery.viewer.components.Image
import nikmax.gallery.gallery.viewer.components.PlayPauseButton
import nikmax.gallery.gallery.viewer.components.Seekbar
import nikmax.gallery.gallery.viewer.components.Video
import nikmax.gallery.gallery.viewer.components.bottom_bar.ViewerBottomBar
import nikmax.gallery.gallery.viewer.components.top_bar.ViewerTopBar
import nikmax.material_tree.gallery.dialogs.Dialog
import nikmax.material_tree.gallery.dialogs.album_picker.AlbumPickerFullScreenDialog
import nikmax.material_tree.gallery.dialogs.conflict_resolver.ConflictResolverDialog
import nikmax.material_tree.gallery.dialogs.deletion.DeletionDialog
import nikmax.material_tree.gallery.dialogs.renaming.RenamingDialog
import kotlin.math.roundToInt

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
    val context = LocalContext.current
    
    AnimatedContent(targetState = state.files.isEmpty()) { filesIsEmpty ->
        when (filesIsEmpty) {
            true -> {
                /* todo add loading placeholder content here? */
            }
            false -> MainContent(
                files = state.files,
                initialFilePath = initialFilePath,
                showUi = state.showControls,
                onSwitchUi = { onAction(ViewerVm.UserAction.SwitchControls) },
                onClose = { onClose() },
                onCopy = { onAction(ViewerVm.UserAction.Copy(it)) },
                onMove = { onAction(ViewerVm.UserAction.Move(it)) },
                onRename = { onAction(ViewerVm.UserAction.Rename(it)) },
                onDelete = { onAction(ViewerVm.UserAction.Delete(it)) },
                onShare = { SharingUtils.shareSingleFile(it, context) }
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
                creationDate = 0,
                modificationDate = 0,
                belongsToVolume = MediaItemUI.Volume.DEVICE
            ),
            MediaItemUI.File(
                path = "/storage/emulated/0/Images/wallpaper.jpg",
                size = 0,
                creationDate = 0,
                modificationDate = 0,
                belongsToVolume = MediaItemUI.Volume.DEVICE
            ),
            MediaItemUI.File(
                path = "/storage/emulated/0/Movies/VID1234567890.mp4",
                size = 0,
                creationDate = 0,
                modificationDate = 0,
                belongsToVolume = MediaItemUI.Volume.DEVICE
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
fun MainContent(
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
            val videoUri = currentFile.uri ?: "file://${currentFile.path}"
            this.setMediaItem(MediaItem.fromUri(videoUri))
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
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
    ) {
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
                
                fun zoomToDoubleTap(tapPoint: Offset) {
                    val newZoom = 2.5f
                    scope.launch { animatableZoom.animateTo(newZoom) }
                    
                    val centerX = this.constraints.maxWidth / 2
                    val centerOffsetX = (tapPoint.x - centerX).roundToInt()
                    val newX = (animatableOffsetX.value - centerOffsetX) * newZoom
                    scope.launch { animatableOffsetX.animateTo(newX) }
                    
                    val centerY = this.constraints.maxHeight / 2
                    val centerOffsetY = (tapPoint.y - centerY).roundToInt()
                    val newY = (animatableOffsetX.value - centerOffsetY) * newZoom
                    scope.launch { animatableOffsetY.animateTo(newY) }
                }
                
                fun resetZoomAndOffset() {
                    scope.launch { animatableZoom.animateTo(1f) }
                    scope.launch { animatableOffsetX.animateTo(Offset.Zero.x) }
                    scope.launch { animatableOffsetY.animateTo(Offset.Zero.y) }
                }
                
                // reset transformation on file switch
                LaunchedEffect(currentFile) { resetZoomAndOffset() }
                // reset transformation on back press
                BackHandler(animatableZoom.value != 1f) { resetZoomAndOffset() }
                
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
                        // switch UI on single tap, switch zoom on double tap
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onSwitchUi() },
                                onDoubleTap = { offset ->
                                    if (animatableZoom.value == 1f) zoomToDoubleTap(offset)
                                    else resetZoomAndOffset()
                                }
                            )
                        }
                    // todo close viewer on vertical up-to-down swipe (if not in transformation mode)
                    // fixme conflicts with transformation
                    /* .then(
                        if (animatableZoom.value == 1f)
                            Modifier.pointerInput(Unit) {
                                var startPosition = Offset.Zero
                                var isUpToDownDrag = false
                                detectVerticalDragGestures(
                                    onDragStart = { offset -> startPosition = offset },
                                    onVerticalDrag = { change, dragAmount ->
                                        isUpToDownDrag = change.position.y - startPosition.y > 0
                                    },
                                    onDragEnd = { if (isUpToDownDrag) onClose() }
                                )
                            }
                        else Modifier
                    ) */
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
                                        videoUri = file.path,
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
                            .fillMaxWidth(0.2F)
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
                        /* .pointerInput(currentFile.isVideoOrGif) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, amount ->
                                    changeVolume(
                                        context = context,
                                        streamType = AudioManager.STREAM_MUSIC,
                                        direction = if (change.position.y > change.previousPosition.y) AudioManager.ADJUST_LOWER
                                        else AudioManager.ADJUST_RAISE
                                    )
                                }
                            )
                        } */
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
                        /* .pointerInput(currentFile.isVideoOrGif) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, amount ->

                                }
                            )
                        } */
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

private fun changeBrightness(context: Context, newBrightness: Float) {
    (context as Activity).window.attributes.apply {
        this.screenBrightness = newBrightness
    }
}

private fun changeVolume(context: Context, streamType: Int, direction: Int) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.adjustStreamVolume(streamType, direction, AudioManager.FLAG_SHOW_UI)
}
