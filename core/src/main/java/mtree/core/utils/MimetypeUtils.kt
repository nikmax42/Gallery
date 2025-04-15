package mtree.core.utils

import kotlin.io.path.Path
import kotlin.io.path.extension

object MimetypeUtils {
    private enum class Type(val prefix: String, val extension: String) {
        JPEG("image", "jpeg"),
        JPG("image", "jpg"),
        PNG("image", "png"),
        GIF("image", "gif"),
        BMP("image", "bmp"),
        WEBP("image", "webp"),
        TIFF("image", "tiff"),
        SVG("image", "svg"),
        
        MP4("video", "mp4"),
        AVI("video", "avi"),
        MKV("video", "mkv"),
        MOV("video", "mov"),
        WMV("video", "wmv"),
        FLV("video", "flv"),
        WEBM("video", "webm")
    }
    
    fun getFromPath(path: String): String? {
        return getFromExtension(Path(path).extension)
    }
    
    fun getFromExtension(extension: String): String? {
        return Type.entries
            .find { it.extension == extension }
            ?.let {
                "${it.prefix}/${it.extension}"
            }
    }
}
