package nikmax.gallery.gallery.explorer.components.drawables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PaletteDisabled: ImageVector
    get() {
        _IconName = ImageVector.Builder(
            name = "PaletteDisabled",
            defaultWidth = 19.dp,
            defaultHeight = 18.dp,
            viewportWidth = 19f,
            viewportHeight = 18f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0.5f, 0f)
                    horizontalLineToRelative(18f)
                    verticalLineToRelative(18f)
                    horizontalLineToRelative(-18f)
                    close()
                }
            ) {
                path(
                    stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 2f
                ) {
                    moveTo(17.027f, 17.191f)
                    lineTo(1.277f, 0.69f)
                }
                path(fill = SolidColor(Color(0xFF1C1B1F))) {
                    moveTo(9.5f, 16.5f)
                    curveTo(8.475f, 16.5f, 7.506f, 16.303f, 6.594f, 15.909f)
                    curveTo(5.681f, 15.516f, 4.884f, 14.978f, 4.203f, 14.297f)
                    curveTo(3.522f, 13.616f, 2.984f, 12.819f, 2.591f, 11.906f)
                    curveTo(2.197f, 10.994f, 2f, 10.025f, 2f, 9f)
                    curveTo(2f, 7.963f, 2.203f, 6.988f, 2.609f, 6.075f)
                    curveTo(3.016f, 5.162f, 3.566f, 4.369f, 4.259f, 3.694f)
                    curveTo(4.953f, 3.019f, 5.762f, 2.484f, 6.688f, 2.091f)
                    curveTo(7.613f, 1.697f, 8.6f, 1.5f, 9.65f, 1.5f)
                    curveTo(10.65f, 1.5f, 11.594f, 1.672f, 12.481f, 2.016f)
                    curveTo(13.369f, 2.359f, 14.147f, 2.834f, 14.816f, 3.441f)
                    curveTo(15.484f, 4.047f, 16.016f, 4.766f, 16.409f, 5.597f)
                    curveTo(16.803f, 6.428f, 17f, 7.325f, 17f, 8.288f)
                    curveTo(17f, 9.725f, 16.563f, 10.828f, 15.688f, 11.597f)
                    curveTo(14.813f, 12.366f, 13.75f, 12.75f, 12.5f, 12.75f)
                    horizontalLineTo(11.113f)
                    curveTo(11f, 12.75f, 10.922f, 12.781f, 10.878f, 12.844f)
                    curveTo(10.834f, 12.906f, 10.813f, 12.975f, 10.813f, 13.05f)
                    curveTo(10.813f, 13.2f, 10.906f, 13.416f, 11.094f, 13.697f)
                    curveTo(11.281f, 13.978f, 11.375f, 14.3f, 11.375f, 14.663f)
                    curveTo(11.375f, 15.288f, 11.203f, 15.75f, 10.859f, 16.05f)
                    curveTo(10.516f, 16.35f, 10.063f, 16.5f, 9.5f, 16.5f)
                    close()
                    moveTo(5.375f, 9.75f)
                    curveTo(5.7f, 9.75f, 5.969f, 9.644f, 6.181f, 9.431f)
                    curveTo(6.394f, 9.219f, 6.5f, 8.95f, 6.5f, 8.625f)
                    curveTo(6.5f, 8.3f, 6.394f, 8.031f, 6.181f, 7.819f)
                    curveTo(5.969f, 7.606f, 5.7f, 7.5f, 5.375f, 7.5f)
                    curveTo(5.05f, 7.5f, 4.781f, 7.606f, 4.569f, 7.819f)
                    curveTo(4.356f, 8.031f, 4.25f, 8.3f, 4.25f, 8.625f)
                    curveTo(4.25f, 8.95f, 4.356f, 9.219f, 4.569f, 9.431f)
                    curveTo(4.781f, 9.644f, 5.05f, 9.75f, 5.375f, 9.75f)
                    close()
                    moveTo(7.625f, 6.75f)
                    curveTo(7.95f, 6.75f, 8.219f, 6.644f, 8.431f, 6.431f)
                    curveTo(8.644f, 6.219f, 8.75f, 5.95f, 8.75f, 5.625f)
                    curveTo(8.75f, 5.3f, 8.644f, 5.031f, 8.431f, 4.819f)
                    curveTo(8.219f, 4.606f, 7.95f, 4.5f, 7.625f, 4.5f)
                    curveTo(7.3f, 4.5f, 7.031f, 4.606f, 6.819f, 4.819f)
                    curveTo(6.606f, 5.031f, 6.5f, 5.3f, 6.5f, 5.625f)
                    curveTo(6.5f, 5.95f, 6.606f, 6.219f, 6.819f, 6.431f)
                    curveTo(7.031f, 6.644f, 7.3f, 6.75f, 7.625f, 6.75f)
                    close()
                    moveTo(11.375f, 6.75f)
                    curveTo(11.7f, 6.75f, 11.969f, 6.644f, 12.181f, 6.431f)
                    curveTo(12.394f, 6.219f, 12.5f, 5.95f, 12.5f, 5.625f)
                    curveTo(12.5f, 5.3f, 12.394f, 5.031f, 12.181f, 4.819f)
                    curveTo(11.969f, 4.606f, 11.7f, 4.5f, 11.375f, 4.5f)
                    curveTo(11.05f, 4.5f, 10.781f, 4.606f, 10.569f, 4.819f)
                    curveTo(10.356f, 5.031f, 10.25f, 5.3f, 10.25f, 5.625f)
                    curveTo(10.25f, 5.95f, 10.356f, 6.219f, 10.569f, 6.431f)
                    curveTo(10.781f, 6.644f, 11.05f, 6.75f, 11.375f, 6.75f)
                    close()
                    moveTo(13.625f, 9.75f)
                    curveTo(13.95f, 9.75f, 14.219f, 9.644f, 14.431f, 9.431f)
                    curveTo(14.644f, 9.219f, 14.75f, 8.95f, 14.75f, 8.625f)
                    curveTo(14.75f, 8.3f, 14.644f, 8.031f, 14.431f, 7.819f)
                    curveTo(14.219f, 7.606f, 13.95f, 7.5f, 13.625f, 7.5f)
                    curveTo(13.3f, 7.5f, 13.031f, 7.606f, 12.819f, 7.819f)
                    curveTo(12.606f, 8.031f, 12.5f, 8.3f, 12.5f, 8.625f)
                    curveTo(12.5f, 8.95f, 12.606f, 9.219f, 12.819f, 9.431f)
                    curveTo(13.031f, 9.644f, 13.3f, 9.75f, 13.625f, 9.75f)
                    close()
                    moveTo(9.5f, 15f)
                    curveTo(9.613f, 15f, 9.703f, 14.969f, 9.772f, 14.906f)
                    curveTo(9.841f, 14.844f, 9.875f, 14.762f, 9.875f, 14.663f)
                    curveTo(9.875f, 14.488f, 9.781f, 14.281f, 9.594f, 14.044f)
                    curveTo(9.406f, 13.806f, 9.313f, 13.45f, 9.313f, 12.975f)
                    curveTo(9.313f, 12.45f, 9.494f, 12.031f, 9.856f, 11.719f)
                    curveTo(10.219f, 11.406f, 10.663f, 11.25f, 11.188f, 11.25f)
                    horizontalLineTo(12.5f)
                    curveTo(13.325f, 11.25f, 14.031f, 11.009f, 14.619f, 10.528f)
                    curveTo(15.206f, 10.047f, 15.5f, 9.3f, 15.5f, 8.288f)
                    curveTo(15.5f, 6.775f, 14.922f, 5.516f, 13.766f, 4.509f)
                    curveTo(12.609f, 3.503f, 11.238f, 3f, 9.65f, 3f)
                    curveTo(7.95f, 3f, 6.5f, 3.581f, 5.3f, 4.744f)
                    curveTo(4.1f, 5.906f, 3.5f, 7.325f, 3.5f, 9f)
                    curveTo(3.5f, 10.663f, 4.084f, 12.078f, 5.253f, 13.247f)
                    curveTo(6.422f, 14.416f, 7.838f, 15f, 9.5f, 15f)
                    close()
                }
            }
        }.build()
        
        return _IconName!!
    }

@Suppress("ObjectPropertyName")
private var _IconName: ImageVector? = null
