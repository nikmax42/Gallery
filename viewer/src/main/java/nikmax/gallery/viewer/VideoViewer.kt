package nikmax.gallery.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.videoFramePercent
import dev.vivvvek.seeker.Seeker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.timer
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun VideoViewer(
    videoUri: String,
    showPlayerControls: Boolean,
    modifier: Modifier = Modifier,
    seekBarBottomPadding: Dp = 0.dp
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var seekInProgress by remember { mutableStateOf(false) } // to avoid recomposition while user moving slider
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var bufferedPercentage by remember { mutableIntStateOf(0) }
    var duration by remember { mutableLongStateOf(0L) }
    val player = remember(videoUri) {
        ExoPlayer.Builder(context).build()
            .apply {
                setMediaItem(MediaItem.fromUri("file://$videoUri"))
                repeatMode = Player.REPEAT_MODE_ALL
                addListener(
                    object : Player.Listener {
                        override fun onEvents(player: Player, events: Player.Events) {
                            super.onEvents(player, events)
                            if (!seekInProgress) { // to avoid recomposition while user moving slider
                                isPlaying = player.isPlaying
                                currentPosition = player.currentPosition.coerceAtLeast(0L)
                                bufferedPercentage = player.bufferedPercentage.coerceAtLeast(0)
                                duration = player.duration.coerceAtLeast(0L)
                            }
                        }
                    }
                )
                prepare()
            }
    }

    // pause player when not in focus
    // todo add preference for that
    LifecycleResumeEffect(videoUri) {
        if (currentPosition > 0) player.play()
        onPauseOrDispose { player.pause() }
    }

    DisposableEffect(videoUri) {
        val positionUpdate = timer(period = 100) {
            if (!seekInProgress) { // to avoid recomposition while user moving slider
                scope.launch(Dispatchers.Main) { // player accessing from the wrong thread without
                    currentPosition = player.currentPosition.coerceAtLeast(0L)
                }
            }
        }
        onDispose {
            positionUpdate.cancel()
            player.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        )

        if (!isPlaying && currentPosition == 0L)
            VideoFrame(
                videoUri = videoUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )

        AnimatedVisibility(
            visible = showPlayerControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            PlayPauseButton(
                isPlaying = isPlaying,
                onClick = {
                    when (isPlaying) {
                        true -> player.pause()
                        false -> player.play()
                    }
                },
                modifier = Modifier.size(40.dp)
            )
        }
        AnimatedVisibility(
            visible = showPlayerControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = seekBarBottomPadding + 4.dp
                )
        ) {
            Seekbar(
                currentMillisecond = currentPosition,
                totalMilliseconds = duration,
                onSeek = { seekInProgress = true },
                onSeekFinished = {
                    player.seekTo(it)
                    seekInProgress = false
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Preview
@Composable
private fun VideoViewerPreview() {
    VideoViewer(
        videoUri = "/storage/emulated/0/Movies/4_532580209606526323.mp4",
        showPlayerControls = true,
    )
}


@Composable
private fun VideoFrame(
    videoUri: String,
    modifier: Modifier = Modifier,
    videoFramePercent: Double = 0.1
) {
    val context = LocalContext.current
    val imageRequest = remember {
        ImageRequest.Builder(context)
            .data(videoUri)
            .videoFramePercent(videoFramePercent)
            .dispatcher(Dispatchers.IO)
            .memoryCacheKey(videoUri)
            .diskCacheKey(videoUri)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    AsyncImage(
        model = imageRequest,
        error = painterResource(nikmax.gallery.core.ui.R.drawable.gallery_image_placeholder),
        contentDescription = null,
        modifier = modifier
    )
}


@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = { onClick() },
        modifier = modifier.clip(CircleShape)
    ) {
        when (isPlaying) {
            true -> Icon(Icons.Default.Pause, contentDescription = "pause")
            false -> Icon(Icons.Default.PlayArrow, contentDescription = "play")
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun PlayPauseButtonPreview() {
    var isPlaying by remember { mutableStateOf(true) }
    PlayPauseButton(
        isPlaying = isPlaying,
        onClick = { isPlaying = isPlaying.not() }
    )
}


@Composable
private fun Seekbar(
    currentMillisecond: Long,
    totalMilliseconds: Long,
    onSeek: (Long) -> Unit,
    onSeekFinished: (Long) -> Unit,
    modifier: Modifier = Modifier,
    bufferedMilliseconds: Long = 0,
) {
    fun Float.toMills(): Long {
        return (totalMilliseconds.toFloat() * this).toLong()
    }

    fun Duration.toHHMMSSString(includeMills: Boolean = false): String {
        this.toComponents { hours, minutes, seconds, nanoseconds ->
            val hh = if (hours > 0) "${hours.toString().padStart(2, '0')}:" else ""
            val mm = "${minutes.toString().padStart(2, '0')}:"
            val ss = "${seconds.toString().padStart(2, '0')}"
            val mills = if (includeMills) ".${nanoseconds / 1_000_000}" else ""
            return "$hh$mm$ss$mills"
        }
    }

    var currentValue by remember(currentMillisecond) {
        val value = if (totalMilliseconds > 0) {
            currentMillisecond.toFloat() / totalMilliseconds.toFloat()
        } else 0F
        mutableFloatStateOf(value)
    }
    val bufferedValue by remember(bufferedMilliseconds) {
        val value = if (totalMilliseconds > 0) {
            bufferedMilliseconds.toFloat() / totalMilliseconds.toFloat()
        } else 0F
        mutableFloatStateOf(value)
    }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(currentMillisecond.toDuration(DurationUnit.MILLISECONDS).toHHMMSSString())
            Seeker(
                value = currentValue,
                readAheadValue = bufferedValue,
                range = 0f..1f,
                onValueChange = {
                    currentValue = it
                    onSeek(currentValue.toMills())
                },
                onValueChangeFinished = {
                    onSeekFinished(currentValue.toMills())
                },
                modifier = Modifier.weight(1F)
            )
            Text(totalMilliseconds.toDuration(DurationUnit.MILLISECONDS).toHHMMSSString())
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun SeekbarPreview() {
    var currentMills by remember { mutableLongStateOf(6242) }
    val bufferedMills by remember { mutableLongStateOf(11103) }
    Seekbar(
        currentMillisecond = currentMills,
        bufferedMilliseconds = bufferedMills,
        totalMilliseconds = 16535,
        onSeek = {},
        onSeekFinished = { currentMills = it }
    )
}
