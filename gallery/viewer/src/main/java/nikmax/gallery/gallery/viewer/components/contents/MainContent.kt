package nikmax.gallery.gallery.viewer.components.contents

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nikmax.gallery.gallery.core.ui.MediaItemUI
import nikmax.gallery.gallery.viewer.components.FramePreview
import nikmax.gallery.gallery.viewer.components.Image
import nikmax.gallery.gallery.viewer.components.PlayPauseButton
import nikmax.gallery.gallery.viewer.components.Seekbar
import nikmax.gallery.gallery.viewer.components.Video
import nikmax.gallery.gallery.viewer.components.bottom_bar.ViewerBottomBar
import nikmax.gallery.gallery.viewer.components.top_bar.ViewerTopBar
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun MainContent(
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
    modifier: Modifier = Modifier.Companion
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
                            modifier = Modifier.Companion
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
                contentAlignment = Alignment.Companion.Center,
                modifier = modifier
            ) {
                val scope = rememberCoroutineScope()
                
                val animatableZoom = remember { Animatable(1f) }
                val animatableOffsetX = remember { Animatable(Offset.Companion.Zero.x) }
                val animatableOffsetY = remember { Animatable(Offset.Companion.Zero.y) }
                
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
                    scope.launch { animatableOffsetX.animateTo(Offset.Companion.Zero.x) }
                    scope.launch { animatableOffsetY.animateTo(Offset.Companion.Zero.y) }
                }
                
                // reset transformation on file switch
                LaunchedEffect(currentFile) { resetZoomAndOffset() }
                // reset transformation on back press
                BackHandler(animatableZoom.value != 1f) { resetZoomAndOffset() }
                
                // pager to swipe files
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = animatableZoom.value == 1f,
                    modifier = Modifier.Companion
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
                    // fixme conflicts with other gesture
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
                                    modifier = Modifier.Companion
                                        .fillMaxSize()
                                        .align(Alignment.Companion.Center),
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
                                        modifier = Modifier.Companion
                                            .fillMaxSize()
                                            .align(Alignment.Companion.Center)
                                    )
                                }
                            }
                        }
                        false -> Image(
                            imagePath = file.path,
                            modifier = Modifier.Companion.fillMaxSize()
                        )
                    }
                }
                
                // if not in transformation mode - enable screen borders gestures
                if (animatableZoom.value == 1f) {
                    Box(
                        Modifier.Companion
                            .fillMaxHeight()
                            .fillMaxWidth(0.2F)
                            .align(Alignment.Companion.CenterStart)
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
                        Modifier.Companion
                            .fillMaxHeight()
                            .fillMaxWidth(0.25F)
                            .align(Alignment.Companion.CenterEnd)
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
                modifier = Modifier.Companion.align(Alignment.Companion.Center)
            ) {
                PlayPauseButton(
                    isPlaying = videoIsPlaying,
                    onClick = { if (videoIsPlaying) player.pause() else player.play() },
                )
            }
        }
    }
}


private fun setBrightness(context: Context, newBrightness: Float) {
    (context as Activity).window.attributes.apply {
        this.screenBrightness = newBrightness
    }
}

private fun setVolume(context: Context, streamType: Int, direction: Int) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.adjustStreamVolume(streamType, direction, AudioManager.FLAG_SHOW_UI)
}
