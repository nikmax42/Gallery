package mtree.viewer.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.videoFramePercent
import dev.vivvvek.seeker.Seeker
import dev.vivvvek.seeker.SeekerDefaults
import kotlinx.coroutines.Dispatchers
import mtree.core.ui.theme.GalleryTheme
import mtree.core.utils.MeasurementUnitsUtils.percentageToPosition
import mtree.core.utils.MeasurementUnitsUtils.positionToPercentage
import mtree.core.utils.MeasurementUnitsUtils.videoDurationToString
import mtree.viewer.R

@Composable
internal fun FramePreview(
    videoUri: String,
    modifier: Modifier = Modifier,
    videoFramePercent: Double = 0.1
) {
    val context = LocalContext.current
    val frameKey = "$videoUri-$videoFramePercent" // to not recreate already rendered frames
    val imageRequest = remember(frameKey) {
        ImageRequest.Builder(context)
            .data(videoUri)
            .videoFramePercent(videoFramePercent)
            .dispatcher(Dispatchers.IO)
            .memoryCacheKey(frameKey)
            .diskCacheKey(frameKey)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    AsyncImage(
        model = imageRequest,
        contentDescription = null,
        modifier = modifier
    )
}

@Preview
@Composable
private fun FramePreviewPreview() {
    GalleryTheme {
        FramePreview("")
    }
}


@Composable
internal fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play)
        )
    }
}

@Preview
@Composable
private fun PlayPausePreview() {
    GalleryTheme {
        PlayPauseButton(
            isPlaying = true,
            onClick = { /*TODO*/ }
        )
    }
}


@Composable
internal fun Seekbar(
    position: Long,
    bufferedPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onSeekFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(position.videoDurationToString())
        Spacer(Modifier.size(4.dp))
        Seeker(
            value = positionToPercentage(position, duration),
            readAheadValue = positionToPercentage(bufferedPosition, duration),
            onValueChange = { onSeek(percentageToPosition(it, duration)) },
            onValueChangeFinished = { onSeekFinished(position) },
            colors = SeekerDefaults.seekerColors(
                progressColor = SliderDefaults.colors().activeTrackColor,
                disabledProgressColor = SliderDefaults.colors().disabledActiveTrackColor,
                thumbColor = SliderDefaults.colors().thumbColor,
                disabledThumbColor = SliderDefaults.colors().disabledThumbColor,
            ),
            modifier = Modifier.weight(1F)
        )
        Spacer(Modifier.size(4.dp))
        Text(duration.videoDurationToString())
    }
}

@Preview(showBackground = true)
@Composable
private fun SeekbarPreview() {
    var position by remember { mutableLongStateOf(2_000) }
    GalleryTheme {
        Seekbar(
            position = position,
            bufferedPosition = 40_000,
            duration = 10_000,
            onSeek = { position = it },
            onSeekFinished = {}
        )
    }
}
