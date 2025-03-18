package nikmax.gallery.gallery.explorer.components.drawables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nikmax.gallery.core.ui.theme.GalleryTheme

val UndrawLockedMedia: ImageVector
    @Composable
    get() {
        if (_UndrawLockedMedia != null) {
            return _UndrawLockedMedia!!
        }
        _UndrawLockedMedia = ImageVector.Builder(
            name = "UndrawLockedMedia",
            defaultWidth = 571.55.dp,
            defaultHeight = 594.28.dp,
            viewportWidth = 571.55f,
            viewportHeight = 594.28f
        ).apply {
            path(fill = SolidColor(Color(0xFFF2F2F2))) {
                moveToRelative(571.48f, 123.13f)
                arcToRelative(12.13f, 12.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.72f, -12.2f)
                lineTo(340.32f, 1.23f)
                arcToRelative(12.1f, 12.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16.19f, 5.55f)
                lineToRelative(-0f, 0.01f)
                lineToRelative(-84.18f, 172.23f)
                arcToRelative(12.12f, 12.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.56f, 16.19f)
                lineTo(469.95f, 304.91f)
                arcToRelative(12.12f, 12.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16.19f, -5.56f)
                lineToRelative(84.18f, -172.23f)
                arcToRelative(12.01f, 12.01f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.16f, -3.99f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(439.99f, 505.4f)
                horizontalLineTo(148.77f)
                arcToRelative(13.84f, 13.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.83f, -13.83f)
                verticalLineTo(96.22f)
                arcToRelative(13.84f, 13.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.83f, -13.83f)
                horizontalLineToRelative(291.22f)
                arcToRelative(13.84f, 13.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.83f, 13.83f)
                verticalLineTo(491.57f)
                arcToRelative(13.84f, 13.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.83f, 13.83f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFE6E6E6)),
                strokeLineWidth = 1.64488f
            ) {
                moveTo(229.38f, 168.36f)
                moveToRelative(-31.25f, 0f)
                arcToRelative(31.25f, 31.25f, 0f, isMoreThanHalf = true, isPositiveArc = true, 62.51f, 0f)
                arcToRelative(31.25f, 31.25f, 0f, isMoreThanHalf = true, isPositiveArc = true, -62.51f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFFE6E6E6)),
                strokeLineWidth = 1.64488f
            ) {
                moveToRelative(181.75f, 253.78f)
                arcToRelative(89.53f, 89.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.82f, 6.37f)
                lineToRelative(0.84f, 0.29f)
                arcToRelative(90.08f, 90.08f, 0f, isMoreThanHalf = false, isPositiveArc = false, 29.95f, 5.09f)
                curveToRelative(2.05f, 0f, 4.03f, -0.07f, 5.88f, -0.21f)
                arcToRelative(89.05f, 89.05f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.36f, -1.84f)
                curveToRelative(2.42f, -0.53f, 4.82f, -1.16f, 7.15f, -1.87f)
                curveToRelative(0.56f, -0.17f, 1.09f, -0.33f, 1.66f, -0.51f)
                arcToRelative(90.77f, 90.77f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26.4f, -13.68f)
                arcToRelative(40.8f, 40.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.1f, -3.53f)
                arcToRelative(45.11f, 45.11f, 0f, isMoreThanHalf = false, isPositiveArc = false, -37.8f, -20.41f)
                horizontalLineToRelative(-16.45f)
                arcToRelative(45.26f, 45.26f, 0f, isMoreThanHalf = false, isPositiveArc = false, -41.16f, 26.46f)
                arcToRelative(41.1f, 41.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.53f, 3.85f)
                close()
            }
            path(fill = SolidColor(Color(0xFFCCCCCC))) {
                moveTo(439.99f, 505.4f)
                horizontalLineTo(148.77f)
                arcToRelative(13.84f, 13.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.83f, -13.83f)
                verticalLineTo(96.22f)
                arcToRelative(13.84f, 13.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.83f, -13.83f)
                horizontalLineToRelative(291.22f)
                arcToRelative(13.84f, 13.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.83f, 13.83f)
                verticalLineTo(491.57f)
                arcToRelative(13.84f, 13.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -13.83f, 13.83f)
                close()
                moveTo(148.77f, 84.02f)
                arcToRelative(12.22f, 12.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12.2f, 12.2f)
                verticalLineTo(491.57f)
                arcToRelative(12.22f, 12.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12.2f, 12.2f)
                horizontalLineToRelative(291.22f)
                arcToRelative(12.22f, 12.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12.2f, -12.2f)
                verticalLineTo(96.22f)
                arcToRelative(12.22f, 12.22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12.2f, -12.2f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveToRelative(561.48f, 236.13f)
                arcToRelative(12.13f, 12.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.72f, -12.2f)
                lineTo(330.32f, 114.23f)
                arcToRelative(12.1f, 12.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16.19f, 5.55f)
                lineToRelative(-0f, 0.01f)
                lineToRelative(-84.18f, 172.23f)
                arcToRelative(12.12f, 12.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.56f, 16.19f)
                lineTo(459.95f, 417.91f)
                arcToRelative(12.12f, 12.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16.19f, -5.56f)
                lineToRelative(84.18f, -172.23f)
                arcToRelative(12.01f, 12.01f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.16f, -3.99f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3F3D56))) {
                moveToRelative(561.48f, 236.13f)
                arcToRelative(12.13f, 12.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.72f, -12.2f)
                lineTo(330.32f, 114.23f)
                arcToRelative(12.1f, 12.1f, 0f, isMoreThanHalf = false, isPositiveArc = false, -16.19f, 5.55f)
                lineToRelative(-0f, 0.01f)
                lineToRelative(-84.18f, 172.23f)
                arcToRelative(12.12f, 12.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.56f, 16.19f)
                lineTo(459.95f, 417.91f)
                arcToRelative(12.12f, 12.12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16.19f, -5.56f)
                lineToRelative(84.18f, -172.23f)
                arcToRelative(12.01f, 12.01f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.16f, -3.99f)
                close()
                moveTo(474.86f, 411.73f)
                arcToRelative(10.69f, 10.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.29f, 4.91f)
                lineTo(236.13f, 306.93f)
                arcToRelative(10.69f, 10.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.91f, -14.29f)
                lineToRelative(84.18f, -172.23f)
                arcToRelative(10.69f, 10.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.29f, -4.91f)
                lineToRelative(224.44f, 109.7f)
                arcToRelative(10.69f, 10.69f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.91f, 14.29f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE6E6E6))) {
                moveTo(537f, 243.96f)
                lineTo(478.22f, 364.22f)
                arcToRelative(3.37f, 3.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.51f, 1.55f)
                horizontalLineToRelative(-0f)
                lineToRelative(-204.16f, -99.78f)
                arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.42f, -0.25f)
                arcToRelative(3.37f, 3.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.13f, -4.26f)
                lineToRelative(58.78f, -120.27f)
                arcToRelative(3.37f, 3.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.51f, -1.55f)
                verticalLineToRelative(0f)
                lineToRelative(204.16f, 99.78f)
                arcToRelative(3.37f, 3.37f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.55f, 4.51f)
                verticalLineToRelative(0f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.primaryContainer)) {
                moveTo(357.76f, 208.28f)
                moveToRelative(-29.57f, 0f)
                arcToRelative(29.57f, 29.57f, 0f, isMoreThanHalf = true, isPositiveArc = true, 59.13f, 0f)
                arcToRelative(29.57f, 29.57f, 0f, isMoreThanHalf = true, isPositiveArc = true, -59.13f, 0f)
            }
            path(fill = SolidColor(Color(0xFF3F3D56))) {
                moveTo(401.05f, 330.86f)
                lineTo(270.74f, 267.17f)
                arcToRelative(2.95f, 2.95f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.47f, -0.28f)
                lineToRelative(113.81f, -76.63f)
                arcToRelative(4.8f, 4.8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.51f, 3.67f)
                lineToRelative(6.35f, 91.86f)
                lineToRelative(0.31f, 4.4f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(393.9f, 288.06f)
                lineToRelative(2.69f, -1.52f)
                lineToRelative(1.34f, -0.76f)
                lineToRelative(0.31f, 4.4f)
                lineToRelative(2.81f, 40.67f)
                lineToRelative(-44.49f, -21.75f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3F3D56))) {
                moveToRelative(473.61f, 366.32f)
                lineToRelative(-112.71f, -55.09f)
                lineToRelative(37.35f, -21.05f)
                lineToRelative(2.69f, -1.52f)
                lineToRelative(48.67f, -27.43f)
                curveToRelative(3.19f, -1.8f, 7.76f, 0.23f, 8.8f, 3.67f)
                arcToRelative(5.72f, 5.72f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.16f, 0.71f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(526.46f, 594.28f)
                horizontalLineTo(33f)
                arcToRelative(33.04f, 33.04f, 0f, isMoreThanHalf = false, isPositiveArc = true, -33f, -33f)
                verticalLineToRelative(-271f)
                arcToRelative(33.04f, 33.04f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33f, -33f)
                horizontalLineToRelative(60.5f)
                arcToRelative(32.92f, 32.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.2f, 8.58f)
                lineToRelative(65.61f, 46.85f)
                arcToRelative(23.18f, 23.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.66f, 6.07f)
                horizontalLineToRelative(332.56f)
                arcToRelative(33f, 33f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33f, 33.48f)
                lineToRelative(-3.06f, 209.5f)
                arcToRelative(33.14f, 33.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, -33f, 32.52f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF2F2F2))) {
                moveTo(522.08f, 578.28f)
                horizontalLineTo(35.92f)
                arcTo(15.94f, 15.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 562.36f)
                verticalLineTo(286.93f)
                arcToRelative(15.94f, 15.94f, 0f, isMoreThanHalf = false, isPositiveArc = true, 15.92f, -15.92f)
                arcToRelative(14.1f, 14.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.08f, 14.08f)
                verticalLineToRelative(258.27f)
                arcToRelative(14.94f, 14.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 14.92f, 14.92f)
                horizontalLineToRelative(455.52f)
                arcToRelative(10.13f, 10.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.92f, 7.59f)
                arcToRelative(9.72f, 9.72f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.31f, 11.18f)
                arcToRelative(7.6f, 7.6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.97f, 1.23f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3F3D56))) {
                moveTo(526.46f, 594.28f)
                horizontalLineTo(33f)
                arcToRelative(33.04f, 33.04f, 0f, isMoreThanHalf = false, isPositiveArc = true, -33f, -33f)
                verticalLineToRelative(-271f)
                arcToRelative(33.04f, 33.04f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33f, -33f)
                horizontalLineToRelative(60.5f)
                arcToRelative(32.92f, 32.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.2f, 8.58f)
                lineToRelative(65.61f, 46.85f)
                arcToRelative(23.18f, 23.18f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.66f, 6.07f)
                horizontalLineToRelative(332.56f)
                arcToRelative(33f, 33f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33f, 33.48f)
                lineToRelative(-3.06f, 209.5f)
                arcTo(33.14f, 33.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 526.46f, 594.28f)
                close()
                moveTo(33f, 259.28f)
                arcToRelative(31.04f, 31.04f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31f, 31f)
                verticalLineToRelative(271f)
                arcToRelative(31.04f, 31.04f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31f, 31f)
                horizontalLineToRelative(493.46f)
                arcToRelative(31.13f, 31.13f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31f, -30.55f)
                lineToRelative(3.06f, -209.5f)
                arcTo(31f, 31f, 0f, isMoreThanHalf = false, isPositiveArc = false, 529.53f, 320.78f)
                horizontalLineTo(196.97f)
                arcToRelative(25.18f, 25.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, -16.96f, -6.55f)
                lineToRelative(-65.61f, -46.85f)
                arcToRelative(30.93f, 30.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, -20.89f, -8.1f)
                close()
            }
            path(
                fill = SolidColor(MaterialTheme.colorScheme.primaryContainer),
                strokeLineWidth = 1.1944f
            ) {
                moveTo(281.26f, 440.57f)
                moveToRelative(-81.57f, 0f)
                arcToRelative(81.57f, 81.57f, 0f, isMoreThanHalf = true, isPositiveArc = true, 163.14f, 0f)
                arcToRelative(81.57f, 81.57f, 0f, isMoreThanHalf = true, isPositiveArc = true, -163.14f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 1.1944f
            ) {
                moveToRelative(303.86f, 435.38f)
                verticalLineToRelative(-15.61f)
                arcToRelative(22.6f, 22.6f, 0f, isMoreThanHalf = true, isPositiveArc = false, -45.2f, 0f)
                verticalLineToRelative(15.61f)
                arcToRelative(11.75f, 11.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, -11.22f, 11.73f)
                verticalLineToRelative(36.88f)
                horizontalLineToRelative(67.64f)
                lineTo(315.09f, 447.11f)
                arcToRelative(11.75f, 11.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, -11.22f, -11.73f)
                close()
                moveTo(281.26f, 405.13f)
                arcToRelative(14.66f, 14.66f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.64f, 14.64f)
                verticalLineToRelative(15.58f)
                horizontalLineToRelative(-29.28f)
                verticalLineToRelative(-15.58f)
                arcToRelative(14.66f, 14.66f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.64f, -14.64f)
                close()
            }
        }.build()

        return _UndrawLockedMedia!!
    }

@Suppress("ObjectPropertyName")
private var _UndrawLockedMedia: ImageVector? = null

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    GalleryTheme {
        Surface {
            Image(UndrawLockedMedia, null)
        }
    }
}
