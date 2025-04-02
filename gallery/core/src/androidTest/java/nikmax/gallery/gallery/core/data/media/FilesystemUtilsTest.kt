package nikmax.gallery.gallery.core.data.media

import org.junit.Test
import kotlin.io.path.Path
import kotlin.io.path.isWritable

class FilesystemUtilsTest {
    
    val dcim = Path("/storage/emulated/0/DCIM/")
    val emulated = Path("/storage/emulated/")
    val djsfhksdhg = Path("djsfhksdhg")
    
    @Test
    fun writeCheck_ReturnsTrue_ForExistingUserspaceDirectories() {
        assert(dcim.isWritable())
    }
    
    @Test
    fun writeCheck_ReturnsFalse_ForRootDirectories() {
        assert(!emulated.isWritable())
    }
    
    @Test
    fun writeCheck_ReturnsFalse_ForNotExistingDirectory() {
        assert(!djsfhksdhg.isWritable())
    }
}
