package nikmax.gallery.gallery.explorer.components.drawables

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.ui.theme.GalleryTheme

val UndrawLock: ImageVector
    @Composable
    get() {
        if (_Lock != null) {
            return _Lock!!
        }
        _Lock = ImageVector.Builder(
            name = "Lock",
            defaultWidth = 598.38.dp,
            defaultHeight = 519.37.dp,
            viewportWidth = 598.38f,
            viewportHeight = 519.37f
        ).apply {
            path(
                fill = SolidColor(MaterialTheme.colorScheme.primaryContainer),
                strokeLineWidth = 2.62445f
            ) {
                moveTo(299.19f, 259.68f)
                moveToRelative(-254.4f, 0f)
                arcToRelative(254.4f, 254.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 508.79f, 0f)
                arcToRelative(254.4f, 254.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, -508.79f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFF3F3D56)),
                strokeLineWidth = 2.62445f
            ) {
                moveTo(298.84f, 312.1f)
                moveToRelative(-36.34f, 0f)
                arcToRelative(36.34f, 36.34f, 0f, isMoreThanHalf = true, isPositiveArc = true, 72.68f, 0f)
                arcToRelative(36.34f, 36.34f, 0f, isMoreThanHalf = true, isPositiveArc = true, -72.68f, 0f)
            }
            path(
                fill = SolidColor(MaterialTheme.colorScheme.primaryContainer),
                strokeLineWidth = 2.62445f
            ) {
                moveTo(298.84f, 312.1f)
                moveToRelative(-16.77f, 0f)
                arcToRelative(16.77f, 16.77f, 0f, isMoreThanHalf = true, isPositiveArc = true, 33.55f, 0f)
                arcToRelative(16.77f, 16.77f, 0f, isMoreThanHalf = true, isPositiveArc = true, -33.55f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2.62445f
            ) {
                moveTo(366.3f, 393f)
                lineTo(232.08f, 393f)
                arcToRelative(50.19f, 50.19f, 0f, isMoreThanHalf = false, isPositiveArc = true, -50.13f, -50.13f)
                verticalLineToRelative(-61.54f)
                arcToRelative(50.19f, 50.19f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50.13f, -50.13f)
                horizontalLineToRelative(134.22f)
                arcToRelative(50.19f, 50.19f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50.13f, 50.13f)
                verticalLineToRelative(61.54f)
                arcToRelative(50.19f, 50.19f, 0f, isMoreThanHalf = false, isPositiveArc = true, -50.13f, 50.13f)
                close()
                moveTo(232.08f, 236.45f)
                arcToRelative(44.93f, 44.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, -44.88f, 44.88f)
                verticalLineToRelative(61.54f)
                arcToRelative(44.93f, 44.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44.88f, 44.88f)
                horizontalLineToRelative(134.22f)
                arcToRelative(44.93f, 44.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44.88f, -44.88f)
                verticalLineToRelative(-61.54f)
                arcToRelative(44.93f, 44.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, -44.88f, -44.88f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3F3D56)),
                strokeLineWidth = 2.62445f
            ) {
                moveToRelative(224.13f, 359.59f)
                horizontalLineToRelative(119.14f)
                arcToRelative(47.09f, 47.09f, 0f, isMoreThanHalf = false, isPositiveArc = false, 47.09f, -47.09f)
                verticalLineToRelative(-51.5f)
                arcToRelative(46.88f, 46.88f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.11f, -16.71f)
                arcToRelative(46.96f, 46.96f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18.71f, 37.52f)
                verticalLineToRelative(51.5f)
                arcToRelative(47.09f, 47.09f, 0f, isMoreThanHalf = false, isPositiveArc = true, -47.09f, 47.09f)
                lineTo(239.74f, 380.4f)
                arcToRelative(47.06f, 47.06f, 0f, isMoreThanHalf = false, isPositiveArc = true, -43.98f, -30.38f)
                arcToRelative(46.82f, 46.82f, 0f, isMoreThanHalf = false, isPositiveArc = false, 28.37f, 9.57f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3F3D56)),
                strokeLineWidth = 2.62445f
            ) {
                moveToRelative(301.26f, 121.75f)
                curveToRelative(-24.88f, 0f, -46.75f, 11.26f, -59.14f, 28.17f)
                arcToRelative(76.43f, 76.43f, 0f, isMoreThanHalf = false, isPositiveArc = true, 45.59f, -14.63f)
                curveToRelative(38.53f, 0f, 69.88f, 26.99f, 69.88f, 60.17f)
                verticalLineToRelative(27.58f)
                horizontalLineToRelative(13.54f)
                verticalLineToRelative(-41.12f)
                curveToRelative(-0f, -33.18f, -31.35f, -60.17f, -69.88f, -60.17f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2.62445f
            ) {
                moveTo(380.44f, 237.15f)
                lineTo(217.94f, 237.15f)
                verticalLineToRelative(-47.81f)
                curveToRelative(0f, -38.57f, 36.45f, -69.96f, 81.25f, -69.96f)
                curveToRelative(44.8f, 0f, 81.25f, 31.38f, 81.25f, 69.96f)
                close()
                moveTo(223.19f, 231.9f)
                horizontalLineToRelative(152f)
                verticalLineToRelative(-42.56f)
                curveToRelative(0f, -35.68f, -34.09f, -64.71f, -76f, -64.71f)
                curveToRelative(-41.91f, 0f, -76f, 29.03f, -76f, 64.71f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2.62445f
            ) {
                moveTo(340.81f, 218.89f)
                lineTo(257.57f, 218.89f)
                arcTo(14.14f, 14.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 243.45f, 204.77f)
                verticalLineToRelative(-19.03f)
                curveToRelative(0f, -26.53f, 25.01f, -48.11f, 55.74f, -48.11f)
                curveToRelative(30.73f, 0f, 55.74f, 21.58f, 55.74f, 48.11f)
                verticalLineToRelative(19.03f)
                arcToRelative(14.14f, 14.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.12f, 14.12f)
                close()
                moveTo(299.19f, 142.88f)
                curveToRelative(-27.84f, 0f, -50.49f, 19.23f, -50.49f, 42.86f)
                verticalLineToRelative(19.03f)
                arcToRelative(8.88f, 8.88f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.87f, 8.88f)
                horizontalLineToRelative(83.23f)
                arcToRelative(8.88f, 8.88f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.87f, -8.88f)
                verticalLineToRelative(-19.03f)
                curveToRelative(0f, -23.64f, -22.65f, -42.86f, -50.49f, -42.86f)
                close()
            }
        }.build()

        return _Lock!!
    }

@Suppress("ObjectPropertyName")
private var _Lock: ImageVector? = null

@Preview
@Composable
private fun Preview() {
    GalleryTheme {
        Image(UndrawLock, "")
    }
}
