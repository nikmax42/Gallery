package nikmax.gallery.gallery.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nikmax.gallery.gallery.core.R
import java.text.DecimalFormat

object MeasurementUnitsUtils {
    
    fun positionToPercentage(position: Long, duration: Long): Float {
        return position.toFloat() / duration.toFloat()
    }
    
    fun percentageToPosition(percentage: Float, duration: Long): Long {
        return (percentage * duration).toLong()
    }
    
    fun Long.videoDurationToString(): String {
        val totalSeconds = this / 1000
        val totalHours = totalSeconds / 3600
        val hours = "${totalSeconds / 3600}".padStart(2, '0')
        val minutes = "${(totalSeconds % 3600) / 60}".padStart(2, '0')
        val seconds = "${totalSeconds % 60}".padStart(2, '0')
        return if (totalHours > 0) "$hours:$minutes:$seconds"
        else "$minutes:$seconds"
    }
    
    @Composable
    fun Long.sizeToString(): String {
        return when {
            this < 1024 -> stringResource(R.string.b, this)
            this < 1024 * 1024 -> stringResource(R.string.kb, this.kilobytes())
            this < 1024 * 1024 * 1024 -> stringResource(R.string.mb, this.megabytes())
            else -> stringResource(R.string.gb, this.gigabytes())
        }
    }
    
    private fun Long.kilobytes(): String {
        return DecimalFormat("#.00").format(this / 1024.0)
    }
    
    private fun Long.megabytes(): String {
        return DecimalFormat("#.00").format(this / (1024.0 * 1024.0))
    }
    
    private fun Long.gigabytes(): String {
        return DecimalFormat("#.00").format(this / (1024.0 * 1024.0 * 1024.0))
    }
}
