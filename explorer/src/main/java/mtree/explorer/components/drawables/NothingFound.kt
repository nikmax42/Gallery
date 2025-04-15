package mtree.explorer.components.drawables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mtree.core.ui.theme.GalleryTheme

/* 
* Based on illustration from https://undraw.co/
*  */

val NothingFound: ImageVector
    @Composable
    @ReadOnlyComposable
    get() {
        //objects must be recreated each time to reflect theme or dynamic colors changes
        /* if (_IconName != null) {
            return _IconName!!
        } */
        _IconName = ImageVector.Builder(
            name = "NothingFound",
            defaultWidth = 368.dp,
            defaultHeight = 245.dp,
            viewportWidth = 368f,
            viewportHeight = 245f
        ).apply {
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(186.43f, 68.68f)
                curveTo(183.14f, 63.25f, 179.26f, 57.46f, 173.17f, 55.69f)
                curveTo(166.12f, 53.63f, 158.87f, 57.71f, 152.73f, 61.73f)
                curveTo(134.35f, 73.77f, 116.66f, 86.8f, 99.72f, 100.79f)
                lineTo(99.74f, 101f)
                curveTo(112.92f, 100.09f, 126.11f, 99.18f, 139.29f, 98.27f)
                curveTo(148.82f, 97.61f, 158.67f, 96.87f, 167.09f, 92.36f)
                curveTo(170.29f, 90.65f, 173.4f, 88.37f, 177.02f, 88.21f)
                curveTo(181.52f, 88.01f, 185.44f, 91.15f, 188.52f, 94.43f)
                curveTo(206.75f, 113.84f, 212.02f, 143.53f, 232.48f, 160.58f)
                curveTo(219.58f, 128.78f, 204.18f, 98.05f, 186.43f, 68.68f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(298.48f, 233.33f)
                curveTo(296.46f, 230.78f, 295.64f, 230.18f, 293.65f, 227.62f)
                curveTo(277.46f, 206.68f, 262.24f, 185.03f, 248f, 162.67f)
                curveTo(238.32f, 147.49f, 229.12f, 132.01f, 220.41f, 116.25f)
                curveTo(216.26f, 108.75f, 212.22f, 101.2f, 208.3f, 93.59f)
                curveTo(205.26f, 87.68f, 202.28f, 81.75f, 199.37f, 75.77f)
                curveTo(198.54f, 74.07f, 197.73f, 72.35f, 196.93f, 70.63f)
                curveTo(195.04f, 66.59f, 193.18f, 62.54f, 191.18f, 58.55f)
                curveTo(188.9f, 54.01f, 186.14f, 49.25f, 181.96f, 46.2f)
                curveTo(180.06f, 44.75f, 177.78f, 43.88f, 175.4f, 43.67f)
                curveTo(172f, 43.45f, 168.87f, 44.86f, 165.92f, 46.39f)
                curveTo(144.34f, 57.64f, 124.13f, 71.75f, 105.86f, 87.81f)
                curveTo(87.54f, 103.89f, 71.17f, 122.07f, 57.07f, 141.97f)
                curveTo(56.64f, 142.58f, 55.62f, 141.99f, 56.05f, 141.38f)
                curveTo(57.77f, 138.95f, 59.52f, 136.55f, 61.3f, 134.18f)
                curveTo(83.45f, 104.71f, 110.62f, 79.37f, 141.56f, 59.32f)
                curveTo(146.76f, 55.94f, 152.07f, 52.73f, 157.47f, 49.67f)
                curveTo(160.19f, 48.13f, 162.92f, 46.61f, 165.71f, 45.18f)
                curveTo(168.43f, 43.78f, 171.32f, 42.52f, 174.44f, 42.47f)
                curveTo(185.04f, 42.31f, 190.52f, 54.31f, 194.29f, 62.39f)
                curveTo(195.48f, 64.93f, 196.68f, 67.46f, 197.89f, 69.99f)
                curveTo(202.48f, 79.58f, 207.24f, 89.08f, 212.18f, 98.49f)
                curveTo(215.22f, 104.31f, 218.34f, 110.08f, 221.52f, 115.83f)
                curveTo(231.42f, 133.72f, 241.94f, 151.24f, 253.08f, 168.41f)
                curveTo(267.85f, 191.15f, 282.49f, 211.23f, 299.31f, 232.49f)
                curveTo(299.77f, 233.08f, 298.95f, 233.92f, 298.48f, 233.33f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(104.81f, 81.88f)
                curveTo(104.2f, 81.19f, 103.59f, 80.51f, 102.97f, 79.82f)
                curveTo(98.08f, 74.42f, 92.84f, 69.07f, 86.12f, 65.98f)
                curveTo(82.92f, 64.45f, 79.43f, 63.64f, 75.89f, 63.6f)
                curveTo(72.22f, 63.61f, 68.64f, 64.57f, 65.26f, 65.92f)
                curveTo(63.66f, 66.56f, 62.09f, 67.29f, 60.55f, 68.06f)
                curveTo(58.79f, 68.94f, 57.06f, 69.89f, 55.34f, 70.84f)
                curveTo(52.11f, 72.63f, 48.92f, 74.5f, 45.77f, 76.44f)
                curveTo(39.51f, 80.31f, 33.44f, 84.48f, 27.57f, 88.94f)
                curveTo(24.52f, 91.26f, 21.54f, 93.65f, 18.62f, 96.11f)
                curveTo(15.91f, 98.4f, 13.25f, 100.75f, 10.65f, 103.17f)
                curveTo(10.09f, 103.68f, 9.26f, 102.85f, 9.81f, 102.34f)
                curveTo(10.5f, 101.7f, 11.2f, 101.06f, 11.89f, 100.43f)
                curveTo(13.85f, 98.66f, 15.84f, 96.92f, 17.87f, 95.22f)
                curveTo(21.56f, 92.1f, 25.34f, 89.11f, 29.23f, 86.24f)
                curveTo(35.26f, 81.77f, 41.51f, 77.6f, 47.96f, 73.73f)
                curveTo(51.18f, 71.8f, 54.45f, 69.95f, 57.76f, 68.19f)
                curveTo(58.76f, 67.65f, 59.78f, 67.13f, 60.8f, 66.64f)
                curveTo(63.12f, 65.47f, 65.54f, 64.48f, 68.02f, 63.7f)
                curveTo(71.53f, 62.56f, 75.25f, 62.2f, 78.92f, 62.63f)
                curveTo(82.46f, 63.14f, 85.87f, 64.32f, 88.97f, 66.11f)
                curveTo(95.56f, 69.79f, 100.67f, 75.49f, 105.64f, 81.04f)
                curveTo(106.15f, 81.61f, 105.32f, 82.44f, 104.81f, 81.88f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(239.82f, 137.89f)
                lineTo(255.61f, 132.11f)
                lineTo(263.45f, 129.24f)
                curveTo(266.01f, 128.31f, 268.55f, 127.25f, 271.19f, 126.58f)
                curveTo(273.48f, 125.92f, 275.92f, 125.96f, 278.2f, 126.67f)
                curveTo(280.23f, 127.42f, 282.08f, 128.57f, 283.66f, 130.05f)
                curveTo(285.27f, 131.54f, 286.77f, 133.14f, 288.14f, 134.84f)
                curveTo(289.85f, 136.89f, 291.53f, 138.96f, 293.22f, 141.03f)
                curveTo(300.19f, 149.56f, 307.12f, 158.12f, 314f, 166.72f)
                curveTo(320.88f, 175.32f, 327.71f, 183.96f, 334.5f, 192.63f)
                curveTo(341.3f, 201.32f, 348.06f, 210.05f, 354.77f, 218.82f)
                curveTo(355.6f, 219.89f, 356.42f, 220.97f, 357.24f, 222.04f)
                curveTo(357.69f, 222.63f, 358.71f, 222.05f, 358.25f, 221.45f)
                curveTo(351.49f, 212.59f, 344.68f, 203.77f, 337.83f, 194.99f)
                curveTo(330.96f, 186.19f, 324.04f, 177.42f, 317.07f, 168.7f)
                curveTo(310.11f, 159.97f, 303.09f, 151.28f, 296.03f, 142.62f)
                curveTo(294.27f, 140.47f, 292.51f, 138.31f, 290.75f, 136.16f)
                curveTo(289.26f, 134.35f, 287.79f, 132.51f, 286.15f, 130.83f)
                curveTo(283.16f, 127.73f, 279.41f, 124.99f, 274.93f, 124.91f)
                curveTo(272.29f, 124.87f, 269.74f, 125.71f, 267.29f, 126.59f)
                curveTo(264.65f, 127.55f, 262.02f, 128.52f, 259.39f, 129.48f)
                lineTo(243.49f, 135.3f)
                lineTo(239.51f, 136.75f)
                curveTo(238.8f, 137.01f, 239.11f, 138.15f, 239.82f, 137.89f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(84.05f, 72.78f)
                curveTo(76.02f, 68.65f, 65.75f, 69.46f, 58.37f, 74.61f)
                curveTo(72.44f, 77.04f, 86.35f, 80.28f, 100.03f, 84.33f)
                curveTo(94.35f, 81f, 89.9f, 75.79f, 84.05f, 72.78f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF2F2F2))) {
                moveTo(58.29f, 74.6f)
                lineTo(56.75f, 75.85f)
                curveTo(57.27f, 75.41f, 57.82f, 75f, 58.37f, 74.61f)
                curveTo(58.35f, 74.61f, 58.32f, 74.6f, 58.29f, 74.6f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(283.07f, 137.95f)
                curveTo(281.51f, 136.05f, 279.83f, 134.07f, 277.51f, 133.3f)
                lineTo(275.34f, 133.39f)
                curveTo(291.86f, 162.58f, 314.17f, 188.1f, 340.91f, 208.35f)
                curveTo(321.63f, 184.88f, 302.35f, 161.41f, 283.07f, 137.95f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(148.1f, 122.34f)
                curveTo(149.1f, 125.99f, 151.36f, 129.16f, 154.49f, 131.3f)
                curveTo(155.69f, 132.13f, 157.12f, 132.91f, 157.53f, 134.31f)
                curveTo(157.75f, 135.21f, 157.62f, 136.15f, 157.16f, 136.95f)
                curveTo(156.7f, 137.73f, 156.15f, 138.46f, 155.51f, 139.1f)
                lineTo(155.45f, 139.32f)
                curveTo(152.45f, 137.55f, 149.6f, 135.3f, 147.95f, 132.24f)
                curveTo(146.3f, 129.18f, 146.07f, 125.16f, 148.1f, 122.34f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(247.78f, 209.19f)
                curveTo(248.79f, 212.84f, 251.05f, 216.01f, 254.17f, 218.16f)
                curveTo(255.38f, 218.98f, 256.81f, 219.77f, 257.22f, 221.17f)
                curveTo(257.44f, 222.06f, 257.3f, 223f, 256.85f, 223.8f)
                curveTo(256.39f, 224.58f, 255.83f, 225.31f, 255.19f, 225.95f)
                lineTo(255.13f, 226.17f)
                curveTo(252.14f, 224.4f, 249.29f, 222.15f, 247.64f, 219.09f)
                curveTo(245.98f, 216.03f, 245.75f, 212.02f, 247.78f, 209.19f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.secondary)) {
                moveTo(48.84f, 203.2f)
                curveTo(49.84f, 206.85f, 52.11f, 210.02f, 55.23f, 212.17f)
                curveTo(56.43f, 212.99f, 57.86f, 213.78f, 58.27f, 215.18f)
                curveTo(58.49f, 216.07f, 58.36f, 217.01f, 57.9f, 217.81f)
                curveTo(57.44f, 218.59f, 56.89f, 219.32f, 56.25f, 219.96f)
                lineTo(56.19f, 220.18f)
                curveTo(53.19f, 218.41f, 50.35f, 216.16f, 48.69f, 213.1f)
                curveTo(47.04f, 210.04f, 46.81f, 206.03f, 48.84f, 203.2f)
                close()
            }
            path(fill = SolidColor(MaterialTheme.colorScheme.primary)) {
                moveTo(277.78f, 43.64f)
                curveTo(289.83f, 43.64f, 299.6f, 33.87f, 299.6f, 21.82f)
                curveTo(299.6f, 9.77f, 289.83f, 0f, 277.78f, 0f)
                curveTo(265.73f, 0f, 255.96f, 9.77f, 255.96f, 21.82f)
                curveTo(255.96f, 33.87f, 265.73f, 43.64f, 277.78f, 43.64f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF0F0F0))) {
                moveTo(317.15f, 5.08f)
                curveTo(306.58f, 3.65f, 294.5f, 9.37f, 291.76f, 19.68f)
                curveTo(291.07f, 17.84f, 289.82f, 16.28f, 288.18f, 15.2f)
                curveTo(286.54f, 14.13f, 284.6f, 13.61f, 282.64f, 13.71f)
                curveTo(280.69f, 13.81f, 278.81f, 14.53f, 277.29f, 15.76f)
                curveTo(275.77f, 17f, 274.68f, 18.69f, 274.18f, 20.58f)
                lineTo(275.39f, 21.45f)
                curveTo(298.05f, 26.35f, 321.51f, 26.24f, 344.13f, 21.14f)
                curveTo(336.77f, 13.41f, 327.73f, 6.51f, 317.15f, 5.08f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF0F0F0))) {
                moveTo(271.8f, 34.17f)
                curveTo(261.23f, 32.74f, 249.15f, 38.46f, 246.41f, 48.77f)
                curveTo(245.72f, 46.94f, 244.47f, 45.37f, 242.83f, 44.3f)
                curveTo(241.19f, 43.22f, 239.25f, 42.7f, 237.29f, 42.8f)
                curveTo(235.34f, 42.9f, 233.46f, 43.62f, 231.94f, 44.86f)
                curveTo(230.42f, 46.09f, 229.33f, 47.78f, 228.83f, 49.67f)
                lineTo(230.04f, 50.54f)
                curveTo(252.7f, 55.44f, 276.16f, 55.34f, 298.78f, 50.23f)
                curveTo(291.42f, 42.5f, 282.38f, 35.6f, 271.8f, 34.17f)
                close()
            }
            path(fill = SolidColor(Color(0xFFCCCCCC))) {
                moveTo(364.1f, 39.67f)
                curveTo(364.05f, 39.67f, 364.01f, 39.67f, 363.97f, 39.65f)
                curveTo(250.85f, 3.88f, 151.14f, 8.56f, 87.42f, 18.78f)
                curveTo(78.75f, 20.17f, 70.05f, 21.78f, 61.58f, 23.56f)
                curveTo(59.41f, 24.01f, 57.17f, 24.49f, 54.9f, 25f)
                curveTo(52.2f, 25.6f, 49.53f, 26.22f, 46.98f, 26.84f)
                curveTo(45.87f, 27.1f, 44.79f, 27.37f, 43.73f, 27.63f)
                curveTo(42.12f, 28.03f, 40.49f, 28.44f, 38.74f, 28.9f)
                curveTo(36.78f, 29.4f, 34.8f, 29.93f, 32.83f, 30.47f)
                curveTo(32.83f, 30.47f, 32.82f, 30.47f, 32.81f, 30.48f)
                lineTo(32.81f, 30.48f)
                curveTo(30.6f, 31.08f, 28.39f, 31.7f, 26.26f, 32.33f)
                curveTo(25.1f, 32.66f, 23.99f, 32.99f, 22.92f, 33.31f)
                curveTo(22.8f, 33.35f, 22.7f, 33.38f, 22.59f, 33.41f)
                lineTo(22.36f, 33.48f)
                curveTo(22.23f, 33.52f, 22.09f, 33.56f, 21.96f, 33.6f)
                lineTo(21.96f, 33.6f)
                lineTo(21.96f, 33.6f)
                lineTo(21.61f, 33.71f)
                curveTo(21.19f, 33.83f, 20.79f, 33.96f, 20.39f, 34.08f)
                curveTo(9.89f, 37.32f, 4.12f, 39.62f, 4.06f, 39.64f)
                curveTo(4.01f, 39.66f, 3.95f, 39.67f, 3.9f, 39.67f)
                curveTo(3.84f, 39.67f, 3.79f, 39.66f, 3.73f, 39.64f)
                curveTo(3.68f, 39.62f, 3.64f, 39.58f, 3.6f, 39.54f)
                curveTo(3.56f, 39.5f, 3.53f, 39.46f, 3.51f, 39.4f)
                curveTo(3.48f, 39.35f, 3.47f, 39.3f, 3.47f, 39.24f)
                curveTo(3.47f, 39.18f, 3.49f, 39.13f, 3.51f, 39.08f)
                curveTo(3.53f, 39.03f, 3.56f, 38.98f, 3.6f, 38.94f)
                curveTo(3.64f, 38.9f, 3.69f, 38.87f, 3.74f, 38.85f)
                curveTo(3.8f, 38.83f, 9.6f, 36.51f, 20.14f, 33.26f)
                curveTo(20.54f, 33.14f, 20.94f, 33.02f, 21.36f, 32.89f)
                lineTo(21.68f, 32.79f)
                curveTo(21.69f, 32.79f, 21.7f, 32.78f, 21.72f, 32.78f)
                curveTo(21.85f, 32.74f, 21.98f, 32.7f, 22.11f, 32.66f)
                lineTo(22.34f, 32.59f)
                curveTo(22.45f, 32.56f, 22.57f, 32.52f, 22.68f, 32.49f)
                curveTo(23.74f, 32.17f, 24.86f, 31.84f, 26.02f, 31.51f)
                curveTo(28.15f, 30.88f, 30.36f, 30.26f, 32.58f, 29.65f)
                curveTo(32.59f, 29.65f, 32.59f, 29.65f, 32.6f, 29.65f)
                curveTo(34.57f, 29.11f, 36.56f, 28.58f, 38.52f, 28.07f)
                curveTo(40.28f, 27.62f, 41.91f, 27.2f, 43.52f, 26.8f)
                curveTo(44.59f, 26.54f, 45.67f, 26.27f, 46.78f, 26.01f)
                curveTo(49.34f, 25.39f, 52.01f, 24.77f, 54.72f, 24.17f)
                curveTo(56.99f, 23.66f, 59.23f, 23.17f, 61.4f, 22.72f)
                curveTo(69.89f, 20.94f, 78.6f, 19.33f, 87.29f, 17.94f)
                curveTo(151.1f, 7.7f, 250.94f, 3.01f, 364.23f, 38.84f)
                curveTo(364.32f, 38.87f, 364.41f, 38.93f, 364.46f, 39.02f)
                curveTo(364.52f, 39.11f, 364.54f, 39.21f, 364.52f, 39.31f)
                curveTo(364.51f, 39.41f, 364.45f, 39.5f, 364.38f, 39.57f)
                curveTo(364.3f, 39.64f, 364.2f, 39.67f, 364.1f, 39.67f)
                close()
            }
        }.build()
        
        return _IconName!!
    }

@Suppress("ObjectPropertyName")
private var _IconName: ImageVector? = null


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    GalleryTheme {
        Surface {
            Image(NothingFound, "")
        }
    }
}
