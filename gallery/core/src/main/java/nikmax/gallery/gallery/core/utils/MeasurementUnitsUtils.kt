package nikmax.gallery.gallery.core.utils

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

    fun Long.sizeToString(): String {
        return when {
            this < 1024 -> "$this B"
            this < 1024 * 1024 -> String.format("%.2f KB", this / 1024.0)
            this < 1024 * 1024 * 1024 -> String.format("%.2f MB", this / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", this / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
